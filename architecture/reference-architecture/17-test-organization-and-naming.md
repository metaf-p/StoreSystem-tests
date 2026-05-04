# Test organization and naming

## Goal

Describe how tests are organized in the current framework and what naming and package structure decisions are visible in the repository.

## Why it exists

The current documentation explains structure broadly, but the concrete organization of test packages and names is also part of framework ergonomics.

This matters because:

- package structure affects discoverability
- naming affects readability
- protocol coverage affects how tests are grouped
- not every package in the test tree has the same architectural status

## Main elements

- `test/web`
  UI scenario-oriented tests such as `LoginTest`, `ProfileTest`, `FriendsTest`, `SpendingTest`, and `RegistrationTest`.
- `test/rest`
  REST-focused tests such as `FriendsRestTest`, `FriendsV2RestTest`, `InvitationsRestTest`, and `UsersV2RestTest`.
- `test/gql`
  GraphQL tests plus `BaseGraphQlTest`.
- `test/grpc`
  gRPC tests plus `BaseGrpcTest`.
- `test/soap`
  SOAP tests plus `BaseSoapTest`.
- `test/fake`
  Non-mainline or experimental tests such as `JdbcTest` and `OAuthTest`.

## Internal structure

The main test organization rule is protocol-first package grouping.

1. UI scenarios are grouped under `web`
2. REST scenarios are grouped under `rest`
3. Other protocols have dedicated package roots
4. Heavier protocol setup is colocated with that protocol through `Base*Test` classes
5. Experimental or support-style tests are separated into `fake`

This gives the suite a clear browsing model:

- find tests by interface style first
- then find scenarios inside that interface style

## Naming observations

Visible naming patterns:

- UI tests are usually named by scenario area, for example `LoginTest`, `FriendsTest`, `SpendingTest`
- REST tests often include protocol or version in the class name, for example `FriendsRestTest` and `UsersV2RestTest`
- gRPC and SOAP tests often include service area plus protocol suffix
- base classes are named `BaseGraphQlTest`, `BaseGrpcTest`, `BaseSoapTest`
- `fake` package names suggest non-regression or exploratory purpose

## Dependencies

Organization depends on:

- protocol-specific family annotations and base classes
- shared model layer
- foundation extensions
- protocol runtime setup

The package structure does not create hard module boundaries, but it communicates intended usage and entry points.

## Typical flow

A newcomer reading the suite can usually navigate it like this:

1. Choose the interface style to inspect.
2. Open the corresponding `test/<protocol>` package.
3. Inspect the relevant `Base*Test` if the protocol is heavy.
4. Read scenario-specific test classes in that package.

## Patterns used

- protocol-first test taxonomy
- scenario naming inside protocol packages
- colocated protocol base classes
- explicit separation of exploratory or fake tests

## What is essential

- Keep the main package split readable by execution style or interface.
- Keep class names scenario- or capability-oriented, not helper-oriented.
- Separate active regression paths from exploratory or fake tests.

## What is optional

- Including protocol suffixes in every non-UI test class name.
- Keeping base classes in the same package as protocol tests.
- Using a `fake` package instead of another experimental label.

## What looks overengineered

- Nothing here is especially overengineered, but broad protocol coverage naturally increases package count.
- The `fake` package is useful for separation, but its exact architectural status is ambiguous without additional explanation.

## Adaptation for my project

- Choose one dominant grouping rule early.
- If the framework is API-first, keep API test organization especially clean and scalable.
- If UI is present, keep it readable by scenario rather than by widget or helper.
- Keep exploratory or support tests visibly outside the main regression path.

## Readiness criteria

- A newcomer can find the main test families quickly.
- Active regression paths are visually distinct from experiments.
- Naming makes it obvious which interface or scenario a test belongs to.

## Open questions

- The reference project suggests protocol-first organization works well here.
- For adaptation, the main open choice is whether domain-first grouping would be clearer once API coverage becomes the main testing surface.
