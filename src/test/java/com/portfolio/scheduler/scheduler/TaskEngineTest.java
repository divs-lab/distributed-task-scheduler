package com.portfolio.scheduler.scheduler;

import com.portfolio.scheduler.entity.*;
import com.portfolio.scheduler.enums.TaskType;
import com.portfolio.scheduler.executor.*;
import com.portfolio.scheduler.lock.TaskLockManager;
import com.portfolio.scheduler.repository.ScheduledTaskRepository;
import com.portfolio.scheduler.service.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TaskEngineTest {
    @Test void lockContentionStopsDispatchBeforeDatabaseClaim() {
        ScheduledTaskRepository repository = mock(ScheduledTaskRepository.class);
        TaskService tasks = mock(TaskService.class);
        TaskLockManager locks = mock(TaskLockManager.class);
        ScheduledTask candidate = mock(ScheduledTask.class);
        when(candidate.getId()).thenReturn(11L);
        when(repository.findTop100ByStatusAndExecutionTimeLessThanEqualOrderByExecutionTimeAsc(any(), any())).thenReturn(List.of(candidate));
        when(locks.acquire(11L)).thenReturn(Optional.empty());

        TaskEngine engine = new TaskEngine(repository, tasks, mock(ExecutionService.class),
                new TaskExecutorRegistry(List.of()), locks, new DirectExecutor(), 1);
        engine.poll();

        verify(tasks, never()).claim(anyLong(), any());
        verify(locks, never()).release(anyLong(), anyString());
    }

    @Test void claimsAndPersistsSuccessThenSafelyReleasesLock() throws Exception {
        ScheduledTaskRepository repository = mock(ScheduledTaskRepository.class);
        TaskService tasks = mock(TaskService.class);
        ExecutionService executions = mock(ExecutionService.class);
        TaskLockManager locks = mock(TaskLockManager.class);
        TaskExecutor executor = mock(TaskExecutor.class);
        when(executor.supports()).thenReturn(TaskType.EMAIL);
        TaskExecutorRegistry registry = new TaskExecutorRegistry(List.of(executor));
        ScheduledTask candidate = mock(ScheduledTask.class);
        ScheduledTask claimed = mock(ScheduledTask.class);
        TaskExecutionHistory history = mock(TaskExecutionHistory.class);
        when(candidate.getId()).thenReturn(17L);
        when(claimed.getTaskType()).thenReturn(TaskType.EMAIL);
        when(history.getId()).thenReturn(31L);
        when(repository.findTop100ByStatusAndExecutionTimeLessThanEqualOrderByExecutionTimeAsc(any(), any())).thenReturn(List.of(candidate));
        when(locks.acquire(17L)).thenReturn(Optional.of("owner-token"));
        when(tasks.claim(eq(17L), any())).thenReturn(true);
        when(tasks.get(17L)).thenReturn(claimed);
        when(executions.start(17L)).thenReturn(history);

        TaskEngine engine = new TaskEngine(repository, tasks, executions, registry, locks, new DirectExecutor(), 1);
        engine.poll();

        verify(executor).execute(claimed);
        verify(executions).succeeded(17L, 31L);
        verify(executions, never()).failed(anyLong(), anyLong(), anyString());
        verify(locks).release(17L, "owner-token");
    }

    @Test void recordsFailureAndRetriesOnlyAfterClaim() throws Exception {
        ScheduledTaskRepository repository = mock(ScheduledTaskRepository.class);
        TaskService tasks = mock(TaskService.class);
        ExecutionService executions = mock(ExecutionService.class);
        TaskLockManager locks = mock(TaskLockManager.class);
        TaskExecutor executor = mock(TaskExecutor.class);
        when(executor.supports()).thenReturn(TaskType.ALERT);
        TaskExecutorRegistry registry = new TaskExecutorRegistry(List.of(executor));
        ScheduledTask candidate = mock(ScheduledTask.class), claimed = mock(ScheduledTask.class);
        TaskExecutionHistory history = mock(TaskExecutionHistory.class);
        when(candidate.getId()).thenReturn(23L); when(claimed.getTaskType()).thenReturn(TaskType.ALERT);
        when(history.getId()).thenReturn(32L);
        when(repository.findTop100ByStatusAndExecutionTimeLessThanEqualOrderByExecutionTimeAsc(any(), any())).thenReturn(List.of(candidate));
        when(locks.acquire(23L)).thenReturn(Optional.of("token"));
        when(tasks.claim(eq(23L), any())).thenReturn(true);
        when(tasks.get(23L)).thenReturn(claimed);
        when(executions.start(23L)).thenReturn(history);
        doThrow(new IllegalStateException("simulated failure")).when(executor).execute(claimed);

        new TaskEngine(repository, tasks, executions, registry, locks, new DirectExecutor(), 1).poll();

        verify(executions).failed(23L, 32L, "simulated failure");
        verify(executions, never()).succeeded(anyLong(), anyLong());
        verify(locks).release(23L, "token");
    }

    private static final class DirectExecutor extends AbstractExecutorService {
        private boolean shutdown;
        public void shutdown() { shutdown = true; }
        public List<Runnable> shutdownNow() { shutdown = true; return List.of(); }
        public boolean isShutdown() { return shutdown; }
        public boolean isTerminated() { return shutdown; }
        public boolean awaitTermination(long timeout, TimeUnit unit) { return shutdown; }
        public void execute(Runnable command) { command.run(); }
    }
}
