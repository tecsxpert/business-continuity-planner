package com.internship.tool.controller;

import com.internship.tool.entity.AuditLog;
import com.internship.tool.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-log")
@CrossOrigin(origins = "*")
@Tag(name = "Audit Log", description = "Read-only trail of all create/update/delete operations")
public class AuditLogController {

    @Autowired
    private AuditLogService service;

    @GetMapping
    @Operation(summary = "Get all audit log entries (paginated, newest first)")
    public ResponseEntity<Page<AuditLog>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getAll(page, size));
    }

    @GetMapping("/plan/{id}")
    @Operation(summary = "Get audit history for a specific plan")
    public ResponseEntity<List<AuditLog>> getByPlan(@PathVariable Long id) {
        return ResponseEntity.ok(service.getByEntity("ContinuityPlan", id));
    }
}
