# Framework evolution

## Goal

Describe how the current framework appears to have evolved over time and what that means for adaptation.

## Why it exists

The current reference architecture is easier to understand when read as an evolved system rather than as a perfectly planned greenfield framework.

The repository history suggests a growth pattern:

1. extension-driven foundation
2. early DB setup and transactions
3. richer DB abstractions
4. fixture extensions
5. shared runtime infrastructure
6. reporting and UI condition enrichments
7. auth and API tests
8. broader protocol coverage
9. dockerized CI/runtime support

This is useful because the historical order is not automatically the best order to repeat in a new project.

## Main stages

### Stage 1. JUnit extension foundation

The framework first became extension-driven through:

- `BrowserExtension`
- `SuiteExtension`
- `TestMethodContextExtension`
- meta-annotations
- declarative fixture annotations

### Stage 2. Early DB-backed setup

Before a mature API-first test architecture appeared, the framework invested heavily in:

- JDBC
- JTA
- Spring JDBC
- Hibernate
- repositories
- DB-backed clients

### Stage 3. Fixture extensions

DB-backed or hybrid setup moved into the foundation through extensions such as `UserExtension`.

### Stage 4. Shared runtime core

`RestClient`, `ThreadSafeCookieStore`, and base classes created a more reusable technical core.

### Stage 5. Reporting and UI enrichments

Allure lifecycle integrations, SQL reporting, screenshot tests, and custom UI conditions widened the framework.

### Stage 6. Auth and API maturation

OAuth login and REST tests became stronger explicit architectural concerns later.

### Stage 7. Protocol broadening

GraphQL, gRPC, and SOAP support turned the project into a broader multi-interface framework.

### Stage 8. Docker and operational CI support

The framework then gained containerized execution and external report publishing support.

## Patterns used

- evolution by educational increments
- early privileged setup power before clean API-first discipline
- widening through reporting and protocol breadth
- retention of some historical mechanisms after the preferred model changed

## What is essential

- Read the framework as an evolved teaching system, not as a single-pass ideal design.
- Separate enduring architectural ideas from the order in which they historically appeared.
- Use evolution history to identify which parts are core and which parts are growth artifacts.

## What is optional

- Repeating the historical order in a new project.
- Preserving every capability that appeared during growth.

## What looks overengineered

- The reference project introduced DB depth early and reporting breadth before a fully explicit API-first model was in place.
- Some of that complexity is educationally useful, but not all of it should be copied into another project unchanged.

## Adaptation for my project

- Reuse the strong ideas:
  - extension-first orchestration
  - declarative fixtures
  - explicit runtime seams
  - clear auth handling
  - optional protocol adapters
- Do not automatically repeat the exact historical order.
- If the new project is API-first, move API core earlier than the reference history did.

## Readiness criteria

- The team can explain which parts of the reference are core ideas and which are historical growth artifacts.
- Architectural choices for the new project are not justified only by “the reference project also has this”.

## Open questions

- The reference project’s growth path is understandable and teachable.
- The main adaptation question is how much of that path should be preserved as learning structure versus filtered into a cleaner sequence.
