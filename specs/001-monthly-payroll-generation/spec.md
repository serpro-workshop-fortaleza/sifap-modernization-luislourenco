# Especificação — Geração mensal da folha de pagamento

> **Trilha:** [Kit do Time](../../README.md) › [Especificações](../README.md) › **001 — Folha mensal**

**Recorte vertical selecionado para o SIFAP 2.0.** Reproduz o fluxo que hoje é executado por `SIFAPJ01` → `BATCHPGT`, incluindo elegibilidade, cálculo, descontos e geração do arquivo de remessa.

| Campo | Valor |
|---|---|
| **Feature** | `001-monthly-payroll-generation` |
| **Estágio** | Estágio 2 — Especificação |
| **Contextos envolvidos** | `payment`, `beneficiary`, `socialprogram`, `benefitcalculation`, `audit` |
| **Origem legada** | `BATCHPGT.NSP`, `CALCBENF.NSN`, `VALELEG.NSN`, `CALCDSCT.NSP`, `SIFAPJ01.jcl` |
| **Contextos delimitados** | [`02-modern-spec/bounded-contexts.md`](../../02-modern-spec/bounded-contexts.md) |

---

## Escopo

### Selecionado

Geração da folha para um período `YYYYMM`: seleção de beneficiários ativos, verificação de elegibilidade, cálculo do valor bruto, aplicação de descontos, cálculo do líquido, persistência do pagamento, emissão do arquivo de remessa e registro da trilha de auditoria do ciclo.

### Adiado

| Item | Motivo | Origem legada |
|---|---|---|
| Conciliação bancária CNAB 240 | Depende de resolver `SIFAP-M-13` (semântica do status) | `BATCHCON.NSP` |
| Correção monetária retroativa | Depende de resolver `SIFAP-M-12` (índice não acumula) | `CALCCORR.NSP` |
| Cadastro de beneficiários e dependentes | Recorte seguinte; a folha consome o cadastro apenas em leitura | `CADBENEF.NSP`, `CADDEPEN.NSP` |
| Relatórios impressos | Modelos de leitura; sem regra de negócio própria | `RELPGT.NSP`, `BATCHREL.NSP` |
| 13º salário e abono de férias | Bloqueado por `SIFAP-M-07` (fórmula diverge da documentação) | `CALCBENF.NSN:275-288` |

---

## Convenções

- **Padrões EARS:** Ubíquo, Evento (`QUANDO`), Estado (`ENQUANTO`), Opcional (`ONDE`), Indesejado (`SE ... ENTÃO`), Complexo.
- Todo requisito tem `source_legacy:` apontando para o acervo ou marcado `[GREENFIELD]` com justificativa.
- Requisitos bloqueados por mistério aberto trazem `blocked_by:` e **não** entram em `tasks.md` antes da validação humana.

---

## Requisitos

### Seleção e controle do ciclo

---

**REQ-001** — O sistema DEVE processar a folha para exatamente um período de referência no formato `YYYYMM` informado na solicitação.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:170-183`
- **Critérios de aceitação:**
  - Período `202601` é aceito e processado.
  - Período ausente resulta em erro de validação, sem derivar da data corrente.
- **Nota de divergência deliberada:** o legado deriva o período de `*DATN` quando a entrada é zero (`BATCHPGT.NSP:177-179`). O sistema moderno exige o período explicitamente, porque a derivação implícita impede reprocessamento auditável.

---

**REQ-002** — SE já existir pagamento gerado para o par CPF e período de referência, ENTÃO o sistema DEVE ignorar o beneficiário e incrementar o contador de ignorados.

- **Padrão EARS:** Indesejado
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:295-301`
- **Critérios de aceitação:**
  - Segunda execução do mesmo período não cria pagamentos adicionais.
  - O contador de ignorados reflete a quantidade de duplicatas evitadas.
- **Nota:** garante a reexecutabilidade declarada em `SIFAPJ01.jcl:25-27`. No legado o índice é o superdescritor `SUPER-CPF-PERIOD` (`PAYMENT.ddm:159-160`).

