package com.iqbalmz.ratelimiterstarter.key;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the key from an API key header ({@code X-API-Key}), falling back to
 * the remote IP when absent.
 */
public class ApiKeyKeyResolver implements KeyResolver {

    public static final String HEADER = "X-API-Key";

    @Override
    public String resolve(HttpServletRequest request) {
        String key = request.getHeader(HEADER);
        if (key != null && !key.isBlank()) {
            return "apikey:" + key.trim();
        }
        return "ip:" + (request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr());
    }
}
