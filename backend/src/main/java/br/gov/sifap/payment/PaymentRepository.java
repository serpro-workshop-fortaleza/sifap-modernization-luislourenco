package br.gov.sifap.payment;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso a {@link Payment}.
 *
 * <p>REQ-002 — {@link #existsByCpfAndReferencePeriod} traduz o superdescritor
 * {@code SUPER-CPF-PERIOD} usado por {@code BATCHPGT.NSP:299}. No PostgreSQL isso e um
 * indice unico sobre {@code (cpf, reference_period)}; ver ADR-001.
 */
interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByCpfAndReferencePeriod(String cpf, Integer referencePeriod);

    long countByReferencePeriod(Integer referencePeriod);
}
