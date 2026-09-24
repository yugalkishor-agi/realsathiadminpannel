create table if not exists public.support_threads (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.users(id) on delete cascade,
  status text not null default 'open' check (status in ('open', 'in_progress', 'resolved', 'closed')),
  subject text not null default 'RealSaathi support request',
  last_message_at timestamptz not null default now(),
  assigned_to uuid references auth.users(id) on delete set null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.support_messages (
  id uuid primary key default gen_random_uuid(),
  thread_id uuid not null references public.support_threads(id) on delete cascade,
  sender_type text not null check (sender_type in ('user', 'assistant', 'admin')),
  sender_id uuid,
  body text not null,
  created_at timestamptz not null default now()
);

create index if not exists support_threads_user_idx on public.support_threads(user_id, updated_at desc);
create index if not exists support_threads_status_idx on public.support_threads(status, last_message_at desc);
create index if not exists support_messages_thread_idx on public.support_messages(thread_id, created_at);

alter table public.support_threads enable row level security;
alter table public.support_messages enable row level security;
grant select, update on public.support_threads to authenticated;
grant select, insert on public.support_messages to authenticated;
grant select on public.users to authenticated;

drop policy if exists admin_support_users_read on public.users;
create policy admin_support_users_read on public.users for select to authenticated
  using (public.has_admin_permission('support'));

drop policy if exists admin_support_threads_read on public.support_threads;
create policy admin_support_threads_read on public.support_threads for select to authenticated
  using (public.has_admin_permission('support'));
drop policy if exists admin_support_threads_update on public.support_threads;
create policy admin_support_threads_update on public.support_threads for update to authenticated
  using (public.has_admin_permission('support')) with check (public.has_admin_permission('support'));
drop policy if exists admin_support_messages_read on public.support_messages;
create policy admin_support_messages_read on public.support_messages for select to authenticated
  using (public.has_admin_permission('support'));
drop policy if exists admin_support_messages_insert on public.support_messages;
create policy admin_support_messages_insert on public.support_messages for insert to authenticated
  with check (public.has_admin_permission('support') and sender_type = 'admin' and sender_id = auth.uid());
