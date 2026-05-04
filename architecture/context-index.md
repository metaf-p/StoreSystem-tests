# Architecture context index

This file is the lightweight entry point for new chats.
Read it before opening larger architecture documents.

## Core direction

Build the test automation framework iteratively.

The framework should grow from real working flows into small reusable abstractions:

1. prove one real flow
2. repeat it in a small number of tests
3. identify stable repetition
4. extract the smallest useful abstraction
5. expand one framework block at a time

Keep the API layer as the primary framework core.
Let JUnit extensions become the main way to share test setup and runtime behavior.
Add UI, DB, reporting, CI, and parallel execution around the API core, not instead of it.

## Current reading strategy

Do not load every architecture document at chat start.

Use this order:

1. Read this file for orientation.
2. Read `architecture/deferred-tasks.md` to see postponed work.
3. Read `architecture/implementation-roadmap.md` only when choosing or validating the next implementation step.
4. Read `architecture/architecture-map.md` only when block boundaries or dependencies matter.
5. Read `architecture/feature-rich-learning-sequence.md` only when planning learning order or staged capability growth.
6. Read files from `architecture/reference-architecture/` only for the relevant technical area.

## Main project documents

- `architecture/implementation-roadmap.md`: staged implementation plan from baseline to richer framework capabilities.
- `architecture/architecture-map.md`: architectural blocks, dependency rules, initial baseline, deferred capabilities, open questions.
- `architecture/feature-rich-learning-sequence.md`: learning-oriented sequence for growing the framework without premature abstraction.
- `architecture/deferred-tasks.md`: backlog of intentionally postponed tasks.

## Reference architecture lookup

Use reference documents as on-demand notes, not as required context.

- Project structure: `architecture/reference-architecture/01-project-structure.md`
- Test foundation and JUnit extensions: `architecture/reference-architecture/02-test-foundation.md`
- Configuration: `architecture/reference-architecture/03-configuration.md`
- Data generation: `architecture/reference-architecture/04-data-generation.md`
- API layer: `architecture/reference-architecture/05-api-layer-adaptation.md`
- DB layer: `architecture/reference-architecture/06-db-layer.md`
- UI layer: `architecture/reference-architecture/07-ui-layer.md`
- CI/CD: `architecture/reference-architecture/08-ci-cd.md`
- Parallel execution: `architecture/reference-architecture/09-parallel-execution.md`
- Assertions: `architecture/reference-architecture/10-assertion-layer.md`
- API current state notes: `architecture/reference-architecture/11-api-layer-current-state.md`
- Auth and sessions: `architecture/reference-architecture/12-auth-session-model.md`
- Runtime core: `architecture/reference-architecture/13-runtime-core.md`
- Protocol adapters: `architecture/reference-architecture/14-protocol-adapters.md`
- Model layer: `architecture/reference-architecture/15-model-layer.md`
- Build and generation toolchain: `architecture/reference-architecture/16-build-generation-toolchain.md`
- Test organization and naming: `architecture/reference-architecture/17-test-organization-and-naming.md`
- Legacy and experimental mechanisms: `architecture/reference-architecture/18-legacy-and-experimental-mechanisms.md`
- Framework evolution: `architecture/reference-architecture/19-framework-evolution.md`
- Monorepo integration boundary: `architecture/reference-architecture/20-monorepo-integration-boundary.md`

## Practical rule for next-task suggestions

When suggesting the next task, prefer the smallest step that advances the current roadmap stage.
If a useful idea is not needed yet, write it to `architecture/deferred-tasks.md` instead of expanding the current scope.
