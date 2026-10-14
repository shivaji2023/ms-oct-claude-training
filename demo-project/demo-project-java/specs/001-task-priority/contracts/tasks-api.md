# Contract: Tasks API (changes for task priority)

Base path `/api/tasks`. All endpoints require authentication and operate only on the
authenticated user's tasks. Only differences from the current contract are listed; everything
not mentioned is unchanged.

## Task representation (responses)

Existing fields are unchanged: `id`, `title`, `description`, `completed`, `ownerId`,
`createdAt`. **Added**: `priority` (`"LOW" | "MEDIUM" | "HIGH"`, never null).

```json
{ "id": 7, "title": "Pay rent", "description": null, "completed": false,
  "ownerId": 1, "createdAt": "2026-10-14T09:00:00Z", "priority": "MEDIUM" }
```

## GET /api/tasks

Optional query parameters (all optional; none supplied = today's behavior and order):

| Parameter | Values | Meaning |
|-----------|--------|---------|
| `priority` | `LOW`, `MEDIUM`, `HIGH`; repeatable | Keep tasks with any listed priority |
| `completed` | `true`, `false` | Keep tasks with that completion status |
| `sort` | `priority,desc` (High first), `priority,asc` (Low first) | Sort by priority; ties keep the unsorted relative order |

- Different parameters combine with AND; repeated `priority` values combine with OR.
- Responses: `200` with an array (possibly empty); `400` with `{"error": "..."}` for an unknown
  priority, non-boolean `completed`, or unsupported `sort`.

## POST /api/tasks

Body adds optional `priority`:

```json
{ "title": "Pay rent", "description": "Oct", "priority": "HIGH" }
```

- Omitted `priority` creates a `MEDIUM` task.
- Invalid value: `400`.

## PUT /api/tasks/{id}

Body adds optional `priority`.

- Omitted `priority` leaves the stored priority unchanged.
- Invalid value: `400`. Task not owned or not found: `404`.

## PATCH /api/tasks/{id}/complete and DELETE /api/tasks/{id}

Request and response shapes are unchanged. A task not owned by the caller now returns `404`
(previously these two operations did not check ownership).

## Compatibility summary

| Client behavior | Result |
|-----------------|--------|
| Sends no `priority`, no query parameters | Same as before; responses gain a `priority` field |
| Ignores unknown response fields | Unaffected |
| Strict clients rejecting unknown fields | Would see the new `priority` field (additive change) |