---

**REQ-003** — ENQUANTO o beneficiário estiver com status diferente de `ATIVO`, o sistema DEVE excluí-lo da folha.

- **Padrão EARS:** Estado
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:258-262`
- **Critérios de aceitação:**
  - Status `SUSPENSO`, `CANCELADO`, `INATIVO` e `DESLIGADO` não geram pagamento.
  - Somente `ATIVO` prossegue.

---

**REQ-004** — QUANDO o ciclo terminar, o sistema DEVE registrar na trilha de auditoria um evento de ação `BT` contendo o período e a quantidade de pagamentos gerados.

- **Padrão EARS:** Evento
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:531-540`
- **Critérios de aceitação:**
  - Um evento por ciclo, com `TYPE-ENTITY = PGTO`.
  - O evento persiste mesmo quando zero pagamentos foram gerados.
- **Base legal:** IN-TCU 63/2010, citada em `AUDIT.ddm:15`.

---

**REQ-005** — O sistema DEVE encerrar o ciclo com um resultado distinto para cada desfecho: sucesso sem rejeições, sucesso com rejeições, nada a processar e falha técnica.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:556-570`
- **Critérios de aceitação:**
  - Os quatro desfechos são distinguíveis programaticamente.
  - O contrato preserva a semântica de `SIFAPJ01.jcl:99-104` (RC 0, 4, 8, 12).

---

### Validação de documento

---

**REQ-006** — QUANDO um beneficiário for selecionado, o sistema DEVE validar o CPF por módulo 11 antes de qualquer cálculo.

- **Padrão EARS:** Evento
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/SUBVALCP.NSN:70-80`
- **Critérios de aceitação:**
  - CPF com dígito verificador incorreto é rejeitado.
  - CPF com caractere não numérico é rejeitado.
  - O beneficiário rejeitado entra no registro de rejeitados e não gera pagamento.

---

**REQ-007** — O sistema DEVE tratar CPF com todos os dígitos iguais como inválido, incluindo `00000000000`.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/SUBVALCP.NSN:57-60`
- `blocked_by: SIFAP-M-14`
- **Critérios de aceitação:**
  - `00000000000` é rejeitado.
  - `11111111111` é rejeitado.
- **Conflito registrado:** `VALBENEF.NSN:239-242` aceita explicitamente sequências iniciadas em `000` como "CPF de teste de governo". Esta especificação adota a regra restritiva de `SUBVALCP` por ser a rotina declarada como fonte única (`SUBVALCP.NSN:8`). **A decisão exige confirmação humana antes da implementação**, pois pode excluir registros existentes na base.

---

### Elegibilidade

---

**REQ-008** — SE o programa social do beneficiário não estiver ativo, ENTÃO o sistema DEVE considerá-lo inelegível e interromper a avaliação.

- **Padrão EARS:** Indesejado
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:114-118`
- **Critérios de aceitação:** programa com status `INATIVO` ou `ENCERRADO` impede a geração.

---

**REQ-009** — ONDE o programa social definir idade mínima ou máxima, o sistema DEVE considerar inelegível o beneficiário cuja idade esteja fora do intervalo.

- **Padrão EARS:** Opcional
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:163-178`
- **Critérios de aceitação:**
  - Limite igual a zero significa "sem restrição" e não é aplicado.
  - A idade é calculada em anos completos na data de referência.

---

**REQ-010** — ONDE o programa social for do tipo assistencial, o sistema DEVE exigir documentação completa do beneficiário.

- **Padrão EARS:** Opcional
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:196-212`
- **Critérios de aceitação:** indicador de documentação diferente de "sim" torna o beneficiário inelegível.

---

**REQ-011** — ONDE o programa social for previdenciário, o sistema DEVE exigir idade mínima de 60 anos; ONDE for de trabalho e emprego, DEVE exigir idade entre 16 e 65 anos.

