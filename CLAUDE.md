# feedback-lambda-relatorio

## Projeto
Plataforma de feedback de aulas (Tech Challenge, fase 4), serverless na AWS. O aluno envia `POST /avaliacao` com `{"descricao": string, "nota": int 0 a 10}`. Os administradores recebem e-mail de urgência e um relatório semanal.

## Os 4 repositórios
- feedback-infra: Terraform de toda a infraestrutura e a documentação geral
- feedback-lambda-ingestao: recebe a avaliação e grava no DynamoDB
- feedback-lambda-notificacao: avisa os admins por e-mail quando a urgência é alta
- feedback-lambda-relatorio: gera e envia o relatório semanal

## Convenções (definidas na tarefa 0.5; ajuste aqui se o grupo mudar)
- Região AWS: sa-east-1 (sugestão; confirmar). Prefixo de nomes: `feedback-`
- Funções: `feedback-ingestao`, `feedback-notificacao`, `feedback-relatorio`. Python 3.12
- Grupos do Cognito: `alunos` e `admins`

## Contrato de dados (igual nos 4 repositórios)
Tabela `Avaliacoes`, chave `avaliacaoId` (string, UUID). Atributos: `usuarioId` (sub do Cognito), `descricao`, `nota` (número), `urgencia` (`alta`, `media` ou `baixa`), `dataEnvio` (ISO 8601 em UTC) e `dia` (AAAA-MM-DD no horário de Brasília, UTC-3 fixo).
Índice `por-dia`: chave `dia`, ordenação `dataEnvio`. Urgência (sugestão, editável): nota 0 a 3 alta, 4 a 6 media, 7 a 10 baixa.

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
- Estrutura: `src/handler.py` (`lambda_handler`), `tests/`, `requirements-dev.txt`, `.github/workflows/ci-cd.yml` e `README.md`.
- Variáveis de ambiente: `TABLE_NAME`, `INDEX_NAME` (`por-dia`), `USER_POOL_ID`, `ADMIN_GROUP`, `SES_SENDER` e `DIAS` (7).
- Consultar o índice `por-dia` com um Query por dia da janela, com paginação. Nunca usar Scan.
- Janela (sugestão, documentar no README): os 7 dias completos anteriores à execução, no horário de Brasília.
- Conteúdo: média das notas (1 casa decimal), total de avaliações, quantidade por dia (incluindo dias com 0), quantidade por urgência e a lista com Descrição, Urgência e Data de envio. Sem avaliações na semana, enviar e-mail avisando.
- Separar funções puras (média, contagens e montagem do HTML) para testar fácil. O handler só orquestra.
- Permissões: `dynamodb:Query` só no índice `por-dia`, `cognito-idp:ListUsersInGroup` e `ses:SendEmail`.
- Esteira e testes como na ingestão. Teste manual: invocar a função pelo console com evento vazio.
