package com.iqbalmz.ratelimiterstarter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method (or any Spring bean method) as rate limited.
 *
 * <p>When {@code capacity} or {@code refillPerSecond} is negative, the value from
 * {@code rate-limiter.default-*} properties is used.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimited {

    long capacity() default -1;

    double refillPerSecond() default -1.0;

    /**
     * Optional explicit key. When blank, the configured {@code KeyResolver} is used.
     */
    String key() default "";
}