- **Padrão EARS:** Opcional
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:213-224`
- **Critérios de aceitação:** as três combinações de tipo de programa produzem as decisões descritas.

---

**REQ-012** — SE o tipo do programa social não for reconhecido, ENTÃO o sistema DEVE considerar o beneficiário inelegível.

- **Padrão EARS:** Indesejado
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:225-229`
- **Critérios de aceitação:** falha fechada, nunca aberta.

---

**REQ-013** — ONDE o código de elegibilidade do programa exigir NIS na primeira posição, o sistema DEVE considerar inelegível o beneficiário sem NIS registrado; ONDE exigir dependentes na segunda posição, DEVE considerar inelegível quem não tiver dependentes ativos.

- **Padrão EARS:** Opcional
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:258-276`
- **Critérios de aceitação:** as posições do código são avaliadas de forma independente e cumulativa.

---

**REQ-014** — O sistema DEVE registrar todos os motivos de inelegibilidade encontrados, não apenas o primeiro.

- **Padrão EARS:** Ubíquo
- `source_legacy: [GREENFIELD]` — o legado acumula até 10 motivos em `VALELEG.NSN:135-231` mas devolve somente `#REASON(1)` ao chamador (`VALELEG.NSN:233-236`), descartando o restante. Preservar todos é necessário para que o beneficiário saiba o que corrigir.
- **Critérios de aceitação:** uma avaliação com três impedimentos retorna os três.

---

**REQ-015** — ONDE o código de região do beneficiário for `99`, o sistema DEVE aplicar tratamento de região especial.

- **Padrão EARS:** Opcional
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/VALELEG.NSN:123-129`
- `blocked_by: SIFAP-M-09`
- **Critérios de aceitação:** pendentes de definição humana.
- **Bloqueio:** no legado, a região `99` concede elegibilidade **antes** de verificar status, idade e renda, permitindo que um beneficiário cancelado seja considerado elegível. Esta especificação **não reproduz** esse comportamento sem confirmação escrita da Coordenação de Benefícios de que o desvio é intencional.

---

### Cálculo do valor

---

**REQ-016** — O sistema DEVE calcular o valor bruto como o produto do valor base do programa pelos fatores regional, familiar, de renda e etário, ajustado pelo fator do programa.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:258-262`
- **Critérios de aceitação:**
  - `bruto = base × f_regional × f_familiar × f_renda × f_etário × (1 + f_ajuste)`.
  - Um caso de referência por combinação de fatores reproduz o centavo exato do legado.

---

**REQ-017** — O sistema DEVE determinar o fator familiar por faixas de dependentes: acréscimo de 0,05 por dependente até 2, de 0,03 do terceiro ao quarto e de 0,02 a partir do quinto.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:216-228`
- **Critérios de aceitação:** 0 dependentes → 1,0000; 2 → 1,1000; 4 → 1,1600; 6 → 1,2000.

---

**REQ-018** — O sistema DEVE determinar o fator etário como 1,15 a partir de 65 anos, 1,10 a partir de 60, 1,05 abaixo de 18 e 1,00 nos demais casos.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:246-256`
- **Critérios de aceitação:** os quatro intervalos são cobertos, incluindo as fronteiras 18, 60 e 65.

---

**REQ-019** — O sistema DEVE determinar o fator de renda pela primeira faixa cujo teto seja maior ou igual à renda familiar declarada.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:330-336`
- **Critérios de aceitação:** tetos 300 → 1,0000; 600 → 0,8500; 1000 → 0,7000; 1500 → 0,5500; acima → 0,4000.

---

**REQ-020** — O sistema DEVE truncar todo valor monetário em duas casas decimais, sem arredondamento.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:265-267`
- **Critérios de aceitação:**
  - `123,459` resulta em `123,45`, não `123,46`.
  - A regra vale para bruto, desconto, líquido e abono.
- **Nota:** o legado usa multiplicação e divisão inteira por 100. Ver [ADR-003](../../02-modern-spec/adr/ADR-003-representacao-monetaria.md).

---

