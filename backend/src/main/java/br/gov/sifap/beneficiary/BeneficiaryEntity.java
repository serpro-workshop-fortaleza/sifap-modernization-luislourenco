package br.gov.sifap.beneficiary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Beneficiario ({@code BENEFIC.ddm}, FNR 150).
 *
 * <p>Mapeamento conforme ADR-001: campos {@code P} do Adabas viram {@code BigDecimal} com
 * precisao e escala explicitas; o grupo periodico {@code GRP-DEPEND} vira a tabela filha
 * {@code dependent}, fora do escopo do recorte 001 — aqui so a contagem de dependentes
 * ativos ({@code CK QTY-DEPEND}) e necessaria.
 */
@Entity
@Table(name = "beneficiary")
class BeneficiaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "full_name", nullable = false, length = 60)
    private String fullName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 14)
    private BeneficiaryStatus status;

    @Column(name = "program_code", nullable = false, length = 4)
    private String programCode;

    @Column(name = "family_income", nullable = false, precision = 11, scale = 2)
    private BigDecimal familyIncome;

    @Column(name = "active_dependents", nullable = false)
    private Integer activeDependents;

    @Column(name = "region_code", nullable = false)
    private Integer regionCode;

    @Column(name = "federative_unit", length = 2)
    private String federativeUnit;

    @Column(name = "nis")
    private Long nis;

    @Column(name = "documentation_complete", nullable = false)
    private Boolean documentationComplete;

    protected BeneficiaryEntity() {
        // exigido pelo JPA
    }

    String cpf() {
        return cpf;
    }

    BeneficiaryStatus status() {
        return status;
    }

    BeneficiarySnapshot toSnapshot() {
        return new BeneficiarySnapshot(
                cpf,
                fullName,
                birthDate,
                status,
                programCode,
                br.gov.sifap.shared.Money.of(familyIncome),
                activeDependents,
                regionCode,
                federativeUnit,
                nis,
                Boolean.TRUE.equals(documentationComplete));
    }
}
