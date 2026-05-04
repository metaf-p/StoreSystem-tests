# Build and generation toolchain

## Goal

Describe the build-time mechanisms that generate sources and shape the test framework before runtime begins.

## Why it exists

The current framework does not rely only on handwritten test code.

It also depends on generated artifacts:

- GraphQL schema and generated operation models
- SOAP JAXB classes generated from WSDL
- test source-set wiring for generated code

These build-time steps are architecturally important because they affect:

- protocol adapter design
- module dependencies
- CI requirements
- framework maintenance cost

## Main elements

- Apollo GraphQL plugin
  Downloads schema through introspection and generates GraphQL client sources.
- XJC generation plugin
  Generates SOAP/JAXB classes from WSDL.
- `sourceSets.test`
  Includes handwritten test sources plus generated GraphQL and SOAP sources.
- `tasks.named(...generateNifflerApolloSources...)`
  Makes GraphQL source generation depend on schema download.
- `test` task dependency wiring
  Ensures generated GraphQL sources exist before tests run.

## Internal structure

The build/generation model is embedded into the test module.

1. The test module is not only a consumer of generated artifacts
   It is responsible for generating them.

2. GraphQL generation is environment-aware
   Introspection target depends on `test.env`.

3. SOAP generation is schema-driven
   WSDL schema is part of the test sources and generates Java classes into the test source set.

4. Generated artifacts become normal test sources
   They are not treated as an external library boundary.

## Dependencies

Important dependency relationships:

- GraphQL generation depends on:
  - Apollo plugin
  - environment-aware introspection URL
  - schema file location
- SOAP generation depends on:
  - XJC plugin
  - WSDL schema file
  - JAXB dependencies
- protocol adapters depend on the generated source output
- CI and docker execution implicitly depend on generation succeeding before test execution

## Typical flow

GraphQL generation flow:

1. Build config resolves the gateway introspection URL from `test.env`.
2. Apollo downloads schema through introspection.
3. Apollo generates Java sources into the test source set.
4. GraphQL tests compile against generated classes.

SOAP generation flow:

1. XJC reads the WSDL schema from `src/test/schemas/xjc`.
2. Java classes are generated into the test source set.
3. SOAP tests and clients compile against those generated types.

## Patterns used

- generated-client-first protocol support
  GraphQL and SOAP support rely on generated artifacts rather than handwritten protocol models.
- build-time integration into the test module
  Generation is part of the same module that owns the tests.
- environment-aware generation
  Some generated inputs depend on the current runtime mode.

## What is essential

- Recognize generated sources as part of the framework architecture, not just build noise.
- Keep generation wiring explicit and reproducible.
- Understand that protocol adapters may depend on build-time generation as much as on runtime clients.

## What is optional

- Keeping generation inside the test module rather than in a separate generated-client module.
- Using live introspection for GraphQL schema acquisition.

## What looks overengineered

- Generated protocol support inside one test module increases build and maintenance complexity.
- Environment-aware generation adds fragility if the target service is unavailable.

## Adaptation for my project

- Only keep build-time generation if the protocol coverage genuinely needs it.
- Prefer explicit, reproducible generation wiring over ad hoc manual updates.
- Treat generated artifacts as architectural dependencies when estimating framework complexity.

## Readiness criteria

- The team can explain which tests depend on generated sources.
- Build and CI flows make generation requirements explicit.
- Generated protocol support is maintained intentionally rather than passively.

## Open questions

- The reference project treats generation as part of the test module itself.
- For adaptation, a separate generated-client boundary may be worth considering if protocol coverage grows further.
