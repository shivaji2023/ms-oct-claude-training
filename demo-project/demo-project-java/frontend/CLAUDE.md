# Frontend CLAUDE.md

Frontend-only notes. Basic commands, the auth flow, routing and the API/CORS setup are in `../CLAUDE.md`.

## Stack

Angular 17.3 using standalone components only (no NgModules), the `application` (esbuild) builder, RxJS 7.8, Karma + Jasmine. TypeScript runs in `strict` mode and Angular runs with `strictTemplates`, so template type errors fail the build. `.editorconfig` sets 2-space indent and single quotes in `.ts` files.

## Extra commands

```bash
npx ng test --watch=false --browsers=ChromeHeadless   # single CI-style run
npx ng build --configuration development             # skip prod budgets/optimization
npx ng generate component tasks/task-detail          # scaffolds a standalone component
```

## Code conventions

- **One folder per feature:** `auth/` and `tasks/` each hold a service and its component(s). `core/` holds cross-cutting HTTP plumbing.
- **Feature components** are `standalone: true`, use **inline** `template` and `styles`, and import `CommonModule`/`FormsModule` directly. They use template-driven forms (`[(ngModel)]`), not reactive forms. (`AppComponent` is the exception, with separate `.html`/`.css` files.)
- **Templates** use the older structural directives (`*ngIf`, `*ngFor`), not the Angular 17 `@if`/`@for` control flow. Stay consistent within a file.
- **Dependency injection:** components and services use constructor injection. The functional interceptor uses `inject()`.
- **Services** are `providedIn: 'root'` and return `Observable`s.
- The `Task` interface in `tasks/task.service.ts` mirrors the backend `Task` entity's JSON exactly, with `createdAt` as an ISO string. Keep the two in sync.

## Non-obvious behavior

- **Auth state is just "a token exists in `localStorage`".** `isLoggedIn()` doesn't check expiry, and neither it nor `logout()` is called anywhere. The UI has no logout button, and nothing clears a rejected token.
- **API errors are handled inconsistently.** `LoginComponent` shows one generic message for every error. `TaskListComponent` has no error handlers at all, so failed calls fail silently (for example, opening `/tasks` without logging in). The backend returns an empty-body **403** (not 401) for a missing or invalid token, and `{"error": "..."}` bodies for 400/404/500.
- **The production build enforces a 2 kB warning / 4 kB error budget per component stylesheet.** Inline `styles` count toward it, so large inline style blocks will fail `ng build`.
- `AppComponent.title` is unused, because the root template is only `<router-outlet>`. The page title comes from `src/index.html` (currently just "Frontend").
