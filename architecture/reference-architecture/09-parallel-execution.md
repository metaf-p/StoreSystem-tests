# Parallel execution

## Goal

Explain how `niffler-e-2-e-tests` enables concurrent test execution, what isolation mechanisms make that possible, and where the framework still needs explicit opt-outs for tests that are not safe to run alongside others.

## Why it exists

This test module is large enough and infrastructure-heavy enough that sequential execution would slow feedback significantly. The framework therefore enables JUnit 5 parallel mode by default and then builds supporting isolation around:

- per-test context
- per-thread request/session state
- per-thread DB handles
- mostly unique fixture data
- browser teardown after each test

At the same time, the project acknowledges that not every scenario is parallel-safe. Some tests use stronger reset behavior and are explicitly isolated.

Evidence:

- `niffler-e-2-e-tests/src/test/resources/junit-platform.properties`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/TestMethodContextExtension.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/api/core/ThreadSafeCookieStore.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/test/rest/UsersV2RestTest.java`

## Main elements

- `src/test/resources/junit-platform.properties`
  Globally enables JUnit Jupiter parallel execution with a fixed pool size of `3`.
- `TestMethodContextExtension`
  Stores the current `ExtensionContext` in a `ThreadLocal`, giving extensions thread-bound access to method-scoped stores.
- JUnit method stores via extension namespaces
  `UserExtension`, `CategoryExtension`, `SpendingExtension`, `ApiLoginExtension`, and `ScreenShotTestExtension` keep per-test data in the current method context rather than in global mutable fields.
- `ThreadSafeCookieStore`
  Uses a `ThreadLocal<CookieStore>` so REST/API login state does not leak between concurrently running tests.
- `CookieStoreExtension`
  Clears the thread-local cookie store after each test execution.
- `BrowserExtension`
  Configures browser runtime and closes the current WebDriver after each test, which keeps browser state short-lived.
- DB handle isolation helpers
  `JdbcConnectionHolder` keeps one connection per thread and `ThreadSafeEntityManager` keeps one `EntityManager` per thread.
- shared factory caches
  `DataSources` and `EntityManagers` cache expensive factories in concurrent maps while leaving request/transaction state thread-scoped.
- explicit opt-out mechanisms
  `@Isolated` and `TruncatedDatabasesExtension` are used for tests that would otherwise conflict with concurrent execution.
- deprecated static-user pool
  `UsersQueueExtension` shows an older queue-based resource-sharing approach that is concurrency-aware but no longer the preferred design.

## Internal structure

The parallel-execution design is layered.

1. JUnit enables concurrency globally
   The platform configuration turns on concurrent execution for both test methods and classes.

2. Global extensions establish thread-bound infrastructure
   `TestMethodContextExtension` and `CookieStoreExtension` are auto-registered through `META-INF/services/org.junit.jupiter.api.extension.Extension`.

3. Meta-annotations compose mostly stateless test support
   `@WebTest`, `@RestTest`, `@GqlTest`, `@GrpcTest`, and `@SoapTest` attach extensions that create per-test fixtures and session state.

4. Mutable test state is stored in method context or thread locals
   Tokens, generated users, categories, spendings, screenshots, and cookies are resolved from the current method or thread, not from shared globals.

5. Lower layers separate shared heavy objects from thread-bound mutable objects
   Data source factories and entity-manager factories are shared, while connections, entity managers, cookies, and extension data are scoped to the active thread/test.

6. Conflict-prone scenarios are pushed out of the normal concurrent flow
   Tests that truncate shared databases or broadly mutate global data can declare `@Isolated`.

## Dependencies

Parallel execution here depends on:

- JUnit 5 Jupiter parallel runner configuration
- extension-based lifecycle hooks
- Selenide and Selenium thread-bound driver behavior
- DB helper classes that avoid sharing live connections/entity managers across worker threads
- fixture generators that usually create fresh data instead of reusing named fixtures

Important dependency relationships:

- `ApiLoginExtension` depends on `TestMethodContextExtension` for token/code storage and on `ThreadSafeCookieStore` for session cookie isolation
- `UserExtension`, `CategoryExtension`, and `SpendingExtension` depend on the current method context to expose fixture objects safely
- `CookieStoreExtension` depends on thread-local cookie storage to make cleanup narrow and parallel-safe
- `DatabasesExtension` depends on `Connections.closeAllConnections()` to clean shared DB infrastructure at suite end
- `UsersV2RestTest` combines `@Isolated` with `TruncatedDatabasesExtension`, showing that full-database cleanup is treated as incompatible with normal concurrent execution

Evidence:

- `niffler-e-2-e-tests/src/test/resources/junit-platform.properties`
- `niffler-e-2-e-tests/src/test/resources/META-INF/services/org.junit.jupiter.api.extension.Extension`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/annotation/meta/WebTest.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/annotation/meta/RestTest.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/ApiLoginExtension.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/DatabasesExtension.java`

