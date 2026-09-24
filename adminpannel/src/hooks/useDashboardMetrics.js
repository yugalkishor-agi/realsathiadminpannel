import { useCallback, useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'

const empty = { users: 0, active: 0, hosts: 0, calls: 0, answered: 0, missed: 0, cancelled: 0, minutes: 0, rechargeUsers: 0, recharges: 0, rechargeAmount: 0, revenue: 0, reports: 0, support: 0, trend: [], recent: [], loading: true, error: '' }
const dateKey = value => new Date(value).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })

export function useDashboardMetrics(permissions) {
  const [data, setData] = useState(empty)
  const load = useCallback(async () => {
    if (!supabase) return setData({ ...empty, loading: false, error: 'Supabase is not configured.' })
    const can = key => permissions.includes('*') || permissions.includes(key)
    const [users, ledger, recharges, reports, support] = await Promise.all([
      can('users') ? supabase.from('users').select('id,role,account_status,last_active,created_at').limit(5000) : { data: [], error: null },
      can('wallet') ? supabase.from('wallet_ledger').select('user_id,kind,rupees_delta,recharge_amount_rupees,coins_delta,status,metadata,created_at').order('created_at', { ascending: false }).limit(2000) : { data: [], error: null },
      can('wallet') ? supabase.from('recharge_orders').select('user_id,amount_rupees,status,created_at').order('created_at', { ascending: false }).limit(2000) : { data: [], error: null },
      can('moderation') ? supabase.from('user_reports').select('id,status,created_at').limit(2000) : { data: [], error: null },
      can('support') ? supabase.from('support_threads').select('id,status,created_at').limit(2000) : { data: [], error: null },
    ])
    const rows = ledger.data || []; const callRows = rows.filter(row => ['audio_call', 'video_call'].includes(row.kind)); const calls = [...new Map(callRows.map(row => [row.metadata?.callId || `${row.kind}-${row.created_at}-${row.user_id}`, row])).values()]; const completed = rows.filter(row => row.status === 'completed')
    const trendMap = {}; [...calls, ...(recharges.data || [])].forEach(row => { const key = dateKey(row.created_at); trendMap[key] = (trendMap[key] || 0) + 1 })
    const trend = Object.entries(trendMap).slice(-14).map(([label, value]) => ({ label, value }))
    const error = [users, ledger, recharges, reports, support].find(item => item.error)?.error?.message || ''
    setData({ users: users.data?.length || 0, active: users.data?.filter(row => row.account_status === 'active').length || 0, hosts: users.data?.filter(row => row.role === 'host').length || 0, calls: calls.length, answered: calls.filter(row => row.metadata?.callStatus === 'completed').length, missed: calls.filter(row => row.metadata?.callStatus === 'missed').length, cancelled: calls.filter(row => ['canceled', 'declined'].includes(row.metadata?.callStatus)).length, minutes: Math.round(calls.reduce((sum, row) => sum + Number(row.metadata?.durationSeconds || 0), 0) / 60), rechargeUsers: new Set((recharges.data || []).filter(row => row.status === 'completed').map(row => row.user_id)).size, recharges: (recharges.data || []).filter(row => row.status === 'completed').length, rechargeAmount: (recharges.data || []).filter(row => row.status === 'completed').reduce((sum, row) => sum + Number(row.amount_rupees || 0), 0), revenue: completed.reduce((sum, row) => sum + Math.max(0, Number(row.rupees_delta || 0)), 0), reports: reports.data?.filter(row => row.status === 'pending').length || 0, support: support.data?.filter(row => ['open', 'in_progress'].includes(row.status)).length || 0, trend, recent: rows.slice(0, 8), loading: false, error })
  }, [permissions])
  useEffect(() => { load() }, [load])
  return { ...data, reload: load }
}
