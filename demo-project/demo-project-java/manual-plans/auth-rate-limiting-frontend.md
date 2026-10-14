# Plan: Showing rate-limit messages on the login/register screen (Angular)

| Field    | Value                                                                  |
|----------|------------------------------------------------------------------------|
| Status   | Proposed (plan only, no code changed)                                  |
| Created  | 2026-10-13                                                             |
| Branch   | feature branch (never `main`)                                          |
| Depends  | [auth-rate-limiting.md](auth-rate-limiting.md) (backend 429 behaviour) |

## Context
The backend plan returns `429` with a `Retry-After` header and a JSON body `{"error":"Too many requests, try again later"}` on `/api/auth/**` after 5 requests/minute per IP. That plan lists "No frontend change" as a known limitation.

Today the frontend would show this for a 429:

- `LoginComponent.submit()` has a single catch-all `error` handler: `'Something went wrong. Check your details and try again.'`
- That is misleading for a rate limit, because the user's details are fine and retrying immediately makes it worse.
- `AuthService` and `auth.interceptor.ts` do not look at error status codes at all.

## Findings that shape the design
1. **`Retry-After` is not readable from the browser yet.** `SecurityConfig.corsConfigurationSource()` sets allowed origins, methods and headers but no `exposedHeaders`. Under CORS, JS can only read safelisted response headers, so `err.headers.get('Retry-After')` would return `null` in the Angular app even though the header is on the wire.
2. **The 429 body is the fallback.** Angular puts the parsed JSON in `HttpErrorResponse.error`, so `error.error?.error` is readable without any CORS change.
3. **Both login and register are limited**, and they share the same component, so one handler covers both modes.
4. **There is no frontend test file** (`*.spec.ts`) in the repo yet, and CLAUDE.md requires tests for every change, so a spec setup is part of this work.
5. The same limit applies across modes. An IP that has used its 5 requests on login is also blocked from register.

## Design decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Where to detect 429 | `AuthService` maps the error into a typed value; `LoginComponent` only renders it | CLAUDE.md: services own HTTP, components never touch `HttpClient` or raw HTTP details |
| How to get the wait time | Prefer `Retry-After` header, fall back to a default of 60 s if the header is unreadable or invalid | Works even if the CORS change is missed; never shows "0" or "NaN" |
| Backend change needed | Yes, one line: expose `Retry-After` (and `X-RateLimit-Remaining`) via `setExposedHeaders` | Without it the countdown is always the fallback |
| Global interceptor vs. local handling | Local (auth service + login component) | Only the auth endpoints are limited; a global toast would be noise and would couple the interceptor to UI |
| UX | Inline message in the existing `.error` slot, submit button disabled with a live countdown, auto re-enabled at 0 | Reuses the current layout; prevents the user from burning more requests while blocked |

## Progress Tracker

### 1. Backend prerequisite (small, called out separately)
- [ ] `backend/src/main/java/com/technizer/taskapi/config/SecurityConfig.java`: in `corsConfigurationSource()` add `configuration.setExposedHeaders(List.of("Retry-After", "X-RateLimit-Remaining"));`
- [ ] Add or extend a backend test asserting the CORS response to an allowed origin includes `Access-Control-Expose-Headers: Retry-After`
- [ ] Verify the 429 response from `AuthRateLimitFilter` also passes through CORS (the backend plan already relies on the filter running after `CorsFilter`)

### 2. `frontend/src/app/auth/auth.service.ts`
- [ ] Add an exported error type, e.g. `export class RateLimitError extends Error { constructor(readonly retryAfterSeconds: number, message: string) }`
- [ ] Add a private `mapError` used with `catchError` in both `login()` and `register()`:
  - If `err instanceof HttpErrorResponse && err.status === 429`: read `Retry-After`, parse with `parseInt`, accept only a finite integer >= 1, otherwise use `DEFAULT_RETRY_AFTER_SECONDS = 60`. Cap it (e.g. 3600) so a bad header cannot lock the UI for hours. Throw `RateLimitError` with the server's `error.error?.error` message or a default.
  - Any other error is rethrown unchanged, so existing behaviour is preserved.
- [ ] `storeToken` still runs only on success (the `tap` stays before `catchError`)

