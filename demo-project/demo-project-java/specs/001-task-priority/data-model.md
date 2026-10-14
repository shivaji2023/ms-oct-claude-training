# Data Model: Task Priority

## TaskPriority (new enum)

| Value | Rank (for sorting) |
|-------|--------------------|
| `LOW` | 1 |
| `MEDIUM` | 2 |
| `HIGH` | 3 |

Descending sort = High, Medium, Low. Ascending sort = Low, Medium, High.

## Task (existing entity, one new field)

| Field | Type | Rules |
|-------|------|-------|
| id | Long | Existing, generated |
| title | String | Existing, required |
| description | String | Existing, optional |
| completed | boolean | Existing, default false |
| ownerId | Long | Existing, required; every operation is scoped to the authenticated user |
| createdAt | Instant | Existing |
| **priority** | TaskPriority | **New.** Never null; defaults to `MEDIUM`; stored as a string; cannot be cleared |

### Validation rules
- Priority must be one of `LOW`, `MEDIUM`, `HIGH`; anything else yields 400 (FR-006).
- Create without priority stores `MEDIUM` (FR-003).
- Update without priority leaves the stored value unchanged (FR-005).

### Migration
- Add column `priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM'`; existing rows become `MEDIUM`
  (FR-004). No data loss.

### State transitions
- Priority may change freely between the three values at any time through update. No transition
  restrictions; completing a task does not alter priority.

## Relationships
- Task belongs to exactly one owner (user) via `ownerId`; unchanged.
