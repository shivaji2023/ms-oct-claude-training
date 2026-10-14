---

description: "Task list for Task Priority"
---

# Tasks: Task Priority

**Input**: Design documents from `/specs/001-task-priority/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/tasks-api.md, quickstart.md

**Tests**: Included. The constitution (Principle IV) and `CLAUDE.md` require tests for every code change; `mvn test` and `npm test` MUST pass. Existing tests MUST NOT be removed; where signatures change, existing tests are edited, never dropped.

**Organization**: Tasks are grouped by user story so each can be implemented and tested independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: User story the task belongs to (US1-US4)

## Path Conventions

- Backend: `backend/src/main/java/com/technizer/taskapi/`, tests under `backend/src/test/java/com/technizer/taskapi/`
- Frontend: `frontend/src/app/tasks/`

---

## Phase 1: Setup

**Purpose**: Confirm the baseline before changing anything

- [X] T001 Run `mvn test` in `backend/` and `npm test` in `frontend/` to record the passing baseline before changes
- [X] T002 Ask the user to confirm the schema-generation mode (`application.properties` is not readable by the assistant and its secrets MUST NOT be changed) and record the answer in `specs/001-task-priority/research.md` under R2; if the schema is not auto-updated, add a manual `ALTER TABLE task ADD COLUMN priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM'` step to `specs/001-task-priority/quickstart.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core pieces every user story depends on

**CRITICAL**: No user story work can begin until this phase is complete

- [X] T003 Create enum `TaskPriority { LOW, MEDIUM, HIGH }` (declaration order = ranking) in `backend/src/main/java/com/technizer/taskapi/task/TaskPriority.java`
- [X] T004 Add `priority` field to `backend/src/main/java/com/technizer/taskapi/task/Task.java`: type `TaskPriority`, `@Enumerated(EnumType.STRING)`, never null, defaults to `MEDIUM`, database default `'MEDIUM'` so existing rows become `MEDIUM`; getter returns `MEDIUM` if the stored value is null; add getter/setter
- [X] T005 Create record `TaskResponse(Long id, String title, String description, boolean completed, Long ownerId, Instant createdAt, TaskPriority priority)` with a static `from(Task)` factory in `backend/src/main/java/com/technizer/taskapi/task/TaskResponse.java`; field names and types MUST match today's `Task` JSON (Principle I, FR-013)
- [X] T006 [P] Add tests in `backend/src/test/java/com/technizer/taskapi/task/TaskResponseTest.java` for `from(Task)` mapping and that serialized JSON keys are exactly `id`, `title`, `description`, `completed`, `ownerId`, `createdAt`, `priority`
- [X] T007 [P] Add handlers in `backend/src/main/java/com/technizer/taskapi/exception/GlobalExceptionHandler.java` for `HttpMessageNotReadableException` and `MethodArgumentTypeMismatchException` returning HTTP 400 with `{"error": "..."}` (invalid priority/filter values MUST NOT fall through to the 500 handler)
- [X] T008 [P] Add tests for the two new 400 handlers in `backend/src/test/java/com/technizer/taskapi/exception/GlobalExceptionHandlerTest.java`
- [X] T009 [P] Add tests for `TaskPriority` ranking order and the `Task` default/null-safe priority getter in `backend/src/test/java/com/technizer/taskapi/task/TaskPriorityTest.java`
- [X] T010 Add `List<Task> findByOwnerIdOrderByIdAsc(Long ownerId)` to `backend/src/main/java/com/technizer/taskapi/task/TaskRepository.java` and make `TaskService.listForOwner` in `backend/src/main/java/com/technizer/taskapi/task/TaskService.java` use it (baseline order: oldest first, FR-010a)
- [X] T011 Update the existing `listForOwner` stubs/assertions in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceTest.java` to the ordered finder (edit only; no test removed) and add a test that the list uses id-ascending order
- [X] T012 Enforce ownership in `TaskService.markCompleted` and `TaskService.delete` in `backend/src/main/java/com/technizer/taskapi/task/TaskService.java` (FR-016): another user's task and a missing task both throw `NoSuchElementException` (same pattern as `getForOwner`); remove the TODO comment
- [X] T013 Add tests for T012 (owner can complete/delete; non-owner and missing task get not-found; no save/delete performed for non-owner) in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceOwnershipTest.java` (FR-016)
- [X] T014 Change `TaskController` list/get/create/update/complete return types to `TaskResponse` (mapping with `TaskResponse.from`) in `backend/src/main/java/com/technizer/taskapi/task/TaskController.java`
- [X] T015 Update the assertions in the existing `backend/src/test/java/com/technizer/taskapi/task/TaskControllerTest.java` to the `TaskResponse` return type (edit only; no test or case removed)

