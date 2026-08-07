# Changes required in budget-bot-backend

The current backend ZIP contains `TelegramWebhookController`, `TelegramUpdateService`, and `TelegramClient`. In the target architecture Telegram is a frontend adapter in n8n.

## Remove / deprecate

- `com.budgetbot.telegram.TelegramWebhookController`
- direct Telegram Bot API calls from backend
- Telegram-specific message formatting from business services

The backend may keep `telegramChatId` as an external user identity, but must not depend on Telegram SDK/API classes.

## Add REST endpoints

Implement the contract in `backend-contract/openapi.yaml`:

- `POST /api/v1/users/telegram/{telegramUserId}/bootstrap`
- `GET /api/v1/users/telegram/{telegramUserId}/budget/status`
- `PUT /api/v1/users/telegram/{telegramUserId}/budget/daily`
- `POST /api/v1/users/telegram/{telegramUserId}/banks/monobank/sync`
- `POST /api/v1/users/telegram/{telegramUserId}/imports/csv`
- `GET /api/v1/health`
- `GET /api/v1/webhooks/monobank`
- `POST /api/v1/webhooks/monobank`

## API response principle

Return structured JSON, not Telegram-formatted text.

Example `BudgetStatusResponse`:

```json
{
  "date": "2026-08-07",
  "currency": "UAH",
  "dailyBudget": 1000.00,
  "allowedToDate": 7000.00,
  "spentToDate": 6450.50,
  "balance": 549.50,
  "spentToday": 730.00,
  "projectedTomorrowBalance": 1549.50,
  "remainingMonthBudget": 24549.50,
  "recommendedDailyLimit": 1022.90,
  "countedTransactions": 37,
  "lastSyncedAt": "2026-08-07T16:55:00+03:00"
}
```

Formatting symbols such as 🟢/🔴 and Telegram HTML belong to n8n.

## Authentication between n8n and backend

MVP: static `X-API-Key` header over private network/HTTPS.

Later: service-to-service JWT/mTLS if needed.

## Monobank webhook

n8n is the public endpoint and forwards the raw Monobank body to Spring Boot. Backend should treat event processing as idempotent and deduplicate using Monobank transaction IDs.
