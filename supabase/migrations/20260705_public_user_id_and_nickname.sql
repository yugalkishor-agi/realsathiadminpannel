create sequence if not exists public.users_public_id_seq
  start with 10000001
  increment by 1;

alter table public.users
  add column if not exists public_id bigint;

update public.users
set public_id = nextval('public.users_public_id_seq')
where public_id is null;

alter table public.users
  alter column public_id set default nextval('public.users_public_id_seq');

alter table public.users
  alter column public_id set not null;

create unique index if not exists users_public_id_key
  on public.users (public_id);

alter sequence public.users_public_id_seq
  owned by public.users.public_id;
