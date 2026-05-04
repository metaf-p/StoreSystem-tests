# Test architecture overview

## Goal

Provide one practical map of the `niffler-e-2-e-tests` architecture so the extracted documents can be read as one system and adapted into another project without copying unnecessary complexity.

## Scope

This overview covers only:

- the test framework architecture of `niffler-e-2-e-tests`
- the CI/CD pieces that directly run it
- the monorepo context that matters to understanding that test architecture

It does not describe the architecture of the whole repository.

## How to read this document set

Read the reference architecture in three layers.

### 1. Core block documents

These describe the main architectural blocks:

- `01-project-structure.md`
- `02-test-foundation.md`
- `03-configuration.md`
- `04-data-generation.md`
- `05-api-layer-adaptation.md`
- `06-db-layer.md`
- `07-ui-layer.md`
- `08-ci-cd.md`
- `09-parallel-execution.md`
- `10-assertion-layer.md`

### 2. Supplementary reference notes

These sharpen important aspects that are cross-cutting, implicit, historical, or not fully visible in the first block set:

- `11-api-layer-current-state.md`
- `12-auth-session-model.md`
- `13-runtime-core.md`
- `14-protocol-adapters.md`
- `15-model-layer.md`
- `16-build-generation-toolchain.md`
- `17-test-organization-and-naming.md`
- `18-legacy-and-experimental-mechanisms.md`
- `19-framework-evolution.md`
- `20-monorepo-integration-boundary.md`

### 3. Adaptation documents outside this folder

These define the recommended target shape for a new project:

- `docs/architecture-map.md`
- `docs/implementation-roadmap.md`

Important reading rule:

- the `reference-architecture/*` documents explain the current reference project
- `architecture-map.md` and `implementation-roadmap.md` explain how a new project should be built

Those are related, but they are not the same thing.

## Main architectural blocks

### 1. Project structure

Purpose:

- organize one dedicated test-only module around protocol-specific suites and shared framework layers

Primary document:

- `01-project-structure.md`

### 2. Test foundation

Purpose:

- provide the shared execution model for web, REST, GraphQL, gRPC, and SOAP tests

Primary document:

- `02-test-foundation.md`

### 3. Configuration

Purpose:

- select runtime environment and expose core test-facing settings

Primary document:

- `03-configuration.md`

### 4. Data generation

Purpose:

- give tests declarative and reusable fixture setup

Primary document:

- `04-data-generation.md`

### 5. API-facing adapter layer

Purpose:

- abstract direct interaction with the system through non-UI interfaces

Primary documents:

- `05-api-layer-adaptation.md`
- `11-api-layer-current-state.md`

Reading note:

- `05-api-layer-adaptation.md` describes the recommended API architecture for a new project
- `11-api-layer-current-state.md` describes how the current reference project actually organizes its API-facing runtime

### 6. DB layer

Purpose:

- provide privileged setup, lookup, and reset capabilities

Primary document:

- `06-db-layer.md`

### 7. UI layer

Purpose:

- keep web tests readable and stable

Primary document:

- `07-ui-layer.md`

### 8. CI/CD

Purpose:

- run the test module in a repeatable environment and return useful diagnostics

Primary document:

- `08-ci-cd.md`

### 9. Parallel execution

Purpose:

- shorten feedback time without letting test state leak across concurrent runs

Primary document:

- `09-parallel-execution.md`

### 10. Assertion layer

Purpose:

- distribute assertions across tests, transport helpers, pages, components, and custom conditions without requiring a heavy standalone DSL

Primary document:

- `10-assertion-layer.md`

## Cross-cutting and support concerns

Some important aspects of the current framework are not block-local.

They cut across several architectural blocks and should be read separately.

### Auth and session model

Relevant document:

- `12-auth-session-model.md`

Touches:

- test foundation
- configuration
- API-facing layer
- UI layer
- parallel execution

### Shared runtime core

Relevant document:

- `13-runtime-core.md`

Touches:

- API-facing layer
- protocol adapters
- auth/session handling
- support-service clients
- parallel execution

### Protocol-specific adapters

Relevant document:

- `14-protocol-adapters.md`

Touches:

- API-facing layer
- test foundation
- build/generation tooling

### Shared model layer

Relevant document:

- `15-model-layer.md`

Touches:

- data generation
- API-facing layer
- DB layer
- UI layer
- assertion layer

### Build and generation toolchain

Relevant document:

- `16-build-generation-toolchain.md`

Touches:

- API-facing layer
- protocol adapters
- CI/CD

### Test organization and naming

Relevant document:

- `17-test-organization-and-naming.md`

Touches:

- project structure
- test ergonomics
- protocol packaging

### Legacy and experimental mechanisms

Relevant document:

- `18-legacy-and-experimental-mechanisms.md`

Touches:

- test foundation
- parallel execution
- exploratory test paths

### Framework evolution

Relevant document:

- `19-framework-evolution.md`

Use this document to distinguish:

- enduring architectural ideas
- historical growth artifacts
- features that should not automatically be copied in the same order

### Monorepo integration boundary

Relevant document:

- `20-monorepo-integration-boundary.md`

Use this document to understand:

- which surrounding modules matter
- which shared contracts matter
- where to stop analysis

## Responsibility map

The architecture can be read as a layered test platform.

1. Entry layer: `test/*`
   Protocol-specific tests express scenario intent.

2. Orchestration layer: `jupiter/*`
   Meta-annotations and extensions translate declarative test intent into runtime behavior.

