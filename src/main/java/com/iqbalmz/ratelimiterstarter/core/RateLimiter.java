package com.iqbalmz.ratelimiterstarter.core;

/**
 * A rate limiter that can atomically decide whether a request is allowed.
 */
public interface RateLimiter {

    /**
     * Try to acquire {@code requested} tokens for the given key.
     *
     * @param key               the bucket key (already namespaced by the caller)
     * @param capacity          maximum tokens the bucket can hold
     * @param refillPerSecond   refill rate in tokens per second
     * @return the decision and remaining tokens
     */
    RateLimitResult tryAcquire(String key, long capacity, double refillPerSecond);
}
