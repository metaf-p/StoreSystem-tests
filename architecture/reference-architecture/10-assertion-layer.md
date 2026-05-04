# Assertion layer

## Goal

Describe how `niffler-e-2-e-tests` organizes assertions across its test framework, and identify which assertion patterns are worth keeping or simplifying in another project.

## Why it exists

This test module covers several interfaces:

- web UI
- REST
- GraphQL
- gRPC
- SOAP

Because of that, one single assertion style would not fit every layer well. The project instead uses a mixed model:

- protocol and business-result assertions mostly stay in test classes
- UI-specific assertions are extracted into pages, components, and custom Selenide conditions
- transport-level correctness checks are enforced in shared client helpers

This gives the suite a practical split between:

- domain expectations close to the test scenario
- technical UI checks close to selectors and widgets
- technical HTTP checks close to the requester

Evidence:

- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/test/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/page/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/page/component/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/condition/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/service/RestClient.java`

## Main elements

- JUnit assertions in test classes
  Most REST, GraphQL, gRPC, and SOAP tests use `assertEquals`, `assertTrue`, `assertNull`, `assertNotNull`, `assertIterableEquals`, and `assertAll` directly in the test body.
- `Allure.step(...)` wrappers in tests
  Many protocol tests wrap assertions inside step blocks so failures are grouped by business meaning.
- page-level check methods
  UI pages such as `LoginPage` and `BasePage` provide methods like `checkError`, `checkAlert`, `checkFormErrorMessage`, and `checkThatPageLoaded`.
- component-level check methods
  UI components such as `SpendingTable` and `StatComponent` contain reusable checks like `checkTableContains`, `checkTableSize`, `checkBubbles`, and `checkStatisticImage`.
- custom Selenide condition helpers
  `ScreenshotConditions`, `StatConditions`, and `SpendConditions` implement reusable comparison logic beyond built-in Selenide text and visibility checks.
- technical response assertions in `RestClient`
  `executeForBody`, `executeNoBody`, and `assertStatus` enforce expected HTTP status codes and non-null bodies at the transport helper level.

## Internal structure

The assertion layer is organized by interaction style, not by one central assertion library.

1. Test-level assertions for API and protocol checks
   REST, GraphQL, gRPC, and SOAP tests keep most semantic result checks in the test class itself.

2. Extracted UI assertions in pages and components
   UI assertions that are tied to stable selectors, widgets, or repeated fragments are moved into page/component methods.

3. Custom visual and collection comparison logic in `condition/*`
   When built-in Selenide conditions are not enough, the project adds custom `WebElementCondition` and `WebElementsCondition` implementations.

4. Technical transport assertions inside the requester/helper layer
   The REST helper validates status codes and body presence before returning values to tests.

This means the suite has no heavy standalone assertion DSL. Instead, it distributes assertions across:

- tests for scenario meaning
- pages/components for reusable UI checks
- conditions for custom UI comparison mechanics
- shared transport helpers for low-level HTTP correctness

## Dependencies

The assertion approach depends on:

- JUnit 5 assertions for protocol-level checks
- Selenide built-in conditions for standard UI checks
- custom Selenide conditions for visual and structured UI comparisons
- Allure step wrappers for readable failure grouping

Important dependency relationships:

- UI page/component checks depend on Selenide selectors and conditions
- `StatComponent` depends on `ScreenshotConditions.image(...)` and `StatConditions.statBubble(...)`
- `BasePage` centralizes common UI error/alert assertions for descendant pages
- `RestClient` depends on Retrofit `Response<?>` and throws assertion-style failures when HTTP expectations are violated
- protocol tests depend directly on DTO/model objects and perform their own domain assertions

Important non-dependency:

- the DB layer has no visible dedicated assertion abstraction
  DB support is used for setup/reset, not for a separate assertion architecture

Evidence:

- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/page/BasePage.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/page/LoginPage.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/page/component/SpendingTable.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/page/component/StatComponent.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/condition/SpendConditions.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/service/RestClient.java`

## Typical flow

A typical assertion flow in this project looks like this:

1. Test setup creates the required state.
2. A client call or UI flow produces the result.
3. Technical correctness is partially checked automatically:
   - UI waits/checks through Selenide conditions
   - HTTP status/body checks through `RestClient`
4. The test asserts business meaning:
   - directly in JUnit assertions for REST, GraphQL, gRPC, and SOAP
   - indirectly through page/component `check...` methods for UI
5. If the UI check is custom, a condition helper performs the actual comparison and returns a rich mismatch message.

Examples:

- `LoginTest.java`
  The test mostly reads like a scenario because `LoginPage` and `MainPage` own the UI checks.
- `FriendsRestTest.java` and `UsersV2RestTest.java`
  The tests call API clients, then use JUnit assertions directly for domain expectations.
- `SpendingTest.java`
  The test delegates table/stat assertions to page components and custom conditions.
- `RestClient.java`
  Status-code and non-null-body assertions happen before API tests receive the result object.

## Patterns used

- assertion placement by layer
  UI checks are extracted more aggressively than API/protocol checks.
- domain assertions stay near scenarios
  REST, GraphQL, gRPC, and SOAP tests usually keep semantic expectations in the test body.
- selector-bound assertions stay near selectors
  UI checks tied to stable elements live in pages/components.
- technical assertions close to infrastructure
  HTTP status and null-body validation are enforced in the transport helper.
- custom condition pattern for non-trivial UI checks
  Visual comparison, chart bubble matching, and table-content normalization use Selenide custom conditions instead of ad hoc inline logic.
- fluent check methods
  UI assertions are often exposed as chainable `check...` methods returning the page/component object.
- Allure step wrapping for readability
  Many non-UI tests keep assertions in the test body but wrap them in steps for better report narration.

## What is essential

- Keep business-result assertions close to the test scenario.
  This is especially important for API and protocol tests.
- Extract repeated UI checks into page/component methods.
  Alert, form error, table presence, and page-loaded checks are high-value candidates.
- Keep technical transport assertions in one shared client/requester layer.
  Status-code and body-presence checks should not be duplicated in every test.
- Add custom assertion helpers only where built-in framework conditions are clearly insufficient.
  This is the strongest reusable lesson from `condition/*`.
- Maintain a clear separation between:
  - technical assertions
  - domain assertions
  - selector-bound UI assertions

## What is optional

- Wrapping most assertions in `Allure.step(...)`
  Helpful for reports, but not required for a minimal project.
- Fluent `check...` methods everywhere in the UI layer
  Convenient, but not mandatory.
- Dedicated custom conditions for collection comparison
  Useful when the UI presents structured repeated data; optional in a smaller suite.
- Screenshot-based assertions
  Valuable only if visual correctness matters enough to justify image-baseline maintenance.
- Rich mismatch messages in every helper
  Good for debugging, but not necessary at the very beginning.

## What looks overengineered

- Visual assertion stack for a small training project
  `ScreenshotConditions`, screenshot baselines, and diff support are powerful but heavy if visual regression is not a core need.
- Several custom UI comparison helpers
  `SpendConditions` and `StatConditions` are well-structured, but they add framework weight beyond a minimal suite.
- Mixed assertion styles across the suite
  The project uses direct JUnit assertions, Allure step-wrapped assertions, page/component checks, and custom Selenide conditions. This is practical, but less uniform than a smaller project may want.
- No dedicated API assertion abstraction, but transport assertions in the requester
  This is not wrong, but it means API assertions are split between tests and client infrastructure rather than having one explicit API-assertion layer.

## Adaptation for my project

- Keep API and protocol assertions mostly in test code at first.
  That preserves readability and avoids premature assertion frameworks.
- Extract only repeated UI checks.
  Alerts, page-loaded checks, form errors, and table presence are good early candidates.
- Put technical response validation in one shared requester/client base.
  Status and null-body checks belong there.
- Add custom assertion helpers only for:
  - non-trivial UI structures
  - image checks
  - repeated comparison rules that would otherwise clutter many tests
- Do not build a separate assertion DSL unless repetition clearly demands it.
- If you add custom helpers, keep them technical.
  Business meaning should still be visible in the tests.
- If your project is API-first, you may not need anything beyond:
  - JUnit assertions in tests
  - a requester/client base that enforces status/body expectations

## Readiness criteria

- The team can explain where UI assertions should live versus where API assertions should live.
- Tests are not full of duplicated selector-based checks.
- Technical HTTP validation is centralized.
- Business expectations remain readable in the test scenarios.
- Custom assertion helpers exist only where they clearly reduce duplication or improve failure diagnostics.
- The assertion approach is understandable without introducing a separate mini-framework just for checks.

## Open questions

- Hypothesis: the project intentionally avoids a dedicated API assertion layer because direct JUnit assertions plus shared transport checks were enough for the current protocol tests.
- There is no visible DB-specific assertion architecture in scope files. That likely means DB is treated mainly as setup/reset support rather than as an assertion surface.
- The UI assertion extraction is stronger than the API assertion extraction. That appears intentional, but in another project the balance might shift if API response shapes become more repetitive than the current suite shows.
