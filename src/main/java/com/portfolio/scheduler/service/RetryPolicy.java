package com.portfolio.scheduler.service;

import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RetryPolicy {
    private final long baseDelayMs;
    private final long maxDelayMs;
    public RetryPolicy(@Value("${scheduler.retry.base-delay-ms:1000}") long baseDelayMs,
                       @Value("${scheduler.retry.max-delay-ms:60000}") long maxDelayMs) {
        this.baseDelayMs = baseDelayMs; this.maxDelayMs = maxDelayMs;
    }
    public Instant nextAttemptAt(Instant now, int nextRetryNumber) {
        int exponent = Math.min(Math.max(nextRetryNumber - 1, 0), 30);
        long delay = Math.min(maxDelayMs, baseDelayMs * (1L << exponent));
        return now.plus(Duration.ofMillis(delay));
    }
}
