# Registro de Questões em Aberto — Estágio 1

> **Trilha:** [Kit do Time](../README.md) › [Estágio 1](README.md) › **Questões em Aberto**

**Registro rastreável das incertezas do Estágio 1.** Cada entrada documenta uma pergunta sem resposta, com evidência, hipótese marcada como não confirmada e responsável pela validação.

| Campo | Valor |
|---|---|
| **Público-alvo** | Todas as duplas |
| **Pré-requisitos** | Ler os programas atribuídos |
| **Estágio** | Estágio 1 — Arqueologia |
| **Resultado esperado** | Perguntas sem conclusão, com evidência e responsável identificado |

> [!IMPORTANT]
> Uma pergunta só vira regra de negócio, requisito ou conclusão depois de validação humana explícita e com a evidência preservada como `path:line`. Este registro não é uma resposta e não substitui essa validação.

---

## Registro

Use uma linha por mistério. Preencha com o **ID canônico** da sua dupla (`SIFAP-M-01` … `SIFAP-M-20` — veja o [checklist](mysteries-checklist.md)) ou `BONUS` para achados fora da lista.

| ID | Questão em aberto | Evidência (`path:line`) | Impacto | Hipótese (não confirmada) | Pessoa/área responsável | Status |
|---|---|---|---|---|---|---|
| `SIFAP-M-01` | Por que `CADBENEF` chama `SUBVALCP` e depois avalia `#CPF-VALID` da rotina interna, ignorando `#PV-COD-RETURN`? | `CADBENEF.NSP:161-167` | A rotina corporativa de CPF não tem efeito no cadastro; migrar "a validação de CPF" pode mudar quem entra na base | Não confirmada: a chamada foi adicionada em 2011 sem remover a rotina antiga, e o teste ficou apontando para a variável errada | Coordenação de Benefícios | aberta |
| `SIFAP-M-02` | Por que `VALBENEF` opera em "modo aviso" e não impede a gravação de um cadastro inválido? | `CADBENEF.NSP:263-270` | Existem registros gravados que falham na própria validação do sistema; o Java precisa decidir se rejeita ou migra | Não confirmada: decisão temporária de 2011 aguardando revisão que nunca ocorreu | Coordenação de Benefícios | aberta |
| `SIFAP-M-03` | Por que beneficiários com mais de 75 anos recebem status `S` (suspenso) automaticamente no cadastro? | `CADBENEF.NSP:245-247` | Regra de negócio sem lei citada que suspende idosos; reproduzi-la ou não muda o resultado de milhares de pessoas | Não confirmada: possível controle de prova de vida implementado como efeito colateral do cadastro | Coordenação de Benefícios / Jurídico | aberta |
| `SIFAP-M-04` | Por que `CADDEPEN` limita dependentes a 5 e usa parentescos `FI/CO/IR/OU`, se o DDM permite 10 e define `FI/CJ/NT/TU`? | `CADDEPEN.NSP:118-120`, `:152-156` contra `BENEFIC.ddm:96-104` | Dois vocabulários de parentesco coexistem; dados históricos podem ter códigos que a tela nunca aceitou | Não confirmada: o DDM foi ampliado sem atualizar a tela de cadastro | Coordenação de Benefícios | aberta |
| `SIFAP-M-05` | Por que `BATCHPGT` chama `CALCBENF` e mantém o mesmo cálculo inline logo em seguida? | `BATCHPGT.NSP:381` e `:430-436` | Duas gravações de pagamento por beneficiário por período; define se a folha real paga uma ou duas vezes | Não confirmada: o ticket 6622/2011 previa remover o inline e continua aberto no comentário do código | Coordenação de Benefícios | aberta |
| `SIFAP-M-06` | Por que `CALCBENF` executa `STORE PAYMENT-V` sem nunca atribuir `NUM-PAYMENT`, que é descritor único? | `CALCBENF.NSN:305-320` contra `PAYMENT.ddm:31` | Se o Adabas rejeita a chave duplicada, o batch aborta com RC=12 no segundo beneficiário; se aceita, há registros órfãos | Não confirmada: comportamento depende da configuração de unicidade em produção, que não consta no acervo | DBA Adabas | aberta |
| `SIFAP-M-07` | Por que a fórmula do 13º usa fator etário, se o comentário diz `meses_ativos/12`? | `CALCBENF.NSN:275-278` (comentário em `:271-273`) | O 13º pago diverge do 13º documentado; proporcionalidade por tempo de vínculo simplesmente não existe no código | Não confirmada: a fórmula proporcional pode nunca ter sido implementada | Coordenação de Benefícios | aberta |
| `SIFAP-M-08` | Por que o cabeçalho de `BATCHPGT` declara que chama `CALCDSCT`, se nenhum `CALLNAT` para ele existe no acervo? | `BATCHPGT.NSP:16` contra `CALCDSCT.NSP:70` | A folha usa 3% fixo enquanto a rotina completa de descontos (faixas, teto de 30%, judicial) fica sem uso | Não confirmada: `CALCDSCT` é interativo (`INPUT`) e talvez nunca tenha sido convertido para subprograma | Coordenação de Benefícios | aberta |
| `SIFAP-M-09` | Por que região 99 concede elegíbilidade antes de verificar status, idade e renda? | `VALELEG.NSN:123-129` | Um beneficiário cancelado em região 99 passa como elegível; é um desvio de controle inteiro | Não confirmada: atalho para casos diplomáticos/internacionais adicionado em 2013 | Coordenação de Benefícios | aberta |
| `SIFAP-M-10` | Por que `MAX-PERCAP-INCOME` (renda per capita) é comparado com `AMT-FAMILY-INCOME` (renda total)? | `VALELEG.NSN:183-191` contra `SOCPROG.ddm:57` | Famílias grandes são excluídas indevidamente; o campo `IND-PERCAP-INCOME` existe no DDM e não é usado | Não confirmada: o cálculo per capita pode ter sido previsto e nunca ligado | Coordenação de Benefícios | aberta |
| `SIFAP-M-11` | Por que `CALCDSCT` compara tipos de desconto de 1 caractere, se o DDM define códigos de 2 (`IR/JD/CS/PA/EM/TX/OU/EX`)? | `CALCDSCT.NSP:127-170` contra `PAYMENT.ddm:47` | `CS` (consignado), `EM` (empréstimo) e `TX` (taxa) caem em `NONE` e são silenciosamente ignorados | Não confirmada: a variável `#TYPE-DISC (A1)` trunca o campo `A3` e o `DECIDE` casa apenas a primeira letra | Coordenação de Benefícios | aberta |
| `SIFAP-M-12` | Por que a sub-rotina `CALC-INDEX-ACCUM` aplica somente o índice do próprio mês, se o nome indica acumulação? | `CALCCORR.NSP:229-239` | A correção retroativa fica muito abaixo do devido; e fora de 2010–2012 o índice é 1,0 e nada acontece | Não confirmada: a iteração mês a mês até a data atual pode nunca ter sido escrita | Coordenação de Benefícios | aberta |
| `SIFAP-M-13` | Por que o retorno bancário `00` (crédito efetuado) grava status `P`, que o DDM define como PENDENTE? | `BATCHCON.NSP:207` contra `PAYMENT.ddm:57-59` | O estado real do pagamento no legado é ambíguo; a máquina de estados moderna depende dessa resposta | Não confirmada: `P` pode significar "pago" no código e "pendente" no DDM, com a legenda desatualizada | DBA Adabas / Coordenação de Benefícios | aberta |
| `SIFAP-M-14` | Por que `VALBENEF` aceita CPF com todos os dígitos iguais iniciados em `000`, se `SUBVALCP` rejeita `00000000000`? | `VALBENEF.NSN:239-242` contra `SUBVALCP.NSN:57-60` | Dois validadores de CPF discordam sobre o mesmo documento; a migração precisa de uma única regra | Não confirmada: exceção para CPF de teste de governo que vazou para produção | Coordenação de Benefícios | aberta |
| `SIFAP-M-15` | Por que fevereiro tem 29 dias fixos na tabela compartilhada desde 1997? | `LDASIFAP.NSL:76`, `VALBENEF.NSN:105` | Datas 29/02 de anos não bissextos foram aceitas por 29 anos; o Java vai rejeitá-las na migração | Não confirmada: ticket 3120 citado no comentário não está no acervo | DBA Adabas | aberta |
| `SIFAP-M-16` | Por que a sequência de auditoria é lida uma vez e incrementada em memória, sendo `NUM-AUDIT` descritor único? | `CCAUDIT.NSC:70-77` contra `AUDIT.ddm:31` | Dois usuários simultâneos colidem na chave; pode haver perda silenciosa de trilha exigida pela IN-TCU 63/2010 | Não confirmada: o desenho assume execução serial, o que vale para batch mas não para as telas online | DBA Adabas / Auditoria | aberta |
| `SIFAP-M-17` | Por que `RELAUDIT` remove as ações `EX` (exclusão) do relatório de auditoria? | `RELAUDIT.NSP:125-129`, confirmado por `AUDIT.ddm:151-153` | Exclusões existem na base mas ficam invisíveis no relatório oficial; é um risco de conformidade | Não confirmada: filtro adicionado em 2006 sem justificativa registrada | Auditoria | aberta |
| `SIFAP-M-18` | Por que `RELAUDIT` conta `CO` como conciliação e `CN` como consulta, se o DDM define `CO` como consulta e não tem `CN`? | `RELAUDIT.NSP:160-172` contra `AUDIT.ddm:38-47` | Os números do relatório de auditoria não correspondem às ações realmente gravadas | Não confirmada: o significado dos códigos mudou ao longo do tempo e só o relatório foi atualizado | Auditoria | aberta |
| `SIFAP-M-19` | Por que `RELPGT` e `BATCHREL` exibem o status `P` como "PAGO" e `C` como "CANCELADO", invertendo o DDM? | `RELPGT.NSP:186-200`, `BATCHREL.NSP:96-100` contra `PAYMENT.ddm:57-59` | Combinado com `SIFAP-M-13`, explica por que a inversão nunca foi percebida: o relatório confirma o erro do batch | Não confirmada: relatório e conciliação compartilham a mesma interpretação divergente do DDM | Coordenação de Benefícios | aberta |
| `SIFAP-M-20` | Por que a tabela de fator regional tem 27 posições rotuladas por UF, mas é indexada por `COD-REGION`, que o DDM define como 01-05 ou 99? | `CALCBENF.NSN:201`, `BATCHPGT.NSP:391`, `LDASIFAP.NSL:36-46` contra `BENEFIC.ddm:77` | Somente as 5 primeiras posições (AC, AM, AP, PA, RO) são alcançáveis; as outras 22 são inatingíveis e a região 99 cai no `ELSE` com fator 1,0 | Não confirmada: a tabela pode ter sido projetada para UF e a indexação migrada para região sem conversão | Coordenação de Benefícios | aberta |

