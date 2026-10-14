# Implementation Plan: Task Priority

**Branch**: `001-task-priority` | **Date**: 2026-10-14 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-task-priority/spec.md`

## Summary

Add a three-level priority (Low, Medium, High; default Medium) to tasks, and let users filter
the task list by one or more priorities and by completion status, and sort by priority. The
backend extends the existing `/api/tasks` resource additively: a new `priority` field on the
task, an optional `priority` on create/update bodies, and optional `priority`, `completed`, and
`sort` query parameters on the list endpoint. Filtering and sorting run only over the
authenticated owner's tasks. The Angular task list and detail views gain a priority selector,
filter controls, and a sort control, all via `TaskService`.

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.2.5); TypeScript on Angular 17

**Primary Dependencies**: Spring Web, Spring Data JPA, Spring Security, Bean Validation; Angular
Forms/Router/HttpClient. No new dependencies.

**Storage**: JPA entity `task` on H2 (dev); one new column `priority`

**Testing**: JUnit 5 + Mockito + AssertJ (`mvn test`); Jasmine/Karma (`ng test`)

**Target Platform**: Spring Boot web service plus Angular SPA in a browser

**Project Type**: Web application (`backend/` + `frontend/`)

**Performance Goals**: Filter/sort results visible in under 1 second for up to 500 tasks per
user (SC-003)

**Constraints**: Existing response fields unchanged; clients omitting priority/filters behave as
before; unsorted order unchanged; no existing tests removed

**Scale/Scope**: Hundreds of tasks per user; one entity, one list endpoint, two UI components

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Backend conventions (constructor injection, record DTOs, no entity exposure) | Pass | Request inputs and responses are records; a `TaskResponse` record replaces direct `Task` exposure with an identical JSON shape plus `priority`. |
| II. Frontend conventions (standalone, services own HTTP) | Pass | All new HTTP lives in `TaskService`; components stay standalone. |
| III. Task ownership enforcement | Pass (existing gap fixed here, FR-016) | `complete` and `delete` did not check ownership; this plan fixes both. New list filtering is owner-scoped at the query. |
| IV. Test-backed changes | Pass | Tests planned per layer (see quickstart). |
| V. API backward compatibility | Pass | Additive fields and optional parameters only. |

Post-design re-check: all gates pass with no deviations.

## Project Structure

### Documentation (this feature)

```text
specs/001-task-priority/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/
│   └── tasks-api.md     # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks - not created here)
```

### Source Code (repository root)

```text
backend/src/main/java/com/technizer/taskapi/
├── task/
│   ├── Task.java                  # + priority field
│   ├── TaskPriority.java          # NEW enum LOW, MEDIUM, HIGH
│   ├── TaskResponse.java          # NEW record: id, title, description, completed, ownerId, createdAt, priority
│   ├── TaskController.java        # priority on requests; TaskResponse returns; list query params
│   ├── TaskService.java           # priority handling, filter + stable sort, ownership checks (FR-016)
│   └── TaskRepository.java        # + findByOwnerIdOrderByIdAsc (defined baseline order)
└── exception/GlobalExceptionHandler.java   # 400 for malformed enum / bad query values

backend/src/test/java/com/technizer/taskapi/task/
├── TaskControllerTest.java        # extended
├── TaskServiceTest.java           # extended
├── TaskServiceUpdateTest.java     # extended
└── TaskServiceListTest.java       # NEW: filter/sort/ownership

frontend/src/app/tasks/
├── task.service.ts                # + priority on Task/create/update; list(filters)
├── task-list.component.ts         # priority select, filters, sort, priority badge
├── task-detail.component.ts       # show + change priority
└── *.spec.ts                      # extended
```

**Structure Decision**: Web application layout already in place; changes stay within the
existing `task` packages and `tasks` frontend folder, matching `CLAUDE.md` architecture.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
No constitution violations. One design trade-off is recorded for transparency:

| Choice | Why Needed | Simpler Alternative Rejected Because |
|--------|------------|-------------------------------------|
| Filter/sort in the service, not in the database query | Preserves the baseline order exactly (FR-010a) and keeps one owner-scoped query | Derived/JPQL queries for every filter combination multiply methods; in-memory is sufficient for hundreds of tasks per user |
