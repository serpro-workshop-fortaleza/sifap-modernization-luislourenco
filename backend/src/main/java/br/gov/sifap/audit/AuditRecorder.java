package br.gov.sifap.audit;

/**
 * Interface publica do contexto {@code audit}.
 *
 * <p>REQ-004 — base legal IN-TCU 63/2010, citada em {@code AUDIT.ddm:15}. A trilha e
 * somente inserção: nao existe operacao de alteracao ou exclusao, conforme
 * {@code AUDIT.ddm:13-14}.
 */
public interface AuditRecorder {

    void record(AuditEvent event);
}
