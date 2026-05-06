# Current framework state

This file stores the latest known snapshot of the framework implementation.

Update it when the user asks to record, snapshot, or fix the current state.
Do not treat it as a roadmap replacement.

## Last updated

2026-05-06

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
  - `TEST_ONLY` logging suppresses calls wrapped as setup or cleanup.
- Auth and authenticated runtime:
  - `AuthClient` for login/register flows.
  - `AuthContext` for access token, refresh token, token type, and user id.
  - `AdminAuthBootstrap` for explicit admin authentication.
- API clients and DTOs:
  - `BaseApiClient`.
  - `UserClient` for `/me` profile, paginated user list, promote, edit, delete, and raw negative-test paths.
  - user list methods are named `getAll(...)`, `getAllRaw(...)`, and `getAllWithoutAuthRaw(...)`.
  - `SupplierClient` for supplier list, get-by-id, create, delete, raw negative-test paths, unauthenticated paths, and quiet delete.
  - auth/user DTOs split into `model.auth.common`, `model.auth.request`, and `model.auth.response`.
  - supplier request/response DTOs in `model.product.request` and `model.product.response`.
  - generic `MessageResponse` in `model.common` for simple `{ "message": ... }` success responses.
- Data generation and fixture lifecycle:
  - `AuthTestData` for auth-domain test data.
  - `ProductTestData` for supplier test data.
  - `AuthUserFixture` for creating users through API.
  - `AuthUserFixture.registerUsers(...)` for small batch setup through repeated API registration.
  - `SupplierFixture` for creating suppliers with default admin actor or explicit actor.
  - `SupplierFixture.createWithAllFields(...)` for full supplier setup data.
  - fixture-created users are registered in `UserCleanup`.
  - fixture-created suppliers are registered in `SupplierCleanup`.
  - setup API calls inside auth and supplier fixtures are wrapped in `ApiLogContext.asSetup(...)`.
- Extension-first test foundation:
  - `@ApiTest` entry point.
  - `@UiTest` entry point.
  - `@Admin` parameter marker.
  - `@TestUser` for method-level declarative user creation with a `role()` attribute.
  - `@CurrentUser` for resolving the user created by `@TestUser`.
  - `ApiTestRuntime` as the shared API runtime for extensions.
  - `ApiFixtureExtension` for `@Admin`, `AuthUserFixture`, and `SupplierFixture` parameter resolution.
  - `ApiFixtureExtension` runs framework-owned cleanup after each test.
  - `UserExtension` creates `@TestUser` users and resolves `@CurrentUser`.
  - `ApiClients` as an explicit lightweight client aggregator used by tests and runtime setup.
  - `UiExtension` for Selenide base URL setup and browser cleanup.
- Framework-owned targeted cleanup:
  - `UserCleanup` stores user ids created by fixture APIs.
  - `SupplierCleanup` stores supplier ids created by `SupplierFixture`.
  - `ApiTestRuntime.cleanupUsers()` deletes registered users through `UserClient.deleteQuietly(...)`.
  - `ApiTestRuntime.cleanupSuppliers()` deletes registered suppliers through `SupplierClient.deleteQuietly(...)`.
  - cleanup blocks are wrapped in `ApiLogContext.asCleanup(...)`.
  - cleanup registries are cleared in `finally` blocks.
  - cleanup is targeted by recorded ids, not by global table cleanup or prefix deletion.
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
  - API user flows use `@TestUser` / `@CurrentUser` for test-created current users.
  - pagination coverage uses non-default `page` and `page_size` query parameters and intentionally avoids exact global totals.
  - current-user assertions in promotion/edit/delete flows use `/me` instead of searching the paginated users list.
  - product-service supplier tests exercise create, list, get-by-id, delete, role access, duplicate-name, unauthenticated access, and validation-error paths.
  - supplier get-by-id coverage verifies the created supplier with all fields.
  - supplier delete coverage verifies the success message and that the deleted supplier is absent from the list.
  - UI auth tests for login page, login/logout flow, registration page visibility, and API-authenticated UI entry.

## Partially implemented

- Configuration is intentionally minimal:
  - only one `LocalConfig` implementation exists.
  - environment-aware selection is deferred until a second real environment or CI/runtime divergence appears.
  - product service URL is configured directly in `LocalConfig`, not through environment-aware selection.
