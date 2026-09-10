package br.gov.sifap.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Valor monetario em reais com escala 2.
 *
 * <p>REQ-020 — o truncamento usa {@link RoundingMode#DOWN}, nunca arredondamento.
 * O legado trunca multiplicando por 100 para um campo inteiro {@code N11} e dividindo
 * de volta, o que descarta a parte fracionaria
 * ({@code CALCBENF.NSN:265-267}). Ver ADR-003.
 *
 * <p>O truncamento e explicito: as operacoes aritmeticas preservam a precisao e so
 * {@link #truncate()} reduz a escala. Isso permite truncar exatamente nos mesmos
 * pontos do legado, em vez de a cada operacao.
 */
public final class Money implements Comparable<Money> {

    public static final Money ZERO = new Money(BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));

    private static final int SCALE = 2;

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount;
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }

    public static Money of(BigDecimal value) {
        return new Money(Objects.requireNonNull(value, "value"));
    }

    public static Money ofCents(long cents) {
        return new Money(BigDecimal.valueOf(cents, SCALE));
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor));
    }

    /** REQ-020 — descarta a parte fracionaria alem da segunda casa. */
    public Money truncate() {
        return new Money(this.amount.setScale(SCALE, RoundingMode.DOWN));
    }

    /** REQ-027 — o valor liquido nunca e negativo. */
    public Money atLeastZero() {
        return isNegative() ? ZERO : this;
    }

    public boolean isNegative() {
        return this.amount.signum() < 0;
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

    public BigDecimal toBigDecimal() {
        return this.amount;
    }

    /** REQ-029 — o arquivo de remessa expressa o valor em centavos, sem separador. */
    public long toCents() {
        return truncate().amount.movePointRight(SCALE).longValueExact();
    }

    @Override
    public int compareTo(Money other) {
        return this.amount.compareTo(other.amount);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Money money && this.amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return this.amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return truncate().amount.toPlainString();
    }
}
