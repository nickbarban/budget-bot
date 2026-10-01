# Setup

## 1. Backend

Apply `BACKEND_CHANGES_REQUIRED.md` to the backend project and run it with PostgreSQL.

Required env on backend:
- API key matching n8n Header Auth credential
- webhook forward secret matching n8n `BANK_WEBHOOK_FORWARD_SECRET`
- Monobank connection/token configuration managed by backend

## 2. n8n Data tables (not n8n Variables, not Postgres)

Free n8n has no `$vars`. Workflows read config from **Data tables** (Overview → Data tables).

| Data table | Scope |
|---|---|
| `n8n_variables` | shared across every n8n app |
| `budget_bot_variables` | this app; keys override the shared table |
| `<project_name>_variables` | next apps, same columns |

Create both tables with columns:

| Column | Type |
|---|---|
| `key` | string |
| `value` | string |
| `description` | string |

CSV templates to import (or copy rows from):

- `n8n/data-tables/n8n_variables.csv`
- `n8n/data-tables/budget_bot_variables.csv`
- `n8n/data-tables/template_project_variables.csv` — copy for the next app

In n8n: create the table with those three columns, then import the matching CSV. Replace `change-me` values after import.

`BUDGET_BACKEND_URL` must be reachable **from the n8n process**, not from your Mac:

| Where n8n runs | URL |
|---|---|
| Docker on the same Ubuntu as backend | `http://172.17.0.1:18080` |
| Directly on that Ubuntu (no container) | `http://127.0.0.1:18080` |
| n8n Cloud | public HTTPS URL (nginx/caddy/ngrok) |

Do **not** use `host.docker.internal` on Linux — it does not resolve, and the HTTP node fails with “incorrect host (domain) value”. Port is **18080**, not 8080.

App keys win when the same `key` exists in both tables. Edit a cell in the Data tables UI — no workflow change.

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

After importing workflows, replace credential placeholders by selecting the saved credentials in every Telegram/HTTP node. Data table nodes look up `n8n_variables` and `budget_bot_variables` by name — no extra credential.

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
