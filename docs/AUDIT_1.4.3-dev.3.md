# Easy Farmer's Delight 1.4.3-dev.4 audit — Forge 1.20.1

## Cutter / Cutting Board parity

- Farmer's Delight Cutting Board recipes support six tool families: Knife, Pickaxe, Axe, Shovel, Hoe and Shears.
- The Cutter previously admitted only Knives and Axes in its protected tool slot, even though the existing recipe resolver itself could execute arbitrary matching Cutting Board recipes.
- 1.4.3-dev.4 expands Cutter tool admission, automation routing, tool examples and Jade requirement reporting to all six official Cutting Board tool families.
- Axe fallback interactions (stripping, scraping, unwaxing and compatible modded log actions) remain separate and continue to run only when no Farmer's Delight Cutting recipe matches first.
- Cutting recipe Fortune handling was already correct: the equipped tool's Fortune level is passed to Farmer's Delight's rollable results.

## Farmer harvest-tool policy

The existing harvest architecture already matches the intended policy and is now documented explicitly in code:

- Normal farmland crops and Tomatoes: optional Hoe; Fortune may affect compatible loot tables.
- Rice: Knife-aware path; no Hoe Fortune fallback.
- Mushroom Colonies: Knife requirement; no Hoe Fortune fallback.
- Melon/Pumpkin: Axe requirement; the Axe is passed into the fruit loot context so Fortune and Silk Touch are preserved.
- Orchards/fruit leaves: Shears requirement; no Hoe Fortune fallback.
- Sugar Cane: static harvest path; no tool or enchantment effect.

No broad refactor was made here because the current separate harvest paths already encode these rules safely.

## Jade

- Removed redundant Rich Soil status lines from Rich Farmer/Rich Paddy Farmer tooltips. Rich Soil is intrinsic to those machine variants and does not need to be reported as an active state.
- Standalone Grafting Support Rich Soil feedback remains intact because soil placement is meaningful information in-world.
- Cutter Jade now recognizes Pickaxe, Shovel, Hoe, Shears and generic multi-tool Cutting Board requirements in addition to Knife/Axe.

## Item automation re-audit

No regression was found in the Cutter sided handlers:

- Top: protected tool slot + material inputs.
- Sides: materials only.
- Bottom: output extraction only.
- Tool extraction remains blocked through normal automation.
- Expanded Cutting Board tool recognition is also respected by sided insertion, preventing the new tool families from being routed into material slots.

## Grafting Support

- Integrated the new pixel-grid-aligned Blockbench model for the base support.
- Orchard definitions remain data-driven and are not tied to Croptopia Java classes.
- No additional mod bridge is added in this snapshot; other fruit-leaf mods should be tested first.

## Deferred cleanup

- `CompatFarmerBlockEntity` remains the largest maintenance hotspot. It contains many independent crop paths and should only be split incrementally after behavior is covered by QA; a large refactor during this feature pass would add unnecessary regression risk.
- Noise Switch/Cutter model normalization remains a manual Blockbench polish task.
- Wild Crops remain intentionally excluded from Farmer planting because they are decorative wild plants/drop sources rather than farm crops.

## Interaction priority follow-up (dev.2)

- Shift + Right Click is now a dedicated dismantle path and is evaluated before every held-item interaction.
- A compatible held item can no longer be inserted while the player is sneaking.
- Tomato ropes are removed from the top down before the crop; subsequent Shift + Right Clicks continue through crop and villager contents without requiring an empty hand.
- Orchard, attached-crop and paddy dismantling use the same priority rule.
- Returned contents are inserted into the player inventory in removal order; any remainder that cannot fit is dropped beside the player instead of blocking the dismantle sequence.

## Tomato Rope state separation

- Rope infrastructure and tomato growth are now separate persistent states. Progress 0 no longer ambiguously means both an empty Rope and an age-0 tomato section.
- Sequential extension prevents installing two Ropes from bypassing the normal tomato growth path.
- Rich Soil only targets Rope sections whose planted-state flag is active.
- The renderer displays Farmer's Delight Rope itself until tomato growth reaches that section.
