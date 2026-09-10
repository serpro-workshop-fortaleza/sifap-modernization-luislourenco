package br.gov.sifap.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CpfTest {

    // CPFs sinteticos com digito verificador valido.
    private static final String VALID_CPF = "52998224725";

    @Test
    @DisplayName("REQ-006 - aceita CPF com digito verificador correto")
    void should_accept_cpf_when_check_digits_are_valid() {
        assertThat(Cpf.of(VALID_CPF).unmasked()).isEqualTo(VALID_CPF);
    }

    @Test
    @DisplayName("REQ-006 - rejeita CPF com digito verificador incorreto")
    void should_reject_cpf_when_check_digit_is_wrong() {
        assertThatThrownBy(() -> Cpf.of("52998224726"))
                .isInstanceOf(Cpf.InvalidCpfException.class)
                .extracting(ex -> ((Cpf.InvalidCpfException) ex).reason())
                .isEqualTo(Cpf.Reason.CHECK_DIGIT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"5299822472a", "529.982.247-25", "1234567890"})
    @DisplayName("REQ-006 - rejeita CPF com caractere nao numerico ou tamanho errado")
    void should_reject_cpf_when_not_eleven_digits(String raw) {
        assertThatThrownBy(() -> Cpf.of(raw))
                .isInstanceOf(Cpf.InvalidCpfException.class)
                .extracting(ex -> ((Cpf.InvalidCpfException) ex).reason())
                .isEqualTo(Cpf.Reason.NON_NUMERIC);
    }

    @Test
    @DisplayName("REQ-006 - rejeita CPF ausente")
    void should_reject_cpf_when_not_provided() {
        assertThatThrownBy(() -> Cpf.of("  "))
                .isInstanceOf(Cpf.InvalidCpfException.class)
                .extracting(ex -> ((Cpf.InvalidCpfException) ex).reason())
                .isEqualTo(Cpf.Reason.NOT_PROVIDED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000000000", "11111111111", "99999999999"})
    @DisplayName("REQ-007 - rejeita CPF com todos os digitos iguais")
    void should_reject_cpf_when_all_digits_are_equal(String raw) {
        // BLOQUEADO por SIFAP-M-14: VALBENEF.NSN:239-242 aceita sequencias iniciadas em 000
        // como "CPF de teste de governo". Adotamos a regra restritiva de SUBVALCP.NSN:57-60.
        // Se a Coordenacao de Beneficios confirmar a excecao, este teste muda junto com a
        // condicao em Cpf.of.
        assertThatThrownBy(() -> Cpf.of(raw))
                .isInstanceOf(Cpf.InvalidCpfException.class)
                .extracting(ex -> ((Cpf.InvalidCpfException) ex).reason())
                .isEqualTo(Cpf.Reason.ALL_DIGITS_EQUAL);
    }

    @Test
    @DisplayName("REQ-031 - mascara o documento na exposicao")
    void should_mask_cpf_when_exposed() {
        assertThat(Cpf.of(VALID_CPF).masked()).isEqualTo("***.982.247-25");
    }

    @Test
    @DisplayName("REQ-031 - toString nunca expoe o documento completo")
    void should_mask_cpf_when_converted_to_string() {
        assertThat(Cpf.of(VALID_CPF).toString()).doesNotContain(VALID_CPF).startsWith("***.");
    }
}