**REQ-021** — O sistema DEVE determinar o fator regional a partir da região do beneficiário.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:201`
- `blocked_by: SIFAP-M-20`
- **Critérios de aceitação:** pendentes de definição humana.
- **Bloqueio:** a tabela legada tem 27 posições rotuladas por UF (`LDASIFAP.NSL:36-46`) mas é indexada por `COD-REGION`, que o DDM define como `01-05` ou `99` (`BENEFIC.ddm:77`). Somente as 5 primeiras posições são alcançáveis e a região `99` recebe fator 1,0000 pelo caminho de exceção. **Não é possível saber, apenas pelo código, se o comportamento correto é por região ou por UF.** Reproduzir o defeito e corrigi-lo produzem folhas diferentes para milhões de pessoas.

---

### Descontos

---

**REQ-022** — O sistema DEVE calcular a contribuição social por faixa progressiva sobre o valor bruto.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCDSCT.NSP:196-205`
- **Critérios de aceitação:** até 500 → 3%; até 1.000 → 5%; até 2.000 → 7%; acima → 9%.
- **Nota de divergência deliberada:** o batch legado aplica 3% fixo (`BATCHPGT.NSP:458-462`) porque `CALCDSCT` nunca é chamado (`SIFAP-M-08`). Esta especificação adota a tabela progressiva por ser a regra completa e documentada. **A diferença de valor precisa ser quantificada antes da virada.**

---

**REQ-023** — O sistema DEVE limitar o total de descontos a 30% do valor bruto.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCDSCT.NSP:107`
- **Critérios de aceitação:** desconto calculado acima do teto é reduzido ao teto.

---

**REQ-024** — ONDE o desconto for de origem judicial, o sistema DEVE aplicá-lo integralmente, sem sujeitá-lo ao teto de 30%.

- **Padrão EARS:** Opcional
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCDSCT.NSP:130-141`, `:172-177`
- **Critérios de aceitação:**
  - Desconto judicial de 50% do bruto é aplicado integralmente.
  - O teto continua valendo para os demais tipos, calculado sobre a parcela não judicial.
- **Nota de divergência deliberada:** no legado, judicial e não judicial somam no mesmo acumulador e o teto é aplicado dentro do laço (`CALCDSCT.NSP:172-177`), tornando o resultado **dependente da ordem** dos itens no grupo periódico. A especificação separa os acumuladores para tornar o resultado determinístico.

---

**REQ-025** — SE o desconto estiver fora do seu período de vigência na data de processamento, ENTÃO o sistema DEVE ignorá-lo.

- **Padrão EARS:** Indesejado
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCDSCT.NSP:113-121`
- **Critérios de aceitação:** data final igual a zero significa vigência aberta.

---

**REQ-026** — SE o tipo de desconto não for reconhecido, ENTÃO o sistema DEVE rejeitar o pagamento e registrar o tipo desconhecido.

- **Padrão EARS:** Indesejado
- `source_legacy: [GREENFIELD]` — o legado ignora silenciosamente tipos não reconhecidos (`CALCDSCT.NSP:168-169`), o que faz consignados, empréstimos e taxas desaparecerem do cálculo (`SIFAP-M-11`). Falhar de forma visível é obrigatório em sistema financeiro.
- **Critérios de aceitação:** um desconto de tipo desconhecido interrompe o pagamento daquele beneficiário e o envia ao registro de rejeitados.

---

**REQ-027** — O sistema DEVE calcular o valor líquido como o valor bruto menos o total de descontos, nunca inferior a zero.

- **Padrão EARS:** Ubíquo
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/CALCBENF.NSN:296-300`
- **Critérios de aceitação:** desconto superior ao bruto resulta em líquido zero.

---

### Persistência e saída

---

**REQ-028** — QUANDO o cálculo for concluído com sucesso, o sistema DEVE persistir exatamente um pagamento por beneficiário e período, com status inicial `GERADO`.

