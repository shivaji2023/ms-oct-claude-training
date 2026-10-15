# Feature Specification: Task Due Date

**Feature Branch**: `002-task-due-date`

**Created**: 2026-10-15

**Status**: Draft

**Input**: User description: "Users can optionally set a due date on a task"

## Clarifications

### Session 2026-10-15

- Q: Besides setting and clearing a due date, what else is in scope? → A: An overdue indicator in the task list, sorting by due date, and filtering by due date.
- Q: Is the due date a calendar date or a date with a time? → A: Date only, with no time of day.
- Q: Can a due date be in the past? → A: Yes. Past dates are accepted on create and edit, and the task shows as overdue right away.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Set, change, or clear a due date (Priority: P1)

A signed-in user can optionally choose a due date when creating a task. They can change or remove it when editing the task. The due date is saved and shown with the task.

**Why this priority**: Without a stored due date, overdue marking, sorting, and filtering have nothing to act on. This is the foundation and a viable MVP on its own.

**Independent Test**: Create a task with a due date, reopen it and confirm the date is shown; edit it to a different date and confirm the new date; clear it and confirm the task has no due date.

**Acceptance Scenarios**:

1. **Given** a signed-in user on the create-task form, **When** they choose a due date and save, **Then** the task is saved and displayed with that due date.
2. **Given** a user creates a task without choosing a due date, **When** they save, **Then** the task is saved with no due date.
3. **Given** an existing task with a due date, **When** the user edits it and picks a different date, **Then** the task is displayed with the new date.
4. **Given** an existing task with a due date, **When** the user edits it and clears the date, **Then** the task is displayed with no due date.
5. **Given** a user picks a date earlier than today, **When** they save, **Then** the task is saved with that date.

---

### User Story 2 - Existing clients keep working (Priority: P1)

Clients and integrations built before this feature continue to create, edit, and list tasks without sending a due date and without breaking on responses.

**Why this priority**: Breaking current clients is unacceptable and is a constitutional requirement.

**Independent Test**: Run a pre-feature create/update/list request flow with no due date and confirm the same outcomes as before.

**Acceptance Scenarios**:

1. **Given** a client that omits the due date, **When** it creates a task, **Then** the request succeeds and the task has no due date.
2. **Given** a client that omits the due date, **When** it edits a task that has a due date, **Then** the request succeeds and the due date is unchanged.
3. **Given** a client built before this feature, **When** it lists or fetches tasks, **Then** all fields it previously received are still present with unchanged meaning.
4. **Given** tasks that existed before this feature, **When** the user views them, **Then** they show no due date.

---

### User Story 3 - See overdue tasks (Priority: P2)

A user can tell at a glance which incomplete tasks are past their due date, because the task list marks them as overdue.

**Why this priority**: Spotting late work is the main reason to set a due date.

**Independent Test**: With tasks due yesterday, today, and tomorrow, confirm only the task due yesterday is marked overdue; complete it and confirm the marker goes away.

**Acceptance Scenarios**:

1. **Given** an incomplete task with a due date before today, **When** the task list is shown, **Then** the task is clearly marked as overdue.
2. **Given** an incomplete task due today, **When** the task list is shown, **Then** it is not marked overdue.
3. **Given** a completed task with a due date before today, **When** the task list is shown, **Then** it is not marked overdue.
4. **Given** a task with no due date, **When** the task list is shown, **Then** it is not marked overdue.

---

### User Story 4 - Sort the task list by due date (Priority: P3)

A user sorts their task list by due date, soonest first or latest first.

**Why this priority**: Sorting is a convenience on top of storing due dates.

**Independent Test**: With tasks due on different dates and some with none, sort soonest-first and confirm the order, with tasks without a due date last; sort latest-first and confirm the reverse, again with no-date tasks last.

**Acceptance Scenarios**:

1. **Given** tasks with different due dates, **When** the user sorts soonest-first, **Then** earlier dates appear before later dates.
2. **Given** tasks with different due dates, **When** the user sorts latest-first, **Then** later dates appear before earlier dates.
3. **Given** tasks with and without due dates, **When** either due-date sort is applied, **Then** tasks with no due date appear last.
4. **Given** a due-date sort and a filter are both active, **When** the list is shown, **Then** the filtered tasks are displayed in the chosen order.

---

### User Story 5 - Filter the task list by due date (Priority: P3)

A user narrows their task list to overdue tasks, tasks due today, or tasks with no due date, alone or combined with the existing priority and completion filters.

**Why this priority**: Filtering lets users focus on what is late or due now, and builds on stored due dates.

**Independent Test**: With a mix of overdue, due-today, future, and no-date tasks, apply each due-date filter and confirm only the matching tasks appear; combine it with a priority filter and confirm only tasks matching both appear.

**Acceptance Scenarios**:

