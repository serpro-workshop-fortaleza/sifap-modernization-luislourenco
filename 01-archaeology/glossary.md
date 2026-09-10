# Glossário do SIFAP Legado

> **Trilha:** [Kit do Time](../README.md) › [Estágio 1](README.md) › **Glossário**

**Artefato preenchido pelo time durante o Estágio 1.** Uma tabela com todos os termos, abreviações e siglas encontrados no código Natural/Adabas — a base da linguagem ubíqua para o Estágio 2.

| Campo | Valor |
|---|---|
| **Público-alvo** | Todas as duplas — cada dupla contribui com os termos dos seus programas |
| **Pré-requisitos** | Abrir os arquivos `.NSN` e `.ddm` atribuídos |
| **Estágio** | Estágio 1 — Arqueologia |
| **Resultado esperado** | 30 termos ou mais, com programa de origem e status CONFIRMADO/HIPÓTESE |

> [!NOTE]
> Guia passo a passo: [`GUIDE.md`](GUIDE.md).

---

## Por que o glossário importa

Sistemas legados têm vocabulário próprio, raramente documentado em um lugar acessível — ele vive em nomes de variável, abreviações de campo e comentários de código. Se o time do Estágio 2 não souber o que significam `DSCT`, `BENF`, `PE` ou `CTC`, vai escrever uma especificação baseada em suposições sobre esses termos.

O glossário transforma abreviações de 3 a 6 caracteres em uma linguagem ubíqua compartilhada pelo time inteiro — e dá a base para os nomes de entidades e atributos do modelo de domínio no Estágio 3.

**Erro comum:** marcar um termo como CONFIRMADO sem evidência literal no código ou na documentação histórica. Se você inferiu o significado pelo contexto, marque como HIPÓTESE e identifique quem é responsável pela validação.

---

## Como preencher

| Coluna | O que registrar |
|---|---|
| **Termo** | A abreviação ou sigla exatamente como aparece no código. |
| **Expansão** | O significado completo do termo. |
| **Programa** | O arquivo `.NSN` ou `.ddm` onde o termo foi encontrado. |
| **Contexto** | Explicação breve de como e onde o termo é usado. |
| **Status** | `CONFIRMADO` — evidência literal no código ou na documentação. `HIPÓTESE` — inferido do contexto e aguardando validação. |

### Dica de extração com o modo Ask do GitHub Copilot

Antes de usar o prompt abaixo, cole no chat o conteúdo de 2 a 3 arquivos `.NSN`:

> "Liste todas as abreviações e siglas usadas neste código Natural. Para cada uma, sugira a expansão e marque como 'CONFIRMADO' ou 'HIPÓTESE'."

Compare a sugestão do Copilot com o que você observou diretamente no código. Se coincidirem, registre como CONFIRMADO; caso contrário, registre como HIPÓTESE.

---

## Termos encontrados

### Domínio — beneficiário e cadastro

| # | Termo | Expansão | Programa | Contexto | Status |
|---|---|---|---|---|---|
| 1 | `BENEF` | Beneficiário | `BENEFIC.ddm` | Nome do DDM do arquivo 150; prefixo em `CADBENEF`, `VALBENEF`, `CONSBENF` | CONFIRMADO |
| 2 | `CAD` | Cadastro | `CADBENEF.NSP:1` | Prefixo dos programas de manutenção de entidades | CONFIRMADO |
| 3 | `CONS` | Consulta | `CONSBENF.NSP:1` | Prefixo do único programa de leitura pura | CONFIRMADO |
| 4 | `DEPEN` | Dependente | `CADDEPEN.NSP`, `BENEFIC.ddm:96` | Grupo periódico `GRP-DEPEND (1:10)` | CONFIRMADO |
| 5 | `NIS` | Número de Identificação Social | `BENEFIC.ddm:53` | Campo `AM NUM-NIS`, validado por módulo 11 com pesos 3-2-9-8-7-6-5-4-3-2 | CONFIRMADO |
| 6 | `PIS/PASEP` | Programa de Integração Social / Programa de Formação do Patrimônio do Servidor Público | `SUBVALNI.NSN:16` | Sinônimo de NIS no código | CONFIRMADO |
| 7 | `CPF` | Cadastro de Pessoas Físicas | `BENEFIC.ddm:41` | Campo `AB NUM-CPF`, alfanumérico A11 sem máscara | CONFIRMADO |
| 8 | `RG` | Registro Geral (identidade) | `BENEFIC.ddm:49-52` | Quatro campos: número, órgão, UF e data de emissão | CONFIRMADO |
| 9 | `UF` | Unidade Federativa | `LDASIFAP.NSL:65` | Tabela de 27 siglas válidas | CONFIRMADO |
| 10 | `CEP` | Código de Endereçamento Postal | `BENEFIC.ddm:72` | `BH CEP`, N8 sem hífen | CONFIRMADO |
| 11 | `SISOBI` | Sistema de Óbitos | `BENEFIC.ddm:11` | Origem do cruzamento que popula `IA IND-DEATH` desde 2001 | CONFIRMADO |
| 12 | `IBGE` | Instituto Brasileiro de Geografia e Estatística | `BENEFIC.ddm:74` | `BI COD-IBGE`, código de município | CONFIRMADO |

