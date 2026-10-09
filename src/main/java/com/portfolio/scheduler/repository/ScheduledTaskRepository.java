package com.portfolio.scheduler.repository;

import com.portfolio.scheduler.entity.ScheduledTask;
import com.portfolio.scheduler.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    List<ScheduledTask> findTop100ByStatusAndExecutionTimeLessThanEqualOrderByExecutionTimeAsc(TaskStatus status, Instant dueTime);
    Page<ScheduledTask> findByStatus(TaskStatus status, Pageable pageable);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ScheduledTask t set t.status = com.portfolio.scheduler.enums.TaskStatus.RUNNING, t.updatedAt = :now where t.id = :id and t.status = com.portfolio.scheduler.enums.TaskStatus.PENDING and t.executionTime <= :now")
    int claim(@Param("id") Long id, @Param("now") Instant now);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ScheduledTask t set t.status = com.portfolio.scheduler.enums.TaskStatus.CANCELLED, t.updatedAt = :now where t.id = :id and t.status = com.portfolio.scheduler.enums.TaskStatus.PENDING")
    int cancelPending(@Param("id") Long id, @Param("now") Instant now);
}
