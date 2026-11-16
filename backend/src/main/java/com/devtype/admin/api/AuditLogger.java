package com.devtype.admin.api;

public interface AuditLogger {
    void log(Long actorId, String action, String resourceType, String resourceId, String details);
}