### Domínio — cálculo e pagamento

| # | Termo | Expansão | Programa | Contexto | Status |
|---|---|---|---|---|---|
| 13 | `PGT` / `PGTO` | Pagamento | `BATCHPGT.NSP`, `RELPGT.NSP` | Sufixo de programa e valor de `TYPE-ENTITY` na auditoria | CONFIRMADO |
| 14 | `CALC` | Cálculo | `CALCBENF.NSN:1` | Prefixo da família de cálculo | CONFIRMADO |
| 15 | `DSCT` | Desconto | `CALCDSCT.NSP:1` | Descontos e deduções sobre o valor bruto | CONFIRMADO |
| 16 | `CORR` | Correção monetária | `CALCCORR.NSP:9` | Recalculo retroativo por variação de índice | CONFIRMADO |
| 17 | `AMT-GROSS` | Valor bruto | `PAYMENT.ddm:38` | Resultado de base × 4 fatores × ajuste do programa | CONFIRMADO |
| 18 | `AMT-NET` | Valor líquido | `PAYMENT.ddm:39` | Bruto menos descontos, nunca negativo | CONFIRMADO |
| 19 | `FACTOR-K` | Fator de correção especial | `SOCPROG.ddm:44-48` | Marcado `>>> UNDOCUMENTED <<<` no próprio DDM; inserido em 2008 "a pedido da SENARC" | HIPÓTESE |
| 20 | `FACTOR-ADJUST` | Fator de ajuste sobre o valor base | `SOCPROG.ddm:50` | Aplicado em `CALCBENF.NSN:262` como `(1 + fator)` | CONFIRMADO |
| 21 | `13º` / `TYPE-PAYMENT = 'D'` | Décimo terceiro | `CALCBENF.NSN:275-278` | Calculado apenas em dezembro | CONFIRMADO |
| 22 | `IPCA` | Índice Nacional de Preços ao Consumidor Amplo | `CALCCORR.NSP:83` | Índice de correção; tabela carregada só para 2010–2012 | CONFIRMADO |
| 23 | `IRRF` (`IR`) | Imposto de Renda Retido na Fonte | `SOCPROG.ddm:79` | Tipo de desconto válido | CONFIRMADO |
| 24 | `JD` | Desconto judicial | `SOCPROG.ddm:79`, `CALCDSCT.NSP:130` | Único tipo isento do teto de 30% | CONFIRMADO |
| 25 | `CS` | Consignado | `SOCPROG.ddm:80` | Tipo declarado no DDM que `CALCDSCT` não trata — ver `SIFAP-M-11` | CONFIRMADO |
| 26 | `PA` | Pensão alimentícia | `SOCPROG.ddm:80` | Tipo de desconto | CONFIRMADO |

### Domínio — programa social e elegíbilidade

| # | Termo | Expansão | Programa | Contexto | Status |
|---|---|---|---|---|---|
| 27 | `PROG` | Programa social | `SOCPROG.ddm`, `CADPROG.NSP` | Arquivo 151, tabela de parâmetros com ~45 programas | CONFIRMADO |
| 28 | `ELEG` | Elegíbilidade | `VALELEG.NSN:1` | Confere cadastro contra as regras do programa | CONFIRMADO |
| 29 | `PBF` | Programa Bolsa Família | `SOCPROG.ddm:31` | Exemplo de sigla em `AC ACRONYM-PROGRAM` | HIPÓTESE |
| 30 | `BPC` | Benefício de Prestação Continuada | `SOCPROG.ddm:31` | Exemplo de sigla no DDM | HIPÓTESE |
| 31 | `PETI` | Programa de Erradicação do Trabalho Infantil | `SOCPROG.ddm:31` | Exemplo de sigla no DDM | HIPÓTESE |
| 32 | `SENARC` | Secretaria Nacional de Renda de Cidadania | `SOCPROG.ddm:14` | Área que autoriza mudanças no `FACTOR-K` | CONFIRMADO |
| 33 | `MDS` / `MDAS` | Ministério do Desenvolvimento Social (e Agrário) | `SOCPROG.ddm:9`, `BENEFIC.ddm:23` | Órgão responsável | CONFIRMADO |
| 34 | `CGTI` | Coordenação-Geral de Tecnologia da Informação | `BENEFIC.ddm:23` | Comitê técnico que autoriza mudança de layout (Portaria 847/2003) | CONFIRMADO |

