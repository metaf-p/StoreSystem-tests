# Test foundation

## Goal

Explain the shared execution foundation of `niffler-e-2-e-tests`: how tests start, how JUnit lifecycle is composed, how fixtures are injected, and which framework mechanics are worth reusing in another project.

## Why it exists

The module supports several test styles from one codebase: web, REST, GraphQL, gRPC, and SOAP. Without a common foundation, each test class would need to repeat browser setup, API login, user creation, fixture initialization, cleanup, and reporting hooks.

The foundation exists to make tests declarative:

- test classes choose a family with meta-annotations such as `@WebTest` or `@RestTest`
- test methods declare required state with annotations such as `@User`, `@ApiLogin`, `@Category`, and `@Spending`
- extensions create runtime state and inject values into parameters such as `UserJson`, `CategoryJson[]`, `SpendJson[]`, `BufferedImage`, and bearer tokens

Evidence:

- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/annotation/meta/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/*`
- `niffler-e-2-e-tests/src/test/resources/META-INF/services/org.junit.jupiter.api.extension.Extension`

## Main elements

- `jupiter/annotation/meta`
  Defines protocol-level test families: `WebTest`, `RestTest`, `GqlTest`, `GrpcTest`, `SoapTest`.
- `jupiter/annotation`
  Defines method- or parameter-level declarative inputs such as `@User`, `@ApiLogin`, `@Token`, `@ScreenShotTest`, and `@DisabledByIssue`.
- `jupiter/extension`
  Implements lifecycle hooks, fixture setup, condition evaluation, cleanup, and parameter resolution.
- `test/*/Base*Test`
  Provides base classes only where shared client wiring is substantial: `BaseGraphQlTest`, `BaseGrpcTest`, and `BaseSoapTest`.
- `src/test/resources/junit-platform.properties`
  Enables concurrent execution and sets fixed thread pool size.
- `src/test/resources/META-INF/services/org.junit.jupiter.api.extension.Extension`
  Auto-registers global extensions for every test run.
- `build.gradle`
  Enables JUnit Platform execution and includes generated-source steps that some test families depend on before tests start.

## Internal structure

The foundation is built from four mechanisms working together.

The architectural bias is JUnit-extension-first.

Tests see declarative annotations first, but the real execution model is built around JUnit 5 extensions and their lifecycle hooks.

1. JUnit 5 as the runtime container
   `build.gradle` configures `useJUnitPlatform()` and passes system properties through the `test` task.
2. Meta-annotation bundles for class-level setup
   Each test family is a reusable extension set. For example, `WebTest` combines `BrowserExtension`, `AllureJunit5`, `UserExtension`, `CategoryExtension`, `SpendingExtension`, and `ApiLoginExtension`.
3. Auto-registered global extensions
   The service file under `META-INF/services/...Extension` registers:
   - `CookieStoreExtension`
   - `DatabasesExtension`
   - `TestMethodContextExtension`
   - `AllureBackendLogsExtension`
   - `AllureDockerExtension`
4. Base classes and registered extensions for heavier protocols
   `BaseGraphQlTest` and some REST tests use `@RegisterExtension ApiLoginExtension.rest()` because those flows need API login but not browser login bootstrapping.

The key structural point is that the framework relies more on composition than inheritance:

- inheritance is used sparingly for reusable client setup
- JUnit extensions provide most of the test lifecycle behavior
- method-level annotations define fixtures
- a thread-local method context is used as the shared storage backbone between extensions

## Dependencies

Core dependency relationships inside the foundation:

- meta-annotations depend on JUnit `@ExtendWith` and on the extension classes they bundle
- fixture extensions depend on:
  - `TestMethodContextExtension` for per-test storage
  - `service/*` clients for creating users, categories, spendings, and authentication
  - `config/*` for environment-specific URLs and behavior
- base test classes depend on protocol-specific clients and configuration
- global suite extensions depend on runtime resources such as logs, Allure result files, DB connections, or cookie state

External dependencies that matter to the foundation:

- JUnit Jupiter for callbacks, conditions, parameter resolution, parallel execution, and `@RegisterExtension`
- Allure JUnit 5 and transport integrations for reporting hooks
- Selenide for browser lifecycle management in `BrowserExtension`
- Spring JDBC and SQL script utilities for `TruncatedDatabasesExtension`
- Apollo, gRPC, and SOAP tooling where the foundation provides protocol-specific base classes

Evidence:

- `niffler-e-2-e-tests/build.gradle`
- `niffler-e-2-e-tests/src/test/resources/junit-platform.properties`
- `niffler-e-2-e-tests/src/test/resources/META-INF/persistence.xml`

## Typical flow

A typical test execution flows like this:

1. Gradle starts the JUnit Platform test task.
2. Global extensions are discovered from `META-INF/services/...Extension`.
3. The test class activates a family annotation such as `@WebTest`, `@RestTest`, or `@SoapTest`.
4. Before each test, `TestMethodContextExtension` stores the current `ExtensionContext` in thread-local storage.
5. Fixture extensions inspect method annotations:
   - `UserExtension` creates or registers the test user
   - `CategoryExtension` creates categories if declared
   - `SpendingExtension` creates spendings if declared
6. If the test uses `@ApiLogin`, `ApiLoginExtension` authenticates the user and stores the token.
   For web tests it also injects browser session state.
7. Parameter resolvers expose prepared objects to the test method.
8. After execution, cleanup extensions clear cookie state, archive categories if needed, close drivers, and run suite-level post-processing when the whole run ends.

Concrete evidence:

- `ProfileTest.java` shows `@WebTest`, `@User`, `@ApiLogin`, and `@ScreenShotTest` working together in one method.
- `UsersV2RestTest.java` shows `@RestTest`, `@RegisterExtension ApiLoginExtension.rest()`, `@Isolated`, and `TruncatedDatabasesExtension`.
- `UserDataUsersSoapTest.java` shows a protocol base class plus plain `@User`-driven setup.

## Patterns used

- JUnit-extension-first orchestration
  The core foundation behavior is implemented through JUnit 5 extensions, while annotations act as the declarative surface over that extension model.
- Meta-annotation composition
  The main pattern is class-level bundling of extension stacks per protocol.
- Declarative fixture DSL
  Test methods describe state through annotations rather than imperative setup code.
- Parameter injection as the handoff boundary
  Extensions produce objects and JUnit injects them into test methods.
- Thread-local method context
  `TestMethodContextExtension` gives multiple extensions a shared per-test storage mechanism even during parallel execution.
- Global cross-cutting hooks via service registration
  Cookie cleanup, suite cleanup, and reporting hooks are loaded without repeating `@ExtendWith` everywhere.
- Minimal inheritance, targeted base classes
  Base classes exist only where shared client wiring is more convenient than re-declaring it.
- Per-suite lifecycle emulation
  `SuiteExtension` uses root-context storage and `AutoCloseable` to simulate `beforeSuite` and `afterSuite`.

## What is essential

- JUnit 5 extension-based setup
  This is the real backbone of the foundation.
- One class-level abstraction for test families
  Meta-annotations like `@WebTest` and `@RestTest` are high-value and low-noise.
- One per-test shared state mechanism
  The thread-local `ExtensionContext` pattern is important here because multiple extensions cooperate.
- Declarative fixture creation for common entities
  `@User` plus a small number of fixture extensions give tests a compact, readable style.
- Environment-aware authentication/bootstrap handling
  `ApiLoginExtension` is central because many tests need authenticated state without repeating login code.
- Predictable cleanup for parallel runs
  `CookieStoreExtension` and browser cleanup prevent cross-test leakage.

## What is optional

- Protocol-specific base classes
  Helpful for GraphQL, gRPC, and SOAP, but not mandatory if a project has only one or two test interfaces.
- Screenshot comparison support
  `@ScreenShotTest` and `ScreenShotTestExtension` are useful for UI regression but not foundational for every training project.
- GitHub issue-based conditional disabling
  `@DisabledByIssue` and `IssueExtension` are nice workflow automation, not a core test foundation requirement.
- Suite-level Allure publishing and backend log collection
  `AllureDockerExtension` and `AllureBackendLogsExtension` are reporting conveniences.
- Full database truncation hooks
  `TruncatedDatabasesExtension` is situational and used selectively.

## What looks overengineered

- Large number of cooperating extensions for one test module
  The model is powerful, but it raises the learning curve for a smaller project.
- Custom suite lifecycle abstraction
  `SuiteExtension` is clever and practical, but it is extra framework machinery that a smaller project may not need.
- Heavy reporting integration in the foundation layer
  Allure Docker upload, backend log bundling, screenshot diffs, and transport logging make the foundation wider than a minimal training suite needs.
- Mixed extension styles
  The project combines auto-registered extensions, meta-annotations, `@ExtendWith`, and `@RegisterExtension`. This is flexible, but harder to reason about than one or two consistent patterns.
- Legacy or auxiliary mechanisms still present
  `UsersQueueExtension` is marked `@Deprecated`, and `ClientResolver` is used only in disabled fake tests. These indicate foundation evolution but are not part of the main path anymore.

## Adaptation for my project

- Keep the architecture extension-first.
  Start with JUnit extensions and only add base classes where client initialization is genuinely repetitive.
- Preserve the meta-annotation idea.
  A small set of test-family annotations is one of the best reusable ideas from this project.
- Reduce fixture scope to the minimum useful set.
  For a smaller project, `@User` plus one authentication helper may be enough at first.
- Prefer one registration style for most mechanics.
  For example:
  - global cleanup via service registration
  - family setup via meta-annotations
  - avoid extra one-off mechanisms unless clearly needed
- Add screenshot diffing, issue-based disabling, and suite report publishing only after the core workflow is stable.
- If you enable parallel execution early, also design storage and cleanup for thread isolation early.
  This project already had to solve that with `TestMethodContextExtension` and `ThreadSafeCookieStore`.

## Readiness criteria

- Test classes can start with one family annotation instead of repeating setup annotations.
- Common fixtures are declared in tests, not manually built in each method.
- Shared runtime state is isolated per test execution.
- Authentication setup is reusable and protocol-aware.
- Cleanup for cookies, browser state, and any mutable shared resources is automatic.
- The team can explain which extensions are global, which are family-specific, and which are optional extras.
- A minimal newcomer can read one test class and understand where its data and authenticated state come from.

## Open questions

- Hypothesis: `ClientResolver` and `UsersQueueExtension` represent an older foundation style kept for experiments or legacy support rather than active mainline usage.
- The current project has no visible tagging strategy and no explicit suite classes; if adaptation needs selective execution by layer or feature, an additional taxonomy may be required.
- `TestMethodContextExtension` is effective, but it creates implicit coupling between extensions through shared storage. In a smaller project, it may be worth checking whether simpler explicit parameter passing would be enough.
