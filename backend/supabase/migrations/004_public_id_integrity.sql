alter table public.users
add column if not exists public_id text;

create unique index if not exists idx_users_public_id_unique
on public.users(public_id)
where public_id ~ '^[0-9]{8}$';

do $$
begin
    if not exists (
        select 1
        from pg_constraint
        where conname = 'users_public_id_8_digit_chk'
    ) then
        alter table public.users
        add constraint users_public_id_8_digit_chk
        check (public_id is null or public_id ~ '^[0-9]{8}$')
        not valid;
    end if;
end $$;

create or replace function public.prevent_valid_public_id_change()
returns trigger
language plpgsql
as $$
begin
    if old.public_id is not null
        and old.public_id ~ '^[0-9]{8}$'
        and new.public_id is distinct from old.public_id then
        raise exception 'public_id cannot be changed once generated';
    end if;

    return new;
end;
$$;

drop trigger if exists users_prevent_public_id_change on public.users;

create trigger users_prevent_public_id_change
before update of public_id on public.users
for each row
execute function public.prevent_valid_public_id_change();
