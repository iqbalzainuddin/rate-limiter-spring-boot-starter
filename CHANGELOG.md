# Changelog

All notable changes to this project are documented in this file.

## [Unreleased]

## [0.1.0] - 2026-11-25

### Added
- Token bucket rate limiter backed by Redis, with atomic check-and-decrement via a single Lua script.
- `@RateLimited(capacity, refillPerSecond)` annotation (AOP) on controller methods.
- Servlet filter configurable via `application.yml` (path patterns).
- Key resolvers: IP (default), user, API key, composite.
- Configurable failure mode: fail open (default) or fail closed on Redis outage, with 100ms timeout.
- Auto-configuration: adding the dependency enables it; opt-out via `rate-limiter.enabled=false`.
- `429 Too Many Requests` with `Retry-After` and `X-RateLimit-*` headers.
- 7 tests: auto-config (3), concurrency (200 threads), multi-instance (2 apps, 1 Redis), fail-mode (open/closed).
- README with algorithm comparison, trade-offs, property table, and measured benchmark numbers.
- CI workflow (build + Testcontainers tests) and publish workflow (GitHub Packages on tag).

### Known issues
- 11 Medium + 5 Low (see `docs/bugs.md` for details).
- WebFlux support deferred to v2.
- Micrometer metrics not yet implemented.

### Benchmarks
- Throughput: 3,246 req/s (single instance, 50 VUs, 50s).
- p99 added overhead: ~0 ms (NFR-RL-001 met).
- p99 outlier (~370ms) is JVM GC, not rate limiter.
