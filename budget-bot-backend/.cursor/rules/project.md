# Budget Bot project rules

- Architecture: modular monolith. Telegram is an adapter/front end; business logic must not live in Telegram handlers.
- Backend: Java 21, Spring Boot, PostgreSQL, Flyway.
- Treat banking sources as adapters. Normalize every external transaction before persistence.
- Budget calculations must query persisted normalized transactions, never Telegram payloads or raw CSV directly.
- Preserve raw bank payloads where useful, but never log tokens or secrets.
- Prefer explicit domain/application services over large controllers.
- Keep Monobank-specific behavior out of the budget package.
- CSV parsers must be bank-specific and selected through BankCsvDetector.
- Deduplication is mandatory for imports. Improve cross-source deduplication before enabling automatic webhook + CSV overlap.
- Do not invent PrivatBank CSV columns; implement only from an actual sample file.
