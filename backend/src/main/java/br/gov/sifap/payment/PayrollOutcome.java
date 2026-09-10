package br.gov.sifap.payment;

import br.gov.sifap.shared.ReferencePeriod;
import java.util.List;

/**
 * Resultado consolidado de um ciclo da folha.
 *
 * <p>REQ-005 — os quatro desfechos sao distinguiveis programaticamente, preservando a
 * semantica dos codigos de retorno de {@code BATCHPGT.NSP:556-570} e
 * {@code SIFAPJ01.jcl:99-104}.
 */
public record PayrollOutcome(
        ReferencePeriod period,
        Outcome outcome,
        int processed,
        int generated,
        int skipped,
        List<RejectedBeneficiary> rejected) {

    public PayrollOutcome {
        rejected = List.copyOf(rejected);
    }

    public enum Outcome {
        /** RC=0 — folha gerada sem rejeitados. */
        COMPLETED,
        /** RC=4 — folha gerada com rejeitados. */
        COMPLETED_WITH_REJECTIONS,
        /** RC=8 — nada a processar. */
        NOTHING_TO_PROCESS
    }

    /**
     * REQ-030 — o motivo identifica o beneficiario de forma mascarada.
     *
     * @param maskedCpf REQ-031 — nunca o documento completo
     */
    public record RejectedBeneficiary(String maskedCpf, RejectionReason reason, String detail) {}

    public enum RejectionReason {
        /** REQ-006 — {@code BATCHPGT.NSP:279-291}. */
        INVALID_CPF,
        /** REQ-030 — {@code BATCHPGT.NSP:307-318}. */
        PROGRAM_NOT_FOUND,
        /** REQ-026 — tipo de desconto desconhecido; divergencia deliberada do legado. */
        UNKNOWN_DISCOUNT_TYPE,
        /** Regra do legado ainda sem decisao humana; ver {@code mysteries-found.md}. */
        UNRESOLVED_LEGACY_RULE
    }
}
