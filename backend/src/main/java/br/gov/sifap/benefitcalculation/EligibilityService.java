package br.gov.sifap.benefitcalculation;

import br.gov.sifap.beneficiary.BeneficiarySnapshot;
import br.gov.sifap.shared.Money;
import br.gov.sifap.shared.ReferencePeriod;
import br.gov.sifap.shared.UnresolvedLegacyRuleException;
import br.gov.sifap.socialprogram.ProgramRules;
import br.gov.sifap.socialprogram.ProgramType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Avalia a elegibilidade de um beneficiario a um programa social.
 *
 * <p>Traducao de {@code VALELEG.NSN}. Funcao pura: nao acessa banco e nao persiste.
 *
 * <p>Cobre REQ-008 a REQ-014. REQ-015 (regiao 99) permanece bloqueado por
 * {@code SIFAP-M-09} e falha de forma visivel.
 */
@Service
public class EligibilityService {

    /** Regiao internacional/diplomatica. {@code VALELEG.NSN:123-129}. */
    static final int SPECIAL_REGION = 99;

    /** {@code VALELEG.NSN:199} — limite de renda do ramo assistencial. */
    private static final Money ASSISTANCE_INCOME_THRESHOLD = Money.of("600.00");

    private static final int SOCIAL_SECURITY_MINIMUM_AGE = 60;
    private static final int EMPLOYMENT_MINIMUM_AGE = 16;
    private static final int EMPLOYMENT_MAXIMUM_AGE = 65;

    public EligibilityResult assess(
            BeneficiarySnapshot beneficiary, ProgramRules program, ReferencePeriod period) {

        // REQ-008 — programa inativo interrompe a avaliacao antes de qualquer outra regra.
        if (!program.status().isActive()) {
            return EligibilityResult.rejected(List.of(IneligibilityReason.PROGRAM_NOT_ACTIVE));
        }

        rejectUnresolvedSpecialRegion(beneficiary);

        List<IneligibilityReason> reasons = new ArrayList<>();
        int age = beneficiary.ageOn(period);

        appendStatusReason(beneficiary, reasons);
        appendAgeRangeReasons(program, age, reasons);
        appendIncomeCeilingReason(beneficiary, program, reasons);
        appendProgramTypeReasons(beneficiary, program, age, reasons);
        appendEligibilityCodeReasons(beneficiary, program, reasons);

        return reasons.isEmpty() ? EligibilityResult.approved() : EligibilityResult.rejected(reasons);
    }

    /**
     * REQ-015 (bloqueado por SIFAP-M-09).
     *
     * <p>No legado, {@code VALELEG.NSN:123-129} concede elegibilidade a regiao 99 antes de
     * verificar status, idade e renda — um beneficiario cancelado passaria. Nao reproduzimos
     * esse desvio sem confirmacao escrita da Coordenacao de Beneficios.
     */
    private void rejectUnresolvedSpecialRegion(BeneficiarySnapshot beneficiary) {
        if (beneficiary.regionCode() == SPECIAL_REGION) {
            throw new UnresolvedLegacyRuleException(
                    "SIFAP-M-09",
                    "Regiao 99 concede elegibilidade sem verificar status, idade e renda "
                            + "(VALELEG.NSN:123-129)");
        }
    }

    /** REQ-003 — {@code VALELEG.NSN:134-158}. */
    private void appendStatusReason(BeneficiarySnapshot beneficiary, List<IneligibilityReason> reasons) {
        switch (beneficiary.status()) {
            case ACTIVE -> { /* elegivel quanto ao status */ }
            case SUSPENDED -> reasons.add(IneligibilityReason.BENEFICIARY_SUSPENDED);
            case CANCELED, DISCONNECTED -> reasons.add(IneligibilityReason.BENEFICIARY_CANCELED);
            case INACTIVE -> reasons.add(IneligibilityReason.BENEFICIARY_INACTIVE);
        }
    }

    /** REQ-009 — limite zero significa "sem restricao". */
    private void appendAgeRangeReasons(ProgramRules program, int age, List<IneligibilityReason> reasons) {
        if (program.hasMinimumAge() && age < program.minimumAge()) {
            reasons.add(IneligibilityReason.AGE_BELOW_PROGRAM_MINIMUM);
        }
        if (program.hasMaximumAge() && age > program.maximumAge()) {
            reasons.add(IneligibilityReason.AGE_ABOVE_PROGRAM_MAXIMUM);
        }
    }

    /**
     * REQ-026 do catalogo — {@code VALELEG.NSN:183-191}.
     *
     * <p>O campo do DDM e {@code MAX-PERCAP-INCOME} (renda per capita) mas o legado compara
     * com a renda familiar total. Divergencia registrada em {@code SIFAP-M-10}; preservamos o
     * comportamento observado ate haver decisao.
     */
    private void appendIncomeCeilingReason(
            BeneficiarySnapshot beneficiary, ProgramRules program, List<IneligibilityReason> reasons) {
        if (program.maximumIncome().isGreaterThan(Money.ZERO)
                && beneficiary.familyIncome().isGreaterThan(program.maximumIncome())) {
            reasons.add(IneligibilityReason.INCOME_ABOVE_PROGRAM_CEILING);
        }
    }

    /** REQ-010, REQ-011 e REQ-012 — {@code VALELEG.NSN:196-229}. */
    private void appendProgramTypeReasons(
            BeneficiarySnapshot beneficiary,
            ProgramRules program,
            int age,
            List<IneligibilityReason> reasons) {

        ProgramType type = program.type();
        if (type == null) {
            reasons.add(IneligibilityReason.UNKNOWN_PROGRAM_TYPE);
            return;
        }

        switch (type) {
            case ASSISTANCE -> {
                if (beneficiary.familyIncome().isGreaterThan(ASSISTANCE_INCOME_THRESHOLD)
                        && !beneficiary.hasDependents()) {
                    reasons.add(IneligibilityReason.ASSISTANCE_INCOME_WITHOUT_DEPENDENTS);
                }
                if (!beneficiary.documentationComplete()) {
                    reasons.add(IneligibilityReason.INCOMPLETE_DOCUMENTATION);
                }
            }
            case SOCIAL_SECURITY -> {
                if (age < SOCIAL_SECURITY_MINIMUM_AGE) {
                    reasons.add(IneligibilityReason.SOCIAL_SECURITY_AGE_BELOW_60);
                }
            }
            case EMPLOYMENT -> {
                if (age < EMPLOYMENT_MINIMUM_AGE || age > EMPLOYMENT_MAXIMUM_AGE) {
                    reasons.add(IneligibilityReason.EMPLOYMENT_AGE_OUTSIDE_RANGE);
                }
            }
        }
    }

    /** REQ-013 — {@code VALELEG.NSN:258-276}. As posicoes sao avaliadas de forma cumulativa. */
    private void appendEligibilityCodeReasons(
            BeneficiarySnapshot beneficiary, ProgramRules program, List<IneligibilityReason> reasons) {
        if (program.requiresNis() && !beneficiary.hasNis()) {
            reasons.add(IneligibilityReason.NIS_NOT_REGISTERED);
        }
        if (program.requiresDependents() && !beneficiary.hasDependents()) {
            reasons.add(IneligibilityReason.DEPENDENTS_REQUIRED);
        }
    }
}
