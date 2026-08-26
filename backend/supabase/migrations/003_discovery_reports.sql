alter table public.users
add column if not exists nickname text,
add column if not exists username text,
add column if not exists gender text,
add column if not exists language text not null default 'All',
add column if not exists native_languages text[] not null default '{}',
add column if not exists topic_tags text[] not null default '{}',
add column if not exists interests text[] not null default '{}',
add column if not exists avatar_id integer not null default 1,
add column if not exists account_mode text not null default 'customer',
add column if not exists host_audio_live boolean not null default false,
add column if not exists host_video_live boolean not null default false,
add column if not exists host_audio_rate integer not null default 35,
add column if not exists host_video_rate integer not null default 65,
add column if not exists host_profile_photo_url text,
add column if not exists host_story_items jsonb not null default '[]'::jsonb;

alter table public.user_profiles
add column if not exists interests text[] not null default '{}',
add column if not exists topic_tags text[] not null default '{}',
add column if not exists native_languages text[] not null default '{}',
add column if not exists avatar_id integer not null default 1;

create table if not exists public.user_reports (
    id uuid primary key default gen_random_uuid(),
    reporter_id uuid not null references public.users(id) on delete cascade,
    reported_id uuid not null references public.users(id) on delete cascade,
    reason text not null default 'other',
    context text not null default 'profile',
    note text,
    status text not null default 'pending',
    created_at timestamptz not null default timezone('utc', now())
);

create table if not exists public.user_blocks (
    id uuid primary key default gen_random_uuid(),
    blocker_id uuid not null references public.users(id) on delete cascade,
    blocked_id uuid not null references public.users(id) on delete cascade,
    created_at timestamptz not null default timezone('utc', now()),
    unique(blocker_id, blocked_id)
);

create index if not exists idx_users_host_discovery
on public.users(role, host_status, host_audio_live, host_video_live);

create index if not exists idx_user_reports_reporter on public.user_reports(reporter_id);
create index if not exists idx_user_reports_reported on public.user_reports(reported_id);
create index if not exists idx_user_blocks_blocker on public.user_blocks(blocker_id);
create index if not exists idx_user_blocks_blocked on public.user_blocks(blocked_id);

alter table public.user_reports enable row level security;
alter table public.user_blocks enable row level security;
