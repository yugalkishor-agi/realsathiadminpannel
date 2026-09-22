create table if not exists public.wallet_ledger (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null,
    kind text not null default 'adjustment',
    title text not null,
    detail text not null default '',
    amount_text text not null default '0',
    coins_delta integer not null default 0,
    rupees_delta integer not null default 0,
    recharge_amount_rupees integer not null default 0,
    status text not null default 'completed',
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now(),
    constraint wallet_ledger_kind_check check (kind in ('audio_call', 'video_call', 'chat', 'payment', 'adjustment')),
    constraint wallet_ledger_status_check check (status in ('completed', 'rejected', 'pending'))
);

create index if not exists wallet_ledger_user_created_idx
    on public.wallet_ledger (user_id, created_at desc);
