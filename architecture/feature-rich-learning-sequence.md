# Feature-Rich Learning Sequence

## Purpose

Define a practical implementation sequence for a training project that aims to become feature-rich while still proving the architecture on a minimal necessary set of tests.

This sequence assumes:

- the project is educational
- the framework should grow by architectural blocks
- API is the main testing core
- UI must be present from the baseline
- DB support is valuable, but should not define the framework too early

The goal is not to copy the historical growth of the reference project exactly.

The goal is to keep the strongest architectural ideas from the reference project while using a cleaner growth order for a new API-centered test framework.

## Role of this document

This document describes capability milestones, not the canonical execution order.

Use `implementation-roadmap.md` as the source of truth for the step-by-step implementation sequence.

This document should be read as a grouped view of what the framework should become at each milestone, not as the exact internal order in which every capability is implemented.

## Core principle

The framework should grow in this shape:

- API as the core adapter block
- UI as a minimal adapter in the first version
- DB as an early second setup path, not the first setup path
- richer framework features added in layers
- advanced complexity kept optional until justified

This means the project should not grow:

- as a DB-first test framework
- as a UI-first page-object framework
- as a reporting-first demo project

## Milestone 1. Minimal baseline

### Goal

Build a small but architecturally complete framework that already supports both API and UI paths.

This milestone is implemented incrementally through roadmap stages 1-8.

### What must exist

#### 1. Test foundation

Keep a small extension-driven foundation.

Minimum baseline:

- one family entry point for API tests
- one family entry point for UI tests
- shared setup hooks or extensions
- cleanup baseline

Why this matters:

- the reference project shows that foundation shape influences everything else
- if the project starts without a clean execution model, later growth becomes noisy very quickly

#### 2. Configuration

Keep one configuration boundary for the main runtime paths.

Minimum baseline:

- API base URL
- UI base URL
- auth-related settings
- a minimal environment switch only if truly needed

Why this matters:

- tests, clients, and pages should not hardcode runtime addresses
- configuration must stay outside test logic

#### 3. API transport core

Use Rest Assured as the technical transport seam.

Minimum baseline:

- `RequestSpecs`
- `ResponseSpecs`
- `ApiRequester`
- minimal endpoint handling

Why this matters:

- the API layer is the architectural core for the new project
- tests should not assemble long raw Rest Assured flows as the normal path

#### 4. Auth layer

Keep authentication explicit and reusable.

Minimum baseline:

- `AuthClient`
- optional `AuthContext`
- one reusable login or bootstrap path
- one explicit rule for how authenticated UI state is obtained when UI tests need it

Why this matters:

- auth is one of the first cross-cutting concerns that otherwise leaks into specs, pages, and tests

#### 5. First domain API client

Prove the API architecture on one real domain.

Minimum baseline:

- one client for one real API area
- meaningful business-oriented methods
- positive-path methods first
- optional raw path for negative tests

Why this matters:

- the architecture is only proven when tests stop calling raw transport directly

#### 6. Minimal API fixture helpers

Keep setup small but real.

Minimum baseline:

- root entity creation
- one related entity path
- fixture state discoverable by tests

Why this matters:

- tests need readable state setup
- fixture generation should stay outside transport and page layers

#### 7. Minimal UI layer

Include UI immediately, but keep it thin.

Minimum baseline:

- `BasePage`
- one or two route pages
- browser or session bootstrap outside page classes
- only small reusable UI checks
- components only if duplication appears immediately

Why this matters:

- UI is required from the start in this training project
- but UI must remain an adapter around the framework core, not become a second core

#### 8. API-to-UI bridge

Define one explicit way to connect authenticated API state with browser tests.

Possible shapes:

- login through the UI flow
- API auth plus browser session injection
- one explicit hybrid bootstrap path

Why this matters:

- if this rule is left implicit, the project will grow several competing auth/bootstrap styles

#### 9. CI baseline

Run one small but repeatable test path.

Minimum baseline:

- one automated run
- one clear pass or fail signal
- one useful diagnostic output

Why this matters:

- CI should prove the baseline architecture, not just execute a disconnected shell script

### Outcome of milestone 1

After this milestone the project should already have:

- extension-first foundation
- API-first architecture
- explicit auth handling
- minimal fixture support
- minimal UI adapter
- one CI path

This is the smallest acceptable proof of the architecture.

## Milestone 2. Feature-rich expansion

### Goal

Make the framework educationally rich without losing the clean baseline shape.

The items below are growth areas, not a required internal order.

They should be introduced incrementally through the roadmap after the baseline is already proven.

### What should be added next

#### 1. Optional DB support as a second setup path

Introduce DB support here, not before the API core exists.

Good shape:

- narrow DB helper boundary
- privileged setup or reset support
- explicit separation from the normal API path

Why now:

- the project is educational, so DB support is valuable
- but DB should enrich the framework, not define it from the beginning

#### 2. Richer test foundation

Extend the orchestration model after the main runtime paths already exist.

Possible additions:

- more annotations
- richer fixture orchestration
- isolated-test support
- additional family bundles where justified

#### 3. More domain API clients

Scale by bounded API areas.

Good shape:

- one client per API area
- separate versioned clients only when needed
- request parameter models introduced when signatures start to grow

Do not allow:

- one mega-client for the whole product API

#### 4. Negative testing path

Make raw-response handling explicit.

Good shape:

- typed client methods for normal positive tests
- explicit raw response path for negative or protocol-level checks

Why now:

- once API coverage grows, negative testing usually grows too

#### 5. Assertion layer

Add assertion structure after repetition appears.

Good shape:

- technical response validation in shared transport or client layer
- domain assertions in tests
- reusable UI checks in pages or components

#### 6. Parallel execution support

Add it when enough mutable state exists to justify it.

Enable this only when speed, suite size, or runtime cost creates real pressure for concurrency.

Good shape:

- auth and session isolation
- fixture isolation
- thread-safe cleanup
- explicit isolated path for unsafe tests

#### 7. Reporting enrichments

Add richer diagnostics only after the baseline is worth diagnosing in detail.

Possible additions:

- request and response logging
- fixture or debug attachments
- richer reporting integration

#### 8. UI layer growth

Expand the UI layer only after the minimal path has proven useful.

Possible additions:

- more route pages
- reusable components
- page-level reusable checks
- selective custom UI helpers

### Outcome of milestone 2

After this milestone the framework becomes feature-rich in a disciplined way:

- API core
- UI adapter
- DB fallback
- richer orchestration
- richer assertions
- diagnostics
- concurrency support

## Milestone 3. Advanced and optional block

### Goal

Add heavy educational or showcase capabilities without making them mandatory for the core architecture.

### Optional additions

#### 1. Visual checks

Examples:

- screenshot assertions
- visual baselines
- custom visual conditions

#### 2. Advanced UI abstractions

Examples:

- specialized components
- richer widget abstractions
- larger UI helper catalog

#### 3. Multi-protocol support

Examples:

- GraphQL
- gRPC
- SOAP

Important rule:

- these should be added as adapters, not as a replacement for the API-centered core

#### 4. Advanced auth and session models

Examples:

- multiple personas
- richer role-aware auth contexts
- refresh-token or long-session handling

#### 5. Advanced DB abstractions

Examples:

- repositories
- multiple DB implementations
- richer transaction strategies
- more complex reset models

#### 6. Hosted reporting or publishing

Examples:

- external report publishing
- log aggregation
- richer CI commentary

## How this differs from the reference project history

The reference project appears to have grown roughly like this:

1. JUnit extensions
2. DB setup and transactions
3. more DB abstractions
4. fixture extensions
5. shared framework core
6. reporting and UI assertion enrichments
7. auth
8. REST API tests
9. additional protocols
10. docker and CI portability

This new sequence is different on purpose.

It keeps:

- extension-first orchestration
- clean transport boundaries
- explicit auth handling
- declarative fixture setup

But it changes the order:

- API core comes earlier
- UI is included early but stays minimal
- DB support comes after API baseline
- reporting and advanced enrichments come later

## Architectural reading of the sequence

This sequence should be understood as:

- API is the main system-facing core
- UI is required from the start, but remains a thin adapter
- DB is educationally valuable, but should arrive as a second path
- feature richness should grow in layers, not all at once

## Readiness check

The sequence is being followed correctly if:

- the first version already supports both API and UI tests
- tests normally enter through API clients or pages, not raw transport calls
- auth has one explicit reusable path
- fixture setup is visible and separated from transport and page layers
- DB support enriches the architecture instead of replacing the API path
- advanced features are added because they teach or enable something meaningful, not just because they exist in the reference project

## Practical summary

For this training project, the recommended order is:

1. foundation
2. configuration
3. API transport core
4. auth
5. first domain API client
6. minimal fixture helpers
7. minimal UI layer
8. API-to-UI bridge
9. CI baseline
10. DB as second setup path
11. richer foundation, clients, assertions, parallel support, and reporting
12. advanced optional capabilities

## Mapping to the implementation roadmap

This document groups the target architecture into milestones.

`implementation-roadmap.md` defines the execution order inside those milestones.

The intended mapping is:

| Learning sequence | Implementation roadmap |
|---|---|
| Baseline milestone | Stages 1-8 |
| Expansion milestone | Stage 9 plus selected Stage 10 growth areas, and Stage 11 only when needed |
| Advanced optional milestone | Post-roadmap optional capabilities |

This produces a framework that is:

- educational
- feature-rich
- API-centered
- UI-capable from the baseline
- more disciplined than the historical growth path of the reference project
