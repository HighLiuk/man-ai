---
name: fix-bug
description: Fix a bug using strict TDD. Use when fixing bugs, resolving issues, or when the user reports something broken.
---

# Fix Bug via TDD

For EACH bug fix, follow this cycle internally. Do NOT combine steps.

## For each fix:

### 1. RED — Write the failing test

- Write a test that reproduces the bug
- Choose the right test level:
  - Logic bug → unit test with `cd android && ./gradlew test`
  - Device/permission/UI bug → instrumented test with `cd android && ./gradlew connectedDebugAndroidTest`
- Verify the test FAILS. If it passes, the test is wrong — rewrite it.
- Log the failure output.

### 2. GREEN — Minimal fix

- Write the MINIMUM production code to make the test pass
- Run the SAME test command you used in step 1 (unit or instrumented)
- ALL tests must pass. If not, fix until green.

### 3. REFACTOR (optional)

- Clean up while keeping tests green

## Rules

- NEVER write production code before its test exists and has been shown RED
- Each task in the plan = one RED-GREEN-REFACTOR cycle
- If you realize you wrote production code first, DELETE IT, write the test,
  verify RED, then rewrite the production code
