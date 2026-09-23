import { useState } from 'react'

export default function AdminLogin({ error, onSignIn }) {
  const [email, setEmail] = useState(''); const [password, setPassword] = useState(''); const [busy, setBusy] = useState(false)
  const submit = async (event) => { event.preventDefault(); setBusy(true); await onSignIn(email, password); setBusy(false) }
  return <main className="login-page"><form className="login-card" onSubmit={submit}>
    <div className="brand-mark">RealSaathi / private console</div><h1>Welcome back.</h1>
    <p>Secure access to users, hosts, wallet movement and moderation data.</p>
    {error && <div className="error">{error}</div>}
    <div className="field"><label htmlFor="email">Admin email</label><input id="email" type="email" required value={email} onChange={e => setEmail(e.target.value)} /></div>
    <div className="field"><label htmlFor="password">Password</label><input id="password" type="password" required value={password} onChange={e => setPassword(e.target.value)} /></div>
    <button className="primary-btn" disabled={busy}>{busy ? 'Authenticating…' : 'Enter control room'}</button>
    <div className="login-foot">AUTHENTICATED BY SUPABASE · RLS ENFORCED</div>
  </form></main>
}