- API logging is useful but still small:
  - `OFF`, `ALL`, and `TEST_ONLY` modes exist.
  - mode selection currently comes from the `api.log` system property, with `TEST_ONLY` as the local default.
  - `TEST_ONLY` logs calls in the default test scope and suppresses setup/cleanup scopes.
  - `ALL` still logs setup and cleanup calls by design.
  - there is no richer per-domain logging policy or report integration yet.
- Query parameter support is intentionally minimal:
  - only the currently needed `ApiRequest.withQueryParams(...)` factory exists.
  - no generic pagination abstraction such as `PaginationQuery` exists yet.
  - pagination behavior is expressed directly in `UserClient.getAll(authContext, page, size)`.
- API error assertions are useful for current flows but still narrow:
  - validation assertions focus on the first validation error.
  - `loc` handling currently assumes simple field-level validation.
- UI layer is intentionally thin:
  - page objects cover only proven auth/product navigation flows.
  - no component abstraction, screenshot comparison, or advanced UI infrastructure exists.
  - stronger page identity checks beyond the common page title locator are intentionally deferred.
  - UI tests have not yet been refactored to the `@TestUser` / `@CurrentUser` user fixture style.
  - `@UiTest` wires `UiExtension` and `ApiFixtureExtension`, but not `UserExtension`.
- Test foundation is extension-first but still small:
  - API runtime responsibilities are split between `ApiTestRuntime`, `ApiFixtureExtension`, and `UserExtension`.
  - tests still create `ApiClients` explicitly instead of receiving domain clients through parameter resolution.
  - cleanup exists for users and suppliers only.
  - there is no generic cleanup registry abstraction yet.
  - there is no domain-level fixture aggregator such as `ProductFixtures` yet.
- Product-service support is still supplier-focused:
  - supplier create/list/get/delete flows exist.
  - product, warehouse, product-in-warehouse, supplier document, and multipart flows are not implemented yet.
  - supplier cleanup exists, but cleanup for future product-service entities has not been designed yet.
- CI/CD baseline currently runs API tests only:
  - UI tests are not included in the first CI e2e run.
  - full UI CI remains a later step because it can introduce separate browser/runtime concerns.

## Not implemented yet

- DB support as a secondary setup path.
- Parallel execution support and isolation review.
- Rich reporting or hosted report integration.
- Multiple runtime config implementations and config factory/selection.
- Product-service domain coverage beyond suppliers.
- Product-service fixture aggregator for future supplier/product/warehouse setup.
- Generic cleanup registry abstraction across domains.
- Generic pagination model or shared pagination assertions.
- Multipart/form-data transport support for upload/document endpoints.
- Larger assertion catalog.
- UI component layer or visual regression infrastructure.
- UI tests in CI.

## Current roadmap position

- Stage: Stage 8 is implemented as a minimal API e2e CI baseline; post-baseline API expansion is active inside auth/user and product-service areas.
- Reason:
  - stages 2-6 are represented in code by the API technical base, auth path, first client path, data generation, and extension-first test foundation.
  - Stage 7 is represented by a minimal Selenide page layer and explicit `UiAuthBridge`.
  - Stage 8 is represented by a GitHub Actions workflow that checks out the app repository, starts the app through Docker Compose, waits for auth service readiness, runs API-tagged tests, and preserves diagnostics.
  - the pagination and `/me` work is a narrow Stage 10-style API expansion inside auth/user.
  - framework-owned targeted cleanup has moved from deferred idea to implemented baseline for users and suppliers.
  - product-service work has advanced through supplier get-by-id, delete, `MessageResponse`, supplier fixture setup, and supplier cleanup.
  - UI test refactoring to the declarative user fixture model is still deferred while API/product-service work is active.
  - DB support, parallel execution, reporting, and broader product-service flows remain deferred.

## Architectural observations

