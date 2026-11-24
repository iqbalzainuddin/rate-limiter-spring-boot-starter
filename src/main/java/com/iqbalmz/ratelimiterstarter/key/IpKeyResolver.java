package com.iqbalmz.ratelimiterstarter.key;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the key from the client IP address.
 *
 * <p>Honors {@code X-Forwarded-For} when present (first hop), falling back to
 * {@link HttpServletRequest#getRemoteAddr()}.
 */
public class IpKeyResolver implements KeyResolver {

    @Override
    public String resolve(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }
}
