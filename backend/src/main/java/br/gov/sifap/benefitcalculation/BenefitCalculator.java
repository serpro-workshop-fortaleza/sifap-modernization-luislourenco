package br.gov.sifap.benefitcalculation;

import br.gov.sifap.beneficiary.BeneficiarySnapshot;
import br.gov.sifap.shared.Money;
import br.gov.sifap.shared.ReferencePeriod;
import br.gov.sifap.socialprogram.ProgramRules;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Calcula o valor do beneficio de um periodo.
 *
 * <p>Traducao de {@code CALCBENF.NSN}. Cobre REQ-016 a REQ-020 e REQ-027.
 *
 * <p>Diferenca estrutural em relacao ao legado: este servico <strong>nao persiste</strong>.
 * {@code CALCBENF.NSN:319} grava o pagamento e {@code BATCHPGT.NSP:488} grava de novo, o
 * que produz a dupla gravacao de {@code SIFAP-M-05}. Aqui a persistencia pertence
 * exclusivamente ao contexto {@code payment}. Ver ADR-002.
 */
@Service
public class BenefitCalculator {

    private final DiscountCalculator discountCalculator;

    public BenefitCalculator(DiscountCalculator discountCalculator) {
        this.discountCalculator = discountCalculator;
    }

    public BenefitCalculation calculate(
            BeneficiarySnapshot beneficiary,
            ProgramRules program,
            ReferencePeriod period,
            List<Discount> discounts,
            LocalDate processingDate) {

        BigDecimal regionalFactor = BenefitFactors.regional(beneficiary.regionCode());
        BigDecimal familyFactor = BenefitFactors.family(beneficiary.activeDependents());
        BigDecimal incomeFactor = BenefitFactors.income(beneficiary.familyIncome());
        BigDecimal ageFactor = BenefitFactors.age(beneficiary.ageOn(period));

        // REQ-016 — base x regional x familiar x renda x etario, ajustado pelo fator do programa.
        Money grossAmount = program.individualBaseAmount()
                .multiply(regionalFactor)
                .multiply(familyFactor)
                .multiply(incomeFactor)
                .multiply(ageFactor)
                .multiply(BigDecimal.ONE.add(program.adjustmentFactor()))
                .truncate(); // REQ-020 — primeiro ponto de truncamento.

        Money totalDiscount = discountCalculator.calculate(grossAmount, discounts, processingDate);

        // REQ-027 — liquido nunca negativo. REQ-020 — segundo ponto de truncamento.
        Money netAmount = grossAmount.subtract(totalDiscount).atLeastZero().truncate();

        return new BenefitCalculation(
                grossAmount,
                totalDiscount,
                netAmount,
                PaymentKind.NORMAL,
                new AppliedFactors(regionalFactor, familyFactor, incomeFactor, ageFactor));
    }
}
