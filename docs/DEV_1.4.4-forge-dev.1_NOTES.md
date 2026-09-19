# Easy Farmer's Delight 1.4.4-dev.1 — Forge 1.20.1

## Scope

This snapshot starts the Forge-specific 1.4.4 compatibility pass. Its gameplay change is intentionally narrow: add Delightful Cantaloupe support where Delightful actually exists (Minecraft 1.20.1 / Forge).

## Delightful 3.8.1 audit

The supplied `Delightful-1.20.1-3.8.1.jar` confirms:

- mod id: `delightful`;
- Forge dependency: `47+`;
- Farmer's Delight dependency: `1.20.1-1.3.2+`;
- seed item: `delightful:cantaloupe_seeds`;
- crop block: `delightful:cantaloupe_plant`;
- crop age property: integer `age`, values `0..3`;
- mature right-click harvest: one `delightful:cantaloupe`;
- native post-harvest state: age `0`;
- breaking the plant is a different action that returns seeds and, when mature, Cantaloupe. EFD models the native right-click/regrowing harvest instead of destructive breaking.

## EFD implementation

The integration is a normal `efdc_regrowing_crops` definition. No Delightful classes are imported or linked. Missing registry entries make the definition disappear safely when Delightful is not installed.

The JEI/EMI viewer fallback explicitly loads the bundled Cantaloupe definition so its guide remains discoverable during viewer initialization.

## Expected QA

1. Build on JDK 17 / Forge 47.4.x.
2. Launch once without Delightful and verify EFD loads normally.
3. Launch with Delightful 3.8.1.
4. Insert Cantaloupe Seeds into an empty Rich Farmer and confirm the rendered plant starts at age 0.
5. Confirm growth reaches age 3.
6. Confirm harvest produces exactly one Cantaloupe and resets the rendered plant to age 0 without consuming another seed.
7. Confirm Rich Soil accelerates growth but does not increase harvest count.
8. Confirm JEI/EMI Block Guide shows the Cantaloupe regrowing lifecycle when Delightful is installed.
