package br.gov.sifap.beneficiary;

/**
 * Situacao cadastral do beneficiario ({@code BENEFIC.ddm:85} — campo {@code CE STAT-BENEFICIARY}).
 *
 * <p>REQ-003 — somente {@link #ACTIVE} entra na folha ({@code BATCHPGT.NSP:258-262}).
 * REQ-048 do catalogo confirma que estes cinco sao os unicos valores validos
 * ({@code VALBENEF.NSN:175-181}).
 */
public enum BeneficiaryStatus {

    ACTIVE("A"),
    SUSPENDED("S"),
    CANCELED("C"),
    INACTIVE("I"),
    DISCONNECTED("D");

    private final String legacyCode;

    BeneficiaryStatus(String legacyCode) {
        this.legacyCode = legacyCode;
    }

    public String legacyCode() {
        return legacyCode;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
}
