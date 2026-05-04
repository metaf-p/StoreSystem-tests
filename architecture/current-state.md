# Current framework state

This file stores the latest known snapshot of the framework implementation.

Update it when the user asks to record, snapshot, or fix the current state.
Do not treat it as a roadmap replacement.

## Last updated

2026-04-28

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
  - config-driven API logging through `ApiLogMode` and `ApiLoggingFilter`, attached from `AuthServiceRequestSpecs`.
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
- Minimal data generation and cleanup:
  - `AuthTestData` for auth-domain test data.
  - `AuthUserFixture` for creating users through API.
  - `AuthUserFixture.registerUsers(...)` for small batch setup through repeated API registration.
  - `UserCleanup` plus extension-managed cleanup after each test.
- Extension-first test foundation:
  - `@ApiTest` entry point.
  - `@UiTest` entry point.
  - `@Admin` parameter marker.
  - `ApiClientExtension` for API client resolution, auth contexts, fixtures, admin bootstrap, and cleanup.
  - `ApiClientExtension` now resolves any `BaseApiClient` with a public `ApiRequester` constructor instead of listing every concrete client.
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
  - pagination coverage uses non-default `page` and `page_size` query parameters.
  - current-user assertions in promotion/edit/delete flows use `/me` instead of searching the paginated users list.
  - product-service supplier test class exists as a scaffold, but it does not yet exercise a real supplier flow.
  - UI auth tests for login page, login/logout flow, registration page visibility, and API-authenticated UI entry.

## Partially implemented

- Configuration is intentionally minimal:
  - only one `LocalConfig` implementation exists.
  - environment-aware selection is deferred until a second real environment or CI/runtime divergence appears.
  - product service URL is configured directly in `LocalConfig`, not through environment-aware selection.
- API logging is intentionally small:
  - only `OFF` and `ALL` modes exist.
  - mode selection currently comes from the `api.log` system property, with `ALL` as the local default.
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
- Test foundation is extension-first but still small:
  - one API extension currently owns automatic client creation, fixture access, admin auth, and cleanup.
  - this is acceptable for the current size, but may need splitting only when responsibilities grow from real pressure.
  - `ApiRequester` is still injectable as a test parameter even though it is now mostly a transport detail behind API clients.
- Product-service support is intentionally incomplete:
  - `SupplierClient` currently covers only list and create.
  - supplier get-by-id, delete, cleanup, test data helper, and real supplier tests are not implemented yet.
  - product, warehouse, product-in-warehouse, supplier document, and multipart flows are not implemented yet.
- CI/CD baseline currently runs API tests only:
  - UI tests are not included in the first CI e2e run.
  - full UI CI remains a later step because it can introduce separate browser/runtime concerns.

## Not implemented yet

- DB support as a secondary setup path.
- Parallel execution support and isolation review.
- Rich reporting or hosted report integration.
- Multiple runtime config implementations and config factory/selection.
- Completed product-service domain coverage beyond the initial supplier scaffold.
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
  - product-service work has started with supplier endpoints, config, request spec, DTOs, and client scaffold, but it is not yet a proven real flow.
  - DB support, parallel execution, reporting, and broader product-service flows remain deferred.

## Architectural observations

- The framework is still aligned with the API-first direction: API clients, auth context, fixtures, and cleanup are the center of the design.
- The project is also aligned with the extension-first direction: tests receive clients, auth contexts, fixtures, and cleanup through JUnit parameter resolution rather than base classes.
- Current abstractions appear mostly extracted from proven flows rather than invented upfront.
- `ApiRequester` remains technical and stateless, which keeps domain behavior inside clients.
- Query parameter support remains transport-level; pagination behavior is expressed in `UserClient`, not in the requester.
- `ApiClientExtension` no longer needs manual registration for every API client; concrete clients are created through the common `BaseApiClient` constructor contract.
- This automatic client creation keeps the extension from growing with every new service client, but it should stay limited to API clients, not fixtures.
- `AuthServiceRequestSpecs` still contains only request defaults and authentication header composition; auth state is not hidden in specs.
- `ProductServiceRequestSpec` repeats the auth-service spec shape for a second base URL; this duplication is acceptable while only two services exist.
- `/me` is now the preferred way to assert current-user state, which avoids relying on the first page of the paginated users list.
- `AuthUserFixture.registerUsers(...)` keeps batch setup API-based by repeating the proven single-user registration flow and preserving cleanup.
- The model package split makes request, response, and shared auth/user models more explicit without adding a framework-wide model abstraction.
- Product-service models currently use a separate `model.product` namespace; this matches the second API domain without introducing a global model framework.
- `UiAuthBridge` keeps authenticated UI bootstrap outside page objects, which preserves the page layer as a thin adapter.
- CI now proves the API-centered path against a real Docker Compose application runtime.
- The current design should still avoid adding DB support, parallel execution, rich reporting, generic pagination abstractions, or larger UI abstractions before repeated flows create real pressure.

## Code review notes

- `ApiErrorExtractor` currently casts validation `loc` to `List<String>`. This is enough for current simple field errors but may be too narrow for nested or indexed validation errors.
- `LoginPage.submit(T expectedPage)` was removed from the page object API. Login flow now uses explicit scenario methods.
- Page object identity checks still rely mainly on the common page title locator, currently `h1`. Strengthening them is recorded in `architecture/deferred-tasks.md`.
- Pagination tests avoid exact `total` and exact page contents because the users list is global and may contain data from other tests or seed users.
- `SupplierTest.shouldCreateSupplierWithValidData` is currently a no-op scaffold and should not be counted as real coverage.
- `ApiRequester` remains exposed through `ApiClientExtension`; consider removing it from parameter resolution if tests should only use domain clients.
- `ProductServiceRequestSpec` uses singular naming while the existing auth spec uses plural `AuthServiceRequestSpecs`; naming can be aligned during cleanup.
- The latest GitHub Actions API test run was reported green by the user after adding Docker Compose startup, readiness waiting, API tag filtering, diagnostics artifacts, and cleanup.

## Next steps

1. Keep the API CI baseline stable and inspect artifacts from failed runs when needed.
2. Turn the supplier scaffold into one real API flow: create supplier, fetch or otherwise verify it, assert fields, and clean it up.
3. Add supplier delete/get-by-id support before product creation tests start depending on suppliers.
4. Decide whether `ApiRequester` should remain injectable or become internal-only behind `BaseApiClient`.
5. Decide API logging default policy for local and CI runs.
6. Keep pagination abstraction deferred until another domain client or repeated pagination assertions appear.
7. Keep API error assertion improvements deferred until a second real validation response shape appears.
8. Keep DB support, parallel execution, and richer reporting deferred until the API CI baseline is stable across repeated runs.

## Deferred during review

- `Strengthen page object identity checks` is recorded in `architecture/deferred-tasks.md`.
