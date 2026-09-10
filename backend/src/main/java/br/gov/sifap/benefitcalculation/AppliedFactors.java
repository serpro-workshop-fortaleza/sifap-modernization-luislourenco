package br.gov.sifap.benefitcalculation;

import java.math.BigDecimal;

/**
 * Fatores efetivamente aplicados no calculo, em escala 4 como os campos {@code N3.4} do
 * legado ({@code CALCBENF.NSN:88-90}).
 *
 * <p>REQ-016 — expostos para a comparacao de equivalencia descrita no ADR-004.
 */
public record AppliedFactors(
        BigDecimal regional, BigDecimal family, BigDecimal income, BigDecimal age) {}
