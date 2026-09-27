alter table public.users
  add column if not exists host_rank text not null default 'starter',
  add column if not exists host_total_call_minutes integer not null default 0,
  add column if not exists host_share_percent numeric(5,2) not null default 25.00;

alter table public.users alter column host_audio_rate set default 20;
alter table public.users alter column host_video_rate set default 60;
update public.users
set host_audio_rate = 20
where role = 'host' and coalesce(host_audio_rate, 0) in (6, 20, 33, 35);
update public.users
set host_video_rate = 60
where role = 'host' and coalesce(host_video_rate, 0) in (17, 60, 64, 65);

with call_minutes as (
  select wl.user_id,
    sum(greatest(1, ceil(coalesce(nullif(wl.metadata->>'durationSeconds', '')::numeric, 0) / 60.0)))::integer as total_minutes
  from public.wallet_ledger wl
  join public.users u on u.id = wl.user_id and u.role = 'host'
  where wl.status = 'completed'
    and wl.kind in ('audio_call', 'video_call')
    and coalesce(wl.metadata->>'callStatus', '') = 'completed'
  group by wl.user_id
)
update public.users u
set host_total_call_minutes = call_minutes.total_minutes
from call_minutes
where u.id = call_minutes.user_id;

update public.users
set host_rank = case
      when host_total_call_minutes >= 2200 then 'diamond'
      when host_total_call_minutes >= 1500 then 'platinum'
      when host_total_call_minutes >= 1000 then 'gold'
      when host_total_call_minutes >= 600 then 'silver'
      when host_total_call_minutes >= 300 then 'bronze'
      else 'starter'
    end,
    host_share_percent = case
      when host_total_call_minutes >= 2200 then 42.00
      when host_total_call_minutes >= 1500 then 38.00
      when host_total_call_minutes >= 1000 then 35.00
      when host_total_call_minutes >= 600 then 32.00
      when host_total_call_minutes >= 300 then 28.00
      else 25.00
    end
where role = 'host';

alter table public.wallet_ledger
  alter column rupees_delta type numeric(12,2) using rupees_delta::numeric;

create table if not exists public.system_earnings (
  id uuid primary key default gen_random_uuid(),
  call_id text not null unique,
  client_id uuid not null references public.users(id) on delete cascade,
  host_id uuid not null references public.users(id) on delete cascade,
  kind text not null check (kind in ('audio_call', 'video_call')),
  duration_seconds integer not null default 0,
  billed_minutes integer not null default 0,
  gross_charge_rupees numeric(12,2) not null default 0,
  host_earning_rupees numeric(12,2) not null default 0,
  system_earning_rupees numeric(12,2) not null default 0,
  host_rank text not null default 'starter',
  share_percent numeric(5,2) not null default 25.00,
  status text not null default 'completed',
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);

create index if not exists system_earnings_created_idx
  on public.system_earnings(created_at desc);
create index if not exists system_earnings_host_idx
  on public.system_earnings(host_id, created_at desc);

alter table public.system_earnings enable row level security;
revoke all on public.system_earnings from anon, authenticated;
grant select on public.system_earnings to authenticated;
drop policy if exists admin_read_system_earnings on public.system_earnings;
create policy admin_read_system_earnings on public.system_earnings
  for select to authenticated using (public.has_admin_permission('wallet'));

create or replace function public.record_call_billing(
  p_call_id text,
  p_client_id uuid,
  p_host_id uuid,
  p_kind text,
  p_status text,
  p_duration_seconds integer
)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
  existing_row public.wallet_ledger;
  host_row public.users;
  client_row public.users;
  v_billed_minutes integer := 0;
  v_next_minutes integer := 0;
  v_charge_coins integer := 0;
  v_gross numeric(12,2) := 0;
  v_share numeric(5,2) := 25.00;
  v_host_earning numeric(12,2) := 0;
  v_system_earning numeric(12,2) := 0;
  v_rank text := 'starter';
  v_balance bigint := 0;
