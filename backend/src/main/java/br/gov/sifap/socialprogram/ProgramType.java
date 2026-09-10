package br.gov.sifap.socialprogram;

/**
 * Tipo do programa social ({@code SOCPROG.ddm:29} — campo {@code AD TYPE-PROGRAM}).
 *
 * <p>REQ-011 e REQ-012 — cada tipo tem exigencia etaria propria e um tipo
 * desconhecido torna o beneficiario inelegivel ({@code VALELEG.NSN:196-229}).
 */
public enum ProgramType {

    /** Assistencial. REQ-010 — exige documentacao completa. */
    ASSISTANCE("A"),

    /** Previdenciario. REQ-011 — exige idade minima de 60 anos. */
    SOCIAL_SECURITY("P"),

    /** Trabalho e emprego. REQ-011 — exige idade entre 16 e 65 anos. */
    EMPLOYMENT("T");

    private final String legacyCode;

    ProgramType(String legacyCode) {
        this.legacyCode = legacyCode;
    }

    public String legacyCode() {
        return legacyCode;
    }

    /**
     * REQ-012 — o legado cai no ramo {@code NONE} do {@code DECIDE} e marca inelegivel
     * ({@code VALELEG.NSN:225-229}). Aqui a ausencia e representada por {@link java.util.Optional}
     * e tratada pelo servico de elegibilidade.
     */
    public static java.util.Optional<ProgramType> fromLegacyCode(String code) {
        for (ProgramType type : values()) {
            if (type.legacyCode.equals(code)) {
                return java.util.Optional.of(type);
            }
        }
        return java.util.Optional.empty();
    }
}
