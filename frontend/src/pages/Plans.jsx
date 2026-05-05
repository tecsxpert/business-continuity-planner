import { useEffect, useState, useCallback, useRef } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import { ScoreBadge } from "./Dashboard.jsx";

const STATUSES    = ["", "Active", "Pending", "Failed", "Under Review"];
const PRIORITIES  = ["", "High", "Medium", "Low"];
const DEPARTMENTS = ["", "IT", "Operations", "Finance", "HR", "Legal", "Facilities", "Management"];

const EMPTY_FORM = {
  title: "", description: "", status: "Pending", priority: "Medium",
  owner: "", department: "", rtoHours: "", rpoHours: "", score: 50
};

export default function Plans() {
  const navigate = useNavigate();

  // ── List / search state ─────────────────────────────────────────────────
  const [plans, setPlans]           = useState([]);
  const [total, setTotal]           = useState(0);
  const [page, setPage]             = useState(0);
  const [search, setSearch]         = useState("");
  const [statusFilter, setStatus]   = useState("");
  const [deptFilter, setDept]       = useState("");
  const [loading, setLoading]       = useState(false);

  // ── Form state ──────────────────────────────────────────────────────────
  const [form, setForm]             = useState(EMPTY_FORM);
  const [editingId, setEditingId]   = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError]   = useState("");

  // ── AI panel state ──────────────────────────────────────────────────────
  const [aiResult, setAiResult]     = useState(null);
  const [aiLoading, setAiLoading]   = useState(false);

  // ── Debounce search ─────────────────────────────────────────────────────
  const debounceRef = useRef(null);
  const PAGE_SIZE = 10;

  const fetchPlans = useCallback((p = 0, q = search, s = statusFilter, d = deptFilter) => {
    setLoading(true);
    const params = new URLSearchParams({ page: p, size: PAGE_SIZE });
    if (q) params.set("q", q);
    if (s) params.set("status", s);
    if (d) params.set("department", d);

    const url = q || s || d ? `/api/plans/search?${params}` : `/api/plans?${params}`;
    api.get(url)
      .then(res => {
        setPlans(res.data.content || res.data);
        setTotal(res.data.totalElements ?? (res.data.length ?? 0));
        setPage(p);
      })
      .finally(() => setLoading(false));
  }, [search, statusFilter, deptFilter]);

  useEffect(() => { fetchPlans(0); }, []);

  // Debounce text search (500ms)
  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => fetchPlans(0, search, statusFilter, deptFilter), 500);
    return () => clearTimeout(debounceRef.current);
  }, [search]);

  // Immediate filter on dropdown change
  useEffect(() => { fetchPlans(0, search, statusFilter, deptFilter); }, [statusFilter, deptFilter]);

  // ── CRUD handlers ────────────────────────────────────────────────────────

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError("");
    setSubmitting(true);
    const payload = {
      ...form,
      rtoHours: form.rtoHours !== "" ? Number(form.rtoHours) : null,
      rpoHours: form.rpoHours !== "" ? Number(form.rpoHours) : null,
      score:    Number(form.score),
    };
    try {
      if (editingId) {
        await api.put(`/api/plans/${editingId}`, payload);
      } else {
        await api.post("/api/plans", payload);
      }
      resetForm();
      fetchPlans(0);
    } catch (err) {
      const msg = err.response?.data?.message
        || Object.values(err.response?.data?.fieldErrors || {}).join(", ")
        || "Error saving plan";
      setFormError(msg);
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (plan) => {
    setEditingId(plan.id);
    setForm({
      title: plan.title || "",
      description: plan.description || "",
      status: plan.status || "Pending",
      priority: plan.priority || "Medium",
      owner: plan.owner || "",
      department: plan.department || "",
      rtoHours: plan.rtoHours ?? "",
      rpoHours: plan.rpoHours ?? "",
      score: plan.score ?? 50,
    });
    setFormError("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleDelete = async (id) => {
    if (!confirm("Delete this plan? (It will be soft-deleted and hidden.)")) return;
    await api.delete(`/api/plans/${id}`);
    fetchPlans(page);
  };

  const resetForm = () => {
    setForm(EMPTY_FORM);
    setEditingId(null);
    setFormError("");
  };

  const handleExport = () => {
    const base = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
    window.open(`${base}/api/plans/export`, "_blank");
  };

  // ── AI Describe ──────────────────────────────────────────────────────────
  const handleAiDescribe = async () => {
    if (!form.title.trim()) { setFormError("Enter a title first"); return; }
    setAiLoading(true);
    setAiResult(null);
    try {
      const res = await api.post("http://localhost:5000/describe", { text: form.title });
      setAiResult({ type: "describe", data: res.data });
    } catch {
      setAiResult({ type: "error", data: "AI service unavailable. Start the Python service on port 5000." });
    } finally {
      setAiLoading(false);
    }
  };

  const totalPages = Math.ceil(total / PAGE_SIZE);

  return (
    <div className="page">
      <div className="page-header">
        <h2>Business Continuity Plans</h2>
        <button className="button btn-outline" onClick={handleExport}>⬇ Export CSV</button>
      </div>

      {/* ── Create / Edit Form ─────────────────────────────────────────── */}
      <div className="card">
        <div className="card-header">
          <h3>{editingId ? "✏️ Edit Plan" : "➕ Create New Plan"}</h3>
          {editingId && <button className="btn-ghost" onClick={resetForm}>Cancel</button>}
        </div>

        {formError && <div className="alert alert-error">{formError}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-grid-2">
            <div className="form-group">
              <label>Title <span className="req">*</span></label>
              <input className="input" placeholder="e.g. IT Disaster Recovery Plan"
                value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} required />
            </div>
            <div className="form-group">
              <label>Owner</label>
              <input className="input" placeholder="Responsible person"
                value={form.owner} onChange={e => setForm({ ...form, owner: e.target.value })} />
            </div>
          </div>

          <div className="form-group">
            <label>Description</label>
            <textarea className="input textarea" rows={3} placeholder="What does this plan cover?"
              value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
          </div>

          <div className="form-grid-4">
            <div className="form-group">
              <label>Status <span className="req">*</span></label>
              <select className="input" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
                {STATUSES.filter(Boolean).map(s => <option key={s}>{s}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Priority</label>
              <select className="input" value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}>
                {PRIORITIES.filter(Boolean).map(p => <option key={p}>{p}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Department</label>
              <select className="input" value={form.department} onChange={e => setForm({ ...form, department: e.target.value })}>
                {DEPARTMENTS.map(d => <option key={d} value={d}>{d || "Select…"}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Score (0–100)</label>
              <input className="input" type="number" min={0} max={100}
                value={form.score} onChange={e => setForm({ ...form, score: e.target.value })} />
            </div>
          </div>

          <div className="form-grid-2">
            <div className="form-group">
              <label>RTO (hours)</label>
              <input className="input" type="number" min={0} placeholder="Recovery Time Objective"
                value={form.rtoHours} onChange={e => setForm({ ...form, rtoHours: e.target.value })} />
            </div>
            <div className="form-group">
              <label>RPO (hours)</label>
              <input className="input" type="number" min={0} placeholder="Recovery Point Objective"
                value={form.rpoHours} onChange={e => setForm({ ...form, rpoHours: e.target.value })} />
            </div>
          </div>

          <div className="form-actions">
            <button type="submit" className="button" disabled={submitting}>
              {submitting ? "Saving…" : editingId ? "Update Plan" : "+ Add Plan"}
            </button>
            <button type="button" className="button btn-ai" onClick={handleAiDescribe} disabled={aiLoading}>
              {aiLoading ? "AI thinking…" : "🤖 AI Describe"}
            </button>
          </div>
        </form>

        {/* AI response panel */}
        {aiResult && (
          <div className={`ai-panel ${aiResult.type === "error" ? "ai-error" : ""}`}>
            <div className="ai-panel-header">🤖 AI Response</div>
            <pre className="ai-panel-body">
              {typeof aiResult.data === "string"
                ? aiResult.data
                : JSON.stringify(aiResult.data, null, 2)}
            </pre>
          </div>
        )}
      </div>

      {/* ── Search & Filter Bar ────────────────────────────────────────── */}
      <div className="filter-bar">
        <input className="input filter-search" placeholder="🔍 Search by title or description…"
          value={search} onChange={e => setSearch(e.target.value)} />
        <select className="input filter-select" value={statusFilter} onChange={e => setStatus(e.target.value)}>
          {STATUSES.map(s => <option key={s} value={s}>{s || "All statuses"}</option>)}
        </select>
        <select className="input filter-select" value={deptFilter} onChange={e => setDept(e.target.value)}>
          {DEPARTMENTS.map(d => <option key={d} value={d}>{d || "All departments"}</option>)}
        </select>
        {(search || statusFilter || deptFilter) && (
          <button className="btn-ghost" onClick={() => { setSearch(""); setStatus(""); setDept(""); fetchPlans(0, "", "", ""); }}>
            Clear
          </button>
        )}
      </div>

      {/* ── Table ─────────────────────────────────────────────────────── */}
      <div className="card">
        <div className="card-header">
          <h3>All Plans <span className="count-badge">{total}</span></h3>
        </div>

        {loading ? (
          <div className="table-loading">Loading…</div>
        ) : (
          <div className="table-wrapper">
            <table className="table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Department</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>RTO</th>
                  <th>Score</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {plans.length === 0 ? (
                  <tr><td colSpan="7" className="empty-cell">No plans found</td></tr>
                ) : (
                  plans.map(plan => (
                    <tr key={plan.id}>
                      <td className="td-title">{plan.title}</td>
                      <td>{plan.department || "—"}</td>
                      <td><span className={`badge badge-priority-${plan.priority?.toLowerCase()}`}>{plan.priority || "—"}</span></td>
                      <td><span className={`badge badge-${plan.status?.toLowerCase().replace(" ", "-")}`}>{plan.status}</span></td>
                      <td>{plan.rtoHours != null ? `${plan.rtoHours}h` : "—"}</td>
                      <td><ScoreBadge score={plan.score} /></td>
                      <td className="td-actions">
                        <button className="btn-view"   onClick={() => navigate(`/plans/${plan.id}`)}>View</button>
                        <button className="edit-btn"   onClick={() => handleEdit(plan)}>Edit</button>
                        <button className="delete-btn" onClick={() => handleDelete(plan.id)}>Delete</button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="pagination">
            <button className="page-btn" onClick={() => fetchPlans(page - 1)} disabled={page === 0}>← Prev</button>
            {[...Array(Math.min(totalPages, 7))].map((_, i) => {
              const p = totalPages <= 7 ? i : Math.max(0, page - 3) + i;
              if (p >= totalPages) return null;
              return (
                <button key={p} className={`page-btn${p === page ? " active" : ""}`}
                  onClick={() => fetchPlans(p)}>{p + 1}</button>
              );
            })}
            <button className="page-btn" onClick={() => fetchPlans(page + 1)} disabled={page >= totalPages - 1}>Next →</button>
            <span className="page-info">Page {page + 1} of {totalPages}</span>
          </div>
        )}
      </div>
    </div>
  );
}
