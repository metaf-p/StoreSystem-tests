# Model layer

## Goal

Describe the shared test-facing model layer used across the current test framework.

## Why it exists

The framework uses shared models across several concerns:

- REST responses
- GraphQL results
- fixture generation
- UI assertions
- support-service reporting

This layer is not just transport DTO storage. It also carries test-specific state in some places.

## Main elements

- `model/*`
  Core test-facing DTOs such as `UserJson`, `SpendJson`, `CategoryJson`, `SessionJson`, `StatisticJson`, `FriendJson`, and enums/value objects.
- `model/TestData`
  Test-only enrichment attached to user objects for fixture state.
- `model/page/RestPage`
  A page-like model abstraction used for pageable REST responses.
- `model/allure/*`
  Support models for Allure Docker integration and file handling.

## Internal structure

The model layer is shared, but not strictly separated by source.

1. Product-facing DTOs
   These represent API-visible domain entities such as users, spends, categories, sessions, and statistics.

2. Test-only enrichment
   `TestData` attaches passwords, friends, invitations, and related test-owned metadata to runtime user models.

3. Support-model sublayers
   Reporting and paging support models sit in subpackages instead of being separated into a fully distinct support-model module.

This means the current model layer is pragmatic:

- one shared place for many test-facing shapes
- some transport-facing models
- some support models
- some test-state models

## Dependencies

Important dependency relationships:

- tests depend directly on shared models
- clients return shared models
- fixture extensions store shared models in extension contexts
- UI pages and components assert against shared models
- Allure integration uses `model/allure/*`

Important observation:

- the model layer is shared across blocks more broadly than the API adaptation document suggests

## Typical flow

Typical model flow:

1. A client returns a shared model such as `UserJson` or `SpendJson`.
2. A fixture helper or extension may enrich it with `TestData`.
3. The model is stored in the test context or passed into a test method.
4. The test uses the same object for assertions and sometimes for further setup actions.

## Patterns used

- shared test-facing DTO layer
  One model package supports several adapters and test styles.
- model enrichment for fixture state
  Runtime DTOs may carry test-only metadata.
- support submodels inside the same overall model area
  Paging and Allure-specific shapes stay near the main model package.

## What is essential

- One visible place for shared test-facing models.
- Clear awareness when a model is enriched with test-only state.
- Stable DTOs for repeated assertions across adapters.

## What is optional

- Keeping paging and reporting support models in the same overall model area.
- Attaching test-only metadata to runtime DTOs rather than storing it separately.

## What looks overengineered

- The model layer is broad enough that its boundaries are no longer obvious at a glance.
- Test-only enrichment inside runtime DTOs is practical, but it blurs transport state and fixture state.

## Adaptation for my project

- Keep one shared model layer if it keeps the framework simpler.
- Be explicit about which models are:
  - transport-facing
  - test-facing
  - support/reporting-facing
- Consider separating test-only state from product-facing DTOs more strictly if the new framework is built from scratch.

## Readiness criteria

- The team can explain which models are shared across adapters.
- Test-only fixture state is not confused with product contract data by accident.
- The model layer remains understandable as it grows.

## Open questions

- The reference project uses a pragmatic mixed model layer.
- For adaptation, the main decision is whether to preserve this pragmatism or separate test-state models more sharply.
