create table if not exists public.recharge_orders (
  order_id text primary key,
  user_id uuid not null references public.users(id) on delete cascade,
  coins integer not null check (coins > 0),
  amount_rupees integer not null check (amount_rupees > 0),
  status text not null default 'pending' check (status in ('pending', 'completed', 'failed')),
  created_at timestamptz not null default now(),
  completed_at timestamptz
);

create index if not exists recharge_orders_user_created_idx
  on public.recharge_orders(user_id, created_at desc);
alter table public.recharge_orders enable row level security;

-- Only the service role can call this after independently checking Cashfree's order.
-- The row lock makes callback and webhook retries credit exactly once.
create or replace function public.complete_recharge_order(p_order_id text)
returns public.recharge_orders
language plpgsql
security definer
set search_path = public
as $$
declare
  recharge public.recharge_orders;
begin
  select * into recharge from public.recharge_orders
    where order_id = p_order_id for update;
  if not found then raise exception 'Recharge order not found'; end if;
  if recharge.status = 'completed' then return recharge; end if;
  if recharge.status <> 'pending' then raise exception 'Recharge order is not pending'; end if;

  insert into public.wallet_ledger
    (user_id, kind, title, detail, amount_text, coins_delta,
     rupees_delta, recharge_amount_rupees, status, metadata)
  values
    (recharge.user_id, 'payment', 'Recharge',
     recharge.coins::text || ' coins recharge', '+' || recharge.coins::text,
     recharge.coins, -recharge.amount_rupees, recharge.amount_rupees,
     'completed', jsonb_build_object('orderId', p_order_id, 'provider', 'cashfree'));

  update public.recharge_orders
    set status = 'completed', completed_at = now()
    where order_id = p_order_id returning * into recharge;
  return recharge;
end;
$$;

revoke all on function public.complete_recharge_order(text) from public, anon, authenticated;
grant execute on function public.complete_recharge_order(text) to service_role;
