package br.gov.sifap.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ReferencePeriodTest {

    @Test
    @DisplayName("REQ-001 - aceita periodo no formato YYYYMM")
    void should_parse_period_when_format_is_valid() {
        ReferencePeriod period = ReferencePeriod.parse("202601");

        assertThat(period.year()).isEqualTo(2026);
        assertThat(period.month()).isEqualTo(1);
        assertThat(period.toYyyymm()).isEqualTo(202601);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "2026", "20260", "2026011", "2026-01", "abcdef"})
    @DisplayName("REQ-001 - rejeita periodo fora do formato YYYYMM")
    void should_reject_period_when_format_is_invalid(String raw) {
        assertThatThrownBy(() -> ReferencePeriod.parse(raw))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    @DisplayName("REQ-001 - rejeita mes fora do intervalo 1-12")
    void should_reject_period_when_month_is_out_of_range() {
        assertThatThrownBy(() -> ReferencePeriod.parse("202613"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("REQ-001 - nao existe derivacao implicita da data corrente")
    void should_not_expose_any_factory_without_explicit_period() {
        // O legado deriva o periodo de *DATN quando CMSYNIN vem zerado
        // (BATCHPGT.NSP:177-179), o que impede reprocessamento auditavel.
        boolean hasNoArgFactory = java.util.Arrays.stream(ReferencePeriod.class.getDeclaredMethods())
                .anyMatch(method -> java.lang.reflect.Modifier.isStatic(method.getModifiers())
                        && method.getParameterCount() == 0
                        && method.getReturnType() == ReferencePeriod.class);

        assertThat(hasNoArgFactory).isFalse();
    }

    @Test
    @DisplayName("REQ-009 - idade em anos completos na data de referencia")
    void should_compute_completed_age_on_period() {
        ReferencePeriod period = ReferencePeriod.of(202601);

        assertThat(period.completedAgeOn(LocalDate.of(1980, 5, 12))).isEqualTo(46);
    }

    @Test
    @DisplayName("REQ-001 - rejeita ano anterior ao inicio do SIFAP")
    void should_reject_period_before_sifap_started() {
        assertThatThrownBy(() -> ReferencePeriod.of(199601))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
