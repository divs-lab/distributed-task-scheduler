package com.portfolio.scheduler.scheduler;

import com.portfolio.scheduler.entity.ScheduledTask;
import com.portfolio.scheduler.entity.TaskExecutionHistory;
import com.portfolio.scheduler.enums.TaskStatus;
import com.portfolio.scheduler.executor.*;
import com.portfolio.scheduler.lock.TaskLockManager;
import com.portfolio.scheduler.repository.ScheduledTaskRepository;
import com.portfolio.scheduler.service.*;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskEngine {
    private static final Logger log = LoggerFactory.getLogger(TaskEngine.class);
    private final ScheduledTaskRepository repository;
    private final TaskService tasks;
    private final ExecutionService executions;
    private final TaskExecutorRegistry registry;
    private final TaskLockManager locks;
    private final ExecutorService executor;
    private final Semaphore capacity;

    public TaskEngine(ScheduledTaskRepository repository, TaskService tasks, ExecutionService executions,
                      TaskExecutorRegistry registry, TaskLockManager locks, ExecutorService taskExecutorService,
                      @Value("${scheduler.execution.max-concurrency:32}") int maxConcurrency) {
        this.repository = repository; this.tasks = tasks; this.executions = executions; this.registry = registry;
        this.locks = locks; this.executor = taskExecutorService; this.capacity = new Semaphore(maxConcurrency);
    }

    @Scheduled(fixedDelayString = "${scheduler.poll-interval-ms:750}")
    public void poll() {
        for (ScheduledTask candidate : repository.findTop100ByStatusAndExecutionTimeLessThanEqualOrderByExecutionTimeAsc(TaskStatus.PENDING, Instant.now())) {
            dispatch(candidate);
        }
    }

    private void dispatch(ScheduledTask candidate) {
        if (!capacity.tryAcquire()) return;
        Optional<String> lock;
        try { lock = locks.acquire(candidate.getId()); }
        catch (RuntimeException e) { capacity.release(); return; }
        if (lock.isEmpty()) { capacity.release(); return; }
        String token = lock.get();
        boolean claimed = false;
        boolean submitted = false;
        try {
            claimed = tasks.claim(candidate.getId(), Instant.now());
            if (!claimed) return;
            executor.submit(() -> run(candidate.getId(), token));
            submitted = true;
        } catch (RuntimeException e) {
            log.error("Task dispatch failed: taskId={}", candidate.getId(), e);
            if (claimed) tasks.releaseClaim(candidate.getId());
        } finally {
            if (!submitted) { locks.release(candidate.getId(), token); capacity.release(); }
        }
    }

    private void run(Long taskId, String token) {
        try {
            ScheduledTask task = tasks.get(taskId);
            TaskExecutionHistory attempt = executions.start(taskId);
            try {
                registry.get(task.getTaskType()).execute(task);
                executions.succeeded(taskId, attempt.getId());
            } catch (Exception e) {
                log.warn("Task execution failed: taskId={}, type={}", taskId, task.getTaskType(), e);
                executions.failed(taskId, attempt.getId(), safeMessage(e));
            }
        } catch (Exception e) {
            log.error("Task worker failed before completion was persisted: taskId={}", taskId, e);
            try { tasks.releaseClaim(taskId); } catch (Exception releaseError) { log.error("Could not restore task after worker failure: taskId={}", taskId, releaseError); }
        } finally {
            locks.release(taskId, token);
            capacity.release();
        }
    }
    private String safeMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : message.substring(0, Math.min(1900, message.length()));
    }
}
