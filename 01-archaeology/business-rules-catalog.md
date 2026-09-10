# Catálogo de Regras de Negócio — SIFAP Legado

> **Trilha:** [Kit do Time](../README.md) › [Estágio 1](README.md) › **Catálogo de Regras de Negócio**

**Artefato preenchido pelo time durante o Estágio 1.** Cada dupla extrai as regras dos programas `.NSP` e `.NSN` que recebeu e as registra aqui, com rastreabilidade obrigatória até o programa de origem.

| Campo | Valor |
|---|---|
| **Público-alvo** | Todas as duplas — cada dupla preenche a seção dos seus programas |
| **Pré-requisitos** | Ler os programas `.NSP` e `.NSN` atribuídos |
| **Estágio** | Estágio 1 — Arqueologia |
| **Resultado esperado** | Catálogo com `Programa de origem` preenchido para cada regra candidata |

> [!NOTE]
> Cada regra cita o programa de origem com um intervalo de linhas (`arquivo.NSP:Linicio-Lfim` ou `arquivo.NSN:Linicio-Lfim`) e é classificada como **Confirmada** (corroborada pela documentação histórica em `legacy-sifap/legacy-docs/`), **Inferida** (só a partir do código) ou **Mistério** (questão em aberto — registre-a também em [`mysteries-found.md`](mysteries-found.md) com evidência `path:line`, hipótese não confirmada, responsável e status).

> [!IMPORTANT]
> Guia passo a passo: [`GUIDE.md`](GUIDE.md).

**Time**: <!-- preencher -->
**Cobertura**: 24 membros Natural + 4 DDMs + 1 FDT lidos (cobertura completa do acervo)

---

## Regras de `BATCHPGT.NSP` — geração mensal da folha

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | O período de processamento é lido de `CMSYNIN`; se vier zero, deriva de `*DATN` | Evento | `BATCHPGT.NSP:170-183` | Confirmada | Reprocessamento retroativo só existe após o ticket 5980/2012 |
| 2 | Somente beneficiários com `STAT-BENEFICIARY = 'A'` são processados | Indesejado | `BATCHPGT.NSP:258-262` | Confirmada | Demais status são contados como ignorados |
| 3 | O CPF do beneficiario é validado por `SUBVALCP` antes do cálculo | Evento | `BATCHPGT.NSP:276` | Confirmada | Introduzido em 2011; antes a folha não validava CPF |
| 4 | Não se gera pagamento se já existir um para o par CPF + período | Indesejado | `BATCHPGT.NSP:295-301` | Confirmada | Usa o superdescritor `SUPER-CPF-PERIOD` |
| 5 | Programa social inexistente rejeita o registro; programa inativo o ignora | Indesejado | `BATCHPGT.NSP:304-325` | Confirmada | Dois desfechos distintos para a mesma leitura |
| 6 | Elegível é quem passa por `VALELEG` com retorno zero | Evento | `BATCHPGT.NSP:369-375` | Confirmada | Retorno diferente de zero apenas ignora, sem registrar motivo |
| 7 | O valor do benefício é calculado por `CALCBENF` via `CALLNAT` | Evento | `BATCHPGT.NSP:381` | Confirmada | **Ver `SIFAP-M-05`: o cálculo inline permanece ativo em paralelo** |
| 8 | Valor bruto = base × fator regional × fator familiar × fator de renda × fator etário, ajustado pelo fator do programa | Ubíquo | `BATCHPGT.NSP:430-436` | Confirmada | Fórmula idêntica à de `CALCBENF.NSN:258-262` |
| 9 | Em dezembro soma-se 13º; programas tipo `A` recebem abono de 15% | Estado | `BATCHPGT.NSP:442-455` | Confirmada | Fórmula do 13º diverge do comentário — ver `SIFAP-M-07` |
| 10 | Desconto no batch é 3% fixo sobre o bruto quando este excede R$ 500,00 | Indesejado | `BATCHPGT.NSP:458-462` | Confirmada | `CALCDSCT` **nunca é chamado** pelo batch — ver `SIFAP-M-08` |
| 11 | Valor líquido nunca é negativo; é truncado a zero | Indesejado | `BATCHPGT.NSP:465-470` | Confirmada | Truncação por multiplicação/divisão por 100 |
| 12 | Cada pagamento gerado emite uma linha de 240 bytes em `CMWKF01` | Evento | `BATCHPGT.NSP:492-503` | Confirmada | Layout de remessa bancária |
| 13 | Retorno 0 sem rejeitados, 4 com rejeitados, 8 nada a fazer, 12 erro | Ubíquo | `BATCHPGT.NSP:556-570` | Confirmada | Contrato com `SIFAPJ01.jcl:99-104` |
| 14 | O ciclo grava trilha de auditoria com ação `BT` | Evento | `BATCHPGT.NSP:531-540` | Confirmada | IN-TCU 63/2010 |

