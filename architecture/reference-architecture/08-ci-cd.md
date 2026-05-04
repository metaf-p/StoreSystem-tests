# CI/CD

## Goal

Describe the CI/CD architecture that directly runs `niffler-e-2-e-tests`, including how the tests are triggered, executed in dockerized form, and connected to reports, artifacts, logs, and pull request feedback.

## Why it exists

`niffler-e-2-e-tests` is not run as a simple local unit-test task in CI. The module expects a coordinated runtime:

- application images must exist
- browser automation must run against a remote browser service
- the test module itself runs inside a dockerized environment
- reports, screenshots, and backend logs need to be collected after execution

The CI/CD layer exists to provide that environment and turn test outcomes into visible feedback on pull requests.

Evidence:

- `.github/workflows/e2e.yaml`
- `niffler-e-2-e-tests/Dockerfile`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/AllureDockerExtension.java`

## Main elements

- `.github/workflows/e2e.yaml`
  The only visible workflow directly dedicated to this test module.
- `niffler-e-2-e-tests/Dockerfile`
  Packages the test module into a runnable container that executes the test task in docker mode.
- Gradle test task configuration in `niffler-e-2-e-tests/build.gradle`
  Defines how tests start, including generated-source prerequisites.
- test-side CI-aware extensions and config
  `AllureDockerExtension`, `AllureBackendLogsExtension`, `BrowserExtension`, `ScreenShotTestExtension`, and `DockerConfig` show the runtime contract that CI must satisfy.
- docker-compose based runtime
  The workflow starts the environment with `docker compose up -d` and waits for the `niffler-e-2-e` container to exit.
- reporting hooks in test extensions
  `AllureDockerExtension` and `AllureBackendLogsExtension` turn CI-provided runtime context into report uploads and backend log attachments.
- artifact publishing
  Screenshot outputs are uploaded as workflow artifacts.
- PR notifications
  The workflow adds comments for build/test status and Allure report links.

## Internal structure

The CI/CD path relevant to this module is structured as one GitHub Actions workflow with one main job.

1. Trigger
   The workflow runs on pull request events: opened, synchronized, and reopened.

2. Build phase
   CI checks out the PR SHA, installs JDK 21, and builds docker images for the application stack.
   The build step explicitly skips running `:niffler-e-2-e-tests:test` during image build.

3. Runtime preparation
   CI:
   - pulls the required remote browser image
   - exports reporting-related environment variables
   - prepares screenshot and backend log directories
   - captures the last commit message for report metadata

4. Test execution
   CI starts the docker-compose environment and waits for the dedicated E2E test container to complete.

5. Post-execution publication
   CI uploads screenshots, publishes JUnit XML results, and comments on the pull request with status and report links.

Architecturally, this is not a staged “build then run tests in the same JVM” setup. It is a container-orchestrated integration run wrapped by a single GitHub Actions job.

There is also no visible CI split by test type inside this workflow. The pipeline does not show separate smoke/regression/ui/api jobs, and the test container is run as one broad E2E bucket.

## Dependencies

Direct dependencies between CI/CD and the test module:

- `niffler-e-2-e-tests/Dockerfile` sets `-Dtest.env=docker` and `-Drepository=jpa`, which shapes how the test framework behaves in CI
- `BrowserExtension` switches to remote browser mode when `test.env=docker`
- `DockerConfig` changes screenshot directories and report endpoint behavior in docker mode
- `ScreenShotTestExtension` expects a writable `.screen-output/...` directory when baseline rewriting/diff output is needed
- `AllureDockerExtension` expects:
  - a docker-mode run
  - Allure result files under `build/allure-results`
  - CI-provided metadata such as execution source and commit message
- `AllureBackendLogsExtension` expects backend log files in a mounted docker-visible log path
- `GhApiClient` depends on a GitHub token env var for issue-related conditional logic used by the test foundation

External dependencies that matter architecturally:

- GitHub Actions
- Docker and docker compose
- remote browser infrastructure compatible with Selenide remote mode
- Allure Docker service or equivalent hosted report receiver
- GitHub PR comment APIs through `actions/github-script`
- JUnit XML reporting through `dorny/test-reporter`

Evidence:

- `.github/workflows/e2e.yaml`
- `niffler-e-2-e-tests/Dockerfile`
- `niffler-e-2-e-tests/build.gradle`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/AllureDockerExtension.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/AllureBackendLogsExtension.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/jupiter/extension/BrowserExtension.java`
- `niffler-e-2-e-tests/src/test/java/guru/qa/niffler/config/DockerConfig.java`

