package br.gov.sifap.benefitcalculation;

import br.gov.sifap.shared.Money;

/**
 * Resultado do calculo do beneficio, sem efeito colateral.
 *
 * <p>REQ-016, REQ-020 e REQ-027. Os fatores aplicados sao devolvidos para que a
 * comparacao com a folha legada (ADR-004, execucao em sombra) possa apontar qual fator
 * divergiu, e nao apenas que o total divergiu.
 */
public record BenefitCalculation(
        Money grossAmount,
        Money totalDiscount,
        Money netAmount,
        PaymentKind kind,
        AppliedFactors factors) {}
