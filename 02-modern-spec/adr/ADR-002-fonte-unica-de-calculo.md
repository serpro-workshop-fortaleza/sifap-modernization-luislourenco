# ADR-002: Fonte única de cálculo do benefício

> **Trilha:** [Kit do Time](../../README.md) › [Estágio 2](../README.md) › **ADR-002**

| Campo | Valor |
|---|---|
| **Status** | `Aceita` |
| **Data** | 2026-09-10 |
| **Feature relacionada** | [`specs/001-monthly-payroll-generation/`](../../specs/001-monthly-payroll-generation/spec.md) |

---

## Contexto

O cálculo do benefício existe **duas vezes** no legado, em versões idênticas linha a linha:

| Local | Evidência |
|---|---|
| Subprograma chamado por `CALLNAT` | `CALCBENF.NSN:258-262` |
| Cópia inline dentro do batch | `BATCHPGT.NSP:430-436` |

`BATCHPGT.NSP:381` chama `CALCBENF` e, logo depois, executa a própria cópia. Ambos terminam em `STORE PAYMENT-V` (`CALCBENF.NSN:319` e `BATCHPGT.NSP:488`). O comentário em `BATCHPGT.NSP:363-367` explica: a cadeia corporativa foi introduzida pelo ticket 6621/2011 e a remoção do inline ficou pendente do ticket 6622/2011, **aberto até hoje**.

A duplicação não para aí. As tabelas de fator regional e de faixas de renda aparecem três vezes: em `LDASIFAP.NSL:36-56`, em `CALCBENF.NSN:100-146` e em `BATCHPGT.NSP:191-237`. `LDASIFAP.NSL:26-29` reconhece o problema por escrito: *"cópias inline em outros módulos não foram removidas [...] ticket 4472/2004 — aberto"*.

Duas consequências não resolvidas: `SIFAP-M-05` (dupla gravação de pagamento) e `SIFAP-M-06` (`CALCBENF` grava sem atribuir a chave única `NUM-PAYMENT`).

---

## Opções consideradas

### Opção 1: Reproduzir a duplicação para garantir equivalência comportamental

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Se a folha de produção realmente gera dois registros, o sistema moderno gera os mesmos dois; nenhuma diferença aparece na comparação de saída |
| **Desvantagens** | Perpetua um defeito financeiro em um sistema que paga benefícios sociais; exige decidir qual das duas gravações é a "verdadeira" sem ter essa informação; a segunda gravação depende de o Adabas aceitar `NUM-PAYMENT = 0` repetido, comportamento que o acervo não permite determinar |

### Opção 2: Cálculo único em módulo de domínio puro, sem persistência

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Uma fórmula, um lugar, testável sem banco; elimina por construção a dupla gravação; o módulo `benefitcalculation` fica sem dependência de infraestrutura e pode ser verificado com casos de referência extraídos do legado |
| **Desvantagens** | Se a produção hoje gera dois pagamentos, a folha moderna terá metade dos registros e a comparação vai acusar diferença massiva; exige confirmar o comportamento real antes da virada |

### Opção 3: Cálculo único, mantendo a persistência dentro do serviço de cálculo

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Mais próximo do desenho legado de `CALCBENF`; uma chamada resolve cálculo e gravação |
| **Desvantagens** | Repete o erro estrutural: o módulo de cálculo escreveria no agregado do contexto `payment`, violando a fronteira definida em [`bounded-contexts.md`](../bounded-contexts.md); torna o cálculo impossível de simular sem efeito colateral |

---

## Decisão

Adotamos a **Opção 2**.

`benefitcalculation` é um módulo de **domínio puro**: recebe `BeneficiarySnapshot` e `ProgramRules`, devolve um objeto `BenefitCalculation` com bruto, descontos, líquido e tipo de pagamento. Não acessa banco, não persiste e não conhece `payment`.

A persistência pertence exclusivamente ao contexto `payment`, que orquestra: consulta cadastro → consulta programa → calcula → persiste um pagamento (REQ-028).

As tabelas de parâmetro (fator regional, faixas de renda, faixas de contribuição) deixam de ser constantes de código e passam a ser dados versionados por vigência, resolvendo a triplicação de `LDASIFAP`/`CALCBENF`/`BATCHPGT`.

**Condição de aceite antes da virada:** executar `BATCHPGT` no ambiente de referência para um período e contar os registros gravados em `PAYMENT` para um mesmo CPF. Se o resultado for dois, `SIFAP-M-05` deixa de ser mistério e vira incidente de produção — o que muda o escopo desta modernização.

---

## Consequências

### Positivas

- Uma única fórmula, coberta por testes de caso de referência derivados de `CALCBENF.NSN`.
- O módulo de cálculo é verificável sem banco, sem contêiner e sem dados de produção.
- A dupla gravação e o `NUM-PAYMENT` não atribuído desaparecem por construção, não por correção pontual.
- Parâmetros com vigência permitem recalcular um período passado com os valores daquele período, algo que o legado não consegue fazer.

### Negativas

- A comparação de saída com o legado só será conclusiva depois de resolver `SIFAP-M-05`; até lá, qualquer diferença de contagem é ambígua.
- Mover as tabelas de parâmetro para dados exige uma carga inicial fiel; um erro nessa carga afeta todos os cálculos de forma silenciosa.
- O contexto `payment` fica mais pesado, concentrando orquestração e persistência.

---

## Requisitos relacionados

- `REQ-016` a `REQ-021` — fórmula e fatores
- `REQ-028` — exatamente um pagamento por beneficiário e período

---

<sub>[Voltar ao índice do kit](../../README.md)</sub>
