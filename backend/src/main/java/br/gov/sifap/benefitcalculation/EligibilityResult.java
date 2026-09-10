package br.gov.sifap.benefitcalculation;

import java.util.List;

/**
 * Resultado da avaliacao de elegibilidade.
 *
 * <p>REQ-014 — carrega todos os motivos encontrados, nao apenas o primeiro.
 */
public record EligibilityResult(boolean eligible, List<IneligibilityReason> reasons) {

    public EligibilityResult {
        reasons = List.copyOf(reasons);
    }

    static EligibilityResult approved() {
        return new EligibilityResult(true, List.of());
    }

    static EligibilityResult rejected(List<IneligibilityReason> reasons) {
        if (reasons.isEmpty()) {
            throw new IllegalArgumentException("Inelegibilidade exige ao menos um motivo");
        }
        return new EligibilityResult(false, reasons);
    }
}
