alter table public.users
add column if not exists public_id text;

create unique index if not exists idx_users_public_id_unique
on public.users(public_id)
where public_id ~ '^[0-9]{8}$';
