import { useMemo, useState } from 'react'
import { adminCreditWallet } from '../lib/adminActions'

export function useAdminWalletCredit(users, onCompleted) {
  const [query, setQuery] = useState('')
  const [userId, setUserId] = useState('')
  const [coins, setCoins] = useState('')
  const [reason, setReason] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [saving, setSaving] = useState(false)
  const filteredUsers = useMemo(() => users.filter(user => JSON.stringify(user).toLowerCase().includes(query.toLowerCase())), [users, query])
  const submit = async event => {
    event.preventDefault(); setError(''); setMessage('')
    const amount = Number(coins)
    if (!userId || !Number.isInteger(amount) || amount < 1 || amount > 1000000) return setError('Select a user and enter coins from 1 to 1,000,000.')
    const user = users.find(item => item.id === userId); const label = user?.username || user?.public_id || userId
    if (!window.confirm(`Credit ${amount.toLocaleString()} coins to ${label}?`)) return
    setSaving(true); const result = await adminCreditWallet(userId, amount, reason)
    if (result.error) setError(result.error.message)
    else { setMessage(`${amount.toLocaleString()} coins credited to ${label}.`); setCoins(''); setReason(''); await onCompleted?.() }
    setSaving(false)
  }
  return { query, setQuery, userId, setUserId, coins, setCoins, reason, setReason, filteredUsers, error, message, saving, submit }
}
