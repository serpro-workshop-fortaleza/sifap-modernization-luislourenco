# Inventário do Legado — Time `<preencher>`

> **Trilha:** [Kit do Time](../README.md) › [Estágio 1](README.md) › **Inventário**

**Primeiro artefato do Estágio 1.** Varra a estrutura e conte os arquivos sem abrir nenhum programa — use apenas os nomes de arquivo e a estrutura de pastas.

| Campo | Valor |
|---|---|
| **Público-alvo** | Dupla responsável pela varredura inicial |
| **Pré-requisitos** | Acesso ao diretório `legacy-sifap/` |
| **Estágio** | Estágio 1 — Arqueologia, Passo 1 |
| **Resultado esperado** | Contagens corretas, padrões de nomenclatura identificados e 3 itens estranhos sinalizados |

> [!NOTE]
> Monte este inventário sem abrir nenhum programa. Trabalhe apenas com nomes de arquivo e estrutura de pastas. Ele será revisado à medida que o time extrai regras, mapeia dependências e registra mistérios.

**Data:** 2026-09-10
**Dupla responsável:** Luis Lourenço
**Caminho varrido:** `01-archaeology/legacy-sifap/`

> [!IMPORTANT]
> Esta é a **primeira passada**, feita apenas com nomes de arquivo e estrutura de pastas. Nenhum programa foi aberto. Toda hipótese abaixo é provisória e deve ser revisada conforme a dupla extrai regras (`/extract-business-rules`) e rastreia dependências (`/map-dependencies`).

---

## Estrutura de pastas

**4 diretórios** (1 raiz + 3 subdiretórios), **40 arquivos** no total.

```text
01-archaeology/legacy-sifap/
├── HOW-TO-READ-NATURAL.md
├── README.md
├── adabas-ddms/          6 arquivos
│   ├── AUDIT.ddm
│   ├── BENEFIC.ddm
│   ├── PAYMENT.ddm
│   ├── SOCPROG.ddm
│   ├── FDT-150-BENEFICIARY.txt
│   └── README.md
├── legacy-docs/          7 arquivos
│   ├── ORIGINAL-ARCHITECTURE-1997.md / .docx
│   ├── TECHNICAL-MANUAL-SIFAP-2008.md / .docx
│   ├── BUSINESS-RULES-2012.md / .docx
│   └── README.md
└── natural-programs/     25 arquivos
    ├── BATCHCON.NSP   BATCHPGT.NSP   BATCHREL.NSP
    ├── CADBENEF.NSP   CADDEPEN.NSP   CADPROG.NSP
    ├── CALCCORR.NSP   CALCDSCT.NSP   CONSBENF.NSP
    ├── RELAUDIT.NSP   RELPGT.NSP     VALDOCS.NSP
    ├── CALCBENF.NSN   VALBENEF.NSN   VALELEG.NSN
    ├── SUBVALCP.NSN   SUBVALNI.NSN
    ├── CCAUDIT.NSC    CCVALCPF.NSC
    ├── PDACALC.NSA    PDAVALID.NSA
    ├── LDASIFAP.NSL
    ├── SIFAPJ01.jcl   SIFAPJ02.jcl
    └── README.md
```

**Verificação independente:**

```bash
find 01-archaeology/legacy-sifap -type d | wc -l    # 4
find 01-archaeology/legacy-sifap -type f | wc -l    # 40
```

---

## Contagem de arquivos por tipo

| Extensão | Contagem | Finalidade provável (conhecimento geral de Natural/Adabas) |
|---|---|---|
| `.NSP` | 12 | Programa Natural (unidade executável, ponto de entrada online ou batch) |
| `.NSN` | 5 | Subprograma Natural (invocado por `CALLNAT`, unidade de compilação separada) |
| `.ddm` | 4 | Data Definition Module — visão Natural de um arquivo Adabas |
| `.docx` | 3 | Documentação histórica em formato binário |
| `.NSC` | 2 | Copycode — fragmento de fonte incluído por `INCLUDE` |
| `.NSA` | 2 | Parameter Data Area (PDA) — contrato de parâmetros entre chamador e subprograma |
| `.jcl` | 2 | Job Control Language — orquestração de execução batch no mainframe |
| `.NSL` | 1 | Local Data Area (LDA) — estrutura de dados local compartilhada |
| `.txt` | 1 | Listagem FDT (Field Definition Table) do Adabas |
| `.md` | 8 | Documentação do kit + conversões da documentação histórica |
| **Total** | **40** | |

