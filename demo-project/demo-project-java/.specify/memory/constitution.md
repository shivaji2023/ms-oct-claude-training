<!--
Sync Impact Report (temporary; remove before commit)
- Version change: template (unversioned) -> 1.0.0
- Modified principles: none renamed; all five template principle slots filled
  - I. Backend Conventions (new)
  - II. Frontend Conventions (new)
  - III. Task Ownership Enforcement (new)
  - IV. Test-Backed Changes (new)
  - V. API Backward Compatibility (new)
- Added sections: Technology & Safety Constraints, Development Workflow
- Removed sections: none
- Deferred TODOs: none
-->
# Task Manager Constitution

## Core Principles

### I. Backend Conventions
The backend is a Spring Boot 3 / Java 17 application. All Spring beans MUST use constructor
injection; field injection is prohibited. DTOs MUST be Java records wherever practical, and
entities MUST NOT be exposed directly through API responses.

Rationale: constructor injection makes dependencies explicit and testable; records give
immutable, boilerplate-free API contracts.

### II. Frontend Conventions
The frontend is Angular with standalone components. New features MUST NOT introduce NgModules.
Services own all HTTP communication; components MUST NOT call `HttpClient` directly.

Rationale: a single HTTP boundary keeps components testable and API usage consistent.

### III. Task Ownership Enforcement (NON-NEGOTIABLE)
Every endpoint that reads or mutates a task MUST resolve the authenticated user and enforce
that the task belongs to that user. Ownership MUST be checked server-side on every request;
client-side checks never satisfy this principle. Access to another user's task MUST be
denied without revealing whether the task exists.

Rationale: prevents broken object-level authorization, the primary risk of a multi-user
task API.

### IV. Test-Backed Changes
Every code change or addition MUST ship with tests: JUnit 5 for the backend (mirroring
`main/java` under `src/test/java`) and Jasmine/Karma for the frontend. `mvn test` and
`npm test` MUST pass before a change is merged. Existing tests MUST NOT be removed to make a
change pass.

Rationale: tests are the evidence that ownership and compatibility guarantees hold.

### V. API Backward Compatibility
Existing API responses MUST remain backward compatible: fields MUST NOT be removed, renamed,
or retyped, and status codes MUST NOT change meaning. Additive changes (new optional fields,
new endpoints) are permitted. A breaking change requires a new API version or an approved
amendment under Governance.

Rationale: existing clients must continue to work without coordinated releases.

## Technology & Safety Constraints
- Stack: Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA, H2 (dev); Angular 17,
  TypeScript; stateless JWT authentication.
- Changes to secrets in `application.properties` MUST be flagged explicitly in the PR.
- New dependencies MUST NOT be added without explicit confirmation.

## Development Workflow
- Commits MUST follow Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`, `refactor:`).
- Direct pushes to `main` are prohibited; changes land through pull requests.
- PR descriptions MUST include what changed, why, and how to test.
- Reviewers MUST verify compliance with every principle above.

## Governance
This constitution supersedes other practices. Amendments require a documented change, a
rationale, review approval, and a migration plan for affected code. Versioning follows
semantic versioning: MAJOR for removed or redefined principles, MINOR for added principles or
materially expanded guidance, PATCH for clarifications. All PRs and reviews MUST verify
compliance; deviations MUST be justified in the PR. Runtime guidance lives in `CLAUDE.md`.

**Version**: 1.0.0 | **Ratified**: 2026-10-14 | **Last Amended**: 2026-10-14
