package com.portfolio.scheduler.lock;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisTaskLockManager implements TaskLockManager {
    private static final Logger log = LoggerFactory.getLogger(RedisTaskLockManager.class);
    private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Long.class);
    private final StringRedisTemplate redis;
    private final Duration lease;
    public RedisTaskLockManager(StringRedisTemplate redis, @Value("${scheduler.lock.lease-ms:30000}") long leaseMs) {
        this.redis = redis; this.lease = Duration.ofMillis(leaseMs);
    }
    public Optional<String> acquire(Long taskId) {
        String token = UUID.randomUUID().toString();
        try { return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key(taskId), token, lease)) ? Optional.of(token) : Optional.empty(); }
        catch (RuntimeException e) { log.error("Redis lock acquisition failed for taskId={}", taskId, e); throw e; }
    }
    public void release(Long taskId, String ownerToken) {
        try { redis.execute(RELEASE_SCRIPT, java.util.List.of(key(taskId)), ownerToken); }
        catch (RuntimeException e) { log.error("Redis lock release failed for taskId={}", taskId, e); }
    }
    private String key(Long taskId) { return "scheduler:task:" + taskId; }
}
