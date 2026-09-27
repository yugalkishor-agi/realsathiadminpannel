create or replace function public.admin_credit_wallet(
  p_user_id uuid,
  p_coins integer,
  p_reason text default ''
)
returns public.wallet_ledger
language plpgsql
security definer
set search_path = public
as $$
declare
  target public.users;
  entry public.wallet_ledger;
  clean_reason text := left(trim(coalesce(p_reason, '')), 240);
begin
  if not public.has_admin_permission('wallet') then
    raise exception 'Not allowed';
  end if;
  if p_coins is null or p_coins <= 0 or p_coins > 1000000 then
    raise exception 'Coins must be between 1 and 1000000';
  end if;
  select * into target from public.users where id = p_user_id for update;
  if target.id is null then raise exception 'User not found'; end if;
  if clean_reason = '' then clean_reason := 'Manual admin recharge'; end if;
  insert into public.wallet_ledger
    (user_id, kind, title, detail, amount_text, coins_delta,
     rupees_delta, recharge_amount_rupees, status, metadata)
  values
    (p_user_id, 'payment', 'Admin recharge', clean_reason,
     '+' || p_coins || ' coins', p_coins, 0, 0, 'completed',
     jsonb_build_object('source', 'admin_panel', 'adminId', auth.uid(),
       'reason', clean_reason))
  returning * into entry;
  perform public.admin_record_audit('wallet_credited', 'user', p_user_id::text,
    jsonb_build_object('coins', p_coins, 'reason', clean_reason));
  return entry;
end;
$$;

revoke all on function public.admin_credit_wallet(uuid, integer, text)
  from public, anon;
grant execute on function public.admin_credit_wallet(uuid, integer, text)
  to authenticated;
