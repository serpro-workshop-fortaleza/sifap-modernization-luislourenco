# ADR-005: Validação canônica de CPF no shared kernel

> **Trilha:** [Kit do Time](../../README.md) › [Estágio 2](../README.md) › **ADR-005**

| Campo | Valor |
|---|---|
| **Status** | `Proposta` — bloqueada por `SIFAP-M-14` |
| **Data** | 2026-09-10 |
| **Feature relacionada** | [`specs/001-monthly-payroll-generation/`](../../specs/001-monthly-payroll-generation/spec.md) |

---

## Contexto

O acervo legado contém **quatro** implementações independentes de validação de CPF por módulo 11. A leitura completa de `CCVALCPF.NSC` no Estágio 1 mostrou que elas não são variações cosméticas: discordam sobre quais documentos são válidos.

| Implementação | Máscara numérica | Dígitos iguais | Resto da divisão | Origem |
|---|---|---|---|---|
| `VALID-CPF-STANDARD` | Sim | Rejeita sempre | `DIVIDE ... REMAINDER` | `CCVALCPF.NSC:39-130` |
| `VALID-CPF` | Não | **Sem verificação** | `#SUM - ((#SUM / 11) * 11)` | `CADBENEF.NSP:344-413` |
| `VALID-CPF-COMPLETE` | Não | Rejeita, **exceto início `000`** | `#SUM - ((#SUM / 11) * 11)` | `VALBENEF.NSN:196-276` |
| `VALID-CPF-DOC` | Não (rejeita `#CPF = 0`) | **Sem verificação** | `#SUM - ((#SUM / 11) * 11)` | `VALDOCS.NSP:137-199` |

Apenas `SUBVALCP.NSN:94` e `CADDEPEN.NSP:230` incluem o copycode. Os outros três programas mantêm cópias inline.

Três fatos tornam esta decisão inadiável:

1. **A divergência é conhecida e documentada pelo próprio legado.** O cabeçalho do copycode declara: *"INLINE COPIES OF THIS ROUTINE EXIST IN OTHER SIFAP MODULES WITH DIFFERENT BEHAVIOR (...) THEY ARE NOT EQUIVALENT"* (`CCVALCPF.NSC:32-37`). O ticket 6620/2011 que consolidaria as cópias está aberto há 15 anos.

2. **A base tem registros que só passam em alguns validadores.** `11111111111` é aceito no cadastro de beneficiário (`CADBENEF.NSP:344-413`, sem verificação de dígitos iguais) e rejeitado por `SUBVALCP`. A escolha da regra canônica define quantos dos 4,2 milhões de registros deixam de ser válidos na migração.

3. **A divergência pode não se limitar aos dígitos iguais.** A rotina padrão passou a usar `DIVIDE ... REMAINDER` em 07/06/2011 (`CCVALCPF.NSC:7`), enquanto as três cópias mantêm `#SUM - ((#SUM / 11) * 11)`. Se a divisão intermediária do Natural arredondar em vez de truncar, o resto — e portanto o dígito verificador calculado — difere. O caso que motivou a correção de 2011 não está no acervo.

A documentação histórica não resolve: `BUSINESS-RULES-2012.md:72` (RN-001) exige "CPF válido (validação por dígito verificador, subprograma VALCPF)" e cita um subprograma que **não existe no acervo** — o membro disponível chama-se `SUBVALCP.NSN`. Nenhuma seção menciona dígitos iguais ou a exceção `000`.

---

## Opções consideradas

### Opção 1: replicar as quatro variantes, uma por contexto chamador

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Preserva o comportamento observado ponto a ponto; nenhum registro muda de status na migração |
| **Desvantagens** | Perpetua na arquitetura de 2026 um defeito que o legado documentou como defeito em 2011. Destrói o shared kernel: `Cpf.of(String)` passaria a exigir o contexto chamador como parâmetro, o que significa que "CPF válido" deixa de ser uma propriedade do documento. Quatro implementações exigem quatro suítes de teste e garantem que a quinta divergência apareça |

### Opção 2: adotar a regra oficial da Receita Federal

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | Regra externa, estável, verificável fora do sistema; elimina a discussão sobre qual variante legada tem razão |
| **Desvantagens** | **Não é preservação de comportamento, é substituição.** Não sabemos quantos registros da base passam nas rotinas legadas e falham na regra oficial. Sem essa medição, a migração rejeitaria beneficiários por um critério que o SIFAP nunca aplicou — exatamente o tipo de mudança silenciosa que o Estágio 1 existe para impedir |

### Opção 3: adotar `VALID-CPF-STANDARD` como canônica no shared kernel, após medir o impacto

