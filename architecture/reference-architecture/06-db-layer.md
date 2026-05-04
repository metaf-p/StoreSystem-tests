# DB layer

## Goal

Explain how `niffler-e-2-e-tests` uses the database as a privileged test-support layer for fixture setup, selective lookup, and full reset, and identify which DB capabilities are truly necessary in an adapted project.

## Why it exists

This test module does not treat the database as a hidden implementation detail. It uses direct DB access to support testing in three main ways:

- create users and related records faster than going through public flows
- create or mutate categories and spendings for controlled scenarios
- reset several databases to a known clean state for tests that require strict isolation

The DB layer is therefore part of the test framework, not just a debugging aid.

Evidence:

- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/data/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/service/impl/UsersDbClient.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/service/impl/SpendDbClient.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/TruncatedDatabasesExtension.java`

## Main elements

- `data/repository/*`
  Main DB-facing abstraction layer used by DB-backed support clients.
- `data/repository/impl/*`
  Three implementation families for the same repository contracts:
  - plain JDBC
  - Spring JDBC
  - Hibernate/JPA
- `data/dao/*` and `data/dao/impl/*`
  Lower-level DAO helpers used mainly by the Spring JDBC repository implementations.
- `data/entity/*`
  Persistence entities for auth, userdata, and spend-related tables.
- `data/jdbc/*`
  Data source, connection, and holder utilities for thread-scoped DB access.
- `data/jpa/*`
  Entity manager bootstrap and thread-safe wrapping for JPA-based repositories.
- `data/tpl/*`
  Transaction helpers:
  - `JdbcTransactionTemplate`
  - `XaTransactionTemplate`
- `data/logging/*`
  SQL-to-Allure reporting integration using p6spy.
- SQL cleanup resources
  `src/test/resources/sql/truncate_*.sql`
- JPA/JNDI resources
  `src/test/resources/META-INF/persistence.xml`, `jndi.properties`, and `spy.properties`

## Internal structure

The DB layer is organized as a support stack rather than as a single access path.

1. Support clients consume repositories
   `UsersDbClient` and `SpendDbClient` are the primary entry points from the test framework into the DB layer.

2. Repositories define the test-facing persistence API
   `AuthUserRepository`, `UserdataUserRepository`, and `SpendRepository` expose operations that matter to test setup and validation.

3. Repositories can swap implementation style
   Each repository chooses `jdbc`, `spring-jdbc`, or default `jpa` based on `repository.impl`.

4. Infrastructure provides connection and transaction behavior
   JDBC-style implementations use `Connections`, `JdbcConnectionHolder`, and `JdbcTransactionTemplate`.
   JPA implementations use `EntityManagers`, `ThreadSafeEntityManager`, and JTA-backed persistence units.

5. Cleanup and observability sit alongside access
   Truncate scripts reset data, while p6spy and `AllureAppender` expose SQL activity in reports.

Architecturally, this means the DB layer is both:

- a fixture creation engine
- a mini persistence toolkit embedded inside the test module

## Dependencies

The DB layer depends on:

- `Config` for JDBC URLs and credentials
- transaction infrastructure from Atomikos and Spring
- JPA resource configuration in `persistence.xml`
- JNDI bootstrap from `jndi.properties`
- p6spy SQL interception from `spy.properties`

It is consumed by:

- `UsersDbClient` for auth and userdata seeding
- `SpendDbClient` for spend/category seeding and lookup
- `TruncatedDatabasesExtension` for full reset
- indirectly, the data-generation layer and JUnit fixture extensions that call those clients

Important dependency relationships:

- `DataSources` creates and caches XA-capable wrapped data sources, then binds them into JNDI names expected by JPA
- `EntityManagers` depends on `DataSources` and on persistence-unit naming conventions derived from JDBC URLs
- `XaTransactionTemplate` coordinates multi-database actions for cross-schema fixture setup
- repository factories depend on the `repository.impl` JVM property
- DB-backed service clients depend on repository contracts, not on concrete implementations

## Typical flow

A typical DB-backed fixture flow is:

1. A fixture extension calls `UsersDbClient` or `SpendDbClient`.
2. The client obtains repository implementations through repository factory methods.
3. The client wraps the operation in a transaction helper:
   - single-database operations use one JDBC or XA transaction path
   - cross-database user creation uses `XaTransactionTemplate` across auth and userdata databases
4. The repository implementation performs persistence work through JDBC, Spring JDBC, or JPA.
5. SQL is observable through p6spy and the custom Allure appender.
6. Connections are kept thread-local during the operation and cleaned later by connection holders or suite cleanup.

Typical reset flow:

1. `TruncatedDatabasesExtension` opens direct JDBC connections to the relevant test databases.
2. It executes `truncate_auth.sql`, `truncate_userdata.sql`, and `truncate_spend.sql`.
3. The test class starts from a broadly empty state.

Evidence:

- `UsersDbClient.java` shows cross-database user creation and relationship setup through repositories
- `SpendDbClient.java` shows spend/category CRUD and lookup through `SpendRepository`
- `TruncatedDatabasesExtension.java` shows explicit reset through SQL scripts

## Patterns used

- Repository abstraction over multiple persistence styles
  One repository contract can run with plain JDBC, Spring JDBC, or Hibernate/JPA.
- Privileged fixture setup
  The DB layer exists mainly to set up and inspect state for tests, not to model the whole product architecture.
- Thread-scoped connection/entity-manager handling
  `JdbcConnectionHolder` and `ThreadSafeEntityManager` isolate DB handles per thread.
- Explicit transaction templates
  Transaction boundaries are controlled by custom helper classes rather than being hidden completely behind frameworks.
- Cross-database transaction coordination
  `XaTransactionTemplate` supports test setup that spans auth and userdata databases.
- Resource-convention bootstrap
  JNDI names, persistence-unit names, and JDBC URL suffixes are aligned by convention.
- SQL observability as part of the test experience
  p6spy plus `AllureAppender` turns executed SQL into report attachments.

## What is essential

- One DB-backed support path for privileged setup, if your tests need it
  Many teams only need this for a small subset of fixtures.
- A small repository or gateway interface
  The support layer should hide raw SQL details from tests and extensions.
- Clear transaction handling
  Even a simple single-database transaction wrapper is important if setup writes several related rows.
- Simple reset capability
  For selected tests, a repeatable cleanup/reset path is valuable.
- Thread-safe access if tests run concurrently
  This project’s parallel execution makes per-thread DB state management necessary.

## What is optional

- Supporting three persistence styles at once
  Useful for experimentation or teaching, not necessary in most test frameworks.
- Full JPA layer inside the test module
  Helpful only if the team actively wants ORM-backed test support.
- JNDI bootstrap and JTA persistence units
  Advanced infrastructure that a smaller project can often avoid.
- SQL-to-Allure attachments
  Nice for debugging, but not essential for a minimal adapted suite.
- DAO layer beneath repositories
  Optional if repositories can stay thin enough without an extra indirection layer.

## What looks overengineered

- Three implementation families for every repository
  Maintaining JDBC, Spring JDBC, and Hibernate versions of the same persistence operations is a lot of surface area for a test module.
- Test-owned JTA/XA infrastructure
  Atomikos data sources, JNDI binding, JTA persistence units, and XA transaction templates are powerful, but heavy for a smaller training project.
- Deep layering under a support concern
  Repositories, DAOs, mappers, extractors, entities, transaction templates, and connection wrappers make the DB layer resemble a mini application backend.
- Broad DB reset through truncate scripts
  Effective for isolation, but coarse-grained and more invasive than many projects need.
- Thread-safe `EntityManager` wrapper
  Necessary here because of concurrency plus ORM use, but another sign that the DB support stack is more advanced than minimal test tooling.

## Adaptation for my project

- Start with one persistence style only.
  For most adapted projects, plain JDBC or Spring JDBC is enough.
- Build one thin DB support gateway per aggregate you need for fixtures.
  You do not need a full persistence architecture unless the test suite truly benefits from it.
- Add transaction helpers early, but keep them small.
  Single-database transaction wrappers are often enough at first.
- Use cross-database transactions only if your fixture setup genuinely spans multiple schemas and must stay atomic.
- Keep cleanup targeted.
  Prefer narrow cleanup or targeted deletion before broad multi-database truncation.
- Add SQL reporting only if debugging DB-backed setup is a recurring pain point.
- Do not copy the full JNDI/JPA/Atomikos stack into a smaller training project unless your main goal is to study those tools.

## Readiness criteria

- The team can explain why direct DB access exists in the test framework.
- Tests and extensions use DB support through a small stable interface, not raw SQL in test bodies.
- Transaction boundaries for setup operations are explicit and predictable.
- Parallel test execution does not corrupt shared DB connection state.
- A reset strategy exists for tests that require a clean DB baseline.
- The chosen persistence style is intentional rather than inherited accidentally from a more complex reference project.

## Open questions

- Hypothesis: the default `repository.impl` value of `jpa` means the Hibernate path may be the intended default, but it is not fully clear whether it is also the most used path in day-to-day practice.
- The coexistence of DAOs and repositories suggests some historical evolution in the DB layer. It is not fully obvious whether all lower-level DAOs are still equally important or whether some remain mainly to support the Spring JDBC branch.
- The current truncate approach resets auth, userdata, and spend data broadly. In another project, a more targeted reset model might be preferable, but it is unclear how often this suite truly requires full multi-database cleanup versus selective fixture deletion.