**Checkpoint**: Foundation ready; user stories can begin

---

## Phase 3: User Story 1 - Set a priority on a task (Priority: P1) MVP

**Goal**: Users choose Low/Medium/High when creating a task and can change it when editing; the priority is saved and shown.

**Independent Test**: Create a task with High, reopen it and see High; change it to Low and see Low; create without choosing and see Medium.

### Tests for User Story 1

- [ ] T016 [P] [US1] Extend `backend/src/test/java/com/technizer/taskapi/task/TaskServiceTest.java` with create tests: explicit priority stored, null priority stored as `MEDIUM`
- [ ] T017 [P] [US1] Extend `backend/src/test/java/com/technizer/taskapi/task/TaskServiceUpdateTest.java` with update tests: new priority applied, null priority leaves the existing value unchanged, non-owner gets not-found
- [ ] T018 [P] [US1] Extend `backend/src/test/java/com/technizer/taskapi/task/TaskControllerTest.java` with tests that create/update pass `priority` from the request records to the service, and that an empty-string `priority` in a request body is rejected with 400 (FR-006)
- [ ] T019 [P] [US1] Extend `frontend/src/app/tasks/task.service.spec.ts` with tests that `create` and `update` send `priority` in the request body and that the `Task` response exposes `priority`
- [ ] T020 [P] [US1] Extend `frontend/src/app/tasks/task-list.component.spec.ts` with tests for the priority selector on the create row (defaults to MEDIUM, selected value passed to the service) and the priority label on each task
- [ ] T021 [P] [US1] Extend `frontend/src/app/tasks/task-detail.component.spec.ts` with tests that the detail view shows priority and that changing it calls `TaskService.update` with title, description, and the new priority

### Implementation for User Story 1

- [ ] T022 [US1] Add optional `TaskPriority priority` to `CreateTaskRequest` and `UpdateTaskRequest` records in `backend/src/main/java/com/technizer/taskapi/task/TaskController.java` and pass it through to the service
- [ ] T023 [US1] Update `TaskService.create` and `TaskService.update` in `backend/src/main/java/com/technizer/taskapi/task/TaskService.java` to take a `TaskPriority` parameter (no deprecated overloads): create stores the given priority or `MEDIUM` when null; update changes priority only when non-null
- [ ] T024 [US1] Update the existing call sites of `create`/`update` in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceTest.java`, `TaskServiceUpdateTest.java`, `TaskServiceGetTest.java`, and `TaskControllerTest.java` for the new signatures (edit calls only; no test removed)
- [ ] T025 [P] [US1] Add `priority: 'LOW' | 'MEDIUM' | 'HIGH'` to the `Task` interface, extend `create(title, description, priority?)`, and add `update(id, title, description, priority?)` in `frontend/src/app/tasks/task.service.ts`
- [ ] T026 [US1] Add a priority `<select>` (default MEDIUM) to the create row and show each task's priority in `frontend/src/app/tasks/task-list.component.ts`
- [ ] T027 [US1] Show priority and add a priority `<select>` that saves via `TaskService.update` in `frontend/src/app/tasks/task-detail.component.ts`

**Checkpoint**: User Story 1 works and is testable on its own

---

## Phase 4: User Story 2 - Existing clients keep working (Priority: P1)

**Goal**: Clients that send no priority, no filters, and no sort behave exactly as before, apart from the extra `priority` response field.

**Independent Test**: Run a pre-feature create/update/list flow without priority; outcomes match the old behavior and new tasks are Medium.

Create and update compatibility is verified here; list compatibility is verified in Phase 8, after the list parameters exist.

- [ ] T028 [P] [US2] Add JSON-shape and request-body compatibility tests in `backend/src/test/java/com/technizer/taskapi/task/TaskJsonCompatibilityTest.java`: serialized `TaskResponse` keeps `id`, `title`, `description`, `completed`, `ownerId`, `createdAt` with unchanged types plus `priority`; request bodies without `priority` deserialize successfully for create and update

**Checkpoint**: Create/update compatibility verified

---

## Phase 5: User Story 3 - Filter the task list (Priority: P2)

**Goal**: Users filter their list by one or more priorities and by completion status; filters combine with AND, multiple priorities with OR; only the user's own tasks are ever returned.

**Independent Test**: With mixed tasks, a High filter shows only High; adding not-completed shows only open High; clearing restores all.

### Tests for User Story 3

- [ ] T029 [P] [US3] Add filter tests in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceListTest.java`: single priority, multiple priorities (OR), completed true/false, priority AND completed, no matches returns an empty list, repository is queried only by owner id
- [ ] T030 [P] [US3] Extend `backend/src/test/java/com/technizer/taskapi/task/TaskControllerTest.java` with tests that `priority` (repeatable) and `completed` query values reach the service and the owner id from the principal is always used
- [ ] T031 [P] [US3] Extend `frontend/src/app/tasks/task.service.spec.ts` with tests that `list(filters)` builds `priority` and `completed` query params and omits them when unset
- [ ] T032 [P] [US3] Extend `frontend/src/app/tasks/task-list.component.spec.ts` with tests for the priority and status filter controls, re-querying on change, the empty-state message, and clearing filters

