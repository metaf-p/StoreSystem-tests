# UI layer

## Goal

Describe how `niffler-e-2-e-tests` structures its UI automation layer so web tests stay readable, reusable, and mostly isolated from raw selector and browser details.

## Why it exists

The web tests in this module are meant to read like user scenarios, not like low-level Selenium scripts. To achieve that, the project separates:

- page-level navigation and entry points
- reusable UI fragments such as header, tables, search, and charts
- UI actions
- UI assertions
- browser lifecycle and screenshot support

Evidence:

- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/page/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/page/component/*`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/test/web/*`

## Main elements

- `page/*`
  Page objects for top-level screens such as `LoginPage`, `MainPage`, `ProfilePage`, `FriendsPage`, `PeoplePage`, `RegisterPage`, and `EditSpendingPage`.
- `page/component/*`
  Reusable UI fragments and sub-objects such as `Header`, `SpendingTable`, `SearchField`, `StatComponent`, `Calendar`, and `SelectField`.
- `BasePage`
  Shared page-level assertions for alerts and form errors.
- `BaseComponent`
  Minimal wrapper for component root elements.
- `condition/*`
  Custom Selenide conditions for advanced UI assertions, especially visual/statistic checks.
- `BrowserExtension`
  Browser setup, teardown, failure screenshot capture, and Selenide listener registration.
- `ApiLoginExtension`
  UI-relevant login bootstrap that injects authenticated browser state before web tests.
- `ScreenShotTestExtension`
  Loads expected baseline images and attaches visual diffs when comparison fails.

## Internal structure

The UI layer is organized in four levels.

1. Web tests express the scenario
   Tests in `test/web/*` describe the business action flow and delegate UI work to pages and components.

2. Pages represent top-level screens or route-level forms
   Examples:
   - `LoginPage` and `RegisterPage` for authentication screens
   - `MainPage` as a composition root for the main app screen
   - `ProfilePage`, `FriendsPage`, `PeoplePage`, and `EditSpendingPage` for domain-facing routes

3. Components encapsulate reusable fragments inside pages
   Examples:
   - `Header` handles top-level navigation and menu flows
   - `SpendingTable` encapsulates table filtering, edit, delete, and presence checks
   - `SearchField` hides search input cleanup and submission
   - `StatComponent` hides chart and legend assertions
   - `Calendar` and `SelectField` wrap awkward widgets

4. Support extensions handle browser/session concerns outside page objects
   Browser management, login bootstrapping, and screenshot comparison are intentionally kept out of the page package.

This gives the UI layer a clean split:

- pages compose components and page-specific selectors
- components own repeated widget behavior
- extensions own browser and session mechanics
- tests orchestrate scenarios

## Dependencies

The UI layer depends on:

- Selenide for element access, waits, and browser interaction
- `Config` for route construction and environment-aware URLs
- test foundation extensions such as `BrowserExtension` and `ApiLoginExtension`
- data generation via `@User`, `@Category`, and `@Spending` when UI tests need prepared state
- custom condition classes for image and chart assertions

Important dependency relationships:

- `BasePage` exposes shared UI assertions used across pages
- `MainPage` composes `Header`, `SpendingTable`, and `StatComponent`
- `EditSpendingPage` depends on widget components `Calendar` and `SelectField`
- `ProfilePage` and `StatComponent` depend on `ScreenshotConditions.image(...)`
- web tests depend on `@WebTest`, which activates browser setup and fixture extensions
- `ApiLoginExtension` can bypass UI login by seeding local storage and cookies directly, then opening `MainPage`

## Typical flow

A typical UI test flow in this project is:

1. `@WebTest` starts browser-related support and fixture extensions.
2. `@User` prepares any needed data.
3. `@ApiLogin` optionally bootstraps an authenticated browser session without going through the login form.
4. The test opens a page or starts from `MainPage`.
5. The test navigates using page methods and component methods.
6. Assertions are performed inside page/component methods rather than directly in the test body.

Examples:

- `LoginTest.java`
  Opens `LoginPage`, fills credentials, submits, and checks `MainPage` loaded.
- `FriendsTest.java`
  Starts from `MainPage`, uses `Header` navigation, then asserts or mutates state through `FriendsPage` and `PeoplePage`.
- `ProfileTest.java` and `SpendingTest.java`
  Use screenshot-based assertions via `@ScreenShotTest` and custom image conditions.

## Patterns used

- Page object pattern
  Top-level routes are represented by page classes with encapsulated selectors and scenario-relevant methods.
- Component object pattern
  Repeated fragments are modeled as reusable components rather than duplicated across pages.
- Composition over deep inheritance
  `MainPage` and `EditSpendingPage` compose components rather than inheriting behavior trees.
- Fluent action methods
  Most actions return `this` or the next page object, which keeps tests chainable and concise.
- Assertions live near selectors
  Methods like `checkExistingFriends`, `checkPhoto`, and `checkTableContains` keep selector knowledge close to verification logic.
- Support-driven session bootstrap
  Authentication is injected by an extension, so most web tests start already logged in.
- Custom visual conditions
  The project extends Selenide conditions for image and chart validation rather than keeping visual comparison logic inside tests.

## What is essential

- A small page-object layer
  This is the core reusable idea.
- A component split for repeated widgets
  `Header`, search, and table components are high-value abstractions.
- Shared alert/error assertions
  `BasePage` is a practical minimal base.
- External browser/session management
  Keeping browser lifecycle and login bootstrapping out of pages makes the UI layer cleaner.
- One or two stability defaults
  Central browser timeout and page load behavior help prevent drift across tests.

## What is optional

- Visual regression support through baseline screenshots
  Useful, but not required for every UI test project.
- Custom Selenide conditions for charts and images
  Helpful when the product has meaningful visual output; optional in a smaller suite.
- Dedicated widget objects like `Calendar` and `SelectField`
  Worth it only when those widgets are reused enough to justify abstraction.
- Fluent chaining everywhere
  Convenient, but not a hard requirement.
- Direct authenticated browser bootstrap
  Efficient for test speed, but some teams prefer logging in through the UI for a subset of tests.

## What looks overengineered

- Visual comparison stack for a small web suite
  `@ScreenShotTest`, screenshot baselines, diff attachments, and custom image conditions add useful capability, but they are heavier than a minimal training project usually needs.
- Custom chart bubble conditions
  Powerful and readable once in place, but more elaborate than simple text assertions.
- Manual sleeps in `Calendar`
  The component abstraction is good, but the implementation relies on repeated `Selenide.sleep(200)`, which suggests the widget is brittle and the synchronization strategy could be cleaner.
- Some page/component boundaries are pragmatic rather than strict
  For example, pages still own many concrete selectors and checks directly, while components cover only the most repeated fragments. That is not wrong, but it shows the layer evolved incrementally rather than from one strict rule.

## Adaptation for my project

- Start with a light page-object layer and one base page.
- Extract components only for repeated fragments.
  Header, search, modal, and table widgets are usually the first good candidates.
- Keep navigation helpers in components if they are global UI elements.
  `Header` is a strong example of this.
- Put assertions in page/component methods when they are tied to stable selectors.
- Keep browser and login bootstrapping outside the page classes.
- Add screenshot-based checks only if visual correctness is a real regression risk.
- Avoid copying brittle synchronization patterns.
  If you need a calendar wrapper, prefer explicit wait conditions over hard sleeps where possible.

## Readiness criteria

- Web tests read like scenarios rather than selector scripts.
- Top-level routes are modeled as pages and repeated fragments as components.
- Navigation, actions, and assertions are encapsulated in the UI layer, not duplicated in tests.
- Browser/session setup is centralized and not scattered through page classes.
- The team can explain where to add:
  - a new route
  - a reusable widget
  - a shared UI assertion
  - a browser-level stability rule

## Open questions

- Hypothesis: `MainPage` acts as the main composition root for authenticated UI tests, while some route pages still keep more direct selector logic because the UI abstraction was expanded incrementally rather than designed fully upfront.
- The current `Calendar` implementation suggests synchronization pain around the date picker. In another project, it would be worth checking whether the widget can be driven more deterministically before copying that pattern.
- The suite relies heavily on API-assisted login for authenticated web scenarios. That is efficient, but it is not fully clear whether the team intentionally treats UI login as a separate concern or simply optimized most flows away from the login form.
