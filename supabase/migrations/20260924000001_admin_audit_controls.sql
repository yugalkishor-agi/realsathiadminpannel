-- Admin-only controls and audit trail for the RealSaathi control room.
create table if not exists public.admin_audit_logs (
  id uuid primary key default gen_random_uuid(),
  actor_id uuid not null references auth.users(id) on delete restrict,
  action text not null,
  entity_type text not null,
  entity_id text not null default '',
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now()
);
create index if not exists admin_audit_logs_created_idx on public.admin_audit_logs(created_at desc);
create index if not exists admin_audit_logs_entity_idx on public.admin_audit_logs(entity_type, entity_id);
alter table public.admin_audit_logs enable row level security;
grant select on public.admin_audit_logs to authenticated;
drop policy if exists admin_audit_logs_read on public.admin_audit_logs;
create policy admin_audit_logs_read on public.admin_audit_logs for select to authenticated
  using (public.has_admin_permission('audit') or public.is_super_admin());

create or replace function public.admin_record_audit(
  p_action text, p_entity_type text, p_entity_id text default '', p_metadata jsonb default '{}'
) returns public.admin_audit_logs language plpgsql security definer set search_path = public as $$
declare item public.admin_audit_logs;
begin
  if not public.is_admin_actor() then raise exception 'Not allowed'; end if;
  insert into public.admin_audit_logs(actor_id, action, entity_type, entity_id, metadata)
  values (auth.uid(), left(trim(p_action), 80), left(trim(p_entity_type), 60), left(trim(p_entity_id), 160), coalesce(p_metadata, '{}'::jsonb))
  returning * into item;
  return item;
end $$;
revoke all on function public.admin_record_audit(text, text, text, jsonb) from public, anon;
grant execute on function public.admin_record_audit(text, text, text, jsonb) to authenticated;

create or replace function public.admin_set_user_status(p_user_id uuid, p_status text, p_reason text default '')
returns boolean language plpgsql security definer set search_path = public as $$
begin
  if not public.has_admin_permission('users') then raise exception 'Not allowed'; end if;
  if p_status not in ('active', 'blocked', 'banned', 'suspended') then raise exception 'Invalid status'; end if;
  update public.users set account_status = p_status, ban_reason = nullif(left(trim(p_reason), 240), ''),
    banned_at = case when p_status in ('banned', 'blocked', 'suspended') then now() else null end,
    banned_by = case when p_status in ('banned', 'blocked', 'suspended') then auth.uid() else null end,
    session_version = coalesce(session_version, 0) + 1 where id = p_user_id;
  if not found then return false; end if;
  perform public.admin_record_audit('user_status_changed', 'user', p_user_id::text,
    jsonb_build_object('status', p_status, 'reason', left(coalesce(p_reason, ''), 240)));
  return true;
end $$;
revoke all on function public.admin_set_user_status(uuid, text, text) from public, anon;
grant execute on function public.admin_set_user_status(uuid, text, text) to authenticated;

create or replace function public.admin_remove_host_story(p_user_id uuid, p_story_id text)
returns boolean language plpgsql security definer set search_path = public as $$
declare changed jsonb;
begin
  if not public.has_admin_permission('moderation') then raise exception 'Not allowed'; end if;
  select coalesce(jsonb_agg(item), '[]'::jsonb) into changed from jsonb_array_elements(
    coalesce((select host_story_items from public.users where id = p_user_id and role = 'host'), '[]'::jsonb)
  ) item where coalesce(item->>'id', item->>'storyId', item->>'mediaUri', '') <> p_story_id;
  if changed is null then return false; end if;
  update public.users set host_story_items = changed where id = p_user_id and role = 'host';
  if not found then return false; end if;
  perform public.admin_record_audit('host_story_removed', 'user', p_user_id::text, jsonb_build_object('storyId', p_story_id));
  return true;
end $$;
revoke all on function public.admin_remove_host_story(uuid, text) from public, anon;
grant execute on function public.admin_remove_host_story(uuid, text) to authenticated;
