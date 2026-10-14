# Research: Task Priority

No `NEEDS CLARIFICATION` items remained after `/speckit-clarify`. The decisions below resolve
the design choices the spec deliberately left open.

## R1. Priority representation

- **Decision**: Java enum `TaskPriority { LOW, MEDIUM, HIGH }`, persisted as a string
  (`@Enumerated(EnumType.STRING)`), serialized as `"LOW" | "MEDIUM" | "HIGH"`.
- **Rationale**: Strings are readable in the database and stable if levels are reordered. The
  enum's declaration order gives the High > Medium > Low ranking directly.
- **Alternatives**: Integer column (compact, but opaque in the data and in JSON); free text
  (cannot enforce FR-006).

## R2. Existing rows and the default

- **Decision**: Add the column with a database default of `MEDIUM` and treat a null read as
  `MEDIUM` in the entity getter. New tasks default to `MEDIUM` in the entity.
- **Rationale**: A `NOT NULL` column without a default fails to add on tables that already have
  rows. The default covers FR-003 and FR-004 whether the schema is created fresh or altered.
- **Alternatives**: Nullable column with application-only defaulting (leaves nulls in data); a
  migration tool (new dependency, needs confirmation per `CLAUDE.md`).
- **Confirmed (2026-10-14, by the user)**: the dev database uses `create`/`create-drop`
  schema generation, so the `priority` column is created automatically and no manual
  `ALTER TABLE` is needed. (`application.properties` is not readable by the assistant.)

## R3. Update semantics for omitted priority

- **Decision**: Request records carry `TaskPriority priority` (nullable). `null`/omitted on
  create means `MEDIUM`; on update it leaves the stored value unchanged (FR-005).
- **Rationale**: Keeps pre-feature clients working with no changes.
- **Limitation**: A JSON record cannot tell omitted from explicit `null`; both are treated as
  omitted (matches FR-006 as clarified). An empty string or unknown value fails enum binding
  and is rejected, so the stored priority can never become empty.

## R4. Error handling for bad values

- **Decision**: Add `HttpMessageNotReadableException` and
  `MethodArgumentTypeMismatchException` handlers returning HTTP 400 with the existing
  `{"error": "..."}` shape.
- **Rationale**: Today the catch-all `Exception` handler would return 500 for an invalid enum
  in a body or query parameter, violating the spec edge cases. 400 matches how
  `IllegalArgumentException` is already reported.
- **Alternatives**: Manual string parsing in the controller (duplicates what binding provides).

## R5. Filter and sort mechanics

- **Decision**: `GET /api/tasks` accepts optional `priority` (repeatable), `completed`
  (`true|false`), and `sort` (`priority,asc|priority,desc`). The service loads
  `findByOwnerId(ownerId)` then filters with AND across parameters, OR within `priority`, and
  sorts with a stable sort on the enum rank.
- **Rationale**: Ownership stays enforced by the single query (FR-011); `List.sort` is stable so
  equal priorities keep today's order (clarification Q1); no parameters means the list is
  untouched (FR-010a, FR-014).
- **Direction semantics**: `priority,desc` = High first; `priority,asc` = Low first.
- **Alternatives**: Spring Data `Sort` on the string column (sorts alphabetically: HIGH, LOW,
  MEDIUM, which is wrong); `Specification` queries (extra complexity for small data).

## R6. Closing the ownership gap on complete and delete

- **Decision**: Apply the same owner check used by `getForOwner`/`update` to `markCompleted`
  and `delete`, reporting other users' tasks as not found.
- **Rationale**: Constitution III is non-negotiable and the spec requires users "only ever see
  their own tasks"; the existing code carries a TODO acknowledging the gap. Behavior for
  legitimate owners is unchanged; only cross-user access changes (now 404).
- **Confirmed in scope**: Specified as FR-016 and implemented unconditionally.

## R8. Response shape

- **Decision**: Controllers return a `TaskResponse` record (`id`, `title`, `description`,
  `completed`, `ownerId`, `createdAt`, `priority`) built from the `Task` entity.
- **Rationale**: Satisfies Principle I (entities not exposed) while keeping the JSON field
  names and types identical to today's responses, plus the additive `priority` (FR-013).
- **Alternatives**: Keep returning the entity (violates Principle I; rejected).

## R9. Baseline list order

- **Decision**: Add `findByOwnerIdOrderByIdAsc` to `TaskRepository` and use it for the list.
- **Rationale**: Today's unspecified database order is, in practice, insertion order; making it
  explicit (id ascending) gives FR-010a and the stable-sort tie-break a testable definition.
- **Alternatives**: Leave the query unordered (behavior undefined; rejected).

## R7. Frontend approach

- **Decision**: Extend `TaskService.list()` with an optional filter object that builds
  `HttpParams`; keep filter/sort state in `TaskListComponent` and re-query the server on change.
  Add a priority `<select>` to the create row and to the detail view (detail calls a new
  `TaskService.update`).
- **Rationale**: Server-side filtering keeps one source of truth for ownership and ordering;
  services own HTTP per Principle II.
- **Alternatives**: Client-side filtering of the full list (simple, but duplicates ordering
  rules and returns more data than shown).
