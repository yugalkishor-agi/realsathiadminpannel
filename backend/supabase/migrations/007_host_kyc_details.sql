create table if not exists public.host_kyc_details (
    user_id uuid primary key references public.users(id) on delete cascade,
    full_name text not null default '',
    email_address text not null default '',
    whatsapp_number text not null default '',
    phone_number text not null default '',
    date_of_birth text not null default '',
    aadhaar_masked text not null default '',
    pan_masked text not null default '',
    bank_name text not null default '',
    bank_account_masked text not null default '',
    ifsc_code text not null default '',
    account_holder_name text not null default '',
    aadhaar_image_data text not null default '',
    pan_image_data text not null default '',
    selfie_aadhaar_image_data text not null default '',
    status text not null default 'pending',
    message text not null default '',
    created_at timestamptz not null default timezone('utc', now()),
    updated_at timestamptz not null default timezone('utc', now())
);

alter table public.host_kyc_details
    add column if not exists full_name text not null default '',
    add column if not exists email_address text not null default '',
    add column if not exists whatsapp_number text not null default '',
    add column if not exists phone_number text not null default '',
    add column if not exists date_of_birth text not null default '',
    add column if not exists aadhaar_masked text not null default '',
    add column if not exists pan_masked text not null default '',
    add column if not exists bank_name text not null default '',
    add column if not exists bank_account_masked text not null default '',
    add column if not exists ifsc_code text not null default '',
    add column if not exists account_holder_name text not null default '',
    add column if not exists aadhaar_image_data text not null default '',
    add column if not exists pan_image_data text not null default '',
    add column if not exists selfie_aadhaar_image_data text not null default '',
    add column if not exists status text not null default 'pending',
    add column if not exists message text not null default '',
    add column if not exists created_at timestamptz not null default timezone('utc', now()),
    add column if not exists updated_at timestamptz not null default timezone('utc', now());

create index if not exists host_kyc_details_status_idx
    on public.host_kyc_details (status);

drop trigger if exists trg_host_kyc_details_set_updated_at on public.host_kyc_details;
create trigger trg_host_kyc_details_set_updated_at
before update on public.host_kyc_details
for each row
execute function public.set_updated_at();

alter table public.host_kyc_details enable row level security;
