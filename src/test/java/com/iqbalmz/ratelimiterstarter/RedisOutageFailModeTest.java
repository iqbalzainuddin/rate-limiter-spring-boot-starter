package com.iqbalmz.ratelimiterstarter;

import com.iqbalmz.ratelimiterstarter.config.RateLimiterProperties;
import com.iqbalmz.ratelimiterstarter.core.RateLimiter;
import com.iqbalmz.ratelimiterstarter.core.RateLimitResult;
import com.iqbalmz.ratelimiterstarter.core.RedisRateLimiter;
import com.iqbalmz.ratelimiterstarter.core.ResilientRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RedisOutageFailModeTest {

    private StringRedisTemplate templateOnClosedPort() {
        // Port 1 is effectively closed -> connection refused.
        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration("127.0.0.1", 1);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(cfg);
        factory.afterPropertiesSet();
        return new StringRedisTemplate(factory);
    }

    @Test
    void failOpenAllowsOnOutage() {
        RateLimiterProperties props = new RateLimiterProperties();
        props.setFailMode(RateLimiterProperties.FailMode.OPEN);
        props.setTimeout(java.time.Duration.ofMillis(100));

        RateLimiter limiter = new ResilientRateLimiter(new RedisRateLimiter(templateOnClosedPort()), props);
        RateLimitResult r = limiter.tryAcquire("outage-open", 10, 1.0);
        assertThat(r.allowed()).isTrue();
    }

    @Test
    void failClosedRejectsOnOutage() {
        RateLimiterProperties props = new RateLimiterProperties();
        props.setFailMode(RateLimiterProperties.FailMode.CLOSED);
        props.setTimeout(java.time.Duration.ofMillis(100));

        RateLimiter limiter = new ResilientRateLimiter(new RedisRateLimiter(templateOnClosedPort()), props);
        RateLimitResult r = limiter.tryAcquire("outage-closed", 10, 1.0);
        assertThat(r.allowed()).isFalse();
    }
}
