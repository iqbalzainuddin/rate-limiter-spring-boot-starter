package com.iqbalmz.ratelimiterstarter.key;

import com.iqbalmz.ratelimiterstarter.config.RateLimiterProperties;

/**
 * Factory for the built-in key resolvers.
 */
public final class KeyResolvers {

    private KeyResolvers() {
    }

    public static KeyResolver from(RateLimiterProperties.KeyResolverType type) {
        return switch (type) {
            case USER -> new UserKeyResolver();
            case API_KEY -> new ApiKeyKeyResolver();
            case COMPOSITE -> new CompositeKeyResolver(new IpKeyResolver());
            case IP -> new IpKeyResolver();
        };
    }
}
