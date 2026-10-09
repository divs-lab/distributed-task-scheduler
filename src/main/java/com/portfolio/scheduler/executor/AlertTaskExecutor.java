package com.portfolio.scheduler.executor;

import com.portfolio.scheduler.entity.ScheduledTask;
import com.portfolio.scheduler.enums.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AlertTaskExecutor implements TaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(AlertTaskExecutor.class);
    public TaskType supports() { return TaskType.ALERT; }
    public void execute(ScheduledTask task) { log.info("Simulated alert task completed: taskId={}, type={}", task.getId(), supports()); }
}
