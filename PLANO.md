# Plano - feedback-lambda-report

Tarefas extraídas da planilha de planejamento. Siga a ordem e respeite "Depende de".
Ao validar uma tarefa, troque `[ ]` por `[x]`.

Como pedir ao Claude: `Execute a tarefa X.Y do PLANO.md` (uma por sessão; use /clear entre tarefas).

## Tarefas deste repositório

### Fase 5 - Relatório

- [ ] **5.4** ReportHandler.java: consultar os últimos 7 dias, calcular a média, a quantidade por dia e por urgency e listar description, urgency e data
  - Validar: Teste local devolve os números esperados
  - Depende de: 5.2

- [ ] **5.5** Montar o e-mail do relatório (HTML simples) e enviar pelo SES
  - Validar: E-mail de teste aparece bem formatado
  - Depende de: 5.4

- [ ] **5.6** Testes unitários (JUnit5 + Mockito) para a média e as contagens
  - Validar: mvn test verde
  - Depende de: 5.4

- [ ] **5.7** Workflow de CI/CD (copiar o modelo da ingestão)
  - Validar: Push na main atualiza a função
  - Depende de: 5.3, 5.6

### Fase 7 - Documentação e entrega

- [ ] **7.5** README da função: agendamento, dados do relatório, variáveis de ambiente, permissões e como testar
  - Validar: README completo
  - Depende de: 5.8

## Pré-requisitos em outros repositórios (não fazer aqui)

Confirme com o time que estas tarefas já foram concluídas antes de começar.

- **5.2** (feedback-infra): Função de relatório com permissões Query no índice by-day e ses:SendEmail
- **5.3** (feedback-infra): Role OIDC do repositório feedback-lambda-report, só com lambda:UpdateFunctionCode nesta função
- **5.8** (grupo): Disparar o relatório manualmente e conferir o e-mail
