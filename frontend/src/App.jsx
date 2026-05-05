import { Routes, Route, Link, useLocation, Navigate } from "react-router-dom";
import { useAuth } from "./context/AuthContext.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import Login from "./pages/Login.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import Plans from "./pages/Plans.jsx";
import Analytics from "./pages/Analytics.jsx";
import PlanDetail from "./pages/PlanDetail.jsx";

function Sidebar() {
  const { user, logout } = useAuth();
  const { pathname } = useLocation();

  const navLink = (to, label, icon) => (
    <Link to={to} className={`sidebar-link${pathname === to ? " active" : ""}`}>
      <span className="sidebar-icon">{icon}</span> {label}
    </Link>
  );

  return (
    <div className="sidebar">
      <div className="sidebar-brand">
        <span className="brand-icon">🛡️</span>
        <span>BCP Tool</span>
      </div>

      <nav className="sidebar-nav">
        {navLink("/", "Dashboard", "📊")}
        {navLink("/plans", "Plans", "📋")}
        {navLink("/analytics", "Analytics", "📈")}
        {navLink("/audit", "Audit Log", "🔍")}
      </nav>

      <div className="sidebar-footer">
        <div className="sidebar-user">
          <span className="user-avatar">{user?.username?.[0]?.toUpperCase()}</span>
          <div>
            <div className="user-name">{user?.username}</div>
            <div className="user-role">{user?.role}</div>
          </div>
        </div>
        <button className="logout-btn" onClick={logout}>Logout</button>
      </div>
    </div>
  );
}

function AppLayout({ children }) {
  return (
    <div className="app-layout">
      <Sidebar />
      <div className="app-main">{children}</div>
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route path="/" element={
        <ProtectedRoute>
          <AppLayout><Dashboard /></AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/plans" element={
        <ProtectedRoute>
          <AppLayout><Plans /></AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/plans/:id" element={
        <ProtectedRoute>
          <AppLayout><PlanDetail /></AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/analytics" element={
        <ProtectedRoute>
          <AppLayout><Analytics /></AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/audit" element={
        <ProtectedRoute>
          <AppLayout>
            <div className="page">
              <div className="page-header"><h2>Audit Log</h2></div>
              <div className="card"><p style={{color:"#888"}}>Audit log is available via <code>GET /api/audit-log</code></p></div>
            </div>
          </AppLayout>
        </ProtectedRoute>
      } />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