3. Adapter layer: `page/*`, `service/*`, `api/*`, protocol base classes
   These layers hide browser, HTTP, SOAP, GraphQL, gRPC, and login details from test bodies.

4. Fixture/setup internals: data-generation extensions plus DB-backed support
   These layers create users, relationships, categories, spendings, and selective resets.

5. Runtime support: `config/*`, `model/*`, generated sources, shared runtime helpers, resources
   These provide environment selection, DTOs, generated artifacts, transport helpers, JUnit config, SQL scripts, and reporting wiring.

6. Delivery layer: CI/CD
   The workflow provides the runtime contract the test module expects in docker mode.

## Allowed dependencies between blocks

The extracted architecture suggests these dependency directions are healthy:

- `test/*` may depend on:
  - test foundation
  - UI layer
  - API-facing adapter layer
  - model

- test foundation may depend on:
  - configuration
  - data generation
  - UI layer for browser or login bootstrap
  - API-facing adapter layer
  - DB layer indirectly through fixture services

- data generation may depend on:
  - configuration
  - API-facing adapter layer
  - DB layer
  - model

- UI layer may depend on:
  - configuration
  - test foundation extensions
  - model

- API-facing adapter layer may depend on:
  - configuration
  - model
  - selected low-level transport helpers
  - protocol-specific generated or shared contracts where required

- DB layer may depend on:
  - configuration
  - resources for SQL, JPA, JNDI, and logging
  - model and entities needed for setup operations

- CI/CD may depend on:
  - module runtime contract
  - docker packaging
  - reporting and artifact outputs

Dependencies that should be avoided in an adapted version:

- UI pages depending directly on DB internals
- test classes depending directly on repository or transaction classes
- configuration depending on higher-level test logic
- CI/CD containing business-specific test behavior that should live in the module itself

## Typical end-to-end flow through the blocks

A typical scenario crosses the architecture like this:

1. A test class selects a family such as `@WebTest`, `@RestTest`, `@GqlTest`, `@GrpcTest`, or `@SoapTest`.
2. The test foundation activates the correct extension set.
3. Configuration selects local or docker mode and any implementation strategy.
4. Data-generation extensions inspect annotations such as `@User`, `@Category`, and `@Spending`.
5. Fixture services create state through API-backed or DB-backed support paths.
6. The test receives prepared objects through parameter injection or protocol-specific base setup.
7. The test interacts through the UI layer or through API or protocol clients.
8. Parallel-execution safeguards isolate cookies, method state, browser sessions, and DB handles.
9. Assertion logic stays close to the appropriate layer:
   - tests for business meaning
   - transport helpers for technical response correctness
   - pages and components for UI checks
10. Reporting and cleanup hooks attach evidence and clear mutable state.
11. In CI, the dockerized runtime and artifact or report publication wrap the whole run.

## Architectural shape

At a high level, `niffler-e-2-e-tests` is not just:

- a page-object suite
- or an API test suite
- or a DB-backed test utility library

It is a hybrid test platform with several strong centers:

- JUnit extension orchestration
- privileged fixture generation
- a shared runtime core
- multiple protocol adapters in one module

That combination makes the suite flexible and expressive, but also explains most of the complexity.

## What is essential for adaptation

The highest-value ideas to preserve are:

- a dedicated test module or clearly isolated test root
- meta-annotations for test families
- extension-based fixture creation instead of manual setup in tests
- one small configuration boundary
- one explicit adapter layer per interaction style
- per-test or per-thread isolation for mutable runtime state
- one repeatable CI path that matches the test module’s real runtime expectations
- explicit awareness of which parts are core and which are historical or optional growth artifacts

## What can be postponed

These parts are useful but not necessary on day one:

- screenshot-based visual comparison
- hosted Allure publishing
- backend-log attachment into reports
- separate protocol generation flows if those protocols are not immediate priorities
- DB-backed fixture creation if API-based setup is already sufficient
- advanced parallel tuning beyond a small fixed pool
- operational or experimental mechanisms such as issue-based disabling

## What looks too specific or overengineered

The reference architecture contains several areas that are powerful but heavy for a smaller adaptation:

- multiple fixture backends at once
- multiple repository styles at once
- deep DB infrastructure inside the test module
- rich reporting surface
- broad protocol coverage in one module
- concurrency support layered with several thread-local helpers plus isolated exceptions
- historical or experimental mechanisms retained alongside the preferred model

These are useful reference points, but not all of them are good defaults for a training project.

## Recommended minimal adaptation shape

A smaller but faithful adaptation would likely keep:

- one test foundation block
- one configuration block
- one fixture-generation block
- one primary interaction block first
  API-first or UI-first, but not every protocol at once
- optional DB support only if privileged setup is truly needed
- one simple CI workflow
- basic parallel safety if concurrent execution is enabled

Then it can grow outward in layers.

## Read this architecture as

The cleanest way to understand this test project is:

- tests describe intent
- the foundation interprets that intent
- fixture layers create the required state
- adapter layers interact with the system
- the runtime core supports those interactions
- config selects the environment
- parallel and cleanup layers prevent cross-test leakage
- CI provides the environment and reporting shell
- supplementary notes explain the cross-cutting, historical, and non-mainline details

## Open points

- The current document set is now broader than the original block list and should be read as:
  - core block documents
  - supplementary cross-cutting notes
  - adaptation documents for the new project
- The main adaptation challenge is no longer missing documentation, but choosing which parts of the reference framework should remain central in a new project.
