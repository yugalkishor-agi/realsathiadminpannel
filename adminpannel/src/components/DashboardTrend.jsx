export default function DashboardTrend({ data }) {
  const peak = Math.max(...data.map(item => item.value), 1)
  return <div className="trend-chart" aria-label="Activity trend">{data.length ? data.map(item => <div className="trend-column" key={item.label}><div className="trend-bar" style={{ height: `${Math.max(8, item.value / peak * 100)}%` }} title={`${item.label}: ${item.value}`} /><small>{item.label}</small></div>) : <div className="empty">No activity in the selected period.</div>}</div>
}
