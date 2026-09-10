package br.gov.sifap.beneficiary;

import br.gov.sifap.shared.Cpf;
import br.gov.sifap.shared.Money;
import br.gov.sifap.shared.ReferencePeriod;
import java.time.LocalDate;

/**
 * Retrato somente leitura do beneficiario, entregue ao contexto de calculo.
 *
 * <p>Substitui o padrao do legado em que {@code CALCBENF.NSN:179-196} rele os mesmos
 * registros que {@code BATCHPGT.NSP:247} acabou de ler — um N+1 sobre 4,2 milhoes de
 * linhas. Ver {@code 02-modern-spec/bounded-contexts.md}.
 *
 * @param rawCpf CPF como esta gravado no cadastro, <strong>sem validacao</strong>. A base
 *     legada contem documentos invalidos porque {@code CADBENEF.NSP:263-270} chama o
 *     validador em modo aviso, que nao bloqueia a gravacao ({@code SIFAP-M-02}). REQ-006
 *     valida na folha e REQ-030 rejeita sem interromper o ciclo.
 * @param regionCode codigo de regiao do DDM ({@code BENEFIC.ddm:77} — 01 a 05 ou 99)
 */
public record BeneficiarySnapshot(
        String rawCpf,
        String fullName,
        LocalDate birthDate,
        BeneficiaryStatus status,
        String programCode,
        Money familyIncome,
        int activeDependents,
        int regionCode,
        String federativeUnit,
        Long nis,
        boolean documentationComplete) {

    /**
     * REQ-006 — valida o documento por modulo 11.
     *
     * @throws Cpf.InvalidCpfException quando o documento gravado e invalido
     */
    public Cpf validatedCpf() {
        return Cpf.of(rawCpf);
    }

    /** REQ-009 e REQ-018 — idade em anos completos na data de referencia. */
    public int ageOn(ReferencePeriod period) {
        return period.completedAgeOn(birthDate);
    }

    /** REQ-013 — o codigo de elegibilidade pode exigir NIS registrado. */
    public boolean hasNis() {
        return nis != null && nis > 0;
    }

    /** REQ-013 — o codigo de elegibilidade pode exigir dependentes ativos. */
    public boolean hasDependents() {
        return activeDependents > 0;
    }
}
