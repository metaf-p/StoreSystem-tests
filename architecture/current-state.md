# Current framework state

This file stores the latest known snapshot of the framework implementation.

Update it when the user asks to record, snapshot, or fix the current state.
Do not treat it as a roadmap replacement.

## Last updated

2026-05-07

## Implemented

- API technical base:
  - `Config` / `LocalConfig` as the current single configuration boundary.
  - separate auth, product service, and UI base URLs in config.
  - reusable request specs in `AuthServiceRequestSpecs`.
  - product-service request spec in `ProductServiceRequestSpec`.
  - JSON and multipart product-service request specs.
  - reusable response specs in `ResponseSpec`.
  - typed endpoint definitions through `Endpoint`, `AuthEndpoints`, and `ProductServiceEndpoints`.
  - technical transport through `ApiRequest` and `ApiRequester`.
  - query parameter support in `ApiRequest` and `ApiRequester`.
  - multipart/form-data support through `MultipartPart`, `ApiRequest.withPathParamsAndMultipart(...)`, and `ApiRequester` multipart handling.
  - config-driven API logging through `ApiLogMode` and `ApiLoggingFilter`, attached from request specs.
  - API log scoping through `ApiCallScope` and `ApiLogContext`.
  - `TEST_ONLY` logging suppresses calls wrapped as setup or cleanup.
- Auth and authenticated runtime:
  - `AuthClient` for login/register flows.
  - `AuthContext` for access token, refresh token, token type, and user id.
  - `AdminAuthBootstrap` for explicit admin authentication.
- API clients and DTOs:
  - `BaseApiClient`.
  - `ApiClients` as an explicit lightweight client aggregator.
  - `UserClient` for `/me` profile, paginated user list, promote, edit, delete, and raw negative-test paths.
  - user list methods are named `getAll(...)`, `getAllRaw(...)`, and `getAllWithoutAuthRaw(...)`.
  - `SupplierClient` for supplier list, get-by-id, create, delete, raw negative-test paths, unauthenticated paths, and quiet delete.
  - `SupplierClient` for supplier document upload, raw upload, list, raw list, delete, raw delete, quiet single-document delete, and quiet all-documents delete.
  - auth/user DTOs split into `model.auth.common`, `model.auth.request`, and `model.auth.response`.
  - supplier request/response DTOs in `model.product.request` and `model.product.response`.
  - supplier document DTOs through `SupplierDocumentType` and `SupplierDocumentResponse`.
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
  - `UserExtension` creates `@TestUser` users and resolves `@CurrentUser`.
  - `@ApiTest` wires `ApiFixtureExtension` and `UserExtension`.
  - `@UiTest` wires `UiExtension`, `ApiFixtureExtension`, and `UserExtension`.
  - `UiExtension` for Selenide base URL setup and browser cleanup.
- Framework-owned targeted cleanup:
  - `UserCleanup` stores user ids created by fixture APIs.
  - `SupplierCleanup` stores supplier ids created by `SupplierFixture`.
  - `ApiFixtureExtension.afterEach(...)` runs supplier cleanup before user cleanup.
  - `ApiTestRuntime.cleanupUsers()` deletes registered users through `UserClient.deleteQuietly(...)`.
  - `ApiTestRuntime.cleanupSuppliers()` deletes all documents for each registered supplier through `SupplierClient.deleteAllDocumentsQuietly(...)`, then deletes the supplier through `SupplierClient.deleteQuietly(...)`.
  - supplier document cleanup discovers current documents by supplier id instead of registering every uploaded document.
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
  - supplier document tests cover PDF upload, customer upload forbidden path, invalid file type rejection, document list, and document delete.
  - UI auth tests for login page, login/logout flow, registration page visibility, and API-authenticated UI entry.

## Partially implemented

- Configuration is intentionally minimal:
  - only one `LocalConfig` implementation exists.
  - environment-aware selection is deferred until a second real environment or CI/runtime divergence appears.
  - service URLs and admin credentials are hardcoded for the current local runtime.
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
- Multipart support is proven only on supplier documents:
  - the transport can send field and file parts.
  - product-service specs include authenticated and unauthenticated multipart request specs.
  - no generic upload abstraction exists beyond the current supplier document flow.
- API error assertions are useful for current flows but still narrow:
  - validation assertions focus on the first validation error.
  - `loc` handling currently assumes simple field-level validation.
- UI layer is intentionally thin:
  - page objects cover only proven auth/product navigation flows.
  - no component abstraction, screenshot comparison, or advanced UI infrastructure exists.
  - stronger page identity checks beyond the common page title locator are intentionally deferred.
  - `UiAuthBridgeTest` uses `@TestUser` / `@CurrentUser`, but login UI tests still use `AuthUserFixture` directly.
