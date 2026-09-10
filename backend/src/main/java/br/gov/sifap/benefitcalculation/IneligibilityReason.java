package br.gov.sifap.benefitcalculation;

/**
 * Motivo pelo qual um beneficiario nao e elegivel a um programa.
 *
 * <p>REQ-014 — o legado acumula ate 10 motivos em {@code VALELEG.NSN:135-231} e devolve
 * somente {@code #REASON(1)} ao chamador ({@code VALELEG.NSN:233-236}). Aqui todos os
 * motivos sao preservados.
 */
public enum IneligibilityReason {

    /** REQ-008 — {@code VALELEG.NSN:114-118}. */
    PROGRAM_NOT_ACTIVE("Programa social inativo"),

    /** REQ-003 e REQ-024 do catalogo — {@code VALELEG.NSN:134-158}. */
    BENEFICIARY_SUSPENDED("Beneficiario suspenso"),
    BENEFICIARY_CANCELED("Beneficiario cancelado ou desligado"),
    BENEFICIARY_INACTIVE("Beneficiario inativo"),

    /** REQ-009 — {@code VALELEG.NSN:163-178}. */
    AGE_BELOW_PROGRAM_MINIMUM("Idade abaixo do minimo do programa"),
    AGE_ABOVE_PROGRAM_MAXIMUM("Idade acima do maximo do programa"),

    /** REQ-026 do catalogo — {@code VALELEG.NSN:183-191}. */
    INCOME_ABOVE_PROGRAM_CEILING("Renda familiar acima do teto do programa"),

    /** REQ-010 — {@code VALELEG.NSN:196-212}. */
    INCOMPLETE_DOCUMENTATION("Documentacao incompleta"),
    ASSISTANCE_INCOME_WITHOUT_DEPENDENTS("Programa assistencial: renda acima de 600 sem dependentes"),

    /** REQ-011 — {@code VALELEG.NSN:213-224}. */
    SOCIAL_SECURITY_AGE_BELOW_60("Programa previdenciario: idade menor que 60"),
    EMPLOYMENT_AGE_OUTSIDE_RANGE("Programa de trabalho: idade fora do intervalo 16-65"),

    /** REQ-012 — {@code VALELEG.NSN:225-229}. */
    UNKNOWN_PROGRAM_TYPE("Tipo de programa desconhecido"),

    /** REQ-013 — {@code VALELEG.NSN:258-276}. */
    NIS_NOT_REGISTERED("NIS nao registrado"),
    DEPENDENTS_REQUIRED("Programa exige dependentes");

    private final String description;

    IneligibilityReason(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
