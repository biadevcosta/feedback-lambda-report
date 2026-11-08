# feedback-lambda-report

## Projeto
Plataforma de feedback de aulas (Tech Challenge, fase 4), serverless na AWS. O aluno envia `POST /evaluations` com `{"description": string, "score": int 0 a 10}`. Os administradores recebem e-mail de urgência e um relatório semanal.

## Os 4 repositórios
- feedback-infra: Terraform de toda a infraestrutura e a documentação geral
- feedback-lambda-ingestion: recebe a avaliação e grava no DynamoDB
- feedback-lambda-notification: avisa os admins por e-mail quando a urgência é alta
- feedback-lambda-report: gera e envia o relatório semanal

## Convenções (definidas na tarefa 0.5; ajuste aqui se o grupo mudar)
- Região AWS: sa-east-1 (São Paulo) - CONFIRMADA. Prefixo de nomes: `feedback-`
- Funções: `feedback-ingestion`, `feedback-notification`, `feedback-report`. Java 21 (Maven), AWS SDK v2
- Grupos do Cognito: `students` e `admins`
- Idioma: código Java, comentários, nomes de variáveis/recursos de código e README em inglês. CLAUDE.md e PLANO.md continuam em português.
- Custos: a conta AWS **não tem free tier**. Antes de pedir pra criar/alterar qualquer recurso na AWS (fora do Terraform da infra, ex.: testar algo manualmente), avise o custo estimado e peça confirmação.
- Economia de requisições (orçamento de US$ 4/mês, sem dinheiro para excedente): o projeto é para entregar e rodar poucas vezes. Testes contra a AWS real só com o mínimo de chamadas, uma por vez e de forma manual. Proibido teste de carga, loops, scripts repetitivos ou agendamentos extras de teste. Testes automatizados (`mvn test`) usam mocks e nunca chamam a AWS. Antes de qualquer chamada real à API ou à AWS, diga quantas chamadas serão feitas.

## Contrato de dados (igual nos 4 repositórios)
Tabela `Evaluations`, chave `evaluationId` (string, UUID). Atributos: `userId` (sub do Cognito), `description`, `score` (número), `urgency` (`high`, `medium` ou `low`), `submittedAt` (ISO 8601 em UTC) e `day` (AAAA-MM-DD no horário de Brasília, UTC-3 fixo).
Índice `by-day`: chave `day`, ordenação `submittedAt`. Urgência (sugestão, editável): score 0 a 3 high, 4 a 6 medium, 7 a 10 low.

## Regras de trabalho
- Uma tarefa por vez do PLANO.md, só deste repositório. Confira "Depende de" antes de começar.
- Ao terminar e validar, marque `[x]` no PLANO.md e resuma em 1 ou 2 frases.
- Nunca commitar segredos, `.env`, `*.tfstate` nem `*.tfvars`. Sem chaves fixas da AWS: a esteira usa OIDC.
- Mudanças por branch e Pull Request (a main é protegida). Commits pequenos, mensagens curtas em português.
- Respostas curtas. Não cole saídas longas (resuma plan, logs e testes).
- Em dúvida entre opções, pergunte antes de decidir.
- LGPD: não registrar e-mails nem o texto das avaliações nos logs.

## Esta função
Disparada pelo EventBridge toda segunda às 8h (Brasília). Gera o relatório da semana e envia por e-mail aos admins (SES).
- Arquitetura e testes: seguir a skill `java-lambda-standard` (hexagonal: `domain/` com `ReportPeriod`, `WeeklyReportCalculator` e `ReportEmailRenderer` puros, `application/` com `SendWeeklyReportUseCase` e as portas `EvaluationQueryRepository`, `AdminDirectory` e `EmailSender`, `adapter/in/scheduler/`, `adapter/out/dynamodb/`, `adapter/out/cognito/`, `adapter/out/ses/`, `config/`). `ReportHandler.java` (`RequestHandler<ScheduledEvent, Void>`) na raiz do pacote só faz o bootstrap. Build com `./mvnw verify`.
- Dependências: `aws-lambda-java-core`, `aws-lambda-java-events`, AWS SDK v2 (`dynamodb`, `cognitoidentityprovider`, `ses`, com `url-connection-client`), `maven-shade-plugin`. Testes: JUnit5, Mockito, AssertJ, ArchUnit e `aws-lambda-java-serialization`.
- Variáveis de ambiente: `TABLE_NAME`, `INDEX_NAME` (`by-day`), `USER_POOL_ID`, `ADMIN_GROUP`, `SES_SENDER` e `DAYS` (7).
- Consultar o índice `by-day` com um Query por dia da janela, com paginação (laço com `lastEvaluatedKey`, mais fácil de testar com mock). Nunca usar Scan.
- Janela (sugestão, documentar no README): os 7 dias completos anteriores à execução, no horário de Brasília.
- Conteúdo: média dos scores (1 casa decimal), total de avaliações, quantidade por dia (incluindo dias com 0), quantidade por urgency e a lista com Description, Urgency e Submitted at. Sem avaliações na semana, enviar e-mail avisando.
- Separar classes/métodos puros (média, contagens e montagem do HTML) para testar fácil. O handler só orquestra.
- Permissões: `dynamodb:Query` só no índice `by-day`, `cognito-idp:ListUsersInGroup` e `ses:SendEmail`.
- Esteira como na ingestão (`mvn test` no PR; `mvn test` + `mvn package` + `update-function-code` na main).
- Testes (JUnit5 + Mockito, mockando `DynamoDbClient`, `CognitoIdentityProviderClient` e `SesClient`). Teste manual: invocar a função pelo console com evento vazio.