- Test foundation is extension-first but still small:
  - API runtime responsibilities are split between `ApiTestRuntime`, `ApiFixtureExtension`, and `UserExtension`.
  - tests still create `ApiClients` explicitly instead of receiving domain clients through parameter resolution.
  - cleanup exists for users and suppliers, with supplier document cleanup handled as a dependent supplier cleanup step.
  - there is no generic cleanup registry abstraction yet.
  - there is no domain-level fixture aggregator such as `ProductFixtures` yet.
- Product-service support is still supplier-focused:
  - supplier create/list/get/delete flows exist.
  - supplier document upload/list/delete flows exist.
  - product, warehouse, and product-in-warehouse flows are not implemented yet.
  - document fixture APIs have not been extracted; tests upload documents directly through `SupplierClient`.
- Parallel execution is configured but not fully reviewed:
  - `junit-platform.properties` enables concurrent test and class execution with fixed parallelism `3`.
  - cleanup registries are method-runtime scoped through JUnit stores, but the full isolation model has not been validated.
  - UI tests may be unsafe under parallel execution because Selenide/browser state is process-global by default.
- CI/CD baseline currently runs API tests only:
  - UI tests are not included in the first CI e2e run.
  - full UI CI remains a later step because it can introduce separate browser/runtime concerns.
  - CI waits explicitly for auth service readiness only; product-service readiness is currently implicit.

## Not implemented yet

- DB support as a secondary setup path.
- Parallel execution isolation review and hardening.
- Rich reporting or hosted report integration.
- Multiple runtime config implementations and config factory/selection.
- Product-service domain coverage beyond suppliers and supplier documents.
- Product-service fixture aggregator for future supplier/product/warehouse setup.
- Product, warehouse, product-in-warehouse, and related cleanup flows.
- Dedicated supplier document fixture API.
- Generic cleanup registry abstraction across domains.
- Generic pagination model or shared pagination assertions.
- Larger assertion catalog.
- UI component layer or visual regression infrastructure.
- UI tests in CI.

## Current roadmap position

- Stage: Stage 8 is implemented as a minimal API e2e CI baseline; selected Stage 10 API expansion is active inside auth/user and product-service areas.
- Reason:
  - stages 2-6 are represented in code by the API technical base, auth path, first client path, data generation, and extension-first test foundation.
  - Stage 7 is represented by a minimal Selenide page layer and explicit `UiAuthBridge`.
  - Stage 8 is represented by a GitHub Actions workflow that checks out the app repository, starts the app through Docker Compose, waits for auth service readiness, runs API-tagged tests, and preserves diagnostics.
  - the pagination and `/me` work is a narrow Stage 10-style API expansion inside auth/user.
  - product-service work has advanced through supplier get-by-id, delete, supplier fixture setup, supplier cleanup, multipart transport, and supplier document coverage.
  - supplier cleanup is now dependency-aware for supplier documents.
  - Stage 9 DB support remains deferred.
  - Stage 11-style parallel execution settings are already present, but parallel safety has not been reviewed to roadmap readiness criteria.
  - UI test refactoring is partially advanced because `@UiTest` includes `UserExtension` and `UiAuthBridgeTest` uses `@TestUser`, but older UI login tests still use direct fixture setup.
  - DB support, reporting, broader product-service flows, and richer UI remain deferred.

## Architectural observations