> Não há arquivos `.NSM` (mapas de tela) nesta base. Se o sistema tinha telas 3270, suas definições **não** foram preservadas — registrar como pergunta em aberto.

**Verificação independente:**

```bash
find 01-archaeology/legacy-sifap -type f | sed 's/.*\.//' | sort | uniq -c | sort -rn
```

---

## Padrões da convenção de nomes

Agrupamento por prefixo, considerando apenas padrões com **2 ou mais** arquivos.

| Prefixo | Contagem | Arquivos | Hipótese de domínio |
|---|---|---|---|
| `BATCH` | 3 | BATCHCON, BATCHPGT, BATCHREL `.NSP` | Pontos de entrada de processamento batch; os 3 últimos caracteres parecem sufixos de função (`CON`, `PGT`, `REL`) — **hipótese não confirmada** |
| `CAD` | 3 | CADBENEF, CADDEPEN, CADPROG `.NSP` | Cadastro/manutenção de entidades (`BENEF`, `DEPEN`, `PROG`) — provável CRUD online |
| `CALC` | 3 | CALCCORR, CALCDSCT `.NSP`; CALCBENF `.NSN` | Rotinas de cálculo. Note que a família mistura `.NSP` e `.NSN` — investigar por que um cálculo é subprograma e dois são programas |
| `VAL` | 3 | VALDOCS `.NSP`; VALBENEF, VALELEG `.NSN` | Rotinas de validação; mesma mistura `.NSP`/`.NSN` da família `CALC` |
| `REL` | 2 | RELAUDIT, RELPGT `.NSP` | Geração de relatórios (`REL`) sobre auditoria e pagamento |
| `SUBVAL` | 2 | SUBVALCP, SUBVALNI `.NSN` | Subvalidações de granularidade menor, provavelmente chamadas pelas rotinas `VAL*` |
| `CC` | 2 | CCAUDIT, CCVALCPF `.NSC` | Prefixo alinhado à extensão `.NSC` — convenção de nomenclatura para **copycode** |
| `PDA` | 2 | PDACALC, PDAVALID `.NSA` | Prefixo alinhado à extensão `.NSA` — convenção para **Parameter Data Area** |
| `SIFAPJ` | 2 | SIFAPJ01, SIFAPJ02 `.jcl` | Jobs JCL numerados sequencialmente; nomeados a partir do sistema, não da função |

**Padrões de ocorrência única** (registrados para completude):

| Arquivo | Hipótese |
|---|---|
| `CONSBENF.NSP` | Prefixo `CONS` = consulta; único programa de leitura sem par |
| `LDASIFAP.NSL` | Prefixo `LDA` alinhado à extensão `.NSL` — Local Data Area única do sistema |

**Observação estrutural relevante:** os prefixos `CC`, `PDA` e `LDA` correspondem exatamente às extensões `.NSC`, `.NSA` e `.NSL`. Isso sustenta a hipótese de que **o prefixo codifica o tipo de membro Natural**, enquanto o sufixo codifica o domínio. As famílias `CALC` e `VAL`, porém, quebram essa regra ao misturar `.NSP` e `.NSN` — **anomalia a investigar**.

---

## Itens estranhos (top 3)

> Não foi possível ordenar por tamanho de arquivo sem abrir os arquivos. Os itens abaixo foram sinalizados por **unicidade de extensão, unicidade de padrão de nome e assimetria estrutural**. A dupla deve complementar com `ls -laS` antes da leitura.

| # | Caminho do arquivo | O que o torna estranho | Investigação sugerida |
|---|---|---|---|
| 1 | `legacy-docs/*.docx` (3 arquivos) | Única família binária do acervo, e cada `.docx` tem um irmão `.md` de mesmo nome. Duplicação sem indicação de qual é a fonte de verdade | Confirmar se os `.md` são conversões fiéis. Divergência entre pares vira pergunta em aberto em `mysteries-found.md`. Datas nos nomes (1997, 2008, 2012) sugerem três ondas de documentação — verificar o que mudou entre elas |
| 2 | `adabas-ddms/FDT-150-BENEFICIARY.txt` | Única extensão `.txt` e único nome contendo dígitos (`150`, provável FNR). Existem **4 DDMs mas apenas 1 FDT** — o esquema físico de 3 arquivos Adabas não foi preservado | Confirmar se `150` é o número do arquivo Adabas de `BENEFIC.ddm`. Registrar a ausência dos FDTs de PAYMENT, SOCPROG e AUDIT como lacuna do acervo |
| 3 | `natural-programs/LDASIFAP.NSL` | Única extensão `.NSL` e único membro nomeado a partir do **sistema inteiro**, não de uma entidade. Provável estrutura de dados compartilhada por vários programas — nó de alto acoplamento | Ler cedo: se for uma LDA global, ela define o vocabulário de dados de toda a base e é a melhor porta de entrada para o glossário |

