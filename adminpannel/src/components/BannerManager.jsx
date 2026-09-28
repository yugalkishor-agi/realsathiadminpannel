import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'

const placements = [{ key: 'home', label: 'Home banners', ratio: 1.9 }, { key: 'wallet', label: 'Wallet banners', ratio: 2.1 }]
const slots = [1, 2, 3]
const emptyDraft = { tag: '', title: '', subtitle: '', accent_start: '', accent_end: '' }

const readRatio = file => new Promise((resolve, reject) => {
  const image = new Image()
  image.onload = () => resolve(image.width / image.height)
  image.onerror = () => reject(new Error('Image preview could not be read.'))
  image.src = URL.createObjectURL(file)
})

export default function BannerManager() {
  const [banners, setBanners] = useState([]); const [drafts, setDrafts] = useState({})
  const [preview, setPreview] = useState({}); const [busy, setBusy] = useState(''); const [error, setError] = useState('')
  const load = async () => {
    const result = await supabase.from('app_banners').select('id,placement,slot,image_path,image_url,version,updated_at,tag,title,subtitle,accent_start,accent_end').order('placement').order('slot')
    if (result.error) setError(result.error.message)
    else { setBanners(result.data || []); setDrafts(Object.fromEntries((result.data || []).map(item => [`${item.placement}-${item.slot}`, { ...emptyDraft, ...item }]))) }
  }
  useEffect(() => { load() }, [])
  const get = (placement, slot) => banners.find(item => item.placement === placement && item.slot === slot)
  const updateDraft = (id, key, value) => setDrafts(current => ({ ...current, [id]: { ...emptyDraft, ...(current[id] || {}), [key]: value } }))
  const save = async (placement, slot, image = {}) => {
    const id = `${placement}-${slot}`; const item = get(placement, slot); const draft = drafts[id] || emptyDraft
    setBusy(id); setError('')
    const user = await supabase.auth.getUser()
    const result = await supabase.from('app_banners').upsert({ placement, slot, image_path: image.image_path ?? item?.image_path ?? null, image_url: image.image_url ?? item?.image_url ?? null, version: Date.now(), updated_by: user.data.user?.id, tag: draft.tag.trim(), title: draft.title.trim(), subtitle: draft.subtitle.trim(), accent_start: draft.accent_start.trim(), accent_end: draft.accent_end.trim() }, { onConflict: 'placement,slot' })
    if (result.error) setError(result.error.message)
    else { await supabase.rpc('admin_record_audit', { p_action: 'banner_updated', p_entity_type: 'banner', p_entity_id: id, p_metadata: { image: Boolean(image.image_url), customized: true } }); await load() }
    setBusy('')
  }
  const upload = async (placement, slot, file) => {
    const target = placements.find(item => item.key === placement); const ratio = await readRatio(file).catch(() => 0)
    if (!ratio || Math.abs(ratio - target.ratio) > 0.08) { setError(`${target.label} Image ${slot} must be ${target.ratio}:1 ratio.`); return }
    const id = `${placement}-${slot}`; setBusy(id); setError('')
    const extension = file.name.split('.').pop()?.toLowerCase() || 'jpg'; const path = `${placement}/banner-${slot}.${extension}`
    const uploaded = await supabase.storage.from('app-banners').upload(path, file, { upsert: true, cacheControl: '300', contentType: file.type })
    if (uploaded.error) { setError(uploaded.error.message); setBusy(''); return }
    const url = supabase.storage.from('app-banners').getPublicUrl(path).data.publicUrl
    setPreview(current => ({ ...current, [id]: URL.createObjectURL(file) })); await save(placement, slot, { image_path: path, image_url: `${url}?v=${Date.now()}` })
  }
  const remove = async item => { setBusy(`${item.placement}-${item.slot}`); if (item.image_path) await supabase.storage.from('app-banners').remove([item.image_path]); const result = await supabase.from('app_banners').delete().eq('id', item.id); if (result.error) setError(result.error.message); else await load(); setBusy('') }
  return <div className="banner-manager"><div className="section-title"><div><h2>Banner manager</h2><p>Customize default content or upload a circular image for each banner.</p></div></div>{error && <div className="error">{error}</div>}{placements.map(place => <section className="panel banner-section" key={place.key}><div className="section-title"><h3>{place.label}</h3><span className="muted">Image ratio: {place.ratio}:1 · 3 slots</span></div><div className="banner-grid">{slots.map(slot => { const item = get(place.key, slot); const id = `${place.key}-${slot}`; const draft = drafts[id] || emptyDraft; return <article className="banner-card" key={id}><div className="banner-preview">{preview[id] || item?.image_url ? <img src={preview[id] || item.image_url} alt={`${place.label} ${slot}`} /> : <span>Default logo image</span>}</div><strong>Banner {slot}</strong><label>Tag<input value={draft.tag} placeholder="REAL CONNECTIONS" onChange={e => updateDraft(id, 'tag', e.target.value)} /></label><label>Title<input value={draft.title} placeholder="Conversations that feel real" onChange={e => updateDraft(id, 'title', e.target.value)} /></label><label>Subtitle<textarea value={draft.subtitle} placeholder="Banner supporting text" onChange={e => updateDraft(id, 'subtitle', e.target.value)} /></label><div className="color-fields"><label>Start<input type="color" value={draft.accent_start || '#7027c9'} onChange={e => updateDraft(id, 'accent_start', e.target.value)} /></label><label>End<input type="color" value={draft.accent_end || '#f03b87'} onChange={e => updateDraft(id, 'accent_end', e.target.value)} /></label></div><input type="file" accept="image/png,image/jpeg,image/webp" onChange={e => { const file = e.target.files?.[0]; if (file) upload(place.key, slot, file) }} /><div className="row-actions"><button className="primary-btn" disabled={busy === id} onClick={() => save(place.key, slot)}>{busy === id ? 'Saving…' : 'Save design'}</button>{item && <button className="mini-btn danger-btn" disabled={busy === id} onClick={() => remove(item)}>Remove all</button>}</div></article> })}</div></section>)}</div>
}
