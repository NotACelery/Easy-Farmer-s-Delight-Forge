# QA — Easy Farmer's Delight 1.4.3-dev.4 (Forge 1.20.1)

## Build

- Build succeeds with the loader's supported Java/Gradle configuration.
- Artifact identifies itself as `1.4.3-dev.4`.

## Cutter

- Knife, Pickaxe, Axe, Shovel, Hoe and Shears can occupy the Cutter tool slot when valid Cutting Board tools.
- Representative official Farmer's Delight Cutting Board recipes execute for each tool family.
- Fortune-sensitive Cutting Board recipes use the actual stored tool.
- Output-full state does not consume input or damage the tool.
- Top/sides/bottom automation routing remains correct and normal automation cannot extract the protected tool.

## Rich Farmer harvesting

- Normal ground crops can use an optional Fortune Hoe.
- Rice retains its Knife-specific path and does not use Hoe Fortune.
- Mushroom Colonies require Knife and do not use Hoe Fortune.
- Melon/Pumpkin use the stored Axe path; Melon preserves Axe Fortune/Silk Touch behavior.
- Orchards require Shears and do not use Hoe Fortune.
- Sugar Cane ignores Harvest Tools/enchantments.

## Tomato + Rope

- Installing Rope does not immediately create tomatoes on it.
- Mature base tomato extends to Rope 1 at age 0.
- Mature Rope 1 extends to Rope 2 at age 0.
- Rich Soil never boosts an empty Rope section.
- Harvest and regrowth remain independent by section.
- Shift + Right Click dismantles in order even while holding an interactable item.
- Full player inventory causes removed contents to drop rather than blocking dismantle.

## Jade

- Rich Farmer/Rich Paddy do not show redundant `Rich Soil: Active`.
- Rich Farmer Orchard growth does not show redundant `Rich Soil: Active`.
- Standalone Grafting Support still reports whether Rich Soil is present/required.
