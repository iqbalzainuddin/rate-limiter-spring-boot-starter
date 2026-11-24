package com.iqbalmz.ratelimiterstarter.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the rate limiter starter.
 *
 * <p>Prefix: {@code rate-limiter}. Bound from {@code application.yml} / {@code application.properties}.
 */
@Validated
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    /** Master switch. When false, no rate-limiting beans are registered. */
    private boolean enabled = true;

    /** Behavior when Redis is unreachable. */
    private FailMode failMode = FailMode.OPEN;

    /** Maximum time to wait for a Redis response. */
    private Duration timeout = Duration.ofMillis(100);

    /** Default bucket capacity (tokens). */
    @Min(1)
    private long defaultCapacity = 100;

    /** Default refill rate (tokens per second). */
    @Min(0)
    private double defaultRefillPerSecond = 10.0;

    /** How the rate-limit key is derived. */
    private KeyResolverType keyResolver = KeyResolverType.IP;

    /** Servlet filter settings. */
    private Filter filter = new Filter();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FailMode getFailMode() {
        return failMode;
    }

    public void setFailMode(FailMode failMode) {
        this.failMode = failMode;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public long getDefaultCapacity() {
        return defaultCapacity;
    }

    public void setDefaultCapacity(long defaultCapacity) {
        this.defaultCapacity = defaultCapacity;
    }

    public double getDefaultRefillPerSecond() {
        return defaultRefillPerSecond;
    }

    public void setDefaultRefillPerSecond(double defaultRefillPerSecond) {
        this.defaultRefillPerSecond = defaultRefillPerSecond;
    }

    public KeyResolverType getKeyResolver() {
        return keyResolver;
    }

    public void setKeyResolver(KeyResolverType keyResolver) {
        this.keyResolver = keyResolver;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public enum FailMode {
        OPEN, CLOSED
    }

    public enum KeyResolverType {
        IP, USER, API_KEY, COMPOSITE
    }

    public static class Filter {

        /** Whether the servlet filter is registered. */
        private boolean enabled = false;

        /** Path patterns the filter applies to (e.g. /api/**). */
        private List<String> paths = new ArrayList<>();

        /** Override capacity for the filter (null = use default). */
        private Long capacity;

        /** Override refill rate for the filter (null = use default). */
        private Double refillPerSecond;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getPaths() {
            return paths;
        }

        public void setPaths(List<String> paths) {
            this.paths = paths;
        }

        public Long getCapacity() {
            return capacity;
        }

        public void setCapacity(Long capacity) {
            this.capacity = capacity;
        }

        public Double getRefillPerSecond() {
            return refillPerSecond;
        }

        public void setRefillPerSecond(Double refillPerSecond) {
            this.refillPerSecond = refillPerSecond;
        }
    }
}
