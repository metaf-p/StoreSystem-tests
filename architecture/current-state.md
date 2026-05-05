# Current framework state

This file stores the latest known snapshot of the framework implementation.

Update it when the user asks to record, snapshot, or fix the current state.
Do not treat it as a roadmap replacement.

## Last updated

2026-05-05

## Implemented

- API technical base:
  - `Config` / `LocalConfig` as the current single configuration boundary.
  - separate auth and product service base URLs in config.
  - reusable request specs in `AuthServiceRequestSpecs`.
  - product-service request spec in `ProductServiceRequestSpec`.
  - reusable response specs in `ResponseSpec`.
  - typed endpoint definitions through `Endpoint`, `AuthEndpoints`, and `ProductServiceEndpoints`.
  - technical transport through `ApiRequest` and `ApiRequester`.
  - query parameter support in `ApiRequest` and `ApiRequester`.
  - config-driven API logging through `ApiLogMode` and `ApiLoggingFilter`, attached from request specs.
  - API log scoping through `ApiCallScope` and `ApiLogContext`.
- Auth and authenticated runtime:
  - `AuthClient` for login/register flows.
  - `AuthContext` for access token, refresh token, token type, and user id.
  - `AdminAuthBootstrap` for explicit admin authentication.
- First API client path:
  - `BaseApiClient`.
  - `UserClient` for `/me` profile, paginated user list, promote, edit, delete, and raw negative-test paths.
  - auth/user DTOs split into `model.auth.common`, `model.auth.request`, and `model.auth.response`.
- Product-service API path started:
  - `SupplierClient` for supplier list and supplier create operations.
  - supplier request/response DTOs in `model.product.request` and `model.product.response`.
- Minimal data generation and user lifecycle:
  - `AuthTestData` for auth-domain test data.
  - `AuthUserFixture` for creating users through API.
  - `AuthUserFixture.registerUsers(...)` for small batch setup through repeated API registration.
  - `@TestUser` for method-level declarative user creation with a `role()` attribute.
  - `@CurrentUser` for resolving the user created by `@TestUser`.
  - test-created users are intentionally not deleted after every test by the framework.
  - user database cleanup is handled periodically by an application-level cleanup script outside this test framework.
- Extension-first test foundation:
  - `@ApiTest` entry point.
  - `@UiTest` entry point.
  - `@Admin` parameter marker.
  - `ApiTestRuntime` as the shared API runtime for extensions.
  - `ApiFixtureExtension` for `@Admin` and `AuthUserFixture` parameter resolution.
  - `UserExtension` for `@TestUser` setup and `@CurrentUser` parameter resolution.
  - `ApiClients` as an explicit lightweight client aggregator used by tests and runtime setup.
  - `UiExtension` for Selenide base URL setup and browser cleanup.
- Minimal UI adapter:
  - `BasePage`, `LoginPage`, `RegistrationPage`, `ProductPage`.
  - shared page opening through `BasePage.open()`.
  - shared page title check through the common `h1` locator in `BasePage`.
  - scenario-level login methods through `LoginPage.loginAs(...)` and `LoginPage.tryLoginAs(...)`.
  - `ProductPage` currently opens `/products` and uses the `[data-testid='logout']` logout locator.
  - `UiAuthBridge` for API-to-UI authenticated entry through the refresh token cookie.
- API assertion support:
  - `ApiErrorAssert`.
  - `ApiErrorExtractor`.
  - simple and validation error models in `model.error`.
- CI/CD baseline:
  - GitHub Actions workflow in `.github/workflows/tests.yml`.
  - Maven Wrapper is used as the CI entry point.
  - Java 21 setup with Maven cache.
  - application repository checkout into `app/`.
  - application startup through Docker Compose.
  - auth service readiness wait before tests.
  - API-only CI execution through JUnit tag `api`.
  - Surefire reports artifact.
  - Docker Compose logs artifact.
  - Docker Compose cleanup and docker-data permission cleanup.
- Test coverage currently present:
  - API auth tests for login, registration, `/me`, users list pagination, promotion, edit, and delete flows.
  - API user tests are being migrated to declarative user setup through `@TestUser` and `@CurrentUser`; current API user flows no longer rely on plain unresolved `AuthContext` parameters.
  - pagination coverage uses non-default `page` and `page_size` query parameters.
  - current-user assertions in promotion/edit/delete flows use `/me` instead of searching the paginated users list.
  - product-service supplier tests now exercise create, list, role-access, duplicate-name, and validation-error paths.
  - supplier positive checks are still shallow: they mostly verify `supplierId` or non-empty lists, not the full created supplier contract.
  - UI auth tests for login page, login/logout flow, registration page visibility, and API-authenticated UI entry.

