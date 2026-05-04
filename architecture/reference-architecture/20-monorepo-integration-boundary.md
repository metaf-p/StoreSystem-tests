# Monorepo integration boundary

## Goal

Describe how the test framework sits inside the larger repository and which surrounding modules matter to understanding it.

## Why it exists

The framework is implemented in a dedicated test module, but it is not isolated from the repository around it.

It depends on monorepo context through:

- build configuration
- generated protocol clients
- docker-compose runtime
- gRPC shared contracts
- product service endpoints under test

At the same time, the framework should not be confused with the architecture of the whole monorepo.

## Main elements

- dedicated test module
  `niffler-e-2-e-tests` is its own Gradle module.
- shared gRPC contract module
  `niffler-grpc-common` is consumed directly by the test module.
- runtime services under test
  auth, gateway, userdata, spend, and currency services shape configuration and adapter boundaries.
- docker and CI environment
  docker-compose and CI workflows define how the framework meets the system at runtime.

## Internal structure

The test framework boundary inside the monorepo works like this:

1. The framework owns test architecture and test runtime code.
2. It consumes product runtime behavior through service endpoints, browser flows, and protocol contracts.
3. It consumes selected shared code from the monorepo when protocol support requires it, for example gRPC stubs.
4. It does not model the whole repository as one unified application-test architecture.

## Dependencies

Relevant monorepo dependencies:

- `settings.gradle` includes the dedicated test module
- `niffler-grpc-common` provides gRPC stubs
- docker-compose setup provides the runtime under test
- product modules provide services whose URLs and contracts are configured in the test module

Important non-goal:

- most product modules are not in scope unless they directly affect test execution architecture

## Typical flow

Typical monorepo-level flow:

1. The test module compiles with its own dependencies plus shared protocol contracts where needed.
2. The runtime environment starts the relevant product services.
3. The test framework interacts with those services through UI, REST, GraphQL, gRPC, or SOAP.
4. CI and docker execution tie the test module back into the repository lifecycle.

## Patterns used

- dedicated test module in a monorepo
- selective reuse of shared contracts
- runtime coupling through environment and service URLs rather than direct code dependency for most tested behaviors

## What is essential

- Keep the framework boundary explicit even inside a monorepo.
- Only analyze surrounding modules when they affect test architecture directly.
- Treat shared contract modules and runtime orchestration as real architectural dependencies.

## What is optional

- Reusing shared code from product modules beyond protocol contracts.
- Broad monorepo-wide analysis for every framework decision.

## What looks overengineered

- Nothing here is inherently overengineered, but monorepo context can easily tempt analysis to sprawl beyond the test architecture itself.

## Adaptation for my project

- Keep the test framework boundary explicit from the start.
- Use surrounding product modules and docs only when they define expected behavior, runtime contracts, or protocol dependencies relevant to the current step.
- Avoid turning the framework project into a general architecture mirror of the whole monorepo.

## Readiness criteria

- The team can explain which monorepo areas matter to the framework and which do not.
- Shared-contract dependencies are intentional.
- Runtime environment assumptions are documented and visible.

## Open questions

- The reference project keeps the test module reasonably isolated, but surrounding runtime dependencies still matter.
- For adaptation, the main question is how much product code or shared contract code should be reused directly versus treated as an external dependency.
