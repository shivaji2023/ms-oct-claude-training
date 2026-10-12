# Task Manager — Claude Code Training Demo Repo (.NET)

A small, real, full-stack app used as the live demo project for the Claude Code
training. It's intentionally simple: a Task Manager with email/password auth
and a per-user task list.

This is the **.NET edition** of the demo repo. It has the same Angular
frontend, REST API, and deliberate bugs as the Java edition
(`demo-project-java`). Pick whichever backend you're most comfortable with.

- **Backend:** .NET 8, ASP.NET Core (controllers), EF Core, SQLite (in-memory), JWT auth
- **Frontend:** Angular 17 (standalone components), TypeScript

## Structure

```
demo-project-dotnet/
├── backend/     ← ASP.NET Core REST API (port 8080)
│   ├── TaskApi.sln
│   ├── src/TaskApi/          ← Auth/, Config/, Data/, Exceptions/, Tasks/, Users/
│   └── tests/TaskApi.Tests/  ← xUnit tests
├── frontend/    ← Angular app (port 4200)
└── README.md
```

This repo does **not** include a `CLAUDE.md`, `.claude/` folder, or a
Spec Kit setup — those are created live by participants during the session
(Modules 4, 6, 7, 8, 9–10, and 11). Do not pre-create them.

## Prerequisites

- .NET 8 SDK (`dotnet --list-sdks` should show an `8.0.x` entry)
- Node.js 18+ and npm
- Claude Code installed and authenticated (`npm install -g @anthropic-ai/claude-code`)
- A GitHub repo this project has been pushed to (needed for the GitHub MCP
  and bug-lifecycle demo in Module 13)
- A Jira project/board (needed for the Jira MCP demo in Module 13)

## Running the backend

```bash
cd backend
dotnet run --project src/TaskApi
```

The API starts on `http://localhost:8080`. The database is an in-memory SQLite
database. It is created on startup and wiped when the API stops.

Run the tests with:

```bash
cd backend
dotnet test
```

## Running the frontend

```bash
cd frontend
npm install
npm start
```

The app starts on `http://localhost:4200` and expects the backend to be
running on port 8080 (CORS is already configured for this).

## API quick reference

```
POST /api/auth/register   { email, password, displayName }  → { token }
POST /api/auth/login      { email, password }                → { token }
GET    /api/tasks                                             (auth required)
POST   /api/tasks         { title, description }              (auth required)
PATCH  /api/tasks/{id}/complete                                (auth required)
DELETE /api/tasks/{id}                                         (auth required)
```

All `/api/tasks/**` endpoints require `Authorization: Bearer <token>`.

## Deliberate issues (for training demos — do not fix before the session)

These are intentional and are the basis for specific module demos. See the
Trainer Handbook for exactly where each one is used.

1. **`JwtUtil.IsTokenValid()`** (`backend/src/TaskApi/Auth/JwtUtil.cs`) always
   returns `true` — expired tokens are still accepted. Used in the Module 13
   `/security-review` → `/create-issue` → `/fix-issue` → `/commit`
   bug-lifecycle demo.
2. **`TaskService.MarkCompletedAsync()` and `.DeleteAsync()`**
   (`backend/src/TaskApi/Tasks/TaskService.cs`) do not check that the task
   belongs to the requesting user (broken object-level authorization / IDOR).
   Used as a secondary finding in the same demo, or as a standalone
   code-review skill demo in Module 6.
3. **`App:Jwt:Secret`** (`backend/src/TaskApi/appsettings.json`) is a
   hardcoded demo secret committed to the repo. Used to illustrate the `deny`
   list in `settings.json` (Module 4) and as a finding in the code-review
   skill (Module 6).
4. **Test coverage is placeholder-only** (`TaskServiceTest.cs`,
   `AuthServiceTest.cs`) — used for the auto test-writing skill demo
   (Module 6) and as a Spec Kit implementation target (Modules 9–10).

## Using the Trainer Handbook with this edition

The handbook's examples are written for the Java edition. Substitute these
when following along:

| Handbook (Java)                         | This repo (.NET)                              |
|-----------------------------------------|-----------------------------------------------|
| `mvn spring-boot:run`                   | `dotnet run --project src/TaskApi`            |
| `mvn test` / `mvn -f backend/pom.xml test` | `dotnet test` / `dotnet test backend`      |
| `mvn clean package`                     | `dotnet publish backend/src/TaskApi -c Release` |
| `Bash(mvn *)`                           | `Bash(dotnet *)`                              |
| `Write(backend/pom.xml)`                | `Write(**/*.csproj)`                          |
| `Read(**/application.properties)` (deny) | `Read(**/appsettings*.json)` (deny) — **required**, or the Module 4 secret demo won't block anything |
| `backend/src/main/java/com/technizer/taskapi/auth/` | `backend/src/TaskApi/Auth/`        |
| `Task.java`, `TaskController.java`      | `TaskItem.cs`, `TaskController.cs`            |
| `JwtAuthFilter` (Spring Security filter) | `JwtAuthHandler` (ASP.NET Core auth handler) |
| JPA / H2                                | EF Core / SQLite                              |
| JUnit 5, `src/test/java`                | xUnit, `backend/tests/TaskApi.Tests`          |
| Module 11 hook: `.java` → `fmt-maven-plugin` | `.cs` → `dotnet format backend/TaskApi.sln` |

## Trainer setup checklist

- [ ] Push this repo to a real GitHub repository (private is fine) before
      the session — Module 13's GitHub MCP demo needs a live remote
- [ ] Create a matching Jira project/board with at least one sample issue
      type configured — Module 13's Jira MCP demo needs a live project
- [ ] Confirm `dotnet run --project src/TaskApi` and `npm start` both work on
      the training machine the day before
- [ ] Do **not** run `/security-review` or fix the deliberate bugs before
      the session
