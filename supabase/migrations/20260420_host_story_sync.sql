alter table public.users
    add column if not exists host_story_items jsonb;

alter table public.users
    alter column host_story_items set default '[]'::jsonb;

update public.users
set host_story_items = coalesce(host_story_items, '[]'::jsonb);
