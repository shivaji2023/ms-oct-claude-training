# Quickstart: Validating Task Priority

Contracts: [contracts/tasks-api.md](contracts/tasks-api.md). Data rules:
[data-model.md](data-model.md).

## Prerequisites
- Java 17 and Maven; Node and npm with the Angular CLI.
- Two registered users (A and B) and a JWT for each.
- The dev schema is generated automatically (`create`/`create-drop`), so no manual column
  step is needed. For any environment with a pre-existing, manually managed schema, add the
  `priority` column first (see data-model.md, Migration).

## Automated checks
1. Backend: `cd backend && mvn test`
2. Frontend: `cd frontend && npm test` (runs `ng test`)

Both must pass (constitution, Principle IV).

## Manual scenarios

Start the backend and frontend, then:

| # | Scenario | Expected |
|---|----------|----------|
| 1 | Create a task with no `priority` as user A | Response `priority` is `MEDIUM` |
| 2 | Create with `priority: "HIGH"` | Response `priority` is `HIGH` |
| 3 | PUT the task with only title and description | Priority unchanged |
| 4 | PUT with `priority: "LOW"` | Priority becomes `LOW` |
| 5 | Create or PUT with `priority: "URGENT"` or `""` | `400`, task unchanged |
| 6 | Pre-existing task fetched after rollout | `priority` is `MEDIUM` |
| 7 | `GET /api/tasks?priority=HIGH` | Only High tasks |
| 8 | `GET /api/tasks?priority=HIGH&priority=MEDIUM&completed=false` | Open High or Medium tasks |
| 9 | `GET /api/tasks?sort=priority,desc` | High, Medium, Low; equal priorities in original order |
| 10 | `GET /api/tasks` with no parameters | Same order as before the feature |
| 11 | User B lists, filters, fetches, completes, deletes user A's task | Never listed; direct access returns `404` |
| 12 | UI: set priority on create, change it on the detail page, apply filters and sort | List and badges reflect choices; clearing filters restores the full list |
