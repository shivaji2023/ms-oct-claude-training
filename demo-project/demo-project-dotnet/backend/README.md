# Task API — .NET backend

A .NET 8 / ASP.NET Core port of the Spring Boot backend in the Java edition of
this demo repo (`demo-project-java`). It exposes the same REST API on the same
port (8080), so the Angular frontend works unchanged.

- **Stack:** .NET 8, ASP.NET Core (controllers), EF Core + SQLite (in-memory),
  JWT (HS256), BCrypt password hashing
- **Tests:** xUnit (`tests/TaskApi.Tests`)

## Running

```bash
cd backend
dotnet run --project src/TaskApi    # http://localhost:8080
dotnet test                         # run the test project
```

Requires the .NET 8 SDK.

## Java → .NET map

| Java (`demo-project-java/backend/`)       | .NET (`backend/src/TaskApi/`)                   |
|-------------------------------------------|-------------------------------------------------|
| `TaskApiApplication.java`                 | `Program.cs`                                    |
| `application.properties`                  | `appsettings.json`                              |
| `auth/AuthController`, `AuthService`      | `Auth/AuthController.cs`, `Auth/AuthService.cs` |
| `auth/JwtUtil`                            | `Auth/JwtUtil.cs`                               |
| `auth/JwtAuthFilter`                      | `Auth/JwtAuthHandler.cs` (authentication handler) |
| `config/SecurityConfig` (+ BCrypt bean)   | `Config/SecurityConfig.cs`, `Auth/PasswordEncoder.cs` |
| `exception/GlobalExceptionHandler`        | `Exceptions/GlobalExceptionHandler.cs`          |
| `task/Task`, `TaskRepository`, `TaskService`, `TaskController` | `Tasks/TaskItem.cs`, `TaskRepository.cs`, `TaskService.cs`, `TaskController.cs` |
| `user/User`, `UserRepository`             | `Users/User.cs`, `Users/UserRepository.cs`      |
| JPA / H2                                  | `Data/AppDbContext.cs` / SQLite in-memory       |
| `IllegalArgumentException` → 400          | `ArgumentException` → 400                       |
| `NoSuchElementException` → 404            | `KeyNotFoundException` → 404                    |

## Deliberate issues (kept on purpose — same as the Java backend)

1. `JwtUtil.IsTokenValid()` always returns `true`, so token expiry is never checked.
2. `TaskService.MarkCompletedAsync()` and `.DeleteAsync()` don't check task
   ownership (IDOR).
3. `App:Jwt:Secret` in `appsettings.json` is a hardcoded demo secret.
4. `TaskServiceTest.cs` and `AuthServiceTest.cs` are placeholders only.

## Differences from the Java backend

- **Unauthenticated requests get `401`**, where Spring Security returns `403`.
- **Request validation is enforced.** The Java DTOs declare `@Email`/`@NotBlank`
  but the controllers don't use `@Valid`, so those checks never run. Here
  `[ApiController]` enforces them and returns `400 {"error": "..."}`.
- **No database console.** There is no equivalent of the H2 web console.
- **The task entity is `TaskItem`**, so it doesn't clash with `System.Threading.Tasks.Task`.
  The JSON shape is unchanged.
