package com.iqbalmz.ratelimiterstarter.key;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the key from the authenticated user (principal name or {@code X-User-Id}
 * header), falling back to the remote IP.
 */
public class UserKeyResolver implements KeyResolver {

    @Override
    public String resolve(HttpServletRequest request) {
        if (request.getUserPrincipal() != null && request.getUserPrincipal().getName() != null) {
            return "user:" + request.getUserPrincipal().getName();
        }
        String header = request.getHeader("X-User-Id");
        if (header != null && !header.isBlank()) {
            return "user:" + header.trim();
        }
        return "ip:" + (request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr());
    }
}
