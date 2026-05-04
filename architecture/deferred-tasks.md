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

### Introduce declarative test user fixture annotation

- Status: deferred
- Area: Test foundation / user fixtures
- Task: finish a method-level `@TestUser` annotation with a `role()` attribute, handled by a dedicated `UserExtension` that creates the user before the test, resolves `AuthContext`, and performs cleanup automatically.
- Reason: `ApiClients` is already separated from `ApiClientExtension`, but user creation is still implicit through method parameters and cleanup is still passed around manually.
- Return when: replacing repeated `AuthContext user` setup in API tests or when the first role-based user flow needs a declarative entry point.
