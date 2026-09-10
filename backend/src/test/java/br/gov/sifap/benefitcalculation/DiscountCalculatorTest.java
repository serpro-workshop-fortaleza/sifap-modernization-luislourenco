package br.gov.sifap.benefitcalculation;

import static org.assertj.core.api.Assertions.assertThat;

import br.gov.sifap.shared.Money;
import br.gov.sifap.socialprogram.DiscountType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DiscountCalculatorTest {

    private static final LocalDate PROCESSING_DATE = LocalDate.of(2026, 1, 5);

    private final DiscountCalculator discountCalculator = new DiscountCalculator();

    @ParameterizedTest
    @CsvSource({
        "400.00, 12.00",
        "500.00, 15.00",
        "800.00, 40.00",
        "1000.00, 50.00",
        "1500.00, 105.00",
        "2000.00, 140.00",
        "3000.00, 270.00"
    })
    @DisplayName("REQ-022 - contribuicao social por faixa progressiva")
    void should_apply_progressive_contribution_band(String gross, String expected) {
        // Divergencia deliberada: BATCHPGT.NSP:458-462 aplica 3% fixo porque CALCDSCT
        // nunca e chamado (SIFAP-M-08). Adotamos a tabela completa de CALCDSCT.NSP:196-205.
        Money total = discountCalculator.calculate(Money.of(gross), List.of(), PROCESSING_DATE);

        assertThat(total).isEqualTo(Money.of(expected));
    }

    @Test
    @DisplayName("REQ-023 - total de descontos limitado a 30% do bruto")
    void should_cap_total_discount_at_thirty_percent() {
        Money total = discountCalculator.calculate(
                Money.of("1000.00"),
                List.of(Discount.percentage(DiscountType.INCOME_TAX, "40")),
                PROCESSING_DATE);

        assertThat(total).isEqualTo(Money.of("300.00"));
    }

    @Test
    @DisplayName("REQ-024 - desconto judicial nao se sujeita ao teto")
    void should_apply_court_ordered_discount_in_full() {
        Money total = discountCalculator.calculate(
                Money.of("1000.00"),
                List.of(Discount.percentage(DiscountType.COURT_ORDERED, "50")),
                PROCESSING_DATE);

        // 50% judicial (500) + contribuicao social de 5% (50), sem teto sobre o judicial.
        assertThat(total).isEqualTo(Money.of("550.00"));
    }

    @Test
    @DisplayName("REQ-024 - resultado independe da ordem dos descontos")
    void should_produce_same_total_regardless_of_discount_order() {
        List<Discount> ascending = List.of(
                Discount.percentage(DiscountType.COURT_ORDERED, "50"),
                Discount.percentage(DiscountType.INCOME_TAX, "40"));
        List<Discount> descending = List.of(
                Discount.percentage(DiscountType.INCOME_TAX, "40"),
                Discount.percentage(DiscountType.COURT_ORDERED, "50"));

        // No legado o teto e aplicado dentro do laco sobre um acumulador unico
        // (CALCDSCT.NSP:172-177), o que torna o total dependente da ordem.
        Money first = discountCalculator.calculate(Money.of("1000.00"), ascending, PROCESSING_DATE);
        Money second = discountCalculator.calculate(Money.of("1000.00"), descending, PROCESSING_DATE);

        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("REQ-025 - desconto encerrado antes do processamento e ignorado")
    void should_ignore_discount_when_period_has_ended() {
        Discount expired = new Discount(
                DiscountType.ALIMONY,
                Money.of("100.00"),
                null,
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2025, 12, 31),
                null);

        Money total = discountCalculator.calculate(Money.of("1000.00"), List.of(expired), PROCESSING_DATE);

        assertThat(total).isEqualTo(Money.of("50.00")); // apenas a contribuicao social
    }

    @Test
    @DisplayName("REQ-025 - desconto ainda nao iniciado e ignorado")
    void should_ignore_discount_when_period_has_not_started() {
        Discount future = new Discount(
                DiscountType.ALIMONY,
                Money.of("100.00"),
                null,
                LocalDate.of(2026, 6, 1),
                null,
                null);

        Money total = discountCalculator.calculate(Money.of("1000.00"), List.of(future), PROCESSING_DATE);

        assertThat(total).isEqualTo(Money.of("50.00"));
    }

    @Test
    @DisplayName("REQ-025 - data final nula significa vigencia aberta")
    void should_apply_discount_when_end_date_is_open() {
        Discount open = new Discount(
                DiscountType.ALIMONY, Money.of("100.00"), null, LocalDate.of(2020, 1, 1), null, null);

        Money total = discountCalculator.calculate(Money.of("1000.00"), List.of(open), PROCESSING_DATE);

        assertThat(total).isEqualTo(Money.of("150.00"));
    }

    @Test
    @DisplayName("REQ-026 - consignado entra no calculo em vez de ser ignorado")
    void should_include_payroll_deducted_discount() {
        // No legado, 'CS' cai no ramo NONE do DECIDE por truncamento A3 -> A1 e desaparece
        // do calculo (SIFAP-M-11).
        Money total = discountCalculator.calculate(
                Money.of("1000.00"),
                List.of(Discount.fixed(DiscountType.PAYROLL_DEDUCTED, Money.of("80.00"))),
                PROCESSING_DATE);

        assertThat(total).isEqualTo(Money.of("130.00"));
    }

    @Test
    @DisplayName("REQ-020 - o total de descontos e truncado")
    void should_truncate_total_discount() {
        Money total = discountCalculator.calculate(Money.of("333.33"), List.of(), PROCESSING_DATE);

        // 333.33 * 0.03 = 9.9999 -> truncado para 9.99
        assertThat(total).isEqualTo(Money.of("9.99"));
    }
}