### Implementation for User Story 3

- [ ] T033 [US3] Add `@RequestParam(required = false) List<TaskPriority> priority` and `@RequestParam(required = false) Boolean completed` to `GET /api/tasks` in `backend/src/main/java/com/technizer/taskapi/task/TaskController.java`, and update the existing `list(...)` call sites in `TaskControllerTest.java` and `TaskServiceGetTest.java` for the new signature (edit calls only; no test removed)
- [ ] T034 [US3] Implement filtering in `backend/src/main/java/com/technizer/taskapi/task/TaskService.java`: load `findByOwnerIdOrderByIdAsc(ownerId)`, keep tasks matching any given priority AND the given completion status when provided
- [ ] T035 [P] [US3] Add an optional filter object to `list()` building `HttpParams` in `frontend/src/app/tasks/task.service.ts`
- [ ] T036 [US3] Add priority (multi-select) and status filter controls, an empty-state message, and a clear-filters action in `frontend/src/app/tasks/task-list.component.ts`

**Checkpoint**: Filtering works on its own on top of US1

---

## Phase 6: User Story 4 - Sort the task list by priority (Priority: P3)

**Goal**: Users sort by priority high-to-low or low-to-high; equal priorities keep their existing relative order; with no sort the order is unchanged.

**Independent Test**: With Low/Medium/High tasks, descending shows High, Medium, Low; ascending reverses; ties keep original order.

### Tests for User Story 4

- [ ] T037 [P] [US4] Add sort tests in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceListTest.java`: `desc` gives High, Medium, Low; `asc` reverses; equal priorities keep id-ascending order (stability); sort combined with filters; unsupported sort value rejected with `IllegalArgumentException`
- [ ] T038 [P] [US4] Extend `backend/src/test/java/com/technizer/taskapi/task/TaskControllerTest.java` with a test that the `sort` query value reaches the service
- [ ] T039 [P] [US4] Extend `frontend/src/app/tasks/task.service.spec.ts` with a test that `list` sends `sort=priority,desc|asc`
- [ ] T040 [P] [US4] Extend `frontend/src/app/tasks/task-list.component.spec.ts` with tests for the sort control (default none, high-first, low-first) and that it combines with active filters

### Implementation for User Story 4

- [ ] T041 [US4] Add `@RequestParam(required = false) String sort` to `GET /api/tasks` in `backend/src/main/java/com/technizer/taskapi/task/TaskController.java`
- [ ] T042 [US4] Implement stable sort by `TaskPriority` rank in `backend/src/main/java/com/technizer/taskapi/task/TaskService.java` (`priority,desc` = High first, `priority,asc` = Low first); reject other `sort` values with `IllegalArgumentException("Unsupported sort: ...")`; apply after filtering
- [ ] T043 [P] [US4] Add a `sort` option to the `list()` filter object in `frontend/src/app/tasks/task.service.ts`
- [ ] T044 [US4] Add a sort control (None, High first, Low first) in `frontend/src/app/tasks/task-list.component.ts`

**Checkpoint**: All four stories work together

---

## Phase 7: User Story 2 (continued) - List compatibility

**Purpose**: Verify unchanged list behavior now that list parameters exist

- [ ] T045 [P] [US2] Add a test in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceListTest.java` that with no filter/sort arguments the list returns the owner's tasks in id-ascending order, unchanged
- [ ] T046 [US2] Verify `GET /api/tasks` with no query parameters applies no filtering or sorting in `backend/src/main/java/com/technizer/taskapi/task/TaskController.java` and `TaskService.java`
- [ ] T047 [US2] Run all pre-existing backend and frontend tests and confirm none were removed and any edited ones changed only signatures or return types

