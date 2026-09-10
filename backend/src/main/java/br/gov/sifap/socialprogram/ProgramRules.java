package br.gov.sifap.socialprogram;

import br.gov.sifap.shared.Money;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Parametros imutaveis de um programa social, entregues ao contexto de calculo.
 *
 * <p>Origem: {@code SOCPROG.ddm} (FNR 151). Ver
 * {@code 02-modern-spec/bounded-contexts.md} — o calculo nunca le a tabela
 * diretamente, recebe este objeto por parametro.
 */
public record ProgramRules(
        String code,
        String name,
        ProgramType type,
        ProgramStatus status,
        Money individualBaseAmount,
        BigDecimal adjustmentFactor,
        Money maximumIncome,
        int minimumAge,
        int maximumAge,
        String eligibilityCode,
        Set<DiscountType> applicableDiscounts) {

    /** REQ-009 — limite igual a zero significa "sem restricao" ({@code VALELEG.NSN:163-178}). */
    public boolean hasMinimumAge() {
        return minimumAge > 0;
    }

    public boolean hasMaximumAge() {
        return maximumAge > 0;
    }

    /** REQ-013 — primeira posicao do codigo de elegibilidade exige NIS registrado. */
    public boolean requiresNis() {
        return charAtOrSpace(0) == 'R';
    }

    /** REQ-013 — segunda posicao exige dependentes ativos. */
    public boolean requiresDependents() {
        return charAtOrSpace(1) == 'D';
    }

    private char charAtOrSpace(int index) {
        return eligibilityCode != null && eligibilityCode.length() > index
                ? eligibilityCode.charAt(index)
                : ' ';
    }
}
