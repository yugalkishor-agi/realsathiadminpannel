import { useAdminData } from '../hooks/useAdminData'
import MetricCard from './MetricCard'
import StatusBadge from './StatusBadge'

export default function OverviewPage({ permissions }) {
  const can = permission => permissions.includes('*') || permissions.includes(permission)
  const users = useAdminData(can('users') ? 'users' : '', 'id,public_id,phone,username,role,account_status,host_status,created_at', 8)
  const ledger = useAdminData(can('wallet') ? 'wallet_ledger' : '', 'id,user_id,kind,coins_delta,status,created_at', 8)
  const active = users.rows.filter(row => row.account_status === 'active').length
  const hosts = users.rows.filter(row => row.role === 'host').length
  const movement = ledger.rows.reduce((total, row) => total + Number(row.coins_delta || 0), 0)
  return <><div className="metric-grid"><MetricCard label="Users sampled" value={users.loading ? '—' : users.rows.length} note={`${active} active in latest view`} /><MetricCard label="Hosts sampled" value={users.loading ? '—' : hosts} note="From users table" /><MetricCard label="Wallet movement" value={ledger.loading ? '—' : movement.toLocaleString()} note="Coins in latest ledger" /><MetricCard label="Data health" value={users.error || ledger.error ? 'Check' : 'Live'} note="RLS query status" /></div>
    <div className="overview-grid"><section><div className="section-title"><div><h2>Recent users</h2><p>Latest records from public.users</p></div></div><div className="panel table-wrap"><table className="table"><thead><tr><th>Phone</th><th>Role</th><th>State</th><th>Created</th></tr></thead><tbody>{users.rows.map((row, index) => <tr key={`${row.id}-${index}`}><td>{row.phone || '—'}</td><td>{row.role}</td><td><StatusBadge value={row.account_status || 'unknown'} /></td><td>{row.created_at ? new Date(row.created_at).toLocaleDateString() : '—'}</td></tr>)}</tbody></table></div></section>
      <section><div className="section-title"><div><h2>Wallet pulse</h2><p>Latest ledger events</p></div></div><div className="panel table-wrap"><table className="table"><thead><tr><th>Kind</th><th>Coins</th><th>Status</th></tr></thead><tbody>{ledger.rows.map((row, index) => <tr key={`${row.id}-${index}`}><td>{row.kind}</td><td>{row.coins_delta > 0 ? '+' : ''}{row.coins_delta}</td><td><StatusBadge value={row.status} /></td></tr>)}</tbody></table></div></section></div></>
}
