package com.iqbalmz.ratelimiterstarter.aspect;

import com.iqbalmz.ratelimiterstarter.annotation.RateLimited;
import com.iqbalmz.ratelimiterstarter.config.RateLimiterProperties;
import com.iqbalmz.ratelimiterstarter.core.RateLimiter;
import com.iqbalmz.ratelimiterstarter.core.RateLimitResult;
import com.iqbalmz.ratelimiterstarter.exception.RateLimitExceededException;
import com.iqbalmz.ratelimiterstarter.key.KeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * Enforces {@link RateLimited} on annotated methods.
 */
@Aspect
public class RateLimitAspect {

    private final RateLimiter rateLimiter;
    private final KeyResolver keyResolver;
    private final RateLimiterProperties properties;

    public RateLimitAspect(RateLimiter rateLimiter, KeyResolver keyResolver, RateLimiterProperties properties) {
        this.rateLimiter = rateLimiter;
        this.keyResolver = keyResolver;
        this.properties = properties;
    }

    @Around("@annotation(rateLimited)")
    public Object around(ProceedingJoinPoint pjp, RateLimited rateLimited) throws Throwable {
        long capacity = rateLimited.capacity() > 0 ? rateLimited.capacity() : properties.getDefaultCapacity();
        double refill = rateLimited.refillPerSecond() > 0 ? rateLimited.refillPerSecond() : properties.getDefaultRefillPerSecond();

        String key;
        if (rateLimited.key() != null && !rateLimited.key().isBlank()) {
            key = rateLimited.key();
        } else {
            key = resolveKey();
        }

        RateLimitResult result = rateLimiter.tryAcquire(key, capacity, refill);

        HttpServletResponse response = currentResponse();
        if (response != null) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));
        }

        if (!result.allowed()) {
            if (response != null) {
                response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            }
            throw new RateLimitExceededException(result.retryAfterSeconds());
        }

        return pjp.proceed();
    }

    private String resolveKey() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            try {
                return keyResolver.resolve(request);
            } catch (Exception ignored) {
                // fall through
            }
        }
        // Non-web invocation: fall back to a method-based key.
        return "method:" + Objects.hash();
    }

    private HttpServletResponse currentResponse() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getResponse();
    }
}
