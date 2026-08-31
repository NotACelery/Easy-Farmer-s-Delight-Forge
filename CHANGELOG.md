# Changelog

## 1.4.0-dev.2 — unreleased

### Development build hygiene

- Added migration cleanup for obsolete pre-regularization development files when a snapshot is overlaid onto an existing working tree.
- Build helpers now remove the retired `compat/jade` source directory and old shipped `cutter_logs.json` whitelist before compilation, preventing stale Java/resources from surviving a package move.
- No gameplay, registry, NBT or world-data behavior changed from dev.1.

### Interoperability and asset-boundary cleanup

- Removed every direct Easy Villagers model, texture and GUI asset reference from Easy Farmer's Delight Compat resources. Existing block IDs, block entities, NBT and world data remain unchanged.
- Added a project-owned `machine_cage` model built from vanilla resources for the Cutter and Noise Switch family, while existing Farmer models now use vanilla frame textures.
- Replaced the three Easy Villagers-backed container backgrounds with project-rendered GUI panels that preserve the existing slot coordinates and gameplay layout.
- Moved all Jade API code under `integration/jade` and reduced `EfdcJadePlugin` to registration only; Jade remains optional and compile-only.
- Added `THIRD_PARTY_NOTICES.md` documenting dependency ownership, license boundaries and the fact that third-party binaries/assets are not redistributed.

### Generic Cutter wood variants

- Replaced the hardcoded vanilla Cutter wood whitelist with Minecraft's standard `logs` tag plus runtime filtering for unstripped base logs.
- Kept the historical `easyfarmersdelightcompat:cutter_logs` tag as a compatibility fallback for datapacks that extended it, without shipping the old hardcoded whitelist.
- Modded logs such as Ars Nouveau Archwood and Pale Garden Update Pale Oak can become Cutter work surfaces automatically when their source mod exposes them through the standard log tag.
- Cutter items continue storing only the selected block registry ID. Existing pre-regularization 1.4.0-dev.1 Cutter items remain compatible and Oak remains the safe fallback if a stored modded log is unavailable.
- Cutter tooltip and Jade output now use the source block's own translated name instead of addon-maintained wood-name translations.

## 1.4.0-dev.1 — internal development checkpoint — 2026-08-30

- Added the optional Easy Mob Farm Noise Switch, registered only when `easy_mob_farm` is installed.
- Added the five-Glass-Pane recipe with Lever, Copper Block, Mossy Cobblestone and Redstone Block.
- Added six-step Rotten Flesh Zombie assembly: right leg, left leg, torso, right arm, left arm and head.
- Completed switches toggle a persistent client-local mute only for Easy Mob Farm's card-display mobs; real world mobs remain untouched.
- Added state-preserving item previews, Jade status, JEI/EMI Block Guide support and the same corrected hollow-shape/interior-light handling used by the existing Noise Switches.

## 1.3.2 — 2026-08-29

- Fixed Villager Noise Switch and Iron Farm Noise Switch interior lighting next to opaque blocks and local light sources.
- Both switches now use the same hollow 1/16 shell shape as the compat Farmers, matching the visible enclosure instead of behaving like a logical full cube.
- Dynamic switch contents now sample surrounding world light while inventory previews continue to use their supplied preview lighting.

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
