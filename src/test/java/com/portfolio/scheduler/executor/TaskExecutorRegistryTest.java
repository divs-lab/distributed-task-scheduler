package com.portfolio.scheduler.executor;

import com.portfolio.scheduler.enums.TaskType;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskExecutorRegistryTest {
    @Test void resolvesEachRegisteredType() {
        TaskExecutor email = executor(TaskType.EMAIL), cleanup = executor(TaskType.CLEANUP), alert = executor(TaskType.ALERT);
        TaskExecutorRegistry registry = new TaskExecutorRegistry(List.of(email, cleanup, alert));
        assertSame(email, registry.get(TaskType.EMAIL));
        assertSame(cleanup, registry.get(TaskType.CLEANUP));
        assertSame(alert, registry.get(TaskType.ALERT));
    }
    @Test void rejectsMissingExecutor() {
        assertThrows(IllegalArgumentException.class, () -> new TaskExecutorRegistry(List.of()).get(TaskType.ALERT));
    }
    private TaskExecutor executor(TaskType type) {
        TaskExecutor executor = mock(TaskExecutor.class); when(executor.supports()).thenReturn(type); return executor;
    }
}
