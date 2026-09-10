package br.gov.sifap.payment;

import br.gov.sifap.shared.ReferencePeriod;

/**
 * Registro de detalhe do arquivo de remessa bancaria.
 *
 * <p>REQ-029 — reproduz o extrato {@code CMWKF01} de {@code BATCHPGT.NSP:492-503}, com
 * 240 posicoes e registro de detalhe identificado pelo tipo {@code 3}
 * ({@code SIFAPJ01.jcl:53-58}).
 *
 * <p>O valor vai em centavos, sem separador decimal, como em
 * {@code BATCHPGT.NSP:497-499}.
 */
public record RemittanceRecord(
        String recordType, String cpf, String programCode, String period, String paymentId, long netAmountCents) {

    private static final String DETAIL_RECORD = "3";
    private static final int LINE_LENGTH = 240;

    static RemittanceRecord from(Payment payment) {
        return new RemittanceRecord(
                DETAIL_RECORD,
                payment.cpf().unmasked(),
                payment.programCode(),
                payment.referencePeriod().toString(),
                String.valueOf(payment.id()),
                payment.netAmount().toCents());
    }

    /** Linha posicional de 240 bytes, preenchida a direita com espacos. */
    public String toFixedWidthLine() {
        String content = "%s%s%s%s%015d%09d"
                .formatted(recordType, cpf, programCode, period, parsePaymentId(), netAmountCents);
        if (content.length() > LINE_LENGTH) {
            throw new IllegalStateException("Registro de remessa excede " + LINE_LENGTH + " posicoes");
        }
        return String.format("%-" + LINE_LENGTH + "s", content);
    }

    private long parsePaymentId() {
        return paymentId == null || paymentId.equals("null") ? 0L : Long.parseLong(paymentId);
    }

    public static ReferencePeriod periodOf(RemittanceRecord record) {
        return ReferencePeriod.parse(record.period());
    }
}
