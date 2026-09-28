import { useCallback, useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'
import { adminUnbanDevice, recordAudit } from '../lib/adminActions'

export default function useDeviceBans() {
  const [rows, setRows] = useState([]); const [loading, setLoading] = useState(true); const [error, setError] = useState('')
  const load = useCallback(async () => {
    setLoading(true)
    const result = await supabase.from('banned_devices').select('*').order('created_at', { ascending: false }).limit(200)
    if (result.error) { setError(result.error.message); setLoading(false); return }
    const ids = [...new Set((result.data || []).map(row => row.user_id).filter(Boolean))]
    const people = ids.length ? await supabase.from('users').select('id,public_id,username,role').in('id', ids) : { data: [], error: null }
    if (people.error) { setError(people.error.message); setLoading(false); return }
    const byId = Object.fromEntries((people.data || []).map(person => [person.id, person]))
    setRows((result.data || []).map(row => ({ ...row, user: byId[row.user_id] }))); setError(''); setLoading(false)
  }, [])
  useEffect(() => { load() }, [load])
  const unban = async row => { const result = await adminUnbanDevice(row.device_id); if (result.error) { setError(result.error.message); return false } await recordAudit('device_unbanned', 'device', row.device_id, { userId: row.user_id }); await load(); return true }
  return { rows, loading, error, load, unban }
}
