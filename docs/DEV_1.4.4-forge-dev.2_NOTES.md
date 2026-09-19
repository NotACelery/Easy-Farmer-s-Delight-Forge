# Easy Farmer's Delight 1.4.4-dev.2 — Forge 1.20.1

## Purpose

This snapshot corrects the scope omission in Forge `1.4.4-dev.1`. Dev.1 added Delightful Cantaloupe but did not
backport the Fruits Delight compatibility wave already implemented on the NeoForge branch. Dev.2 ports that system
to Forge 1.20.1 and keeps the Cantaloupe integration intact.

## Fruits Delight 1.1.3

- Productive Orchards: Pear, Hawberry, Lychee, Mango, Persimmon, Peach, Orange, Apple, Mangosteen, Bayberry, Kiwi, Fig.
- Durian: standalone decorative canopy only; hard-excluded from productive Orchard definitions and Rich Farmer selection.
- Regrowing crops: Blueberry, Cranberry, Pineapple.
- Tall crop: Lemon (`upper_from_age = 2`).
- Stem crop: Hamimelon.
- Orchard definitions support arbitrary serialized property stages while legacy numeric-age definitions remain valid.
- Grafting/Rich Farmer rendering iterates the source model's render layers to preserve multipart/mixed-layer models.
- Manual canopy breaking clears the stored support state even without Shears; Shears/Silk Touch only recover the leaf item.

## Delightful

The `1.4.4-dev.1` Forge-only Cantaloupe integration remains unchanged and available when Delightful 3.8.x is installed.

## Validation state

Static source/resource verification is required for this snapshot. A Windows ForgeGradle/JDK 17 build and in-game QA
remain required before declaring the snapshot build-clean.
