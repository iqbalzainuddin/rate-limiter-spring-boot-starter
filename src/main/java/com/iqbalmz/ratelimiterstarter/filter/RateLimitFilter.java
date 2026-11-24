package com.iqbalmz.ratelimiterstarter.filter;

import com.iqbalmz.ratelimiterstarter.config.RateLimiterProperties;
import com.iqbalmz.ratelimiterstarter.core.RateLimiter;
import com.iqbalmz.ratelimiterstarter.core.RateLimitResult;
import com.iqbalmz.ratelimiterstarter.key.KeyResolver;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Servlet filter that applies rate limiting to configured path patterns.
 */
public class RateLimitFilter implements Filter {

    private final RateLimiter rateLimiter;
    private final KeyResolver keyResolver;
    private final RateLimiterProperties properties;

    public RateLimitFilter(RateLimiter rateLimiter, KeyResolver keyResolver, RateLimiterProperties properties) {
        this.rateLimiter = rateLimiter;
        this.keyResolver = keyResolver;
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        long capacity = properties.getFilter().getCapacity() != null
                ? properties.getFilter().getCapacity()
                : properties.getDefaultCapacity();
        double refill = properties.getFilter().getRefillPerSecond() != null
                ? properties.getFilter().getRefillPerSecond()
                : properties.getDefaultRefillPerSecond();

        String key = keyResolver.resolve(httpReq);
        RateLimitResult result = rateLimiter.tryAcquire(key, capacity, refill);

        httpRes.setHeader("X-RateLimit-Limit", String.valueOf(capacity));
        httpRes.setHeader("X-RateLimit-Remaining", String.valueOf(result.remaining()));

        if (!result.allowed()) {
            httpRes.setStatus(429);
            httpRes.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            httpRes.setContentType("application/json");
            String body = "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded\"}";
            httpRes.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            return;
        }

        chain.doFilter(request, response);
    }
}