| Aspecto | Avaliação |
|---|---|
| **Vantagens** | É a única variante que o próprio legado declara padrão corporativo (NT-SUPDE-014, `CCVALCPF.NSC:9`); é a mais recente (correção de 2011) e a mais restritiva, o que torna o conjunto de aceitos um subconjunto do das demais — a divergência é mensurável por contagem, não por análise caso a caso; concentra a regra em um único objeto de valor `Cpf` |
| **Desvantagens** | Rejeita registros hoje aceitos por `CADBENEF` e `VALDOCS`; exige uma medição na base de produção antes da virada; a exceção `000` de `VALBENEF` pode ter uso operacional legítimo que ninguém documentou |

---

## Decisão

Adotamos a **Opção 3**: `VALID-CPF-STANDARD` (`CCVALCPF.NSC:39-130`) é a regra canônica, implementada uma única vez no objeto de valor `Cpf` do shared kernel.

- `Cpf.of(String)` rejeita: entrada com tamanho diferente de 11, caractere não numérico, todos os dígitos iguais (**sem** exceção para `000`) e dígito verificador divergente.
- O resto da divisão é calculado por operação inteira explícita, reproduzindo `DIVIDE ... REMAINDER`, não a expressão das cópias inline.
- Nenhum outro módulo pode implementar validação de CPF. Um teste de arquitetura (ArchUnit) proíbe que classes fora do shared kernel referenciem `Cpf` de forma a recalcular dígitos verificadores.

**Esta decisão permanece com status `Proposta` e não libera REQ-007 para implementação.** Duas condições precisam ser satisfeitas antes de promovê-la a `Aceita`:

1. **Medição obrigatória.** Contar, na base de produção, quantos registros de `BENEFIC` (FNR 150) são aceitos pelas cópias inline e rejeitados pela rotina padrão. O número é o custo real da decisão, e decidir sem ele é adivinhar.
2. **Validação humana da Coordenação de Benefícios** sobre a exceção `000` de `VALBENEF.NSN:238-242` — se existe caso de uso legítimo, ela vira uma regra explícita com REQ próprio, não uma exceção escondida em uma sub-rotina.

Registramos deliberadamente a incerteza em vez de escondê-la atrás de uma escolha técnica. A decisão de qual CPF é válido determina quem recebe benefício; ela pertence à Coordenação de Benefícios, e o papel da arquitetura é apresentar as opções com o impacto quantificado.

---

## Consequências

### Positivas

- Uma regra, um lugar, um conjunto de testes. `Cpf` é um objeto de valor: se existe uma instância, o documento é válido — invariante impossível de violar por esquecimento.
- A migração de dados ganha um critério único e uma contagem de exceções auditável, em vez de quatro comportamentos implícitos.
- A regra mais restritiva como canônica torna toda divergência detectável na direção segura: nenhum registro passa a ser aceito por engano.

### Negativas

- Registros hoje válidos em `CADBENEF` e `VALDOCS` passam a ser inválidos. Sem a medição da condição 1, o volume é desconhecido.
- Se a divergência do cálculo do resto (2011) for real, alguns CPFs com dígito verificador considerado correto pelas cópias inline serão rejeitados — e o inverso. Exige um teste de equivalência sobre amostra real, não sobre casos sintéticos.
- A migração precisa de uma estratégia para os registros rejeitados: bloqueá-los, marcá-los para regularização ou migrá-los com sinalização. Essa decisão não pertence a este ADR e deve ser tomada no recorte 002 (cadastro).

---

## Requisitos relacionados

- `REQ-006` — validação de CPF por módulo 11 antes do cálculo
- `REQ-007` — CPF com todos os dígitos iguais é inválido (**bloqueado** por `SIFAP-M-14`)
- `REQ-030` — beneficiário com CPF inválido vai para o registro de rejeitados
- `REQ-031` — CPF nunca aparece sem máscara em log ou relatório

---

## Questões em aberto que este ADR não resolve

| Questão | Evidência | Responsável |
|---|---|---|
| Quantos registros de produção mudam de status com a regra canônica? | `CADBENEF.NSP:344-413` contra `CCVALCPF.NSC:79-90` | DBA Adabas |
| A exceção `000` tem uso operacional legítimo? | `VALBENEF.NSN:238-242` | Coordenação de Benefícios |
| O cálculo do resto de 2011 corrige um defeito real das cópias? | `CCVALCPF.NSC:7`, `:99-100` contra `CADBENEF.NSP:384` | Coordenação de Benefícios / DBA |
| Dependente com CPF inválido continua entrando no cadastro? | `CADDEPEN.NSP:162-171` — "warning mode" | Coordenação de Benefícios (recorte 002) |

---

<sub>[Voltar ao índice do kit](../../README.md)</sub>
