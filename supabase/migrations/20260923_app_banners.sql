create table if not exists public.app_banners (
  id uuid primary key default gen_random_uuid(),
  placement text not null check (placement in ('home', 'wallet')),
  slot smallint not null check (slot between 1 and 3),
  image_path text not null,
  image_url text not null,
  version bigint not null default extract(epoch from now())::bigint,
  updated_at timestamptz not null default now(),
  updated_by uuid references auth.users(id),
  unique (placement, slot)
);

insert into storage.buckets (id, name, public)
values ('app-banners', 'app-banners', true)
on conflict (id) do update set public = true;

alter table public.app_banners enable row level security;
grant select, insert, update, delete on public.app_banners to authenticated;

drop policy if exists app_banners_read on public.app_banners;
create policy app_banners_read on public.app_banners for select to authenticated
  using (public.has_admin_permission('banners'));
drop policy if exists app_banners_write on public.app_banners;
create policy app_banners_write on public.app_banners for all to authenticated
  using (public.has_admin_permission('banners'))
  with check (public.has_admin_permission('banners'));

drop policy if exists app_banners_storage_write on storage.objects;
create policy app_banners_storage_write on storage.objects for all to authenticated
  using (bucket_id = 'app-banners' and public.has_admin_permission('banners'))
  with check (bucket_id = 'app-banners' and public.has_admin_permission('banners'));
