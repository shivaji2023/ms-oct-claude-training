# Feature Specification: Task Priority

**Feature Branch**: `001-task-priority`

**Created**: 2026-10-14

**Status**: Draft

**Input**: User description: "Let users set a priority of Low, Medium, or High on a task when creating or editing it. In the task list, users can filter tasks by priority and by completion status, and sort by priority. Tasks that existed before this change are treated as Medium priority. Clients that don't send a priority must keep working, existing API responses must not break, and users only ever see their own tasks."

## Clarifications

### Session 2026-10-14

- Q: When sorting by priority, how are same-priority tasks ordered, and what is the order with no sort chosen? → A: No sort selected keeps today's order unchanged; sorting by priority keeps tasks of equal priority in that same existing order.
- Q: Can a priority be cleared once set, or must every task always have one? → A: Every task always has Low, Medium, or High; priority cannot be cleared and no "clear" option is offered.
- Q: How do the priority and completion-status filters combine? → A: Different filters combine with AND; the priority filter accepts one or more priorities combined with OR.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Set a priority on a task (Priority: P1)

A signed-in user chooses Low, Medium, or High when creating a task, and can change it when editing the task. The chosen priority is saved and shown with the task.

**Why this priority**: Without a stored priority, filtering and sorting have nothing to act on. This is the foundation and a viable MVP on its own.

**Independent Test**: Create a task with High priority, reopen it, and confirm High is shown; edit it to Low and confirm Low is shown.

**Acceptance Scenarios**:

1. **Given** a signed-in user on the create-task form, **When** they select High and save, **Then** the task is saved and displayed with High priority.
2. **Given** an existing task with Medium priority, **When** the user edits it and selects Low, **Then** the task is displayed with Low priority.
3. **Given** a user creates a task without choosing a priority, **When** they save, **Then** the task is saved with Medium priority.
4. **Given** a task that existed before this feature, **When** the user views it, **Then** it shows Medium priority.

---

### User Story 2 - Existing clients keep working (Priority: P1)

Clients and integrations built before this feature continue to create, edit, and list tasks without sending a priority and without breaking on responses.

**Why this priority**: Breaking current clients is unacceptable and is a constitutional requirement.

**Independent Test**: Run a pre-feature create/update/list request flow with no priority and confirm the same outcomes as before, with tasks defaulting to Medium.

**Acceptance Scenarios**:

1. **Given** a client that omits priority, **When** it creates a task, **Then** the request succeeds and the task has Medium priority.
2. **Given** a client that omits priority, **When** it edits a task that has High priority, **Then** the request succeeds and the priority remains High.
3. **Given** a client built before this feature, **When** it lists or fetches tasks, **Then** all fields it previously received are still present with unchanged meaning.

---

### User Story 3 - Filter the task list (Priority: P2)

A user narrows their task list by priority (Low, Medium, High) and by completion status (completed or not completed), alone or combined.

**Why this priority**: Filtering lets users focus on what matters, and it builds directly on stored priorities.

**Independent Test**: With tasks of mixed priority and completion, apply a High filter and confirm only High tasks appear; add a not-completed filter and confirm only incomplete High tasks appear.

**Acceptance Scenarios**:

1. **Given** tasks with mixed priorities, **When** the user filters by High, **Then** only High priority tasks are listed. Selecting High and Medium lists tasks of either priority.
2. **Given** tasks with mixed completion states, **When** the user filters by completed, **Then** only completed tasks are listed.
3. **Given** both filters are applied, **When** the list is shown, **Then** only tasks matching both are listed.
4. **Given** filters that match no tasks, **When** the list is shown, **Then** an empty-state message is displayed.
5. **Given** filters are applied, **When** the user clears them, **Then** all of their tasks are listed again.

---

### User Story 4 - Sort the task list by priority (Priority: P3)

A user sorts their task list by priority, with High first or Low first.

**Why this priority**: Sorting is a convenience on top of storing and filtering priority.

**Independent Test**: With Low, Medium, and High tasks, sort by priority descending and confirm order High, Medium, Low; sort ascending and confirm the reverse.

**Acceptance Scenarios**:

1. **Given** tasks of mixed priority, **When** the user sorts by priority high-to-low, **Then** High tasks appear before Medium, then Low.
2. **Given** tasks of mixed priority, **When** the user sorts low-to-high, **Then** the order is reversed.
3. **Given** a sort and a filter are both active, **When** the list is shown, **Then** the filtered tasks are displayed in the chosen order.

---

### Edge Cases

- A priority value other than Low, Medium, or High is submitted: the request is rejected with a clear validation error and no task is changed.
- A user attempts to view, edit, or filter another user's tasks: those tasks are never returned or modified, and their existence is not revealed.
- A user attempts to complete or delete another user's task: the request fails as not found and the task is unchanged.
- A filter value is unrecognized: the request is rejected with a clear validation error rather than silently ignored.
- Many tasks share the same priority: tasks within the same priority keep the same relative order as the unsorted list.
- A user has no tasks matching the filters: an empty result is shown, not an error.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Users MUST be able to set a priority of Low, Medium, or High when creating a task.
- **FR-002**: Users MUST be able to change a task's priority when editing it.
- **FR-003**: Tasks created without a priority MUST be assigned Medium priority.
- **FR-004**: Tasks that existed before this feature MUST be treated as Medium priority.
- **FR-005**: Edits that omit a priority MUST leave the task's existing priority unchanged.
- **FR-006**: System MUST reject priority values other than Low, Medium, or High with a clear validation error. Every task MUST always have exactly one of these priorities; a task's priority cannot be cleared or left empty. An empty string is rejected; an omitted or null priority is treated as not provided (see FR-003 and FR-005).
- **FR-007**: Users MUST be able to filter their task list by one or more priorities; when several priorities are selected, a task matches if it has any of them.
- **FR-008**: Users MUST be able to filter their task list by completion status.
- **FR-009**: Priority and completion-status filters MUST be combinable with AND semantics, returning only tasks that match the priority filter and the completion-status filter.
- **FR-010**: Users MUST be able to sort their task list by priority, ascending or descending, with the order High > Medium > Low; tasks of equal priority MUST keep their existing unsorted relative order.
- **FR-010a**: When no sort is selected, the task list order MUST remain exactly as it is today: the user's tasks ordered by creation, oldest first (id ascending).
- **FR-011**: Filtering and sorting MUST apply only to the authenticated user's own tasks; users MUST never see or modify another user's tasks.
- **FR-012**: Every task view and edit operation MUST continue to enforce task ownership for the authenticated user.
- **FR-013**: All fields in existing task responses MUST remain present with unchanged names, types, and meanings; the priority is additive.
- **FR-014**: Clients that do not send a priority, filters, or sort options MUST continue to work with the same results as before, apart from the added priority.
- **FR-015**: The task list MUST display each task's priority.
- **FR-016**: Completing and deleting a task MUST enforce task ownership for the authenticated user; a task owned by another user, or a missing task, MUST be reported as not found and left unchanged.

### Key Entities

- **Task**: A user's to-do item with title, description, completion status, and now a **priority**. Belongs to exactly one user.
- **Priority**: One of Low, Medium, High, ordered High > Medium > Low. Defaults to Medium.
- **User**: The authenticated owner of tasks; may only access their own tasks.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can set or change a task's priority in a single save, with no more steps than editing the task before this feature.
- **SC-002**: 100% of tasks that existed before this feature display as Medium priority after release, with no data loss.
- **SC-003**: Applying a filter or sort updates the visible list in under 1 second for a user with up to 500 tasks.
- **SC-004**: In 100% of tested cases, filtered and sorted lists contain only the requesting user's tasks.
- **SC-005**: 100% of requests from clients that predate this feature succeed with the same outcomes as before.
- **SC-006**: Users can find all of their High priority incomplete tasks in 2 interactions or fewer from the task list.

## Assumptions

- Priority is a single value per task with exactly three levels; custom levels are out of scope.
- Filtering and sorting are available in the task list UI and the underlying task listing, with no persistence of the user's last-used filters across sessions.
- Completion status filtering offers completed and not completed, plus an unfiltered default.
- Sorting by priority is optional; the default list order stays as it is today (creation order, oldest first).
- Existing authentication and task ownership rules are reused unchanged.
- Notifications, reminders, due dates, and priority-based automation are out of scope.
