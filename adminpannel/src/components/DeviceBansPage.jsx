import { useMemo, useState } from 'react'
import StatusBadge from './StatusBadge'
import useDeviceBans from '../hooks/useDeviceBans'

export default function DeviceBansPage() {
  const { rows, loading, error, load, unban } = useDeviceBans(); const [query, setQuery] = useState('')
  const visible = useMemo(() => rows.filter(row => [row.device_id, row.device_brand, row.reason, row.user?.username, row.user?.public_id].join(' ').toLowerCase().includes(query.toLowerCase())), [rows, query])
  const revoke = async row => { if (window.confirm(`Unban device ${row.device_id}?`)) await unban(row) }
  return <><div className="section-title"><div><h2>Device bans</h2><p>Review devices blocked from signing in. New bans are created from Reports.</p></div><button className="ghost-btn" onClick={load}>Refresh</button></div><div className="toolbar"><input className="search-input" placeholder="Search device, user or reason" value={query} onChange={event => setQuery(event.target.value)} /></div>{error && <div className="error">{error}</div>}<div className="panel table-wrap"><table className="table"><thead><tr><th>Device</th><th>Account</th><th>Reason</th><th>Created</th><th>Status</th><th /></tr></thead><tbody>{visible.map(row => <tr key={row.id}><td><strong>{row.device_brand || 'Unknown device'}</strong><small>{row.device_id}</small></td><td>{row.user?.username || row.user?.public_id || row.user_id || 'Unlinked'}</td><td>{row.reason}</td><td>{new Date(row.created_at).toLocaleString()}</td><td><StatusBadge value="banned" /></td><td><button className="mini-btn danger-btn" onClick={() => revoke(row)}>Unban device</button></td></tr>)}</tbody></table>{loading && <div className="empty">Loading device bans…</div>}{!loading && !visible.length && <div className="empty">No banned devices found.</div>}</div></>
}
