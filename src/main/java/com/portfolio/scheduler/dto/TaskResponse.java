package com.portfolio.scheduler.dto;

import com.portfolio.scheduler.entity.ScheduledTask;
import com.portfolio.scheduler.enums.TaskStatus;
import com.portfolio.scheduler.enums.TaskType;
import java.time.Instant;

public record TaskResponse(Long id, TaskType taskType, Instant executionTime, TaskStatus status,
                           int retryCount, int maxRetries, String lastError, Instant createdAt, Instant updatedAt) {
    public static TaskResponse from(ScheduledTask task) {
        return new TaskResponse(task.getId(), task.getTaskType(), task.getExecutionTime(), task.getStatus(),
                task.getRetryCount(), task.getMaxRetries(), task.getLastError(), task.getCreatedAt(), task.getUpdatedAt());
    }
}
