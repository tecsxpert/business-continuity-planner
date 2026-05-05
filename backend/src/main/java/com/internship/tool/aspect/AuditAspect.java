package com.internship.tool.aspect;

import com.internship.tool.entity.ContinuityPlan;
import com.internship.tool.service.AuditLogService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Automatically logs every CREATE, UPDATE, and DELETE to the audit_log table.
 * Uses Spring AOP — no manual logging needed in the service.
 */
@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditLogService auditLogService;

    @AfterReturning(
            pointcut = "execution(* com.internship.tool.service.ContinuityPlanService.create(..))",
            returning = "result")
    public void afterCreate(JoinPoint jp, Object result) {
        if (result instanceof ContinuityPlan plan) {
            auditLogService.log("CREATE", "ContinuityPlan", plan.getId(),
                    "Created plan: \"" + plan.getTitle() + "\"");
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.internship.tool.service.ContinuityPlanService.update(..))",
            returning = "result")
    public void afterUpdate(JoinPoint jp, Object result) {
        if (result instanceof ContinuityPlan plan) {
            auditLogService.log("UPDATE", "ContinuityPlan", plan.getId(),
                    "Updated plan: \"" + plan.getTitle() + "\" → status=" + plan.getStatus());
        }
    }

    @AfterReturning(
            pointcut = "execution(* com.internship.tool.service.ContinuityPlanService.softDelete(..))")
    public void afterDelete(JoinPoint jp) {
        Object[] args = jp.getArgs();
        if (args.length > 0 && args[0] instanceof Long id) {
            auditLogService.log("DELETE", "ContinuityPlan", id,
                    "Soft-deleted plan ID: " + id);
        }
    }
}
