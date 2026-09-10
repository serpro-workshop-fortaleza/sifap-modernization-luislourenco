package br.gov.sifap.socialprogram;

import br.gov.sifap.shared.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Programa social ({@code SOCPROG.ddm}, FNR 151).
 *
 * <p>Tabela de parametros com cerca de 45 registros. O grupo periodico
 * {@code GRP-CALC-BAND} e o MU {@code EA TYPE-DISC-APPLIC} sao tratados conforme ADR-001:
 * a faixa de calculo vira tabela filha (fora do recorte 001) e a lista de descontos
 * aplicaveis vira coluna de texto delimitado, por ser lista acessoria de baixa
 * cardinalidade.
 */
@Entity
@Table(name = "social_program")
class SocialProgramEntity {

    @Id
    @Column(name = "code", nullable = false, length = 4)
    private String code;

    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ProgramType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private ProgramStatus status;

    @Column(name = "individual_base_amount", nullable = false, precision = 9, scale = 2)
    private BigDecimal individualBaseAmount;

    @Column(name = "adjustment_factor", nullable = false, precision = 7, scale = 4)
    private BigDecimal adjustmentFactor;

    @Column(name = "maximum_income", nullable = false, precision = 9, scale = 2)
    private BigDecimal maximumIncome;

    @Column(name = "minimum_age", nullable = false)
    private Integer minimumAge;

    @Column(name = "maximum_age", nullable = false)
    private Integer maximumAge;

    @Column(name = "eligibility_code", length = 5)
    private String eligibilityCode;

    @Column(name = "applicable_discounts", length = 64)
    private String applicableDiscounts;

    protected SocialProgramEntity() {
        // exigido pelo JPA
    }

    ProgramRules toRules() {
        return new ProgramRules(
                code,
                name,
                type,
                status,
                Money.of(individualBaseAmount),
                adjustmentFactor,
                Money.of(maximumIncome),
                minimumAge,
                maximumAge,
                eligibilityCode,
                parseDiscounts());
    }

    private Set<DiscountType> parseDiscounts() {
        if (applicableDiscounts == null || applicableDiscounts.isBlank()) {
            return EnumSet.noneOf(DiscountType.class);
        }
        return Arrays.stream(applicableDiscounts.split(","))
                .map(String::trim)
                .map(DiscountType::fromLegacyCode)
                .flatMap(java.util.Optional::stream)
                .collect(Collectors.toUnmodifiableSet());
    }
}
