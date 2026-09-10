# ADR-003: Representação monetária e regra de truncamento

> **Trilha:** [Kit do Time](../../README.md) › [Estágio 2](../README.md) › **ADR-003**

| Campo | Valor |
|---|---|
| **Status** | `Aceita` |
| **Data** | 2026-09-10 |
| **Feature relacionada** | [`specs/001-monthly-payroll-generation/`](../../specs/001-monthly-payroll-generation/spec.md) |

---

## Contexto

O SIFAP armazena valores em campos Adabas `P` (decimal empacotado): `AMT-GROSS (P9.2)`, `AMT-NET (P9.2)`, `AMT-DISC (P7.2)` — ver `PAYMENT.ddm:37-42`. Os fatores usam `N3.4`, com quatro casas decimais.

O truncamento não é implícito. Aparece explicitamente após **cada** operação monetária, sempre no mesmo idioma:

```natural
COMPUTE #AMT-TEMP = #AMT-BENF * 100     /* #AMT-TEMP e N11, inteiro */
COMPUTE #AMT-BENF = #AMT-TEMP / 100
```

Evidência em `CALCBENF.NSN:265-267`, `:279-281`, `:290-292`, `:302-304` e replicada em `BATCHPGT.NSP:436-438`.

O efeito é **truncamento, não arredondamento**: `#AMT-TEMP` é `N11`, inteiro, e a atribuição descarta a parte fracionária. Um bruto de `123,459` vira `123,45`.

O ponto crítico: o truncamento acontece em **cada etapa intermediária**, não apenas no resultado final. Bruto é truncado, depois o 13º é truncado, depois o abono é truncado, depois o desconto, depois o líquido. A ordem e a quantidade de truncamentos fazem parte da regra — em 4,2 milhões de beneficiários, um centavo por etapa é dinheiro real.

---

## Opções consideradas

### Opção 1: `double` ou `float`

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Aritmética rápida; sintaxe direta |
| **Desvantagens** | Ponto flutuante binário não representa `0,01` exatamente; proibido em cálculo financeiro; impossível reproduzir o comportamento de `P9.2` de forma determinística |

### Opção 2: `BigDecimal` com `RoundingMode.HALF_UP` no resultado final

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Precisão decimal exata; é a convenção mais comum em sistemas financeiros modernos |
| **Desvantagens** | **Muda o resultado.** `HALF_UP` arredonda para cima o que o legado descarta, e aplicar só no final elimina os truncamentos intermediários. Duas divergências somadas, ambas a favor do beneficiário — o que soa inofensivo até ser multiplicado por 4,2 milhões e comparado com o legado numa auditoria do TCU |

### Opção 3: `BigDecimal` com `RoundingMode.DOWN` aplicado nos mesmos pontos do legado

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Reproduz o comportamento observado ao centavo; `DOWN` é exatamente o descarte da parte fracionária; a comparação de saída com o legado pode exigir igualdade absoluta em vez de tolerância |
| **Desvantagens** | Perpetua um arredondamento desfavorável ao beneficiário; exige disciplina para truncar nos pontos certos, e cada ponto precisa de teste |

---

## Decisão

Adotamos a **Opção 3**, encapsulada em um objeto de valor `Money` no shared kernel.

- Representação interna: `BigDecimal` com escala 2.
- Operação de truncamento: `setScale(2, RoundingMode.DOWN)`, exposta como `Money.truncate()`.
- Fatores permanecem `BigDecimal` com escala 4, sem truncamento intermediário — o legado também não trunca fatores (`N3.4` em `CALCBENF.NSN:88-90`).
- `Money` é truncado **nos mesmos cinco pontos** do legado: bruto, 13º, abono, desconto e líquido.
- A ordem das operações no código Java segue a ordem de `CALCBENF.NSN:258-304`, e essa ordem é documentada como parte da regra, não como detalhe de implementação.

Escolhemos preservar o comportamento em vez de corrigi-lo porque **preservação é verificável e correção não é**. Se a folha moderna produzir valores diferentes da legada, não conseguiremos distinguir um erro de migração de uma melhoria intencional. Corrigir o arredondamento é uma decisão de política pública, com impacto orçamentário, que pertence à Coordenação de Benefícios — não à equipe de modernização.

Registramos a questão como candidata a mudança **posterior** à virada, quando a equivalência já estiver comprovada.

---

## Consequências

### Positivas

- A comparação com a folha legada pode exigir igualdade exata ao centavo, o que torna o teste de equivalência um sinal confiável.
- `Money` centraliza a regra: nenhum cálculo espalhado pode truncar de forma diferente.
- Fatores em escala 4 evitam perda de precisão antes da multiplicação final, exatamente como no legado.

### Negativas

- O sistema moderno nasce reproduzindo um arredondamento sistematicamente desfavorável ao beneficiário.
- Cada ponto de truncamento vira um teste; esquecer um produz divergência de centavos difícil de rastrear.
- `Money.truncate()` precisa ser chamado explicitamente; a API não impede que alguém esqueça. Um teste de arquitetura verificando que operações aritméticas de `Money` não escapam do agregado reduz o risco, mas não o elimina.

---

## Requisitos relacionados

- `REQ-016` — fórmula do valor bruto
- `REQ-020` — truncamento em duas casas sem arredondamento
- `REQ-027` — valor líquido não negativo

---

<sub>[Voltar ao índice do kit](../../README.md)</sub>
