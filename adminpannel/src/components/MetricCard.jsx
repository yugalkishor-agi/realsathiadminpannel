export default function MetricCard({ label, value, note }) {
  return <div className="panel metric"><span>{label}</span><strong>{value}</strong><small>{note}</small></div>
}
