create or replace function public.record_chat_call_progress(
  p_client_id uuid, p_host_id uuid, p_duration_seconds integer
)
returns jsonb language plpgsql security definer set search_path = public
as $$
declare
  access_row public.chat_access;
  next_seconds integer;
  is_unlocked boolean := false;
  newly_unlocked boolean := false;
  next_expiry timestamptz;
begin
  if p_duration_seconds <= 0 or p_client_id = p_host_id then
    return jsonb_build_object('updated', false);
  end if;
  insert into public.chat_access (client_id, host_id)
  values (p_client_id, p_host_id)
  on conflict (client_id, host_id) do nothing;
  select * into access_row from public.chat_access as ca
  where ca.client_id = p_client_id and ca.host_id = p_host_id for update;
  next_seconds := access_row.qualified_call_seconds + least(p_duration_seconds, 86400);
  is_unlocked := next_seconds >= 600;
  newly_unlocked := is_unlocked and (access_row.expires_at is null or access_row.expires_at <= now());
  next_expiry := case when newly_unlocked then now() + interval '7 days' else access_row.expires_at end;
  update public.chat_access as ca
  set qualified_call_seconds = next_seconds,
      unlocked_at = case when newly_unlocked then now() else access_row.unlocked_at end,
      expires_at = next_expiry,
      billed_hours = case when newly_unlocked then 0 else access_row.billed_hours end,
      last_billed_at = case when newly_unlocked then null else access_row.last_billed_at end,
      updated_at = now()
  where ca.client_id = p_client_id and ca.host_id = p_host_id;
  if newly_unlocked then
    insert into public.direct_chat_messages (conversation_id, sender_id, recipient_id, body)
    select p_host_id::text, p_host_id, p_client_id, 'Hi'
    where not exists (
      select 1 from public.direct_chat_messages
      where conversation_id = p_host_id::text
        and sender_id = p_host_id and recipient_id = p_client_id
        and body = 'Hi' and created_at >= now() - interval '7 days'
    );
  end if;
  return jsonb_build_object('updated', true, 'qualifiedCallSeconds', next_seconds,
    'unlocked', is_unlocked, 'greetingSent', newly_unlocked, 'expiresAt', next_expiry::text);
end;
$$;

revoke all on function public.record_chat_call_progress(uuid, uuid, integer) from public, anon, authenticated;
grant execute on function public.record_chat_call_progress(uuid, uuid, integer) to service_role;
