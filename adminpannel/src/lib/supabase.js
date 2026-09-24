import { createClient } from '@supabase/supabase-js'

const url = 'https://qefskbzqbduruznfshao.supabase.co'
const key = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFlZnNrYnpxYmR1cnV6bmZzaGFvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjkzNjk1NzIsImV4cCI6MjA4NDk0NTU3Mn0.yIZ89IW9HCFj5VhzOMViakoEUfRjb6if_CAa3DcyYuE'

export const supabase = url && key ? createClient(url, key) : null
export const configError = supabase ? '' : 'Supabase environment variables are missing.'
