# Easy Farmer's Delight 1.4.4-dev.3 — Forge 1.20.1

QA follow-up shared by Forge and NeoForge.

## Fixes

- Grafting Support item model now has an explicit 0.5 GUI transform so the block model stays inside a single inventory/hotbar slot. The placed block model is unchanged.
- Added Delightful 3.8.1 Salmonberry Pips support. `delightful:salmonberry_pips` configures `delightful:salmonberry_bush`; the Rich Farmer grows it to age 4, harvests 2-3 `delightful:salmonberries`, then resets the bush to age 1 instead of destroying it.
- Existing Regions Unexplored Salmonberry support remains separate; its planting item is still `regions_unexplored:salmonberry`.

## Cutter audit / modded Cutting Board recipes

- The Cutter scans the complete runtime RecipeManager and accepts every recipe whose recipe type is `farmersdelight:cutting`, regardless of recipe namespace/mod.
- Tool matching calls the recipe's own `Ingredient.test(tool)`, so Farmer's Delight custom tool-action ingredients such as `shovel_dig` are supported rather than requiring a concrete shovel item in the JSON.
- Delightful 3.8.1's `cutting/green_tea_leaves` recipe was audited: it consumes `#forge:tea_leaves/green`, uses `farmersdelight:tool_action` / `shovel_dig`, and that tag explicitly includes optional `croptopia:tea_leaves`. This path therefore remains generic; no Croptopia-specific Matcha hardcode was added.
- JEI/EMI keep the Cutter registered as a workstation/catalyst for the Farmer's Delight cutting category.

Build-clean status: pending Windows compile/in-game QA.
