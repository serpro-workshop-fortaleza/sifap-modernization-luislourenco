package br.gov.sifap.benefitcalculation;

/**
 * Natureza do pagamento ({@code PAYMENT.ddm:75} — campo {@code EG TYPE-PAYMENT}).
 *
 * <p>Somente {@link #NORMAL} esta no escopo do recorte 001. O 13o salario
 * ({@code TYPE-PAYMENT = 'D'}) foi adiado porque a formula do codigo diverge do
 * comentario que a documenta ({@code SIFAP-M-07}).
 */
public enum PaymentKind {

    NORMAL("N"),
    RETROACTIVE("R"),
    ALLOWANCE("A"),
    CORRECTION("C");

    private final String legacyCode;

    PaymentKind(String legacyCode) {
        this.legacyCode = legacyCode;
    }

    public String legacyCode() {
        return legacyCode;
    }
}
