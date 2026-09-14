package com.corhuila.sgie.Security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class LoginRateLimiter {

    private final int capacity;
    private final Duration window;
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public LoginRateLimiter(
            @Value("${security.login.rate-limit.capacity:5}") int capacity,
            @Value("${security.login.rate-limit.window-minutes:1}") long windowMinutes) {
        this.capacity = capacity;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    public ConsumptionProbe tryConsume(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> newBucket());
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, window));
        return Bucket.builder().addLimit(limit).build();
    }
}
