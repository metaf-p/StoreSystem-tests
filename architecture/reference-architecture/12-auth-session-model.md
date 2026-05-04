# Auth and session model

## Goal

Describe how authentication, session state, cookies, auth codes, and token propagation work across the current test framework.

## Why it exists

Auth is one of the strongest cross-cutting concerns in this framework.

It affects:

- web tests
- REST tests
- GraphQL tests
- fixture creation
- parallel execution
- browser bootstrap

Yet the behavior is spread across several places:

- `ApiLoginExtension`
- `AuthApiClient`
- `ThreadSafeCookieStore`
- `CodeInterceptor`
- browser session hydration in web flows

## Main elements

- `AuthApiClient`
  Performs the multi-step login flow and returns an ID token.
- `ApiLoginExtension`
  Bridges auth into the JUnit lifecycle and stores token/code in the per-test method context.
- `ThreadSafeCookieStore`
  Stores cookies in a thread-local cookie jar for transport flows.
- `CodeInterceptor`
  Extracts OAuth-like authorization code values from redirect responses and stores them through `ApiLoginExtension`.
- `CookieStoreExtension`
  Clears thread-local cookie state after test execution.
- browser bootstrap in `ApiLoginExtension`
  For web tests, the extension opens the UI, injects local storage token and `JSESSIONID`, then navigates to the main page.

## Internal structure

The auth/session model is hybrid.

1. Login is API-driven
   Auth does not primarily happen through UI forms in the framework foundation.

2. The login flow is multi-step
   `AuthApiClient` first initiates authorization, then performs login, then exchanges the captured code for a token.

3. Session state uses two different storage styles
   - cookies live in `ThreadSafeCookieStore`
   - tokens and auth codes live in the method-scoped JUnit extension context

4. Web tests reuse API-acquired auth state
   `ApiLoginExtension` can take the acquired token and cookie state and hydrate the browser instead of forcing a UI login scenario.

5. Cleanup is split
   - cookie cleanup happens through a dedicated extension
   - token/code cleanup follows the test method context lifecycle

## Dependencies

Important dependency relationships:

- `AuthApiClient` depends on:
  - `AuthApi`
  - `ThreadSafeCookieStore`
  - `CodeInterceptor`
  - `OAuthUtils`
  - `Config`
- `ApiLoginExtension` depends on:
  - `AuthApiClient`
  - `UserExtension`
  - `ThreadSafeCookieStore`
  - `TestMethodContextExtension`
  - Selenide and browser infrastructure
- `CodeInterceptor` depends on `ApiLoginExtension` to persist the captured code
- `CookieStoreExtension` depends on the thread-local cookie storage model

This creates a distributed auth model rather than one single auth service object.

## Typical flow

Typical API login flow:

1. A test declares `@ApiLogin` or explicitly uses an auth-aware base path.
2. `ApiLoginExtension` determines which user should be logged in.
3. `AuthApiClient` starts authorization.
4. Redirect handling passes through `CodeInterceptor`, which extracts the code and stores it.
5. `AuthApiClient` performs the login request using cookie state from `ThreadSafeCookieStore`.
6. `AuthApiClient` exchanges the code for an ID token.
7. `ApiLoginExtension` stores the token in the current method context.

Typical web bootstrap flow:

1. The API login flow runs first.
2. `ApiLoginExtension` opens the front-end.
3. It writes `id_token` into local storage.
4. It injects `JSESSIONID` cookie into the browser.
5. The test starts from an already authenticated UI session.

## Patterns used

- API-driven authentication bootstrap
  The framework prefers acquiring auth state through API calls rather than through repeated UI login flows.
- split auth state storage
  Tokens and codes are method-scoped; cookies are thread-scoped.
- interceptor-assisted auth flow
  Transport interception is used to capture redirect-carried auth artifacts.
- auth-as-extension
  JUnit extensions turn auth from an imperative step into declarative test setup.
- browser hydration from API auth
  Web flows can reuse API-acquired auth state instead of logging in manually through the UI.

## What is essential

- One dedicated auth client path.
- One explicit place that stores auth token per test.
- One explicit place that stores request cookies per thread.
- Clear cleanup of cookie state.
- The ability to bootstrap authenticated UI state without copying login logic into page objects.

## What is optional

- OAuth-like code capture through an interceptor.
- Reusing API auth for browser hydration.
- Keeping token storage and cookie storage in two separate mechanisms.

## What looks overengineered

- The auth model is spread across clients, interceptors, extensions, thread-local storage, and browser bootstrap code.
- `CodeInterceptor` depends directly on extension-level storage through `ApiLoginExtension`, which creates cross-layer coupling.
- The current solution is effective, but it is harder to explain than a cleaner explicit `AuthContext` model would be.

## Adaptation for my project

- Keep auth as a first-class architectural concern.
- Prefer one explicit auth state object if the new project is built from scratch.
- Keep cookie or session state explicit and parallel-safe.
- If UI tests need authenticated bootstrap, decide early whether they use UI login or API-to-browser hydration.
- Avoid hiding auth acquisition inside generic request specs.

## Readiness criteria

- The team can explain where token state lives, where cookie state lives, and who cleans them up.
- Auth can be reused by several test styles without duplicating login logic.
- Web authentication is not implemented ad hoc inside page classes.
- Parallel execution does not mix auth/session state across tests.

## Open questions

- The current reference project uses a pragmatic but distributed auth model.
- For adaptation, it may be better to collapse code, token, cookie, and browser bootstrap dependencies into a more explicit session-context abstraction.
