package br.gov.sifap.benefitcalculation;

import br.gov.sifap.shared.Money;
import br.gov.sifap.shared.UnresolvedLegacyRuleException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Fatores multiplicadores do valor bruto.
 *
 * <p>Traducao de {@code CALCBENF.NSN:198-256} e {@code :330-336}. Todos os fatores usam
 * escala 4, como os campos {@code N3.4} do legado, e nao sao truncados — somente o
 * resultado monetario e ({@link Money#truncate()}, ADR-003).
 */
final class BenefitFactors {

    private BenefitFactors() {
    }

    /**
     * REQ-021 (bloqueado por SIFAP-M-20).
     *
     * <p>A tabela legada tem 27 posicoes rotuladas por UF ({@code LDASIFAP.NSL:36-46}) mas e
     * indexada por {@code COD-REGION}, que o DDM define como 01 a 05 ou 99
     * ({@code BENEFIC.ddm:77}). Somente as cinco primeiras posicoes sao alcancaveis. Elas
     * estao reproduzidas abaixo; qualquer outra regiao falha de forma visivel em vez de
     * receber o 1,0000 silencioso do ramo {@code ELSE} de {@code CALCBENF.NSN:203}.
     */
    private static final Map<Integer, BigDecimal> REACHABLE_REGION_FACTORS = Map.of(
            1, new BigDecimal("1.3500"),
            2, new BigDecimal("1.3200"),
            3, new BigDecimal("1.3000"),
            4, new BigDecimal("1.2800"),
            5, new BigDecimal("1.3100"));

    static BigDecimal regional(int regionCode) {
        BigDecimal factor = REACHABLE_REGION_FACTORS.get(regionCode);
        if (factor == null) {
            throw new UnresolvedLegacyRuleException(
                    "SIFAP-M-20",
                    ("Fator regional indefinido para a regiao %d. A tabela legada e rotulada por UF "
                            + "mas indexada por COD-REGION (CALCBENF.NSN:201)").formatted(regionCode));
        }
        return factor;
    }

    /**
     * REQ-017 — {@code CALCBENF.NSN:216-228}.
     *
     * <p>Acrescimo de 0,05 por dependente ate 2; de 0,03 do terceiro ao quarto; de 0,02 a
     * partir do quinto.
     */
    static BigDecimal family(int dependents) {
        if (dependents < 0) {
            throw new IllegalArgumentException("Quantidade de dependentes negativa: " + dependents);
        }
        if (dependents == 0) {
            return new BigDecimal("1.0000");
        }
        if (dependents <= 2) {
            return new BigDecimal("1.0000")
                    .add(BigDecimal.valueOf(dependents).multiply(new BigDecimal("0.0500")));
        }
        if (dependents <= 4) {
            return new BigDecimal("1.1000")
                    .add(BigDecimal.valueOf(dependents - 2L).multiply(new BigDecimal("0.0300")));
        }
        return new BigDecimal("1.1600")
                .add(BigDecimal.valueOf(dependents - 4L).multiply(new BigDecimal("0.0200")));
    }

    /** REQ-018 — {@code CALCBENF.NSN:246-256}. */
    static BigDecimal age(int age) {
        if (age >= 65) {
            return new BigDecimal("1.1500");
        }
        if (age >= 60) {
            return new BigDecimal("1.1000");
        }
        if (age < 18) {
            return new BigDecimal("1.0500");
        }
        return new BigDecimal("1.0000");
    }

    /**
     * REQ-019 — {@code CALCBENF.NSN:330-336}.
     *
     * <p>Primeira faixa cujo teto e maior ou igual a renda declarada.
     */
    private static final Money[] INCOME_BAND_CEILINGS = {
        Money.of("300.00"), Money.of("600.00"), Money.of("1000.00"), Money.of("1500.00"), Money.of("9999.99")
    };

    private static final BigDecimal[] INCOME_BAND_FACTORS = {
        new BigDecimal("1.0000"),
        new BigDecimal("0.8500"),
        new BigDecimal("0.7000"),
        new BigDecimal("0.5500"),
        new BigDecimal("0.4000")
    };

    static BigDecimal income(Money familyIncome) {
        for (int band = 0; band < INCOME_BAND_CEILINGS.length; band++) {
            if (!familyIncome.isGreaterThan(INCOME_BAND_CEILINGS[band])) {
                return INCOME_BAND_FACTORS[band];
            }
        }
        // O legado deixa #FACTOR-INCOME com o valor anterior quando nenhuma faixa casa;
        // como a ultima faixa e 9999.99, rendas acima disso caem aqui.
        return INCOME_BAND_FACTORS[INCOME_BAND_FACTORS.length - 1];
    }
}
