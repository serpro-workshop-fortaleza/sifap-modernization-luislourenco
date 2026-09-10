# ADR-001: Mapeamento de estruturas Adabas para PostgreSQL

> **Trilha:** [Kit do Time](../../README.md) › [Estágio 2](../README.md) › **ADR-001**

| Campo | Valor |
|---|---|
| **Status** | `Aceita` |
| **Data** | 2026-09-10 |
| **Feature relacionada** | [`specs/001-monthly-payroll-generation/`](../../specs/001-monthly-payroll-generation/spec.md) |

---

## Contexto

Os 4 DDMs do SIFAP usam três construções Adabas que não têm equivalente direto em SQL relacional:

| Construção | Ocorrências | Exemplo |
|---|---|---|
| **PE** (grupo periódico) | 4 | `GRP-DEPEND (1:10)` em `BENEFIC.ddm:96`; `GRP-DISC (1:8)` em `PAYMENT.ddm:46` |
| **MU** (campo multivalorado) | 4 | `ED NUM-PHONE (1:5)` em `BENEFIC.ddm:114`; `HC COD-OCCURRENCE (1:10)` em `PAYMENT.ddm:126` |
| **Superdescritor** | 10 | `SUPER-CPF-PERIOD` em `PAYMENT.ddm:159`, usado por `BATCHPGT.NSP:299` |

O volume impede tratar a decisão como detalhe: `PAYMENT` tem 612 milhões de registros e `AUDIT` tem 417 milhões (`PAYMENT.ddm:170`, `AUDIT.ddm:139`). Uma escolha errada de mapeamento multiplica linhas ou inviabiliza consultas.

Há ainda uma restrição herdada: `PAYMENT.ddm:181-182` alerta que o ISN não é chave estável e não deve ser usado como identificador externo.

---

## Opções consideradas

### Opção 1: Achatar PE e MU em colunas numeradas (`dependente_1_cpf`, `dependente_2_cpf`, …)

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Tradução mecânica; uma linha no legado continua uma linha no Postgres; migração trivial de verificar |
| **Desvantagens** | 10 dependentes × 6 campos = 60 colunas em `beneficiary`; consultar "quem tem dependente com deficiência" exige varrer 10 colunas; o limite de 10 vira limite físico do schema, quando `CADDEPEN.NSP:118` já usa 5 e o DDM permite 10 (`SIFAP-M-04`) |

### Opção 2: Tabelas filhas normalizadas para PE, coluna JSONB para MU

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | `dependent` e `payment_discount` viram tabelas com chave estrangeira e índice próprio; o limite de ocorrências passa a ser regra de domínio, não do schema; MU de baixa cardinalidade (telefones, ocorrências bancárias) não justifica uma tabela |
| **Desvantagens** | `payment_discount` herda o volume de `PAYMENT`: até 8 descontos × 612 M pagamentos; exige particionamento desde o primeiro dia; JSONB não tem integridade referencial |

### Opção 3: JSONB para PE e MU

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Absorve a variabilidade histórica sem migração de schema; uma linha por registro legado |
| **Desvantagens** | Descontos são dados financeiros que precisam de soma, agregação e auditoria; `CALCDSCT.NSP:110-177` percorre e totaliza os itens; enterrar isso em JSONB impede constraints de valor e torna o relatório de descontos um `jsonb_array_elements` sobre 612 milhões de linhas |

---

## Decisão

Adotamos a **Opção 2**, com a seguinte regra:

- **PE com significado de negócio → tabela filha.** `GRP-DEPEND` vira `dependent`; `GRP-DISC` vira `payment_discount`; `GRP-CALC-BAND` e `GRP-REGIONAL-PARAM` viram `program_calculation_band` e `program_regional_parameter`.
- **MU sem significado de negócio → `jsonb`.** `NUM-PHONE` e `COD-OCCURRENCE` são listas acessórias que nenhum programa do acervo consulta por conteúdo.
- **Superdescritor → índice composto.** `SUPER-CPF-PERIOD` vira `UNIQUE INDEX (cpf, reference_period)`, o que dá suporte direto ao REQ-002.
- **Chave primária → identificador próprio.** Nenhuma coluna deriva de ISN, conforme `PAYMENT.ddm:181-182`.
- **`payment` e `payment_discount` particionadas por `reference_period`.** O volume de 612 M e a ausência de política de expurgo (`PAYMENT.ddm:176-178`) tornam o particionamento obrigatório, não otimização.

---

## Consequências

### Positivas

- Descontos ganham integridade referencial e podem ser somados por SQL, o que sustenta REQ-022 a REQ-027.
- O limite de dependentes passa a ser regra de domínio testável, expondo a divergência de `SIFAP-M-04` em vez de escondê-la no schema.
- O índice único sobre `(cpf, reference_period)` traduz o superdescritor sem inventar estrutura nova.
- O particionamento por período espelha o particionamento que o legado já faz por FNR em `AUDIT` (`AUDIT.ddm:155-158`).

### Negativas

- Duas tabelas grandes em vez de uma; a geração da folha passa a fazer inserção em lote em `payment` e `payment_discount` dentro da mesma transação.
- JSONB para telefones significa que uma futura busca por telefone exigirá índice GIN ou migração para tabela.
- A migração de dados precisa explodir os grupos periódicos, e um registro legado com ocorrências além do limite declarado revelará inconsistências históricas. Isso é desejável, mas vai gerar volume de exceções na carga.

---

## Requisitos relacionados

- `REQ-002` — índice único sobre CPF e período
- `REQ-022` a `REQ-027` — descontos como tabela filha
- `REQ-028` — um pagamento por beneficiário e período

---

<sub>[Voltar ao índice do kit](../../README.md)</sub>
