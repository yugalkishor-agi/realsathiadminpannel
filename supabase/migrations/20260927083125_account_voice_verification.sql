create table if not exists public.account_voice_verifications (
  user_id uuid primary key references public.users(id) on delete cascade,
  account_name text not null default '',
  phone_number text not null default '',
  prompt text not null,
  audio_path text not null,
  status text not null default 'pending' check (status in ('pending', 'approved', 'rejected')),
  review_message text not null default '',
  metadata jsonb not null default '{}'::jsonb,
  created_at timestamptz not null default now(),
  reviewed_at timestamptz,
  reviewed_by uuid
);

alter table public.users add column if not exists profile_photo_url text not null default '';
alter table public.users add column if not exists age integer;

alter table public.account_voice_verifications enable row level security;
grant select on public.account_voice_verifications to authenticated;

create policy account_voice_verification_admin_read
  on public.account_voice_verifications for select to authenticated
  using (public.has_admin_permission('kyc'));

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('account-verification-audio', 'account-verification-audio', false, 10485760,
        array['audio/mp4', 'audio/m4a', 'audio/aac', 'audio/3gpp', 'audio/amr'])
on conflict (id) do update set public = false, file_size_limit = 10485760,
  allowed_mime_types = excluded.allowed_mime_types;

create policy account_verification_audio_admin_read
  on storage.objects for select to authenticated
  using (bucket_id = 'account-verification-audio' and public.has_admin_permission('kyc'));

create or replace function public.review_account_voice_verification(
  p_user_id uuid, p_status text, p_review_message text default ''
) returns public.account_voice_verifications
language plpgsql security definer set search_path = public
as $$
declare reviewed public.account_voice_verifications;
begin
  if not public.has_admin_permission('kyc') then raise exception 'Not authorized'; end if;
  if p_status not in ('approved', 'rejected') then raise exception 'Invalid review status'; end if;
  update public.account_voice_verifications
    set status = p_status, review_message = left(trim(p_review_message), 500),
        reviewed_at = now(), reviewed_by = auth.uid()
    where user_id = p_user_id and status = 'pending' returning * into reviewed;
  if not found then raise exception 'Verification record not found'; end if;
  perform public.admin_record_audit(
    'account_voice_verification_' || p_status, 'account_voice_verification', p_user_id::text,
    jsonb_build_object('review_message', left(trim(p_review_message), 500))
  );
  return reviewed;
end;
$$;

revoke all on function public.review_account_voice_verification(uuid, text, text) from public, anon;
grant execute on function public.review_account_voice_verification(uuid, text, text) to authenticated;
