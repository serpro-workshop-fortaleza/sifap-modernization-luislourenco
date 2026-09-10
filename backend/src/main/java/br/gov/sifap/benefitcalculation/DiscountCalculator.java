package br.gov.sifap.benefitcalculation;

import br.gov.sifap.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Calcula o total de descontos de um pagamento.
 *
 * <p>Traducao de {@code CALCDSCT.NSP}. Cobre REQ-022 a REQ-026.
 *
 * <p>Duas divergencias deliberadas em relacao ao legado, ambas declaradas na
 * especificacao:
 *
 * <ul>
 *   <li>REQ-022 usa a tabela progressiva de {@code CALCDSCT.NSP:196-205}, nao os 3% fixos
 *       que {@code BATCHPGT.NSP:458-462} aplica por {@code CALCDSCT} nunca ser chamado
 *       ({@code SIFAP-M-08}).
 *   <li>REQ-024 separa os acumuladores judicial e nao judicial. No legado ambos somam no
 *       mesmo acumulador e o teto e aplicado dentro do laco
 *       ({@code CALCDSCT.NSP:172-177}), o que torna o resultado dependente da ordem dos
 *       itens no grupo periodico.
 * </ul>
 */
@Service
public class DiscountCalculator {

    /** REQ-023 — {@code CALCDSCT.NSP:107}. */
    private static final BigDecimal CAP_RATE = new BigDecimal("0.30");

    /** REQ-022 — {@code CALCDSCT.NSP:196-205}. Tetos e aliquotas da contribuicao social. */
    private static final Money[] CONTRIBUTION_CEILINGS = {
        Money.of("500.00"), Money.of("1000.00"), Money.of("2000.00"), Money.of("9999.99")
    };

    private static final BigDecimal[] CONTRIBUTION_RATES = {
        new BigDecimal("0.03"), new BigDecimal("0.05"), new BigDecimal("0.07"), new BigDecimal("0.09")
    };

    public Money calculate(Money grossAmount, List<Discount> discounts, LocalDate processingDate) {
        Money cappedTotal = socialContribution(grossAmount);
        Money exemptTotal = Money.ZERO;

        for (Discount discount : discounts) {
            // REQ-025 — descontos fora da vigencia sao ignorados.
            if (!discount.isEffectiveOn(processingDate)) {
                continue;
            }
            Money amount = discount.amountOver(grossAmount);
            if (discount.type().isExemptFromCap()) {
                exemptTotal = exemptTotal.add(amount);
            } else {
                cappedTotal = cappedTotal.add(amount);
            }
        }

        // REQ-023 — o teto de 30% incide somente sobre a parcela nao isenta.
        Money cap = grossAmount.multiply(CAP_RATE).truncate();
        if (cappedTotal.isGreaterThan(cap)) {
            cappedTotal = cap;
        }

        // REQ-024 — o desconto judicial e somado depois do teto, integralmente.
        return cappedTotal.add(exemptTotal).truncate();
    }

    /** REQ-022 — primeira faixa cujo teto e maior ou igual ao valor bruto. */
    private Money socialContribution(Money grossAmount) {
        for (int band = 0; band < CONTRIBUTION_CEILINGS.length; band++) {
            if (!grossAmount.isGreaterThan(CONTRIBUTION_CEILINGS[band])) {
                return grossAmount.multiply(CONTRIBUTION_RATES[band]);
            }
        }
        return grossAmount.multiply(CONTRIBUTION_RATES[CONTRIBUTION_RATES.length - 1]);
    }
}
