package com.portfolio.scheduler.entity;

import com.portfolio.scheduler.enums.TaskStatus;
import com.portfolio.scheduler.enums.TaskType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "scheduled_tasks", indexes = {
        @Index(name = "idx_task_due", columnList = "status, execution_time")
})
public class ScheduledTask {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private TaskType taskType;
    @Column(name = "execution_time", nullable = false)
    private Instant executionTime;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private TaskStatus status = TaskStatus.PENDING;
    @Column(nullable = false)
    private int retryCount;
    @Column(nullable = false)
    private int maxRetries;
    @Column(length = 2000)
    private String lastError;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected ScheduledTask() { }

    public ScheduledTask(TaskType taskType, Instant executionTime, int maxRetries) {
        this.taskType = taskType;
        this.executionTime = executionTime;
        this.maxRetries = maxRetries;
    }

    @PrePersist void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }

    public void markRunning(Instant now) {
        requireStatus(TaskStatus.PENDING);
        if (executionTime.isAfter(now)) throw new IllegalStateException("Task is not due yet");
        status = TaskStatus.RUNNING;
    }
    public void markSucceeded() { requireStatus(TaskStatus.RUNNING); status = TaskStatus.SUCCESS; lastError = null; }
    public void markFailure(String error, Instant retryAt) {
        requireStatus(TaskStatus.RUNNING);
        lastError = error;
        if (retryCount < maxRetries) { retryCount++; executionTime = retryAt; status = TaskStatus.PENDING; }
        else { status = TaskStatus.FAILED; }
    }
    public void retryManually(Instant when) {
        requireStatus(TaskStatus.FAILED); status = TaskStatus.PENDING; retryCount = 0; executionTime = when; lastError = null;
    }
    public void cancel() { requireStatus(TaskStatus.PENDING); status = TaskStatus.CANCELLED; }
    public void restoreAfterDispatchFailure(Instant retryAt) { requireStatus(TaskStatus.RUNNING); status = TaskStatus.PENDING; executionTime = retryAt; }
    private void requireStatus(TaskStatus expected) {
        if (status != expected) throw new IllegalStateException("Task must be " + expected + " but was " + status);
    }

    public Long getId() { return id; }
    public TaskType getTaskType() { return taskType; }
    public Instant getExecutionTime() { return executionTime; }
    public TaskStatus getStatus() { return status; }
    public int getRetryCount() { return retryCount; }
    public int getMaxRetries() { return maxRetries; }
    public String getLastError() { return lastError; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
