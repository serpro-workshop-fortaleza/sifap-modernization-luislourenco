# Decisões de escopo — Estágio 2

> **Trilha:** [Kit do Time](../README.md) › [Estágio 2](README.md) › **Decisões de escopo**

**Registre as decisões de escopo tomadas durante o Estágio 2: o que foi selecionado, o que foi adiado e quais questões permanecem em aberto.**

| Campo | Valor |
|---|---|
| **Público-alvo** | Dupla 2 durante o Estágio 2; Duplas 3 e 4 durante o handoff H2 |
| **Finalidade** | Apoiar a conversa do estágio; não substitui os artefatos formais do Spec-Kit |
| **Feature relacionada** | [`specs/001-monthly-payroll-generation/`](../specs/001-monthly-payroll-generation/spec.md) |

> [!NOTE]
> Os entregáveis formais permanecem em `specs/<NNN>-<feature>/spec.md`, `plan.md` e `tasks.md`. Não registre requisitos EARS completos aqui. Este arquivo registra somente decisões de escopo e questões em aberto.

---

## Decisões de escopo

| Decisão | Evidência ou justificativa | Impacto nos artefatos formais |
|---|---|---|
| Recorte vertical na **geração mensal da folha**, não no cadastro | `BATCHPGT` é o único fluxo com fronteira de integração já existente (arquivo `CMWKF01`, `SIFAPJ01.jcl:53-58`), o que viabiliza execução em sombra | `spec.md` cobre 31 requisitos; cadastro fica para o recorte 002 |
| **Cinco** bounded contexts, com validação de documentos como shared kernel | Validação de CPF/NIS não possui dado próprio (`SUBVALCP.NSN:8`) e é chamada por 4 contextos | [`bounded-contexts.md`](bounded-contexts.md) — 4 hipóteses rejeitadas e documentadas |
| Conciliação bancária **dentro** do contexto `payment` | `BATCHCON.NSP:200-222` escreve em `PAYMENT`; duas fronteiras não podem escrever no mesmo agregado | Hipótese 3 rejeitada em `bounded-contexts.md` |
| Preservar o **truncamento** legado em vez de arredondar | Preservação é verificável; correção é decisão de política pública | [ADR-003](adr/ADR-003-representacao-monetaria.md); REQ-020 |
| Adotar `VALID-CPF-STANDARD` como **regra canônica** de CPF, com medição de impacto obrigatória antes da virada | É a única variante declarada padrão corporativo (`CCVALCPF.NSC:9`) e a mais restritiva das quatro; o próprio cabeçalho registra que as cópias não são equivalentes (`:32-37`) | [ADR-005](adr/ADR-005-validacao-canonica-de-cpf.md) em status `Proposta`; REQ-006 e REQ-007 |
| Adotar a **contribuição progressiva** de `CALCDSCT`, não os 3% fixos do batch | `CALCDSCT` é a regra completa; é órfão por acidente de conversão (`SIFAP-M-08`) | REQ-022, com divergência deliberada declarada |
| **Adiar** 13º e abono de férias | Fórmula do código diverge da documentação (`SIFAP-M-07`) | Fora do escopo de `spec.md` |
| Manter **3 requisitos bloqueados** visíveis na spec | Tornar a lacuna explícita vale mais do que uma spec aparentemente completa | REQ-007, REQ-015 e REQ-021 com `blocked_by:` |
| Estratégia de virada por **execução em sombra**, mínimo 3 ciclos | Nenhum beneficiário afetado antes da equivalência comprovada | [ADR-004](adr/ADR-004-coexistencia-strangler-fig.md) |

---

## Questões em aberto

| Questão | Fonte consultada | Próxima pessoa responsável |
|---|---|---|
| A folha de produção gera **um ou dois** pagamentos por beneficiário? | `SIFAP-M-05`; `BATCHPGT.NSP:381` e `:488` | Coordenação de Benefícios — bloqueia a validação do [ADR-002](adr/ADR-002-fonte-unica-de-calculo.md) |
| O fator regional deve ser por **região** ou por **UF**? | `SIFAP-M-20`; `LDASIFAP.NSL:36-46` contra `BENEFIC.ddm:77` | Coordenação de Benefícios — bloqueia REQ-021 |
| Qual validador de CPF prevalece entre os **quatro** que discordam? | `SIFAP-M-14`; `CCVALCPF.NSC:79-90`, `VALBENEF.NSN:238-242`, `CADBENEF.NSP:344-413`, `VALDOCS.NSP:137-199` | Coordenação de Benefícios — bloqueia REQ-007 e o [ADR-005](adr/ADR-005-validacao-canonica-de-cpf.md) |
| Quantos registros de `BENEFIC` mudam de status com a regra canônica de CPF? | [ADR-005](adr/ADR-005-validacao-canonica-de-cpf.md), condição 1 | DBA Adabas — medição obrigatória antes de promover o ADR a `Aceita` |
| A região 99 deve mesmo ignorar status, idade e renda? | `SIFAP-M-09`; `VALELEG.NSN:123-129` | Coordenação de Benefícios — bloqueia REQ-015 |
| Status `P` significa PAGO ou PENDENTE? | `SIFAP-M-13` e `SIFAP-M-19` | DBA Adabas — bloqueia o recorte de conciliação |
| Qual caminho de acesso o Java usará para ler o Adabas na fase de sombra? | [ADR-004](adr/ADR-004-coexistencia-strangler-fig.md), consequências negativas | Engenheiro DevOps + DBA |
| Existe FDT para os arquivos 151, 152 e 153? | Apenas `FDT-150-BENEFICIARY.txt` no acervo | DBA Adabas |

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [Guia do Estágio 2](GUIDE.md)<br/><sub>Especificação moderna passo a passo.</sub> | [Template de ADR](ADR-TEMPLATE.md)<br/><sub>Registre a decisão de escopo como uma ADR.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
