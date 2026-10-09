package com.portfolio.scheduler.dto;

import com.portfolio.scheduler.entity.TaskExecutionHistory;
import java.time.Instant;

public record HistoryResponse(Long id, int attemptNumber, Instant startTime, Instant endTime,
                              String outcome, String errorMessage) {
    public static HistoryResponse from(TaskExecutionHistory h) {
        return new HistoryResponse(h.getId(), h.getAttemptNumber(), h.getStartTime(), h.getEndTime(), h.getOutcome(), h.getErrorMessage());
    }
}
