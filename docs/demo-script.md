# Roteiro da demonstração

> **Trilha:** [Kit do Time](../README.md) › [Documentação](README.md) › **Roteiro da demonstração**

**Estrutura para a apresentação final da imersão** — o time define o conteúdo com base no que produziu.

| Campo | Valor |
|---|---|
| **Público-alvo** | O time inteiro, especialmente o Product Owner |
| **Quando usar** | 16:50–17:30 |
| **Tempo estimado** | 10 min de preparação + até 30 min de apresentação |
| **Resultado esperado** | Demonstração apresentada sem improviso e sustentada por evidências verificáveis |

---

## Caminho de 3 minutos — recorte 001 (geração mensal da folha)

Roteiro concreto do time, na ordem de apresentação. Cada passo tem um artefato verificável; nada aqui depende de narrativa.

### 0:00–0:30 · O legado já sabia do problema

Abra [`CCVALCPF.NSC`](../01-archaeology/legacy-sifap/natural-programs/CCVALCPF.NSC) nas linhas 32-37 e leia o comentário em voz alta:

> `INLINE COPIES OF THIS ROUTINE EXIST IN OTHER SIFAP MODULES WITH DIFFERENT BEHAVIOR (...) THEY ARE NOT EQUIVALENT` — ticket 6620/2011, aberto há 15 anos.

**Fala:** existem quatro validadores de CPF que discordam sobre quem é beneficiário válido. Não descobrimos isso por inferência; está escrito no código, e ninguém tinha lido.

### 0:30–1:15 · A cadeia de rastreabilidade

Mostre os três artefatos na sequência, sem detalhar cada um:

| Artefato | O que apontar |
|---|---|
| [`business-rules-catalog.md`](../01-archaeology/business-rules-catalog.md) | Tabela de divergência das quatro rotinas |
| [`spec.md`](../specs/001-monthly-payroll-generation/spec.md) | REQ-007 com `blocked_by: SIFAP-M-14` |
| [`ADR-005`](../02-modern-spec/adr/ADR-005-validacao-canonica-de-cpf.md) | Status `Proposta`, com duas condições de desbloqueio |

**Fala:** a linha do Natural chega até um requisito bloqueado e uma decisão que ainda não foi tomada. O sistema não finge que sabe.

### 1:15–2:00 · O código preserva a dúvida

Rode ao vivo:

```bash
cd backend && ./mvnw verify
```

Aponte o resultado: **128 testes, BUILD SUCCESS**. Abra [`CpfTest.java`](../backend/src/test/java/br/gov/sifap/shared/CpfTest.java) e mostre o `@DisplayName` com o REQ-ID, e o comentário do teste de REQ-007 dizendo exatamente o que muda se a Coordenação de Benefícios confirmar a exceção `000`.

**Fala:** cada teste cita o requisito que verifica. A regra bloqueada está implementada de forma reversível, com uma condição e um teste.

### 2:00–2:40 · Portões de qualidade

```bash
cd infra && checkov -d . --framework terraform --compact
```

Aponte: **`Failed: 0, Skipped: 6`** — e abra um `#checkov:skip` para mostrar que cada exceção tem motivo e número de issue.

**Fala:** não desligamos o portão para ficar verde. Cada exceção é auditável e tem trabalho pendente vinculado.

### 2:40–3:00 · O que continua aberto

| Aberto | Quem decide |
|---|---|
| REQ-007, REQ-015, REQ-021 bloqueados | Coordenação de Benefícios |
| Branch base do Copilot Agent (issue #11) | Admin do repositório |
| CodeQL: default setup versus avançado (issue #10) | Admin do repositório |

**Fala:** o valor do estágio não é o que ficou pronto, é saber com precisão o que falta e de quem depende.

### Plano alternativo

Se o build ao vivo falhar, use `sdk env` antes — o JDK padrão do container é o 25 e o projeto exige o 21. Se ainda assim falhar, mostre a execução verde do CI em Actions em vez de improvisar.

---

## Preparação (16:50–17:00)

- [ ] **Distribua os papéis** — decida quem apresenta e quem controla o tempo.
- [ ] **Selecione as evidências** — use somente artefatos produzidos e verificados pelo time.
- [ ] **Ensaie as transições** — combine quem fala em cada momento, sem sobreposição.
- [ ] **Prepare uma alternativa** — decida o que mostrar se um recurso ao vivo falhar.
- [ ] **Abra as abas do navegador** — Swagger, frontend e pull request integrado.

---

## Estrutura sugerida

| Seção | Conteúdo | Tempo sugerido |
|---|---|---|
| **1. Abertura** | Apresente o problema selecionado pelo time e o objetivo da demonstração | 30 s |
| **2. Descobertas e decisões** | Mostre os artefatos que registram evidências, hipóteses e decisões | 60 s |
| **3. Trabalho concluído** | Apresente parte do fluxo implementado e explique como ele se conecta aos artefatos | 60 s |
| **4. Validação** | Mostre as evidências disponíveis da validação realizada | 30 s |
| **5. Encerramento** | Diga o que o time aprendeu, o que permanece em aberto e qual é o próximo passo | 30 s |

---

## Plano alternativo

Se uma apresentação ao vivo não estiver disponível:

- [ ] Apresente o artefato ou a evidência que o time preparou com antecedência.
- [ ] Explique a limitação objetivamente.
- [ ] Não alegue uma funcionalidade que não possa ser demonstrada.

---

## Regra de ouro

> [!IMPORTANT]
> Apresente somente o que o time consegue sustentar com as próprias evidências. Não preencha lacunas com uma narrativa que não possa ser verificada.

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [Lições aprendidas](lessons-learned.md)<br/><sub>Erros comuns dos times.</sub> | [Painel diário](STATUS.md)<br/><sub>Acompanhe o progresso.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
