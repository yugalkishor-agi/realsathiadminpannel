create or replace function public.send_direct_chat_message(
  p_conversation_id text, p_sender_id uuid, p_recipient_id uuid, p_body text
)
returns jsonb language plpgsql security definer set search_path = public
as $$
declare
  sender_role text;
  recipient_role text;
  v_client_id uuid;
  v_host_id uuid;
  access_row public.chat_access;
  current_balance bigint;
  next_hours integer;
  charge_applied boolean := false;
  host_rewarded boolean := false;
  inserted_message public.direct_chat_messages;
begin
  select role into sender_role from public.users where id = p_sender_id;
  select role into recipient_role from public.users where id = p_recipient_id;
  if sender_role is null or recipient_role is null then raise exception 'Chat participants not found'; end if;
  if sender_role = 'host' and recipient_role <> 'host' then
    v_client_id := p_recipient_id; v_host_id := p_sender_id;
  elsif sender_role <> 'host' and recipient_role = 'host' then
    v_client_id := p_sender_id; v_host_id := p_recipient_id;
  else raise exception 'Chat requires one client and one host'; end if;

  if sender_role <> 'host' then
    select * into access_row from public.chat_access as ca
    where ca.client_id = v_client_id and ca.host_id = v_host_id for update;
    if access_row.client_id is null or access_row.expires_at is null or access_row.expires_at <= now() then
      raise exception 'Chat is locked. Complete 10 minutes of calls with this host.';
    end if;
    if access_row.last_billed_at is null or access_row.last_billed_at + interval '1 hour' <= now() then
      perform 1 from public.users where id = v_client_id for update;
      select coalesce(sum(coins_delta), 0) into current_balance from public.wallet_ledger
      where user_id = v_client_id and status = 'completed';
      if current_balance < 2 then raise exception 'Insufficient coins for chat.'; end if;
      insert into public.wallet_ledger
        (user_id, kind, title, detail, amount_text, coins_delta, rupees_delta, status, metadata)
      values (v_client_id, 'chat', 'Host chat', '1 hour chat charge', '-2', -2, 0, 'completed',
        jsonb_build_object('hostId', v_host_id, 'chargeCoins', 2, 'billingWindowHours', 1));
      next_hours := access_row.billed_hours + 1;
      charge_applied := true;
      if mod(next_hours, 3) = 0 then
        insert into public.wallet_ledger
          (user_id, kind, title, detail, amount_text, coins_delta, rupees_delta, status, metadata)
        values (v_host_id, 'chat', 'Chat earning', '3 hours chat earning', '+₹2', 0, 2, 'completed',
          jsonb_build_object('clientId', v_client_id, 'earningRupees', 2, 'billedHours', next_hours));
        host_rewarded := true;
      end if;
      update public.chat_access as ca set billed_hours = next_hours, last_billed_at = now(), updated_at = now()
      where ca.client_id = v_client_id and ca.host_id = v_host_id;
    end if;
  end if;
  insert into public.direct_chat_messages
    (conversation_id, sender_id, recipient_id, body, billing_window_started_at)
  values (left(trim(p_conversation_id), 160), p_sender_id, p_recipient_id, left(trim(p_body), 2000),
    case when sender_role = 'host' then null else coalesce(access_row.last_billed_at, now()) end)
  returning * into inserted_message;
  return jsonb_build_object('message', jsonb_build_object('id', inserted_message.id,
    'sender_id', inserted_message.sender_id, 'recipient_id', inserted_message.recipient_id,
    'body', inserted_message.body, 'created_at', inserted_message.created_at,
    'read_at', inserted_message.read_at), 'chargeApplied', charge_applied,
    'hostRewarded', host_rewarded);
end;
$$;

revoke all on function public.send_direct_chat_message(text, uuid, uuid, text) from public, anon, authenticated;
grant execute on function public.send_direct_chat_message(text, uuid, uuid, text) to service_role;
