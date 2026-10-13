# Plan: Rate limiting for `/api/auth/**`

| Field    | Value                                        |
|----------|----------------------------------------------|
| Status   | Proposed                                     |
| Created  | 2026-10-13                                   |
| Branch   | feature branch (never `main`)                |
| Approach | Bucket4j (dependency confirmed by user)      |
| Limit    | 5 requests / minute per client IP            |

## Context
`/api/auth/login` and `/api/auth/register` are `permitAll()` (`SecurityConfig.java:41`) with no throttling, so they are open to brute-force and signup spam. This plan adds per-client-IP rate limiting using Bucket4j.

## Progress Tracker

### 1. Dependency
- [x] Add `com.bucket4j:bucket4j-core:8.10.1` to `backend/pom.xml`
  - 8.10.1 is the last release under the `bucket4j-core` artifact id. Later versions are published as `bucket4j_jdk17-core`.

### 2. Rate limit filter: `backend/src/main/java/com/technizer/taskapi/auth/AuthRateLimitFilter.java`
- [ ] Create a `@Component` that extends `OncePerRequestFilter` (same pattern as `JwtAuthFilter`)
- [ ] `shouldNotFilter`: only filter paths that start with `/api/auth/`, and skip `OPTIONS` (CORS preflight)
- [ ] Per-IP buckets in a `ConcurrentHashMap<String, Bucket>`, keyed by `request.getRemoteAddr()`
- [ ] Bucket: `Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofMinutes(1)).build()`
- [ ] When a token is consumed: set the `X-RateLimit-Remaining` header and continue the chain
- [ ] When rejected: return `429`, a `Retry-After` header (seconds, from `getNanosToWaitForRefill`) and the JSON body `{"error":"Too many requests, try again later"}`
  - The response is written directly because filter exceptions bypass `GlobalExceptionHandler`
- [ ] Constructors (constructor injection only):
  - an `@Autowired` public no-arg constructor that uses 5/min
  - a package-private `(long capacity, Duration period)` constructor for tests

### 3. Security wiring: `backend/src/main/java/com/technizer/taskapi/config/SecurityConfig.java`
- [ ] Inject `AuthRateLimitFilter` via the constructor
- [ ] Register it with `.addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)`, ahead of the JWT filter
  - Because it runs inside the security chain (after `CorsFilter`), 429 responses still carry CORS headers for the Angular app

### 4. Tests: `backend/src/test/java/com/technizer/taskapi/auth/AuthRateLimitFilterTest.java`
Plain JUnit 5 with `MockHttpServletRequest`, `MockHttpServletResponse` and `MockFilterChain`:
- [ ] 5 POSTs to `/api/auth/login` from one IP pass. The 6th returns 429 with `Retry-After` and a JSON `error` body, and the chain is not invoked
- [ ] A different IP is still allowed after the first IP is limited
- [ ] `/api/tasks` is never rate-limited (more than 5 requests pass)
- [ ] `OPTIONS /api/auth/login` is not counted
- [ ] `X-RateLimit-Remaining` decrements from 4 to 0
- [ ] Existing tests are left untouched and still pass

### 5. Verification
- [ ] `cd backend && mvn test` is green, including `AuthServiceTest`, `TaskServiceTest` and `TaskServiceUpdateTest`
- [ ] Manual check: run `mvn spring-boot:run`, then send 7 `curl -i -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"email":"a@b.c","password":"x"}'` requests
  - Expected: requests 1–5 get the normal response (400 for bad credentials), and requests 6–7 get `429` with `Retry-After`

### 6. Wrap-up (only when asked)
- [ ] Commit as `feat: rate limit /api/auth endpoints with Bucket4j` on a feature branch
- [ ] PR description covers what changed, why, and how to test

## Known Limitations (note in PR)
- In-memory and per-instance only; limits are not shared across replicas.
- No eviction from the bucket map, so memory grows with the number of distinct IPs. Fine for the demo; a Caffeine- or Redis-backed Bucket4j would fix it later.
- Uses `getRemoteAddr()` and deliberately ignores `X-Forwarded-For`, which can be spoofed. Behind a proxy, configure `server.forward-headers-strategy` instead.
- No frontend change. Angular login/register shows its generic error on 429 (possible follow-up).
