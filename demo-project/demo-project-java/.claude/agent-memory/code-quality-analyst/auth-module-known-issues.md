---
name: auth-module-known-issues
description: Known quality issues and test gaps in backend auth/ and frontend/src/app/auth (as of 2026-10-14 audit)
metadata:
  type: feedback
---

- JwtUtil.isTokenValid() is a deliberate training bug (always true, "do not fix before session"); do not treat as a surprise, but still flag it.
- AuthService throws IllegalArgumentException for both duplicate email and bad credentials (generic, needs custom exceptions).
- AuthRateLimitFilter uses redundant @Autowired on a constructor (harmless, but noise).
- Tests exist only for AuthService and AuthRateLimitFilter; no tests for JwtUtil, JwtAuthFilter, AuthController, nor any frontend auth specs.
- Frontend: hardcoded baseUrl in auth.service.ts, TOKEN_KEY duplicated risk with interceptor in core/, generic catch-all error message in LoginComponent.
- Backend CLAUDE.md says DTO @Valid is inert; backend/frontend CLAUDE.md files hold extra non-obvious behavior notes.
