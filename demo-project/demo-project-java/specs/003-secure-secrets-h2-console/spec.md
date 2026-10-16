# Feature Specification: Secure Signing Secret and Database Console

**Feature Branch**: `fix/issue-1`

**Created**: 2026-10-16

**Status**: Draft

**Input**: User description: "Check Github Issue #1" — synechron-data/ms-oct-claude-training#1, "bug: hardcoded JWT secret in application.properties and unauthenticated H2 console exposed" (OWASP A02:2021, A05:2021; severity Critical)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Sign-in tokens cannot be forged from the repository (Priority: P1)

An operator deploys the Task Manager API. The key used to sign sign-in tokens is supplied by the operator at deployment time and is never stored in the source repository. If the operator forgets to supply it, or supplies one that is too weak, the service refuses to start instead of running with an insecure key.

**Why this priority**: Today anyone who can read the repository can mint a valid token for any user and read or change that user's tasks. This is a complete authentication bypass and is the highest-impact defect.

**Independent Test**: Start the service with no signing key, then with a 31-byte key, then with a 32-byte key supplied from the deployment environment. Only the last start succeeds, and no committed file contains a usable key.

**Acceptance Scenarios**:

1. **Given** no signing key is configured, **When** the service starts, **Then** startup fails with a message naming the missing setting.
2. **Given** a signing key shorter than 32 bytes, **When** the service starts, **Then** startup fails with a message stating the minimum length.
3. **Given** a signing key of at least 32 bytes supplied through the deployment environment, **When** the service starts, **Then** it starts and issues tokens that verify successfully.
4. **Given** the repository at its current head, **When** all committed configuration files are searched for a literal signing key value, **Then** none is found.
5. **Given** a token signed with the previously committed key, **When** it is presented to a service running with the new key, **Then** it is rejected.

---

### User Story 2 - The database console is unreachable outside local development (Priority: P1)

An operator running the service in any shared or production environment must not expose the in-browser database console. A developer working locally can still opt in to the console by choosing the development profile.

**Why this priority**: The console currently allows anonymous access to every table, including password hashes, with an empty database password. This is a second independent route to full data compromise.

**Independent Test**: Start the service with the default profile and request the console address without credentials; it must not be served. Start it with the development profile and confirm the console is available to the developer.

**Acceptance Scenarios**:

1. **Given** the default profile, **When** an unauthenticated client requests the console address or any path beneath it, **Then** the request is denied or not found and no console page is returned.
2. **Given** the default profile, **When** an authenticated API user requests the console address, **Then** the console is still not served.
3. **Given** the development profile, **When** a developer opens the console, **Then** it is available.
4. **Given** any profile, **When** a response is returned, **Then** the standard clickjacking protection header is present.

---

### User Story 3 - Operators know how to run and rotate safely (Priority: P2)

A new developer or operator reads the setup documentation and learns that the signing key must be provided, how long it must be, and how to run locally with the console. The team has a recorded step to rotate the key that was exposed in repository history.

**Why this priority**: The code protections only work if people can deploy the service correctly and the exposed key is retired. It does not block the P1 protections but is required to close the issue.

**Independent Test**: A person with no prior knowledge follows only the documentation and starts the service locally on the first attempt; the rotation step is recorded as completed for every environment that used the old key.

**Acceptance Scenarios**:

1. **Given** the setup documentation, **When** a developer follows it from a clean checkout, **Then** the service starts with the console available locally.
2. **Given** each environment that ever ran with the old key, **When** rotation is complete, **Then** a record shows the new key is in use there.

---

### Edge Cases

- A signing key made of multi-byte characters is measured in bytes, not characters: 16 two-byte characters (32 bytes) is accepted, 15 two-byte characters plus one single-byte character (31 bytes) is rejected.
- A key consisting only of whitespace, or an empty value, is treated as missing.
- Automated tests run without operator-supplied environment values; they need their own test-only key that is clearly not a production secret.
- Existing signed-in users lose their sessions when the key is rotated; this is expected and must be communicated.
- Paths beneath the console address (not only its root) are denied on the default profile.
- A page served by another site cannot embed the application in a frame.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST obtain the token-signing key only from operator-supplied deployment configuration, with no literal or default key in any committed file.
- **FR-002**: The system MUST refuse to start when the signing key is missing, empty, whitespace-only, or shorter than 32 bytes.
- **FR-003**: The system MUST measure signing-key length in bytes using a fixed, platform-independent text encoding.
- **FR-004**: The system MUST reject tokens signed with the previously committed key after rotation.
- **FR-005**: The system MUST NOT serve the database console on the default profile, for anonymous or authenticated callers.
- **FR-006**: The system MUST serve the database console only when the development profile is explicitly selected.
- **FR-007**: The system MUST NOT allow unauthenticated access to the database console address or any path beneath it, on any profile.
- **FR-008**: The system MUST send the standard clickjacking-protection header on responses.
- **FR-009**: The project documentation MUST state that the signing key is required, its minimum length, how to provide it, and how to run with the development profile.
- **FR-010**: Automated tests MUST cover key rejection (missing, blank, 31 bytes), key acceptance (32 bytes), and denial of unauthenticated console access on the default profile.
- **FR-011**: All pre-existing automated tests MUST continue to pass without being removed or weakened.
- **FR-012**: Task endpoints MUST continue to require authentication and per-user ownership, unchanged by this work.

### Key Entities

- **Signing key**: The secret used to sign and verify sign-in tokens. Attributes: source (operator-supplied only), minimum length (32 bytes), rotation status per environment.
- **Runtime profile**: The named mode the service runs in (default or development). Determines whether the database console is available.
- **Rotation record**: Evidence that each environment which used the exposed key now uses a new one.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A search of all committed configuration files finds zero literal signing-key values.
- **SC-002**: 100% of start attempts with a missing or under-32-byte key fail before accepting any request.
- **SC-003**: 100% of anonymous requests to the database console address and its sub-paths on the default profile are denied or not found.
- **SC-004**: A tester can forge no accepted token using any key found in the repository or its history after rotation.
- **SC-005**: A new developer starts the service locally from the documentation alone in under 10 minutes.
- **SC-006**: The full automated test suite passes with zero failures and zero removed tests.

## Assumptions

- Target users are operators and developers of the Task Manager API; end users see no interface change except being signed out once on rotation.
- The signing key is provided through an environment variable named `JWT_SECRET` (the name used in the GitHub issue).
- Part of this work is already delivered by pull request #2: startup rejection of short keys with fixed text encoding, removal of the console from the anonymous-access list, restoration of the clickjacking header, and the associated tests. This specification covers the full issue; remaining work is configuration changes (FR-001, FR-005, FR-006), documentation (FR-009), test-environment key handling, and rotation (FR-004).
- Editing the application's secret-bearing configuration file requires explicit flagging in the pull request under the project constitution and is currently restricted by local permission settings; the person doing that edit has the necessary access.
- The development profile is the only context where the database console is wanted; no production use of the console is required.
- Rotation of the exposed key is an operational task performed by the team; removing the key from repository history is out of scope because the key is treated as compromised regardless.
- The empty database password applies only to the in-memory development database and is addressed by keeping the console off the default profile; replacing the database is out of scope.
