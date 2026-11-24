package com.iqbalmz.ratelimiterstarter;

import com.iqbalmz.ratelimiterstarter.autoconfigure.RateLimiterAutoConfiguration;
import com.iqbalmz.ratelimiterstarter.core.RateLimiter;
import com.iqbalmz.ratelimiterstarter.key.KeyResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RateLimiterAutoConfiguration.class));

    @Test
    void backsOffWhenRedisIsAbsent() {
        runner.run(ctx -> assertThat(ctx).doesNotHaveBean(RateLimiter.class));
    }

    @Test
    void optsOutWhenDisabled() {
        runner
                .withPropertyValues("rate-limiter.enabled=false")
                .withBean(RedisConnectionFactory.class, () -> org.mockito.Mockito.mock(RedisConnectionFactory.class))
                .withBean(StringRedisTemplate.class, () -> org.mockito.Mockito.mock(StringRedisTemplate.class))
                .run(ctx -> assertThat(ctx).doesNotHaveBean(RateLimiter.class));
    }

    @Test
    void registersCoreBeansWhenRedisPresent() {
        runner
                .withBean(RedisConnectionFactory.class, () -> org.mockito.Mockito.mock(RedisConnectionFactory.class))
                .withBean(StringRedisTemplate.class, () -> org.mockito.Mockito.mock(StringRedisTemplate.class))
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(RateLimiter.class);
                    assertThat(ctx).hasSingleBean(KeyResolver.class);
                });
    }
}
