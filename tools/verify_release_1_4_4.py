#!/usr/bin/env python3
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def fail(message: str) -> None:
    raise SystemExit(f"FAIL: {message}")


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


props = read("gradle.properties")
if "mod_version=1.4.4" not in props:
    fail("gradle.properties must declare final mod_version=1.4.4")
if re.search(r"mod_version=.*(?:dev|snapshot|rc)", props, re.IGNORECASE):
    fail("development qualifier remains in mod_version")

java_files = sorted((ROOT / "src/main/java").rglob("*.java"))
if not java_files:
    fail("no Java sources found")

for path in java_files:
    text = path.read_text(encoding="utf-8")
    rel = path.relative_to(ROOT)
    if "\t" in text:
        fail(f"tab found in {rel}")
    if re.search(r"[ \t]+$", text, re.MULTILINE):
        fail(f"trailing whitespace found in {rel}")
    if "\n\n\n" in text:
        fail(f"repeated blank-line run found in {rel}")
    if re.search(r"^import\s+[^;]*\.\*;", text, re.MULTILINE):
        fail(f"wildcard import found in {rel}")
    if path.resolve() != Path(__file__).resolve() and re.search(r"\b(?:TODO|FIXME)\b", text):
        fail(f"development marker found in {rel}")
    if any(token in text for token in ("System.out", "System.err", "printStackTrace()")):
        fail(f"direct console/stacktrace logging found in {rel}")
    for line_no, line in enumerate(text.splitlines(), 1):
        if len(line) > 120:
            fail(f"Java line exceeds 120 chars: {rel}:{line_no} ({len(line)})")

python_files = sorted((ROOT / "tools").glob("*.py"))
for path in python_files:
    text = path.read_text(encoding="utf-8")
    rel = path.relative_to(ROOT)
    if "\t" in text:
        fail(f"tab found in {rel}")
    if re.search(r"[ \t]+$", text, re.MULTILINE):
        fail(f"trailing whitespace found in {rel}")
    if path.resolve() != Path(__file__).resolve() and re.search(r"\b(?:TODO|FIXME)\b", text):
        fail(f"development marker found in {rel}")
    for line_no, line in enumerate(text.splitlines(), 1):
        if len(line) > 120:
            fail(f"Python line exceeds 120 chars: {rel}:{line_no} ({len(line)})")

json_count = 0
for path in sorted((ROOT / "src/main/resources").rglob("*.json")):
    try:
        json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        fail(f"invalid JSON {path.relative_to(ROOT)}: {exc}")
    json_count += 1

for path in ROOT.rglob("*"):
    if not path.is_file():
        continue
    rel = path.relative_to(ROOT)
    forbidden_dirs = {".gradle", ".gradle-dist", "build", "run", ".idea", "node_modules", "__pycache__"}
    if any(part in forbidden_dirs for part in rel.parts):
        fail(f"generated/cache path present: {rel}")
    if path.suffix.lower() in {".class", ".jar", ".pyc"}:
        fail(f"generated/binary artifact present: {rel}")

readme = read("README.md")
changelog = read("docs/CHANGELOG.md")
development = read("docs/DEVELOPMENT.md")
if "Current release: **1.4.4**" not in readme:
    fail("README does not identify final 1.4.4 release")
if not changelog.startswith("# Changelog\n\n## 1.4.4 — 2026-09-19"):
    fail("CHANGELOG does not start with final 1.4.4 release")
if changelog.count("# Changelog") != 1:
    fail("CHANGELOG contains duplicate top-level headers")
if "1.4.4-dev" in changelog or "1.4.4-forge-dev" in changelog:
    fail("1.4.4 development snapshot text remains in final CHANGELOG")
if "## 21. 1.4.4 compatibility and corrections" not in development:
    fail("DEVELOPMENT lacks final 1.4.4 compatibility section")
if "DEV_1.4.0_ROADMAP" in development:
    fail("obsolete roadmap reference remains")

headings = [int(m.group(1)) for m in re.finditer(r"^## (\d+)\.", development, re.MULTILINE)]
if headings != list(range(1, 22)):
    fail(f"DEVELOPMENT numbered sections are not sequential 1..21: {headings}")

mods_toml = ROOT / "src/main/resources/META-INF/mods.toml"
if not mods_toml.exists():
    fail("Forge mods.toml missing")
mods_text = mods_toml.read_text(encoding="utf-8")
if 'modId = "delightful"' not in mods_text:
    fail("Forge release lost optional Delightful metadata")
for name in ("delightful_cantaloupe.json", "delightful_salmonberry.json"):
    path = ROOT / "src/main/resources/data/easyfarmersdelightcompat/efdc_regrowing_crops" / name
    if not path.exists():
        fail(f"Forge release lost Delightful resource {name}")
cf = read("docs/CURSEFORGE_CHANGELOG_1.4.4_FORGE.md")
if "Delightful 3.8.x" not in cf:
    fail("Forge CurseForge changelog must document Delightful 3.8.x")

print(f"PASS: Forge 1.4.4 release hygiene ({len(java_files)} Java, {json_count} JSON)")
