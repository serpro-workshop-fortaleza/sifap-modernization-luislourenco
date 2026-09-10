package br.gov.sifap.shared;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

/**
 * Periodo de referencia da folha, no formato {@code YYYYMM}.
 *
 * <p>REQ-001 — o periodo e sempre explicito. O legado deriva de {@code *DATN} quando a
 * entrada vem zerada ({@code BATCHPGT.NSP:177-179}); aqui essa derivacao implicita nao
 * existe, porque impede reprocessamento auditavel.
 */
public record ReferencePeriod(int year, int month) implements Comparable<ReferencePeriod> {

    public ReferencePeriod {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Mes fora do intervalo 1-12: " + month);
        }
        if (year < 1997) {
            throw new IllegalArgumentException("Ano anterior ao inicio do SIFAP: " + year);
        }
    }

    /** Aceita a representacao {@code YYYYMM} usada pelo CMSYNIN do legado. */
    public static ReferencePeriod parse(String value) {
        if (value == null || !value.matches("\\d{6}")) {
            throw new DateTimeParseException("Periodo deve ter o formato YYYYMM", String.valueOf(value), 0);
        }
        int numeric = Integer.parseInt(value);
        return of(numeric);
    }

    public static ReferencePeriod of(int yyyymm) {
        return new ReferencePeriod(yyyymm / 100, yyyymm % 100);
    }

    public int toYyyymm() {
        return year * 100 + month;
    }

    public YearMonth toYearMonth() {
        return YearMonth.of(year, month);
    }

    /** Idade em anos completos na data de referencia. Base de REQ-009 e REQ-018. */
    public int completedAgeOn(LocalDate birthDate) {
        return toYearMonth().atEndOfMonth().getYear() - birthDate.getYear();
    }

    @Override
    public int compareTo(ReferencePeriod other) {
        return Integer.compare(toYyyymm(), other.toYyyymm());
    }

    @Override
    public String toString() {
        return "%04d%02d".formatted(year, month);
    }
}
