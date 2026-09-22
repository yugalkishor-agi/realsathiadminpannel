-- Tables used by the Android app's Edge Functions. Keep this migration in the
-- same Supabase project as the functions; the backend migration folder is separate.
alter table public.users
  add column if not exists topic_tags text[] not null default '{}',
  add column if not exists native_languages text[] not null default '{}';

create table if not exists public.user_reports (
  id uuid primary key default gen_random_uuid(),
  reporter_id uuid not null references public.users(id) on delete cascade,
  reported_id uuid not null references public.users(id) on delete cascade,
  reason text not null default 'other',
  context text not null default 'profile',
  note text,
  status text not null default 'pending',
  created_at timestamptz not null default now()
);

create table if not exists public.user_blocks (
  id uuid primary key default gen_random_uuid(),
  blocker_id uuid not null references public.users(id) on delete cascade,
  blocked_id uuid not null references public.users(id) on delete cascade,
  created_at timestamptz not null default now(),
  unique (blocker_id, blocked_id)
);

create table if not exists public.wallet_ledger (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.users(id) on delete cascade,
  kind text not null,
  title text not null,
  detail text not null default '',
  amount_text text not null default '0',
  coins_delta integer not null default 0,
  rupees_delta integer not null default 0,
  recharge_amount_rupees integer not null default 0,
  status text not null default 'completed',
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  constraint wallet_ledger_kind_check check (kind in
    ('audio_call', 'video_call', 'chat', 'payment', 'withdrawal', 'payout', 'adjustment')),
  constraint wallet_ledger_status_check check (status in
    ('completed', 'rejected', 'pending', 'processing', 'failed'))
);

create table if not exists public.host_kyc_details (
  user_id uuid primary key references public.users(id) on delete cascade,
  full_name text not null default '',
  email_address text not null default '',
  whatsapp_number text not null default '',
  phone_number text not null default '',
  date_of_birth text not null default '',
  aadhaar_masked text not null default '',
  pan_masked text not null default '',
  bank_name text not null default '',
  bank_account_masked text not null default '',
  ifsc_code text not null default '',
  account_holder_name text not null default '',
  aadhaar_image_data text not null default '',
  pan_image_data text not null default '',
  selfie_aadhaar_image_data text not null default '',
  status text not null default 'pending',
  message text not null default '',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists idx_users_host_discovery
  on public.users(role, host_status, host_audio_live, host_video_live);
create index if not exists idx_user_blocks_blocker on public.user_blocks(blocker_id);
create index if not exists idx_user_blocks_blocked on public.user_blocks(blocked_id);
create index if not exists wallet_ledger_user_created_idx
  on public.wallet_ledger(user_id, created_at desc);

alter table public.user_reports enable row level security;
alter table public.user_blocks enable row level security;
alter table public.wallet_ledger enable row level security;
alter table public.host_kyc_details enable row level security;

-- Serialize debits on the user row so concurrent calls cannot spend the same coins.
create or replace function public.record_wallet_debit(
  p_user_id uuid, p_kind text, p_title text, p_detail text,
  p_coins_delta integer, p_metadata jsonb
)
returns public.wallet_ledger
language plpgsql
security definer
set search_path = public
as $$
declare
  current_balance bigint;
  inserted public.wallet_ledger;
begin
  if p_kind not in ('audio_call', 'video_call', 'chat') or
     p_coins_delta >= 0 or p_coins_delta < -10000 then
    raise exception 'Invalid wallet debit';
  end if;
  perform 1 from public.users where id = p_user_id for update;
  if not found then raise exception 'User not found'; end if;
  select coalesce(sum(coins_delta), 0) into current_balance
    from public.wallet_ledger
    where user_id = p_user_id and status = 'completed';
  if current_balance + p_coins_delta < 0 then
    raise exception 'Insufficient coins';
  end if;
  insert into public.wallet_ledger
    (user_id, kind, title, detail, amount_text, coins_delta, metadata)
    values (p_user_id, p_kind, left(p_title, 120), left(p_detail, 240),
      p_coins_delta::text, p_coins_delta, p_metadata)
    returning * into inserted;
  return inserted;
end;
$$;
revoke all on function public.record_wallet_debit(uuid, text, text, text, integer, jsonb) from public, anon, authenticated;
grant execute on function public.record_wallet_debit(uuid, text, text, text, integer, jsonb) to service_role;

create or replace function public.wallet_balance(p_user_id uuid)
returns bigint
language sql
stable
security definer
set search_path = public
as $$
  select coalesce(sum(coins_delta), 0)::bigint
  from public.wallet_ledger
  where user_id = p_user_id and status = 'completed';
$$;
revoke all on function public.wallet_balance(uuid) from public, anon, authenticated;
grant execute on function public.wallet_balance(uuid) to service_role;
