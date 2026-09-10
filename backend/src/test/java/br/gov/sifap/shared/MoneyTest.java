package br.gov.sifap.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyTest {

    @Nested
    @DisplayName("REQ-020 - truncamento sem arredondamento")
    class Truncation {

        @ParameterizedTest
        @CsvSource({
            "123.459, 123.45",
            "123.451, 123.45",
            "123.999, 123.99",
            "0.019, 0.01",
            "100.00, 100.00"
        })
        void should_discard_fraction_when_truncating(String raw, String expected) {
            assertThat(Money.of(raw).truncate()).isEqualTo(Money.of(expected));
        }

        @Test
        void should_not_round_half_up_when_truncating() {
            // O legado descarta a fracao (CALCBENF.NSN:265-267); HALF_UP daria 123.46.
            assertThat(Money.of("123.455").truncate()).isEqualTo(Money.of("123.45"));
        }

        @Test
        void should_preserve_precision_before_explicit_truncation() {
            // Truncar so nos pontos do legado exige que a aritmetica preserve a precisao.
            Money result = Money.of("100.00").multiply(new BigDecimal("1.3333"));
            assertThat(result.toBigDecimal()).isEqualByComparingTo("133.3300");
        }
    }

    @Nested
    @DisplayName("REQ-027 - valor liquido nunca negativo")
    class NonNegative {

        @Test
        void should_return_zero_when_amount_is_negative() {
            assertThat(Money.of("-50.00").atLeastZero()).isEqualTo(Money.ZERO);
        }

        @Test
        void should_keep_amount_when_positive() {
            assertThat(Money.of("50.00").atLeastZero()).isEqualTo(Money.of("50.00"));
        }
    }

    @Nested
    @DisplayName("REQ-029 - valor em centavos no arquivo de remessa")
    class Cents {

        @Test
        void should_convert_to_cents_without_separator() {
            assertThat(Money.of("1234.56").toCents()).isEqualTo(123456L);
        }

        @Test
        void should_truncate_before_converting_to_cents() {
            assertThat(Money.of("1234.569").toCents()).isEqualTo(123456L);
        }
    }

    @Test
    void should_reject_null_amount() {
        assertThatThrownBy(() -> Money.of((BigDecimal) null))
                .isInstanceOf(NullPointerException.class);
    }
}