### Técnico — Natural, Adabas e mainframe

| # | Termo | Expansão | Programa | Contexto | Status |
|---|---|---|---|---|---|
| 35 | `DDM` | Data Definition Module | `BENEFIC.ddm:30` | Visão Natural de um arquivo Adabas | CONFIRMADO |
| 36 | `FDT` | Field Definition Table | `BENEFIC.ddm:195` | Definição física dos campos no Adabas | CONFIRMADO |
| 37 | `FNR` | File Number | `BENEFIC.ddm:32` | 150=BENEFIC, 151=SOCPROG, 152=PAYMENT, 153=AUDIT | CONFIRMADO |
| 38 | `ISN` | Internal Sequence Number | `PAYMENT.ddm:171` | Identificador interno; reuso desligado em PAYMENT | CONFIRMADO |
| 39 | `MU` | Multiple Value field | `BENEFIC.ddm:180` | Campo multivalorado, ex.: `ED NUM-PHONE (1:5)` | CONFIRMADO |
| 40 | `PE` | Periodic Group | `BENEFIC.ddm:181` | Grupo periódico, ex.: `DA GRP-DEPEND (1:10)` | CONFIRMADO |
| 41 | `LDA` | Local Data Area | `LDASIFAP.NSL:2` | Área de dados compartilhada, prefixo `#L-` | CONFIRMADO |
| 42 | `PDA` | Parameter Data Area | `PDACALC.NSA:2` | Contrato de parâmetros entre chamador e subprograma | CONFIRMADO |
| 43 | `CALLNAT` | Chamada de subprograma Natural | `BATCHPGT.NSP:381` | Mecanismo de invocação entre módulos | CONFIRMADO |
| 44 | `CMWKF01` / `CMWKF02` | Work files Natural | `SIFAPJ01.jcl:53-66` | Extrato de remessa (240 bytes) e log de rejeitados (120 bytes) | CONFIRMADO |
| 45 | `CMSYNIN` | Entrada de comandos Natural | `SIFAPJ01.jcl:70-78` | Fornece o período `YYYYMM` ao `BATCHPGT` | CONFIRMADO |
| 46 | `CNAB 240` | Layout bancário da Febraban | `BATCHCON.NSP:11` | Arquivo de retorno com 240 bytes por registro | CONFIRMADO |
| 47 | `SIAFI` | Sistema Integrado de Administração Financeira | `PAYMENT.ddm:79-86` | Integração de ordem bancária e nota de empenho | CONFIRMADO |
| 48 | `FEBRABAN` | Federação Brasileira de Bancos | `BENEFIC.ddm:141` | Origem do `COD-BANK` de 3 dígitos | CONFIRMADO |
| 49 | `Y2K` / janela de século | Remediação do bug do ano 2000 | `LDASIFAP.NSL:92-96` | Pivô 50: `YY < 50` → 20xx, senão 19xx | CONFIRMADO |
| 50 | `IN-TCU 63/2010` | Instrução Normativa do Tribunal de Contas da União | `AUDIT.ddm:15` | Base legal da trilha de auditoria imutável | CONFIRMADO |

> **50 termos registrados.** Os marcados como HIPÓTESE aparecem como exemplo em comentário de DDM, sem uso literal no código executado — precisam de validação humana antes de virarem linguagem ubíqua.

---

## Definição de pronto

- [x] 30 termos ou mais registrados.
- [x] Todo termo tem um programa de origem.
- [x] Todo termo tem status CONFIRMADO ou HIPÓTESE.
- [x] As hipóteses estão marcadas para validação com um facilitador.

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [GUIDE do Estágio 1](GUIDE.md)<br/><sub>Cronograma passo a passo.</sub> | [Relatório de Descoberta](discovery-report.md)<br/><sub>Consolidação final do estágio.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
