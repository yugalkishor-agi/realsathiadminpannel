create table if not exists public.chat_access (
  client_id uuid not null references public.users(id) on delete cascade,
  host_id uuid not null references public.users(id) on delete cascade,
  qualified_call_seconds integer not null default 0,
  unlocked_at timestamptz,
  expires_at timestamptz,
  billed_hours integer not null default 0,
  last_billed_at timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  primary key (client_id, host_id),
  check (client_id <> host_id),
  check (qualified_call_seconds >= 0),
  check (billed_hours >= 0)
);

create index if not exists chat_access_host_idx on public.chat_access(host_id, expires_at);
alter table public.chat_access enable row level security;
revoke all on public.chat_access from anon, authenticated;

alter table public.direct_chat_messages
  add column if not exists billing_window_started_at timestamptz;

create or replace function public.record_chat_call_progress(
  p_client_id uuid, p_host_id uuid, p_duration_seconds integer
)
returns jsonb language plpgsql security definer set search_path = public
as $$
declare
  access_row public.chat_access;
  next_seconds integer;
  is_unlocked boolean := false;
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
  update public.chat_access as ca
  set qualified_call_seconds = next_seconds,
      unlocked_at = case when is_unlocked and (access_row.expires_at is null or access_row.expires_at <= now()) then now() else access_row.unlocked_at end,
      expires_at = case when is_unlocked and (access_row.expires_at is null or access_row.expires_at <= now()) then now() + interval '7 days' else access_row.expires_at end,
      billed_hours = case when is_unlocked and (access_row.expires_at is null or access_row.expires_at <= now()) then 0 else access_row.billed_hours end,
      last_billed_at = case when is_unlocked and (access_row.expires_at is null or access_row.expires_at <= now()) then null else access_row.last_billed_at end,
      updated_at = now()
  where ca.client_id = p_client_id and ca.host_id = p_host_id;
  return jsonb_build_object('updated', true, 'qualifiedCallSeconds', next_seconds,
    'unlocked', is_unlocked, 'expiresAt', case when is_unlocked and (access_row.expires_at is null or access_row.expires_at <= now()) then (now() + interval '7 days')::text else access_row.expires_at::text end);
end;
$$;

revoke all on function public.record_chat_call_progress(uuid, uuid, integer) from public, anon, authenticated;
grant execute on function public.record_chat_call_progress(uuid, uuid, integer) to service_role;
