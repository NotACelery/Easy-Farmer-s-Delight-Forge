# Easy Farmer's Delight 1.4.3-dev.5 — development notes

## Scope

This snapshot reconstructs the lost dev.5 crop-compatibility work on top of 1.4.3-dev.4.

### Generic support added

- Rich Farmer fallback for `BlockItem` / `ItemNameBlockItem` planting items whose target block is a `CropBlock`.
- Generic `SweetBerryBushBlock` regrowing route using the crop block loot table at maturity and resetting to age 1.
- Generic `StemBlock` route for planting items that point directly to a stem; vanilla-style single-fruit stems resolve their fruit through the StemBlock fruit field.
- Data-driven `efdc_stem_crops` definitions for stems with non-standard fruit behavior.
- Exact planting-item registry IDs are persisted for tooltip and dismantle return behavior.

### Built-in compatibility definitions

- `deep_aether:squash_seeds` -> `deep_aether:squash_stem`, with Blue/Green Squash fruit choices.
- Hearth & Harvest Cotton as a regrowing crop (age 7 harvest, reset to age 5).
- Regions Unexplored Salmonberry as a regrowing crop (age 3 harvest, reset to age 1).

### Covered automatically by generic behavior

- Eternal Starlight Pungency Fruit.
- Eternal Starlight Crinoa.
- Hearth & Harvest Peanut.
- Hearth & Harvest Blueberry/Raspberry bushes, including their distinct registry-backed planting items.
- Other compatible modded CropBlock/SweetBerryBushBlock/standard StemBlock crops.

### Audited but intentionally not forced into generic handling

- Hearth & Harvest Corn: custom multi-section crop.
- Hearth & Harvest Grapes: trellis/vine structure.
- Hearth & Harvest Sunflower Seeds: directly place a two-block vanilla sunflower rather than a harvestable crop lifecycle.
- Eternal Starlight Nocturnal Millet: custom bottom stalk + top panicle multi-block lifecycle.

## Validation status

Source and JSON structure were statically checked in the reconstruction environment. A full Gradle compile could not be run there because the source-only snapshots intentionally contain no Gradle distribution/wrapper and the environment could not download one. Compile/runtime QA is therefore still pending.
