package com.portfolio.scheduler.repository;

import com.portfolio.scheduler.entity.TaskExecutionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskExecutionHistoryRepository extends JpaRepository<TaskExecutionHistory, Long> {
    List<TaskExecutionHistory> findByTaskIdOrderByAttemptNumberAsc(Long taskId);
    long countByTaskId(Long taskId);
}
