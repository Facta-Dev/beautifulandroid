#!/usr/bin/env bash
# check-lints.sh — prove the drop-in detekt config fires.
# The clean control must stay quiet; the violation harness must fail.
# A missing detekt is a visible SKIP, never a silent pass; CI installs it.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if ! command -v detekt >/dev/null 2>&1; then
  echo "check-lints: SKIP detekt — not installed (CI runs it)"
  exit 0
fi

if detekt --config lints/detekt.yml --input lints/check/clean.kt >/tmp/detekt-clean.log 2>&1; then
  echo "check-lints: clean control is quiet"
else
  cat /tmp/detekt-clean.log >&2
  echo "check-lints: FAIL — the clean control reported a finding" >&2
  exit 1
fi

if detekt --config lints/detekt.yml --input lints/check/violations.kt >/tmp/detekt-violations.log 2>&1; then
  echo "check-lints: FAIL — the violation harness stayed quiet" >&2
  exit 1
fi

echo "check-lints: violation harness fired; OK"
