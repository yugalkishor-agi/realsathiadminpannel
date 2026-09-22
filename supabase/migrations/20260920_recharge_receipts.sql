-- Keep recharge notifications on the server, including updates received while
-- the app is closed. Account verification IDs remain in users.public_id.
alter table public.recharge_orders drop constraint if exists recharge_orders_status_check;
alter table public.recharge_orders add constraint recharge_orders_status_check
  check (status in ('pending', 'processing', 'completed', 'failed'));

create table if not exists public.recharge_receipts (
  id uuid primary key default gen_random_uuid(),
  order_id text not null references public.recharge_orders(order_id) on delete cascade,
  user_id uuid not null references public.users(id) on delete cascade,
  status text not null check (status in ('pending', 'processing', 'completed', 'failed')),
  coins integer not null check (coins > 0),
  amount_rupees integer not null check (amount_rupees > 0),
  order_created_at timestamptz not null,
  created_at timestamptz not null default now()
);
create index if not exists recharge_receipts_user_created_idx
  on public.recharge_receipts(user_id, created_at desc);
alter table public.recharge_receipts enable row level security;
revoke all on public.recharge_receipts from anon, authenticated;
grant all on public.recharge_receipts to service_role;

create or replace function public.record_recharge_receipt()
returns trigger language plpgsql security definer set search_path = public as $$
begin
  if tg_op = 'UPDATE' then
    if new.status is not distinct from old.status then return new; end if;
  end if;
  insert into public.recharge_receipts
    (order_id, user_id, status, coins, amount_rupees, order_created_at)
  values (new.order_id, new.user_id, new.status, new.coins, new.amount_rupees, new.created_at);
  return new;
end;
$$;
revoke all on function public.record_recharge_receipt() from public, anon, authenticated;
drop trigger if exists recharge_receipt_on_status on public.recharge_orders;
create trigger recharge_receipt_on_status after insert or update of status
  on public.recharge_orders for each row execute function public.record_recharge_receipt();

insert into public.recharge_receipts
  (order_id, user_id, status, coins, amount_rupees, order_created_at, created_at)
select o.order_id, o.user_id, o.status, o.coins, o.amount_rupees, o.created_at,
  coalesce(o.completed_at, o.created_at)
from public.recharge_orders o
where not exists (select 1 from public.recharge_receipts r where r.order_id = o.order_id);

-- A failed payment attempt can be retried on the same Cashfree order. Only a
-- verified PAID order reaches this service-only function. Locking prevents
-- duplicate credit when callbacks and webhooks arrive together.
create or replace function public.complete_recharge_order(p_order_id text)
returns public.recharge_orders language plpgsql security definer set search_path = public as $$
declare recharge public.recharge_orders;
begin
  select * into recharge from public.recharge_orders where order_id = p_order_id for update;
  if not found then raise exception 'Recharge order not found'; end if;
  if recharge.status = 'completed' then return recharge; end if;

  insert into public.wallet_ledger
    (user_id, kind, title, detail, amount_text, coins_delta,
     rupees_delta, recharge_amount_rupees, status, metadata)
  values
    (recharge.user_id, 'payment', 'Recharge', recharge.coins::text || ' coins recharge',
     '+' || recharge.coins::text, recharge.coins, -recharge.amount_rupees,
     recharge.amount_rupees, 'completed',
     jsonb_build_object('orderId', p_order_id, 'provider', 'cashfree'));

  update public.recharge_orders set status = 'completed', completed_at = now()
    where order_id = p_order_id returning * into recharge;
  return recharge;
end;
$$;
revoke all on function public.complete_recharge_order(text) from public, anon, authenticated;
grant execute on function public.complete_recharge_order(text) to service_role;
