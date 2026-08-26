create extension if not exists pgcrypto;

alter table public.users
    add column if not exists avatar_id integer,
    add column if not exists interests text[],
    add column if not exists account_mode text,
    add column if not exists host_status text,
    add column if not exists host_audio_live boolean,
    add column if not exists host_video_live boolean,
    add column if not exists host_audio_rate integer,
    add column if not exists host_video_rate integer,
    add column if not exists host_profile_photo_url text,
    add column if not exists community_name text,
    add column if not exists community_city text,
    add column if not exists community_about text,
    add column if not exists community_experience text;

alter table public.users alter column username_change_count set default 0;
alter table public.users alter column is_online set default false;
alter table public.users alter column wallet_locked set default false;
alter table public.users alter column avatar_id set default 1;
alter table public.users alter column interests set default '{}'::text[];
alter table public.users alter column account_mode set default 'customer';
alter table public.users alter column host_status set default 'not_applicable';
alter table public.users alter column host_audio_live set default false;
alter table public.users alter column host_video_live set default false;
alter table public.users alter column host_audio_rate set default 35;
alter table public.users alter column host_video_rate set default 65;
alter table public.users alter column host_profile_photo_url set default '';
alter table public.users alter column account_status set default 'active';
alter table public.users alter column role set default 'user';
alter table public.users alter column language set default 'All';

update public.users
set username = coalesce(nullif(trim(username), ''), 'frndzzz_' || right(coalesce(phone, '0000'), 4)),
    gender = coalesce(gender, ''),
    language = coalesce(nullif(trim(language), ''), 'All'),
    role = coalesce(nullif(trim(role), ''), 'user'),
    username_change_count = coalesce(username_change_count, 0),
    last_active = coalesce(last_active, timezone('utc', now())),
    is_online = coalesce(is_online, false),
    wallet_locked = coalesce(wallet_locked, false),
    account_status = coalesce(nullif(trim(account_status), ''), 'active'),
    avatar_id = coalesce(avatar_id, 1),
    interests = coalesce(interests, '{}'::text[]),
    account_mode = coalesce(nullif(trim(account_mode), ''), 'customer'),
    host_status = coalesce(
        nullif(trim(host_status), ''),
        case when coalesce(nullif(trim(role), ''), 'user') = 'host' then 'approved' else 'not_applicable' end
    ),
    host_audio_live = coalesce(host_audio_live, false),
    host_video_live = coalesce(host_video_live, false),
    host_audio_rate = coalesce(host_audio_rate, 35),
    host_video_rate = coalesce(host_video_rate, 65),
    host_profile_photo_url = coalesce(host_profile_photo_url, ''),
    community_name = coalesce(community_name, ''),
    community_city = coalesce(community_city, ''),
    community_about = coalesce(community_about, ''),
    community_experience = coalesce(community_experience, '');

create unique index if not exists idx_wallet_user_id on public.wallet(user_id);

insert into public.wallet (id, user_id, balance, total_spent, total_purchased, created_at)
select
    gen_random_uuid(),
    u.id,
    0,
    0,
    0,
    timezone('utc', now())
from public.users u
left join public.wallet w on w.user_id = u.id
where w.user_id is null;
