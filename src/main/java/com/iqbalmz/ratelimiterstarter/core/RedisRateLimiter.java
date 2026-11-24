package com.iqbalmz.ratelimiterstarter.core;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.List;

/**
 * Redis-backed token bucket rate limiter.
 *
 * <p>Check-and-decrement is performed by a single Lua script, which runs
 * atomically because Redis is single-threaded per instance.
 */
public class RedisRateLimiter implements RateLimiter {

    private static final String KEY_PREFIX = "rl:bucket:";

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List> script;

    public RedisRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        DefaultRedisScript<List> s = new DefaultRedisScript<>();
        s.setLocation(new ClassPathResource("lua/token_bucket.lua"));
        s.setResultType(List.class);
        this.script = s;
    }

    @Override
    public RateLimitResult tryAcquire(String key, long capacity, double refillPerSecond) {
        String redisKey = KEY_PREFIX + sanitize(key);
        long now = System.currentTimeMillis();

        List<?> result = redisTemplate.execute(
                script,
                Collections.singletonList(redisKey),
                String.valueOf(capacity),
                String.valueOf(refillPerSecond),
                String.valueOf(now),
                "1"
        );

        if (result == null || result.size() < 3) {
            // Should not happen; treat as allowed to fail open at this layer.
            return new RateLimitResult(true, capacity, 0);
        }

        boolean allowed = toLong(result.get(0)) == 1L;
        long remaining = toLong(result.get(1));
        long retryAfter = toLong(result.get(2));
        return new RateLimitResult(allowed, remaining, retryAfter);
    }

    /**
     * Sanitize a key so it is safe to use as a Redis key segment.
     * Replaces anything outside [a-zA-Z0-9:._-] with '_'.
     */
    static String sanitize(String key) {
        if (key == null || key.isBlank()) {
            return "anonymous";
        }
        StringBuilder sb = new StringBuilder(key.length());
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (Character.isLetterOrDigit(c) || c == ':' || c == '.' || c == '_' || c == '-') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    private static long toLong(Object o) {
        if (o instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(o));
    }
}
