# Setup

## 1. Backend

Apply `BACKEND_CHANGES_REQUIRED.md` to the backend project and run it with PostgreSQL.

Required env on backend:
- API key matching n8n Header Auth credential
- webhook forward secret matching n8n `BANK_WEBHOOK_FORWARD_SECRET`
- Monobank connection/token configuration managed by backend

## 2. n8n environment

Set:

```bash
BUDGET_BACKEND_URL=http://budget-backend:8080
BANK_WEBHOOK_FORWARD_SECRET=...
```

If n8n and Spring Boot share Docker/network, keep backend private and use the service hostname.

## 3. Credentials

### Budget Bot Telegram
Create Telegram credential using BotFather token.

### Budget Backend Auth
Create `Header Auth`:

```text
Name: Budget Backend Auth
Header: X-API-Key
Value: <backend api key>
```

After importing workflows, replace credential placeholders by selecting the saved credentials in every Telegram/HTTP node.

## 4. Import workflows

Import:
- `telegram-budget-bot.json`
- `monobank-webhook.json`
- optionally `backend-health.json`

Activate Telegram and Monobank workflows.

## 5. Monobank webhook URL

The public production webhook URL from n8n will look like:

```text
https://<n8n-host>/webhook/budget-bot/monobank
```

Register that URL through the Monobank personal API from the backend connection setup flow. Do not store the Monobank token in n8n.

## 6. Test

Telegram:

```text
/start
/budget 1000
/sync
/status
```

Then upload a Monobank CSV.

PrivatBank CSV support becomes fully testable after adding a real Privat24 sample to the backend parser tests.
