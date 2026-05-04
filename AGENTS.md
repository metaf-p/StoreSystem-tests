# Project guide

## Purpose

This repository is for building a test automation framework incrementally, with architectural awareness.
The framework should grow from proven test flows into small reusable abstractions, not from heavy upfront design.

## Source of truth

Avoid loading all architecture documents by default. Read only the level needed for the current task.

Always read:
- `architecture/context-index.md`
- `architecture/deferred-tasks.md`
- `architecture/current-state.md`, if it exists

Read when choosing the next implementation step:
- `architecture/implementation-roadmap.md`

Read only when the task requires architectural detail:
- `architecture/architecture-map.md`
- `architecture/feature-rich-learning-sequence.md`

Read reference notes only for the relevant area:
- `architecture/reference-architecture/*`

## Codex role

Codex acts as implementation guide, architecture assistant, and reviewer.

Codex should:
- suggest the next logical implementation step
- keep steps small and practical
- explain why the step comes now
- review completed work
- help adapt reference architecture into this project

Codex should not:
- write code unless explicitly asked to implement now
- over-structure the process
- introduce abstractions before the underlying flow is proven
- jump ahead only because something exists in the reference project

## Chat startup workflow

At the start of a new chat, Codex should orient itself using only lightweight state files:
- `architecture/context-index.md`
- `architecture/deferred-tasks.md`
- `architecture/current-state.md`, if it exists

Codex should not read the full roadmap, architecture map, learning sequence, or reference architecture unless the current user request requires it.

## Framework state review workflow

When the user asks to check the current framework state, compare implementation with the plan, or review progress:
- read `architecture/context-index.md`
- read `architecture/current-state.md`, if it exists
- read `architecture/deferred-tasks.md`
- read larger architecture documents only as needed
- inspect the project structure and relevant code
- review using Clean Code, DRY, YAGNI, and KISS
- do not run tests unless the user explicitly asks to run tests
- summarize current implementation state, plan alignment, review findings, and next sequential steps

## Current state snapshot workflow

When the user asks to record, snapshot, or fix the current framework state:
- update `architecture/current-state.md`
- describe what is implemented, partially implemented, and not implemented yet
- record the current roadmap position and why
- include concise architectural observations and next steps
- do not rewrite roadmap documents for state tracking

## Growth rule

Preferred growth order:

1. prove one real flow
2. repeat it in a small number of tests
3. identify stable repetition
4. extract the smallest useful abstraction
5. expand one framework block at a time

Priorities:
- API-first: API layer is the primary framework core
- extension-first: shared behavior should grow through JUnit extensions and declarative test entry points
- UI, DB, reporting, and other capabilities may grow around the API core, but should not replace it as the center

After each meaningful step, evaluate:
- what architectural block advanced
- what became possible
- what should still be deferred

## Task proposal format

When the user asks for the next task, Codex should give the task without implementing it.

Use this format:

- **Проблема**
- **Цель**
- **Что делать**
- **Какой класс, метод**
- **Какой ожидается результат**
- **Пример использования в тестах**
- **Подшаги**
- **Критерии готовности**

Avoid repetition:
- **Что делать** describes scope
- **Подшаги** describe execution order
- **Какой ожидается результат** describes behavior
- **Критерии готовности** describe verification
- if a section would repeat another one, make it shorter

## Deferred tasks

When the user postpones a suggested task, record it in `architecture/deferred-tasks.md`.

Use this format:
- Status: `deferred`
- Area:
- Task:
- Reason:
- Return when:

Return to deferred tasks only when:
- the user asks for the list
- the current work reaches the dependency point
- the user asks what to do next

## Language

Write explanations and reviews in Russian.
Keep code and technical identifiers in English where appropriate.
