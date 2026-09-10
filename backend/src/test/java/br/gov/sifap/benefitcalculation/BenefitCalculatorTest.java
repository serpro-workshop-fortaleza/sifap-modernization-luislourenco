package br.gov.sifap.benefitcalculation;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.sifap.shared.Money;
import br.gov.sifap.shared.ReferencePeriod;
import br.gov.sifap.testing.Fixtures;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BenefitCalculatorTest {

    private static final ReferencePeriod PERIOD = ReferencePeriod.of(202601);
    private static final LocalDate PROCESSING_DATE = LocalDate.of(2026, 1, 5);

    private final BenefitCalculator benefitCalculator = new BenefitCalculator(new DiscountCalculator());

    @Test
    @DisplayName("REQ-016 - bruto = base x regional x familiar x renda x etario x ajuste")
    void should_multiply_base_by_every_factor() {
        var beneficiary = Fixtures.beneficiary()
                .regionCode(1) // 1.3500
                .dependents(2) // 1.1000
                .familyIncome("250.00") // 1.0000
                .birthDate(LocalDate.of(1980, 5, 12)) // 46 anos -> 1.0000
                .build();
        var program = Fixtures.program().baseAmount("600.00").adjustmentFactor("0").build();

        var calculation = benefitCalculator.calculate(
                beneficiary, program, PERIOD, List.of(), PROCESSING_DATE);

        // 600.00 * 1.3500 * 1.1000 * 1.0000 * 1.0000 = 891.00
        assertThat(calculation.grossAmount()).isEqualTo(Money.of("891.00"));
    }

    @Test
    @DisplayName("REQ-016 - o fator de ajuste do programa incide sobre o bruto")
    void should_apply_program_adjustment_factor() {
        var beneficiary = Fixtures.beneficiary().regionCode(5).dependents(0).build();
        var program = Fixtures.program().baseAmount("1000.00").adjustmentFactor("0.1000").build();

        var calculation = benefitCalculator.calculate(
                beneficiary, program, PERIOD, List.of(), PROCESSING_DATE);

        // 1000.00 * 1.3100 * 1.0 * 1.0 * 1.0 * 1.1 = 1441.00
        assertThat(calculation.grossAmount()).isEqualTo(Money.of("1441.00"));
    }

    @Test
    @DisplayName("REQ-020 - o bruto e truncado, nao arredondado")
    void should_truncate_gross_amount_without_rounding() {
        var beneficiary = Fixtures.beneficiary().regionCode(3).dependents(1).build();
        var program = Fixtures.program().baseAmount("333.33").build();

        var calculation = benefitCalculator.calculate(
                beneficiary, program, PERIOD, List.of(), PROCESSING_DATE);

        // 333.33 * 1.3000 * 1.0500 = 454.99545 -> truncado para 454.99.
        // Com HALF_UP o resultado seria 455.00, um centavo a mais por beneficiario.
        assertThat(calculation.grossAmount()).isEqualTo(Money.of("454.99"));
    }

    @Test
    @DisplayName("REQ-027 - liquido = bruto - descontos")
    void should_subtract_discount_from_gross() {
        var beneficiary = Fixtures.beneficiary().regionCode(1).dependents(0).build();
        var program = Fixtures.program().baseAmount("1000.00").build();

        var calculation = benefitCalculator.calculate(
                beneficiary, program, PERIOD, List.of(), PROCESSING_DATE);

        assertThat(calculation.netAmount())
                .isEqualTo(calculation.grossAmount().subtract(calculation.totalDiscount()).truncate());
    }

    @Test
    @DisplayName("REQ-016 - os fatores aplicados sao expostos para a comparacao em sombra")
    void should_expose_applied_factors() {
        var beneficiary = Fixtures.beneficiary().regionCode(2).dependents(3).build();

        var calculation = benefitCalculator.calculate(
                beneficiary, Fixtures.program().build(), PERIOD, List.of(), PROCESSING_DATE);

        assertThat(calculation.factors().regional()).isEqualByComparingTo("1.3200");
        assertThat(calculation.factors().family()).isEqualByComparingTo("1.1300");
    }

    @Test
    @DisplayName("O calculo nao persiste nada - ADR-002")
    void should_return_calculation_without_side_effects() {
        var calculation = benefitCalculator.calculate(
                Fixtures.beneficiary().build(),
                Fixtures.program().build(),
                PERIOD,
                List.of(),
                PROCESSING_DATE);

        assertThat(calculation.kind()).isEqualTo(PaymentKind.NORMAL);
        assertThat(calculation).isInstanceOf(BenefitCalculation.class);
    }
}
