package com.internship.tool.service;

import com.internship.tool.entity.AuditLog;
import com.internship.tool.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository repo;

    @Async
    public void log(String action, String entityType, Long entityId, String details) {
        repo.save(new AuditLog(action, entityType, entityId, details));
    }

    public Page<AuditLog> getAll(int page, int size) {
        return repo.findAllByOrderByPerformedAtDesc(
                PageRequest.of(page, size, Sort.by("performedAt").descending()));
    }

    public List<AuditLog> getByEntity(String entityType, Long entityId) {
        return repo.findByEntityTypeAndEntityIdOrderByPerformedAtDesc(entityType, entityId);
    }
}
