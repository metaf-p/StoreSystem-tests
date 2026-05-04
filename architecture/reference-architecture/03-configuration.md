# Configuration

## Goal

Describe how configuration is organized for `niffler-e-2-e-tests`, how it is selected at runtime, and which configuration patterns are worth adapting or simplifying in another test project.

## Why it exists

The test module talks to the system through several channels at once:

- browser UI
- REST and GraphQL HTTP endpoints
- gRPC endpoints
- SOAP endpoints
- direct database access
- reporting infrastructure

Because of that, the framework needs configuration for environment endpoints, database connectivity, execution mode, screenshot baselines, reporting destinations, and implementation strategy switches.

The project solves this mostly through code-based configuration and JVM system properties rather than through YAML or `.properties` business-style application config.

Evidence:

- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/config/*`
- `niffler-e-2-e-tests/build.gradle`
- `niffler-e-2-e-tests/src/test/resources/*`

## Main elements

- `config/Config.java`
  Central configuration contract used across pages, services, data access, and extensions.
- `config/LocalConfig.java` and `config/DockerConfig.java`
  Two built-in runtime environments selected by the `test.env` JVM property.
- JVM system properties
  Used to choose environment and strategy implementations, especially `test.env`, `client.impl`, and `repository.impl`.
- environment variables
  Used mainly for CI/reporting integrations, especially Allure and GitHub-related data.
- test resources
  Resource files provide execution tuning and infrastructure wiring:
  - `junit-platform.properties`
  - `jndi.properties`
  - `spy.properties`
  - `logback.xml`
  - `META-INF/persistence.xml`
- build-time generation settings
  `build.gradle` derives GraphQL introspection and SOAP WSDL locations from the active test environment.
- CI wiring
  `.github/workflows/e2e.yaml` provides environment variables and file-system preparation that the test module expects in docker runs.

## Internal structure

Configuration is split into three layers.

1. Runtime environment selection
   `Config.getInstance()` checks `System.getProperty("test.env")` and returns either `DockerConfig` or `LocalConfig`.

2. Strategy selection inside the test framework
   Some components choose their implementation by JVM property rather than by environment:
   - `UsersClient.getInstance()` and `SpendClient.getInstance()` switch between API-backed and DB-backed setup using `client.impl`
   - repository factories switch between `jdbc`, `spring-jdbc`, and default JPA using `repository.impl`

3. Infrastructure-specific resource configuration
   Resource files configure supporting runtimes:
   - JUnit parallelism in `junit-platform.properties`
   - JNDI bootstrap in `jndi.properties`
   - SQL spy logging in `spy.properties`
   - logger levels in `logback.xml`
   - JPA persistence units in `META-INF/persistence.xml`

This means configuration is not centralized in one file format. Instead, it is distributed by concern:

- endpoint and mode configuration in Java code
- execution behavior in JUnit resource properties
- DB/JPA support wiring in JNDI and persistence resources
- observability/reporting settings in logging and CI env vars

## Dependencies

Configuration is consumed broadly across the module:

- `page/*` uses `Config` for UI entry points
- `service/*` and `service/impl/*` use `Config` for HTTP, SOAP, and reporting endpoints
- `test/gql/BaseGraphQlTest.java` and `test/grpc/BaseGrpcTest.java` use `Config` for protocol client targets
- `data/*` uses `Config` for JDBC URLs and DB credentials
- `jupiter/extension/*` uses both `Config` and raw system properties for browser mode, screenshot directories, and reporting behavior

Important dependencies:

- `BrowserExtension.java` reads `test.env` directly to decide whether to enable remote browser execution
- `DataSources.java` uses `Config` plus `jndi.properties` conventions to create and bind data sources
- `EntityManagers.java` depends on `META-INF/persistence.xml` naming conventions that align persistence-unit names with derived database identifiers
- `AllureDockerExtension.java` depends on both `Config` and CI-provided environment variables for report publishing metadata

External configuration dependencies that matter architecturally:

- JUnit Platform resource properties
- JPA persistence-unit declarations
- P6Spy configuration
- Logback configuration
- GitHub Actions environment injection for docker-based CI runs

## Typical flow

Typical configuration flow in this project looks like this:

1. Tests start with JVM system properties provided by Gradle, Docker, or CI.
2. `Config.getInstance()` selects the active environment object.
3. Pages, clients, extensions, and data access helpers call `Config.getInstance()` and read the endpoints or paths they need.
4. Strategy factories also inspect JVM properties to decide whether test data is created through APIs or directly in databases, and whether repositories use JDBC, Spring JDBC, or JPA.
5. Resource-based configuration is picked up automatically by the supporting libraries:
   - JUnit reads `junit-platform.properties`
   - logging reads `logback.xml`
   - JPA reads `persistence.xml`
   - P6Spy reads `spy.properties`
6. In CI docker runs, the workflow injects environment variables used by the Allure reporting path and prepares directories expected by screenshot and log collectors.

Evidence:

- `Config.java` selects the environment object from `test.env`
- `UsersClient.java`, `SpendClient.java`, and repository interfaces switch implementation based on JVM properties
- `build.gradle` derives generated-source endpoints from `test.env`
- `.github/workflows/e2e.yaml` exports reporting env vars and prepares screenshot/log directories

## Patterns used

- Code-first environment configuration
  Instead of external application config files, endpoint and path decisions live in Java enums implementing one interface.
- Small environment matrix
  The framework has only two main environment variants: local and docker.
- Strategy-by-property
  Implementation choices are controlled by JVM properties, independent of environment.
- Convention-based resource wiring
  Persistence units, JNDI names, and JDBC URL-derived identifiers are expected to line up by naming convention.
- Mixed configuration sources
  The project combines:
  - Java code for environment endpoints
  - resource files for supporting libraries
  - environment variables for CI/reporting metadata
  - Gradle logic for generated-source setup

## What is essential

- One explicit environment abstraction
  A small `Config` interface with environment implementations is a strong and understandable core.
- A narrow set of runtime switches
  Selecting environment and perhaps one fixture strategy is enough for most adapted projects.
- Resource-based library configuration where the library expects it
  JUnit, logging, and JPA support benefit from standard resource files.
- A clear access pattern
  Most framework layers obtain configuration the same way: through `Config.getInstance()`.
- CI to test-module handoff for runtime metadata
  If reports or artifacts depend on CI context, that contract should be explicit.

## What is optional

- Separate fixture strategy switches like `client.impl`
  Useful when the framework supports both API-seeded and DB-seeded test data, but not required in a smaller project.
- Multiple repository strategy options like `repository.impl`
  Valuable only if the project intentionally compares or supports several persistence styles.
- Dynamic report publishing metadata from CI
  Helpful for hosted Allure integration, but not essential for a minimal learning project.
- JNDI plus JPA persistence-unit wiring in a test module
  Useful only if the test framework directly owns transactional database setup through JPA.
- Build-time GraphQL schema introspection and SOAP generation keyed off test environment
  Necessary only if those protocol clients are part of the test architecture.

## What looks overengineered

- Too many strategy dimensions
  The module can vary environment, fixture transport, repository style, browser mode, and reporting mode. For a smaller project, that is more variability than necessary.
- Hardcoded infrastructure values inside Java config classes
  This is workable for a controlled training repo, but not ideal if environments grow or change frequently.
- Configuration spread across many mechanisms
  The project uses Java enums, resource files, system properties, environment variables, Gradle generation settings, and CI workflow env injection. Each piece makes sense locally, but together they increase cognitive load.
- Test framework ownership of advanced DB infrastructure config
  JNDI binding, Atomikos data sources, persistence-unit setup, and p6spy integration are powerful, but heavy for a smaller adapted suite.
- CI/reporting coupling in the test module
  `AllureDockerExtension` depends on CI-exported metadata such as execution source and commit message, which makes the reporting path less portable.

## Adaptation for my project

- Keep one small configuration interface and two environment implementations at most.
  That is the most reusable idea from this project.
- Prefer externalized values over many hardcoded endpoints if your environments change often.
  Even if you keep code-based selection, the surface should stay small.
- Start with only one strategy switch, if any.
  For example, choose either API-based or DB-based fixture creation first, not both.
- Keep resource files only for libraries that naturally use them.
  `junit-platform.properties` and logging config are normal; JNDI and JPA test infrastructure may be unnecessary in a smaller suite.
- Keep CI-injected values limited to true runtime metadata or secrets.
  Avoid making the local developer experience depend on many CI-only variables.
- If your test project does not need GraphQL schema generation or SOAP client generation, do not copy those build-time configuration paths.

## Readiness criteria

- The project has one clear way to choose the active test environment.
- Pages, clients, and setup helpers all read configuration through a small, consistent interface.
- Optional strategy switches are few and clearly documented.
- Supporting resource files exist only where required by the underlying libraries.
- CI provides only the environment metadata the test module actually consumes.
- A new contributor can answer:
  - how local vs docker execution is selected
  - where endpoint configuration lives
  - which values are code constants vs CI-provided env vars vs library resource files

## Open questions

- Hypothesis: the default DB credentials in `Config.java` are intended only for the controlled training environment. In a real adaptation, that part should likely be externalized rather than copied as-is.
- The project uses code-level environment values rather than generic external property files. That is simple here, but it is unclear how well it would scale beyond the current local/docker split.
- `GhApiClient` uses a GitHub token environment variable, while the issue-based disabling annotation appears to be optional and rarely central. In a smaller project, that integration may not justify its configuration surface.
