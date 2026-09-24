alter table public.user_reports
  add column if not exists admin_note text not null default '',
  add column if not exists resolved_at timestamptz,
  add column if not exists resolved_by uuid references public.users(id);

create table if not exists public.banned_devices (
  id uuid primary key default gen_random_uuid(),
  device_id text not null unique,
  device_brand text not null default '',
  user_id uuid references public.users(id) on delete set null,
  report_id uuid references public.user_reports(id) on delete set null,
  reason text not null default 'Policy violation',
  banned_by uuid references public.users(id) on delete set null,
  created_at timestamptz not null default now()
);

create index if not exists banned_devices_device_id_idx on public.banned_devices(device_id);
alter table public.banned_devices enable row level security;
grant select, insert, update on public.banned_devices to authenticated;

drop policy if exists admin_read_banned_devices on public.banned_devices;
create policy admin_read_banned_devices on public.banned_devices
  for select to authenticated using (public.has_admin_permission('moderation'));
drop policy if exists admin_write_banned_devices on public.banned_devices;
create policy admin_write_banned_devices on public.banned_devices
  for all to authenticated using (public.has_admin_permission('moderation'))
  with check (public.has_admin_permission('moderation'));

drop policy if exists admin_update_user_reports on public.user_reports;
create policy admin_update_user_reports on public.user_reports
  for update to authenticated using (public.has_admin_permission('moderation'))
  with check (public.has_admin_permission('moderation'));

drop policy if exists admin_read_users_for_moderation on public.users;
create policy admin_read_users_for_moderation on public.users
  for select to authenticated using (public.has_admin_permission('moderation'));

create or replace function public.admin_ban_user(p_user_id uuid, p_reason text default 'Policy violation')
returns boolean language plpgsql security definer set search_path = public
as $$ begin
  if not public.has_admin_permission('moderation') then raise exception 'Not allowed'; end if;
  update public.users set account_status = 'banned', ban_reason = left(p_reason, 240),
    banned_at = now(), banned_by = auth.uid(), session_version = coalesce(session_version, 0) + 1
    where id = p_user_id;
  return found;
end $$;

create or replace function public.admin_ban_device(p_device_id text, p_user_id uuid default null, p_report_id uuid default null, p_reason text default 'Policy violation', p_brand text default '')
returns boolean language plpgsql security definer set search_path = public
as $$ begin
  if not public.has_admin_permission('moderation') then raise exception 'Not allowed'; end if;
  if nullif(trim(p_device_id), '') is null then raise exception 'Device id missing'; end if;
  insert into public.banned_devices(device_id, device_brand, user_id, report_id, reason, banned_by)
    values (trim(p_device_id), left(coalesce(p_brand, ''), 120), p_user_id, p_report_id, left(p_reason, 240), auth.uid())
    on conflict (device_id) do update set reason = excluded.reason, banned_by = excluded.banned_by;
  if p_user_id is not null then
    update public.users set account_status = 'banned', ban_reason = left(p_reason, 240),
      banned_at = now(), banned_by = auth.uid(), session_version = coalesce(session_version, 0) + 1
      where id = p_user_id;
  end if;
  return true;
end $$;

revoke all on function public.admin_ban_user(uuid, text) from public, anon;
revoke all on function public.admin_ban_device(text, uuid, uuid, text, text) from public, anon;
grant execute on function public.admin_ban_user(uuid, text) to authenticated;
grant execute on function public.admin_ban_device(text, uuid, uuid, text, text) to authenticated;
