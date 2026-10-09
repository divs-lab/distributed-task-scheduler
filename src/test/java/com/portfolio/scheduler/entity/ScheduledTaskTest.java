package com.portfolio.scheduler.entity;

import com.portfolio.scheduler.enums.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

class ScheduledTaskTest {
    @Test void startsPendingAndRequiresDueTimeToRun() {
        ScheduledTask task = new ScheduledTask(TaskType.EMAIL, Instant.parse("2026-01-01T00:00:00Z"), 1);
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertThrows(IllegalStateException.class, () -> task.markSucceeded());
        task.markRunning(Instant.parse("2026-01-01T00:00:00Z"));
        task.markSucceeded();
        assertEquals(TaskStatus.SUCCESS, task.getStatus());
        assertThrows(IllegalStateException.class, () -> task.markRunning(Instant.now()));
    }
    @Test void failureUsesBoundedRetryThenBecomesTerminal() {
        ScheduledTask task = new ScheduledTask(TaskType.ALERT, Instant.EPOCH, 1);
        task.markRunning(Instant.now());
        task.markFailure("temporary", Instant.parse("2026-01-01T00:00:00Z"));
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(1, task.getRetryCount());
        task.markRunning(Instant.parse("2026-01-01T00:00:01Z"));
        task.markFailure("permanent", Instant.parse("2026-01-01T00:00:02Z"));
        assertEquals(TaskStatus.FAILED, task.getStatus());
        assertEquals(1, task.getRetryCount());
        assertThrows(IllegalStateException.class, () -> task.markSucceeded());
    }
    @Test void manualRetryResetsRetryCycle() {
        ScheduledTask task = new ScheduledTask(TaskType.CLEANUP, Instant.EPOCH, 0);
        task.markRunning(Instant.now()); task.markFailure("broken", Instant.now());
        task.retryManually(Instant.now());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals(0, task.getRetryCount());
        assertNull(task.getLastError());
    }
}
