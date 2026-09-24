import { useCallback, useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'
import StatusBadge from './StatusBadge'

export default function AuditTrailPage() {
  const [rows, setRows] = useState([]); const [query, setQuery] = useState(''); const [error, setError] = useState(''); const [loading, setLoading] = useState(true)
  const load = useCallback(async () => { setLoading(true); const result = await supabase.from('admin_audit_logs').select('id,actor_id,action,entity_type,entity_id,metadata,created_at').order('created_at', { ascending: false }).limit(300); if (result.error) setError(result.error.message); else { setRows(result.data || []); setError('') } setLoading(false) }, [])
  useEffect(() => { load() }, [load])
  const visible = rows.filter(row => JSON.stringify(row).toLowerCase().includes(query.toLowerCase()))
  return <><div className="page-actions"><div><p className="eyebrow">Admin activity</p><h2 className="page-heading">Audit trail</h2><p className="muted">Recent changes made by admins and co-workers.</p></div><button className="ghost-btn" onClick={load}>Refresh</button></div>{error && <div className="error">{error} · Audit history is not available yet.</div>}<div className="panel"><div className="toolbar"><input placeholder="Search actions, entities, or actor IDs…" value={query} onChange={event => setQuery(event.target.value)} /><span>{visible.length} events</span></div><div className="table-wrap"><table className="table"><thead><tr><th>Time</th><th>Action</th><th>Entity</th><th>Actor</th><th>Metadata</th></tr></thead><tbody>{visible.map(row => <tr key={row.id}><td>{new Date(row.created_at).toLocaleString('en-IN')}</td><td><StatusBadge value={row.action.replaceAll('_', ' ')} /></td><td>{row.entity_type} · {row.entity_id}</td><td>{row.actor_id}</td><td><code>{JSON.stringify(row.metadata || {})}</code></td></tr>)}</tbody></table>{loading && <div className="empty">Loading audit events…</div>}{!loading && !visible.length && <div className="empty">No audit events found.</div>}</div></div></>
}
