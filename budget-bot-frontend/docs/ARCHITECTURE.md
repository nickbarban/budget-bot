# Frontend architecture

```text
Telegram
   |
   v
n8n: telegram-budget-bot
   |  REST
   v
Spring Boot API -----------------> PostgreSQL
   ^
   | raw bank event
n8n: monobank-webhook
   ^
   |
Monobank webhook
```

## Design rule

n8n must stay stateless with respect to the financial domain. Re-running or replacing n8n workflows must not affect budgets or transaction history.

### n8n may
- identify the Telegram command shape
- download a Telegram file
- pass IDs/arguments/files to backend
- transform backend DTOs into Telegram text
- retry safe transport failures

### n8n must not
- calculate budget balance
- decide whether an operation is an expense
- parse Monobank or PrivatBank CSV columns
- deduplicate transactions
- store bank tokens
- call Monobank statement API for application business logic

Bank API access belongs to Spring Boot.