## Typical flow

Typical concurrent test flow in this project:

1. JUnit schedules methods and classes concurrently using the fixed pool from `junit-platform.properties`.
2. `TestMethodContextExtension` stores the current method context in a thread-local holder.
3. Meta-annotation extensions create fixtures for that test only.
4. `UserExtension` creates a fresh user and stores it in the method-scoped extension store.
5. `ApiLoginExtension` logs that user in, stores token/code in the same method context, and uses `ThreadSafeCookieStore` for request cookies.
6. If the test is web-based, `BrowserExtension` configures or starts a browser session for that thread and later closes it.
7. DB-backed helper code uses thread-specific connections or entity managers while reusing shared factories underneath.
8. After test execution, `CookieStoreExtension` clears cookies and `BrowserExtension` closes the driver.
9. At full-suite end, `DatabasesExtension` closes remaining shared DB connections.

Special-case flow for non-parallel-safe scenarios:

1. A class marks itself `@Isolated`.
2. `TruncatedDatabasesExtension` truncates shared databases before the class and after each test method.
3. JUnit runs that class outside the normal concurrent mix.

## Parallelization model

- Runner level
  JUnit Jupiter parallel mode is enabled globally.
- Class level
  Classes default to concurrent execution.
- Method level
  Methods default to concurrent execution.
- Pool level
  The suite uses a fixed worker pool of `3`.
- Browser level
  There is no visible browser-session pool abstraction. Isolation is per test/thread via WebDriver lifecycle, not via shared browser leasing.
- Environment level
  There is no visible sharding across containers, machines, or CI matrix jobs in the test module itself.
- Escape hatch level
  `@Isolated` is used when class-level concurrency would be unsafe.

This means the project favors one local concurrency model:

- a modest number of worker threads
- broad concurrent eligibility by default
- explicit isolation for exceptional cases

## State management model

The framework uses a hybrid model:

- immutable or shareable heavy infrastructure is cached globally
- mutable runtime state is scoped per thread or per test

Per-thread or per-test state:

- `ExtensionContext` reference in `TestMethodContextExtension`
- API login token and auth code in `ApiLoginExtension`
- generated `UserJson`, `CategoryJson[]`, and `SpendJson[]` in extension stores
- cookie jar in `ThreadSafeCookieStore`
- JDBC connections in `JdbcConnectionHolder`
- JPA `EntityManager` in `ThreadSafeEntityManager`
- current browser session through the test’s active WebDriver lifecycle

Shared state with concurrency control:

- data source cache in `DataSources` via `ConcurrentHashMap`
- entity-manager-factory cache in `EntityManagers` via `ConcurrentHashMap`
- legacy user queues in `UsersQueueExtension` via `ConcurrentLinkedQueue`

This is the core architectural idea: share factories, not sessions.

## Isolation strategy

Browser/session isolation:

- `BrowserExtension` closes the WebDriver after each test
- remote browser mode in docker still remains per test rather than shared mutable browser state
- Selenide/WebDriver access is effectively thread-bound during execution

Auth/session isolation:

- request cookies live in `ThreadSafeCookieStore`, which is thread-local
- `CookieStoreExtension` clears those cookies after test execution
- bearer tokens and OAuth-like codes are stored in the current method context, not in static global fields

Fixture isolation:

- generated users, categories, and spendings are attached to the current test context
- extensions resolve parameters from the current method’s store
- many tests create their own fresh user graph instead of reusing a shared account

DB isolation:

- connections are thread-scoped
- entity managers are thread-scoped
- some tests still require coarse shared reset through truncation, which is why `@Isolated` appears

Limits of the isolation model:

- category names and usernames are randomized, but not visibly guaranteed by deterministic unique IDs
- broad DB truncation is inherently hostile to unrelated concurrent tests
- the suite still contains a deprecated shared-resource extension (`UsersQueueExtension`), which suggests older patterns were less fully isolated

## Data strategy for parallel runs

The main data strategy is fresh fixture generation per test.

- `UserExtension` creates a new user when `@User` does not specify a fixed username
- `UsersApiClient` and `UsersDbClient` create related users by repeatedly calling `randomUsername()`
- `CategoryExtension` generates category names with `randomCategoryName()` when the annotation does not provide one
- ad hoc search-negative tests use `UUID.randomUUID()` for guaranteed-miss values

What this achieves:

- most concurrent tests do not compete over a preallocated shared account
- friend/invitation/category/spending graphs are usually attached to one generated root user
- test data ownership is mostly local to the running test

