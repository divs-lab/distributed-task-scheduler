package com.portfolio.scheduler.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "task_execution_history", indexes = @Index(name = "idx_history_task_attempt", columnList = "task_id, attempt_number"))
public class TaskExecutionHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "task_id", nullable = false)
    private ScheduledTask task;
    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    @Column(name = "end_time")
    private Instant endTime;
    @Column(nullable = false, length = 16)
    private String outcome;
    @Column(length = 2000)
    private String errorMessage;

    protected TaskExecutionHistory() { }
    public TaskExecutionHistory(ScheduledTask task, int attemptNumber, Instant startTime) {
        this.task = task; this.attemptNumber = attemptNumber; this.startTime = startTime; this.outcome = "RUNNING";
    }
    public void finish(String result, String error, Instant endedAt) { outcome = result; errorMessage = error; endTime = endedAt; }
    public Long getId() { return id; }
    public int getAttemptNumber() { return attemptNumber; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public String getOutcome() { return outcome; }
    public String getErrorMessage() { return errorMessage; }
}
