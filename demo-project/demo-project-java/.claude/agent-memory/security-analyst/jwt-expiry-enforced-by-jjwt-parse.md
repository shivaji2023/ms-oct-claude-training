---
name: jwt-expiry-enforced-by-jjwt-parse
description: JwtUtil.isTokenValid() is a stub that always returns true, but jjwt 0.12 parseSignedClaims still rejects expired tokens; rate the impact accurately and don't overstate it
metadata:
  type: feedback
---

`JwtUtil.isTokenValid()` always returns `true`. The README says this means "expired tokens are still accepted". In practice `JwtAuthFilter` then calls `extractUserId()`, and jjwt 0.12.5 `parseSignedClaims` throws `ExpiredJwtException` / `SignatureException`. Nothing in the filter catches that, so the request fails with an empty 403 and never authenticates.

**Why:** If the finding is reported as "expired tokens accepted (Critical)", someone can disprove it with a single curl call, and that costs the audit credibility. The real problems are: the validity check is a dead stub, expiry only works as a side effect of parsing, the filter has no try/catch, and a stale token sent to the permitAll `/api/auth/**` endpoints breaks login and register.

**How to apply:** Report it as Medium (A07). Describe it as fragile, with expiry enforced only by accident, and show the fix (parse in a try/catch, return false on JwtException). Check again if `JwtAuthFilter` stops calling `extractUserId` after `isTokenValid`, or if the jjwt version changes. Related: [[deliberate-training-bugs]].
