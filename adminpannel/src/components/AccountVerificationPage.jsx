import { useCallback, useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'

export default function AccountVerificationPage() {
  const [rows, setRows] = useState([]), [selected, setSelected] = useState(null)
  const [audioUrl, setAudioUrl] = useState(''), [message, setMessage] = useState(''), [error, setError] = useState('')
  const load = useCallback(async () => {
    const { data, error: queryError } = await supabase.from('account_voice_verifications').select('*').order('created_at', { ascending: false }).limit(100)
    if (queryError) setError(queryError.message); else setRows(data || [])
  }, [])
  useEffect(() => { load() }, [load])
  const open = async row => {
    setSelected(row); setMessage(row.review_message || ''); setAudioUrl('')
    const { data, error: urlError } = await supabase.storage.from('account-verification-audio').createSignedUrl(row.audio_path, 300)
    if (urlError) setError(urlError.message); else setAudioUrl(data.signedUrl)
  }
  const review = async status => {
    if (!selected || !window.confirm(`${status === 'approved' ? 'Approve' : 'Decline'} voice verification?`)) return
    const { error: reviewError } = await supabase.rpc('review_account_voice_verification', {
      p_user_id: selected.user_id, p_status: status, p_review_message: message,
    })
    if (reviewError) setError(reviewError.message); else { setSelected(null); await load() }
  }
  return <>
    <div className="section-title"><div><h2>Account verification</h2><p>Voice identification review queue</p></div><button className="ghost-btn" onClick={load}>Refresh</button></div>
    <div className="panel"><div className="table-wrap"><table className="table"><thead><tr><th>Account</th><th>Mobile</th><th>Prompt</th><th>Status</th><th>Submitted</th></tr></thead><tbody>
      {rows.map(row => <tr className="clickable-row" key={row.user_id} onClick={() => open(row)}><td>{row.account_name || 'New account'}</td><td>{row.phone_number}</td><td>{row.prompt}</td><td>{row.status}</td><td>{new Date(row.created_at).toLocaleString()}</td></tr>)}
    </tbody></table>{!rows.length && <p className="empty-state">No account verification requests.</p>}</div></div>
    {selected && <div className="panel account-review"><div className="section-title"><div><h2>{selected.account_name || 'New account'}</h2><p>Voice verification · {selected.user_id}</p></div><button className="ghost-btn" onClick={() => setSelected(null)}>Close</button></div>
      <p>Mobile: <strong>{selected.phone_number}</strong> <button className="mini-btn" onClick={() => navigator.clipboard.writeText(selected.phone_number)}>Copy number</button></p>
      <p>Read: {selected.prompt}</p>{audioUrl && <audio controls preload="none" src={audioUrl} />}
      <textarea className="review-message" placeholder="Optional review note" value={message} onChange={event => setMessage(event.target.value)} />
      {selected.status === 'pending' && <div className="row-actions"><button className="primary-btn" onClick={() => review('approved')}>Approve</button><button className="mini-btn danger-btn" onClick={() => review('rejected')}>Decline</button></div>}
    </div>}{error && <p className="error">{error}</p>}
  </>
}
