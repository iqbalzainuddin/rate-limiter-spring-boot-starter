package com.iqbalmz.ratelimiterstarter;

import com.iqbalmz.ratelimiterstarter.core.RateLimiter;
import com.iqbalmz.ratelimiterstarter.core.RateLimitResult;
import com.iqbalmz.ratelimiterstarter.core.RedisRateLimiter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class RateLimiterConcurrencyTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    private static StringRedisTemplate template;

    @BeforeAll
    static void setUp() {
        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration(redis.getHost(), redis.getFirstMappedPort());
        LettuceConnectionFactory factory = new LettuceConnectionFactory(cfg);
        factory.afterPropertiesSet();
        template = new StringRedisTemplate(factory);
    }

    @Test
    void concurrentRequestsNeverExceedCapacity() throws InterruptedException {
        RateLimiter limiter = new RedisRateLimiter(template);
        String key = "conc:" + System.nanoTime();
        int capacity = 50;
        int threads = 200;

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger allowed = new AtomicInteger(0);
        AtomicInteger rejected = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(32);
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    RateLimitResult r = limiter.tryAcquire(key, capacity, 1.0);
                    if (r.allowed()) allowed.incrementAndGet();
                    else rejected.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        assertThat(allowed.get()).isLessThanOrEqualTo(capacity);
        assertThat(allowed.get() + rejected.get()).isEqualTo(threads);
    }
}
