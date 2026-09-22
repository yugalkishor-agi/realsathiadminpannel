alter table public.users add column if not exists device_id text;
alter table public.users add column if not exists device_brand text;
alter table public.users add column if not exists signup_country text;
alter table public.users add column if not exists app_brand text not null default 'RealSaathi';
alter table public.users add column if not exists session_version integer not null default 0;

create index if not exists idx_users_device_id on public.users(device_id);