### 3. `frontend/src/app/auth/login.component.ts`
- [ ] New state: `retryAfterSeconds = 0` and a private `countdownTimer` handle
- [ ] In the `error` handler: if the error is a `RateLimitError`, call `startCooldown(err.retryAfterSeconds)` and set a specific message; otherwise keep the existing generic message
- [ ] Message copy: `Too many attempts. Please try again in {{ retryAfterSeconds }}s.` (singular/plural not needed with "s"; switch to "a moment" at 0 and clear the message)
- [ ] `startCooldown(n)`: `setInterval` once per second decrementing `retryAfterSeconds`; at 0 clear the interval and the error message
- [ ] Disable the submit button with `[disabled]="retryAfterSeconds > 0"`; guard `submit()` too so Enter-key or programmatic calls cannot bypass it
- [ ] `toggleMode()` must **not** clear a running cooldown (the limit is per IP, not per mode), but should keep clearing ordinary errors
- [ ] Implement `OnDestroy` to clear the interval so navigating away does not leak the timer
- [ ] Use a distinct style (`.error` stays red; consider a neutral amber `.warning` class) so rate limiting does not look like wrong credentials
- [ ] Add `role="alert"` (or `aria-live="polite"`) on the message so screen readers announce it; avoid announcing every second of the countdown (announce on start and on completion only)

### 4. Tests (Jasmine/Karma, per CLAUDE.md; no spec files exist yet)
Check first with the user whether a Karma/Jasmine setup is already wired in `package.json`/`angular.json`. Adding test dependencies requires confirmation per CLAUDE.md, so do not install anything without asking.

`frontend/src/app/auth/auth.service.spec.ts` (with `HttpClientTestingModule` / `provideHttpClientTesting`):
- [ ] 429 with `Retry-After: 30` surfaces `RateLimitError` with `retryAfterSeconds === 30`
- [ ] 429 with no header falls back to 60
- [ ] 429 with a non-numeric, zero or negative header falls back to 60
- [ ] 429 with a huge header value is capped
- [ ] 401/400/500 are rethrown unchanged (not converted)
- [ ] A successful login still stores the token (existing behaviour regression test)

`frontend/src/app/auth/login.component.spec.ts` (fake `AuthService`, `fakeAsync`/`tick`):
- [ ] A `RateLimitError` shows the rate-limit message and disables the button
- [ ] The countdown decrements each second and re-enables the button and clears the message at 0
- [ ] A generic error still shows the existing generic message and does not disable the button
- [ ] `submit()` during an active cooldown does not call the service
- [ ] `toggleMode()` during a cooldown keeps the cooldown active
- [ ] Destroying the component clears the timer (no pending timers in `fakeAsync`)
- [ ] Applies to both login and register modes

### 5. Manual verification
- [ ] Start the backend (`mvn spring-boot:run`) and frontend (`ng serve`), open `http://localhost:4200`
- [ ] Submit bad credentials 6 times quickly: attempts 1–5 show the generic error, attempt 6 shows the rate-limit message with a countdown and a disabled button
- [ ] In DevTools > Network, confirm the 429 response has `Access-Control-Expose-Headers: Retry-After` and the countdown starts at roughly the `Retry-After` value rather than 60
- [ ] Wait out the countdown: button re-enabled, message cleared, next attempt works
- [ ] Switch to Register mid-cooldown: still blocked, countdown continues
- [ ] Reload the page mid-cooldown: cooldown is lost (accepted, see below), the next attempt gets a fresh 429 and a correct countdown

### 6. Wrap-up (only when asked)
- [ ] Commit in two logical commits: `feat: expose Retry-After via CORS` (backend + test) and `feat: show rate-limit message on login` (frontend + tests)
- [ ] PR description covers what changed, why, and how to test; flag the `SecurityConfig.java` change explicitly

## Open questions for the user
1. **Message wording and tone**: is "Too many attempts. Please try again in 30s." acceptable, or is there house copy?
2. **Persist the cooldown across reloads?** Default is no (simple; the server stays the source of truth). Persisting in `sessionStorage` would avoid a wasted request after reload, at the cost of more state to test.
3. **Show remaining attempts** (using `X-RateLimit-Remaining`, e.g. "2 attempts left") before the user is blocked? This is exposed by step 1 but not used by this plan. It is a small extension if wanted.
4. **Frontend test tooling**: confirm Jasmine/Karma is already configured, or approve the setup.

## Known limitations
- The cooldown timer is client-side only; the server remains authoritative and will re-issue 429 if the user bypasses the UI.
- Multiple users behind one NAT/proxy share an IP and therefore share the limit, so one user's failed attempts can block others. This is a backend design limitation inherited from the IP-based approach.
- If the backend `Retry-After` header is not exposed (step 1 skipped), the UI still works but uses the 60 s default rather than the real wait.
- Cooldown state is lost on page reload (see open question 2).
