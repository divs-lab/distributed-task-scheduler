package com.portfolio.scheduler.lock;

import java.util.Optional;

public interface TaskLockManager {
    Optional<String> acquire(Long taskId);
    void release(Long taskId, String ownerToken);
}
