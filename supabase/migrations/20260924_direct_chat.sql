create table if not exists public.direct_chat_messages (
  id uuid primary key default gen_random_uuid(),
  conversation_id text not null,
  sender_id uuid not null references public.users(id) on delete cascade,
  recipient_id uuid references public.users(id) on delete set null,
  body text not null,
  created_at timestamptz not null default now(),
  read_at timestamptz
);

create index if not exists direct_chat_conversation_idx on public.direct_chat_messages(conversation_id, created_at);
alter table public.direct_chat_messages enable row level security;
revoke all on public.direct_chat_messages from anon, authenticated;
