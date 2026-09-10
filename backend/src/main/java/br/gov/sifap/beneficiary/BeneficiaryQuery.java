package br.gov.sifap.beneficiary;

import java.util.List;
import java.util.Optional;

/**
 * Interface publica do contexto {@code beneficiary}.
 *
 * <p>Nenhum outro modulo importa entidades ou repositorios internos deste contexto — a
 * regra de fronteira esta verificada em {@code ModuleBoundaryTest}.
 */
public interface BeneficiaryQuery {

    /**
     * REQ-003 — somente beneficiarios ativos entram na folha
     * ({@code BATCHPGT.NSP:258-262}).
     *
     * <p>A ordenacao por CPF reproduz {@code BATCHPGT.NSP:247} ({@code READ ... BY NUM-CPF}).
     * O comentario em {@code BATCHPGT.NSP:242-245} avisa que sistemas a jusante dependem
     * dessa ordem, e o arquivo de remessa e um deles.
     */
    List<BeneficiarySnapshot> findActiveOrderedByCpf();

    Optional<BeneficiarySnapshot> findByCpf(String cpf);
}
