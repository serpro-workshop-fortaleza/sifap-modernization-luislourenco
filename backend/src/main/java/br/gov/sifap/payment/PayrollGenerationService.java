package br.gov.sifap.payment;

import br.gov.sifap.audit.AuditEvent;
import br.gov.sifap.audit.AuditRecorder;
import br.gov.sifap.beneficiary.BeneficiaryQuery;
import br.gov.sifap.beneficiary.BeneficiarySnapshot;
import br.gov.sifap.benefitcalculation.BenefitCalculation;
import br.gov.sifap.benefitcalculation.BenefitCalculator;
import br.gov.sifap.benefitcalculation.EligibilityResult;
import br.gov.sifap.benefitcalculation.EligibilityService;
import br.gov.sifap.shared.Cpf;
import br.gov.sifap.shared.ReferencePeriod;
import br.gov.sifap.shared.UnresolvedLegacyRuleException;
import br.gov.sifap.socialprogram.ProgramRules;
import br.gov.sifap.socialprogram.SocialProgramQuery;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gera a folha de pagamento de um periodo.
 *
 * <p>Traducao de {@code BATCHPGT.NSP}. Cobre REQ-001 a REQ-005 e REQ-028 a REQ-031.
 *
 * <p>Diferenca estrutural: o calculo e delegado a {@link BenefitCalculator}, que nao
 * persiste. O legado chama {@code CALCBENF} e mantem a mesma formula inline, gravando o
 * pagamento duas vezes ({@code SIFAP-M-05}); ver ADR-002.
 */
@Service
public class PayrollGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PayrollGenerationService.class);

    private final BeneficiaryQuery beneficiaryQuery;
    private final SocialProgramQuery socialProgramQuery;
    private final EligibilityService eligibilityService;
    private final BenefitCalculator benefitCalculator;
    private final PaymentRepository paymentRepository;
    private final RemittanceWriter remittanceWriter;
    private final AuditRecorder auditRecorder;
    private final Clock clock;

    PayrollGenerationService(
            BeneficiaryQuery beneficiaryQuery,
            SocialProgramQuery socialProgramQuery,
            EligibilityService eligibilityService,
            BenefitCalculator benefitCalculator,
            PaymentRepository paymentRepository,
            RemittanceWriter remittanceWriter,
            AuditRecorder auditRecorder,
            Clock clock) {
        this.beneficiaryQuery = beneficiaryQuery;
        this.socialProgramQuery = socialProgramQuery;
        this.eligibilityService = eligibilityService;
        this.benefitCalculator = benefitCalculator;
        this.paymentRepository = paymentRepository;
        this.remittanceWriter = remittanceWriter;
        this.auditRecorder = auditRecorder;
        this.clock = clock;
    }

    @Transactional
    public PayrollOutcome generate(ReferencePeriod period) {
        LocalDate processingDate = LocalDate.now(clock);
        List<PayrollOutcome.RejectedBeneficiary> rejected = new ArrayList<>();
        List<RemittanceRecord> remittance = new ArrayList<>();

        int processed = 0;
        int generated = 0;
        int skipped = 0;

        // REQ-003 — somente ativos, na ordem de CPF que os sistemas a jusante esperam.
        for (BeneficiarySnapshot beneficiary : beneficiaryQuery.findActiveOrderedByCpf()) {
            processed++;

            Cpf cpf;
            try {
                cpf = beneficiary.validatedCpf(); // REQ-006
            } catch (Cpf.InvalidCpfException ex) {
                // REQ-030 — rejeita e prossegue. REQ-031 — nao registra o documento completo.
                rejected.add(new PayrollOutcome.RejectedBeneficiary(
                        maskUnvalidated(beneficiary.rawCpf()),
                        PayrollOutcome.RejectionReason.INVALID_CPF,
                        ex.getMessage()));
                continue;
            }

            // REQ-002 — nao regera pagamento ja existente para o par CPF + periodo.
            if (paymentRepository.existsByCpfAndReferencePeriod(cpf.unmasked(), period.toYyyymm())) {
                skipped++;
                continue;
            }

            Optional<ProgramRules> program = socialProgramQuery.findByCode(beneficiary.programCode());
            if (program.isEmpty()) {
                // REQ-030 — programa inexistente rejeita ({@code BATCHPGT.NSP:307-318}).
                rejected.add(new PayrollOutcome.RejectedBeneficiary(
                        cpf.masked(),
                        PayrollOutcome.RejectionReason.PROGRAM_NOT_FOUND,
                        "Programa " + beneficiary.programCode() + " nao encontrado"));
                continue;
            }

            try {
                // REQ-008 a REQ-014 — programa inativo apenas ignora, nao rejeita.
                EligibilityResult eligibility =
                        eligibilityService.assess(beneficiary, program.get(), period);
                if (!eligibility.eligible()) {
                    skipped++;
                    continue;
                }

                // REQ-016 a REQ-027 — descontos do recorte 001 vem vazios; o cadastro de
                // descontos por pagamento pertence ao recorte seguinte.
                BenefitCalculation calculation = benefitCalculator.calculate(
                        beneficiary, program.get(), period, List.of(), processingDate);

                // REQ-028 — exatamente um pagamento, status inicial GERADO.
                Payment payment = paymentRepository.save(
                        Payment.generate(cpf, beneficiary.programCode(), period, calculation, processingDate));

                // REQ-029 — um registro de remessa por pagamento gerado.
                remittance.add(RemittanceRecord.from(payment));
                generated++;

            } catch (UnresolvedLegacyRuleException ex) {
                rejected.add(new PayrollOutcome.RejectedBeneficiary(
                        cpf.masked(),
                        PayrollOutcome.RejectionReason.UNRESOLVED_LEGACY_RULE,
                        ex.getMessage()));
            }
        }

        remittanceWriter.write(period, remittance);

        PayrollOutcome.Outcome outcome = resolveOutcome(generated, rejected.size());

        // REQ-004 - trilha do ciclo com acao BT, mesmo quando nada e gerado.
        auditRecorder.record(AuditEvent.payrollCycle(
                period.toString(),
                "Folha gerada periodo %s - pagamentos %d, ignorados %d, rejeitados %d"
                        .formatted(period, generated, skipped, rejected.size())));

        // REQ-031 - o log traz contadores e periodo, nunca documento ou valor individual.
        log.info(
                "folha gerada periodo={} processados={} gerados={} ignorados={} rejeitados={} desfecho={}",
                period, processed, generated, skipped, rejected.size(), outcome);

        return new PayrollOutcome(period, outcome, processed, generated, skipped, rejected);
    }

    /** REQ-005 — desfechos equivalentes aos RC 0, 4 e 8 do legado. */
    private PayrollOutcome.Outcome resolveOutcome(int generated, int rejectedCount) {
        if (generated == 0) {
            return PayrollOutcome.Outcome.NOTHING_TO_PROCESS;
        }
        return rejectedCount > 0
                ? PayrollOutcome.Outcome.COMPLETED_WITH_REJECTIONS
                : PayrollOutcome.Outcome.COMPLETED;
    }

    /** REQ-031 — mascara um documento que nao pode ser convertido em {@link Cpf}. */
    private String maskUnvalidated(String rawCpf) {
        if (rawCpf == null || rawCpf.length() != 11) {
            return "***.***.***-**";
        }
        return "***.%s.%s-%s".formatted(
                rawCpf.substring(3, 6), rawCpf.substring(6, 9), rawCpf.substring(9, 11));
    }
}
