package br.gov.sifap.audit;

/**
 * Evento a registrar na trilha de auditoria.
 *
 * <p>REQ-004 e REQ-031 — {@code maskedCpf} nunca carrega o documento completo. O legado
 * grava {@code NUM-CPF-AFFECTED} sem mascara ({@code CCAUDIT.NSC:97}); aqui a trilha
 * guarda o identificador da entidade e o documento mascarado.
 *
 * @param entityType BENF, PGTO, PROG ou SIST ({@code AUDIT.ddm:52})
 */
public record AuditEvent(
        AuditAction action,
        String entityType,
        String entityId,
        String maskedCpf,
        String moduleName,
        String description) {

    /** REQ-004 — evento de encerramento de um ciclo de folha. */
    public static AuditEvent payrollCycle(String period, String description) {
        return new AuditEvent(
                AuditAction.BATCH, "PGTO", period, null, "PayrollGenerationService", description);
    }
}
