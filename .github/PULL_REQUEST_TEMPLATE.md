# Solicitação de incorporação (Pull Request)

## Descrição

<!-- Descreva o que foi implementado nesta PR -->

## Estágio

- [ ] Estágio 1 - Arqueologia
- [ ] Estágio 2 - Especificação moderna
- [ ] Estágio 3 - Implementação
- [ ] Estágio 4 - Evolução (IaC)

## Persona responsável

<!-- Qual persona criou esta PR? -->

## Lista de verificação

- [ ] O código compila sem erros
- [ ] Os testes unitários passam
- [ ] Nenhum dado sensível é exposto
- [ ] CHANGELOG ou README atualizado, se necessário
- [ ] Vinculado ao REQ-ID correspondente na especificação (quando aplicável)
- [ ] Todo REQ-ID novo ou alterado inclui um `source_legacy:` válido ou `[GREENFIELD]` justificado
- [ ] Alterações no fluxo de trabalho usam permissões de privilégio mínimo e ações do GitHub fixadas por SHA

## Revisão de PR gerada por IA

> Preencha somente quando a PR foi aberta pelo Copilot Agent. Uma PR gerada por IA é **rascunho** até uma pessoa percorrer esta lista.

- [ ] **Regra preservada, não reinventada.** Toda mudança de cálculo cita o programa Natural de origem com `arquivo:linha`. Uma fórmula “melhorada” sem citação é rejeição.
- [ ] **Truncamento intacto.** Nenhum `RoundingMode.HALF_UP` foi introduzido; `Money.truncate()` continua sendo o único ponto de redução de escala ([ADR-003](../02-modern-spec/adr/ADR-003-representacao-monetaria.md)).
- [ ] **Mistério não foi resolvido por conta própria.** Se a PR remove um `UnresolvedLegacyRuleException`, existe decisão humana registrada em [`mysteries-found.md`](../01-archaeology/mysteries-found.md). Sem isso, é adivinhação com aparência de correção.
- [ ] **Fronteira de módulo intacta.** `benefitcalculation` continua sem depender de persistência ou web; as regras ArchUnit não foram afrouxadas nem anotadas com `@ArchIgnore`.
- [ ] **Teste prova a regra, não a implementação.** Um teste que só repete o código sem asserção de valor esperado não conta.
- [ ] **Cobertura não caiu.** O portão de 85% em `benefitcalculation` e `shared` continua ativo, e o limite não foi reduzido no `pom.xml`.
- [ ] **CPF mascarado em toda saída.** Log, resposta HTTP, mensagem de erro e trilha de auditoria (REQ-031).
- [ ] **Sem segredo no código.** Nenhuma string de conexão, chave ou senha; segredos vêm do Key Vault.
- [ ] **Migração é reversível.** Nenhum `DROP TABLE` nem `DROP COLUMN` sem estratégia de expandir e contrair.

## REQ-IDs atendidos

<!-- Exemplo: REQ-001, REQ-003 -->
