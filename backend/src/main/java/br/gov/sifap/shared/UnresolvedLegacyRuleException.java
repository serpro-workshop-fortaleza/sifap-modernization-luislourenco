package br.gov.sifap.shared;

/**
 * Sinaliza uma regra do legado cuja semantica correta ainda nao foi decidida por
 * uma pessoa responsavel.
 *
 * <p>O sistema falha de forma visivel em vez de adivinhar. Cada ocorrencia aponta
 * para um mistério aberto em {@code 01-archaeology/mysteries-found.md}.
 */
public class UnresolvedLegacyRuleException extends RuntimeException {

    private final String mysteryId;

    public UnresolvedLegacyRuleException(String mysteryId, String detail) {
        super("%s: %s. Decisao humana pendente — ver 01-archaeology/mysteries-found.md"
                .formatted(mysteryId, detail));
        this.mysteryId = mysteryId;
    }

    public String mysteryId() {
        return mysteryId;
    }
}
