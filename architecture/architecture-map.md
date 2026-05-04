# Architecture map

## Purpose

Define a practical, minimal architecture for a future test project.

The goal of this architecture is to keep tests readable, setup reusable, API access structured, and future growth possible without importing unnecessary framework weight too early.

## Architectural blocks

### 1. Test foundation

Purpose:

- provide the shared execution model for the test project
- act as an extension-first foundation with declarative test entry points

Responsibilities:

- define test-family entry points
- compose shared setup through extensions or equivalent lifecycle hooks
- provide fixture injection and cleanup
- centralize auth/bootstrap hooks when tests need them
- grow shared test behavior primarily through JUnit extensions and declarative test entry points

### 2. Configuration

Purpose:

- provide one clear way to read environment settings

Responsibilities:

- expose runtime URLs and core switches
- separate local and CI/runtime differences
- keep configuration access consistent across layers

### 3. Data generation

Purpose:

- provide reusable fixture creation for tests

Responsibilities:

- create root test entities
- attach related entities when needed
- keep generated test data discoverable by the test
- stay outside transport and UI mechanics

### 4. API layer

Purpose:

- provide the non-UI interaction layer for tests
- act as the primary API core for the framework

Responsibilities:

- define typed endpoint contracts
- provide reusable request and response defaults
- execute HTTP requests through a technical requester
- expose domain-oriented API clients
- isolate auth concerns from raw transport mechanics

### Cross-cutting auth and session model

Purpose:

- keep authentication and session state explicit across API, UI bootstrap, foundation, and parallel execution

Responsibilities:

- provide one clear auth path
- keep auth acquisition out of request specs and page objects
- keep session state reusable and safe for concurrent execution

### Supporting runtime core

Purpose:

- centralize shared technical runtime mechanics used by several blocks

Responsibilities:

- provide shared requester or base-client behavior
- support cookie or session handling
- support protocol-specific runtime helpers where needed
- keep technical execution concerns separate from scenario logic

### 5. UI layer

Purpose:

- provide browser-facing interaction only if UI testing is part of the project

Responsibilities:

- represent screens as pages
- represent repeated widgets as components
- keep browser/session setup outside page classes

### 6. Optional DB support

Purpose:

- support privileged setup or reset only when API-driven setup is not enough

Responsibilities:

- provide focused fixture/reset helpers
- hide direct DB mechanics behind a narrow support interface

### 7. CI/CD

Purpose:

- run the test project in a repeatable way and preserve useful diagnostics

Responsibilities:

- start the required runtime
- execute the tests
- publish the minimum useful failure evidence

### 8. Parallel execution support

Purpose:

- allow concurrent execution without cross-test leakage

Responsibilities:

- isolate mutable state per test or per thread
- keep auth/session state scoped
- make cleanup safe for concurrent runs

## Dependency rules

Allowed dependencies:

- tests may depend on:
  - test foundation
  - API layer
  - UI layer
  - data generation outputs

- test foundation may depend on:
  - configuration
  - data generation
  - API layer for auth/bootstrap helpers
  - UI layer for browser/bootstrap helpers

- data generation may depend on:
  - configuration
  - API layer
  - optional DB support
  - shared models

- API layer may depend on:
  - configuration
  - shared models
  - small API-specific data helpers
  - runtime core helpers

- UI layer may depend on:
  - configuration
  - test foundation hooks
  - shared models
  - auth/session bootstrap helpers

- optional DB support may depend on:
  - configuration
  - shared models
  - its own technical persistence helpers

- auth/session model may depend on:
  - configuration
  - API layer
  - test foundation
  - runtime core helpers

- runtime core may depend on:
  - configuration
  - shared models
  - technical transport libraries

- CI/CD may depend on:
  - the runtime contract exposed by the test project
  - test outputs such as reports or artifacts

Dependencies to avoid:

- tests assembling raw HTTP as the normal path
- tests depending directly on DB internals
- API requester depending on business flows or fixture generation
- request specs performing login
- UI pages depending on DB support
- configuration depending on higher-level test logic
- auth logic hiding inside page objects
- auth logic hiding inside request specs

## Initial baseline

Start with the smallest clean version of each block.

### Core blocks to include immediately

- test foundation
- configuration
- data generation
- API layer
- auth/session model
- supporting runtime core
- CI/CD

### UI baseline

For this project, browser tests are part of the immediate scope, so the baseline should include a minimal UI adapter.

If included, keep it minimal:

- one base page
- a few pages
- components only for repeated UI fragments
- browser/session bootstrap outside page classes
- one explicit API-to-UI auth bridge if UI tests require authenticated entry

### DB baseline

Do not include DB support by default.

Add it only if:

- API setup is insufficient
- reset/setup must happen through privileged access

### API baseline

The initial API layer should contain:

- `ApiConfig`
- `RequestSpecs`
- `ResponseSpecs`
- `Endpoint`
- `Endpoints`
- `ApiRequester`
- `BaseApiClient`
- `AuthClient`
- one first domain client
- minimal request/response/common DTOs
- one small API-oriented data helper

`ClientFactory` and `AuthProvider` may be included in very thin forms if multiple auth contexts already exist.

The initial supporting auth/runtime path should contain:

- one explicit auth client path
- one small auth/session state model if reusable auth is already needed
- one shared requester or base-client seam

This should be treated as the initial API core of the framework.

### Data baseline

The initial data-generation block should contain:

- one root fixture concept
- one small set of helpers for related data
- one simple way for tests to access generated fixture state

### CI baseline

The initial CI/CD block should contain:

- one repeatable test run
- one clear pass/fail signal
- one useful diagnostic output for failures

### Parallel baseline

Treat parallel execution as an important architectural block, but not as a baseline implementation requirement unless speed is already a real need.

The first version should instead:

- avoid hidden shared mutable state
- keep auth/session state explicit
- keep future isolation needs visible in the design

## Deferred capabilities

These should stay out of the first version unless a real need appears.

### API capabilities to defer

- many domain clients at once
- a heavy steps layer
- large error-model hierarchy
- advanced auth session caching
- many auth personas
- broad raw/negative helper catalog
- protocol-unified abstractions across multiple interface styles

### Test-framework capabilities to defer

- screenshot-based visual comparison
- hosted report publishing
- backend-log aggregation
- advanced concurrency tuning
- broad tagging and suite slicing
- large base-class hierarchies before the extension-first model is proven

### Data and setup capabilities to defer

- dual API and DB fixture backends
- broad truncate/reset mechanisms
- deep persistence abstractions

### UI capabilities to defer

- visual diff infrastructure
- highly specialized widget abstractions
- large component libraries before duplication exists

## Open questions

- Will the future project start API-first, UI-first, or with both from the beginning?
- Will authenticated tests need more than one auth context immediately?
- Is API-driven fixture creation enough, or will privileged DB support be required early?
- Does the team need parallel execution from the start, or can isolation stay simpler at first?
- What is the first real domain area that should prove the API layer end to end?