begin
  if p_call_id is null or length(trim(p_call_id)) = 0
     or p_client_id = p_host_id
     or p_kind not in ('audio_call', 'video_call')
     or p_status not in ('completed', 'declined', 'canceled', 'missed')
     or p_duration_seconds < 0 then
    raise exception 'Invalid call billing request';
  end if;

  select * into host_row from public.users where id = p_host_id and role = 'host' for update;
  select * into client_row from public.users where id = p_client_id for update;
  if host_row.id is null or client_row.id is null then
    raise exception 'Call participants not found';
  end if;

  select * into existing_row
    from public.wallet_ledger
    where metadata->>'callId' = p_call_id
      and user_id in (p_client_id, p_host_id)
    order by created_at asc
    limit 1;
  if existing_row.id is not null then
    return jsonb_build_object('recorded', false, 'duplicate', true,
      'hostRank', host_row.host_rank,
      'hostTotalCallMinutes', host_row.host_total_call_minutes,
      'sharePercent', host_row.host_share_percent);
  end if;

  if p_status = 'completed' and p_duration_seconds > 0 then
    v_billed_minutes := greatest(1, ceil(p_duration_seconds / 60.0)::integer);
    v_charge_coins := v_billed_minutes * case when p_kind = 'video_call' then 60 else 20 end;
    v_next_minutes := host_row.host_total_call_minutes + v_billed_minutes;
    v_rank := case
      when v_next_minutes >= 2200 then 'diamond'
      when v_next_minutes >= 1500 then 'platinum'
      when v_next_minutes >= 1000 then 'gold'
      when v_next_minutes >= 600 then 'silver'
      when v_next_minutes >= 300 then 'bronze'
      else 'starter'
    end;
    v_share := case v_rank
      when 'diamond' then 42.00
      when 'platinum' then 38.00
      when 'gold' then 35.00
      when 'silver' then 32.00
      when 'bronze' then 28.00
      else 25.00
    end;
    v_gross := round(v_billed_minutes * case when p_kind = 'video_call' then
      case v_rank
        when 'diamond' then 6.99
        when 'platinum' then 6.32
        when 'gold' then 5.82
        when 'silver' then 5.32
        when 'bronze' then 4.66
        else 4.16
      end
    else
      case v_rank
        when 'diamond' then 2.33
        when 'platinum' then 2.11
        when 'gold' then 1.94
        when 'silver' then 1.77
        when 'bronze' then 1.55
        else 1.39
      end
    end, 2);
    v_host_earning := round(v_gross * v_share / 100.0, 2);
    v_system_earning := round(v_gross - v_host_earning, 2);

    select coalesce(sum(coins_delta), 0) into v_balance
      from public.wallet_ledger
      where user_id = p_client_id and status = 'completed';
    if v_balance < v_charge_coins then
      raise exception 'Insufficient coins';
    end if;

    update public.users
      set host_total_call_minutes = v_next_minutes,
          host_rank = v_rank,
          host_share_percent = v_share
      where id = p_host_id;
  else
    v_rank := coalesce(nullif(host_row.host_rank, ''), 'starter');
    v_share := coalesce(host_row.host_share_percent, 25.00);
  end if;

  insert into public.wallet_ledger
    (user_id, kind, title, detail, amount_text, coins_delta, rupees_delta, status, metadata)
  values
    (p_host_id, p_kind, coalesce(client_row.username, 'Caller'),
     initcap(replace(p_kind, '_', ' ')) || ' ' || p_status,
     case when v_host_earning > 0 then '+₹' || to_char(v_host_earning, 'FM999999990.00') else '₹0' end,
     0, v_host_earning, 'completed',
     jsonb_build_object('callId', p_call_id, 'callStatus', p_status,
       'counterpartyName', coalesce(client_row.username, 'Caller'), 'counterpartyId', p_client_id,
       'durationSeconds', p_duration_seconds, 'billedMinutes', v_billed_minutes,
       'grossChargeRupees', v_gross, 'hostEarningRupees', v_host_earning,
       'systemEarningRupees', v_system_earning, 'hostRank', v_rank,
       'sharePercent', v_share, 'chargeCoins', v_charge_coins));

  insert into public.wallet_ledger
    (user_id, kind, title, detail, amount_text, coins_delta, rupees_delta, status, metadata)
  values
    (p_client_id, p_kind, coalesce(host_row.username, 'Host'),
     initcap(replace(p_kind, '_', ' ')) || ' ' || p_status,
     case when v_charge_coins > 0 then '-' || v_charge_coins || ' coins' else '0 coins' end,
     -v_charge_coins, 0, 'completed',
     jsonb_build_object('callId', p_call_id, 'callStatus', p_status,
       'counterpartyName', coalesce(host_row.username, 'Host'), 'counterpartyId', p_host_id,
       'durationSeconds', p_duration_seconds, 'billedMinutes', v_billed_minutes,
       'grossChargeRupees', v_gross, 'chargeCoins', v_charge_coins,
       'hostRank', v_rank, 'sharePercent', v_share));

  if p_status = 'completed' then
    insert into public.system_earnings
      (call_id, client_id, host_id, kind, duration_seconds, billed_minutes,
       gross_charge_rupees, host_earning_rupees, system_earning_rupees,
       host_rank, share_percent, status, metadata)
    values
      (p_call_id, p_client_id, p_host_id, p_kind, p_duration_seconds, v_billed_minutes,
       v_gross, v_host_earning, v_system_earning, v_rank, v_share, 'completed',
       jsonb_build_object('chargeCoins', v_charge_coins));
  end if;

  return jsonb_build_object('recorded', true, 'hostRank', v_rank,
    'hostTotalCallMinutes', case when p_status = 'completed' then v_next_minutes else host_row.host_total_call_minutes end,
    'sharePercent', v_share, 'chargeCoins', v_charge_coins,
    'grossChargeRupees', v_gross, 'hostEarningRupees', v_host_earning,
    'systemEarningRupees', v_system_earning);
end;
$$;

revoke all on function public.record_call_billing(text, uuid, uuid, text, text, integer)
  from public, anon, authenticated;
grant execute on function public.record_call_billing(text, uuid, uuid, text, text, integer)
  to service_role;
