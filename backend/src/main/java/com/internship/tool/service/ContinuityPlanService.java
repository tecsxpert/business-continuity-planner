package com.internship.tool.service;

import com.internship.tool.entity.ContinuityPlan;
import com.internship.tool.exception.ResourceNotFoundException;
import com.internship.tool.repository.ContinuityPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContinuityPlanService {

    @Autowired
    private ContinuityPlanRepository repo;

    // ── Read ──────────────────────────────────────────────────────────────

    public Page<ContinuityPlan> getAll(int page, int size) {
        return repo.findByDeletedFalse(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public List<ContinuityPlan> getAll() {
        return repo.findByDeletedFalseOrderByCreatedAtDesc();
    }

    public ContinuityPlan getById(Long id) {
        return repo.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + id));
    }

    // ── Write ─────────────────────────────────────────────────────────────

    public ContinuityPlan create(ContinuityPlan plan) {
        plan.setDeleted(false);
        if (plan.getStatus() == null || plan.getStatus().isBlank()) {
            plan.setStatus("Pending");
        }
        if (plan.getPriority() == null || plan.getPriority().isBlank()) {
            plan.setPriority("Medium");
        }
        if (plan.getScore() == null) {
            plan.setScore(50);
        }
        return repo.save(plan);
    }

    public ContinuityPlan update(Long id, ContinuityPlan updated) {
        ContinuityPlan existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setStatus(updated.getStatus());
        existing.setPriority(updated.getPriority());
        existing.setOwner(updated.getOwner());
        existing.setDepartment(updated.getDepartment());
        existing.setRtoHours(updated.getRtoHours());
        existing.setRpoHours(updated.getRpoHours());
        existing.setScore(updated.getScore());
        return repo.save(existing);
    }

    public void softDelete(Long id) {
        ContinuityPlan plan = getById(id);
        plan.setDeleted(true);
        repo.save(plan);
    }

    // ── Search ────────────────────────────────────────────────────────────

    public Page<ContinuityPlan> search(String q, String status, String department, int page, int size) {
        String qParam      = (q          != null && !q.isBlank())          ? q          : null;
        String statusParam = (status     != null && !status.isBlank())     ? status     : null;
        String deptParam   = (department != null && !department.isBlank()) ? department : null;

        return repo.search(qParam, statusParam, deptParam,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    // ── Stats (used by dashboard) ─────────────────────────────────────────

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long total      = repo.countByDeletedFalse();
        long active     = repo.countByStatusAndDeletedFalse("Active");
        long pending    = repo.countByStatusAndDeletedFalse("Pending");
        long failed     = repo.countByStatusAndDeletedFalse("Failed");
        long underReview = repo.countByStatusAndDeletedFalse("Under Review");
        long highPriority = repo.countByPriorityAndDeletedFalse("High");

        Double rawAvg = repo.findAverageScore();
        double avgScore = rawAvg != null ? Math.round(rawAvg * 10.0) / 10.0 : 0.0;

        stats.put("total", total);
        stats.put("active", active);
        stats.put("pending", pending);
        stats.put("failed", failed);
        stats.put("underReview", underReview);
        stats.put("highPriority", highPriority);
        stats.put("avgScore", avgScore);

        // Department breakdown for bar chart
        Map<String, Long> byDepartment = new LinkedHashMap<>();
        for (Object[] row : repo.countByDepartment()) {
            if (row[0] != null) {
                byDepartment.put((String) row[0], (Long) row[1]);
            }
        }
        stats.put("byDepartment", byDepartment);

        return stats;
    }
}
