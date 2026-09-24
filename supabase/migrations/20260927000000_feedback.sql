create table if not exists public.feedback_messages (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.users(id) on delete cascade,
  role text not null default 'user' check (role in ('user', 'host')),
  category text not null default 'other',
  body text not null,
  status text not null default 'new' check (status in ('new', 'in_review', 'resolved')),
  admin_note text not null default '',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists feedback_messages_created_idx
  on public.feedback_messages(created_at desc);
alter table public.feedback_messages enable row level security;
revoke all on public.feedback_messages from anon, authenticated;
grant select, update on public.feedback_messages to authenticated;
drop policy if exists admin_read_feedback_messages on public.feedback_messages;
create policy admin_read_feedback_messages on public.feedback_messages
  for select to authenticated using (public.has_admin_permission('moderation'));
drop policy if exists admin_update_feedback_messages on public.feedback_messages;
create policy admin_update_feedback_messages on public.feedback_messages
  for update to authenticated
  using (public.has_admin_permission('moderation'))
  with check (public.has_admin_permission('moderation'));
