export default function StatusBadge({ value }) {
  const text = String(value ?? 'unknown'); const tone = /failed|rejected|blocked/i.test(text) ? 'danger' : /pending|processing/i.test(text) ? 'warn' : ''
  return <span className={`status ${tone}`}>{text}</span>
}
