# Data generation

## Goal

Explain how `niffler-e-2-e-tests` creates, enriches, reuses, and occasionally resets test data so the same patterns can be adapted into another test framework without copying product-specific fixture content.

## Why it exists

Most tests in this module need more than one isolated record. They need a user plus related state such as:

- friends
- incoming or outgoing invitations
- categories
- spendings
- spends with specific dates or currencies

Instead of creating this state manually inside each test, the framework turns fixture intent into generated data through annotations and extensions.

Evidence:

- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/annotation/User.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/UserExtension.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/CategoryExtension.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/SpendingExtension.java`

## Main elements

- `RandomDataUtils`
  Central random-value helper based on Java Faker for usernames, names, category names, and free-text sentences.
- `@User`, `@Category`, `@Spending`
  Declarative fixture DSL used directly in tests.
- `UserExtension`
  Creates the root user fixture and linked people such as friends or invitations.
- `CategoryExtension`
  Creates category fixtures, including archived categories.
- `SpendingExtension`
  Creates spend fixtures and resolves category references for them.
- `UsersClient` and `SpendClient`
  Abstraction layer over data generation backends.
- `UsersApiClient` / `SpendApiClient`
  API-based fixture creation path.
- `UsersDbClient` / `SpendDbClient`
  DB-based fixture creation path.
- `TestData`
  Aggregates generated fixture state so tests can inspect the created data after setup.
- cleanup/reset helpers
  `TruncatedDatabasesExtension` and `sql/truncate_*.sql` provide explicit dataset reset for a subset of tests.
- legacy static fixture pool
  `UsersQueueExtension` contains predefined reusable users and appears to be a deprecated older approach.

## Internal structure

The data generation model is layered.

1. Declarative fixture request
   Tests describe desired state with annotations such as:
   - `@User`
   - `@User(friends = 1, incomeInvitations = 2)`
   - `@User(categories = {...}, spendings = {...})`

2. Root fixture creation
   `UserExtension` creates the main user and related people, then stores the result as `UserJson` with attached `TestData`.

3. Dependent fixture enrichment
   `CategoryExtension` and `SpendingExtension` inspect the same `@User` annotation and add more data to the user context or to extension-local storage.

4. Backend execution
   Actual creation is delegated to `UsersClient` and `SpendClient`, which choose API-based or DB-based generation depending on runtime strategy.

5. Optional dataset reset
   Some tests opt into broad database cleanup using `TruncatedDatabasesExtension` plus SQL scripts.

This structure means data generation is centered around one composite test subject, usually a generated user, and then expanded outward rather than assembled from many unrelated factories.

## Dependencies

Data generation depends on several surrounding layers:

- JUnit extensions trigger fixture generation before test execution
- configuration selects generation strategies and database endpoints
- service clients encapsulate transport-specific or persistence-specific creation
- repository and DAO layers back the DB generation path
- model classes carry generated state into the test body
- utility classes provide randomness and date shifting

Important dependencies:

- `UserExtension` depends on `UsersClient` and `RandomDataUtils`
- `CategoryExtension` and `SpendingExtension` depend on `SpendClient`
- `SpendingExtension` depends on categories being available first, either from `TestData` or `CategoryExtension`
- `UsersDbClient` depends on auth and userdata repositories plus transaction templates
- `SpendDbClient` depends on `SpendRepository`
- `DateUtils.addDaysToDate(...)` supports date-relative spend generation for time-window tests

## Typical flow

A typical data-generation flow in this project is:

1. A test declares fixture needs through `@User`, optionally with nested `@Category` and `@Spending`.
2. `UserExtension` generates a username when needed, creates the primary user, then creates related users such as friends or invitations.
3. The generated root user is wrapped with `TestData`, which stores references to the created related records.
4. `CategoryExtension` creates any categories declared in the same `@User` annotation.
   If the category is marked archived, it is first created and then updated to archived.
5. `SpendingExtension` creates spends declared in the same `@User` annotation.
   It first tries to match the spend’s category name against already created categories; if none exists, it creates an inline category reference.
6. The prepared `UserJson` and optional arrays of categories or spends are injected into the test.
7. Some tests perform full cleanup using truncate scripts before the class and after each method.

Concrete evidence:

- `SpendingTest.java` shows `@User` creating composite spend/category fixtures for web tests.
- `StatGraphQlTest.java` shows date-sensitive and currency-sensitive datasets declared through annotations.
- `UsersV2RestTest.java` shows relationship-heavy user datasets and adds `TruncatedDatabasesExtension` because ordering/pagination assertions need a very controlled dataset.

## Patterns used

- Annotation-driven fixture DSL
  The dominant pattern is declarative fixture definition directly on the test method.
- Composite root fixture
  Everything is anchored on a main user rather than on independent factories.
- Fixture enrichment pipeline
  User creation happens first, then categories, then spends.
- Strategy abstraction for generation backend
  The same fixture request can be materialized through APIs or direct DB writes.
- Randomized uniqueness
  Faker-generated usernames and text reduce collisions and avoid hardcoded fixture identifiers in most tests.
- Metadata attached to generated objects
  `TestData` stores generated side objects so tests can assert against them without rediscovering them from the system.
- Relative-date data generation
  Spend fixtures use date offsets rather than fixed dates, which keeps time-window tests stable.
- Explicit dataset reset for high-control cases
  A minority of tests use full DB truncation when annotation-level fixture creation is not enough to guarantee isolation.

## What is essential

- One small fixture DSL
  `@User` with a few parameters is the most valuable reusable idea here.
- One root fixture extension
  A single extension that creates the primary user and stores it for later use is enough to unlock the rest.
- Random uniqueness helpers
  A few utility methods for usernames, names, and text are practical and low-cost.
- A data carrier for generated state
  `TestData` is useful because tests often need to know exactly which related users or categories were created.
- One backend for actual fixture creation
  Either API-based or DB-based creation is enough for a minimal adapted project.

## What is optional

- Separate category and spend fixture layers
  Useful when many tests need those entities, but not mandatory in a smaller suite.
- Dual backend support
  Supporting both API and DB generation is convenient, but optional.
- Full-database truncate scripts
  Helpful for strongly isolated dataset tests, but not required if fixtures are cheap and local enough.
- Rich text and name randomization
  Nice for readability and UI coverage, but simpler UUID-based helpers could work.
- Deprecated static-user pools
  `UsersQueueExtension` is not part of the main generation path anymore.

## What looks overengineered

- Two complete fixture backends
  The ability to generate through both APIs and direct DB writes adds flexibility but also duplicates creation logic.
- Deep persistence stack behind DB-backed generation
  Repositories, DAOs, entities, JDBC, JPA, and XA transaction templates are a lot of machinery for test-data setup.
- Composite fixture logic spread across multiple cooperating extensions
  This is powerful, but it creates implicit ordering and coupling between user, category, and spending setup.
- Legacy static-user pooling still present
  `UsersQueueExtension` uses hardcoded reusable users and queue leasing, which looks like an older strategy that is more complex and less flexible than the current random-generation model.
- Full truncate scripts for selected tests
  This is practical for isolation, but broad deletion across several databases is heavy for a smaller training project.

## Adaptation for my project

- Start with one root fixture annotation and one extension.
  `@User` is the best idea to copy conceptually.
- Pick one creation backend.
  For a smaller project, choose API-based setup or DB-based setup, not both.
- Keep random generators small and purposeful.
  A few helpers for unique usernames and readable text are enough.
- Store generated side effects in one attached object.
  A simplified `TestData`-style carrier makes tests easier to read and debug.
- Add dependent fixture generators only when repeated patterns justify them.
  Categories and spendings are worth separate layers here because many tests depend on them.
- Use full database reset only for edge cases that truly need a clean universe.
  Prefer local fixture creation over broad truncation by default.
- Do not copy product-specific example values blindly.
  The reusable part is the pattern of expressing fixture relationships, dates, and archived state, not the exact sample categories or descriptions.

## Readiness criteria

- Tests can declare needed fixtures in one line instead of manually calling setup code.
- Generated data is unique enough to avoid routine collisions.
- The framework can create a root subject plus a small set of related records deterministically.
- Tests can inspect generated fixture metadata without extra queries.
- One clear cleanup strategy exists for exceptional cases.
- The team can explain whether fixture generation goes through the public system surface or through direct privileged access.

## Open questions

- Hypothesis: the default DB-backed creation path may be the main one in practice because the configuration factories default to DB-oriented implementations unless explicitly switched.
- The current `UserExtension` only auto-creates a user when `@User.username()` is empty. If a fixed username is provided without preexisting state, the intended behavior is not fully obvious from the extension alone and may rely on test-specific assumptions.
- `UsersQueueExtension` is deprecated and unused in mainline tests, but it suggests there was an older preference for leased static fixtures. It is not fully clear whether any external workflow still depends on that pattern.
