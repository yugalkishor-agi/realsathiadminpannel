import OverviewPage from './OverviewPage'
import DataExplorer from './DataExplorer'
import CoWorkerPage from './CoWorkerPage'
import UserManagementPage from './UserManagementPage'
import KycReviewPage from './KycReviewPage'

const nav = [{ key: 'overview', icon: '◈', label: 'Overview' }, { key: 'hosts', icon: '♙', label: 'Hosts', permission: 'users' }, { key: 'clients', icon: '♙', label: 'Clients', permission: 'users' }, { key: 'kyc', icon: '✓', label: 'KYC review', permission: 'kyc' }, { key: 'data', icon: '⌘', label: 'Data explorer' }, { key: 'moderation', icon: '△', label: 'Moderation', permission: 'moderation' }, { key: 'coworkers', icon: '+', label: 'Co-workers', superOnly: true }]

export default function AdminShell({ access, section, onNavigate, onSignOut }) {
  const visible = nav.filter(item => !item.superOnly || access.role === 'super_admin').filter(item => !item.permission || access.permissions.includes('*') || access.permissions.includes(item.permission)); const title = visible.find(item => item.key === section)?.label || 'Overview'
  return <div className="admin-shell"><aside className="sidebar"><div className="logo">real<b>saathi</b><small> / ADMIN</small></div><nav className="nav">{visible.map(item => <button className={section === item.key ? 'active' : ''} onClick={() => onNavigate(item.key)} key={item.key}>{item.icon}&nbsp;&nbsp;{item.label}</button>)}</nav><div className="sidebar-foot">{access.role.toUpperCase()}<br />RLS POLICY ACTIVE<br />REGION: IN · UTC+5:30</div></aside><main className="content"><header className="topbar"><div><div className="eyebrow">Control room / {section}</div><h1>{title}</h1></div><div className="user-chip"><div className="avatar">A</div><span>{access.role.replace('_', ' ')}</span><button className="ghost-btn" onClick={onSignOut}>Sign out</button></div></header>{section === 'overview' && <OverviewPage permissions={access.permissions} />}{section === 'hosts' && <UserManagementPage kind="hosts" permissions={access.permissions} />}{section === 'clients' && <UserManagementPage kind="clients" permissions={access.permissions} />}{section === 'kyc' && <KycReviewPage />}{section === 'data' && <DataExplorer permissions={access.permissions} />}{section === 'moderation' && <div className="panel empty">Moderation workspace is ready for report and KYC review.</div>}{section === 'coworkers' && <CoWorkerPage />}</main></div>
}
