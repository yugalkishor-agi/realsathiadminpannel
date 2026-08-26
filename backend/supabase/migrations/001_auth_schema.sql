create extension if not exists pgcrypto;

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
    new.updated_at = timezone('utc', now());
    return new;
end;
$$;

create table if not exists public.users (
    id uuid primary key default gen_random_uuid(),
    phone_number text not null unique,
    role text not null default 'user' check (role in ('user', 'host', 'admin')),
    is_host boolean not null default false,
    host_status text not null default 'not_applicable' check (
        host_status in ('not_applicable', 'pending', 'approved', 'rejected')
    ),
    is_active boolean not null default true,
    last_login_at timestamptz,
    created_at timestamptz not null default timezone('utc', now()),
    updated_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.user_profiles (
    user_id uuid primary key references public.users(id) on delete cascade,
    display_name text not null,
    avatar_url text,
    gender text,
    preferred_language text not null default 'All',
    bio text,
    created_at timestamptz not null default timezone('utc', now()),
    updated_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.otp_requests (
    id uuid primary key default gen_random_uuid(),
    request_id uuid not null unique,
    phone_number text not null,
    otp_hash text not null,
    provider text not null default 'mock',
    provider_reference text,
    status text not null default 'pending' check (status in ('pending', 'verified', 'failed')),
    attempts integer not null default 0 check (attempts >= 0),
    expires_at timestamptz not null,
    verified_at timestamptz,
    created_at timestamptz not null default timezone('utc', now())
);

create index if not exists idx_users_phone_number on public.users(phone_number);
create index if not exists idx_otp_requests_request_id on public.otp_requests(request_id);
create index if not exists idx_otp_requests_phone_number on public.otp_requests(phone_number);

drop trigger if exists trg_users_set_updated_at on public.users;
create trigger trg_users_set_updated_at
before update on public.users
for each row
execute function public.set_updated_at();

drop trigger if exists trg_user_profiles_set_updated_at on public.user_profiles;
create trigger trg_user_profiles_set_updated_at
before update on public.user_profiles
for each row
execute function public.set_updated_at();

alter table public.users enable row level security;
alter table public.user_profiles enable row level security;
alter table public.otp_requests enable row level security;
