package br.gov.sifap.payment;

import br.gov.sifap.benefitcalculation.BenefitCalculation;
import br.gov.sifap.benefitcalculation.PaymentKind;
import br.gov.sifap.shared.Cpf;
import br.gov.sifap.shared.Money;
import br.gov.sifap.shared.ReferencePeriod;
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
import java.time.LocalDateTime;

/**
 * Pagamento gerado pela folha ({@code PAYMENT.ddm}, FNR 152).
 *
 * <p>REQ-028 — exatamente um registro por beneficiario e periodo, com identificador
 * proprio. O legado grava duas vezes e a gravacao do subprograma nunca atribui
 * {@code NUM-PAYMENT} ({@code SIFAP-M-05} e {@code SIFAP-M-06}); ver ADR-002.
 *
 * <p>A chave primaria nao deriva de ISN, conforme {@code PAYMENT.ddm:181-182} e ADR-001.
 */
@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "program_code", nullable = false, length = 4)
    private String programCode;

    @Column(name = "reference_period", nullable = false)
    private Integer referencePeriod;

    @Column(name = "gross_amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "total_discount", nullable = false, precision = 11, scale = 2)
    private BigDecimal totalDiscount;

    @Column(name = "net_amount", nullable = false, precision = 11, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 12)
    private PaymentKind kind;

    @Column(name = "generation_date", nullable = false)
    private LocalDate generationDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Payment() {
        // exigido pelo JPA
    }

    private Payment(
            String cpf,
            String programCode,
            Integer referencePeriod,
            BenefitCalculation calculation,
            LocalDate generationDate) {
        this.cpf = cpf;
        this.programCode = programCode;
        this.referencePeriod = referencePeriod;
        this.grossAmount = calculation.grossAmount().truncate().toBigDecimal();
        this.totalDiscount = calculation.totalDiscount().truncate().toBigDecimal();
        this.netAmount = calculation.netAmount().truncate().toBigDecimal();
        this.kind = calculation.kind();
        this.status = PaymentStatus.GENERATED;
        this.generationDate = generationDate;
        this.createdAt = LocalDateTime.now();
    }

    /** REQ-028 — status inicial sempre {@link PaymentStatus#GENERATED}. */
    public static Payment generate(
            Cpf cpf,
            String programCode,
            ReferencePeriod period,
            BenefitCalculation calculation,
            LocalDate generationDate) {
        return new Payment(
                cpf.unmasked(), programCode, period.toYyyymm(), calculation, generationDate);
    }

    public Long id() {
        return id;
    }

    public Cpf cpf() {
        return Cpf.of(cpf);
    }

    public String programCode() {
        return programCode;
    }

    public ReferencePeriod referencePeriod() {
        return ReferencePeriod.of(referencePeriod);
    }

    public Money netAmount() {
        return Money.of(netAmount);
    }

    public Money grossAmount() {
        return Money.of(grossAmount);
    }

    public Money totalDiscount() {
        return Money.of(totalDiscount);
    }

    public PaymentStatus status() {
        return status;
    }

    public PaymentKind kind() {
        return kind;
    }
}
