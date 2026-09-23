import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'

export function useUserFinancials(userId, enabled) {
  const [data, setData] = useState({ rows: [], balance: 0, lifetimeSpend: 0, monthlySpend: 0, totalEarnings: 0, monthlyEarnings: 0 }); const [loading, setLoading] = useState(false); const [error, setError] = useState('')
  useEffect(() => {
    if (!userId || !enabled) return undefined
    let cancelled = false
    const load = async () => {
      setLoading(true); setError('')
      const result = await supabase.from('wallet_ledger').select('id,kind,title,detail,amount_text,coins_delta,rupees_delta,status,metadata,created_at').eq('user_id', userId).order('created_at', { ascending: false }).limit(500)
      if (cancelled) return
      if (result.error) { setError(result.error.message); setLoading(false); return }
      const start = new Date(); start.setDate(1); start.setHours(0, 0, 0, 0); const rows = result.data || []; const completed = rows.filter(row => row.status === 'completed')
      setData({ rows, balance: completed.reduce((sum, row) => sum + Number(row.coins_delta || 0), 0), lifetimeSpend: Math.abs(completed.filter(row => Number(row.coins_delta) < 0).reduce((sum, row) => sum + Number(row.coins_delta || 0), 0)), monthlySpend: Math.abs(completed.filter(row => new Date(row.created_at) >= start && Number(row.coins_delta) < 0).reduce((sum, row) => sum + Number(row.coins_delta || 0), 0)), totalEarnings: completed.reduce((sum, row) => sum + Math.max(0, Number(row.rupees_delta || 0)), 0), monthlyEarnings: completed.filter(row => new Date(row.created_at) >= start).reduce((sum, row) => sum + Math.max(0, Number(row.rupees_delta || 0)), 0) }); setLoading(false)
    }
    load(); return () => { cancelled = true }
  }, [userId, enabled])
  return { ...data, loading, error }
}
