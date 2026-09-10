package br.gov.sifap.shared;

/**
 * CPF sem mascara, com 11 posicoes.
 *
 * <p>REQ-006 — validacao por modulo 11 antes de qualquer calculo
 * ({@code SUBVALCP.NSN:70-80}).
 *
 * <p>REQ-031 — {@link #masked()} e a unica forma de expor o documento em log,
 * relatorio ou mensagem de erro. {@link #toString()} tambem mascara, para que um
 * log acidental nao vaze o numero completo.
 */
public final class Cpf {

    private static final int LENGTH = 11;

    private final String digits;

    private Cpf(String digits) {
        this.digits = digits;
    }

    /**
     * @throws InvalidCpfException quando o documento nao passa na validacao
     */
    public static Cpf of(String raw) {
        String candidate = raw == null ? "" : raw.trim();

        if (candidate.isEmpty()) {
            throw new InvalidCpfException(Reason.NOT_PROVIDED);
        }
        if (!candidate.matches("\\d{" + LENGTH + "}")) {
            throw new InvalidCpfException(Reason.NON_NUMERIC);
        }
        if (hasAllDigitsEqual(candidate)) {
            // REQ-007 (bloqueado por SIFAP-M-14): VALBENEF.NSN:239-242 aceita sequencias
            // iniciadas em 000 como "CPF de teste de governo"; SUBVALCP.NSN:57-60 rejeita.
            // Adotamos a regra restritiva de SUBVALCP, que e a rotina declarada como fonte
            // unica. Reverter e trocar esta condicao — nao ha outra dependencia.
            throw new InvalidCpfException(Reason.ALL_DIGITS_EQUAL);
        }
        if (!hasValidCheckDigits(candidate)) {
            throw new InvalidCpfException(Reason.CHECK_DIGIT);
        }
        return new Cpf(candidate);
    }

    private static boolean hasAllDigitsEqual(String candidate) {
        return candidate.chars().distinct().count() == 1;
    }

    private static boolean hasValidCheckDigits(String candidate) {
        int firstCheckDigit = checkDigit(candidate, 9);
        int secondCheckDigit = checkDigit(candidate, 10);
        return firstCheckDigit == digitAt(candidate, 9) && secondCheckDigit == digitAt(candidate, 10);
    }

    private static int checkDigit(String candidate, int upToPosition) {
        int weight = upToPosition + 1;
        int sum = 0;
        for (int position = 0; position < upToPosition; position++) {
            sum += digitAt(candidate, position) * weight--;
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static int digitAt(String candidate, int position) {
        return candidate.charAt(position) - '0';
    }

    public String unmasked() {
        return digits;
    }

    /** REQ-031 — formato {@code ***.NNN.NNN-NN}, igual ao de {@code RELPGT.NSP:172-176}. */
    public String masked() {
        return "***.%s.%s-%s".formatted(
                digits.substring(3, 6), digits.substring(6, 9), digits.substring(9, 11));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Cpf cpf && this.digits.equals(cpf.digits);
    }

    @Override
    public int hashCode() {
        return digits.hashCode();
    }

    @Override
    public String toString() {
        return masked();
    }

    public enum Reason {
        NOT_PROVIDED("CPF nao informado"),
        NON_NUMERIC("CPF contem caractere nao numerico"),
        ALL_DIGITS_EQUAL("CPF com todos os digitos iguais"),
        CHECK_DIGIT("CPF invalido - digito verificador");

        private final String description;

        Reason(String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }
    }

    public static class InvalidCpfException extends RuntimeException {

        private final transient Reason reason;

        InvalidCpfException(Reason reason) {
            super(reason.description());
            this.reason = reason;
        }

        public Reason reason() {
            return reason;
        }
    }
}
