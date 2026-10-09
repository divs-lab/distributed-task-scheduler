package com.portfolio.scheduler.dto;

import com.portfolio.scheduler.enums.TaskType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ScheduleTaskRequest(@NotNull TaskType taskType, @NotNull Instant executionTime,
                                 @NotNull @Min(0) @Max(20) Integer maxRetries) { }
