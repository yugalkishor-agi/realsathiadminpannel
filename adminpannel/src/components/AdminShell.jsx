import OverviewPage from './OverviewPage'
import DataExplorer from './DataExplorer'
import CoWorkerPage from './CoWorkerPage'
import UserManagementPage from './UserManagementPage'
import KycReviewPage from './KycReviewPage'
import BannerManager from './BannerManager'
import SupportInboxPage from './SupportInboxPage'
import ModerationPage from './ModerationPage'
import RechargeDashboard from './RechargeDashboard'
import StoryModerationPage from './StoryModerationPage'
import AuditTrailPage from './AuditTrailPage'

const nav = [{ key: 'overview', icon: '◈', label: 'Dashboard' }, { key: 'clients', icon: '♙', label: 'Users', permission: 'users' }, { key: 'hosts', icon: '♙', label: 'Hosts', permission: 'users' }, { key: 'recharges', icon: '₹', label: 'Recharges', permission: 'wallet' }, { key: 'moderation', icon: '△', label: 'Reports', permission: 'moderation' }, { key: 'stories', icon: '◌', label: 'Story controls', permission: 'moderation' }, { key: 'support', icon: '✦', label: 'Support inbox', permission: 'support' }, { key: 'kyc', icon: '✓', label: 'KYC review', permission: 'kyc' }, { key: 'audit', icon: '⌁', label: 'Audit trail', permission: 'audit' }, { key: 'banners', icon: '▣', label: 'Banners', permission: 'banners' }, { key: 'data', icon: '⌘', label: 'Data explorer' }, { key: 'coworkers', icon: '+', label: 'Co-workers', superOnly: true }]

export default function AdminShell({ access, section, onNavigate, onSignOut }) {
  const visible = nav.filter(item => !item.superOnly || access.role === 'super_admin').filter(item => !item.permission || access.permissions.includes('*') || access.permissions.includes(item.permission)); const title = visible.find(item => item.key === section)?.label || 'Overview'
 return <div className="admin-shell"><aside className="sidebar"><div className="logo">real<b>saathi</b><small> / OS</small></div><nav className="nav">{visible.map(item => <button className={section === item.key ? 'active' : ''} onClick={() => onNavigate(item.key)} key={item.key}><span>{item.icon}</span>{item.label}</button>)}</nav><div className="sidebar-foot"><span className="secure-badge">● SECURE ADMIN AREA</span><br />{access.role.replace('_', ' ').toUpperCase()}<br />ADMIN ACCESS</div></aside><main className="content"><header className="topbar"><div><div className="eyebrow">RealSaathi OS / {section}</div><h1>{title}</h1></div><div className="user-chip"><div className="avatar">A</div><span>{access.role.replace('_', ' ')}</span><button className="ghost-btn" onClick={onSignOut}>Sign out</button></div></header>{section === 'overview' && <OverviewPage permissions={access.permissions} />}{section === 'hosts' && <UserManagementPage kind="hosts" permissions={access.permissions} />}{section === 'clients' && <UserManagementPage kind="clients" permissions={access.permissions} />}{section === 'recharges' && <RechargeDashboard />}{section === 'stories' && <StoryModerationPage />}{section === 'audit' && <AuditTrailPage />}{section === 'kyc' && <KycReviewPage />}{section === 'support' && <SupportInboxPage />}{section === 'banners' && <BannerManager />}{section === 'data' && <DataExplorer permissions={access.permissions} />}{section === 'moderation' && <ModerationPage />}{section === 'coworkers' && <CoWorkerPage />}</main></div>
}
