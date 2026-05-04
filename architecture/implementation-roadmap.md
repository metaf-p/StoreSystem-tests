# Implementation roadmap

## Purpose

Turn the target test architecture into a practical implementation sequence for a new training project.

This roadmap favors:

- small stages
- early usable results
- minimal baseline first
- delayed complexity until there is real pressure for it

## Role of this document

This document is the authoritative step-by-step implementation order.

Its stages are execution steps, not delivery milestones.

Use `feature-rich-learning-sequence.md` for grouped capability milestones and for the higher-level picture of what the baseline and later expansion should look like.

## Stage 1. Set the boundaries

### Goal

Define the architectural rules and project shape before adding framework code.

### Why this stage comes now

If the boundaries are not explicit from the start, the project will drift into test-specific shortcuts and mixed responsibilities very quickly.

### Architectural block it covers

- test foundation
- configuration
- API layer
- data generation

### Expected outcome

- one agreed target structure
- one agreed dependency model
- one agreed minimum baseline
- one agreed list of deferred capabilities
- one agreed growth rule for how abstractions should appear

### Readiness criteria

- the team can explain the core architectural blocks
- the team agrees that tests should not assemble raw HTTP directly
- the team agrees that auth should not live in request specs
- the team agrees that DB support is optional, not default
- the team agrees to grow abstractions only after simple flows are proven
- the team agrees that the framework is API-first and extension-first

### What must not be implemented yet

- heavy generic abstractions before one real flow works
- DB support as the first setup path
- heavy steps layer
- advanced reporting
- parallel execution tuning

### Growth rule

Build the framework iteratively.

Start from the smallest working version of the core concepts and prove them on a minimal but meaningful set of tests.

Only after a concept works in a simple form may it be extracted into a reusable abstraction.

Apply this together with these priorities:

- feature-rich through staged growth, not through early abstraction-heavy design
- API-first as the primary framework core
- extension-first as the preferred shape for shared test behavior

## Stage 2. Add configuration and technical API base

### Goal

Create the minimum technical foundation required for API-based tests.

### Why this stage comes now

Configuration and technical HTTP mechanics are the base for every later API capability, so they should be stable before domain logic appears.

### Architectural block it covers

- configuration
- API layer

### Expected outcome

- `ApiConfig`
- `RequestSpecs`
- `ResponseSpecs`
- `Endpoint`
- `Endpoints`
- `ApiRequester`
- one clear technical runtime seam for API transport

### Readiness criteria

- environment settings are read from one configuration boundary
- request and response defaults are reusable
- the requester is technical and stateless in shape
- endpoint definitions are typed and centralized
- the design can later plug cleanly into an extension-first foundation

### What must not be implemented yet

- many endpoint registries
- many overloaded requester methods
- domain-specific helpers inside requester/specs
- auth caching
- multiple protocol abstractions

### Current status note

Stage 2 is partially complete.

Current decision:

- the project currently uses one `Config` implementation
- this implementation is accepted for the current stage because the framework still has one real runtime path
- configuration complexity is intentionally kept low until a second real environment appears

Why this is acceptable now:

- the project already has one configuration boundary
- adding several `Config` implementations and a factory selection mechanism now would be premature abstraction
- the current goal is to keep the baseline small and focused while the core execution model is still being proven

Deferred for a later step:

- additional `Config` implementations
- environment-aware config selection via system property
- richer separation between local and CI/runtime settings
- broader UI-related runtime configuration once the UI baseline is introduced

Trigger to revisit:

- CI baseline is introduced
- a second real environment is added
- UI tests require their own runtime path
- local and CI settings start to diverge in a meaningful way

## Stage 3. Add auth path and authenticated runtime

### Goal

Create one explicit reusable authentication path before several domain clients start depending on auth in different ways.

### Why this stage comes now

The reference project shows that auth becomes a cross-cutting concern quickly. It is better to make it explicit before several API clients and UI flows depend on it.

