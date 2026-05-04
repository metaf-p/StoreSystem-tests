# API layer current state

## Goal

Describe the actual API-facing architecture of `niffler-e-2-e-tests` as it exists now, rather than the adaptation-oriented target shape described in `05-api-layer-adaptation.md`.

## Why it exists

The current framework does not have one pure API layer. It has a split runtime:

- `api/*` defines Retrofit service interfaces and low-level transport helpers
- `service/*` defines test-facing client abstractions
- `service/impl/*` contains concrete protocol and fixture-oriented client implementations
- `RestClient` acts as the shared execution seam for HTTP and SOAP traffic

This split is important because many other blocks depend on it:

- test foundation
- auth bootstrap
- data generation
- assertion placement
- reporting

Evidence:

- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/api/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/service/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/service/impl/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/service/RestClient.java`

## Main elements

- `api/*`
  Retrofit interfaces for remote services such as `AuthApi`, `GatewayApi`, `GatewayV2Api`, `SpendApi`, `UserdataApi`, `GhApi`, and `AllureDockerApi`.
- `api/core/*`
  Transport-adjacent infrastructure such as `ThreadSafeCookieStore` and `CodeInterceptor`.
- `service/RestClient`
  Shared HTTP execution base that creates Retrofit services, configures logging and Allure transport attachments, manages cookies, and enforces technical response correctness.
- `service/UsersClient` and `service/SpendClient`
  Test-facing abstractions that hide whether setup is performed through API or DB.
- `service/impl/*`
  Concrete clients such as `AuthApiClient`, `GatewayApiClient`, `GatewayV2ApiClient`, `SpendApiClient`, `UsersApiClient`, `GhApiClient`, `AllureDockerApiClient`, and `UserdataSoapClient`.

## Internal structure

The current API-facing architecture is layered, but not in a fully clean or symmetrical way.

1. Transport contracts live in `api/*`
   These interfaces mostly describe remote endpoints and request shapes.

2. Shared transport execution lives in `RestClient`
   This base class builds Retrofit clients, configures logging and Allure integration, binds a thread-local cookie jar, and exposes common execution helpers.

3. Concrete remote-service clients live in `service/impl/*`
   They convert endpoint contracts into test-facing methods such as `currentUser`, `allFriends`, `addSpend`, or `login`.

4. Some fixture-oriented orchestration also lives in the same client area
   `UsersApiClient` is not just a pure product-API client. It also composes multiple API calls to create test-ready users, invitations, and friendships.

5. Strategy-switching abstractions sit above the concrete implementations
   `UsersClient.getInstance()` and `SpendClient.getInstance()` choose API-backed or DB-backed implementations using system properties.

This means the current project has a hybrid API/service layer:

- some parts are transport-facing
- some are domain-client-facing
- some are fixture orchestration
- some are implementation selection

## Dependencies

Important dependency relationships:

- `service/impl/*` depends on `api/*` contracts
- `RestClient` depends on `config/*`, Allure transport integration, OkHttp, Retrofit, and `ThreadSafeCookieStore`
- `AuthApiClient` depends on `CodeInterceptor`, `ThreadSafeCookieStore`, OAuth utility code, and `ApiLoginExtension`
- `UsersApiClient` depends on `AuthApi`, `UserdataApi`, `RandomDataUtils`, and test-only `TestData`
- foundation extensions depend on `UsersClient`, `SpendClient`, and `AuthApiClient`

Important boundary:

- tests usually depend on `service/impl/*` clients directly for REST use cases
- tests generally do not depend on `api/*` interfaces directly

## Typical flow

A typical REST-oriented flow looks like this:

1. A test obtains authenticated state through `ApiLoginExtension` or an explicit client login call.
2. The concrete client such as `GatewayApiClient` selects the relevant Retrofit service interface.
3. The client invokes `RestClient.executeForBody(...)` or `executeNoBody(...)`.
4. `RestClient` executes the underlying transport call, checks expected status code, and validates body presence where required.
5. The test receives a deserialized DTO and performs domain assertions directly.

A fixture-oriented API flow looks different:

1. `UserExtension` delegates to `UsersClient.getInstance()`.
2. The API implementation `UsersApiClient` creates a user through auth and userdata endpoints.
3. It may compose several API calls to build invitations or friendship state.
4. The resulting DTO is enriched with test-only `TestData` and stored in the extension context.

## Patterns used

- transport-contract plus client split
  Retrofit interfaces define remote shape; client classes provide test-facing methods.
- shared execution seam
  `RestClient` centralizes transport execution, technical status checking, and logging integration.
- technical assertions at transport boundary
  HTTP status validation and null-body protection happen before tests receive DTOs.
- hybrid domain plus fixture clients
  Some clients are clean remote-service adapters, while others also orchestrate test data creation.
- strategy selection by system property
  `UsersClient` and `SpendClient` can switch implementation path at runtime.

## What is essential

- One shared technical execution seam such as `RestClient`.
- A clear difference between endpoint contracts and test-facing clients.
- A dedicated auth client path.
- Technical response validation before DTOs reach tests.
- Explicit awareness that fixture orchestration is different from normal product API access.

## What is optional

- Runtime switching between API-backed and DB-backed setup clients.
- GitHub and Allure Docker clients inside the same service area.
- Keeping SOAP transport in the same shared execution seam as REST.
- One shared `RestClient` base for every HTTP-like protocol.

## What looks overengineered

- Mixing pure remote-service adapters and fixture-building clients in the same implementation area.
- Strategy switching through system properties for multiple setup paths inside one module.
- Reusing the same transport abstraction for both product APIs and support APIs such as GitHub and Allure Docker.
- The current split between `api/*`, `service/*`, and `service/impl/*` is useful, but the exact responsibility boundary is not always sharp.

## Adaptation for my project

- Keep the central technical requester or base client idea.
- Keep auth as a dedicated client path.
- Separate normal domain clients from fixture-oriented helpers more strictly than the reference project does.
- Do not let implementation-switching abstractions dominate the first version unless DB fallback is truly required.
- If the project is API-first, make the main API clients cleaner and more central than in this reference.

## Readiness criteria

- The team can explain the difference between endpoint contracts, technical execution, domain clients, and fixture helpers.
- Tests normally call clients, not transport contracts.
- Transport-level HTTP correctness is centralized.
- Auth flow is isolated in one reusable client path.
- Fixture-building API logic is either explicitly accepted as a hybrid layer or extracted into dedicated helpers.

## Open questions

- The current reference architecture still blurs the line between remote-service clients and fixture-building clients.
- It is not fully clear whether `service/*` was intended to become a clean service abstraction layer or remained mostly as a pragmatic test-client package.
- For adaptation, it may be worth deciding early whether the project wants one client layer or a stricter split between domain clients and fixture orchestration.
