package com.iqbalmz.ratelimiterstarter.key;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Composite key: HTTP method + request URI + caller identity (user/api key/ip).
 * Useful for per-endpoint limits.
 */
public class CompositeKeyResolver implements KeyResolver {

    private final KeyResolver identityResolver;

    public CompositeKeyResolver(KeyResolver identityResolver) {
        this.identityResolver = identityResolver;
    }

    public CompositeKeyResolver() {
        this(new IpKeyResolver());
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String identity = identityResolver.resolve(request);
        return request.getMethod() + ":" + request.getRequestURI() + ":" + identity;
    }
}
