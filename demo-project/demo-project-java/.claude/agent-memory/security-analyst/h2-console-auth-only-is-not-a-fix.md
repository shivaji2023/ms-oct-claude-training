---
name: h2-console-auth-only-is-not-a-fix
description: Moving /h2-console from permitAll to authenticated (PR #2) still exposes it to any self-registered user; MockMvc tests cannot prove the H2 servlet is blocked
metadata:
  type: feedback
---

PR #2 (branch fix/issue-1, reviewed 2026-10-16) removed `/h2-console/**` from permitAll so it falls under `anyRequest().authenticated()`. `/api/auth/register` is open, though, so anyone can register, get a JWT, and send it as a Bearer header to the console. H2 is 2.2.224 and the DB password is empty, so the console gives full SQL access (password hashes) and RCE through `CREATE ALIAS`. The H2 servlet's `webAllowOthers` setting (Spring Boot default false) blocks non-localhost callers, but it can't be checked because application.properties is denied ([[application-properties-access-denied]]). A reverse proxy on the same host would also defeat it.

**Why:** "Requires auth" sounds like a fix but isn't one when registration is self-service. Also, `@SpringBootTest` + MockMvc only dispatches to DispatcherServlet, never to the H2 `ServletRegistrationBean`. An authenticated MockMvc test would return 404 even though real Tomcat would serve the console.

**How to apply:** Rate it High until the console is disabled on the default profile (spec FR-005/006) or set to `denyAll`. Ask for RANDOM_PORT + TestRestTemplate tests with a valid token. Check this again once application.properties or a dev profile changes. Related: [[deliberate-training-bugs]].
