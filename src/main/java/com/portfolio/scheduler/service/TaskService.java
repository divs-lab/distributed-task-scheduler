package com.portfolio.scheduler.service;

import com.portfolio.scheduler.dto.ScheduleTaskRequest;
import com.portfolio.scheduler.entity.ScheduledTask;
import com.portfolio.scheduler.enums.TaskStatus;
import com.portfolio.scheduler.exception.InvalidTaskStateException;
import com.portfolio.scheduler.exception.TaskNotFoundException;
import com.portfolio.scheduler.repository.ScheduledTaskRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class TaskService {
    private final ScheduledTaskRepository repository;
    public TaskService(ScheduledTaskRepository repository) { this.repository = repository; }

    @Transactional
    public ScheduledTask schedule(ScheduleTaskRequest request) {
        return repository.save(new ScheduledTask(request.taskType(), request.executionTime(), request.maxRetries()));
    }
    @Transactional(readOnly = true)
    public ScheduledTask get(Long id) { return repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id)); }
    @Transactional(readOnly = true)
    public Page<ScheduledTask> list(TaskStatus status, Pageable pageable) {
        return status == null ? repository.findAll(pageable) : repository.findByStatus(status, pageable);
    }
    @Transactional
    public void cancel(Long id) {
        if (!repository.existsById(id)) throw new TaskNotFoundException(id);
        if (repository.cancelPending(id, Instant.now()) != 1) throw new InvalidTaskStateException("Only pending tasks can be cancelled");
    }
    @Transactional
    public ScheduledTask retry(Long id) {
        ScheduledTask task = get(id);
        try { task.retryManually(Instant.now()); }
        catch (IllegalStateException ex) { throw new InvalidTaskStateException("Only failed tasks can be retried"); }
        return repository.save(task);
    }
    @Transactional
    public boolean claim(Long id, Instant now) { return repository.claim(id, now) == 1; }
    @Transactional
    public void releaseClaim(Long id) {
        ScheduledTask task = get(id);
        if (task.getStatus() == TaskStatus.RUNNING) {
            task.restoreAfterDispatchFailure(Instant.now().plusSeconds(1));
            repository.save(task);
        }
    }
}
