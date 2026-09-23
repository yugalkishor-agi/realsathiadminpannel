-- Browser admin access uses Auth JWT metadata and RLS. Never expose service_role.
create table if not exists public.admin_members (
  id uuid primary key default gen_random_uuid(), user_id uuid not null unique,
  email text not null, role text not null default 'co_worker' check (role = 'co_worker'),
  permissions jsonb not null default '["users"]'::jsonb,
  status text not null default 'active' check (status in ('active','suspended')),
  created_at timestamptz not null default now()
);
alter table public.users add column if not exists ban_reason text;
alter table public.users add column if not exists banned_at timestamptz;
alter table public.users add column if not exists banned_by uuid;
alter table public.host_kyc_details add column if not exists reviewed_at timestamptz;
alter table public.host_kyc_details add column if not exists reviewed_by uuid;
alter table public.host_kyc_details add column if not exists review_message text not null default '';
alter table public.admin_members enable row level security;

create or replace function public.is_super_admin()
returns boolean language sql stable
as $$ select coalesce((auth.jwt() -> 'app_metadata' ->> 'role') = 'super_admin', false) $$;

create or replace function public.is_admin_actor()
returns boolean language sql stable
as $$ select public.is_super_admin() or exists (
  select 1 from public.admin_members m where m.user_id = auth.uid() and m.status = 'active'
) $$;

create or replace function public.has_admin_permission(p_permission text)
returns boolean language sql stable
as $$ select public.is_super_admin() or exists (
  select 1 from public.admin_members m where m.user_id = auth.uid()
    and m.status = 'active' and m.permissions ? p_permission
) $$;

drop policy if exists admin_members_self_read on public.admin_members;
create policy admin_members_self_read on public.admin_members for select to authenticated
  using (user_id = auth.uid() or public.is_super_admin());
drop policy if exists admin_members_super_write on public.admin_members;
create policy admin_members_super_write on public.admin_members for all to authenticated
  using (public.is_super_admin()) with check (public.is_super_admin());

do $$ declare item record; begin
  for item in select * from (values
    ('users','users'), ('user_profiles','users'), ('user_reports','moderation'),
    ('user_blocks','moderation'), ('wallet_ledger','wallet'),
    ('host_kyc_details','kyc'), ('recharge_orders','wallet'),
    ('recharge_receipts','wallet')
  ) as tables(table_name, permission) loop
    if to_regclass('public.' || item.table_name) is not null then
      execute format('grant select, update on public.%I to authenticated', item.table_name);
      execute format('drop policy if exists admin_read_%I on public.%I', item.table_name, item.table_name);
      execute format('create policy admin_read_%I on public.%I for select to authenticated using (public.has_admin_permission(%L))', item.table_name, item.table_name, item.permission);
    end if;
  end loop;
end $$;

drop policy if exists admin_update_users on public.users;
create policy admin_update_users on public.users for update to authenticated
  using (public.has_admin_permission('users')) with check (public.has_admin_permission('users'));
drop policy if exists admin_update_kyc on public.host_kyc_details;
create policy admin_update_kyc on public.host_kyc_details for update to authenticated
  using (public.has_admin_permission('kyc')) with check (public.has_admin_permission('kyc'));

do $$ begin
  if to_regclass('public.otp_requests') is not null then
    execute 'revoke all on public.otp_requests from anon, authenticated';
  end if;
end $$;
