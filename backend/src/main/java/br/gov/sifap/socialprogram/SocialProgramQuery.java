package br.gov.sifap.socialprogram;

import java.util.Optional;

/**
 * Interface publica do contexto {@code socialprogram}.
 *
 * <p>REQ-005 do catalogo — {@code BATCHPGT.NSP:304-325} distingue "programa inexistente"
 * de "programa inativo": o primeiro rejeita o beneficiario, o segundo apenas o ignora.
 * {@link Optional#empty()} representa a inexistencia; {@link ProgramStatus} representa a
 * inatividade.
 */
public interface SocialProgramQuery {

    Optional<ProgramRules> findByCode(String code);
}
