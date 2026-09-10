# Mapa de Dependências — SIFAP Legado

> **Trilha:** [Kit do Time](../README.md) › [Estágio 1](README.md) › **Mapa de Dependências**

**Artefato preenchido pelo time durante o Estágio 1 — Passo 3.** Registra as dependências entre programas Natural e DDMs Adabas que sustentam o escopo selecionado.

| Campo | Valor |
|---|---|
| **Público-alvo** | Todas as duplas, com liderança da Dupla 2 (Arquitetura) |
| **Pré-requisitos** | Catálogo de regras com as origens identificadas |
| **Estágio** | Estágio 1 — Arqueologia |
| **Resultado esperado** | Diagrama Mermaid e tabelas de arestas com evidência `arquivo:linha` |

> [!IMPORTANT]
> Mapeie apenas as dependências que explicam o escopo selecionado: programas `.NSN` que chamam outros programas (`CALLNAT`, `FETCH`) e programas que acessam DDMs (`READ`, `FIND`, `STORE`, `UPDATE`, `DELETE`). Toda aresta precisa estar apoiada em `arquivo:linha` — nenhuma inferência sem evidência. Este mapa alimenta as hipóteses de fatiamento em [`discovery-report.md`](discovery-report.md).

> [!NOTE]
> Guia passo a passo: [`GUIDE.md`](GUIDE.md).

**Time**: <!-- preencher -->
**Escopo**: cobertura completa — 24 membros Natural e 4 DDMs

---

## Diagrama Mermaid

```mermaid
%%{init: {'theme':'neutral','themeVariables':{'fontFamily':'ui-sans-serif, system-ui, sans-serif','primaryColor':'#F5F5F5','primaryTextColor':'#171717','primaryBorderColor':'#171717','lineColor':'#525252','secondaryColor':'#FFFFFF','tertiaryColor':'#FAFAFA','background':'#FFFFFF'}}}%%
flowchart TD
    classDef step fill:#F5F5F5,stroke:#171717,color:#171717
    classDef alt fill:#FFFFFF,stroke:#525252,color:#171717
    classDef muted fill:#FAFAFA,stroke:#A3A3A3,color:#404040
    classDef result fill:#FFFFFF,stroke:#171717,color:#171717,stroke-width:2px

    J01["SIFAPJ01.jcl"]:::alt -->|"EXEC STEP010"| BPGT["BATCHPGT"]:::step
    J02["SIFAPJ02.jcl"]:::alt -->|"EXEC"| BREL["BATCHREL"]:::step

    BPGT -->|"CALLNAT"| SVCP["SUBVALCP"]:::step
    BPGT -->|"CALLNAT"| VELG["VALELEG"]:::step
    BPGT -->|"CALLNAT"| CBNF["CALCBENF"]:::step
    BPGT -->|"INCLUDE"| CAUD["CCAUDIT"]:::muted

    CADB["CADBENEF"]:::step -->|"CALLNAT"| SVCP
    CADB -->|"CALLNAT"| SVNI["SUBVALNI"]:::step
    CADB -->|"CALLNAT"| VBNF["VALBENEF"]:::step
    CONS["CONSBENF"]:::step -->|"CALLNAT"| SVCP
    VDOC["VALDOCS"]:::step -->|"CALLNAT"| SVNI
    CCOR["CALCCORR"]:::step -->|"CALLNAT"| SVCP
    CCOR -->|"INCLUDE"| CAUD
    SVCP -->|"INCLUDE"| CVCP["CCVALCPF"]:::muted
    BCON["BATCHCON"]:::step -->|"INCLUDE"| CAUD

    CDSC["CALCDSCT<br/>(orfao)"]:::muted

    BPGT -->|"READ / STORE"| B150[("BENEFIC 150")]:::result
    BPGT -->|"FIND / STORE"| P152[("PAYMENT 152")]:::result
    BPGT -->|"FIND"| S151[("SOCPROG 151")]:::result
    CBNF -->|"FIND / STORE"| P152
    CBNF -->|"FIND"| B150
    CBNF -->|"FIND"| S151
    VELG -->|"FIND"| B150
    VELG -->|"FIND"| S151
    CDSC -->|"FIND / UPDATE"| P152
    CCOR -->|"READ / UPDATE"| P152
    BCON -->|"FIND / UPDATE"| P152
    CAUD -->|"STORE"| A153[("AUDIT 153")]:::result
```

---

## Arestas Programa → Programa

