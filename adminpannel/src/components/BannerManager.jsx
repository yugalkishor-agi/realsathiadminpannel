import { useEffect, useState } from 'react'
import { supabase } from '../lib/supabase'

const placements = [{ key: 'home', label: 'Home banners', ratio: 1.9 }, { key: 'wallet', label: 'Wallet banners', ratio: 2.1 }]
const slots = [1, 2, 3]

const readRatio = file => new Promise((resolve, reject) => {
  const image = new Image()
  image.onload = () => resolve(image.width / image.height)
  image.onerror = () => reject(new Error('Image preview could not be read.'))
  image.src = URL.createObjectURL(file)
})

export default function BannerManager() {
  const [banners, setBanners] = useState([]); const [preview, setPreview] = useState({}); const [busy, setBusy] = useState(''); const [error, setError] = useState('')
  const load = async () => { const result = await supabase.from('app_banners').select('id,placement,slot,image_path,image_url,version,updated_at').order('placement').order('slot'); if (result.error) setError(result.error.message); else setBanners(result.data || []) }
  useEffect(() => { load() }, [])
  const get = (placement, slot) => banners.find(item => item.placement === placement && item.slot === slot)
  const upload = async (placement, slot, file) => {
    const target = placements.find(item => item.key === placement); const ratio = await readRatio(file).catch(() => 0)
    if (!ratio || Math.abs(ratio - target.ratio) > 0.08) { setError(`${target.label} Image ${slot} must be ${target.ratio}:1 ratio. Selected image is ${ratio ? ratio.toFixed(2) : 'invalid'}:1.`); return }
    setBusy(`${placement}-${slot}`); setError('')
    const extension = file.name.split('.').pop()?.toLowerCase() || 'jpg'; const path = `${placement}/banner-${slot}.${extension}`
    const uploadResult = await supabase.storage.from('app-banners').upload(path, file, { upsert: true, cacheControl: '300', contentType: file.type })
    if (uploadResult.error) { setError(uploadResult.error.message); setBusy(''); return }
    const url = supabase.storage.from('app-banners').getPublicUrl(path).data.publicUrl; const version = Date.now(); const user = await supabase.auth.getUser()
    const saveResult = await supabase.from('app_banners').upsert({ placement, slot, image_path: path, image_url: `${url}?v=${version}`, version, updated_by: user.data.user?.id }, { onConflict: 'placement,slot' })
    if (saveResult.error) setError(saveResult.error.message); else await load(); setBusy('')
  }
  const remove = async item => { setBusy(`${item.placement}-${item.slot}`); const fileResult = await supabase.storage.from('app-banners').remove([item.image_path]); if (fileResult.error) setError(fileResult.error.message); else { const result = await supabase.from('app_banners').delete().eq('id', item.id); if (result.error) setError(result.error.message); else await load() } setBusy('') }
  return <div className="banner-manager"><div className="section-title"><div><h2>Banner manager</h2><p>Upload images matching the card ratio</p></div></div>{error && <div className="error">{error}</div>}{placements.map(place => <section className="panel banner-section" key={place.key}><div className="section-title"><h3>{place.label}</h3><span className="muted">Required ratio: {place.ratio}:1 · 3 slots</span></div><div className="banner-grid">{slots.map(slot => { const item = get(place.key, slot); const id = `${place.key}-${slot}`; return <article className="banner-card" key={id}><div className="banner-preview">{preview[id] || item?.image_url ? <img src={preview[id] || item.image_url} alt={`${place.label} ${slot}`} /> : <span>Slot {slot}<small>No image uploaded</small></span>}</div><strong>Image {slot}</strong><input type="file" accept="image/png,image/jpeg,image/webp" onChange={async e => { const file = e.target.files?.[0]; if (!file) return; const ratio = await readRatio(file).catch(() => 0); if (!ratio || Math.abs(ratio - place.ratio) > 0.08) { setError(`${place.label} Image ${slot} must be ${place.ratio}:1 ratio.`); return } setPreview({ ...preview, [id]: URL.createObjectURL(file) }); upload(place.key, slot, file) }} /><div className="row-actions">{item && <button className="mini-btn danger-btn" disabled={busy === id} onClick={() => remove(item)}>Remove</button>}{busy === id && <small className="muted">Uploading…</small>}</div></article> })}</div></section>)}</div>
}
