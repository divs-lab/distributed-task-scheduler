package com.portfolio.scheduler.controller;

import com.portfolio.scheduler.dto.*;
import com.portfolio.scheduler.enums.TaskStatus;
import com.portfolio.scheduler.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {
    private final TaskService tasks;
    private final ExecutionService executions;
    public TaskController(TaskService tasks, ExecutionService executions) { this.tasks = tasks; this.executions = executions; }

    @PostMapping("/schedule")
    ResponseEntity<TaskResponse> schedule(@Valid @RequestBody ScheduleTaskRequest request) {
        TaskResponse response = TaskResponse.from(tasks.schedule(request));
        return ResponseEntity.accepted().location(URI.create("/api/v1/tasks/" + response.id())).body(response);
    }
    @GetMapping("/{id}") TaskResponse get(@PathVariable Long id) { return TaskResponse.from(tasks.get(id)); }
    @GetMapping
    Object list(@RequestParam(required = false) TaskStatus status,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page must be non-negative and size must be between 1 and 100");
        return tasks.list(status, PageRequest.of(page, size, Sort.by("createdAt").descending())).map(TaskResponse::from);
    }
    @DeleteMapping("/{id}") ResponseEntity<Void> cancel(@PathVariable Long id) { tasks.cancel(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/{id}/retry") TaskResponse retry(@PathVariable Long id) { return TaskResponse.from(tasks.retry(id)); }
    @GetMapping("/{id}/history") List<HistoryResponse> history(@PathVariable Long id) {
        return executions.history(id).stream().map(HistoryResponse::from).toList();
    }
}
