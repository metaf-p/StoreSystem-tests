# API layer adaptation

## Goal

Define the smallest clean API-test architecture that can fit into the existing `niffler-e-2-e-tests` architecture map without importing the full weight of the reference framework.

This document follows `prompts/api-layer.md` and uses:

- `prompts/API_ARCHITECTURE_SPEC.md`
- `prompts/API_ARCHITECTURE_ROADMAP.md`

It is intentionally adaptation-oriented, not a description of the full ideal end state.

## What should be included now

### 0. API layer as the default test entry point

In the new project, API is not just one adapter among others.

It will be the primary testing surface.

That means:

- most tests should enter the system through API clients
- test foundation should integrate with API clients first
- UI support, DB support, and special helpers should stay secondary around the API core
- repeated test capabilities should be evaluated in API-first form before adding alternative access paths

Why now:

- the new project will have more API tests than the reference project
- if this rule is not explicit, transport logic, auth logic, and scenario composition will drift into tests very quickly

Minimum rule:

- API clients are the normal entry point for tests unless a case is explicitly UI-only or DB-only

### 1. `ApiConfig`

Keep one small API-focused configuration entry point.

It should own only:

- base API URL
- auth URL if separate
- core content type defaults
- any small number of environment switches that API tests truly need

Why now:

- both the architecture spec and the extracted test docs show that configuration drift becomes painful early
- the reference project already benefits from a centralized config boundary

Minimum rule:

- tests and clients should not hardcode URLs

### 2. `RequestSpecs` and `ResponseSpecs`

Keep one reusable place for technical HTTP defaults.

`RequestSpecs` should own only:

- base URI
- content type
- accept type
- generic logging/reporting filters if needed

`ResponseSpecs` should own only:

- common success expectations
- a few reusable error expectations if they are genuinely reused

Why now:

- this is the cleanest way to keep HTTP mechanics out of tests
- the architecture spec explicitly treats specs as technical, not business-aware

Minimum rule:

- auth acquisition must not live inside request specs

### 3. `Endpoint` and `Endpoints`

Keep typed endpoint definitions from the start.

Include:

- one immutable `Endpoint` description object
- one central `Endpoints` registry for the first tested API area plus auth

Why now:

- this gives explicit API contracts early
- it keeps clients from scattering paths and DTO pairing across many classes

Minimum rule:

- endpoints describe structure only
- endpoints do not perform requests or store assertion logic

### 4. `ApiRequester`

Keep one technical requester as the transport engine.

Include only:

- typed `get`
- typed `post`
- raw `getRaw`
- raw `postRaw`

Add `put` and `delete` only when the first real domain needs them.

Why now:

- the requester is the most important technical seam in the API architecture spec
- the reference test project already benefits from a central HTTP execution helper conceptually through `RestClient`

For the new project this requester should be Rest Assured-based.

It should own:

- direct request execution through Rest Assured
- application of technical request specifications
- serialization and deserialization hooks
- generic request/response logging and reporting filters
- raw response access for negative or special-case checks

Minimum rule:

- requester stays technical and stateless or effectively stateless
- tests and domain clients must not assemble long Rest Assured chains directly

### 5. `BaseApiClient`

Keep one minimal base client.

It should do only two things:

- hold the request specification or requester dependencies
- expose a protected helper for constructing request operations

Why now:

- once there is more than one client, repeated requester wiring becomes noise

Minimum rule:

- base client must not become a god class for auth, data generation, or assertions

### 6. `AuthClient`

Add one dedicated auth client immediately.

It should own:

- login-related calls
- token/session retrieval through API calls
- nothing else

Why now:

- the spec treats auth as a first-class concern that must not leak into unrelated technical layers
- the reference project also keeps auth behavior in a dedicated client path

Minimum rule:

- auth logic lives in the auth client or auth provider, not in request specs

### 7. First domain client

Add exactly one domain client now.

Good shape:

- one client per API area
- meaningful business-oriented method names
- positive-path methods first
- raw methods only where needed for negative tests

Why now:

- the roadmap says the architecture is only proven once one real API area works end to end

Minimum rule:

- tests should usually call domain clients, not the requester directly

### 7a. Client scaling rules for an API-heavy project

Because the new project will have many API tests, the client model needs one more explicit rule set from the start.

Preferred growth shape:

- one client per bounded API area
- separate auth client even if auth endpoints live in the same backend
- separate versioned clients only when the API really exposes versioned behavior worth isolating
- keep pageable or searchable operations in the domain client, but extract request parameter objects when signatures start to grow

Do not allow:

- one mega-client for the whole product API
- clients that mix auth, domain operations, and fixture orchestration
- ad hoc raw HTTP helpers scattered across many feature packages

### 8. DTO structure

Keep DTOs simple and explicit.

Start with:

- `models/request`
- `models/response`
- `models/common`
- `models/error` only if the API really returns structured error bodies that tests inspect

