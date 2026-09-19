# Easy Farmer's Delight 1.4.4 — Release QA

This document records the final source/release gates for 1.4.4. The feature set was manually reviewed in-game during
the development snapshots; the final release-prep pass focuses on non-functional formatting/logging/documentation,
version finalization and compatibility-boundary cleanup.

## Release gates

- Final mod version is exactly `1.4.4`.
- Java source has no tabs, trailing whitespace, wildcard imports, TODO/FIXME markers, repeated blank-line runs or
  direct `System.out`/`System.err`/`printStackTrace()` calls.
- Java source lines are kept at or below 120 characters.
- JSON resources parse successfully.
- Existing 1.4.4 compatibility, Cutter lighting, Grafting Support and Hearth & Harvest lifecycle regression scripts
  pass from a clean extraction.
- Source package excludes generated Gradle/build/run/IDE/cache artifacts, classes and JARs.
- ZIP CRC/integrity check passes.

## Compatibility boundary

Forge 1.20.1 retains the optional Delightful 3.8.x compatibility resources/metadata and the Fruits Delight 1.1.3
backport. Optional integrations remain registry/data-driven where possible and safe when the source mod is absent.

## Build note

The release source tree is validated statically here. A normal Forge Java 17 Gradle build remains the final local
packaging confirmation before upload.
