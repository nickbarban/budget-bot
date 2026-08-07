create table app_user (
  id bigserial primary key,
  telegram_user_id bigint not null unique,
  telegram_chat_id bigint not null,
  created_at timestamptz not null default now()
);
create table budget (
  id bigserial primary key,
  user_id bigint not null references app_user(id) on delete cascade,
  daily_amount numeric(19,2) not null,
  currency varchar(3) not null default 'UAH',
  valid_from date not null,
  created_at timestamptz not null default now()
);
create index idx_budget_user_from on budget(user_id, valid_from desc);
create table bank_transaction (
  id bigserial primary key,
  user_id bigint not null references app_user(id) on delete cascade,
  bank varchar(32) not null,
  source varchar(16) not null,
  external_id varchar(128),
  fingerprint varchar(64) not null,
  occurred_at timestamptz not null,
  amount numeric(19,2) not null,
  currency varchar(3) not null,
  description text,
  merchant_name text,
  mcc integer,
  is_expense boolean not null,
  is_transfer boolean not null default false,
  is_refund boolean not null default false,
  raw_data jsonb,
  created_at timestamptz not null default now(),
  unique(user_id, bank, fingerprint)
);
create index idx_tx_user_time on bank_transaction(user_id, occurred_at);
create index idx_tx_user_expense_time on bank_transaction(user_id, is_expense, occurred_at);
create table transaction_import (
  id bigserial primary key,
  user_id bigint not null references app_user(id) on delete cascade,
  bank varchar(32) not null,
  source varchar(16) not null,
  original_filename text,
  imported_count integer not null default 0,
  duplicate_count integer not null default 0,
  created_at timestamptz not null default now()
);