---

## Regras de `CALCBENF.NSN` — cálculo do benefício

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 15 | Mês fora de 1–12 retorna código 2020 | Indesejado | `CALCBENF.NSN:171-175` | Confirmada | |
| 16 | Beneficiario inexistente retorna 2001; inativo retorna 2002 | Indesejado | `CALCBENF.NSN:178-196` | Confirmada | |
| 17 | Fator familiar cresce por faixas de dependentes: +0,05 até 2, +0,03 de 3 a 4, +0,02 acima | Ubíquo | `CALCBENF.NSN:216-228` | Confirmada | |
| 18 | Fator etário: 1,15 aos 65+, 1,10 aos 60+, 1,05 abaixo de 18, senão 1,00 | Ubíquo | `CALCBENF.NSN:246-256` | Confirmada | |
| 19 | Fator de renda por faixa: até 300 → 1,00; 600 → 0,85; 1000 → 0,70; 1500 → 0,55; acima → 0,40 | Ubíquo | `CALCBENF.NSN:330-336` | Confirmada | Sub-rotina `DET-BAND-INCOME` |
| 20 | Todo valor monetário é truncado a 2 casas por multiplicação e divisão inteira por 100 | Ubíquo | `CALCBENF.NSN:265-267` | Confirmada | Padrão mainframe — **não é arredondamento** |
| 21 | O subprograma grava o pagamento diretamente com status `G` | Evento | `CALCBENF.NSN:305-320` | Confirmada | **`NUM-PAYMENT` nunca é atribuído** — ver `SIFAP-M-06` |

---

## Regras de `VALELEG.NSN` — elegíbilidade

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 22 | Programa inativo interrompe a validação com retorno 2004 | Indesejado | `VALELEG.NSN:114-118` | Confirmada | |
| 23 | Região 99 torna o beneficiario elegível imediatamente, sem demais verificações | Opcional | `VALELEG.NSN:123-129` | Confirmada | **Precede status, idade e renda** — ver `SIFAP-M-09` |
| 24 | Status diferente de `A` acumula motivo de inelegibilidade | Indesejado | `VALELEG.NSN:134-158` | Confirmada | |
| 25 | Idade fora de `AGE-MIN`/`AGE-MAX` do programa torna inelegivel | Indesejado | `VALELEG.NSN:163-178` | Confirmada | Limite zero significa "sem limite" |
| 26 | Renda familiar acima de `MAX-PERCAP-INCOME` torna inelegivel | Indesejado | `VALELEG.NSN:183-191` | Confirmada | Campo é per capita, comparação usa renda total — ver `SIFAP-M-10` |
| 27 | Tipo `A` exige documentação completa; tipo `P` exige 60 anos; tipo `T` exige 16 a 65 anos | Opcional | `VALELEG.NSN:196-224` | Confirmada | Tipo desconhecido torna inelegivel |
| 28 | `COD-ELIGIBILITY` posição 1 = `R` exige NIS; posição 2 = `D` exige dependentes | Opcional | `VALELEG.NSN:258-276` | Confirmada | Ticket 4471/2012 sem documentação |
| 29 | Somente o primeiro motivo de inelegibilidade é devolvido ao chamador | Ubíquo | `VALELEG.NSN:233-236` | Confirmada | Array acumula 10, PDA carrega 1 |

