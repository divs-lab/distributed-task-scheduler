package com.portfolio.scheduler.executor;

import com.portfolio.scheduler.enums.TaskType;
import org.springframework.stereotype.Component;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskExecutorRegistry {
    private final Map<TaskType, TaskExecutor> executors = new EnumMap<>(TaskType.class);
    public TaskExecutorRegistry(List<TaskExecutor> executors) {
        for (TaskExecutor executor : executors) {
            if (this.executors.put(executor.supports(), executor) != null)
                throw new IllegalStateException("Duplicate executor for " + executor.supports());
        }
    }
    public TaskExecutor get(TaskType type) {
        TaskExecutor executor = executors.get(type);
        if (executor == null) throw new IllegalArgumentException("No executor registered for " + type);
        return executor;
    }
}
