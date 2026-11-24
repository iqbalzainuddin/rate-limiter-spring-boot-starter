package com.iqbalmz.ratelimiterstarter.key;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the rate-limit key for a given HTTP request.
 *
 * <p>Implementations must be thread-safe. Users may provide their own bean;
 * the auto-configuration backs off when a {@code KeyResolver} bean exists.
 */
@FunctionalInterface
public interface KeyResolver {

    /**
     * @return a stable, non-null key identifying the caller/bucket.
     */
    String resolve(HttpServletRequest request);
}
