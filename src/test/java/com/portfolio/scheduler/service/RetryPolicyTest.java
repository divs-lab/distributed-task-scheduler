package com.portfolio.scheduler.service;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RetryPolicyTest {
    @Test void appliesCappedExponentialBackoff() {
        RetryPolicy policy = new RetryPolicy(1000, 5000);
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        assertEquals(now.plusMillis(1000), policy.nextAttemptAt(now, 1));
        assertEquals(now.plusMillis(2000), policy.nextAttemptAt(now, 2));
        assertEquals(now.plusMillis(5000), policy.nextAttemptAt(now, 5));
    }
}
