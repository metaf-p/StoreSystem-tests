# Legacy and experimental mechanisms

## Goal

Describe the parts of the current framework that appear non-mainline, deprecated, exploratory, or operationally secondary.

## Why it exists

A reference framework often contains useful ideas mixed with old or experimental mechanisms.

Without calling those out explicitly, adaptation risks copying:

- transitional patterns
- deprecated extensions
- experimental tests
- convenience automation that is not part of the architectural backbone

## Main elements

- `UsersQueueExtension`
  Marked `@Deprecated`; uses preallocated static user queues.
- `ClientResolver`
  Field-injection extension used only in disabled fake tests.
- `test/fake/*`
  Experimental or support tests rather than the visible main regression path.
- `IssueExtension` and `@DisabledByIssue`
  Useful operational automation, but not foundational to the core test architecture.

## Internal structure

These mechanisms sit outside the main architectural path.

### Legacy concurrency-aware fixture sharing

`UsersQueueExtension` represents an older model:

- static shared user pools
- queue leasing
- explicit return of users to the pool

This contrasts with the current preferred model:

- generated per-test fixtures
- extension-context storage
- thread-local isolation

### Experimental injection path

`ClientResolver` uses field injection rather than the now-dominant annotation plus extension model.

This suggests an earlier or alternate style that is not the main direction anymore.

### Fake tests

The `fake` package appears to contain tests used for:

- experiments
- validation of infrastructure ideas
- demonstration of technical mechanics

rather than core regression coverage.

## Dependencies

These mechanisms depend on the same foundation and client layers, but they should not be read as defining the main framework shape.

## Typical flow

Typical misreading risk:

1. A reader finds `UsersQueueExtension` or `ClientResolver`.
2. They assume the framework uses shared static users or field injection as a normal model.
3. They copy a non-mainline mechanism into a new project.

This document exists to prevent that.

## Patterns used

- deprecated mechanism kept for historical or exploratory value
- operational automation outside the architectural core
- explicit separation of fake tests from mainline tests

## What is essential

- Understand which mechanisms are active mainline architecture and which are not.
- Prefer the current dominant model over older transitional utilities.
- Keep experiments visibly separated from core framework paths.

## What is optional

- Issue-based disabling through external GitHub state.
- Keeping deprecated utilities in the repository for teaching or reference.
- A `fake` test package as a learning sandbox.

## What looks overengineered

- Queue-based shared user leasing is heavier and less robust than generated per-test fixtures.
- Field injection via `ClientResolver` is harder to reason about than explicit JUnit extension or parameter injection.
- External GitHub issue status checks are useful, but can widen the foundation more than many projects need.

## Adaptation for my project

- Do not treat deprecated or exploratory mechanisms as the default architecture.
- If the project keeps experimental code paths, label them clearly.
- Prefer generated fixtures and explicit extension-driven setup over shared static user pools.
- Only add issue-based disabling if operational workflow truly requires it.

## Readiness criteria

- The team can identify which mechanisms are mainline and which are not.
- Deprecated or exploratory code is not copied blindly into the new architecture.
- Experimental tests are visibly separated from active regression design.

## Open questions

- The reference project retains some historical teaching value in these mechanisms.
- For adaptation, the key decision is whether that value belongs in the main framework repository at all.
