---
name: application-properties-access-denied
description: backend/src/main/resources/application.properties is tracked in git but blocked by the Read permission deny list; JWT secret / H2 console config cannot be verified directly
metadata:
  type: feedback
---

`backend/src/main/resources/application.properties` exists (tracked in git) but Read returns "denied by your permission settings". Glob does not list it either.

**Why:** The deny list is deliberate (README "Deliberate issues" #3 says the hardcoded `app.jwt.secret` there is used to demo the settings.json deny list in Module 4). Bypassing it with `git show` or other shell tricks would defeat the demo and break the agent's "only mvn dependency:tree" shell rule.

**How to apply:** Do not try to read it another way. Report the hardcoded-secret and H2-console findings as "reported by README / implied by SecurityConfig, not verified", and tell the caller the file could not be opened. Related: [[jwt-expiry-enforced-by-jjwt-parse]], [[deliberate-training-bugs]].