- **Padrão EARS:** Evento
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:471-489`
- **Critérios de aceitação:**
  - Um beneficiário elegível produz exatamente um registro.
  - O identificador do pagamento é único e atribuído explicitamente.
- **Nota de divergência deliberada:** o legado grava **duas vezes** — em `CALCBENF.NSN:319` e em `BATCHPGT.NSP:488` (`SIFAP-M-05`) — e a gravação do subprograma nunca atribui o identificador (`SIFAP-M-06`). Ver [ADR-002](../../02-modern-spec/adr/ADR-002-fonte-unica-de-calculo.md).

---

**REQ-029** — QUANDO um pagamento for gerado, o sistema DEVE emitir um registro correspondente no arquivo de remessa bancária.

- **Padrão EARS:** Evento
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:492-503`
- **Critérios de aceitação:**
  - Um registro de remessa por pagamento gerado.
  - Valor expresso em centavos, sem separador decimal.
  - Registro de detalhe identificado pelo tipo `3`.

---

**REQ-030** — SE o CPF for inválido ou o programa social não for encontrado, ENTÃO o sistema DEVE registrar o beneficiário no arquivo de rejeitados com o motivo e prosseguir para o próximo.

- **Padrão EARS:** Indesejado
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/BATCHPGT.NSP:279-291`, `:307-318`
- **Critérios de aceitação:**
  - Uma rejeição não interrompe o ciclo.
  - O motivo é legível e identifica o beneficiário.

---

**REQ-031** — O sistema NÃO DEVE registrar CPF sem máscara em log, relatório ou mensagem de erro.

- **Padrão EARS:** Ubíquo (restrição)
- `source_legacy: 01-archaeology/legacy-sifap/natural-programs/RELPGT.NSP:172-176`
- **Critérios de aceitação:**
  - CPF aparece como `***.NNN.NNN-NN`.
  - Nenhum log de aplicação contém os três primeiros dígitos.
- **Nota:** o legado já mascara CPF no relatório, mas grava CPF completo nos logs do batch (`BATCHPGT.NSP:280-283`). O mascaramento passa a valer em toda saída.

---

## Rastreabilidade

| Origem legada | Requisitos derivados |
|---|---|
| `BATCHPGT.NSP` | REQ-001 a REQ-005, REQ-028 a REQ-031 |
| `SUBVALCP.NSN` | REQ-006, REQ-007 |
| `VALELEG.NSN` | REQ-008 a REQ-015 |
| `CALCBENF.NSN` | REQ-016 a REQ-021, REQ-027 |
| `CALCDSCT.NSP` | REQ-022 a REQ-026 |
| `SIFAPJ01.jcl` | REQ-001, REQ-005 |
| `[GREENFIELD]` | REQ-014, REQ-026 |

**31 requisitos.** 29 fundamentados no legado, 2 greenfield justificados.

---

## Requisitos bloqueados

Os três requisitos abaixo **não podem ser implementados** antes de validação humana. Estão na especificação para que a lacuna seja visível, não para serem construídos.

| Requisito | Mistério | Quem decide |
|---|---|---|
| REQ-007 | `SIFAP-M-14` — dois validadores de CPF discordam | Coordenação de Benefícios |
| REQ-015 | `SIFAP-M-09` — região 99 ignora todas as verificações | Coordenação de Benefícios |
| REQ-021 | `SIFAP-M-20` — tabela de fator regional indexada de forma inconsistente | Coordenação de Benefícios |

---

## Decisões de arquitetura relacionadas

- [ADR-001 — Mapeamento de estruturas Adabas para PostgreSQL](../../02-modern-spec/adr/ADR-001-mapeamento-adabas-postgresql.md)
- [ADR-002 — Fonte única de cálculo do benefício](../../02-modern-spec/adr/ADR-002-fonte-unica-de-calculo.md)
- [ADR-003 — Representação monetária e truncamento](../../02-modern-spec/adr/ADR-003-representacao-monetaria.md)
- [ADR-004 — Coexistência com o legado por Strangler Fig](../../02-modern-spec/adr/ADR-004-coexistencia-strangler-fig.md)

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [Bounded contexts](../../02-modern-spec/bounded-contexts.md)<br/><sub>Fronteiras dos módulos.</sub> | `plan.md`<br/><sub>Gerar com `/speckit.plan`.</sub> |

<sub>[Voltar ao índice do kit](../../README.md)</sub>
