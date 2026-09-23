import { useCallback, useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'

export function useAdminData(table, columns = '*', limit = 12) {
  const [rows, setRows] = useState([]); const [loading, setLoading] = useState(true); const [error, setError] = useState('')
  const load = useCallback(async () => {
    if (!supabase || !table) return
    setLoading(true); setError('')
    const result = await supabase.from(table).select(columns).order('created_at', { ascending: false }).limit(limit)
    if (result.error) setError(result.error.message); else setRows(result.data || [])
    setLoading(false)
  }, [table, columns, limit])
  useEffect(() => { load() }, [load])
  return { rows, loading, error, reload: load }
}
