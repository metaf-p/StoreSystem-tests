# Protocol adapters

## Goal

Describe how the current framework supports multiple system interfaces beyond the main REST and UI paths.

## Why it exists

The project is not only a web-plus-REST test suite.

It also contains:

- GraphQL tests
- gRPC tests
- SOAP tests

These are not implemented as one universal transport abstraction.
Each protocol gets a focused adapter shape.

## Main elements

- GraphQL
  `BaseGraphQlTest` plus generated Apollo classes and `src/test/graphql/*`.
- gRPC
  `BaseGrpcTest` plus gRPC stubs from `niffler-grpc-common`.
- SOAP
  `BaseSoapTest`, `UserdataSoapClient`, XJC-generated JAXB classes, and custom SOAP converters.

## Internal structure

### GraphQL adapter

- `BaseGraphQlTest` builds a reusable Apollo client
- auth still comes from the standard auth/foundation path via `ApiLoginExtension.rest()`
- generated GraphQL sources are part of the test source set

### gRPC adapter

- `BaseGrpcTest` builds channels and blocking stubs
- config provides target addresses and ports
- gRPC transport logging goes through a protocol-specific interceptor

### SOAP adapter

- `BaseSoapTest` exposes a reusable SOAP client
- `UserdataSoapClient` still reuses `RestClient`
- request and response conversion are provided by custom SOAP converter infrastructure
- JAXB classes are generated from WSDL at build time

## Dependencies

Important dependency relationships:

- GraphQL depends on:
  - generated Apollo classes
  - `Config`
  - auth bootstrap through the standard foundation
- gRPC depends on:
  - `niffler-grpc-common`
  - `Config`
  - protocol-specific channel and stub construction
- SOAP depends on:
  - WSDL/XJC generation
  - `SoapConverterFactory`
  - `RestClient`
  - `Config`

Cross-block observation:

- non-REST protocols still depend on the same test foundation and often the same auth/bootstrap model
- the framework remains multi-interface, but not protocol-unified

## Typical flow

GraphQL flow:

1. `BaseGraphQlTest` builds the Apollo client.
2. `ApiLoginExtension.rest()` provides auth state.
3. The test executes GraphQL operations and asserts the result.

gRPC flow:

1. `BaseGrpcTest` builds channels and blocking stubs.
2. The test calls the gRPC service directly through stubs.
3. Assertions remain in the test body.

SOAP flow:

1. `BaseSoapTest` exposes `UserdataSoapClient`.
2. SOAP request objects are built from generated JAXB types.
3. The SOAP client executes the request through Retrofit plus SOAP converters.

## Patterns used

- adapter-per-protocol
  Each protocol gets a dedicated setup shape.
- protocol-specific base class
  Heavier protocols use base classes instead of forcing all setup into annotations alone.
- reuse of shared foundation
  Protocol adapters still rely on the same JUnit-driven setup model.
- build-time generated clients
  GraphQL and SOAP use generated artifacts as part of the adapter layer.

## What is essential

- Treat protocol support as adapter blocks, not as the framework core.
- Keep shared foundation behavior reusable across protocols.
- Allow protocol-specific runtime wiring where transport differences are real.
- Keep assertions near tests even when the protocol differs.

## What is optional

- Supporting all three extra protocols in one test module.
- Using base classes for every protocol.
- Reusing the same `RestClient` infrastructure for SOAP.

## What looks overengineered

- Broad multi-protocol coverage in one module is powerful, but heavy for many teams.
- SOAP support especially carries extra build-time and runtime machinery.
- The project demonstrates protocol breadth more strongly than most product teams will need.

## Adaptation for my project

- Keep REST as the main API-facing core unless another protocol is central to the product.
- Add GraphQL, gRPC, or SOAP only if they represent real testing value.
- Model them as adapters around the framework core.
- Do not let multi-protocol support redefine the whole test architecture too early.

## Readiness criteria

- The team can explain how each protocol enters the framework.
- Extra protocol support does not replace the main API core.
- Shared setup and cleanup remain consistent across protocols.
- Generated artifacts and protocol-specific dependencies are intentional and maintainable.

## Open questions

- The reference project demonstrates a wide adapter surface.
- For adaptation, the main question is not whether multiple protocols are possible, but whether they should be first-class in the new project.
