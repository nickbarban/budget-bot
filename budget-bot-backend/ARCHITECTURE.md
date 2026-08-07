# Architecture

```text
Telegram Bot
    |
    v
telegram adapter ---------------------------+
    |                                       |
    | commands / file                       |
    v                                       |
application services                        |
    |                                       |
    +--> banking/monobank --> Monobank API  |
    |                                       |
    +--> banking/csv --> Mono/Privat parser |
    |                                       |
    v                                       |
NormalizedTransaction                       |
    |                                       |
    v                                       |
TransactionImportService -> PostgreSQL <----+
    |
    v
BudgetService -> Telegram formatter -> Telegram API
```

The key design decision is that `BudgetService` knows nothing about Monobank, PrivatBank, Telegram, or CSV.