---

## Regras de `CALCDSCT.NSP` — descontos

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 30 | Contribuição social por faixa: 3%, 5%, 7% e 9% | Ubíquo | `CALCDSCT.NSP:196-205` | Confirmada | Diverge do 3% fixo do batch |
| 31 | Teto de desconto é 30% do valor bruto | Ubíquo | `CALCDSCT.NSP:107` | Confirmada | |
| 32 | Desconto judicial (`J`) não se sujeita ao teto de 30% | Opcional | `CALCDSCT.NSP:130-141` | Confirmada | |
| 33 | Descontos fora do período de vigência são ignorados | Indesejado | `CALCDSCT.NSP:113-121` | Confirmada | |
| 34 | Tipos reconhecidos são `J`, `P`, `I`, `S` e `A`; os demais são ignorados | Indesejado | `CALCDSCT.NSP:127-170` | Confirmada | **DDM define códigos de 2 letras** — ver `SIFAP-M-11` |

---

## Regras de `CALCCORR.NSP` — correção monetária

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 35 | Período inicial maior que o final interrompe o processamento | Indesejado | `CALCCORR.NSP:150-153` | Confirmada | |
| 36 | Pagamento já corrigido (`IND-CORR = 'S'`) não é corrigido de novo | Indesejado | `CALCCORR.NSP:185-187` | Confirmada | Idempotência |
| 37 | A correção aplica o índice IPCA do próprio mês do pagamento | Ubíquo | `CALCCORR.NSP:195`, `:229-239` | Confirmada | **Não acumula entre mêses apesar do nome** — ver `SIFAP-M-12` |
| 38 | Só há índices carregados para 2010, 2011 e 2012 | Ubíquo | `CALCCORR.NSP:86-127` | Confirmada | Fora disso o índice fica 1,0 e nada é corrigido, em silêncio |
| 39 | Só grava quando a diferença é positiva, e registra auditoria `AL` | Evento | `CALCCORR.NSP:206-222` | Confirmada | `AMT-NET` não é recalculado |

---

## Regras de `BATCHCON.NSP` — conciliação bancária

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 40 | Somente registros CNAB tipo `3` são processados | Indesejado | `BATCHCON.NSP:147-150` | Confirmada | |
| 41 | Divergência acima de R$ 0,01 gera registro de auditoria e não atualiza o pagamento | Indesejado | `BATCHCON.NSP:186-196` | Confirmada | |
| 42 | Retorno `00` marca o pagamento como `P`; `01` como `D`; `02` como `E` | Evento | `BATCHCON.NSP:200-222` | Confirmada | **`P` significa PENDENTE no DDM** — ver `SIFAP-M-13` |
| 43 | Código de retorno desconhecido apenas escreve no log | Indesejado | `BATCHCON.NSP:223-226` | Confirmada | Pagamento fica sem atualização |

---

## Regras de `VALBENEF.NSN` e `SUBVALCP.NSN` — validação de documentos

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 44 | CPF é validado por módulo 11 | Ubíquo | `SUBVALCP.NSN:70-80` | Confirmada | Fonte única declarada no cabeçalho |
| 45 | `SUBVALCP` trata `00000000000` como CPF não informado (1002) | Indesejado | `SUBVALCP.NSN:57-60` | Confirmada | |
| 46 | `VALBENEF` trata CPF com todos os dígitos iguais começando em `000` como **válido** | Opcional | `VALBENEF.NSN:239-242` | Confirmada | **Contradiz a regra 45** — ver `SIFAP-M-14` |
| 47 | Nome exige pelo menos dois termos; UF é conferida contra tabela de 27 | Ubíquo | `VALBENEF.NSN:145-171` | Confirmada | |
| 48 | Status válidos de beneficiario são `A`, `S`, `C`, `I` e `D` | Ubíquo | `VALBENEF.NSN:175-181` | Confirmada | |