- The framework remains aligned with the API-first direction: API clients, auth context, fixtures, and extension lifecycle are the center of the design.
- The project remains aligned with the extension-first direction: tests receive shared fixture/runtime state through JUnit parameter resolution rather than base classes.
- Framework-owned cleanup is now active, but it is targeted and id-based rather than global.
- Cleanup only knows about entities created through fixtures; direct raw client setup remains outside the cleanup registry.
- Wrapping fixture setup and after-test cleanup in `ApiLogContext` keeps `TEST_ONLY` logs focused on the behavior under test.
- `ApiRequester` remains technical and stateless, which keeps domain behavior inside clients.
- `ApiLogContext` remains a narrow runtime helper for API logging scope, not a domain fixture or auth state holder.
- Query parameter support remains transport-level; pagination behavior is expressed in `UserClient`, not in the requester.
- `ApiClients` keeps client construction explicit and small while more API areas are still being proven.
- `AuthServiceRequestSpecs` still contains only request defaults and authentication header composition; auth state is not hidden in specs.
- `ProductServiceRequestSpec` repeats the auth-service spec shape for a second base URL; this duplication is acceptable while only two services exist.
- `/me` remains the preferred way to assert current-user state, which avoids relying on the first page of the paginated users list.
- `AuthUserFixture.registerUsers(...)` keeps batch setup API-based by repeating the proven single-user registration flow.
- `SupplierFixture` now proves the same growth pattern for a second API domain: create through API, register id, cleanup through extension.
- `MessageResponse` is a small common model for simple success messages; broader response unification is not needed yet.
- Product-service models use a separate `model.product` namespace; this matches the second API domain without introducing a global model framework.
- `UiAuthBridge` keeps authenticated UI bootstrap outside page objects, which preserves the page layer as a thin adapter.
- UI tests are still behind the API fixture model: UI auth tests can receive `AuthUserFixture`, but they do not use `@TestUser` / `@CurrentUser`.
- CI proves the API-centered path against a real Docker Compose application runtime.
- The current design should still avoid DB support, parallel execution, rich reporting, generic pagination abstractions, product fixture aggregators, or larger UI abstractions until repeated flows create real pressure.

## Code review notes

- `ApiErrorExtractor` currently casts validation `loc` to `List<String>`. This is enough for current simple field errors but may be too narrow for nested or indexed validation errors.
- Page object identity checks still rely mainly on the common page title locator, currently `h1`. Strengthening them is recorded in `architecture/deferred-tasks.md`.
- Pagination tests avoid exact `total` and exact page contents because the users list is global and may contain data from other tests or seed users.
- User and supplier cleanup is now framework-owned for fixture-created entities, but direct client-created setup data is not tracked.
- Cleanup methods use quiet delete semantics so tests that delete the entity explicitly should not fail during after-each cleanup.
- Supplier full-field contract is currently checked through get-by-id, not by overloading every role/access test with field assertions.
- Supplier list/delete tests still use global list checks, so assertions are anchored to `supplierId` rather than exact list size.
- UI auth bridge test refactoring is deferred; current UI test setup should not be treated as fully aligned with the declarative user fixture model.
- `ProductServiceRequestSpec` uses singular naming while the existing auth spec uses plural `AuthServiceRequestSpecs`; naming can be aligned during cleanup.
- The latest GitHub Actions API test run previously reported green before the cleanup/supplier-delete changes. The current snapshot has not been validated by a full test suite run in this session.

## Next steps

1. Run targeted supplier cleanup/delete tests and then the API suite before treating the current cleanup changes as fully validated.
2. Keep supplier cleanup id-based and fixture-owned; avoid global table cleanup.
3. Add product-service fixture aggregation only when another product-service fixture appears and test parameters start to grow.
4. Continue product-service expansion with the next dependent flow, likely warehouse or product creation, after supplier cleanup is stable.
5. Keep per-domain cleanup small before introducing a generic cleanup registry abstraction.
6. Refactor UI tests to the current declarative user fixture style before enabling UI tests in CI.
7. Decide API logging default policy for local and CI runs after observing cleanup/setup log behavior.
8. Keep pagination abstraction deferred until another domain client or repeated pagination assertions appear.
9. Keep API error assertion improvements deferred until a second real validation response shape appears.
10. Keep DB support, parallel execution, and richer reporting deferred until the API CI baseline and cleanup behavior are stable across repeated runs.

## Deferred during review

- `Strengthen page object identity checks` remains recorded in `architecture/deferred-tasks.md`.
- UI test refactoring to `@TestUser` / `@CurrentUser` remains recorded in `architecture/deferred-tasks.md`.
- User cleanup is no longer just deferred; the current deferred backlog should track only future cleanup isolation concerns.
