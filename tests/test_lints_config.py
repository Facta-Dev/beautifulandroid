#!/usr/bin/env python3
"""Contract tests: the drop-in detekt/ktlint config stays pinned."""

from __future__ import annotations

import unittest
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
DETEKT = REPO_ROOT / "lints" / "detekt.yml"
EDITORCONFIG = REPO_ROOT / "lints" / ".editorconfig"


class DetektConfig(unittest.TestCase):
    def setUp(self):
        self.text = DETEKT.read_text(encoding="utf-8")

    def test_coroutine_rules_are_pinned(self):
        for rule in ("GlobalCoroutineUsage", "RedundantSuspendModifier", "InjectDispatcher"):
            self.assertIn(rule, self.text)

    def test_complexity_and_bug_rules_are_pinned(self):
        for rule in ("LongMethod", "CognitiveComplexMethod", "NestedBlockDepth", "SwallowedException"):
            self.assertIn(rule, self.text)

    def test_zero_issue_budget(self):
        self.assertIn("maxIssues: 0", self.text)
        self.assertIn("warningsAsErrors: true", self.text)

    def test_no_blanket_disable(self):
        self.assertNotIn("active: false", self.text)


class EditorConfig(unittest.TestCase):
    def test_ktlint_settings_are_present(self):
        text = EDITORCONFIG.read_text(encoding="utf-8")
        self.assertIn("indent_size = 4", text)
        self.assertIn("max_line_length = 120", text)
        self.assertIn("ktlint_standard_no-wildcard-imports = enabled", text)


if __name__ == "__main__":
    unittest.main()
