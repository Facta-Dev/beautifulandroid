#!/usr/bin/env bash
# check-build.sh — compile and test the Kotlin example.
# A missing Gradle is a visible SKIP, never a silent pass; CI installs it.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if ! command -v gradle >/dev/null 2>&1; then
  echo "check-build: SKIP — gradle not found (CI runs it)"
  exit 0
fi

gradle -p examples/minimal test detekt --no-daemon
echo "check-build: OK"
