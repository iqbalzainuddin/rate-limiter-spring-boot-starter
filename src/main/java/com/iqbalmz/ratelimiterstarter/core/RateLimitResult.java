package com.iqbalmz.ratelimiterstarter.core;

/**
 * Result of a rate-limit check.
 *
 * @param allowed          whether the request is allowed
 * @param remaining        tokens left in the bucket after this decision
 * @param retryAfterSeconds seconds the client should wait before retrying (0 if allowed)
 */
public record RateLimitResult(boolean allowed, long remaining, long retryAfterSeconds) {
}
