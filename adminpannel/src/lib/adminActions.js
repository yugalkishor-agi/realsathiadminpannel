import { supabase } from './supabase'

const functionMissing = error => /PGRST202|42883|function|does not exist/i.test(String(error?.code || error?.message || ''))

export async function recordAudit(action, entityType, entityId, metadata = {}) {
  if (!supabase) return
  await supabase.rpc('admin_record_audit', { p_action: action, p_entity_type: entityType, p_entity_id: entityId, p_metadata: metadata })
}

export async function setAdminUserStatus(userId, status, reason = '') {
  const rpc = await supabase.rpc('admin_set_user_status', { p_user_id: userId, p_status: status, p_reason: reason })
  if (!rpc.error || !functionMissing(rpc.error)) return rpc
  const user = await supabase.auth.getUser()
  return supabase.from('users').update({ account_status: status, ban_reason: status === 'active' ? null : reason, banned_at: status === 'active' ? null : new Date().toISOString(), banned_by: status === 'active' ? null : user.data.user?.id }).eq('id', userId)
}

export async function removeHostStory(userId, storyId) {
  const rpc = await supabase.rpc('admin_remove_host_story', { p_user_id: userId, p_story_id: storyId })
  if (!rpc.error || !functionMissing(rpc.error)) return rpc
  const current = await supabase.from('users').select('host_story_items').eq('id', userId).single()
  if (current.error) return current
  const stories = (current.data.host_story_items || []).filter(item => String(item.id || item.storyId || item.mediaUri || item.mediaUrl || '') !== storyId)
  return supabase.from('users').update({ host_story_items: stories }).eq('id', userId)
}
