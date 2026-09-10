package br.gov.sifap.socialprogram;

/**
 * Situacao do programa social ({@code SOCPROG.ddm:36} — campo {@code AI STAT-PROGRAM}).
 *
 * <p>REQ-008 — somente {@link #ACTIVE} permite gerar pagamento
 * ({@code VALELEG.NSN:114-118}).
 */
public enum ProgramStatus {

    ACTIVE("A"),
    INACTIVE("I"),
    ENDED("E");

    private final String legacyCode;

    ProgramStatus(String legacyCode) {
        this.legacyCode = legacyCode;
    }

    public String legacyCode() {
        return legacyCode;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