## Partially implemented

- Configuration is intentionally minimal:
  - only one `LocalConfig` implementation exists.
  - environment-aware selection is deferred until a second real environment or CI/runtime divergence appears.
  - product service URL is configured directly in `LocalConfig`, not through environment-aware selection.
- API logging is intentionally small:
  - `OFF`, `ALL`, and `TEST_ONLY` modes exist.
  - mode selection currently comes from the `api.log` system property, with `TEST_ONLY` as the local default.
  - `TEST_ONLY` logs calls in the default test scope and suppresses calls wrapped as setup or cleanup.
  - `AuthUserFixture` and admin bootstrap use `ApiLogContext.asSetup(...)` so user preparation noise can be hidden from default logs.
  - `CLEANUP` scope exists in `ApiLogContext`, but there is no active framework-owned cleanup flow yet.
- Query parameter support is intentionally minimal:
  - only the currently needed `ApiRequest.withQueryParams(...)` factory exists.
  - no generic pagination abstraction such as `PaginationQuery` exists yet.
  - pagination is currently represented directly in `UserClient.listUsers(authContext, page, size)`.
- API error assertions are useful for current flows but still narrow:
  - validation assertions focus on the first validation error.
  - `loc` handling currently assumes simple field-level validation.
- UI layer is intentionally thin:
  - page objects cover only proven auth/product navigation flows.
  - no component abstraction, screenshot comparison, or advanced UI infrastructure exists.
  - stronger page identity checks beyond the common page title locator are intentionally deferred.
  - UI tests have not yet been refactored to the new `@TestUser` / `@CurrentUser` user fixture style.
  - `@UiTest` currently wires `UiExtension` and `ApiFixtureExtension`, but not `UserExtension`.
- Test foundation is extension-first but still small:
  - API runtime responsibilities are split between `ApiTestRuntime`, `ApiFixtureExtension`, and `UserExtension`.
  - tests still create `ApiClients` explicitly instead of receiving domain clients through parameter resolution.
  - user cleanup is not a per-test framework responsibility at the current stage.
  - `UserCleanup` exists in code but is not part of the active extension flow.
- Product-service support is intentionally incomplete:
  - `SupplierClient` currently covers only list and create.
  - supplier tests exist, but the current task is still to strengthen the real supplier flow and prove the created resource contract.
  - supplier get-by-id, delete, cleanup, and product-service fixture support are not implemented yet.
  - product, warehouse, product-in-warehouse, supplier document, and multipart flows are not implemented yet.
- CI/CD baseline currently runs API tests only:
  - UI tests are not included in the first CI e2e run.
  - full UI CI remains a later step because it can introduce separate browser/runtime concerns.

## Not implemented yet

- DB support as a secondary setup path.
- Parallel execution support and isolation review.
- Rich reporting or hosted report integration.
- Multiple runtime config implementations and config factory/selection.
- Completed product-service domain coverage beyond the current supplier create/list baseline.
- Supplier cleanup and product-service fixture support.
- Generic pagination model or shared pagination assertions.
- Multipart/form-data transport support for upload/document endpoints.
- Larger assertion catalog.
- UI component layer or visual regression infrastructure.
- UI tests in CI.

## Current roadmap position

- Stage: Stage 8 is implemented as a minimal API e2e CI baseline; a small post-baseline API expansion has started inside auth/user and product-service areas.
- Reason:
  - stages 2-6 are represented in code by the API technical base, auth path, first client path, minimal data generation, and extension-first test foundation.
  - Stage 7 is represented by a minimal Selenide page layer and explicit `UiAuthBridge`.
  - Stage 8 is represented by a GitHub Actions workflow that checks out the app repository, starts the app through Docker Compose, waits for auth service readiness, runs API-tagged tests, and preserves diagnostics.
  - the pagination and `/me` work is a narrow Stage 10-style API expansion inside auth/user.
  - product-service work has started with supplier endpoints, config, request spec, DTOs, test data, and create/list tests, but the created supplier contract is not fully proven yet.
  - UI test refactoring to the declarative user fixture model is intentionally deferred while API/product-service work is active.
  - DB support, parallel execution, reporting, and broader product-service flows remain deferred.

