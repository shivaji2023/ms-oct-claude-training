# Backend CLAUDE.md

Backend-only notes. Commands, the auth flow, the package layout and the deliberate training bugs are in `../CLAUDE.md`.

## Stack

Spring Boot 3.2.5 (Jakarta EE namespaces), Spring Security 6, Spring Data JPA, jjwt 0.12.5, JUnit 5 + Mockito + AssertJ via `spring-boot-starter-test`, plus `spring-security-test`. No Lombok.

## Code conventions

- Constructor injection with `private final` fields; no `@Autowired` fields.
- Controllers return entities or records directly, not `ResponseEntity`.
- Entities have a public no-arg constructor, a convenience constructor and explicit getters/setters.
- jjwt 0.12 uses the newer builder API (`.subject()`, `.expiration()`, `Jwts.parser().verifyWith(key).build().parseSignedClaims(...)`), not the deprecated `setSubject`/`parseClaimsJws` methods.

## Non-obvious behavior

- **Bean Validation annotations are inert.** The DTO records carry `@Email`/`@NotBlank`, but no controller parameter has `@Valid`, so nothing is validated. If you add `@Valid`, also add a handler for `MethodArgumentNotValidException`; otherwise the catch-all turns it into a 500.
- **Exceptions thrown in `JwtAuthFilter` bypass `GlobalExceptionHandler`**, because the filter runs before the DispatcherServlet. `extractUserId()` throws on a bad signature, a malformed token or an expired token, since jjwt checks `exp` while parsing. Those requests end up as an empty-body 403, not a JSON error.
- **Unauthenticated requests get 403, not 401.** No `AuthenticationEntryPoint` is configured.
- **`deleteById` on a missing ID is a silent no-op** in Spring Data JPA 3.x, so `DELETE /api/tasks/{id}` returns 200 for any ID.
- `JwtUtil` passes the secret to `Keys.hmacShaKeyFor`, which needs at least 32 bytes. A shorter secret (for example in a test) throws `WeakKeyException`.

## Testing

- Tests are plain JUnit classes in the same package as the code they test.
- To unit test a service, mock its repository with Mockito. For `AuthService`, use a real `BCryptPasswordEncoder` and construct `JwtUtil` directly with a secret of at least 32 bytes.
- In controller/MockMvc tests, don't use `@WithMockUser`: its principal isn't a `Long`, so `@AuthenticationPrincipal Long ownerId` resolves to `null`. Use `.with(authentication(new UsernamePasswordAuthenticationToken(1L, null, List.of())))` from `SecurityMockMvcRequestPostProcessors`, or send a real token from `JwtUtil` in the `Authorization` header.
