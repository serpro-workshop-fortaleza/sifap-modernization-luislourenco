package br.gov.sifap.payment;

/**
 * Situacao do pagamento ({@code PAYMENT.ddm:57-59} — campo {@code DA STAT-PAYMENT}).
 *
 * <p>REQ-028 — a folha cria o pagamento em {@link #GENERATED}.
 *
 * <p>Os estados de conciliacao estao declarados mas <strong>nao sao usados</strong> no
 * recorte 001. O legado grava {@code P} quando o banco confirma o credito
 * ({@code BATCHCON.NSP:207}) enquanto o DDM define {@code P} como PENDENTE, e os
 * relatorios exibem {@code P} como "PAGO" ({@code RELPGT.NSP:186-200}). A contradicao
 * esta registrada em {@code SIFAP-M-13} e {@code SIFAP-M-19} e bloqueia o recorte de
 * conciliacao.
 */
public enum PaymentStatus {

    PENDING("P"),
    GENERATED("G"),
    ISSUED("E"),
    CONFIRMED("C"),
    RETURNED("D"),
    CANCELED("X"),
    REPROCESSED("R");

    private final String legacyCode;

    PaymentStatus(String legacyCode) {
        this.legacyCode = legacyCode;
    }

    public String legacyCode() {
        return legacyCode;
    }
}
