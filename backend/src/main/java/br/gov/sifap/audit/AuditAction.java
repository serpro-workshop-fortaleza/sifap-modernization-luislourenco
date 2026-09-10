package br.gov.sifap.audit;

/**
 * Tipo de acao registrada na trilha ({@code AUDIT.ddm:38-47} — campo {@code BA COD-ACTION}).
 *
 * <p>Somente os codigos declarados no DDM estao aqui. {@code RELAUDIT.NSP:160-172} conta
 * ainda {@code CN} e {@code DV}, que o DDM nao define — divergencia registrada em
 * {@code SIFAP-M-18} e nao reproduzida.
 */
public enum AuditAction {

    INSERT("IN"),
    UPDATE("AL"),
    DELETE("EX"),
    QUERY("CO"),
    LOGIN("LG"),
    LOGOUT("LO"),
    /** REQ-004 — ciclo batch. */
    BATCH("BT"),
    ERROR("ER"),
    AUTHORIZATION("AU"),
    REJECTION("RE");

    private final String legacyCode;

    AuditAction(String legacyCode) {
        this.legacyCode = legacyCode;
    }

    public String legacyCode() {
        return legacyCode;
    }
}
