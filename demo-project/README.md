# Task Manager — Claude Code Training Demo Project

The live demo project for the Claude Code hands-on training: a small, real,
full-stack Task Manager with email/password auth and a per-user task list.

It comes in **two editions**. They have the same Angular frontend, the same
REST API, and the same deliberate bugs. Only the backend language differs.
**Pick the one that matches the stack you work in.**

| Edition | Folder | Backend stack |
|---|---|---|
| **Java** | [`demo-project-java/`](demo-project-java/README.md) | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, H2 |
| **.NET** | [`demo-project-dotnet/`](demo-project-dotnet/README.md) | .NET 8, ASP.NET Core, EF Core, SQLite |

Both editions use **Angular 17** (standalone components) for the frontend.

## Structure

```
demo-project/
├── demo-project-java/
│   ├── backend/     ← Spring Boot REST API (port 8080)
│   └── frontend/    ← Angular app (port 4200)
├── demo-project-dotnet/
│   ├── backend/     ← ASP.NET Core REST API (port 8080)
│   └── frontend/    ← Angular app (port 4200) — identical to the Java edition
└── README.md
```

## Getting started

1. Choose an edition (see the table above).
2. Follow that edition's `README.md` to install prerequisites and run the backend and frontend.
3. Start Claude Code in that edition's folder. The next section shows how.

Only run one backend at a time — both listen on port 8080.

## Starting Claude Code

You make two choices: **which folder** to start Claude Code in, and **which
interface** to use.

### Choice 1 — Which folder

Claude Code treats the folder you start it in as your project. Everything you
build during the session is created there: `CLAUDE.md`, `.claude/settings.json`,
your commands, skills, agents and hooks.

| Start Claude Code in… | What happens | Use it? |
|---|---|---|
| `demo-project-java/` or `demo-project-dotnet/` | Claude sees one backend plus the frontend, and your settings and `CLAUDE.md` load as expected | ✅ **Yes — always start here** |
| `demo-project/` (this folder) | Claude sees **both** editions: reviews report every bug twice, and fixes may land in the wrong backend | ❌ No |
| `backend/` or `frontend/` | Claude can't see the other half of the app, and settings you create land in the wrong place | ❌ No |

**Rule of thumb:** open a terminal, `cd` into your edition's folder, *then*
start Claude. If you're unsure where you are, run `pwd` (macOS/Linux/Git Bash)
or `Get-Location` (PowerShell) first — the path should end in
`demo-project-java` or `demo-project-dotnet`.

Keep the backend and frontend running in their own terminals (as their READMEs
describe), and use a **third terminal** for Claude Code.

### Choice 2 — Which interface

All of these run the same Claude Code. Pick the one that fits how you work.
Whichever you choose, it must be opened **on your edition's folder**.

**Terminal (CLI) — recommended for this training.** The handbook's steps are
written for it.

```bash
cd demo-project/demo-project-java      # or: cd demo-project/demo-project-dotnet
claude
```

**VS Code** — good if you want to see diffs inline while Claude edits.

1. *File → Open Folder…* and select `demo-project-java` or `demo-project-dotnet` (not `demo-project`).
2. Open the Claude Code panel from the Claude icon in the sidebar, or run
   `claude` in VS Code's integrated terminal (*Terminal → New Terminal*).

**JetBrains (IntelliJ IDEA / Rider)** — natural for Java (IntelliJ) or .NET (Rider) developers.

1. *File → Open…* and select your edition's folder.
2. Run `claude` in the IDE's built-in terminal. With the Claude Code plugin installed, diffs open in the IDE's diff viewer.

**Claude desktop app** — if you'd rather not use a terminal.

Open the Code tab, then choose your edition's folder as the working folder
when starting a session.

### Quick check once Claude starts

Ask Claude:

```
Which folder are you working in, and which backend stack does this project use?
```

It should name your edition's folder and **one** backend (Spring Boot *or*
ASP.NET Core). If it mentions both, exit (`/exit`), `cd` into your
edition's folder, and start again.

## Deliberate issues (for training demos — do not fix before the session)

Both editions contain the same planted issues, in the equivalent places:

| # | Issue | Java edition | .NET edition |
|---|---|---|---|
| 1 | Token validity check always returns `true` (expired tokens accepted) | `JwtUtil.isTokenValid()` | `JwtUtil.IsTokenValid()` |
| 2 | No task-ownership check on complete/delete (IDOR) | `TaskService.markCompleted()` / `.delete()` | `TaskService.MarkCompletedAsync()` / `.DeleteAsync()` |
| 3 | Hardcoded JWT secret committed to the repo | `application.properties` → `app.jwt.secret` | `appsettings.json` → `App:Jwt:Secret` |
| 4 | Placeholder-only tests | `TaskServiceTest.java`, `AuthServiceTest.java` | `TaskServiceTest.cs`, `AuthServiceTest.cs` |

The Trainer Handbook's examples use the Java edition. The .NET edition's
README has a table mapping each handbook command, permission rule, and file
path to its .NET equivalent.