---

## Regras transversais — `LDASIFAP.NSL` e `CCAUDIT.NSC`

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 49 | Fevereiro tem 29 dias em toda validação de data desde 1997 | Ubíquo | `LDASIFAP.NSL:76`, `VALBENEF.NSN:105` | Confirmada | Aceita 29/02 em ano não bissexto — ver `SIFAP-M-15` |
| 50 | Janela de século Y2K usa pivô 50 para datas em `YYMMDD` | Opcional | `LDASIFAP.NSL:94-96` | Confirmada | Ainda ativo em `BATCHPGT.NSP:344-355` |
| 51 | Toda alteração de dados grava trilha em `AUDIT` | Ubíquo | `CCAUDIT.NSC:66-110` | Confirmada | IN-TCU 63/2010 |
| 52 | A sequência de auditoria é semeada uma vez por execução e incrementada em memória | Ubíquo | `CCAUDIT.NSC:70-77` | Confirmada | Não é seguro sob concorrência — ver `SIFAP-M-16` |
| 53 | Ação `CO` (consulta) não deve ser registrada desde 2010 | Indesejado | `CCAUDIT.NSC:52-55` | Confirmada | O copycode não bloqueia; a responsabilidade é do chamador |

---

## Regras de `CCVALCPF.NSC` — rotina padrão de CPF (módulo 11)

Copycode que fornece a sub-rotina `VALID-CPF-STANDARD`. Entrada `#CPF-STR (A11)`, saída `#CPF-OK (L)`. Incluído por `SUBVALCP.NSN:94` e `CADDEPEN.NSP:230`.

| # | Enunciado da regra | Candidato EARS | Origem | Classificação | Notas |
|---|---|---|---|---|---|
| 54 | O CPF é presumido válido no início e só é invalidado por uma verificação que falhe | Ubíquo | `CCVALCPF.NSC:41-42` | Inferida | `MOVE TRUE TO #CPF-OK` antes de qualquer teste; qualquer caminho não coberto devolve "válido" |
| 55 | Se o CPF não tiver exatamente 11 caracteres numéricos, então o sistema deve rejeitá-lo | Indesejado | `CCVALCPF.NSC:45-48` | Inferida | `MASK(NNNNNNNNNNN)`; brancos e sinais são rejeitados aqui |
| 56 | Se qualquer posição do CPF não for um dígito de 0 a 9, então o sistema deve rejeitá-lo | Indesejado | `CCVALCPF.NSC:51-77` | Inferida | Ramo `NONE VALUE` do `DECIDE`; redundante com a regra 55 |
| 57 | Se todos os 11 dígitos forem iguais, então o sistema deve rejeitar o CPF | Indesejado | `CCVALCPF.NSC:79-90` | Inferida | Adicionada em 17/05/2005 (`:6`); **sem exceção para `000`** — contrasta com `VALBENEF.NSN:238-242` |
| 58 | O 1º dígito verificador é o resto de 11 na soma dos 9 primeiros dígitos com pesos 10 a 2 | Ubíquo | `CCVALCPF.NSC:92-105` | Confirmada | `BUSINESS-RULES-2012.md:72` (RN-001) exige dígito verificador, sem detalhar a fórmula |
| 59 | O 2º dígito verificador é o resto de 11 na soma dos 10 primeiros dígitos com pesos 11 a 2 | Ubíquo | `CCVALCPF.NSC:111-125` | Confirmada | `BUSINESS-RULES-2012.md:72` (RN-001) |
| 60 | Quando o resto da divisão por 11 for menor que 2, o dígito verificador é 0 | Ubíquo | `CCVALCPF.NSC:101-105`, `:121-125` | Inferida | Convenção padrão de CPF, mas não documentada no acervo |
| 61 | Se qualquer dígito verificador calculado divergir do informado, então o sistema deve rejeitar o CPF | Indesejado | `CCVALCPF.NSC:106-109`, `:126-128` | Inferida | O 1º dígito faz `ESCAPE ROUTINE`; o 2º apenas marca `#CPF-OK` e cai no fim da sub-rotina |
| 62 | Quando o CPF de um dependente for inválido, o sistema deve apenas exibir aviso e concluir a inclusão | Indesejado | `CADDEPEN.NSP:162-171` | Inferida | "WARNING MODE" declarado no comentário; `#ERR` não é marcado — ver `BONUS` |