What it does not fully guarantee:

- `Faker`-based random names are probabilistically unique, not structurally unique
- explicit usernames or category names supplied in annotations can still create collisions if reused carelessly
- truncation-based cleanup is safe only when the affected tests are isolated from the rest of the suite

For adaptation purposes, the transferable lesson is:

- prefer generated per-test identities
- do not rely on a tiny shared pool of test accounts
- use deterministic uniqueness when collisions would be costly

## Patterns used

- JUnit-first concurrency
  Parallelism is configured centrally in JUnit rather than implemented as custom threading in tests.
- thread-local context gateway
  `TestMethodContextExtension` gives static helper methods access to the current test without sharing state across workers.
- extension-store scoping
  Mutable fixture objects are stored in `ExtensionContext.Store` keyed by the current method context.
- share factories, isolate handles
  Expensive DB factories are cached globally, while live connections and entity managers are thread-bound.
- generated fixture ownership
  Most tests create their own user graph instead of leasing from a global shared fixture pool.
- explicit concurrency opt-out
  `@Isolated` is used when cleanup or shared-state mutation would break concurrent safety.
- lifecycle cleanup hooks
  Cookie stores, browsers, and DB connections are cleared at defined lifecycle points.

## What is essential

- one explicit place where parallelism is enabled
- per-test or per-thread storage for mutable auth and fixture state
- browser/session cleanup after each test
- unique-enough fixture generation so concurrent tests rarely touch the same records
- thread-safe DB handle management if the framework seeds or verifies through the database
- an explicit way to mark non-parallel-safe tests as isolated

## What is optional

- a fixed pool of exactly `3`
  Useful here, but the exact number is tuning rather than architecture.
- thread-local wrapper classes around every mutable integration object
  Valuable when the framework is large, but smaller projects may need only cookies and DB handles.
- suite-level DB cleanup extension
  Helpful for this project’s support stack, but not required if the adapted project keeps DB access simpler.
- deprecated queue-based shared test-user leasing
  Concurrency-aware, but no longer the preferred pattern.
- visual screenshot support participating in the same per-test context model
  Nice consistency, not a requirement for parallel execution itself.

## What looks overengineered

- the combination of parallel JUnit, thread-local context indirection, thread-local cookie store, thread-local entity manager, thread-bound JDBC holder, and deprecated user queues
  It works, but it is more infrastructure than a smaller training project usually needs.
- probabilistic uniqueness via `Faker`
  Convenient, but weaker than deterministic unique IDs when concurrency increases.
- full database truncation inside a suite that otherwise runs concurrently
  The project mitigates this with `@Isolated`, but the need for that escape hatch shows the cleanup model is still fairly heavy.
- legacy pooled-user approach in `UsersQueueExtension`
  A concurrent queue of reusable static users is harder to reason about than just generating fresh users per test.

## Adaptation for my project

- start with JUnit parallel mode and a small fixed pool
- make mutable test state method-scoped first
  Tokens, current user, and generated fixtures should belong to the current test, not to a singleton service
- use thread-local storage only where libraries force shared entry points
  Cookie jars and DB handles are good candidates
- prefer generated fixtures over reusable shared accounts
- choose deterministic uniqueness for usernames and categories if the target system has strict unique constraints
- isolate destructive cleanup tests explicitly instead of trying to make global truncation coexist with normal concurrent tests
- keep the browser model simple
  One driver per test is easier to reason about than shared-session reuse
- avoid copying the deprecated queue-of-static-users pattern into a new project

## Readiness criteria

- parallel execution is enabled intentionally and documented
- mutable session and fixture state is not stored in shared unsynchronized globals
- concurrent tests do not share cookie jars, live DB handles, or browser sessions
- most tests create their own data rather than depending on a tiny shared fixture pool
- non-parallel-safe tests are explicitly marked and separated
- cleanup logic does not accidentally wipe data needed by unrelated concurrent tests
- the team can explain which objects are shared by design and which are isolated per thread/test

## Open questions

- Hypothesis: the suite relies on Selenide’s normal thread-bound WebDriver model, but there is no explicit custom WebDriver pool or resource-lock layer visible in the test module.
- `RandomDataUtils` uses a shared static `Faker`. It likely works in practice, but from the visible code it is not fully clear whether the team ever saw concurrency-related collisions or thread-safety issues there.
- The presence of `@Isolated` on `UsersV2RestTest` suggests there may be other hidden classes that also need broad shared-state protection, but only one clear example was visible in the scoped files.
- The suite-level DB infrastructure is parallel-aware, but it is not fully clear whether all repository implementations are equally exercised under concurrent load or whether one path is effectively the real default in day-to-day use.