### Architectural block it covers

- API layer
- test foundation
- configuration

### Expected outcome

- `AuthClient`
- one explicit authenticated request path
- optional thin `AuthContext` or equivalent reusable auth state object
- one agreed rule for API-to-UI authentication bootstrap if UI needs authenticated entry

### Readiness criteria

- auth is not hidden inside request specs
- auth is not hidden inside pages
- the team can explain where token, cookie, or session state lives
- several clients could reuse the auth path without duplicating login logic

### What must not be implemented yet

- many auth personas
- advanced token caching
- a large auth workflow layer
- hidden singleton auth state

## Stage 4. Add the first usable API client path

### Goal

Prove the architecture on one real API area.

### Why this stage comes now

The architecture is still theoretical until one real domain works end to end through the intended client flow.

### Architectural block it covers

- API layer

### Expected outcome

- `BaseApiClient`
- one first domain client
- minimal request/response/common DTOs

### Readiness criteria

- tests can call one domain client without touching raw HTTP
- auth calls are already isolated in a dedicated client path
- DTOs reflect API contracts rather than framework convenience
- the first domain client exposes meaningful business-facing methods

### What must not be implemented yet

- many domain clients at once
- a large shared base client
- heavy steps catalog
- large error-model hierarchy

## Stage 5. Add minimal data generation

### Goal

Give tests a small reusable way to create the state they need.

### Why this stage comes now

Once one API path exists, test readability depends on removing repeated setup noise.

### Architectural block it covers

- data generation
- API layer

### Expected outcome

- one root fixture concept
- one small API-oriented data helper
- one simple way for tests to access generated fixture state

### Readiness criteria

- tests can prepare a root entity without repeating low-level setup
- data generation stays outside requester and request specs
- fixture helpers remain small and domain-focused

### What must not be implemented yet

- dual API and DB fixture backends
- broad reset logic
- a large factory catalog for every possible test entity

## Stage 6. Add extension-first test foundation composition

### Goal

Make tests start through one consistent execution model.

### Why this stage comes now

Once clients and fixture helpers exist, the next improvement is to make test setup declarative and reusable instead of manually repeated.

### Architectural block it covers

- test foundation

### Expected outcome

- one or two test-family entry points
- shared setup hooks or extensions
- shared cleanup hooks
- optional auth/bootstrap integration with the API layer
- extension-first composition rather than a base-class-heavy framework shape

### Readiness criteria

- tests do not repeat the same setup in every class
- fixture injection or equivalent shared setup is centralized
- cleanup of mutable state is automatic
- the foundation depends on lower layers without absorbing their responsibilities
- the team can explain the foundation as extension-first, not inheritance-first

### What must not be implemented yet

- many specialized annotations
- large stacks of extensions
- issue-based disabling
- suite-wide advanced reporting hooks

## Stage 7. Complete the baseline with a minimal UI adapter

### Goal

Complete the baseline by adding a minimal UI layer as a thin adapter around the API-centered core.

### Why this stage comes now

The project is intended to be educational and feature-rich, and UI is part of the baseline. But UI should appear only after API core, auth, fixtures, and foundation shape are already understood.

### Architectural block it covers

- UI layer
- test foundation
- configuration

### Expected outcome

- one base page
- a few route pages
- browser/session bootstrap outside page classes
- one explicit API-to-UI auth bridge if authenticated UI entry is required

### Readiness criteria

- browser tests read like scenarios rather than selector scripts
- page objects do not contain browser bootstrap concerns
- UI stays thinner than the API core
- the team can explain how UI authentication entry works

### What must not be implemented yet

- screenshot diffing
- large component libraries
- many specialized widget abstractions
- a second framework center built around pages

## Stage 8. Add CI/CD baseline

### Goal

Run the project in one repeatable automated path.

### Why this stage comes now

