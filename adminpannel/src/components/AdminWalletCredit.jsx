import { useAdminWalletCredit } from '../hooks/useAdminWalletCredit'

export default function AdminWalletCredit({ users, onCompleted }) {
  const form = useAdminWalletCredit(users, onCompleted)
  return <section className="panel inspector">
    <div className="section-title"><div><p className="eyebrow">Manual wallet credit</p><h2>Recharge any user</h2><p className="muted">Adds a completed payment entry to the user coin ledger.</p></div></div>
    <form className="inspector-form" onSubmit={form.submit}>
      <div className="form-grid">
        <label>Search user<input value={form.query} onChange={event => form.setQuery(event.target.value)} placeholder="Name, ID, phone…" /></label>
        <label>Select user<select value={form.userId} onChange={event => form.setUserId(event.target.value)}><option value="">Choose a user</option>{form.filteredUsers.map(user => <option key={user.id} value={user.id}>{user.username || user.public_id || user.id} · {user.role}</option>)}</select></label>
        <label>Coins<input type="number" min="1" max="1000000" step="1" value={form.coins} onChange={event => form.setCoins(event.target.value)} placeholder="Example: 500" /></label>
        <label>Reason<input value={form.reason} onChange={event => form.setReason(event.target.value)} placeholder="Support adjustment" maxLength="240" /></label>
      </div>
      <button className="primary-btn" type="submit" disabled={form.saving}>{form.saving ? 'Crediting…' : 'Credit coins'}</button>
    </form>
    {form.message && <div className="success">{form.message}</div>}{form.error && <div className="error">{form.error}</div>}
  </section>
}
