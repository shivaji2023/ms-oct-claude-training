---
name: deliberate-training-bugs
description: README lists intentional security bugs (isTokenValid stub, TaskService IDOR, hardcoded JWT secret) that must be reported but never fixed before the training session
metadata:
  type: project
---

The section "Deliberate issues" in README.md lists bugs left in on purpose for training modules:
1. `JwtUtil.isTokenValid()` always returns true (Module 13 bug-lifecycle demo). See [[jwt-expiry-enforced-by-jjwt-parse]] for its real impact.
2. IDOR in `TaskService.markCompleted()` / `.delete()`: no owner check (Module 6/13).
3. Hardcoded `app.jwt.secret` in application.properties (Module 4/6). That file is unreadable, see [[application-properties-access-denied]].
4. Placeholder-only tests (`AuthServiceTest`, `TaskServiceTest`).

**Why:** The trainer needs these to still be in the code when the session runs. The README says do not run /security-review or fix them beforehand.

**How to apply:** Always report them as findings, but never modify source (the agent is read-only anyway). Other auth findings I've seen (still present as of 2026-10-14): register leaks user enumeration ("Email already registered: <email>"), login has a timing oracle (no bcrypt call for unknown users), `@Valid` is missing so DTO constraints do nothing, the rate limiter is keyed on remote IP and its bucket map is never evicted, the H2 console is permitAll with frameOptions disabled, and the dependencies are Spring Boot 3.2.5 / Security 6.2.4 / Tomcat 10.1.20, which are out of OSS support.

Added during the AuthService audit on 2026-10-14 (not README bugs, still present):
- Login has a single-request enumeration oracle. Omitting `password` makes `BCryptPasswordEncoder.matches(null, hash)` throw "rawPassword cannot be null" for existing users only, and `GlobalExceptionHandler` echoes `ex.getMessage()`.
- There is no password policy, so `""` is accepted as a password because `@Valid` is missing.
- Email is not normalised (case or whitespace) before `existsByEmail`/`findByEmail`.
- Security 6.2.4 is affected by CVE-2025-22228 (the BCrypt 72-byte `matches` issue).
