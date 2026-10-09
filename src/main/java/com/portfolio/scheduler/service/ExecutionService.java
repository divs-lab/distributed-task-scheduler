package com.portfolio.scheduler.service;

import com.portfolio.scheduler.entity.ScheduledTask;
import com.portfolio.scheduler.entity.TaskExecutionHistory;
import com.portfolio.scheduler.exception.TaskNotFoundException;
import com.portfolio.scheduler.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class ExecutionService {
    private final ScheduledTaskRepository tasks;
    private final TaskExecutionHistoryRepository history;
    private final RetryPolicy retryPolicy;
    public ExecutionService(ScheduledTaskRepository tasks, TaskExecutionHistoryRepository history, RetryPolicy retryPolicy) {
        this.tasks = tasks; this.history = history; this.retryPolicy = retryPolicy;
    }
    @Transactional
    public TaskExecutionHistory start(Long id) {
        ScheduledTask task = tasks.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        // History attempt numbers span manual retry cycles, even when retryCount resets.
        int attemptNumber = Math.toIntExact(history.countByTaskId(id) + 1);
        return history.save(new TaskExecutionHistory(task, attemptNumber, Instant.now()));
    }
    @Transactional
    public void succeeded(Long taskId, Long historyId) {
        ScheduledTask task = tasks.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        TaskExecutionHistory attempt = history.findById(historyId).orElseThrow();
        task.markSucceeded(); attempt.finish("SUCCESS", null, Instant.now());
    }
    @Transactional
    public void failed(Long taskId, Long historyId, String message) {
        ScheduledTask task = tasks.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        TaskExecutionHistory attempt = history.findById(historyId).orElseThrow();
        Instant now = Instant.now(); task.markFailure(message, retryPolicy.nextAttemptAt(now, task.getRetryCount() + 1));
        attempt.finish("FAILED", message, now);
    }
    @Transactional(readOnly = true)
    public List<TaskExecutionHistory> history(Long id) {
        if (!tasks.existsById(id)) throw new TaskNotFoundException(id);
        return history.findByTaskIdOrderByAttemptNumberAsc(id);
    }
}
