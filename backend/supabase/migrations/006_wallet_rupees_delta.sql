alter table if exists public.wallet_ledger
    add column if not exists rupees_delta integer not null default 0;