The framework should already be useful locally before CI is added, but CI should arrive before optional growth features start multiplying.

CI/CD belongs to the baseline scope, but it should be added only after the same baseline already works locally.

### Architectural block it covers

- CI/CD

### Expected outcome

- one automated test run
- one clear pass/fail signal
- one useful failure output such as test results or a simple artifact

### Readiness criteria

- CI runs the same baseline architecture the team uses locally
- failures are diagnosable from preserved outputs
- the CI contract stays small and explicit

### What must not be implemented yet

- hosted report systems
- rich PR commentary
- broad artifact catalogs
- environment-specific complexity beyond what the test project truly needs

## Stage 9. Add optional DB support as a second setup path

### Goal

Introduce DB support as an educationally useful privileged setup path without letting it become the first or default framework center.

### Why this stage comes now

The reference project shows that DB support can become powerful quickly, but also heavy. It should be added only after the API-centered baseline is already proven.

### Architectural block it covers

- DB layer
- data generation

### Expected outcome

- one narrow DB support boundary
- one privileged setup or reset path
- one explicit rule for when DB support is allowed instead of API setup

### Readiness criteria

- DB support is clearly secondary to the API path
- tests do not depend directly on DB internals
- the team can explain why a given setup path is API-based or DB-based

### What must not be implemented yet

- many DB implementations at once
- deep repository abstraction before clear need
- broad reset logic across the whole framework

## Stage 10. Add feature-rich expansion layers

### Goal

Expand the framework in layers after the baseline is already proven.

### Why this stage comes now

The project is educational and feature-rich, so richer capabilities are valuable. But they should grow only after the baseline architecture already works.

### Architectural block it covers

- test foundation
- API layer
- UI layer
- DB layer
- assertion layer
- parallel execution
- CI/CD

### Expected outcome

- more domain API clients
- explicit negative-testing path
- richer assertions
- richer reporting
- parallel-readiness review of auth, fixture, and cleanup seams
- richer UI components only where duplication exists

### Readiness criteria

- new framework power appears through proven repeated use
- abstractions are extracted from real flows, not invented early
- the API core still remains the main framework center
- added richness does not collapse readability

### What must not be implemented yet

- complexity added only because it exists in the reference project
- protocol expansion without a real testing need
- abstraction-heavy redesign of already proven simple paths
- visual regression infrastructure

## Stage 11. Add parallel execution support if speed becomes a real need

### Goal

Enable concurrency safely.

### Why this stage comes now

Parallel execution should follow stable architecture, not precede it, because concurrency amplifies every hidden shared-state mistake.

### Architectural block it covers

- parallel execution support
- test foundation

### Expected outcome

- explicit concurrency settings
- per-test or per-thread isolation for mutable state
- safe cleanup model for concurrent runs

### Readiness criteria

- auth/session state is isolated
- mutable fixture state does not leak across tests
- cleanup does not destroy unrelated concurrent runs

### What must not be implemented yet

- advanced pool tuning
- broad shared state managers
- complex concurrency abstractions before real demand exists

## Mapping to the learning sequence

This roadmap and `feature-rich-learning-sequence.md` describe the same target through two different lenses.

This roadmap defines the implementation order.

The learning sequence defines grouped capability milestones.

The intended mapping is:

- learning milestone 1 maps to roadmap stages 1-8
- learning milestone 2 maps to roadmap stage 9 plus selected stage 10 expansions, and stage 11 only if speed pressure appears
- learning milestone 3 covers optional capabilities beyond the core roadmap

## Summary

The recommended order is:

1. set boundaries
2. build the technical API base
3. add auth path and authenticated runtime
4. prove one real client path
5. add minimal data generation
6. add the extension-first test foundation
7. add the minimal UI adapter
8. add CI
9. add optional DB support as a second setup path
10. add feature-rich expansion layers
11. add parallel support when speed makes it necessary

This keeps the project useful early while protecting it from framework overgrowth.  
