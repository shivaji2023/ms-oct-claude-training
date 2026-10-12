# CLAUDE.md

## Project purpose

Full-stack Task Manager used as the live demo project for a Claude Code training course. It contains **deliberate defects** that specific training modules depend on (see "Deliberate issues" below) — do not fix them unless the user explicitly asks.

- `backend/` — Spring Boot 3.2 REST API, Java 17, Maven, H2 in-memory DB, JWT auth (jjwt 0.12). Port 8080.
- `frontend/` — Angular 17 standalone-component app, Karma/Jasmine. Port 4200.

Each has its own `CLAUDE.md` with tier-specific conventions and gotchas.

## Commands

Backend (run from `backend/`; no Maven wrapper is committed, use system `mvn`):

```bash
mvn spring-boot:run                                   # start API on :8080
mvn test                                              # all tests
mvn test -Dtest=TaskServiceTest                       # one test class
mvn test -Dtest=TaskServiceTest#methodName            # one test method
mvn package                                           # build jar
```

H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:taskdb`, user `sa`, blank password). Data is wiped on every restart; schema comes from JPA entities (`ddl-auto=update`).

Frontend (run from `frontend/`):

```bash
npm install
npm start                                   # ng serve on :4200
npm run build
npm test                                    # Karma + Jasmine (needs Chrome); no *.spec.ts files exist yet
npx ng test --watch=false --include='src/app/tasks/**/*.spec.ts'   # subset of specs
```

No linter is configured for either project.

## Architecture

**Auth flow (spans both tiers):** `POST /api/auth/register|login` → `AuthService` hashes/verifies with BCrypt → `JwtUtil` issues an HMAC-signed JWT whose `subject` is the user ID → frontend `auth.service.ts` stores it in `localStorage` (`task_api_token`) → `core/auth.interceptor.ts` attaches `Authorization: Bearer …` to every request → backend `JwtAuthFilter` parses it and sets the principal to the **user ID as a `Long`** → controllers receive it via `@AuthenticationPrincipal Long ownerId`. Fully stateless; no server sessions, CSRF disabled.

**Backend layout** is package-by-feature under `com.technizer.taskapi` (`auth`, `task`, `user`), each with controller → service → Spring Data repository. Cross-cutting pieces:
- `config/SecurityConfig` — filter chain, public paths (`/api/auth/**`, `/h2-console/**`), CORS allowing only `http://localhost:4200`, `PasswordEncoder` bean.
- `exception/GlobalExceptionHandler` — services signal errors by throwing `IllegalArgumentException` (→ 400) or `NoSuchElementException` (→ 404); anything else → generic 500. All bodies are `{"error": "..."}`. Follow this convention rather than returning `ResponseEntity` from services.
- Request/response DTOs are nested `record`s inside the controllers.
- `Task` references its owner by a plain `ownerId` column, not a JPA relationship to `User`.

**Frontend:** standalone components; providers wired in `app.config.ts`; routes in `app.routes.ts` (`/login`, `/tasks`, no route guards). API base URLs are hardcoded to `http://localhost:8080` in the services (no environment files), so the app only works when served from `http://localhost:4200` (the single CORS origin). Components re-fetch the full task list after each mutation.

## Deliberate issues (training material — leave as-is unless asked)

1. `JwtUtil.isTokenValid()` always returns `true` instead of checking the token. Used in the Module 13 security-review → issue → fix → commit demo. (jjwt still throws on expired or forged tokens inside `extractUserId()`, but that exception escapes the filter rather than being handled.)
2. `TaskService.markCompleted()` and `delete()` don't verify the task belongs to `ownerId` (IDOR).
3. `app.jwt.secret` is hardcoded in `backend/src/main/resources/application.properties`.
4. `AuthServiceTest` / `TaskServiceTest` contain only placeholder tests; real coverage is generated live in Modules 6 and 9–10.
