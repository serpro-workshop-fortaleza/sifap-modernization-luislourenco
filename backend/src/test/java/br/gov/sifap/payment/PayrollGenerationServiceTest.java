package br.gov.sifap.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.sifap.audit.AuditEvent;
import br.gov.sifap.audit.AuditRecorder;
import br.gov.sifap.beneficiary.BeneficiaryQuery;
import br.gov.sifap.beneficiary.BeneficiarySnapshot;
import br.gov.sifap.benefitcalculation.BenefitCalculator;
import br.gov.sifap.benefitcalculation.DiscountCalculator;
import br.gov.sifap.benefitcalculation.EligibilityService;
import br.gov.sifap.shared.ReferencePeriod;
import br.gov.sifap.socialprogram.ProgramStatus;
import br.gov.sifap.socialprogram.SocialProgramQuery;
import br.gov.sifap.testing.Fixtures;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PayrollGenerationServiceTest {

    private static final ReferencePeriod PERIOD = ReferencePeriod.of(202601);

    @Mock private BeneficiaryQuery beneficiaryQuery;
    @Mock private SocialProgramQuery socialProgramQuery;
    @Mock private PaymentRepository paymentRepository;
    @Mock private RemittanceWriter remittanceWriter;
    @Mock private AuditRecorder auditRecorder;

    private PayrollGenerationService payrollGenerationService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-05T10:00:00Z"), ZoneOffset.UTC);
        payrollGenerationService = new PayrollGenerationService(
                beneficiaryQuery,
                socialProgramQuery,
                new EligibilityService(),
                new BenefitCalculator(new DiscountCalculator()),
                paymentRepository,
                remittanceWriter,
                auditRecorder,
                clock);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(call -> call.getArgument(0));
        when(paymentRepository.existsByCpfAndReferencePeriod(anyString(), anyInt())).thenReturn(false);
        when(socialProgramQuery.findByCode(anyString()))
                .thenReturn(Optional.of(Fixtures.program().build()));
    }

    @Test
    @DisplayName("REQ-028 - gera exatamente um pagamento por beneficiario elegivel")
    void should_generate_one_payment_per_eligible_beneficiary() {
        givenActiveBeneficiaries(Fixtures.beneficiary().build());

        PayrollOutcome outcome = payrollGenerationService.generate(PERIOD);

        assertThat(outcome.generated()).isEqualTo(1);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("REQ-028 - o pagamento nasce com status GERADO")
    void should_create_payment_with_generated_status() {
        givenActiveBeneficiaries(Fixtures.beneficiary().build());

        payrollGenerationService.generate(PERIOD);

        var captor = org.mockito.ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(PaymentStatus.GENERATED);
    }

    @Test
    @DisplayName("REQ-002 - nao regera pagamento ja existente para o periodo")
    void should_skip_beneficiary_when_payment_already_exists() {
        givenActiveBeneficiaries(Fixtures.beneficiary().build());
        when(paymentRepository.existsByCpfAndReferencePeriod(anyString(), anyInt())).thenReturn(true);

        PayrollOutcome outcome = payrollGenerationService.generate(PERIOD);

        assertThat(outcome.skipped()).isEqualTo(1);
        assertThat(outcome.generated()).isZero();
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("REQ-006 e REQ-030 - CPF invalido e rejeitado e o ciclo prossegue")
    void should_reject_invalid_cpf_and_continue() {
        givenActiveBeneficiaries(
                Fixtures.beneficiary().cpf("11111111111").build(),
                Fixtures.beneficiary().cpf(Fixtures.VALID_CPF).build());

        PayrollOutcome outcome = payrollGenerationService.generate(PERIOD);

        assertThat(outcome.rejected()).hasSize(1);
        assertThat(outcome.rejected().getFirst().reason())
                .isEqualTo(PayrollOutcome.RejectionReason.INVALID_CPF);
        assertThat(outcome.generated()).isEqualTo(1);
    }

    @Test
    @DisplayName("REQ-030 - programa inexistente rejeita o beneficiario")
    void should_reject_beneficiary_when_program_not_found() {
        givenActiveBeneficiaries(Fixtures.beneficiary().build());
        when(socialProgramQuery.findByCode(anyString())).thenReturn(Optional.empty());

        PayrollOutcome outcome = payrollGenerationService.generate(PERIOD);

        assertThat(outcome.rejected()).hasSize(1);
        assertThat(outcome.rejected().getFirst().reason())
                .isEqualTo(PayrollOutcome.RejectionReason.PROGRAM_NOT_FOUND);
    }

    @Test
    @DisplayName("REQ-008 - programa inativo apenas ignora, nao rejeita")
    void should_skip_beneficiary_when_program_is_inactive() {
        givenActiveBeneficiaries(Fixtures.beneficiary().build());
        when(socialProgramQuery.findByCode(anyString()))
                .thenReturn(Optional.of(Fixtures.program().status(ProgramStatus.INACTIVE).build()));

        PayrollOutcome outcome = payrollGenerationService.generate(PERIOD);

        assertThat(outcome.skipped()).isEqualTo(1);
        assertThat(outcome.rejected()).isEmpty();
    }

    @Test
    @DisplayName("REQ-031 - o resultado expoe somente o documento mascarado")
    void should_mask_cpf_in_rejection_output() {
        givenActiveBeneficiaries(Fixtures.beneficiary().cpf("11111111111").build());

        PayrollOutcome outcome = payrollGenerationService.generate(PERIOD);

        assertThat(outcome.rejected().getFirst().maskedCpf())
                .startsWith("***.")
                .doesNotContain("11111111111");
    }

    @Test
    @DisplayName("REQ-015 - regiao 99 vira rejeicao explicita, nao pagamento silencioso")
    void should_reject_beneficiary_when_legacy_rule_is_unresolved() {
        givenActiveBeneficiaries(Fixtures.beneficiary().regionCode(99).build());

        PayrollOutcome outcome = payrollGenerationService.generate(PERIOD);

        assertThat(outcome.rejected()).hasSize(1);
        assertThat(outcome.rejected().getFirst().reason())
                .isEqualTo(PayrollOutcome.RejectionReason.UNRESOLVED_LEGACY_RULE);
        assertThat(outcome.rejected().getFirst().detail()).contains("SIFAP-M-09");
    }

    @Test
    @DisplayName("REQ-005 - desfecho COMPLETED quando nao ha rejeitados")
    void should_report_completed_when_no_rejections() {
        givenActiveBeneficiaries(Fixtures.beneficiary().build());

        assertThat(payrollGenerationService.generate(PERIOD).outcome())
                .isEqualTo(PayrollOutcome.Outcome.COMPLETED);
    }

    @Test
    @DisplayName("REQ-005 - desfecho COMPLETED_WITH_REJECTIONS quando ha rejeitados")
    void should_report_completed_with_rejections() {
        givenActiveBeneficiaries(
                Fixtures.beneficiary().cpf(Fixtures.VALID_CPF).build(),
                Fixtures.beneficiary().cpf("11111111111").build());

        assertThat(payrollGenerationService.generate(PERIOD).outcome())
                .isEqualTo(PayrollOutcome.Outcome.COMPLETED_WITH_REJECTIONS);
    }

    @Test
    @DisplayName("REQ-005 - desfecho NOTHING_TO_PROCESS quando nada e gerado")
    void should_report_nothing_to_process_when_no_payment_generated() {
        givenActiveBeneficiaries();

        assertThat(payrollGenerationService.generate(PERIOD).outcome())
                .isEqualTo(PayrollOutcome.Outcome.NOTHING_TO_PROCESS);
    }

    @Test
    @DisplayName("REQ-029 - grava o arquivo de remessa do ciclo")
    void should_write_remittance_file() {
        givenActiveBeneficiaries(Fixtures.beneficiary().build());

        payrollGenerationService.generate(PERIOD);

        verify(remittanceWriter).write(any(ReferencePeriod.class), any());
    }

    @Test
    @DisplayName("REQ-004 - registra a trilha do ciclo com acao BT")
    void should_record_audit_event_for_the_cycle() {
        givenActiveBeneficiaries(Fixtures.beneficiary().build());

        payrollGenerationService.generate(PERIOD);

        var captor = org.mockito.ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRecorder).record(captor.capture());
        assertThat(captor.getValue().action()).isEqualTo(br.gov.sifap.audit.AuditAction.BATCH);
        assertThat(captor.getValue().entityType()).isEqualTo("PGTO");
        assertThat(captor.getValue().entityId()).isEqualTo("202601");
    }

    @Test
    @DisplayName("REQ-004 - registra a trilha mesmo quando nada e gerado")
    void should_record_audit_event_when_nothing_is_generated() {
        givenActiveBeneficiaries();

        payrollGenerationService.generate(PERIOD);

        verify(auditRecorder).record(any(AuditEvent.class));
    }

    @Test
    @DisplayName("REQ-031 - a trilha do ciclo nao carrega documento")
    void should_not_include_cpf_in_cycle_audit_event() {
        givenActiveBeneficiaries(Fixtures.beneficiary().build());

        payrollGenerationService.generate(PERIOD);

        var captor = org.mockito.ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRecorder).record(captor.capture());
        assertThat(captor.getValue().maskedCpf()).isNull();
        assertThat(captor.getValue().description()).doesNotContain(Fixtures.VALID_CPF);
    }

    private void givenActiveBeneficiaries(BeneficiarySnapshot... beneficiaries) {
        when(beneficiaryQuery.findActiveOrderedByCpf()).thenReturn(List.of(beneficiaries));
    }
}
