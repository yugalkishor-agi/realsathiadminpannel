import { useEffect, useState } from 'react'
import './App.css'
import AdminLogin from './components/AdminLogin'
import AdminShell from './components/AdminShell'
import { useAdminSession } from './hooks/useAdminSession'

function App() {
  const { session, access, loading, error, signIn, signOut } = useAdminSession()
  const [section, setSection] = useState('overview')
  useEffect(() => { document.title = 'RealSaathi OS' }, [])
  if (loading) return <div className="boot-screen"><span className="pulse-dot" />Loading RealSaathi OS…</div>
  if (!session) return <AdminLogin error={error} onSignIn={signIn} />
  return <AdminShell access={access} section={section} onNavigate={setSection} onSignOut={signOut} />
}

export default App