---

## Phase 8: Polish & Cross-Cutting Concerns

- [ ] T048 Run `mvn test` in `backend/` and fix any failures
- [ ] T049 Run `npm test` in `frontend/` and fix any failures
- [ ] T050 [P] Add a test in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceListTest.java` that filters and sorts 500 tasks and completes well under 1 second (SC-003)
- [ ] T051 [P] Add a cross-user test in `backend/src/test/java/com/technizer/taskapi/task/TaskServiceListTest.java` proving filtered and sorted lists never include another owner's tasks (SC-004)
- [ ] T052 Walk through the manual scenarios in `specs/001-task-priority/quickstart.md` against the running app, including cross-user access (scenario 11)
- [ ] T053 Re-check the constitution gates in `specs/001-task-priority/plan.md` and update the document if the implementation diverged

---

## Dependencies & Execution Order

### Phase dependencies
- Setup (Phase 1) has no dependencies.
- Foundational (Phase 2) depends on Setup and blocks all stories.
- US1 (Phase 3) depends on Foundational; US2 create/update checks (Phase 4) depend on US1.
- US3 depends on US1. US4 depends on US1 and shares the list endpoint with US3.
- US2 list compatibility (Phase 7) runs after US3 and US4, since it needs the list parameters.
- Polish depends on all stories.

### Within a story
- Tests first and failing, then backend, then the frontend service, then components.
- `TaskService.java`, `TaskController.java`, `task.service.ts`, and `task-list.component.ts` are touched by several stories; tasks editing the same file are not parallel.

### Story completion order
US1 -> US2 (create/update) -> US3 -> US4 -> US2 (list) -> Polish

## Parallel Opportunities

- Foundational: T006, T007/T008, T009 in parallel once T003–T005 are done.
- US1 tests T016–T021 touch different files and run in parallel; backend (T022–T024) and frontend service (T025) can proceed in parallel.
- US3 tests T029–T032 in parallel; T035 beside T033/T034.
- US4 tests T037–T040 in parallel; T043 beside T041/T042.
- Polish: T050 and T051 in parallel.

### Parallel example: User Story 1

```text
Task: "Extend TaskServiceTest.java with create tests"            (T016)
Task: "Extend TaskServiceUpdateTest.java with update tests"      (T017)
Task: "Extend task.service.spec.ts for priority"                 (T019)
Task: "Extend task-list.component.spec.ts for priority"          (T020)
```

## Implementation Strategy

### MVP first
1. Phases 1-2 (setup and foundation, including the `TaskResponse` record and ownership fix).
2. Phase 3 (US1): priority can be set, changed, and seen.
3. Phase 4 (US2 create/update): existing clients verified unaffected.
4. Stop and validate; this is a shippable increment.

### Incremental delivery
- Add US3 (filtering), validate, then US4 (sorting), validate; then run the US2 list compatibility checks.

## Notes

- Do not modify secrets in `application.properties`; flag any needed change explicitly (T002 asks the user rather than editing).
- Do not add dependencies; none are required.
- Commit using Conventional Commits (`feat:`, `test:`, `fix:`).

---

## Phase 9: Convergence

**Purpose**: Gaps found by `/speckit-converge` that no earlier task covers. Remaining feature work is tracked by the unchecked tasks above.

- [ ] T054 Add a JPA persistence test in `backend/src/test/java/com/technizer/taskapi/task/TaskPersistenceTest.java` (`@DataJpaTest` on H2) that boots the schema, saves a `Task` with and without an explicit priority, and asserts the `priority` column defaults to `MEDIUM`, is stored as the enum name string, and reloads as `MEDIUM`/`HIGH` as saved, proving the `columnDefinition` in `Task.java` is valid DDL per FR-004, SC-002 (partial)
- [ ] T055 Add a MockMvc-level test in `backend/src/test/java/com/technizer/taskapi/task/TaskPriorityValidationTest.java` (use the `authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of()))` post-processor from `backend/CLAUDE.md`; run after T022 and T033) asserting that an unknown or empty-string `priority` in a create/update body, and an unknown `priority` or non-boolean `completed` query value, each return HTTP 400 with `{"error": ...}` through `GlobalExceptionHandler` and leave the task unchanged per FR-006 (partial)
