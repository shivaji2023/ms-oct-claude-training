---
name: auth-perf-baseline
description: Perf audit baseline for auth/user/security config and frontend auth; known issues and what was verified clean
metadata:
  type: feedback
---

Audited 2026-10-14 (auth/, user/, SecurityConfig, frontend/src/app/auth/).

Clean: no N+1 (User has no associations), User.email is unique (implicit index), JwtUtil builds key once, JwtAuthFilter parses once per request, no sync HTTP on frontend, no leaking subscriptions (HTTP observables complete).

Known issues:
- AuthRateLimitFilter.buckets ConcurrentHashMap keyed by remote IP is never evicted (unbounded growth).
- Task.ownerId (task/Task.java) has no @Index; TaskRepository not audited in that pass.
- LoginComponent submit button not disabled while request in flight; no OnPush.

**How to apply:** do not re-flag the clean items; check whether the known issues are fixed. Adding Caffeine needs user confirmation (CLAUDE.md: no new dependencies).
