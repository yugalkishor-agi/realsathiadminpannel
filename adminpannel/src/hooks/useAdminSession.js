import { useEffect, useState } from 'react'
import { configError, supabase } from '../lib/supabase'

export function useAdminSession() {
  const [session, setSession] = useState(null)
  const [access, setAccess] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(configError)
  const loadAccess = async (user) => {
    const role = user.app_metadata?.role
    if (role === 'super_admin') { setSession({ user }); setAccess({ role, permissions: ['*'] }); setLoading(false); return }
    const result = await supabase.from('admin_members').select('role,permissions,status').eq('user_id', user.id).maybeSingle()
    if (result.error || !result.data || result.data.status !== 'active') { await supabase.auth.signOut(); setError('This account is not authorized for the admin console.'); setLoading(false); return }
    setSession({ user }); setAccess({ role: result.data.role, permissions: result.data.permissions || [] }); setLoading(false)
  }
  useEffect(() => {
    if (!supabase) { setLoading(false); return undefined }
    supabase.auth.getSession().then(({ data }) => { if (data.session) loadAccess(data.session.user); else setLoading(false) })
    const { data: listener } = supabase.auth.onAuthStateChange((_event, next) => { setSession(next); if (next) loadAccess(next.user); else { setAccess(null); setLoading(false) } })
    return () => listener.subscription.unsubscribe()
  }, [])
  const signIn = async (email, password) => {
    if (!supabase) return
    setError(''); const result = await supabase.auth.signInWithPassword({ email, password })
    if (result.error) return setError(result.error.message)
    await loadAccess(result.data.user)
  }
  const signOut = () => supabase?.auth.signOut()
  return { session, access, loading, error, signIn, signOut }
}
