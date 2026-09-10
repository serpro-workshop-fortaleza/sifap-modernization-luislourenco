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
    @ValueSource(strings = {"00000791970", "00001583808", "00047514000"})
    @DisplayName("REQ-006 - resto da divisao por 11 menor que 2 produz digito verificador 0")
    void should_accept_cpf_when_check_digit_is_zero(String raw) {
        // Ramo IF #CPF-REMAIN < 2 de CCVALCPF.NSC:101-105 e :121-125. Os tres casos cobrem
        // digito zero no primeiro, no segundo e em ambos os verificadores.
        assertThat(Cpf.of(raw).unmasked()).isEqualTo(raw);
    }

    @Test
    @DisplayName("REQ-006 - o primeiro verificador usa pesos 10 a 2 sobre os 9 primeiros digitos")
    void should_reject_cpf_when_first_check_digit_diverges() {
        // 52998224725 e valido; alterar so a decima posicao isola o primeiro verificador.
        assertThatThrownBy(() -> Cpf.of("52998224715"))
                .isInstanceOf(Cpf.InvalidCpfException.class)
                .extracting(ex -> ((Cpf.InvalidCpfException) ex).reason())
                .isEqualTo(Cpf.Reason.CHECK_DIGIT);
    }

    @Test
    @DisplayName("REQ-006 - o segundo verificador usa pesos 11 a 2 sobre os 10 primeiros digitos")
    void should_reject_cpf_when_second_check_digit_diverges() {
        // Mantem o primeiro verificador correto e altera so a decima primeira posicao.
        assertThatThrownBy(() -> Cpf.of("52998224724"))
                .isInstanceOf(Cpf.InvalidCpfException.class)
                .extracting(ex -> ((Cpf.InvalidCpfException) ex).reason())
                .isEqualTo(Cpf.Reason.CHECK_DIGIT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"00000000000", "11111111111", "99999999999"})
    @DisplayName("REQ-007 - rejeita CPF com todos os digitos iguais")
    void should_reject_cpf_when_all_digits_are_equal(String raw) {
        // BLOQUEADO por SIFAP-M-14: quatro rotinas legadas discordam. Adotamos a regra
        // restritiva de CCVALCPF.NSC:79-90 (ADR-005, status Proposta). Se a Coordenacao de
        // Beneficios confirmar a excecao de VALBENEF.NSN:238-242, este teste muda junto com
        // a condicao em Cpf.of.
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
