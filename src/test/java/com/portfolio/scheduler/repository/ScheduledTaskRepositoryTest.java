package com.portfolio.scheduler.repository;

import com.portfolio.scheduler.entity.ScheduledTask;
import com.portfolio.scheduler.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ScheduledTaskRepositoryTest {
    @Autowired ScheduledTaskRepository repository;

    @Test void duePendingTaskCanOnlyBeClaimedOnce() {
        ScheduledTask task = repository.saveAndFlush(new ScheduledTask(TaskType.ALERT, Instant.now().minusSeconds(1), 0));
        Instant now = Instant.now();
        assertEquals(1, repository.claim(task.getId(), now));
        assertEquals(0, repository.claim(task.getId(), now));
        assertEquals(TaskStatus.RUNNING, repository.findById(task.getId()).orElseThrow().getStatus());
    }
}