### Achados adicionais (bônus)

Achados legítimos fora dos 20 mistérios canônicos. Contam no debrief, **não** mudam o denominador e **não** substituem um mistério canônico que ficou faltando.

| ID | Questão em aberto | Evidência (`path:line`) | Impacto | Hipótese (não confirmada) | Pessoa/área responsável | Status |
|---|---|---|---|---|---|---|
| `BONUS` | De onde vem a constante `0.347215` aplicada ao valor base no cadastro de programa? | `CADPROG.NSP:124-125` | O valor base gravado já vem ajustado, e `CALCBENF.NSN:262` aplica `(1 + FACTOR-ADJUST)` **de novo** — possível dupla aplicação do reajuste | Não confirmada: pode ser o `FACTOR-K` que o `SOCPROG.ddm:44-48` marca como `>>> UNDOCUMENTED <<<` | Coordenação de Benefícios (SENARC) | aberta |
| `BONUS` | Por que o endereço é capturado em `A80` e gravado em campo `A60`, perdendo 20 bytes? | `CADBENEF.NSP:277-279` | Endereços longos estão truncados na base há anos; afeta a correspondência oficial | Não confirmada: ticket 4471/2003 citado no comentário, sem correspondência no acervo | Coordenação de Benefícios | aberta |
| `BONUS` | Por que existem três implementações distintas de validação de CPF por módulo 11? | `SUBVALCP.NSN`+`CCVALCPF.NSC`, `VALBENEF.NSN:196+`, `VALDOCS.NSP:137+` | A "fonte única de verdade" declarada em `SUBVALCP.NSN:8` não é única; o Java precisa escolher uma | Não confirmada: consolidação iniciada em 2011 e nunca concluída | Coordenação de Benefícios | aberta |
| `BONUS` | Por que `SIFAPJ01` traz o período `202601` fixo no `CMSYNIN`, se o ticket 5980/2012 introduziu período por parâmetro? | `SIFAPJ01.jcl:74` | Reprocessar outro período exige editar o JCL em produção | Não confirmada: o valor pode ser substituído pelo agendador Control-M em tempo de execução | Operações / DevOps | aberta |
| `BONUS` | Por que `BATCHCON` não popula `STAT-RECONCIL`, `DT-RECONCIL` nem `AMT-RECONCILED`, que existem no DDM para isso? | `BATCHCON.NSP:200-222` contra `PAYMENT.ddm:96-101` | O bloco de conciliação do DDM está vazio; a informação vive em campos de outro significado | Não confirmada: campos criados em 2005 e nunca conectados ao programa | DBA Adabas | aberta |

---

## Regras de integridade

- Registre apenas questões em aberto; não escreva uma resposta no catálogo.
- Mantenha a evidência no formato `path:line` para preservar a rastreabilidade.
- Marque toda hipótese explicitamente como **não confirmada**.
- Só a pessoa responsável pode dar a validação humana e mudar o status.
- Sem evidência humana, a questão continua em aberto.

---

### Continue lendo

| Anterior | Próximo |
|---|---|
| [Checklist de Questões em Aberto](mysteries-checklist.md)<br/><sub>Verificação de rastreabilidade.</sub> | [Relatório de Descoberta](discovery-report.md)<br/><sub>Consolidação final do estágio.</sub> |

<sub>[Voltar ao índice do kit](../README.md)</sub>
