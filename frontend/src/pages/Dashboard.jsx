import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  PieChart, Pie, Cell, ResponsiveContainer
} from "recharts";
import api from "../services/api";

const STATUS_COLORS  = { Active: "#27ae60", Pending: "#f39c12", Failed: "#e74c3c", "Under Review": "#3498db" };
const PRIORITY_COLORS = ["#e74c3c", "#f39c12", "#27ae60"];

export default function Dashboard() {
  const [stats, setStats]   = useState(null);
  const [plans, setPlans]   = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get("/api/plans/stats"),
      api.get("/api/plans?page=0&size=5"),
    ])
      .then(([statsRes, plansRes]) => {
        setStats(statsRes.data);
        setPlans(plansRes.data.content || []);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="page"><div className="skeleton-grid">{[...Array(4)].map((_, i) => <div key={i} className="skeleton-card"/>)}</div></div>;

  const statusBarData = [
    { name: "Active",      count: stats?.active      ?? 0 },
    { name: "Pending",     count: stats?.pending     ?? 0 },
    { name: "Failed",      count: stats?.failed      ?? 0 },
    { name: "Under Review",count: stats?.underReview ?? 0 },
  ];

  const priorityPieData = [
    { name: "High",   value: stats?.highPriority ?? 0 },
    { name: "Medium", value: Math.max(0, (stats?.total ?? 0) - (stats?.highPriority ?? 0) - Math.floor((stats?.total ?? 0) * 0.2)) },
    { name: "Low",    value: Math.floor((stats?.total ?? 0) * 0.2) },
  ].filter(d => d.value > 0);

  const deptData = Object.entries(stats?.byDepartment ?? {}).map(([dept, count]) => ({
    name: dept, count
  }));

  return (
    <div className="page">
      <div className="page-header">
        <h2>Dashboard</h2>
        <span className="page-sub">Overview of all business continuity plans</span>
      </div>

      {/* KPI Cards */}
      <div className="stats-grid">
        <KpiCard value={stats?.total ?? 0}       label="Total Plans"    color="#1B4F8A" icon="📋" />
        <KpiCard value={stats?.active ?? 0}      label="Active"         color="#27ae60" icon="✅" />
        <KpiCard value={stats?.pending ?? 0}     label="Pending"        color="#f39c12" icon="⏳" />
        <KpiCard value={stats?.failed ?? 0}      label="Failed"         color="#e74c3c" icon="❌" />
      </div>

      <div className="stats-grid" style={{ marginTop: 16 }}>
        <KpiCard value={stats?.highPriority ?? 0} label="High Priority" color="#9b59b6" icon="🔥" />
        <KpiCard value={stats?.underReview ?? 0}  label="Under Review"  color="#3498db" icon="🔎" />
        <KpiCard value={`${stats?.avgScore ?? 0}%`} label="Avg Score"   color="#1abc9c" icon="📊" />
        <KpiCard value={Object.keys(stats?.byDepartment ?? {}).length} label="Departments" color="#e67e22" icon="🏢" />
      </div>

      {/* Charts row */}
      <div className="charts-row">
        <div className="card chart-card">
          <h3>Plans by Status</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={statusBarData} margin={{ top: 5, right: 10, left: 0, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                {statusBarData.map((entry) => (
                  <Cell key={entry.name} fill={STATUS_COLORS[entry.name] || "#1B4F8A"} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        <div className="card chart-card">
          <h3>Plans by Department</h3>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={deptData} layout="vertical" margin={{ top: 5, right: 10, left: 60, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis type="number" tick={{ fontSize: 12 }} />
              <YAxis dataKey="name" type="category" tick={{ fontSize: 11 }} width={55} />
              <Tooltip />
              <Bar dataKey="count" fill="#1B4F8A" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {priorityPieData.length > 0 && (
          <div className="card chart-card">
            <h3>Priority Breakdown</h3>
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={priorityPieData} cx="50%" cy="50%" outerRadius={80}
                  dataKey="value" label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  labelLine={false}>
                  {priorityPieData.map((_, i) => (
                    <Cell key={i} fill={PRIORITY_COLORS[i % PRIORITY_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      {/* Recent plans */}
      <div className="card">
        <div className="card-header">
          <h3>Recent Plans</h3>
          <Link to="/plans" className="link-btn">View All →</Link>
        </div>
        <table className="table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Department</th>
              <th>Status</th>
              <th>Score</th>
            </tr>
          </thead>
          <tbody>
            {plans.length === 0 ? (
              <tr><td colSpan="4" className="empty-cell">No plans yet</td></tr>
            ) : (
              plans.map(p => (
                <tr key={p.id}>
                  <td><Link to={`/plans/${p.id}`} className="plan-link">{p.title}</Link></td>
                  <td>{p.department || "—"}</td>
                  <td><span className={`badge badge-${p.status?.toLowerCase().replace(" ", "-")}`}>{p.status}</span></td>
                  <td><ScoreBadge score={p.score} /></td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function KpiCard({ value, label, color, icon }) {
  return (
    <div className="kpi-card" style={{ borderTopColor: color }}>
      <div className="kpi-icon">{icon}</div>
      <div>
        <div className="kpi-value" style={{ color }}>{value}</div>
        <div className="kpi-label">{label}</div>
      </div>
    </div>
  );
}

export function ScoreBadge({ score }) {
  const s = score ?? 0;
  const cls = s >= 80 ? "score-high" : s >= 50 ? "score-mid" : "score-low";
  return <span className={`score-badge ${cls}`}>{s}</span>;
}