Why now:

- DTO separation keeps contract knowledge out of tests
- it also aligns well with endpoint typing

Minimum rule:

- DTOs should reflect API contracts, not internal framework convenience

### 9. Data generation for API tests

Keep a minimal model-driven data layer, but keep it outside the transport core.

Start with:

- one small data object or builder for auth/user setup
- one or two focused helpers for the first domain

Why now:

- tests still need data, but the data layer should not distort the API architecture

Minimum rule:

- API data generation must not be hidden inside requester or specs

Additional rule for an API-first project:

- domain clients model product API operations
- API fixture helpers may compose several client calls to build state for tests
- fixture helpers must stay separate from the transport core and from normal domain clients

This distinction matters because the reference framework already shows how easy it is for API fixture setup and API clients to blur into one layer.

### 10. `ClientFactory`

Include a very small factory only if there are already multiple auth contexts.

If there is just:

- unauthenticated access
- authenticated-user access

then a small factory is justified.

If there is only one simple client creation path, this can stay very thin.

### 11. `AuthProvider`

Keep an auth provider only if the suite needs reusable authenticated contexts across clients.

Minimal responsibility:

- obtain and expose auth material
- feed authenticated request specs or authenticated client creation

Why now:

- once several clients need the same auth flow, centralizing it reduces duplication

Minimum rule:

- auth provider should provide auth state, not become a domain workflow layer

### 12. Auth context model

If several clients need the same authenticated context, introduce one explicit auth-state object.

Examples:

- `AuthContext`
- `ApiSession`
- `AuthenticatedRequestContext`

It may contain:

- bearer token
- cookies
- CSRF token
- user id or username
- tenant, account, or role metadata when required by the product

Why now:

- a large API suite usually reuses authenticated context across several clients
- explicit auth state is safer than hidden global state and easier to adapt to parallel execution later

Minimum rule:

- authenticated clients should prefer explicit auth context over hidden singleton or thread-local state

## What should be simplified

### Config

Simplify to one `ApiConfig` view instead of a broad multi-purpose configuration surface.

Do not bring in:

- many strategy switches
- CI-only reporting config
- DB/JPA/JNDI infrastructure config

### Request and response specs

Keep them narrow.

Do not put inside them:

- login calls
- domain headers decided by business meaning
- scenario-specific assertions

### Endpoint registry

Start with only:

- auth endpoints
- endpoints for the first tested domain

Do not model the whole product API up front.

### ApiRequester

Do not build an all-capabilities transport abstraction immediately.

Skip for now:

- multipart support
- broad collection extractors
- many overloads for every parameter shape
- protocol mixing
- business assertions

### Base client

Keep it almost boring.

Do not let it absorb:

- steps
- data generation
- assertion helpers
- auth workflows

### Steps

Treat steps as optional and lightweight.

If tests are still readable with clients alone, do not add a large steps layer yet.

### Data generation

Keep one API-friendly fixture path.

Do not copy the dual API/DB backend model from the reference project into the minimal adaptation.

Do not mix together:

- product API clients
- API fixture builders
- test scenario orchestration

### Client factory and auth provider

Keep them small and explicit.

Do not introduce:

- many auth personas
- hidden singleton state
- complex session managers

unless the real test suite already needs them.

### Assertions in the API layer

Keep the assertion boundary explicit.

The API layer may enforce only technical correctness such as:

- expected HTTP status
- presence of body where body is mandatory
- reusable protocol-level parsing guarantees

Do not move into requester, specs, or clients:

- business-result assertions
- scenario meaning
- feature-specific validation better expressed in tests

This is one of the most transferable lessons from the reference framework.

## What should be deferred

These parts belong later, not in the first minimal adaptation:

- multiple domain clients beyond the first real API area
- broad negative-testing helper layers
- a heavy `steps/` catalog for every feature
- error model hierarchy if the tests do not yet assert structured error payloads
- DB-backed API fixture generation
- full client factories for many user roles
- advanced auth session caching
- protocol-unified abstractions across REST, GraphQL, SOAP, and gRPC
- extensive reporting wrappers beyond core HTTP logging

For the new API-heavy project, also defer until real pressure appears:

- a second assertion DSL dedicated only to API tests
- a large catalog of reusable business-flow steps around clients
- separate transport abstractions beyond Rest Assured unless another protocol genuinely becomes first-class

These are all valid growth points, but they are not part of the smallest clean architecture.

## What architectural rules must remain true

These rules are the part that should not be weakened.

### Rule 1

Tests must not assemble raw HTTP calls directly.

Tests should call:

- a client
- or a light step that delegates to clients

### Rule 2

`ApiRequester` must remain technical.

It may know:

- how to send requests
- how to apply specs
- how to deserialize responses

It must not know:

- business meaning
- test data generation
- scenario orchestration

### Rule 3

Auth must not live in request specs.

Auth belongs in:

- `AuthClient`
- `AuthProvider`
- or authenticated client construction

### Rule 4

Clients are the main API entry point for tests.

That keeps tests readable and creates a stable place for endpoint selection and response expectation defaults.

For the new project, this should be treated as the default rule, not just a recommendation.

### Rule 5

Endpoint definitions must stay explicit and typed.

Do not scatter path strings and DTO pairings across test methods.

### Rule 6

Shared mutable state must stay minimal.

If auth or request context is reused, it should be explicit and parallel-safe.

### Rule 7

Data generation must stay outside the transport core.

API data helpers may support tests, but they must not distort requester or client boundaries.

### Rule 8

Positive-path and raw-response paths must stay distinct.

Normal client methods should:

- deserialize typed bodies
- enforce expected technical response shape

Special raw methods may:

- return Rest Assured `Response`
- support negative tests
- support unusual assertions on headers, empty bodies, or partially structured payloads

But raw paths must remain explicit and limited.

### Rule 9

When endpoint signatures start growing, introduce explicit request parameter models.

Prefer:

- small request objects for paging
- filter objects
- search objects
- domain-specific query parameter models

Do not keep growing long method signatures full of nullable scalars.

## Minimal target shape

The smallest clean target shape is:

```text
api/
  auth/
    AuthClient
    AuthProvider
    AuthContext
  client/
    BaseApiClient
    FirstDomainClient
    ClientFactory
  configs/
    ApiConfig
  data/
    UserData
  models/
    request/
    response/
    common/
  requests/
    ApiRequester
    Endpoint
    Endpoints
  specs/
    RequestSpecs
    ResponseSpecs
  fixtures/
    ApiFixtureHelper
```

This is enough to integrate cleanly into the existing architecture map without prematurely creating a full “ideal framework.”

## Practical recommendation

If the goal is the smallest useful API block, keep:

- `ApiConfig`
- `RequestSpecs`
- `ResponseSpecs`
- `Endpoint`
- `Endpoints`
- `ApiRequester`
- `BaseApiClient`
- `AuthClient`
- one first domain client
- minimal DTO packages
- one tiny data helper for auth/user setup

Keep `ClientFactory` and `AuthProvider`, but only in thin forms.

Defer a large `steps` layer until repeated flows clearly justify it.

If the project quickly gains many API areas, grow by adding more domain clients, not by making `BaseApiClient` smarter.

## Fit with the current architecture map

This minimal API adaptation fits the existing extracted map as:

- configuration block: `ApiConfig`
- API-facing adapter block: requester, endpoints, clients, auth
- data generation block: small API-oriented data helpers
- test foundation block: optional auth injection or setup hooks may use `AuthClient` or `AuthProvider`

That makes the API layer a clear adapter block rather than a second framework core.

For the new project this should be interpreted even more strongly:

- the API layer is the main adapter block
- the test foundation should compose around it
- UI and DB support should integrate into that shape without redefining it

## Additional guidance for Rest Assured

Because the new project will use Rest Assured for both testing and transport, add these practical constraints.

### Keep Rest Assured behind architectural seams

Use Rest Assured directly only in:

- `RequestSpecs`
- `ResponseSpecs`
- `ApiRequester`
- thin client construction helpers when needed

Do not let test classes assemble long `given/when/then` chains as the normal style.

### Separate technical specs from business expectations

Rest Assured specifications should contain:

- base URI and path defaults
- content types
- auth material injection
- reusable filters
- technical status expectations only where clearly generic

They should not contain:

- login workflows
- feature-specific headers chosen by scenario meaning
- business assertions hidden inside reusable specs

### Support both typed and raw execution paths

For an API-heavy suite, Rest Assured should support two explicit modes:

- typed execution for common positive-path tests
- raw `Response` execution for negative tests and protocol-level inspection

This prevents the architecture from creating a second ad hoc negative-testing layer later.

### Keep auth reusable but explicit

If Rest Assured request specifications are built from auth state, that auth state should come from:

- `AuthClient`
- `AuthProvider`
- `AuthContext`

not from hidden mutable transport state.

## Growth rules for an API-first suite

As the new project grows, keep these rules visible.

- Add new clients by API area, not by HTTP verb or transport trick.
- Extract reusable request parameter models before method signatures become unreadable.
- Introduce thin steps only for repeated cross-client business flows.
- Keep clients focused on API operations, not on end-to-end scenario composition.
- Prefer explicit auth context over hidden global state.
- Keep technical response validation centralized.
- Keep business assertions visible in tests.

## Open questions

- Hypothesis: if the first domain needs many authenticated personas immediately, `ClientFactory` and `AuthProvider` should be introduced earlier and more explicitly than this minimal version suggests.
- If the target project will rely heavily on negative API testing from the start, raw-response helpers may need to become first-class sooner.
- If the project already has a mature general configuration system, `ApiConfig` may be a thin view over it rather than a standalone configuration subsystem.
