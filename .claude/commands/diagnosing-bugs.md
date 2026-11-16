---
name: diagnosing-bugs
description: "Delegate to the diagnosing-bugs skill: build a red-capable feedback loop for a hard <app> bug, minimise it, rank falsifiable hypotheses, instrument one variable at a time and fix the root cause."
division: testing
---

# /diagnosing-bugs

## Delegate

Delegate to the **diagnosing-bugs** skill. Activate when something in `<app>`
is broken and the cause is not visible yet: a wrong value, a test that fails
for no clear reason, a screen that hangs, a request that takes too long, or a
regression between two known states.

Steps:

1. Build the feedback loop first: one command that goes red on this exact
   symptom, run it once, show the invocation and the redacted output.
2. Reproduce it, then minimise it, cutting one element at a time until every
   remaining element is load-bearing.
3. Write three to five ranked hypotheses, each with the prediction it makes,
   and show them before testing.
4. Instrument one variable per probe, tagging every temporary log with a
   unique prefix.
5. Write the regression test at a seam that exercises the real bug pattern,
   apply the fix, and re-run the original un-minimised scenario.
6. Remove the tagged instrumentation, delete the throwaway harnesses, and
   state the confirmed cause.
