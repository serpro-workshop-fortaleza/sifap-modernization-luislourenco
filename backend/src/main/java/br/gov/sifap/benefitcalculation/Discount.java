package br.gov.sifap.benefitcalculation;

import br.gov.sifap.shared.Money;
import br.gov.sifap.socialprogram.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Desconto lancado sobre um pagamento ({@code PAYMENT.ddm:46-53} — grupo periodico
 * {@code GRP-DISC}).
 *
 * <p>REQ-025 — {@code endDate} nulo significa vigencia aberta, equivalente ao
 * {@code DT-END-DISC = 0} do legado ({@code CALCDSCT.NSP:113-121}).
 *
 * @param fixedAmount quando presente, prevalece sobre {@code percentage}
 *                    ({@code CALCDSCT.NSP:130-137})
 */
public record Discount(
        DiscountType type,
        Money fixedAmount,
        BigDecimal percentage,
        LocalDate startDate,
        LocalDate endDate,
        String courtCaseNumber) {

    public Discount {
        if (type == null) {
            throw new IllegalArgumentException("Tipo de desconto obrigatorio");
        }
        if (fixedAmount == null && percentage == null) {
            throw new IllegalArgumentException("Desconto exige valor fixo ou percentual");
        }
    }

    public static Discount fixed(DiscountType type, Money amount) {
        return new Discount(type, amount, null, null, null, null);
    }

    public static Discount percentage(DiscountType type, String percentage) {
        return new Discount(type, null, new BigDecimal(percentage), null, null, null);
    }

    /** REQ-025 — fora da vigencia na data de processamento, o desconto e ignorado. */
    public boolean isEffectiveOn(LocalDate processingDate) {
        if (startDate != null && startDate.isAfter(processingDate)) {
            return false;
        }
        return endDate == null || !endDate.isBefore(processingDate);
    }

    /** {@code CALCDSCT.NSP:130-137} — valor fixo tem precedencia sobre o percentual. */
    Money amountOver(Money grossAmount) {
        if (fixedAmount != null && fixedAmount.isGreaterThan(Money.ZERO)) {
            return fixedAmount;
        }
        return grossAmount.multiply(percentage.divide(BigDecimal.valueOf(100)));
    }
}
