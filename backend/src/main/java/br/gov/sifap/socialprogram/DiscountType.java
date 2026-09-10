package br.gov.sifap.socialprogram;

import java.util.Optional;

/**
 * Tipo de desconto aplicavel ({@code SOCPROG.ddm:77-82} — campo MU {@code EA TYPE-DISC-APPLIC}).
 *
 * <p>Os codigos tem duas letras no DDM. O legado compara com uma variavel {@code A1}
 * ({@code CALCDSCT.NSP:127-170}), o que trunca o codigo e faz {@code CS}, {@code EM} e
 * {@code TX} cairem no ramo {@code NONE} e serem ignorados em silencio — ver
 * {@code SIFAP-M-11}. REQ-026 substitui esse comportamento por falha visivel.
 */
public enum DiscountType {

    /** Imposto de Renda Retido na Fonte. */
    INCOME_TAX("IR", false),

    /** Judicial. REQ-024 — nao se sujeita ao teto de 30%. */
    COURT_ORDERED("JD", true),

    /** Consignado. Ignorado pelo legado por truncamento de codigo. */
    PAYROLL_DEDUCTED("CS", false),

    /** Pensao alimenticia. */
    ALIMONY("PA", false),

    /** Emprestimo. Ignorado pelo legado por truncamento de codigo. */
    LOAN("EM", false),

    /** Taxa. Ignorada pelo legado por truncamento de codigo. */
    FEE("TX", false),

    OTHER("OU", false),

    EXTRAORDINARY("EX", false);

    private final String legacyCode;
    private final boolean exemptFromCap;

    DiscountType(String legacyCode, boolean exemptFromCap) {
        this.legacyCode = legacyCode;
        this.exemptFromCap = exemptFromCap;
    }

    public String legacyCode() {
        return legacyCode;
    }

    /** REQ-024 — somente o desconto judicial escapa do teto de 30%. */
    public boolean isExemptFromCap() {
        return exemptFromCap;
    }

    public static Optional<DiscountType> fromLegacyCode(String code) {
        for (DiscountType type : values()) {
            if (type.legacyCode.equalsIgnoreCase(code)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
