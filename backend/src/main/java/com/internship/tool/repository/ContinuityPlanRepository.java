package com.internship.tool.repository;

import com.internship.tool.entity.ContinuityPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContinuityPlanRepository extends JpaRepository<ContinuityPlan, Long> {

    // ── Basic finders (exclude soft-deleted) ──────────────────────────────
    List<ContinuityPlan> findByDeletedFalse();
    Page<ContinuityPlan> findByDeletedFalse(Pageable pageable);
    Optional<ContinuityPlan> findByIdAndDeletedFalse(Long id);
    List<ContinuityPlan> findByDeletedFalseOrderByCreatedAtDesc();

    // ── Search finders ─────────────────────────────────────────────────────
    List<ContinuityPlan> findByTitleContainingIgnoreCaseAndDeletedFalse(String keyword);
    List<ContinuityPlan> findByStatusAndDeletedFalse(String status);
    List<ContinuityPlan> findByTitleContainingIgnoreCaseAndStatusAndDeletedFalse(String keyword, String status);

    // ── Flexible multi-field search with pagination ────────────────────────
    @Query("SELECT p FROM ContinuityPlan p WHERE p.deleted = false " +
           "AND (:q IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:department IS NULL OR p.department = :department)")
    Page<ContinuityPlan> search(@Param("q") String q,
                                @Param("status") String status,
                                @Param("department") String department,
                                Pageable pageable);

    // ── Stats queries ──────────────────────────────────────────────────────
    long countByDeletedFalse();
    long countByStatusAndDeletedFalse(String status);
    long countByPriorityAndDeletedFalse(String priority);

    @Query("SELECT AVG(p.score) FROM ContinuityPlan p WHERE p.deleted = false")
    Double findAverageScore();

    @Query("SELECT p.department, COUNT(p) FROM ContinuityPlan p " +
           "WHERE p.deleted = false AND p.department IS NOT NULL " +
           "GROUP BY p.department ORDER BY COUNT(p) DESC")
    List<Object[]> countByDepartment();
}
