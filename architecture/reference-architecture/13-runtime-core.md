# Runtime core

## Goal

Describe the shared runtime core that sits underneath several test styles in the current framework.

## Why it exists

The framework has several architectural blocks documented separately, but some important mechanics actually live in a shared runtime core:

- transport execution
- cookie and session handling
- protocol base classes
- thread-aware state handling
- support-service clients

Without naming this core explicitly, the framework can look more fragmented than it really is.

## Main elements

- `RestClient`
  Shared HTTP and SOAP execution base.
- `ThreadSafeCookieStore`
  Thread-bound cookie jar for transport flows.
- `CodeInterceptor`
  Redirect-interception helper for auth code capture.
- protocol base classes
  `BaseGraphQlTest`, `BaseGrpcTest`, `BaseSoapTest`.
- support-service clients
  `GhApiClient` and `AllureDockerApiClient` show that the runtime core is used not only for product APIs.

## Internal structure

The runtime core sits between architectural blocks.

1. `RestClient` forms the main execution seam
   It centralizes HTTP-like transport concerns for several clients and even SOAP support.

2. `ThreadSafeCookieStore` supports both auth and parallel execution
   It is not only an auth utility. It is part of the runtime isolation model.

3. Protocol base classes provide heavier protocol-specific runtime wiring
   - GraphQL builds a reusable Apollo client
   - gRPC builds reusable channels and blocking stubs
   - SOAP builds a reusable SOAP-aware client

4. Support clients reuse the same runtime ideas
   GitHub and Allure Docker integration show that the framework runtime core also supports external operational services.

## Dependencies

Important dependency relationships:

- transport clients depend on `RestClient`
- auth depends on `ThreadSafeCookieStore` and `CodeInterceptor`
- web bootstrap depends on auth state produced through the runtime core
- GraphQL and gRPC base classes depend on `Config` and protocol-specific libraries
- reporting and issue-based disabling depend on support-service clients built on the same shared client model

## Typical flow

Typical runtime flow for HTTP-like paths:

1. A concrete client extends or uses `RestClient`.
2. `RestClient` builds the transport client and applies logging, reporting, and cookie infrastructure.
3. The client invokes typed methods on Retrofit contracts.
4. Shared execution helpers validate status and body presence.

Typical runtime flow for non-REST protocols:

1. A base test class constructs the protocol client or stub once for that protocol family.
2. The test reuses that prewired runtime object.
3. Auth or data setup may still depend on the shared foundation and REST-oriented helpers.

## Patterns used

- shared execution seam
  Several protocol-like and support paths reuse one common runtime style.
- protocol-specific wiring at the edge
  GraphQL, gRPC, and SOAP each get a focused base setup instead of forcing one universal transport abstraction.
- thread-aware runtime state
  Cookie and session handling are isolated for parallel execution.
- support-service reuse
  Framework operations such as issue checks and Allure report upload reuse the same client approach as product-facing calls.

## What is essential

- One shared runtime seam for repeated technical behavior.
- Explicit thread-aware mutable state handling.
- Protocol-specific base wiring only where needed.
- Separation between runtime concerns and test scenario concerns.

## What is optional

- Reusing the same HTTP-oriented core for support services.
- Keeping GraphQL, SOAP, and REST close to the same runtime style.
- Protocol base classes for every non-REST interface.

## What looks overengineered

- The shared runtime core is effective, but not named explicitly in the current project structure.
- Some concerns that could be modeled as one explicit runtime layer are spread between `service`, `api/core`, and test base classes.

## Adaptation for my project

- Make the runtime core explicit early.
- Keep shared technical behavior centralized.
- Do not force one universal protocol abstraction if protocol-specific edges remain clearer.
- If the project is API-first, keep the runtime core aligned with the API layer rather than allowing support-service integrations to shape it.

## Readiness criteria

- The team can explain which classes form the shared runtime core.
- Shared transport and session mechanics are not duplicated across clients.
- Protocol-specific runtime wiring is deliberate and limited.
- Test scenario code does not own transport infrastructure.

## Open questions

- In the current reference project, the runtime core is strong in practice but implicit in documentation.
- For adaptation, naming this layer explicitly may reduce confusion between transport, services, and foundation.