| # | De | Para | Tipo | Evidência (`arquivo:linha`) |
|---|---|---|---|---|
| 1 | `BATCHPGT` | `SUBVALCP` | `CALLNAT` | `BATCHPGT.NSP:276` |
| 2 | `BATCHPGT` | `VALELEG` | `CALLNAT` | `BATCHPGT.NSP:369` |
| 3 | `BATCHPGT` | `CALCBENF` | `CALLNAT` | `BATCHPGT.NSP:381` |
| 4 | `BATCHPGT` | `CCAUDIT` | `INCLUDE` | `BATCHPGT.NSP:588` |
| 5 | `CADBENEF` | `SUBVALCP` | `CALLNAT` | `CADBENEF.NSP:161` |
| 6 | `CADBENEF` | `SUBVALNI` | `CALLNAT` | `CADBENEF.NSP:196` |
| 7 | `CADBENEF` | `VALBENEF` | `CALLNAT` | `CADBENEF.NSP:263` |
| 8 | `CONSBENF` | `SUBVALCP` | `CALLNAT` | `CONSBENF.NSP:136` |
| 9 | `VALDOCS` | `SUBVALNI` | `CALLNAT` | `VALDOCS.NSP:109` |
| 10 | `CALCCORR` | `SUBVALCP` | `CALLNAT` | `CALCCORR.NSP:160` |
| 11 | `CALCCORR` | `CCAUDIT` | `INCLUDE` | `CALCCORR.NSP:245` |
| 12 | `SUBVALCP` | `CCVALCPF` | `INCLUDE` | `SUBVALCP.NSN:96` |
| 13 | `BATCHCON` | `CCAUDIT` | `INCLUDE` | `BATCHCON.NSP` (área de sub-rotinas) |
| 14 | `SIFAPJ01` | `BATCHPGT` | `EXEC PGM=NATBATCH` | `SIFAPJ01.jcl:47-78` |

---

## Arestas Programa → DDM

| # | Programa | DDM | Operação | Evidência (`arquivo:linha`) |
|---|---|---|---|---|
| 1 | `BATCHPGT` | `BENEFIC` | `READ` | `BATCHPGT.NSP:247` |
| 2 | `BATCHPGT` | `PAYMENT` | `FIND NUMBER` | `BATCHPGT.NSP:299` |
| 3 | `BATCHPGT` | `SOCPROG` | `FIND` | `BATCHPGT.NSP:306` |
| 4 | `BATCHPGT` | `PAYMENT` | `STORE` | `BATCHPGT.NSP:488` |
| 5 | `CALCBENF` | `BENEFIC` | `FIND` | `CALCBENF.NSN:179` |
| 6 | `CALCBENF` | `SOCPROG` | `FIND` | `CALCBENF.NSN:190` |
| 7 | `CALCBENF` | `PAYMENT` | `STORE` | `CALCBENF.NSN:319` |
| 8 | `VALELEG` | `BENEFIC` | `FIND` | `VALELEG.NSN:89` |
| 9 | `VALELEG` | `SOCPROG` | `FIND` | `VALELEG.NSN:104` |
| 10 | `CALCDSCT` | `PAYMENT` | `FIND` / `UPDATE` | `CALCDSCT.NSP:78`, `:180` |
| 11 | `CALCCORR` | `PAYMENT` | `READ` / `UPDATE` | `CALCCORR.NSP:175`, `:210` |
| 12 | `BATCHCON` | `PAYMENT` | `FIND` / `UPDATE` | `BATCHCON.NSP:174`, `:207` |
| 13 | `CCAUDIT` | `AUDIT` | `READ` / `STORE` | `CCAUDIT.NSC:72`, `:108` |

---

## Observações

- **Programas mais conectados (hubs):** `SUBVALCP` (4 chamadores), `CCAUDIT` (3 inclusões), `PAYMENT` (5 programas escrevem nele) e `BENEFIC` (4 programas leem).
- **Programas isolados ou código morto:** `CALCDSCT.NSP` é **órfão** — nenhum `CALLNAT` aponta para ele em todo o acervo, apesar de o cabeçalho de `BATCHPGT.NSP:16` declarar "CALLS CALCBENF AND CALCDSCT". É um programa interativo com `INPUT` (`CALCDSCT.NSP:70`), incompatível com execução batch. `BATCHCON` não tem JCL (`BATCHCON.NSP:14-15`).
- **Ordem de dependência do batch:** `SIFAPJ01` → `BATCHPGT` → (`SUBVALCP`, `VALELEG`, `CALCBENF`) → `SIFAPJ02` → `BATCHREL`. A conciliação (`BATCHCON`) roda fora de qualquer cadeia automatizada.
- **Dupla escrita em `PAYMENT`:** tanto `CALCBENF.NSN:319` quanto `BATCHPGT.NSP:488` executam `STORE PAYMENT-V` no mesmo ciclo, para o mesmo beneficiario e período. Registrado como `SIFAP-M-05`.

---

## Definição de pronto

- [x] Toda aresta relevante ao escopo cita `arquivo:linha`.
- [x] Diagrama Mermaid gerado com o cabeçalho `%%{init:...}%%` e a paleta neutra.

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [Catálogo de Regras](business-rules-catalog.md)<br/><sub>Passo 2 — extração de regras.</sub> | [Questões em Aberto](mysteries-found.md)<br/><sub>Passo 4 — registro de incertezas.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
