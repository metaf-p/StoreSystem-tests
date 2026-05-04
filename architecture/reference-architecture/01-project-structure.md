# Project structure

## Goal

Describe how `niffler-e-2-e-tests` is organized as a multi-interface test framework, and identify the internal boundaries that matter if the same architecture is adapted into another project.

## Why it exists

The module is a dedicated test project, not application code. Its purpose is to run end-to-end and contract-style checks against the Niffler system through several interfaces from one place:

- web UI tests in `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/test/web`
- REST tests in `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/test/rest`
- GraphQL tests in `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/test/gql`
- gRPC tests in `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/test/grpc`
- SOAP tests in `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/test/soap`

Evidence:

- `niffler-e-2-e-tests/build.gradle` declares only test dependencies and configures generated test sources.
- The module tree contains `src/test/*` only and no `src/main/*`.
- `settings.gradle` includes `niffler-e-2-e-tests` as a separate Gradle module.

## Main elements

- `src/test/java/guru/qa/niffler/test`
  Entry point for protocol-specific test suites. This is where business-readable tests live.
- `src/test/java/guru/qa/niffler/jupiter`
  Test runtime composition layer: custom annotations and JUnit 5 extensions that prepare users, categories, spendings, browser state, API login, and suite cleanup.
- `src/test/java/guru/qa/niffler/page`
  UI layer for Selenide page objects and reusable page components.
- `src/test/java/guru/qa/niffler/api`
  Retrofit-style API interface definitions plus SOAP converter helpers.
- `src/test/java/guru/qa/niffler/service`
  Higher-level clients used by extensions and tests. This layer hides transport and test-data setup choices.
- `src/test/java/guru/qa/niffler/data`
  Direct database access, entities, repositories, JDBC helpers, JPA helpers, transaction templates, and SQL logging attachments.
- `src/test/java/guru/qa/niffler/config`
  Environment selector for local versus docker execution.
- `src/test/java/guru/qa/niffler/model`
  Test-facing DTOs and response models shared across protocols.
- `src/test/resources`
  SQL scripts, JPA config, JUnit platform settings, logback setup, Allure templates, screenshot baselines, and service registration files.
- `src/test/graphql` and `src/test/schemas/xjc`
  Source assets for generated GraphQL and SOAP client code.

## Internal structure

At a high level the module is organized in layers:

1. Test suites
   Files under `test/web`, `test/rest`, `test/gql`, `test/grpc`, and `test/soap` express scenario intent.
2. Test orchestration
   Files under `jupiter/annotation` and `jupiter/extension` convert annotations like `@User`, `@ApiLogin`, and `@WebTest` into runtime setup.
3. Access adapters
   `page`, `api`, and `service` encapsulate how tests talk to the system through UI, HTTP, GraphQL, gRPC, or SOAP.
4. Data setup internals
   `data/*` implements low-level persistence access for direct seeding and cleanup.
5. Shared support
   `config`, `model`, `condition`, and `utils` provide environment lookup, DTOs, assertions, and helpers.

Notable structural decisions:

- The framework is JUnit-extension-first.
  Meta-annotations are the user-facing entry point, but most orchestration lives in JUnit 5 extensions rather than in base classes or transport helpers.
- Meta-annotations define test families.
  `jupiter/annotation/meta/WebTest.java`, `RestTest.java`, `GqlTest.java`, `GrpcTest.java`, and `SoapTest.java` each assemble the extension stack for a protocol.
- Global extensions are auto-registered.
  `src/test/resources/META-INF/services/org.junit.jupiter.api.extension.Extension` adds shared extensions such as `CookieStoreExtension`, `DatabasesExtension`, and `AllureDockerExtension` for the whole suite.
- Protocol-specific base classes exist where setup is heavy.
  Examples: `BaseGraphQlTest.java` and `BaseGrpcTest.java`.
- Test data can be provisioned through interchangeable strategies.
  `UsersClient.getInstance()` switches between `UsersApiClient` and `UsersDbClient`; repositories also switch between `jdbc`, `spring-jdbc`, and default `jpa`.

## Dependencies

Internal dependencies are mostly directional, even though everything lives in one Gradle module.

- `test/*` depends on `jupiter`, `page`, `service`, `model`, and sometimes `api`
- `jupiter/extension` depends on `service`, `model`, and selected `page` classes
- `service` depends on `api`, `data`, `config`, and `model`
- `data/repository` depends on `data/entity`, `data/jdbc`, `data/jpa`, `data/dao`, and transaction helpers
- `config` is consumed almost everywhere but does not depend on higher layers
- `resources` support all layers through runtime configuration

External dependencies that are architecturally important:

- JUnit 5 for execution and extension points in `build.gradle`
- Selenide for the UI layer
- Retrofit and OkHttp for REST and SOAP HTTP access
- Apollo GraphQL plugin and generated sources for GraphQL tests
- gRPC stubs from `project(':niffler-grpc-common')`
- Spring JDBC, Hibernate, and Atomikos for direct DB setup and cross-database transactions
- Allure for reporting and transport logging

Direct CI/CD dependency relevant to this module:

- `.github/workflows/e2e.yaml` builds the system, starts docker compose, waits for the `niffler-e-2-e` container, and uploads screenshots from `niffler-e-2-e-tests/.screen-output/screenshots/selenoid`.
- `niffler-e-2-e-tests/Dockerfile` packages this module together with `niffler-grpc-common` and runs `gradle test -Dtest.env=docker -Drepository=jpa`.

## Typical flow

Typical execution flow for a test such as `LoginTest` or `FriendsRestTest`:

1. A test class selects a protocol bundle using a meta-annotation such as `@WebTest` or `@RestTest`.
2. JUnit loads global extensions from `META-INF/services/...Extension` and protocol-specific extensions from the meta-annotation.
3. Method-level annotations such as `@User` and `@ApiLogin` trigger fixture creation and authentication.
4. Fixture creation delegates to `UsersClient`, which chooses API-based or DB-based setup depending on `client.impl`.
5. If DB setup is used, repositories choose JDBC, Spring JDBC, or Hibernate depending on `repository.impl`.
6. The test interacts through page objects, API clients, Apollo GraphQL client, SOAP client, or gRPC stub.
7. Logging, screenshots, SQL attachments, and Allure integrations capture execution evidence.

Evidence for this flow:

- `LoginTest.java` uses `@WebTest` and `@User`, then drives `LoginPage` and `MainPage`.
- `FriendsRestTest.java` uses `@RestTest`, `@RegisterExtension ApiLoginExtension.rest()`, and a `@Token` parameter.
- `ApiLoginExtension.java` logs in through `AuthApiClient`, stores the token, and optionally hydrates browser state.
- `UserExtension.java` materializes test users and related people before each test.

## Patterns used

- Test-family composition through meta-annotations
  The project avoids repeating `@ExtendWith(...)` in every class by bundling extension sets per protocol.
- Fixture-as-annotation
  Test data is declared at method level with annotations such as `@User`, `@Category`, and `@Spending`.
- Strategy selection by system property
  `Config`, `UsersClient`, and repository factories switch implementation based on runtime properties like `test.env`, `client.impl`, and `repository.impl`.
- Layered access adapters
  Tests call higher-level pages and service clients instead of mixing raw HTTP, browser, and SQL logic in test bodies.
- Generated client artifacts
  GraphQL schema introspection and XJC generation are part of the module structure, not an afterthought.
- Parallel execution with thread-local isolation
  `junit-platform.properties` enables concurrent execution, and `ThreadSafeCookieStore` isolates cookies per thread.

## What is essential

- Separate `test/*` packages by protocol or execution style
  This makes the suite readable and prevents UI, REST, and DB concerns from collapsing into one namespace.
- A dedicated orchestration layer for JUnit extensions
  The `jupiter` package is the real framework core.
- A small configuration boundary
  `Config`, `LocalConfig`, and `DockerConfig` keep environment switching centralized.
- Reusable adapters for system access
  `page`, `api`, and `service` keep test bodies compact.
- Shared test models and resources
  DTOs, SQL scripts, templates, and baseline screenshots are practical necessities.

## What is optional

- Keeping all protocols in one module
  Useful for a teaching or showcase repo, but not mandatory in another project.
- Multiple DB access implementations at once
  Supporting JDBC, Spring JDBC, and Hibernate inside the same test module is optional.
- Generated SOAP and GraphQL clients inside the test project
  Valuable when those interfaces are stable and actively tested, but not required for every E2E suite.
- Fake tests under `test/fake`
  These look like exploratory or support tests rather than the main architecture backbone.
- Dockerized module packaging through the module-specific `Dockerfile`
  Helpful for CI portability, but not essential if the suite runs directly on CI runners.

## What looks overengineered

- Three persistence styles behind one repository abstraction
  `AuthUserRepository`, `UserdataUserRepository`, and `SpendRepository` each support multiple implementations. For adaptation, this is probably more flexibility than most teams need.
- Deep data stack inside a test module
  The presence of DAOs, repositories, entities, JDBC helpers, JPA helpers, and XA transaction templates suggests the test framework duplicates part of application-side persistence architecture.
- Mixing true E2E execution with direct DB state manipulation
  This is pragmatic, but it also makes the project a hybrid of black-box tests and privileged system setup tooling.
- Broad protocol coverage in one package root
  Web, REST, GraphQL, SOAP, and gRPC together make the project powerful, but also heavier to understand and maintain.

## Adaptation for my project

- Keep the package split by responsibility, but reduce the number of interchangeable implementations.
  A good adaptation would keep `test`, `jupiter`, `page` or `client`, `config`, `model`, and a single fixture-setup mechanism.
- Preserve meta-annotations and extensions.
  This is one of the strongest ideas in the project because it keeps tests declarative.
- Choose one privileged fixture path.
  Prefer either API-driven setup or DB-driven setup, not both, unless the extra complexity is justified.
- Separate true E2E tests from protocol contract tests if your team needs clearer boundaries.
  In this reference project they coexist in one module, which is convenient but can blur scope.
- Keep generated clients only for interfaces you really intend to test directly.
  Otherwise they become maintenance overhead.
- If parallelism matters, keep thread-local state isolation from the start.
  `ThreadSafeCookieStore` exists because the suite runs concurrently.

## Readiness criteria

- The adapted project has a dedicated test module or clearly isolated test root.
- Test classes are grouped by protocol or scenario family, not by low-level technical helper.
- Reusable setup lives in JUnit extensions or equivalent orchestration, not copied across tests.
- Environment differences are centralized in one configuration boundary.
- The suite has one explicit strategy for fixture creation and one clear place for access adapters.
- CI can run the module in a repeatable way and collect evidence artifacts that the suite actually produces.

## Open questions

- Hypothesis: `test/fake` may be used for experiments or demonstrations rather than the main regression suite. That should be confirmed before copying this package structure into another project.
- The exact ownership split between `api/*` and `service/impl/*` is slightly blurred. In adaptation, it may be worth collapsing one layer if the team prefers a thinner framework.
- The GraphQL and SOAP generated-source workflow is structurally important here, but it is not yet clear whether both are heavily used in day-to-day regression runs or retained partly for coverage completeness.
