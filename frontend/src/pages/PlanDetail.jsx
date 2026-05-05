import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import api from "../services/api";
import { ScoreBadge } from "./Dashboard.jsx";

export default function PlanDetail() {
  const { id }         = useParams();
  const navigate       = useNavigate();
  const [plan, setPlan] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState("");
  const [aiRec, setAiRec]     = useState(null);
  const [aiLoading, setAiLoading] = useState(false);

  useEffect(() => {
    api.get(`/api/plans/${id}`)
      .then(res => setPlan(res.data))
      .catch(() => setError("Plan not found."))
      .finally(() => setLoading(false));
  }, [id]);

  const handleDelete = async () => {
    if (!confirm("Delete this plan?")) return;
    await api.delete(`/api/plans/${id}`);
    navigate("/plans");
  };

  const handleAiRecommend = async () => {
    setAiLoading(true);
    setAiRec(null);
    try {
      const res = await api.post("http://localhost:5000/recommend", { text: plan.description || plan.title });
      setAiRec(res.data);
    } catch {
      setAiRec({ error: "AI service unavailable." });
    } finally {
      setAiLoading(false);
    }
  };

  if (loading) return <div className="page"><div className="loading-center">Loading…</div></div>;
  if (error)   return <div className="page"><div className="alert alert-error">{error}</div></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <button className="btn-ghost" onClick={() => navigate(-1)}>← Back</button>
          <h2 style={{ marginTop: 8 }}>{plan.title}</h2>
        </div>
        <div className="detail-actions">
          <Link to={`/plans`} state={{ editId: plan.id }} className="button btn-outline">Edit</Link>
          <button className="button btn-danger" onClick={handleDelete}>Delete</button>
        </div>
      </div>

      <div className="detail-grid">
        {/* Main info */}
        <div className="card">
          <h3>Plan Details</h3>
          <dl className="detail-list">
            <dt>Description</dt>
            <dd>{plan.description || <em className="muted">No description provided</em>}</dd>

            <dt>Status</dt>
            <dd><span className={`badge badge-${plan.status?.toLowerCase().replace(" ", "-")}`}>{plan.status}</span></dd>

            <dt>Priority</dt>
            <dd><span className={`badge badge-priority-${plan.priority?.toLowerCase()}`}>{plan.priority}</span></dd>

            <dt>Owner</dt>
            <dd>{plan.owner || "—"}</dd>

            <dt>Department</dt>
            <dd>{plan.department || "—"}</dd>
          </dl>
        </div>

        {/* Score + RTO/RPO */}
        <div>
          <div className="card text-center" style={{ marginBottom: 16 }}>
            <h3>Readiness Score</h3>
            <div className="score-display">
              <ScoreBadge score={plan.score} />
              <div className="score-desc">
                {(plan.score ?? 0) >= 80 ? "Good — plan is well prepared"
                 : (plan.score ?? 0) >= 50 ? "Moderate — some gaps to address"
                 : "Poor — needs immediate attention"}
              </div>
            </div>
          </div>

          <div className="card">
            <h3>Recovery Targets</h3>
            <div className="rto-grid">
              <div className="rto-item">
                <div className="rto-label">RTO</div>
                <div className="rto-value">{plan.rtoHours != null ? `${plan.rtoHours}h` : "Not set"}</div>
                <div className="rto-desc">Recovery Time Objective</div>
              </div>
              <div className="rto-item">
                <div className="rto-label">RPO</div>
                <div className="rto-value">{plan.rpoHours != null ? `${plan.rpoHours}h` : "Not set"}</div>
                <div className="rto-desc">Recovery Point Objective</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* AI recommendations panel */}
      <div className="card">
        <div className="card-header">
          <h3>🤖 AI Recommendations</h3>
          <button className="button btn-ai" onClick={handleAiRecommend} disabled={aiLoading}>
            {aiLoading ? "Asking AI…" : "Get Recommendations"}
          </button>
        </div>

        {aiRec ? (
          <div className="ai-panel">
            <pre className="ai-panel-body">{JSON.stringify(aiRec, null, 2)}</pre>
          </div>
        ) : (
          <p className="muted">Click "Get Recommendations" to ask the AI for improvement suggestions.</p>
        )}
      </div>

      {/* Metadata */}
      <div className="card metadata-card">
        <span>Created: {plan.createdAt ? new Date(plan.createdAt).toLocaleString() : "—"}</span>
        <span>Updated: {plan.updatedAt ? new Date(plan.updatedAt).toLocaleString() : "—"}</span>
        <span>ID: {plan.id}</span>
      </div>
    </div>
  );
}
