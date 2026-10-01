# Smoke test plan — iteration 1

Minimal check that Ubuntu deploy + n8n wiring work. Do not cover CSV parsers, PrivatBank, or `/api/v1` business APIs (not implemented on the backend yet).

## 0. Preconditions

- [ ] `./scripts/deploy-ubuntu-external.sh` finished without port errors
- [ ] n8n Data tables exist: `n8n_variables`, `budget_bot_variables`
- [ ] `budget_bot_variables` has `BUDGET_BACKEND_URL=http://172.17.0.1:18080` (n8n in Docker on Ubuntu)
- [ ] Workflows imported; Telegram / Header Auth credentials selected where needed

## 1. Backend is up

From your Mac:

```bash
curl -fsS http://<ubuntu-host>:18080/actuator/health
```

Expect: `{"status":"UP"}`

```bash
ssh nbarban@100.124.68.57 'cd /srv/docker/stacks/budget-bot && docker compose ps'
```

Expect: `postgres` healthy, `backend` running, host port **18080** (not 8080).

## 2. Data tables load in n8n

Open **Budget Bot - Backend Health Check**, Execute workflow.

Expect:

- **Load Shared Variables** returns rows from `n8n_variables` (at least `TIMEZONE`)
- **Load App Variables** returns `BUDGET_BACKEND_URL` and `BANK_WEBHOOK_FORWARD_SECRET`
- **Merge Variables** has `vars.BUDGET_BACKEND_URL` = `http://172.17.0.1:18080`

If Data table nodes error on table name: table is missing or named differently (lookup is by exact name).

## 3. n8n → backend (transport only)

**Backend Health** HTTP node calls `/api/v1/health`.

Expect: HTTP 200, body `{"status":"UP"}`.

That proves n8n can reach the container via `http://172.17.0.1:18080`.

Send `POST /api/v1/users/telegram/1/bootstrap` with header `X-API-Key` matching `BUDGET_BACKEND_API_KEY`. Expect 200 and `created: true`.

## 4. Telegram (optional, live backend path)

Backend still owns `POST /api/telegram/webhook`, not n8n `/api/v1/...`. Skip this step if the bot webhook is not registered.

If registered to the Spring app:

- send `/help` — bot replies
- send `/budget 1000` then `/status` — status text comes back

n8n **Backend - Bootstrap User** should return 200 after this `/api/v1` implementation. If the bot webhook still points at Spring `/api/telegram/webhook`, both paths work until Telegram is switched to n8n.

## Pass / fail

| Check | Pass |
|---|---|
| 1 Health `UP` on :18080 | required |
| 2 Data tables merge in n8n | required |
| 3 n8n HTTP → `/api/v1/health` | required |
| 4 `POST .../bootstrap` with `X-API-Key` | required |
| 5 Telegram via n8n `/start` | optional |

**Out of scope this iteration:** Monobank live `/sync`, PrivatBank CSV, push-webhook persistence.
