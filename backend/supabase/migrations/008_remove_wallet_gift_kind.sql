update public.wallet_ledger
set kind = 'adjustment'
where kind = 'gift';

alter table if exists public.wallet_ledger
    drop constraint if exists wallet_ledger_kind_check;

alter table if exists public.wallet_ledger
    add constraint wallet_ledger_kind_check
    check (kind in ('audio_call', 'video_call', 'chat', 'payment', 'adjustment'));
