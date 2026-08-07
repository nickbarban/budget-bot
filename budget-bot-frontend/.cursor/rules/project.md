# Budget Bot frontend/integration project rules

- n8n is a thin transport layer. Never implement financial business logic in workflow Code nodes.
- Spring Boot is the source of truth for users, bank connections, imports, transactions, classification, deduplication, budgets, and status calculations.
- Keep workflows importable into current n8n and avoid community nodes.
- Secrets belong in n8n credentials/environment variables; never commit Telegram tokens, Monobank tokens, backend API keys, or webhook secrets.
- Backend responses are structured DTOs. Telegram formatting belongs in the Telegram workflow.
- Bank webhook payloads must be forwarded losslessly; do not remodel them in n8n.
- PrivatBank is CSV-only until an explicit supported webhook/API contract is added.
