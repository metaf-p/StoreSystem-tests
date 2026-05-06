# Deferred tasks

This file stores tasks that were intentionally postponed during iterative framework development.

Use this backlog to avoid losing useful ideas while keeping the current implementation step small.

## Workflow

When a task is postponed:
- add it under the relevant area
- keep the wording short and practical
- include why it was postponed
- include when it should be reconsidered

When reviewing this backlog:
- keep tasks that are still useful but premature
- update tasks whose dependency changed
- remove or mark obsolete tasks that no longer fit the framework direction

## Task format

```md
### <Task title>

- Status: deferred
- Area:
- Task:
- Reason:
- Return when:
```

## Backlog

### Strengthen page object identity checks

- Status: deferred
- Area: UI page objects
- Task: strengthen `shouldBeOpened()` checks by verifying page-specific key elements, not only the common page title locator.
- Reason: current page objects are still minimal, and the existing title checks are enough for the current UI baseline while other cleanup/refinement work is in progress.
- Return when: adding or expanding UI flows, touching page objects again, or when page identity checks become flaky or ambiguous.

### Review fixture cleanup under parallel execution

- Status: deferred
- Area: Test foundation / fixture cleanup
- Task: review whether `UserCleanup` and `SupplierCleanup` remain isolated and reliable when tests run in parallel.
- Reason: targeted framework cleanup now exists for fixture-created users and suppliers, but parallel execution is not enabled or validated yet.
- Return when: enabling JUnit parallel execution, observing cleanup-related flakiness, or adding cleanup for more product-service entities.

### Refactor UI tests to declarative user fixtures

- Status: deferred
- Area: UI tests / test foundation
- Task: align UI tests with the current `@TestUser` / `@CurrentUser` model, including API-authenticated UI entry through `UiAuthBridge`.
- Reason: API user tests were refactored first, while UI tests are intentionally left on the old setup shape during the current supplier-flow work.
- Return when: touching UI auth tests, expanding UI coverage, or preparing UI tests for CI execution.
