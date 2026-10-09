package com.portfolio.scheduler.executor;

import com.portfolio.scheduler.entity.ScheduledTask;
import com.portfolio.scheduler.enums.TaskType;

public interface TaskExecutor {
    TaskType supports();
    void execute(ScheduledTask task) throws Exception;
}
