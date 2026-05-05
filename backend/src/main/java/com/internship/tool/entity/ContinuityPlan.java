package com.internship.tool.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "continuity_plans")
@EntityListeners(AuditingEntityListener.class)
public class ContinuityPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be under 255 characters")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Status is required")
    @Column(nullable = false)
    private String status = "Pending";

    @Column
    private String priority = "Medium";

    @Column
    private String owner;

    @Column
    private String department;

    // Recovery Time Objective — how fast we must recover (hours)
    @Column
    private Integer rtoHours;

    // Recovery Point Objective — how much data loss is acceptable (hours)
    @Column
    private Integer rpoHours;

    // Readiness score 0–100
    @Column
    private Integer score = 50;

    // Soft-delete flag — deleted records are hidden, not erased
    @Column(nullable = false)
    private Boolean deleted = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public ContinuityPlan() {}

    public ContinuityPlan(String title, String description, String status,
                          String priority, String owner, String department,
                          Integer rtoHours, Integer rpoHours, Integer score) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.owner = owner;
        this.department = department;
        this.rtoHours = rtoHours;
        this.rpoHours = rpoHours;
        this.score = score;
        this.deleted = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getRtoHours() { return rtoHours; }
    public void setRtoHours(Integer rtoHours) { this.rtoHours = rtoHours; }

    public Integer getRpoHours() { return rpoHours; }
    public void setRpoHours(Integer rpoHours) { this.rpoHours = rpoHours; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
