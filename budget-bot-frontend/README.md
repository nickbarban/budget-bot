# Budget Bot Frontend / Integration Layer

Thin integration frontend for the Budget Bot project.

## Responsibility split

**n8n owns transport/orchestration only:**
- Telegram updates and replies
- downloading uploaded CSV files from Telegram
- forwarding commands/files to the Spring Boot backend
- receiving bank webhooks and forwarding raw events to the backend
- basic transport-level error handling

**Spring Boot owns all business logic:**
- users and bank connections
- Monobank API tokens and sync
- CSV bank detection/parsing (Monobank / PrivatBank)
- transaction normalization and deduplication
- expense classification
- budgets and status calculation
- persistence in PostgreSQL

No budgeting, transaction filtering, deduplication, or bank-specific parsing should live in n8n.

## Workflows

1. `workflows/telegram-budget-bot.json`
   - Telegram Trigger
   - routes `/start`, `/help`, `/status`, `/budget <amount>`, `/sync`
   - downloads `.csv` documents
   - calls backend REST API
   - formats only transport/UI responses and sends them to Telegram

2. `workflows/monobank-webhook.json`
   - public n8n Webhook endpoint for Monobank
   - supports GET verification
   - forwards POST payload unchanged to Spring Boot
   - backend validates/processes/persists the event

3. `workflows/backend-health.json`
   - optional manual workflow to check backend connectivity from n8n

PrivatBank is CSV-only in the MVP. No Privat webhook workflow is included because there is no personal-card webhook contract being used by this project.

## Required n8n credentials

Create these in n8n before importing/activating workflows:

- **Budget Bot Telegram** — Telegram API credential containing the bot token.
- **Budget Backend Auth** — Header Auth credential:
  - Header: `X-API-Key`
  - Value: same value as `BUDGET_BACKEND_API_KEY` on the backend.

The workflow uses `BUDGET_BACKEND_URL` from the n8n environment.

## Backend contract

See:
- `backend-contract/openapi.yaml`
- `docs/BACKEND_CHANGES_REQUIRED.md`

The previously generated backend still handles Telegram directly. The target architecture removes that responsibility and exposes transport-neutral REST endpoints instead.

## Telegram UX

Commands:

- `/start`
- `/help`
- `/status`
- `/budget 1000`
- `/sync`

File upload:

- upload a Monobank `.csv`
- upload a PrivatBank `.csv`

n8n forwards the file to `POST /api/v1/imports/csv`; backend detects the bank automatically.

## Import into n8n

Import each JSON file via **Workflows → Import from File**. Then assign the two credentials above to the corresponding nodes and set `BUDGET_BACKEND_URL` in the n8n runtime environment.

## Recommended deployment

Internet
→ Telegram / Monobank
→ HTTPS / n8n
→ private HTTPS/network / Spring Boot
→ PostgreSQL

The backend does not need to be directly exposed to Telegram or Monobank if n8n can reach it privately.