## Architectural observations

- The framework is still aligned with the API-first direction: API clients, auth context, and fixtures are the center of the design.
- The project is also aligned with the extension-first direction: tests receive shared fixture/runtime state through JUnit parameter resolution rather than base classes.
- Per-test user deletion is intentionally excluded from the framework for now; periodic database cleanup belongs to the application side and is not modeled as a framework feature.
- Current abstractions appear mostly extracted from proven flows rather than invented upfront.
- `ApiRequester` remains technical and stateless, which keeps domain behavior inside clients.
- `ApiLogContext` is a narrow runtime helper for API logging scope, not a domain fixture or auth state holder.
- Query parameter support remains transport-level; pagination behavior is expressed in `UserClient`, not in the requester.
- `ApiClients` keeps client construction explicit and small while more API areas are still being proven.
- `AuthServiceRequestSpecs` still contains only request defaults and authentication header composition; auth state is not hidden in specs.
- `ProductServiceRequestSpec` repeats the auth-service spec shape for a second base URL; this duplication is acceptable while only two services exist.
- `/me` is now the preferred way to assert current-user state, which avoids relying on the first page of the paginated users list.
- `AuthUserFixture.registerUsers(...)` keeps batch setup API-based by repeating the proven single-user registration flow.
- The model package split makes request, response, and shared auth/user models more explicit without adding a framework-wide model abstraction.
- Product-service models currently use a separate `model.product` namespace; this matches the second API domain without introducing a global model framework.
- `UiAuthBridge` keeps authenticated UI bootstrap outside page objects, which preserves the page layer as a thin adapter.
- UI tests are currently behind the API test foundation refactor: API user flows use `@TestUser` / `@CurrentUser`, but UI auth bridge coverage still needs a separate refactor step.
- CI now proves the API-centered path against a real Docker Compose application runtime.
- The current design should still avoid adding DB support, parallel execution, rich reporting, generic pagination abstractions, or larger UI abstractions before repeated flows create real pressure.

## Code review notes

- `ApiErrorExtractor` currently casts validation `loc` to `List<String>`. This is enough for current simple field errors but may be too narrow for nested or indexed validation errors.
- `LoginPage.submit(T expectedPage)` was removed from the page object API. Login flow now uses explicit scenario methods.
- Page object identity checks still rely mainly on the common page title locator, currently `h1`. Strengthening them is recorded in `architecture/deferred-tasks.md`.
- Pagination tests avoid exact `total` and exact page contents because the users list is global and may contain data from other tests or seed users.
- Test-created users are not deleted after each test by design. Database hygiene is expected to come from a periodic cleanup script in the application repository, not from this framework.
- Supplier positive tests currently prove that create/list endpoints respond successfully, but they still need stronger field-level contract checks for the created supplier.
- UI auth bridge test refactoring is deferred; current UI test setup should not be treated as fully aligned with the new declarative user fixture model.
- `ProductServiceRequestSpec` uses singular naming while the existing auth spec uses plural `AuthServiceRequestSpecs`; naming can be aligned during cleanup.
- The latest GitHub Actions API test run was reported green by the user after adding Docker Compose startup, readiness waiting, API tag filtering, diagnostics artifacts, and cleanup.

## Next steps

1. Keep the API CI baseline stable and inspect artifacts from failed runs when needed.
2. Strengthen the current supplier flow: create supplier, fetch or otherwise verify the exact created supplier, assert fields, and clean it up.
3. Add supplier delete/get-by-id support before product creation tests start depending on suppliers.
4. Keep per-test user cleanup out of the framework unless the external cleanup script stops being sufficient for test reliability.
5. Refactor UI tests to the current declarative user fixture style before enabling UI tests in CI.
6. Decide API logging default policy for local and CI runs.
7. Keep pagination abstraction deferred until another domain client or repeated pagination assertions appear.
8. Keep API error assertion improvements deferred until a second real validation response shape appears.
9. Keep DB support, parallel execution, and richer reporting deferred until the API CI baseline is stable across repeated runs.

## Deferred during review

- `Strengthen page object identity checks` is recorded in `architecture/deferred-tasks.md`.
- UI test refactoring to `@TestUser` / `@CurrentUser` is recorded in `architecture/deferred-tasks.md`.
