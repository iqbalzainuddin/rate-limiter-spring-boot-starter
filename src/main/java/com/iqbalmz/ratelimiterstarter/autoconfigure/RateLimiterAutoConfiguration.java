package com.iqbalmz.ratelimiterstarter.autoconfigure;

import com.iqbalmz.ratelimiterstarter.aspect.RateLimitAspect;
import com.iqbalmz.ratelimiterstarter.config.RateLimiterProperties;
import com.iqbalmz.ratelimiterstarter.core.RateLimiter;
import com.iqbalmz.ratelimiterstarter.core.RedisRateLimiter;
import com.iqbalmz.ratelimiterstarter.core.ResilientRateLimiter;
import com.iqbalmz.ratelimiterstarter.filter.RateLimitFilter;
import com.iqbalmz.ratelimiterstarter.key.KeyResolver;
import com.iqbalmz.ratelimiterstarter.key.KeyResolvers;
import com.iqbalmz.ratelimiterstarter.web.RateLimitExceptionHandler;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@AutoConfiguration
@ConditionalOnProperty(prefix = "rate-limiter", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RateLimiterProperties.class)
@ConditionalOnClass(RedisConnectionFactory.class)
@ConditionalOnBean(RedisConnectionFactory.class)
public class RateLimiterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RateLimiter rateLimiter(StringRedisTemplate redisTemplate, RateLimiterProperties properties) {
        return new ResilientRateLimiter(new RedisRateLimiter(redisTemplate), properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public KeyResolver keyResolver(RateLimiterProperties properties) {
        return KeyResolvers.from(properties.getKeyResolver());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
    public RateLimitAspect rateLimitAspect(RateLimiter rateLimiter, KeyResolver keyResolver, RateLimiterProperties properties) {
        return new RateLimitAspect(rateLimiter, keyResolver, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitExceptionHandler rateLimitExceptionHandler() {
        return new RateLimitExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(prefix = "rate-limiter.filter", name = "enabled", havingValue = "true", matchIfMissing = false)
    public FilterRegistrationBean<Filter> rateLimitFilterRegistration(
            RateLimiter rateLimiter,
            KeyResolver keyResolver,
            RateLimiterProperties properties) {

        RateLimitFilter filter = new RateLimitFilter(rateLimiter, keyResolver, properties);
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(filter);
        List<String> paths = properties.getFilter().getPaths();
        if (paths == null || paths.isEmpty()) {
            registration.addUrlPatterns("/*");
        } else {
            registration.setUrlPatterns(paths);
        }
        registration.setOrder(0);
        return registration;
    }
}
