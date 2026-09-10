package br.gov.sifap.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.CacheMode;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Verifica as fronteiras definidas em {@code 02-modern-spec/bounded-contexts.md}.
 *
 * <p>Sem estes testes, "monolito modular" e so uma intencao no documento.
 */
@AnalyzeClasses(packages = "br.gov.sifap", cacheMode = CacheMode.PER_CLASS)
class ModuleBoundaryTest {

    /**
     * ADR-002 — o calculo e dominio puro. Se ele puder alcancar persistencia, a dupla
     * gravacao de {@code SIFAP-M-05} pode voltar a existir.
     */
    @ArchTest
    static final ArchRule benefit_calculation_must_not_depend_on_persistence =
            noClasses()
                    .that()
                    .resideInAPackage("..benefitcalculation..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "jakarta.persistence..",
                            "org.springframework.data..",
                            "..payment..");

    /** O calculo tambem nao pode alcancar a camada web. */
    @ArchTest
    static final ArchRule benefit_calculation_must_not_depend_on_web =
            noClasses()
                    .that()
                    .resideInAPackage("..benefitcalculation..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("org.springframework.web..");

    /** Contextos so conversam pelas interfaces publicas; entidades ficam internas. */
    @ArchTest
    static final ArchRule entities_must_stay_inside_their_context =
            classes()
                    .that()
                    .areAnnotatedWith(jakarta.persistence.Entity.class)
                    .and()
                    .resideInAPackage("..beneficiary..")
                    .should()
                    .bePackagePrivate();

    /** {@code payment} nao acessa repositorios de outros contextos. */
    @ArchTest
    static final ArchRule payment_must_not_use_foreign_repositories =
            noClasses()
                    .that()
                    .resideInAPackage("..payment..")
                    .should()
                    .dependOnClassesThat()
                    .haveNameMatching(".*\\.(beneficiary|socialprogram)\\..*Repository");

    /** O kernel compartilhado nao pode depender de nenhum contexto. */
    @ArchTest
    static final ArchRule shared_kernel_must_not_depend_on_contexts =
            noClasses()
                    .that()
                    .resideInAPackage("br.gov.sifap.shared..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "..beneficiary..", "..socialprogram..", "..benefitcalculation..", "..payment..");
}
