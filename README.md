# feedback-lambda-relatorio

Lambda de **relatório semanal** da Plataforma de Feedback de Aulas (Tech Challenge fase 4).

Agendada pelo **EventBridge** para toda segunda-feira às 8h de Brasília (`cron(0 11 ? * MON *)`, em UTC). Consulta os últimos 7 dias no DynamoDB e envia o resumo por e-mail aos admins via SES.

- **Função AWS:** `feedback-relatorio` · **Runtime:** Python 3.12 · **Região:** `sa-east-1`
- **Permissões:** `dynamodb:Query` no índice `por-dia` e `ses:SendEmail`

## Conteúdo do relatório

- Média das notas dos últimos 7 dias
- Quantidade de avaliações por dia e por urgência
- Lista com descrição, urgência e data de envio

## Estrutura (em construção)

```
src/handler.py          # lambda_handler
tests/                  # pytest para média e contagens
requirements-dev.txt
.github/workflows/ci-cd.yml
```

## Repositórios relacionados

`feedback-infra` · `feedback-lambda-ingestao` · `feedback-lambda-notificacao`

## Status

Acompanhe o andamento em [PLANO.md](PLANO.md).