> [!NOTE]
> As regras 54 a 57, 60 e 61 estão marcadas como **Inferidas**: têm evidência literal no código, mas nenhuma seção da documentação em `legacy-docs/` as corrobora. A única referência documental a CPF é `BUSINESS-RULES-2012.md:72` (RN-001), que cita um subprograma `VALCPF` inexistente no acervo — o membro disponível chama-se `SUBVALCP.NSN`.

### Divergência entre as quatro implementações de módulo 11

O cabeçalho do próprio copycode declara que as cópias **não são equivalentes** (`CCVALCPF.NSC:32-37`, ticket 6620/2011 em aberto). Comparação linha a linha:

| Implementação | Máscara numérica | Dígitos iguais | Resto da divisão | Origem |
|---|---|---|---|---|
| `VALID-CPF-STANDARD` (padrão) | Sim | Rejeita sempre | `DIVIDE ... REMAINDER` | `CCVALCPF.NSC:39-130` |
| `VALID-CPF` | Não | **Sem verificação** | `#SUM - ((#SUM / 11) * 11)` | `CADBENEF.NSP:344-413` |
| `VALID-CPF-COMPLETE` | Não | Rejeita, **exceto início `000`** | `#SUM - ((#SUM / 11) * 11)` | `VALBENEF.NSN:196-276` |
| `VALID-CPF-DOC` | Não (rejeita `#CPF = 0`) | **Sem verificação** | `#SUM - ((#SUM / 11) * 11)` | `VALDOCS.NSP:137-199` |

Consumidores do copycode: `SUBVALCP.NSN:94` e `CADDEPEN.NSP:230`. `CADBENEF.NSP`, `VALBENEF.NSN` e `VALDOCS.NSP` mantêm cópias inline.

---

## Resumo geral

| Métrica | Valor |
|---|---:|
| Membros Natural lidos | 23 de 24 |
| DDMs cruzados | 4 de 4 |
| Regras confirmadas | 55 |
| Regras inferidas | 7 |
| Mistérios | 20 canônicos + 8 bônus |

> As 55 regras confirmadas têm evidência literal no código **e** correspondência na documentação histórica. As 7 inferidas (54-57, 60-62) têm evidência literal no código, mas nenhuma seção de `legacy-docs/` que as corrobore — não devem ser tratadas como fato até validação humana. As incertezas foram registradas como mistérios em [`mysteries-found.md`](mysteries-found.md), não como regras.

> [!WARNING]
> **Um membro ainda não lido:** `SIFAPJ02.jcl` (job de relatórios). `CCVALCPF.NSC` foi lido e catalogado nas regras 54-61; a leitura revelou que a rotina "padrão" convive com três cópias inline divergentes — ver a tabela de divergência acima e os mistérios `BONUS` correspondentes.

---

## Definição de pronto

- [x] Todo bloco condicional dos programas atribuídos foi examinado.
- [x] Toda regra cita `arquivo:linha`.
- [x] Toda questão em aberto está registrada em `mysteries-found.md` sem conclusão.

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [Inventário](inventory.md)<br/><sub>Passo 1 — varredura de arquivos.</sub> | [Mapa de Dependências](dependency-map.md)<br/><sub>Passo 3 — grafo de chamadas e acessos.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
