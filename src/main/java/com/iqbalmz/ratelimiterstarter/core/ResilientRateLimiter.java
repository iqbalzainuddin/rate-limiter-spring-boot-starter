package com.iqbalmz.ratelimiterstarter.core;

import com.iqbalmz.ratelimiterstarter.config.RateLimiterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wraps a {@link RateLimiter} with timeout enforcement and fail-open/fail-closed
 * behavior on Redis outage.
 */
public class ResilientRateLimiter implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(ResilientRateLimiter.class);

    private final RateLimiter delegate;
    private final RateLimiterProperties properties;

    public ResilientRateLimiter(RateLimiter delegate, RateLimiterProperties properties) {
        this.delegate = delegate;
        this.properties = properties;
    }

    @Override
    public RateLimitResult tryAcquire(String key, long capacity, double refillPerSecond) {
        try {
            RateLimitResult result = CompletableFuture
                    .supplyAsync(() -> delegate.tryAcquire(key, capacity, refillPerSecond))
                    .get(properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS);

            log.debug("rate-limit check key={} allowed={} remaining={}", key, result.allowed(), result.remaining());
            return result;
        } catch (TimeoutException e) {
            log.warn("Redis timeout after {} ms for key={}, applying fail mode {}",
                    properties.getTimeout().toMillis(), key, properties.getFailMode(), e);
        } catch (ExecutionException e) {
            log.warn("Redis error for key={}, applying fail mode {}", key, properties.getFailMode(), e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted during Redis call for key={}, applying fail mode {}", key, properties.getFailMode(), e);
        }

        if (properties.getFailMode() == RateLimiterProperties.FailMode.CLOSED) {
            return new RateLimitResult(false, 0, 60);
        }
        return new RateLimitResult(true, capacity, 0);
    }
}
