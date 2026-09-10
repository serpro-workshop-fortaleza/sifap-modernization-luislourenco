package br.gov.sifap.benefitcalculation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.gov.sifap.beneficiary.BeneficiaryStatus;
import br.gov.sifap.shared.ReferencePeriod;
import br.gov.sifap.shared.UnresolvedLegacyRuleException;
import br.gov.sifap.socialprogram.ProgramStatus;
import br.gov.sifap.socialprogram.ProgramType;
import br.gov.sifap.testing.Fixtures;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EligibilityServiceTest {

    private static final ReferencePeriod PERIOD = ReferencePeriod.of(202601);

    private final EligibilityService eligibilityService = new EligibilityService();

    @Test
    @DisplayName("REQ-008 - programa inativo interrompe a avaliacao")
    void should_be_ineligible_when_program_is_not_active() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().build(),
                Fixtures.program().status(ProgramStatus.INACTIVE).build(),
                PERIOD);

        assertThat(result.eligible()).isFalse();
        assertThat(result.reasons()).containsExactly(IneligibilityReason.PROGRAM_NOT_ACTIVE);
    }

    @Test
    @DisplayName("REQ-003 - beneficiario suspenso e inelegivel")
    void should_be_ineligible_when_beneficiary_is_suspended() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().status(BeneficiaryStatus.SUSPENDED).build(),
                Fixtures.program().build(),
                PERIOD);

        assertThat(result.reasons()).contains(IneligibilityReason.BENEFICIARY_SUSPENDED);
    }

    @Test
    @DisplayName("REQ-009 - idade abaixo do minimo do programa")
    void should_be_ineligible_when_age_is_below_program_minimum() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().birthDate(LocalDate.of(2010, 1, 1)).build(),
                Fixtures.program().type(ProgramType.EMPLOYMENT).ageRange(18, 0).build(),
                PERIOD);

        assertThat(result.reasons()).contains(IneligibilityReason.AGE_BELOW_PROGRAM_MINIMUM);
    }

    @Test
    @DisplayName("REQ-009 - limite zero significa sem restricao")
    void should_ignore_age_limits_when_program_declares_zero() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().birthDate(LocalDate.of(1930, 1, 1)).build(),
                Fixtures.program().ageRange(0, 0).build(),
                PERIOD);

        assertThat(result.eligible()).isTrue();
    }

    @Test
    @DisplayName("REQ-010 - programa assistencial exige documentacao completa")
    void should_be_ineligible_when_documentation_is_incomplete() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().documentationComplete(false).build(),
                Fixtures.program().type(ProgramType.ASSISTANCE).build(),
                PERIOD);

        assertThat(result.reasons()).contains(IneligibilityReason.INCOMPLETE_DOCUMENTATION);
    }

    @Test
    @DisplayName("REQ-010 - assistencial com renda acima de 600 e sem dependentes")
    void should_be_ineligible_when_assistance_income_high_without_dependents() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().familyIncome("700.00").dependents(0).build(),
                Fixtures.program().type(ProgramType.ASSISTANCE).build(),
                PERIOD);

        assertThat(result.reasons())
                .contains(IneligibilityReason.ASSISTANCE_INCOME_WITHOUT_DEPENDENTS);
    }

    @Test
    @DisplayName("REQ-011 - previdenciario exige 60 anos")
    void should_be_ineligible_when_social_security_age_below_60() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().birthDate(LocalDate.of(1980, 1, 1)).build(),
                Fixtures.program().type(ProgramType.SOCIAL_SECURITY).build(),
                PERIOD);

        assertThat(result.reasons()).contains(IneligibilityReason.SOCIAL_SECURITY_AGE_BELOW_60);
    }

    @Test
    @DisplayName("REQ-011 - trabalho exige idade entre 16 e 65")
    void should_be_ineligible_when_employment_age_outside_range() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().birthDate(LocalDate.of(1950, 1, 1)).build(),
                Fixtures.program().type(ProgramType.EMPLOYMENT).build(),
                PERIOD);

        assertThat(result.reasons()).contains(IneligibilityReason.EMPLOYMENT_AGE_OUTSIDE_RANGE);
    }

    @Test
    @DisplayName("REQ-012 - tipo de programa desconhecido torna inelegivel")
    void should_be_ineligible_when_program_type_is_unknown() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().build(),
                new br.gov.sifap.socialprogram.ProgramRules(
                        "0009",
                        "Programa sem tipo",
                        null,
                        ProgramStatus.ACTIVE,
                        br.gov.sifap.shared.Money.of("100.00"),
                        java.math.BigDecimal.ZERO,
                        br.gov.sifap.shared.Money.ZERO,
                        0,
                        0,
                        "  ",
                        java.util.Set.of()),
                PERIOD);

        assertThat(result.reasons()).contains(IneligibilityReason.UNKNOWN_PROGRAM_TYPE);
    }

    @Test
    @DisplayName("REQ-013 - codigo R exige NIS registrado")
    void should_be_ineligible_when_nis_is_required_and_missing() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().nis(null).build(),
                Fixtures.program().eligibilityCode("R    ").build(),
                PERIOD);

        assertThat(result.reasons()).contains(IneligibilityReason.NIS_NOT_REGISTERED);
    }

    @Test
    @DisplayName("REQ-013 - codigo D exige dependentes")
    void should_be_ineligible_when_dependents_are_required_and_missing() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().dependents(0).build(),
                Fixtures.program().eligibilityCode(" D   ").build(),
                PERIOD);

        assertThat(result.reasons()).contains(IneligibilityReason.DEPENDENTS_REQUIRED);
    }

    @Test
    @DisplayName("REQ-014 - devolve todos os motivos, nao apenas o primeiro")
    void should_return_every_reason_when_multiple_impediments_exist() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary()
                        .status(BeneficiaryStatus.SUSPENDED)
                        .documentationComplete(false)
                        .nis(null)
                        .build(),
                Fixtures.program().type(ProgramType.ASSISTANCE).eligibilityCode("R    ").build(),
                PERIOD);

        // O legado acumula ate 10 motivos e devolve so #REASON(1) (VALELEG.NSN:233-236).
        assertThat(result.reasons())
                .containsExactlyInAnyOrder(
                        IneligibilityReason.BENEFICIARY_SUSPENDED,
                        IneligibilityReason.INCOMPLETE_DOCUMENTATION,
                        IneligibilityReason.NIS_NOT_REGISTERED);
    }

    @Test
    @DisplayName("REQ-015 - regiao 99 falha de forma visivel (bloqueado por SIFAP-M-09)")
    void should_fail_loudly_when_region_is_special() {
        assertThatThrownBy(() -> eligibilityService.assess(
                        Fixtures.beneficiary().regionCode(99).build(),
                        Fixtures.program().build(),
                        PERIOD))
                .isInstanceOf(UnresolvedLegacyRuleException.class)
                .hasMessageContaining("SIFAP-M-09");
    }

    @Test
    @DisplayName("Fluxo de sucesso - beneficiario elegivel sem motivos")
    void should_be_eligible_when_all_rules_pass() {
        var result = eligibilityService.assess(
                Fixtures.beneficiary().build(), Fixtures.program().build(), PERIOD);

        assertThat(result.eligible()).isTrue();
        assertThat(result.reasons()).isEmpty();
    }
}
