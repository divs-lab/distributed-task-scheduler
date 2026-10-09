package com.portfolio.scheduler.lock;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisTaskLockManagerTest {
    @SuppressWarnings("unchecked")
    @Test void usesFiniteLeaseAndUniqueOwnerToken() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(eq("scheduler:task:5"), anyString(), eq(Duration.ofMillis(15000)))).thenReturn(true);
        RedisTaskLockManager locks = new RedisTaskLockManager(redis, 15000);

        Optional<String> first = locks.acquire(5L);
        Optional<String> second = locks.acquire(5L);

        assertTrue(first.isPresent()); assertTrue(second.isPresent());
        assertNotEquals(first.orElseThrow(), second.orElseThrow());
        verify(values, times(2)).setIfAbsent(eq("scheduler:task:5"), anyString(), eq(Duration.ofMillis(15000)));
    }

    @Test void releaseUsesCompareAndDeleteScriptWithOwnerToken() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        RedisTaskLockManager locks = new RedisTaskLockManager(redis, 10000);
        locks.release(8L, "owner-8");

        var script = org.mockito.ArgumentCaptor.forClass(DefaultRedisScript.class);
        verify(redis).execute(script.capture(), eq(List.of("scheduler:task:8")), eq("owner-8"));
        assertTrue(script.getValue().getScriptAsString().contains("redis.call('get', KEYS[1]) == ARGV[1]"));
        assertTrue(script.getValue().getScriptAsString().contains("redis.call('del', KEYS[1])"));
    }
}
