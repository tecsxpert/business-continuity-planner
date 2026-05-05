package com.internship.tool.controller;

import com.internship.tool.entity.ContinuityPlan;
import com.internship.tool.service.ContinuityPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "*")
@Tag(name = "Business Continuity Plans", description = "Create, read, update, delete and search continuity plans")
public class ContinuityPlanController {

    @Autowired
    private ContinuityPlanService service;

    // ── GET all (paginated) ────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Get all active plans (paginated)")
    public ResponseEntity<Page<ContinuityPlan>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAll(page, size));
    }

    // ── GET by ID ──────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get a single plan by ID — returns 404 if not found")
    public ResponseEntity<ContinuityPlan> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // ── POST create ────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new plan")
    public ResponseEntity<ContinuityPlan> create(@Valid @RequestBody ContinuityPlan plan) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(plan));
    }

    // ── PUT update ─────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing plan")
    public ResponseEntity<ContinuityPlan> update(
            @PathVariable Long id,
            @Valid @RequestBody ContinuityPlan plan) {
        return ResponseEntity.ok(service.update(id, plan));
    }

    // ── DELETE (soft) ──────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete a plan (hidden, not erased)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    // ── GET search ─────────────────────────────────────────────────────────

    @GetMapping("/search")
    @Operation(summary = "Search by keyword, status, department with pagination")
    public ResponseEntity<Page<ContinuityPlan>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.search(q, status, department, page, size));
    }

    // ── GET stats (dashboard KPIs) ─────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics (counts, avg score, by-department)")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(service.getStats());
    }

    // ── GET export CSV ─────────────────────────────────────────────────────

    @GetMapping("/export")
    @Operation(summary = "Export all plans as a CSV file download")
    public ResponseEntity<byte[]> exportCsv() {
        List<ContinuityPlan> plans = service.getAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Description,Status,Priority,Owner,Department,RTO (hrs),RPO (hrs),Score,Created\n");

        for (ContinuityPlan p : plans) {
            csv.append(String.format("%d,\"%s\",\"%s\",%s,%s,\"%s\",%s,%s,%s,%d,%s\n",
                    p.getId(),
                    csvEscape(p.getTitle()),
                    csvEscape(p.getDescription()),
                    nvl(p.getStatus()),
                    nvl(p.getPriority()),
                    csvEscape(p.getOwner()),
                    nvl(p.getDepartment()),
                    p.getRtoHours() != null ? p.getRtoHours() : "",
                    p.getRpoHours() != null ? p.getRpoHours() : "",
                    p.getScore() != null ? p.getScore() : 0,
                    p.getCreatedAt() != null ? p.getCreatedAt().toString() : ""));
        }

        byte[] bytes = csv.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "continuity-plans.csv");
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    // ── POST upload (file attachment validation) ───────────────────────────

    @PostMapping("/upload")
    @Operation(summary = "Upload a file attachment (max 5 MB, any type)")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        long maxBytes = 5L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            return ResponseEntity.badRequest().body(Map.of("error", "File exceeds 5 MB limit"));
        }
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        String type = file.getContentType() != null ? file.getContentType() : "unknown";
        return ResponseEntity.ok(Map.of(
                "message", "File received successfully",
                "filename", name,
                "size", file.getSize(),
                "contentType", type));
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private String csvEscape(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }

    private String nvl(String val) {
        return val != null ? val : "";
    }
}