**Menções honrosas:** `SIFAPJ01.jcl` / `SIFAPJ02.jcl` são os únicos arquivos não-Natural dentro de `natural-programs/` — mistura de camadas (orquestração + código) na mesma pasta.

---

## Ordem de leitura proposta

> **Isto é hipótese.** A ordem foi derivada apenas de nomes e estrutura e **vai mudar** quando a dupla rastrear as arestas reais de `CALLNAT` e `INCLUDE` com `/map-dependencies`.

| # | Bloco | Arquivos | Justificativa |
|---|---|---|---|
| 1 | Modelo de dados | `adabas-ddms/*.ddm` + `FDT-150-BENEFICIARY.txt` | Entender os dados antes da lógica. Apenas 4 DDMs delimitam todo o universo de persistência do sistema |
| 2 | Contratos de dados compartilhados | `LDASIFAP.NSL`, `PDACALC.NSA`, `PDAVALID.NSA` | LDA e PDAs definem o vocabulário que os programas trocam entre si; alimentam diretamente o glossário |
| 3 | Pontos de entrada batch | `SIFAPJ01.jcl`, `SIFAPJ02.jcl` → `BATCHCON`, `BATCHPGT`, `BATCHREL` `.NSP` | O JCL nomeia quais programas realmente iniciam a execução. Ler o orquestrador antes do orquestrado |
| 4 | Núcleo de regras | `CALCBENF`, `VALBENEF`, `VALELEG`, `SUBVALCP`, `SUBVALNI` `.NSN` | Subprogramas são os candidatos mais conectados (alvos de `CALLNAT`); é onde as regras de negócio tendem a se concentrar |
| 5 | Cálculo e validação de nível superior | `CALCCORR`, `CALCDSCT`, `VALDOCS` `.NSP` | Provável camada que compõe os subprogramas do bloco 4 |
| 6 | Copycodes | `CCAUDIT`, `CCVALCPF` `.NSC` | Ler sob demanda, quando um `INCLUDE` aparecer nos blocos anteriores |
| 7 | Online e relatórios | `CAD*`, `CONSBENF`, `REL*` `.NSP` | Camada de apresentação e saída; depende de tudo acima |

**Critério aplicado:** (a) dados antes de código; (b) orquestradores antes de orquestrados; (c) `.NSN`/`.NSC`/`.NSA` são alvos de chamada e, portanto, os nós mais conectados por definição de tipo de membro.

---

## Perguntas em aberto levantadas por esta varredura

Candidatas a registro em [`mysteries-found.md`](mysteries-found.md) — nenhuma resolvida por este inventário.

1. Por que as famílias `CALC` e `VAL` misturam `.NSP` e `.NSN`, se os demais prefixos correspondem a um único tipo de membro?
2. Onde estão os mapas de tela (`.NSM`)? O sistema tinha interface 3270 sem definições preservadas?
3. Por que existe 1 FDT para 4 DDMs? Os outros três arquivos Adabas foram descartados ou nunca documentados?
4. Os `.md` em `legacy-docs/` são conversões fiéis dos `.docx`, ou divergem?
5. O que significam os sufixos `CON`, `PGT`, `NI` e `CP`? Precisam ser decodificados a partir do código.

---

## Definição de pronto

- [x] O inventário existe com contagens corretas (40 arquivos, verificáveis com `find`).
- [x] 3 padrões de nomenclatura ou mais identificados (9 padrões com 2+ arquivos).
- [x] 3 itens estranhos sinalizados com caminho e motivo.
- [x] Ordem de leitura justificada por padrão de nomenclatura e posição estrutural.
- [x] Nome da dupla preenchido no cabeçalho.

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [GUIDE do Estágio 1](GUIDE.md)<br/><sub>Cronograma passo a passo.</sub> | [Catálogo de Regras](business-rules-catalog.md)<br/><sub>Passo 2 — extração de regras.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
