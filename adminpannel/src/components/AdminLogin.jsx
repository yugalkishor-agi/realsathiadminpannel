import { useState } from 'react'

export default function AdminLogin({ error, onSignIn }) {
  const [email, setEmail] = useState(''); const [password, setPassword] = useState(''); const [busy, setBusy] = useState(false)
  const submit = async (event) => { event.preventDefault(); setBusy(true); await onSignIn(email, password); setBusy(false) }
  return <main className="login-page"><form className="login-card" onSubmit={submit}>
    <div className="brand-mark">RealSaathi OS</div><h1>Admin sign in</h1>
    <p>Manage users, hosts, wallet activity, reports, and support.</p>
    {error && <div className="error">{error}</div>}
    <div className="field"><label htmlFor="email">Admin email</label><input id="email" type="email" required value={email} onChange={e => setEmail(e.target.value)} /></div>
    <div className="field"><label htmlFor="password">Password</label><input id="password" type="password" required value={password} onChange={e => setPassword(e.target.value)} /></div>
    <button className="primary-btn" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button>
    <div className="login-foot">SECURE ADMIN SIGN-IN</div>
  </form></main>
}
