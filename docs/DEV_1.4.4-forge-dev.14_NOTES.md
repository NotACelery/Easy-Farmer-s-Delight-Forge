# Easy Farmer's Delight 1.4.4-forge-dev.14 — Hearth & Harvest Corn lifecycle

## Scope

Fixes the Rich Farmer handling of `hearthandharvest:corn_stalk`. Corn is no longer treated as a normal one-block CropBlock lifecycle.

## Corn behavior

- Keeps the bottom stalk planted.
- Reconstructs Hearth & Harvest's progressive `bottom -> middle -> top` growth inside the Rich Farmer.
- Stores the middle/top ages independently so all three sections can continue growing after the top reaches its unlock stage.
- A section becomes harvestable at age 4.
- Age 4 yields 1 Corn; age 5 yields 2 Corn.
- Harvested sections return independently to age 3 instead of uprooting/replanting the whole stalk.
- Existing saves produced by the previous normal-crop route are repaired progressively: a prematurely mature bottom first rebuilds MIDDLE/TOP before it is eligible for harvest.
- Rich Soil acceleration uses the same structure-aware growth path.
- The renderer now displays up to all three Corn sections inside the Rich Farmer.
- Corn Kernels are preserved as the planting item for tooltips and dismantling.

## Crop audit

Hearth & Harvest's other villager-plantable items were reviewed against their native lifecycle:

- Cotton: already handled by EFD's regrowing definition (age 7 -> age 5).
- Peanut: ordinary CropBlock; generic handling is appropriate.
- Blueberry/Raspberry: persistent SweetBerryBush-style handling already applies.
- Sunflower Seeds: directly place a two-block vanilla Sunflower and are not a Corn-like harvest lifecycle.
- Red/Green Grapes: trellis plants, not normal crops. They remain outside generic crop handling and should only receive dedicated trellis compatibility if implemented later.

## QA status

Static/source QA only in this environment. Runtime compilation and in-game Corn QA remain pending.
