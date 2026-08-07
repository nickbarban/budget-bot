# Budget Bot Backend

Spring Boot backend for a Telegram personal-budget bot. Telegram is only the frontend; all state and business logic live in this application and PostgreSQL.

## MVP capabilities

- Telegram webhook frontend
- `/budget 1000` stores a daily cumulative allowance
- `/sync` imports the current month from Monobank personal API
- `/status` calculates the budget only from PostgreSQL
- Monobank CSV upload through Telegram
- Shared normalized transaction model and deduplication
- PrivatBank CSV adapter boundary (parser awaits one real Privat24 CSV sample)
- Flyway migrations + PostgreSQL
- Monobank webhook endpoint scaffold for push transactions

## Budget semantics

For day `D` of a month:

`allowed = dailyBudget × D`

`balance = allowed - qualifying expenses since month start`

A positive balance carries forward. Example: with 1,000 UAH/day, on July 24 the allowance is 24,000 UAH. If 23,000 UAH was spent, the balance is +1,000 UAH. If another 1,500 UAH is spent, the balance becomes -500 UAH; on July 25, with no new expenses, it becomes +500 UAH.

## Stack

- Java 21
- Spring Boot 3.5.16
- Spring Web / RestClient
- Spring Data JPA
- PostgreSQL 17
- Flyway
- Apache Commons CSV
- Maven

## Run locally

```bash
cp .env.example .env
docker compose up -d
export $(grep -v '^#' .env | xargs)
./mvnw spring-boot:run   # if wrapper is generated
# or
mvn spring-boot:run
```

Required secrets:

- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_WEBHOOK_SECRET`
- `MONOBANK_TOKEN`

Do not commit `.env`.

## Telegram webhook

Expose the backend over HTTPS and register:

`https://<host>/api/telegram/webhook`

Set Telegram's `secret_token` to the same value as `TELEGRAM_WEBHOOK_SECRET`. The controller checks `X-Telegram-Bot-Api-Secret-Token`.

## Monobank

Personal API configuration:

- Base URL: `https://api.monobank.ua`
- Token header: `X-Token`
- Statement endpoint used by `/sync`: `/personal/statement/{account}/{from}/{to}`
- Default account can be configured with `MONOBANK_ACCOUNT_ID`, default `0`

The application deliberately persists transactions and serves `/status` from PostgreSQL instead of calling Monobank on every status request.

### Push webhook

`GET /api/monobank/webhook` already returns HTTP 200 for Monobank URL validation.

`POST /api/monobank/webhook` is scaffolded for milestone 2. To make it production-ready, add an account-to-user mapping and persist `StatementItem` through the existing `MonobankMapper`.

## CSV architecture

`BankCsvDetector -> BankCsvParser -> NormalizedTransaction -> TransactionImportService`

Monobank CSV is implemented against the provided export structure. PrivatBank has a dedicated adapter placeholder so the core does not need to change when a real Privat24 sample is added.

## Important MVP assumptions

1. This build is optimized for a personal/single-owner bot. The Monobank token is an environment secret, not stored per user.
2. Currency conversion is not implemented yet. Budget calculations currently assume UAH card amounts.
3. MCC 4829 and descriptions such as `На баловство` are treated as transfers/non-expenses. This should evolve into configurable rules.
4. Monobank API and Monobank CSV fingerprints differ today. Cross-source API-vs-CSV deduplication is a milestone-2 item; same-source reimports are deduplicated.
5. PrivatBank CSV parsing requires a real export sample before implementation is considered correct.

## Suggested next milestones

1. Add `bank_connection` / `bank_account` tables and encrypted per-user credentials.
2. Finish Monobank webhook ingestion and account mapping.
3. Cross-source deduplication between Monobank API and CSV.
4. Finalize PrivatBank CSV parser from a real statement.
5. Add configurable expense rules and category budgets.
6. Add integration tests with Testcontainers PostgreSQL.