## Typical flow

Typical CI flow for these tests:

1. A pull request event triggers `.github/workflows/e2e.yaml`.
2. CI checks out the PR head commit and sets up Java.
3. CI builds the backend/application images needed for the environment.
4. CI publishes unit/integration JUnit reports from the build phase and comments the result on the PR.
5. CI prepares the browser image, screenshot directory, and backend log directories.
6. CI starts the docker-compose environment and waits for the `niffler-e-2-e` container to finish.
7. The test container runs `gradle test` in docker mode.
8. During the run:
   - browser tests use remote browser execution
   - Allure results are generated locally in the container filesystem
   - suite extensions collect backend logs and publish Allure results if configured
9. CI uploads screenshot artifacts and comments a report link back to the PR.

Evidence:

- `.github/workflows/e2e.yaml`
- `niffler-e-2-e-tests/Dockerfile`
- `AllureDockerExtension.java`

## Patterns used

- Pull-request gated integration workflow
  E2E runs are tied to PR lifecycle rather than every possible git event.
- Single-job orchestration
  Build, environment bootstrap, execution, artifact publication, and PR feedback all live in one workflow job.
- Containerized test execution
  The test module runs in its own container instead of directly on the GitHub runner JVM.
- Environment-by-contract
  CI prepares exactly the directories and env vars the test module expects for screenshots, logs, and report metadata.
- Feedback-rich post-processing
  The workflow does more than pass/fail:
  - JUnit report publishing
  - screenshot artifact upload
  - PR comments
  - external Allure report generation

## What is essential

- One workflow that triggers on PRs.
- A deterministic way to start the application-under-test plus the test runner.
- A clear pass/fail signal based on the dedicated test container exit code.
- Basic artifact or report publication so failures can be diagnosed.
- Environment preparation that matches what the test framework actually expects.

## What is optional

- PR comments for both build and E2E outcomes
  Helpful, but not strictly necessary for a smaller setup.
- keeping build-phase reporting and E2E reporting in the same workflow
  Useful for one-screen visibility, but not required for a minimal training pipeline.
- Hosted Allure report generation
  Useful for richer history and debugging, but optional in a minimal training project.
- Separate screenshot artifact upload
  Valuable for visual failures, but optional if screenshots are already available in another report system.
- Backend log aggregation as a pseudo-test case in Allure
  Nice for diagnosis, but not required for the core pipeline.
- Pre-pulling a specific browser image
  Helpful for reproducibility, but can be simplified depending on the environment.

## What looks overengineered

- Rich reporting surface for a training project
  JUnit publishing, Allure upload, backend log attachments, screenshot artifacts, and PR comments create excellent observability, but a smaller project usually does not need all of them at once.
- Tight CI-to-test-runtime coupling
  The test module expects specific env vars, directories, and mounted paths. That works well here, but increases portability cost.
- Single workflow job doing many concerns
  The current flow is manageable, but build reporting and E2E orchestration are bundled together rather than separated into clearer jobs.
- External report hosting dependence
  Hosted Allure links are useful, but they also add service dependency and CI metadata plumbing.

## Adaptation for my project

- Start with one simple PR-triggered workflow.
- Keep the test runtime contract small.
  Only pass the env vars and directories the tests truly need.
- Use containerized execution only if the test framework genuinely depends on multiple services or remote browser infrastructure.
- Begin with one report surface.
  For a smaller project, JUnit XML or one artifact upload may be enough before adding external Allure.
- Add screenshots and backend log collection only after basic E2E stability is achieved.
- If you split workflows later, consider separating:
  - build/application image preparation
  - E2E execution
  - optional report publication

## Readiness criteria

- The team can explain what event triggers the E2E run.
- The pipeline has one clear place where the environment is started.
- The test runner’s success/failure is tied to an explicit container or command exit code.
- At least one diagnostic output is preserved for failures.
- CI runtime assumptions for the test module are documented and intentionally maintained.
- There is no accidental dependence on unrelated repository pipeline logic.

## Open questions

- Hypothesis: the docker-compose definition and service/container naming conventions are crucial to this flow, but they are outside the current scope files and therefore only partially visible from the test module side.
- The workflow currently reports both general build/test status and E2E status back to the PR. In a smaller adaptation, one combined signal may be enough, but it is unclear whether the team intentionally values that split for triage speed.
- There is no visible smoke/regression layering or tag-based selection for this module in CI. That may be intentional simplicity, or it may mean the suite is still run as one broad bucket rather than as distinct pipeline slices.