- The framework remains aligned with the API-first direction: API clients, auth context, fixtures, and extension lifecycle are the center of the design.
- The project remains aligned with the extension-first direction: tests receive shared fixture/runtime state through JUnit parameter resolution rather than base classes.
- Framework-owned cleanup remains targeted and id-based rather than global.
- Supplier document cleanup fits the current growth rule: documents are cleaned as supplier-owned dependents before introducing a separate document fixture or generic cleanup registry.
- Cleanup only knows about entities created through fixtures; direct raw client setup remains outside the cleanup registry unless it hangs off a registered supplier.
- Wrapping fixture setup and after-test cleanup in `ApiLogContext` keeps `TEST_ONLY` logs focused on the behavior under test.
- `ApiRequester` remains technical and stateless, which keeps domain behavior inside clients.
- `MultipartPart` is still a small transport helper, not a product-domain abstraction.
- `ApiLogContext` remains a narrow runtime helper for API logging scope, not a domain fixture or auth state holder.
- Query parameter support remains transport-level; pagination behavior is expressed in `UserClient`, not in the requester.
- `ApiClients` keeps client construction explicit and small while more API areas are still being proven.
- `AuthServiceRequestSpecs` still contains only request defaults and authentication header composition; auth state is not hidden in specs.
- `ProductServiceRequestSpec` repeats the auth-service spec shape for a second base URL; this duplication is acceptable while only two services exist.
- `/me` remains the preferred way to assert current-user state, which avoids relying on the first page of the paginated users list.
- `AuthUserFixture.registerUsers(...)` keeps batch setup API-based by repeating the proven single-user registration flow.
- `SupplierFixture` proves the same growth pattern for a second API domain: create through API, register id, cleanup through extension.
- `MessageResponse` is a small common model for simple success messages; broader response unification is not needed yet.
- Product-service models use a separate `model.product` namespace; this matches the second API domain without introducing a global model framework.
- `UiAuthBridge` keeps authenticated UI bootstrap outside page objects, which preserves the page layer as a thin adapter.
- CI proves the API-centered path against a real Docker Compose application runtime, but the product service should become an explicit readiness dependency now that product-service tests are growing.
- The current design should still avoid DB support, rich reporting, generic pagination abstractions, product fixture aggregators, or larger UI abstractions until repeated flows create real pressure.
- Parallel execution should be treated as an active risk until cleanup, user creation, supplier creation, and UI/browser isolation are validated under concurrent runs.

## Code review notes

- `src/main/resources/junit-platform.properties` enables method and class concurrency with fixed parallelism `3`, but the current architecture has not completed the Stage 11 isolation review.
- UI tests are likely not safe to run concurrently without explicit browser/session isolation review.
- `ApiErrorExtractor` currently casts validation `loc` to `List<String>`. This is enough for current simple field errors but may be too narrow for nested or indexed validation errors.
- Page object identity checks still rely mainly on the common page title locator, currently `h1`. Strengthening them is recorded in `architecture/deferred-tasks.md`.
- Pagination tests avoid exact `total` and exact page contents because the users list is global and may contain data from other tests or seed users.
- User and supplier cleanup is framework-owned for fixture-created entities, but direct client-created setup data is not tracked unless it is a document under a fixture-created supplier.
- Supplier document cleanup dynamically lists documents before supplier deletion, so it supports multiple documents per registered supplier without storing every document id.
- `SupplierDocumentTest` covers document upload/list/delete behavior, but it does not directly prove cleanup of a supplier with multiple attached documents.
- Supplier full-field contract is currently checked through get-by-id, not by overloading every role/access test with field assertions.
- Supplier list/delete tests still use global list checks, so assertions are anchored to `supplierId` rather than exact list size.
- UI auth bridge test refactoring has started, but login UI tests still use direct `AuthUserFixture` setup.
- `ProductServiceRequestSpec` uses singular naming while the existing auth spec uses plural `AuthServiceRequestSpecs`; naming can be aligned during cleanup.
- `UserExtension` currently keeps an unused `ApiClients` field.
- `.DS_Store` files are present under `src/` and should be removed from source control if tracked.
- The current snapshot was produced by code inspection only. Tests were not run during this review, following the project rule for framework state review unless the user explicitly asks to run tests.

## Next steps

1. Run targeted `SupplierDocumentTest` and `SupplierTest`, then the API suite, before treating supplier document cleanup as validated.
2. Prove or adjust supplier cleanup with multiple documents on one supplier.
3. Review parallel execution immediately: either validate the current fixed parallelism `3` setup or disable it until cleanup and UI isolation are ready.
4. Add product-service readiness waiting to CI now that supplier and supplier document tests depend on it.
5. Keep supplier cleanup id-based and fixture-owned; avoid global table cleanup.
6. Continue product-service expansion with the next dependent flow, likely warehouse or product creation, after supplier document cleanup is stable.
7. Add product-service fixture aggregation only when another product-service fixture appears and test parameters start to grow.
8. Keep generic cleanup registry, DB support, richer reporting, and larger UI abstractions deferred until repeated flows create real pressure.
9. Refactor remaining UI tests to the current declarative user fixture style before enabling UI tests in CI.
10. Remove tracked `.DS_Store` files if they are committed or show up in future status.

## Deferred during review

- `Strengthen page object identity checks` remains recorded in `architecture/deferred-tasks.md`.
- UI test refactoring to `@TestUser` / `@CurrentUser` remains partially complete and should remain deferred for the remaining UI login tests.
- User cleanup is no longer just deferred; the current deferred backlog should track only future cleanup isolation concerns.
- Parallel execution isolation review remains deferred but is now higher priority because concurrency settings are already enabled.
