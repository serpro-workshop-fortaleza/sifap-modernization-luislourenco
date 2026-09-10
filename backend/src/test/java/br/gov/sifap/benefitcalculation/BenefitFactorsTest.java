package br.gov.sifap.benefitcalculation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.gov.sifap.shared.Money;
import br.gov.sifap.shared.UnresolvedLegacyRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BenefitFactorsTest {

    @Nested
    @DisplayName("REQ-017 - fator familiar por faixa de dependentes")
    class FamilyFactor {

        @ParameterizedTest
        @CsvSource({
            "0, 1.0000",
            "1, 1.0500",
            "2, 1.1000",
            "3, 1.1300",
            "4, 1.1600",
            "5, 1.1800",
            "6, 1.2000"
        })
        void should_apply_band_increment_when_dependents_vary(int dependents, String expected) {
            assertThat(BenefitFactors.family(dependents)).isEqualByComparingTo(expected);
        }

        @Test
        void should_reject_negative_dependent_count() {
            assertThatThrownBy(() -> BenefitFactors.family(-1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("REQ-018 - fator etario")
    class AgeFactor {

        @ParameterizedTest
        @CsvSource({
            "17, 1.0500",
            "18, 1.0000",
            "59, 1.0000",
            "60, 1.1000",
            "64, 1.1000",
            "65, 1.1500",
            "80, 1.1500"
        })
        void should_apply_bracket_when_age_varies(int age, String expected) {
            assertThat(BenefitFactors.age(age)).isEqualByComparingTo(expected);
        }
    }

    @Nested
    @DisplayName("REQ-019 - fator de renda pela primeira faixa que comporta a renda")
    class IncomeFactor {

        @ParameterizedTest
        @CsvSource({
            "0.00, 1.0000",
            "300.00, 1.0000",
            "300.01, 0.8500",
            "600.00, 0.8500",
            "600.01, 0.7000",
            "1000.00, 0.7000",
            "1000.01, 0.5500",
            "1500.00, 0.5500",
            "1500.01, 0.4000",
            "9999.99, 0.4000"
        })
        void should_select_first_band_that_fits(String income, String expected) {
            assertThat(BenefitFactors.income(Money.of(income))).isEqualByComparingTo(expected);
        }

        @Test
        void should_fall_back_to_last_band_when_income_exceeds_all_ceilings() {
            assertThat(BenefitFactors.income(Money.of("50000.00"))).isEqualByComparingTo("0.4000");
        }
    }

    @Nested
    @DisplayName("REQ-021 - fator regional (bloqueado por SIFAP-M-20)")
    class RegionalFactor {

        @ParameterizedTest
        @CsvSource({"1, 1.3500", "2, 1.3200", "3, 1.3000", "4, 1.2800", "5, 1.3100"})
        void should_return_legacy_value_for_reachable_regions(int region, String expected) {
            assertThat(BenefitFactors.regional(region)).isEqualByComparingTo(expected);
        }

        @Test
        void should_fail_loudly_for_special_region_99() {
            // O legado cai no ramo ELSE e aplica 1,0000 em silencio (CALCBENF.NSN:203).
            // Nao reproduzimos esse valor sem saber se ele e intencional.
            assertThatThrownBy(() -> BenefitFactors.regional(99))
                    .isInstanceOf(UnresolvedLegacyRuleException.class)
                    .hasMessageContaining("SIFAP-M-20");
        }

        @Test
        void should_fail_loudly_for_unreachable_region_index() {
            // As posicoes 6 a 27 da tabela legada sao rotuladas por UF e nunca alcancadas
            // pelo indice COD-REGION.
            assertThatThrownBy(() -> BenefitFactors.regional(11))
                    .isInstanceOf(UnresolvedLegacyRuleException.class)
                    .hasMessageContaining("SIFAP-M-20");
        }
    }
}
