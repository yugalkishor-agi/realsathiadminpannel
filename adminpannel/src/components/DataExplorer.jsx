import { useMemo, useState } from 'react'
import { ADMIN_TABLES } from '../config/adminTables'
import { useAdminData } from '../hooks/useAdminData'
import StatusBadge from './StatusBadge'

export default function DataExplorer({ permissions }) {
  const tables = ADMIN_TABLES.filter(item => permissions.includes('*') || permissions.includes(item.permission))
  const [selected, setSelected] = useState(tables[0] || ADMIN_TABLES[0]); const [query, setQuery] = useState('')
  const { rows, loading, error, reload } = useAdminData(selected.key, selected.columns.join(','))
  const visible = useMemo(() => rows.filter(row => JSON.stringify(row).toLowerCase().includes(query.toLowerCase())), [rows, query])
  return <><div className="section-title"><div><h2>Database explorer</h2><p>Permission-scoped product tables · live RLS query</p></div><button className="ghost-btn" onClick={reload}>Refresh</button></div>
    <div className="explorer"><aside className="table-menu">{tables.map(item => <button className={item.key === selected.key ? 'active' : ''} onClick={() => setSelected(item)} key={item.key}>{item.label}<span>{item.key}</span></button>)}</aside>
    <section className="panel explorer-main"><div className="toolbar"><input placeholder="Search visible rows…" value={query} onChange={e => setQuery(e.target.value)} /><span>{visible.length} rows</span></div>{error && <div className="error">{error}</div>}{loading ? <div className="empty">Loading {selected.label}…</div> : <div className="table-wrap"><table className="table"><thead><tr>{selected.columns.map(column => <th key={column}>{column}</th>)}</tr></thead><tbody>{visible.map((row, index) => <tr key={`${selected.key}-${row.id || row.user_id || row.order_id || 'row'}-${index}`}>{selected.columns.map(column => <td key={column}>{column === 'status' ? <StatusBadge value={row[column]} /> : String(row[column] ?? '—')}</td>)}</tr>)}</tbody></table>{!visible.length && <div className="empty">No matching records.</div>}</div>}</section></div></>
}