1. **Given** tasks with mixed due dates, **When** the user filters by overdue, **Then** only incomplete tasks with a due date before today are listed.
2. **Given** tasks with mixed due dates, **When** the user filters by due today, **Then** only tasks due today are listed.
3. **Given** tasks with mixed due dates, **When** the user filters by no due date, **Then** only tasks without a due date are listed.
4. **Given** several due-date options are selected, **When** the list is shown, **Then** tasks matching any selected option are listed.
5. **Given** a due-date filter and a priority or completion filter are both applied, **When** the list is shown, **Then** only tasks matching both are listed.
6. **Given** filters that match no tasks, **When** the list is shown, **Then** an empty-state message is displayed.
7. **Given** filters are applied, **When** the user clears them, **Then** all of their tasks are listed again.

---

### Edge Cases

- A due date that is malformed, not a real calendar date, or includes a time of day is rejected with a clear validation error and no task is changed.
- Clearing the due date on a task that has none succeeds and changes nothing.
- A user attempts to view, edit, or filter another user's tasks: those tasks are never returned or modified, and their existence is not revealed.
- A filter or sort value is unrecognized: the request is rejected with a clear validation error rather than silently ignored.
- The date rolls over to the next day: a task due the previous day is shown as overdue from then on.
- Many tasks share the same due date or have none: they keep the same relative order as the unsorted list.
- A user has no tasks matching the filters: an empty result is shown, not an error.
- A priority sort and a due-date sort are both requested at once: the request is rejected with a clear validation error, since only one sort can be active at a time.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Users MUST be able to optionally set a due date, as a calendar date with no time of day, when creating a task.
- **FR-002**: Users MUST be able to change a task's due date when editing it.
- **FR-003**: Users MUST be able to clear a task's due date when editing it, leaving the task with no due date.
- **FR-004**: Tasks created without a due date MUST have no due date, and tasks that existed before this feature MUST have no due date.
- **FR-005**: Edits that omit a due date MUST leave the task's existing due date unchanged; clearing MUST be an explicit action distinct from omitting.
- **FR-006**: System MUST accept due dates that are earlier than today, and MUST reject values that are not a valid calendar date, or that include a time of day, with a clear validation error.
- **FR-007**: The task list MUST display each task's due date when it has one.
- **FR-008**: System MUST treat a task as overdue only when it is incomplete, has a due date, and that date is before the current date.
- **FR-009**: The task list MUST clearly mark overdue tasks; a task due today, a completed task, and a task with no due date MUST NOT be marked overdue.
- **FR-010**: Users MUST be able to sort their task list by due date, soonest first or latest first, with tasks that have no due date always last; tasks with an equal due date or none MUST keep their existing unsorted relative order.
- **FR-011**: Only one sort MAY be active at a time. With no sort selected, the task list order MUST remain exactly as it is today.
- **FR-012**: Users MUST be able to filter their task list by one or more of: overdue, due today, no due date; when several are selected, a task matches if it matches any of them.
- **FR-013**: The due-date filter MUST combine with the existing priority and completion-status filters with AND semantics.
- **FR-014**: Filtering and sorting MUST apply only to the authenticated user's own tasks; users MUST never see or modify another user's tasks, and every task view and edit operation MUST continue to enforce task ownership.
- **FR-015**: All fields in existing task responses MUST remain present with unchanged names, types, and meanings; the due date is additive.
- **FR-016**: Clients that do not send a due date, filter, or sort options MUST continue to work with the same results as before, apart from the added due date.

### Key Entities

- **Task**: A user's to-do item with title, description, completion status, priority, and now an optional **due date**. Belongs to exactly one user.
- **Due Date**: An optional calendar date on a task, with no time of day. A task with no due date is never overdue.
- **Overdue**: A derived state, not stored: the task is incomplete and its due date is before the current date.
- **User**: The authenticated owner of tasks; may only access their own tasks.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can set, change, or clear a due date in a single save, with no more steps than editing the task before this feature.
- **SC-002**: 100% of tasks that existed before this feature show no due date after release, with no data loss.
- **SC-003**: Applying a due-date filter or sort updates the visible list in under 1 second for a user with up to 500 tasks.
- **SC-004**: In 100% of tested cases, filtered and sorted lists contain only the requesting user's tasks.
- **SC-005**: 100% of requests from clients that predate this feature succeed with the same outcomes as before.
- **SC-006**: Users can find all of their overdue incomplete tasks in 2 interactions or fewer from the task list.

## Assumptions

- "Today" and "the current date" mean the user's own local calendar date.
- Each task has at most one due date; recurring dates, start dates, and date ranges are out of scope.
- Reminders, notifications, and due-date-based automation are out of scope.
- Filter and sort choices are not saved across sessions.
- Existing authentication and task ownership rules are reused unchanged.
- This feature extends the filtering and sorting introduced by the task priority feature (001-task-priority); the priority sort and due-date sort are mutually exclusive.
- Users choose dates with a standard date picker control.
