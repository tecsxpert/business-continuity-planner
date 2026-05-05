import { useEffect, useState } from "react";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  PieChart, Pie, Cell, LineChart, Line, ResponsiveContainer
} from "recharts";
import api from "../services/api";

const COLORS = ["#1B4F8A","#27ae60","#f39c12","#e74c3c","#9b59b6","#3498db","#1abc9c","#e67e22"];
const STATUS_COLORS = { Active: "#27ae60", Pending: "#f39c12", Failed: "#e74c3c", "Under Review": "#3498db" };

export default function Analytics() {
  const [stats, setStats]   = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get("/api/plans/stats")
      .then(r => setStats(r.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="page"><div className="loading-center">Loading analytics…</div></div>;

  const statusData = [
    { name: "Active",       value: stats?.active       ?? 0 },
    { name: "Pending",      value: stats?.pending      ?? 0 },
    { name: "Failed",       value: stats?.failed       ?? 0 },
    { name: "Under Review", value: stats?.underReview  ?? 0 },
  ].filter(d => d.value > 0);

  const deptData = Object.entries(stats?.byDepartment ?? {}).map(([name, value]) => ({ name, value }));

  // Simulated score trend (in a real app this would come from historical data)
  const trendData = [
    { month: "Jan", avgScore: 58 }, { month: "Feb", avgScore: 62 },
    { month: "Mar", avgScore: 65 }, { month: "Apr", avgScore: 70 },
    { month: "May", avgScore: stats?.avgScore ?? 72 },
  ];

  return (
    <div className="page">
      <div className="page-header">
        <h2>Analytics</h2>
        <span className="page-sub">Visual breakdown of all continuity plan data</span>
      </div>

      {/* Summary row */}
      <div className="stats-grid" style={{ marginBottom: 24 }}>
        {[
          { label: "Total Plans",      value: stats?.total ?? 0 },
          { label: "Avg Score",        value: `${stats?.avgScore ?? 0}%` },
          { label: "High Priority",    value: stats?.highPriority ?? 0 },
          { label: "Departments",      value: Object.keys(stats?.byDepartment ?? {}).length },
        ].map(({ label, value }) => (
          <div key={label} className="kpi-card" style={{ borderTopColor: "#1B4F8A" }}>
            <div className="kpi-value" style={{ color: "#1B4F8A" }}>{value}</div>
            <div className="kpi-label">{label}</div>
          </div>
        ))}
      </div>

      {/* Charts row 1 */}
      <div className="charts-row">
        <div className="card chart-card">
          <h3>Status Distribution</h3>
          <ResponsiveContainer width="100%" height={260}>
            <PieChart>
              <Pie data={statusData} cx="50%" cy="50%" outerRadius={90}
                dataKey="value"
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}>
                {statusData.map(entry => (
                  <Cell key={entry.name} fill={STATUS_COLORS[entry.name] || "#1B4F8A"} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="card chart-card">
          <h3>Plans by Department</h3>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={deptData} margin={{ top: 5, right: 10, left: 0, bottom: 30 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} angle={-30} textAnchor="end" interval={0} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                {deptData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Score trend */}
      <div className="card">
        <h3>Average Readiness Score Trend</h3>
        <p className="muted" style={{ marginBottom: 12 }}>Simulated historical trend — replace with real time-series data</p>
        <ResponsiveContainer width="100%" height={220}>
          <LineChart data={trendData} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="month" tick={{ fontSize: 12 }} />
            <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} />
            <Tooltip formatter={(v) => [`${v}%`, "Avg Score"]} />
            <Line type="monotone" dataKey="avgScore" stroke="#1B4F8A" strokeWidth={2}
              dot={{ fill: "#1B4F8A", r: 4 }} activeDot={{ r: 6 }} />
          </LineChart>
        </ResponsiveContainer>
      </div>

      {/* Department table */}
      {deptData.length > 0 && (
        <div className="card">
          <h3>Department Summary</h3>
          <table className="table">
            <thead>
              <tr><th>Department</th><th>Plans</th><th>Share</th></tr>
            </thead>
            <tbody>
              {deptData.map(({ name, value }) => (
                <tr key={name}>
                  <td>{name}</td>
                  <td>{value}</td>
                  <td>
                    <div className="progress-bar">
                      <div className="progress-fill"
                        style={{ width: `${((value / (stats?.total ?? 1)) * 100).toFixed(0)}%` }} />
                    </div>
                    <span style={{ fontSize: 12, color: "#888" }}>
                      {((value / (stats?.total ?? 1)) * 100).toFixed(0)}%
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
