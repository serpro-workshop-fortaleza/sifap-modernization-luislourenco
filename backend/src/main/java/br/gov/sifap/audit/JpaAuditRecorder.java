package br.gov.sifap.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro imutavel da trilha ({@code AUDIT.ddm}, FNR 153).
 *
 * <p>A sequencia e delegada ao banco. O legado le o ultimo numero uma vez por execucao e
 * incrementa em memoria ({@code CCAUDIT.NSC:70-77}), o que colide sob concorrencia porque
 * {@code NUM-AUDIT} e descritor unico — ver {@code SIFAP-M-16}.
 */
@Entity
@Table(name = "audit_trail")
class AuditTrailEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 16)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 4)
    private String entityType;

    @Column(name = "entity_id", nullable = false, length = 15)
    private String entityId;

    @Column(name = "masked_cpf", length = 14)
    private String maskedCpf;

    @Column(name = "module_name", nullable = false, length = 40)
    private String moduleName;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected AuditTrailEntry() {
        // exigido pelo JPA
    }

    AuditTrailEntry(AuditEvent event, LocalDateTime occurredAt) {
        this.action = event.action();
        this.entityType = event.entityType();
        this.entityId = event.entityId();
        this.maskedCpf = event.maskedCpf();
        this.moduleName = event.moduleName();
        this.description = event.description();
        this.occurredAt = occurredAt;
    }
}

interface AuditTrailRepository extends JpaRepository<AuditTrailEntry, Long> {}

/** Adaptador JPA de {@link AuditRecorder}. */
@Service
class JpaAuditRecorder implements AuditRecorder {

    private final AuditTrailRepository auditTrailRepository;
    private final java.time.Clock clock;

    JpaAuditRecorder(AuditTrailRepository auditTrailRepository, java.time.Clock clock) {
        this.auditTrailRepository = auditTrailRepository;
        this.clock = clock;
    }

    /**
     * REQ-004 — a trilha usa transacao propria. Se o ciclo da folha falhar e sofrer
     * rollback, o registro de auditoria permanece: uma tentativa auditada e exatamente o
     * que a IN-TCU 63/2010 exige.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditEvent event) {
        auditTrailRepository.save(new AuditTrailEntry(event, LocalDateTime.now(clock)));
    }
}
