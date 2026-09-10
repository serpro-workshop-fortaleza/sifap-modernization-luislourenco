# ADR-004: Coexistência com o legado por Strangler Fig

> **Trilha:** [Kit do Time](../../README.md) › [Estágio 2](../README.md) › **ADR-004**

| Campo | Valor |
|---|---|
| **Status** | `Aceita` |
| **Data** | 2026-09-10 |
| **Feature relacionada** | [`specs/001-monthly-payroll-generation/`](../../specs/001-monthly-payroll-generation/spec.md) |

---

## Contexto

O SIFAP paga benefícios sociais para 4,2 milhões de pessoas (`BENEFIC.ddm:198`). A folha roda no primeiro dia útil de cada mês, com janela de 4 horas (`SIFAPJ01.jcl:18-19`). Não existe cenário em que o pagamento atrase enquanto uma migração é depurada.

O acervo do Estágio 1 deixou 20 mistérios abertos, e **três deles bloqueiam requisitos desta própria feature** (REQ-007, REQ-015, REQ-021). Especificamente, `SIFAP-M-20` significa que não sabemos qual fator regional o sistema *deveria* aplicar — apenas qual ele aplica.

Ao mesmo tempo, o recorte selecionado é atraente para começar: `BATCHPGT` já se comunica com o mundo externo por **arquivo**, não por chamada. `SIFAPJ01.jcl:53-58` aloca `CMWKF01` como extrato de remessa de 240 bytes, e `STEP020` copia esse arquivo para a área de transmissão. A fronteira de integração já existe e é um arquivo posicional.

---

## Opções consideradas

### Opção 1: Virada única (*big bang*)

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Sem período de duplicidade; sem custo de manter dois sistemas; conclusão rápida no papel |
| **Desvantagens** | Um defeito de cálculo só aparece depois de o dinheiro ser creditado; reverter significa reprocessar 4,2 milhões de pagamentos dentro da janela mensal; os três requisitos bloqueados teriam de ser adivinhados |

### Opção 2: Execução paralela com comparação de saída (*shadow run*)

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | O sistema moderno gera o arquivo de remessa **sem transmiti-lo**, e a comparação com `CMWKF01` do legado é byte a byte sobre a mesma população; divergências aparecem antes de qualquer crédito; os mistérios abertos ganham evidência empírica em vez de especulação |
| **Desvantagens** | Exige rodar os dois sistemas por vários ciclos mensais; comparar 4,2 milhões de linhas exige ferramenta própria; a duplicidade dura meses |

### Opção 3: Roteamento por fatia de população

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Reduz o raio de impacto; permite começar por um programa social pequeno |
| **Desvantagens** | Um beneficiário pode ter pagamentos em sistemas diferentes ao longo do tempo, quebrando a continuidade do histórico em `PAYMENT`; a conciliação bancária e a trilha de auditoria ficariam divididas entre dois sistemas, o que colide com a exigência de trilha íntegra da IN-TCU 63/2010 |

---

## Decisão

Adotamos a **Opção 2** como mecanismo principal, com a **Opção 3** reservada para a virada final.

**Fase 1 — Sombra (mínimo 3 ciclos mensais).**
O SIFAP 2.0 lê a mesma base, gera a folha do mesmo período e produz um arquivo de remessa no formato de `CMWKF01`. O arquivo **não é transmitido**. Um comparador confronta os dois arquivos e classifica cada divergência como: defeito do moderno, defeito do legado, ou divergência deliberada já registrada na especificação.

As divergências deliberadas são conhecidas de antemão e esperadas — REQ-022 (contribuição progressiva contra 3% fixo) e REQ-024 (teto de desconto determinístico) vão produzir diferença. Elas servem de calibração do comparador: se **não** aparecerem, o comparador está errado.

**Fase 2 — Resolução dos bloqueios.**
A execução em sombra fornece a evidência que falta aos três requisitos bloqueados. `SIFAP-M-05` (dupla gravação) e `SIFAP-M-20` (fator regional) deixam de ser perguntas teóricas quando há duas saídas para comparar.

**Fase 3 — Virada por programa social.**
Concluída a equivalência, a transmissão passa a usar o arquivo do SIFAP 2.0, um programa social por vez, com o legado ainda executando em sombra invertida.

**O legado permanece a fonte de verdade dos dados durante todas as fases.** O SIFAP 2.0 lê o mesmo Adabas por uma camada anticorrupção; não há sincronização bidirecional, porque duas bases de escrita para 612 milhões de pagamentos é um problema maior do que o que estamos resolvendo.

---

## Consequências

### Positivas

- Nenhum beneficiário é afetado antes de a equivalência estar demonstrada em dados reais.
- A fronteira de integração é um arquivo posicional que já existe, e não uma interface nova a inventar.
- Os mistérios abertos deixam de bloquear o início do trabalho: viram hipóteses testáveis pela comparação.
- Reverter, em qualquer fase, significa não transmitir o arquivo novo. É uma decisão de um passo.

### Negativas

- Dois sistemas em operação por vários meses, com o custo operacional e cognitivo correspondente.
- O comparador é software de produção que precisa ser mantido e testado, e não entrega valor ao beneficiário.
- Ler o Adabas a partir do Java exige um caminho de acesso que ainda não está definido e pode virar o gargalo da fase 1.
- A camada anticorrupção herda temporariamente as inconsistências do legado — inclusive fevereiro com 29 dias (`SIFAP-M-15`), que precisa ser aceito na leitura e rejeitado na escrita.

---

## Requisitos relacionados

- `REQ-005` — desfechos distintos do ciclo, base para a comparação automatizada
- `REQ-029` — arquivo de remessa, artefato comparado na execução em sombra
- `REQ-007`, `REQ-015`, `REQ-021` — requisitos bloqueados que a fase 1 vai desbloquear

---

<sub>[Voltar ao índice do kit](../../README.md)</sub>
