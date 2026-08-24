# Changelog

## 1.3.1 — 2026-08-23

- Replaced the mod icon and resource-pack icon with the new in-game screenshot-based artwork.
- Cleaned up overly verbose and redundant source comments without changing runtime behavior.
- Fixed the Iron Farm Noise Switch display Golem moving its head/body while stored; the miniature Golem now renders in a fully frozen pose.

## 1.3.0 — 2026-08-22

- Villager Noise Switch is now always non-stackable, matching its Iron Farm sibling.
- Iron Farm Noise Switch inventory/JEI/EMI previews always render the pedestal and Lever, and completed item state previews render the stored Iron Golem.
- Iron Farm Noise Switch Block Guide is condensed into a visual `empty switch + 4× Iron Block + Carved Pumpkin -> completed switch` transformation, leaving room to explain the actual client-side sound-muting behavior.

- Added the Forge 1.20.1 Iron Farm Noise Switch with the same two-stage construction and client-local behavior as the NeoForge implementation.
- Requires four inserted Iron Blocks plus a Carved Pumpkin after the base recipe; the completed miniature Iron Golem is permanent.
- Added exact-position filtering for Easy Villagers Iron Farm Zombie/Iron Golem sounds without muting normal world mobs.
- Added persistent per-client preference, renderer/item persistence, Jade status, JEI/EMI Block Guide documentation and creative-tab registration.
- Changed Villager Noise Switch recipe/model pedestal from Iron Block to Emerald Block to visually separate both switches.
- Fixed empty Farmer upgrades being treated as stateful when their source only carried structurally empty block-entity NBT, so clean Paddy/Rich Farmer items remain stackable directly from crafting and recipe transfer.

## 1.2.1 — Forge 1.20.1 port

- Initial Forge 1.20.1 codebase derived from the NeoForge 1.21.1 1.2.1 feature set.
- Targets Easy Villagers 1.1.39 and Farmer's Delight 1.3.3.
- Backported machine persistence from Data Components to NBT.
- Added Forge item-handler capabilities and Forge menu/event registration.
- Backported custom Farmer/Cutter crafting recipes.
- Adapted Farmer's Delight cutting recipes and 1.20.1 knife tags.
- Adapted tomato rope harvesting to Farmer's Delight 1.20.1 `tomatoes_on_rope`.
- Preserved state-aware Farmer stacking and automation protections.
- Retained optional Jade/JEI/EMI integrations.
- Successfully compiled with ForgeGradle on JDK 17.
- In-game validation completed for Farmer recipes and upgrades, Harvest Tools and Fortune behavior, Villager Noise Switch, and Cutter processing/output handling.
