# Relatório de experiência com o GitHub Copilot Agent

> **Trilha:** [Kit do Time](../README.md) › [Estágio 4](README.md) › **Relatório de experiência**

**Preencha este relatório durante o Estágio 4. Registre o que realmente aconteceu. Resultados positivos e negativos têm o mesmo valor para o aprendizado do time.**

![Estágio 4](https://img.shields.io/badge/Est%C3%A1gio-4%20%C2%B7%20Evolu%C3%A7%C3%A3o-171717?style=flat-square) ![Tipo: relatório do time](https://img.shields.io/badge/Tipo-Relat%C3%B3rio%20do%20time-737373?style=flat-square) ![Preencha durante o estágio](https://img.shields.io/badge/Preencha-Durante%20o%20est%C3%A1gio-A3A3A3?style=flat-square)

| Campo | Valor |
|---|---|
| **Público-alvo** | Todo o time; a Dupla 5 coordena o preenchimento |
| **Pré-requisitos** | Estágio 4 em andamento ou concluído |
| **Estágio** | Estágio 4 — Evolução |
| **Resultado esperado** | Relato factual para melhorar futuras imersões |

> [!NOTE]
> Guia passo a passo: [`GUIDE.md`](GUIDE.md). Seja honesto. Feedback positivo forçado não ajuda ninguém.

---

| Campo | Valor |
|---|---|
| **Time** | Luis Lourenço |
| **Data** | 2026-09-10 |
| **Edição** | Imersão SERPRO Fortaleza |
| **Participantes** | `<!-- listar integrantes -->` |

> [!NOTE]
> As seções 1, 2 e 5 estão preenchidas com o que foi observado e verificado. As seções de julgamento (3, 4, 6, 7, 8, 9) ficam para o time preencher em conjunto — nota e opinião não se delegam.

---

## 1. Issues criadas

Nove issues no total: cinco escritas antes da rodada de operação (#1 a #5) e quatro derivadas da revisão de infraestrutura e da revisão de PR (#8 a #11).

| # | Título | Origem |
|---|---|---|
| [#1](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/issues/1) | Ligar o cadastro de descontos ao ciclo da folha | `SIFAP-M-08` |
| [#2](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/issues/2) | Particionar a tabela payment por reference_period | ADR-001 |
| [#3](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/issues/3) | Criar o comparador de remessa para a execução em sombra | ADR-004 |
| [#4](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/issues/4) | Adicionar portões de segurança ao CI | lacuna de controle |
| [#5](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/issues/5) | Decidir as 3 regras legadas bloqueadas | `SIFAP-M-09`, `M-14`, `M-20` |
| [#8](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/issues/8) | Rede privada para PostgreSQL e Key Vault | constatação do Checkov |
| [#9](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/issues/9) | Rotacionar segredos antes de definir expiração | constatação do Checkov |
| [#10](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/issues/10) | Reconciliar CodeQL: default setup bloqueia config avançada | falha real do workflow |
| [#11](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/issues/11) | Copilot Agent usa branch padrão errada | revisão das PRs #6 e #7 |

---

## 2. PRs gerados pelo Agent

Duas PRs abertas pelo `copilot-swe-agent`. **Nenhuma produziu código.**

### PR #6 (da Issue #4)

| Campo | Valor |
|---|---|
| **Link** | [#6](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/pull/6) — `[WIP] Add security gates to CI for CodeQL and dependency checks` |
| **Base** | `portugues-br` (branch padrão) |
| **Arquivos modificados** | 0 |
| **Testes criados** | Não |
| **Exigiu alterações manuais** | Não se aplica — não houve entrega |
| **Merge realizado** | Não — fechada |

### PR #7 (da Issue #1)

| Campo | Valor |
|---|---|
| **Link** | [#7](https://github.com/serpro-workshop-fortaleza/sifap-modernization-luislourenco/pull/7) — `Report blocked implementation: backend module missing from working tree` |
| **Base** | `portugues-br` (branch padrão) |
| **Arquivos modificados** | 0 |
| **Testes criados** | Não |
| **Exigiu alterações manuais** | Não se aplica — não houve entrega |
| **Merge realizado** | Não — fechada |

**Causa comum:** o Copilot Agent usa a branch padrão do repositório como base. Ela é `portugues-br`, onde `backend/` não existe — o código está só em `develop`. O bloco "Base branch: `develop`" escrito no corpo das issues **não altera** esse comportamento. Registrado na issue #11.

---

## 3. O que funcionou bem

> Liste o que o Agent fez bem. Exemplos: entendeu a arquitetura, criou testes adequados, seguiu as convenções de nomenclatura.

1. `<!-- preencher -->`
2. `<!-- preencher -->`
3. `<!-- preencher -->`

---

## 4. O que surpreendeu o time

> O que vocês não esperavam? Inclua surpresas positivas ou negativas.

1. `<!-- preencher -->`
2. `<!-- preencher -->`
3. `<!-- preencher -->`

---

## 5. O que falhou ou decepcionou

> Em que pontos o Agent errou, entendeu mal a tarefa ou produziu código inadequado?

1. **Nenhuma das duas PRs entregou código.** As duas nasceram com base em `portugues-br` e pararam por falta do módulo `backend/`. O problema é de configuração de delegação, não de capacidade do agente — issue #11.
2. **A instrução de branch no corpo da issue não teve efeito.** As issues diziam de forma explícita "não use a branch padrão". O agente não pode obedecer: a base vem da configuração do repositório.
3. **`<!-- preencher: houve mais alguma falha observada pelo time? -->`**

> [!NOTE]
> A salvaguarda escrita nas issues ("se `backend/pom.xml` não existir, pare e reporte em vez de criar o projeto do zero") **funcionou**. A PR #7 se chama literalmente `Report blocked implementation`. O agente parou e reportou em vez de alucinar um projeto inteiro — que era exatamente o comportamento desejado.

### Tipos de falha encontrados

- [ ] O código não compilou
- [ ] Os testes falharam
- [ ] Não seguiu a arquitetura do projeto
- [ ] Imports incorretos ou circulares
- [ ] Lógica de negócio incorreta
- [ ] Tratamento de erros ausente
- [ ] Credenciais ou dados sensíveis no código
- [x] Outro: nenhum código foi produzido — base de trabalho errada por configuração do repositório

---

## 6. Qualidade do PR (nota de 1 a 5)

| Critério | Nota (1–5) | Comentário |
|---|---|---|
| Correção do código | | |
| Alinhamento com a arquitetura | | |
| Qualidade dos testes | | |
| Documentação gerada | | |
| Clareza do código | | |
| **Média geral** | | |

Escala: 1 = Inadequado, 2 = Abaixo das expectativas, 3 = Aceitável, 4 = Bom, 5 = Excelente.

---

## 7. Você usaria o Agent novamente?

- [ ] Sim, para tudo. Ele economiza bastante tempo
- [ ] Sim, para tarefas simples e bem definidas
- [ ] Talvez, mas exige supervisão considerável
- [ ] Não, gasto mais tempo revisando do que implementando
- [ ] Ainda não tenho certeza

**Justificativa:** `<!-- explique a escolha -->`

---

## 8. Recomendações para outros times

> Se outro time fosse usar o Agent pela primeira vez, o que vocês diriam?

1. **Confira a branch padrão antes de atribuir qualquer issue ao agente.** Ele trabalha a partir dela, e nenhum texto no corpo da issue muda isso. Foi o que custou as duas PRs desta rodada.
2. **Escreva a salvaguarda de parada na issue.** A frase "se o arquivo X não existir, pare e reporte em vez de criar do zero" evitou que o agente inventasse um projeto inteiro. Custou uma linha.
3. `<!-- preencher -->`

---

## 9. Comparação: Agent vs. modo Ask do GitHub Copilot vs. implementação manual

| Aspecto | Modo Agent | Modo Ask do GitHub Copilot | Manual |
|---|---|---|---|
| Velocidade | | | |
| Qualidade | | | |
| Controle | | | |
| Aprendizado | | | |
| Quando usar | | | |

---

## 10. Comentários adicionais

> Espaço para qualquer observação adicional sobre o uso de IA generativa no desenvolvimento:

`<!-- preencher -->`

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [GUIDE do Estágio 4](GUIDE.md)<br/><sub>Instruções passo a passo do estágio.</sub> | [Template do relatório](templates/agent-experience-report.template.md)<br/><sub>Template para copiar.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
