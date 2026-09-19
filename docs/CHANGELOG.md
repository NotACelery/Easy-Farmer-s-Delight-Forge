# Changelog

## 1.4.4 — 2026-09-19

### Compatibility

- Backported/expanded optional **Fruits Delight 1.1.3** compatibility across Rich Farmer crop and Orchard systems.
- Added optional **Delightful 3.8.x** compatibility on Forge 1.20.1.
- Expanded and updated compatibility for **Hearth & Harvest**, **Regions Unexplored**, **Croptopia**,
  **Twilight Forest**, **Deep Aether** and **Eternal Starlight**.
- Added/extended data-driven tall, stem, regrowing, orchard and persistent structural crop lifecycles where the
  source mod cannot safely use the normal Easy Villagers harvest/replant path.
- Improved Apple Orchard ownership/migration when supported dedicated apple-tree providers are installed.

### Grafting Support and Orchard fixes

- Fixed the historical transient/ghost leaf placement issue and corrected canopy selection/collision handling.
- Corrected canopy mining speed, hit/break particles, placement interception and client/server synchronization.
- Restored the intended compact Hanging Roots visual treatment while preserving the corrected geometry/hitboxes.
- Hardened standalone/Rich Farmer Orchard migration so stale saved states do not keep producing invalid fruit.

### Rich Farmer fixes

- Corrected **Hearth & Harvest** persistent structural crop handling: sections now finish growing and are harvested
  independently instead of the first mature section causing the entire crop to be uprooted/replanted.
- Added migration for older Rich Farmer saves that stored only the legacy single-section state.

### Other fixes and release cleanup

- Fixed Cutter interior-light sampling next to opaque blocks.
- Villager Noise Switch now also suppresses automated Easy Farmer's Delight Farmer/Cutter action sounds.
- Cleaned source formatting, logging, release documentation and Forge-specific Grafting API adaptations for the
  final 1.4.4 build.

## 1.4.3 — 2026-09-06

### Rich Farmer crop compatibility

- Added a generic fallback for modded planting items that directly expose standard `CropBlock` crops, reducing the need for per-mod hardcoding.
- Expanded regrowing-crop support for Sweet Berry-style bushes and data-driven custom bushes while preserving each crop's post-harvest age.
- Added data-driven stem-crop support and corrected mature stem behavior so stems remain planted until their fruit is actually available.
- Added **Deep Aether Squash** support with Blue/Green Squash fruit selection, attached-stem rendering and fruit-only harvesting.
- Added **Eternal Starlight Nocturnal Millet** support for its stalk + panicle lifecycle, including the virtual Forgotten conversion required inside a Rich Farmer and fully mature Forgotten Nocturnal Millet harvests.
- Added **Hearth & Harvest** compatibility for Cotton, Blueberry/Raspberry bushes and compatible standard crops such as Peanut.
- Added **Regions Unexplored Salmonberry** as a regrowing bush.
- Added **Twilight Forest** Raspberry, Blueberry, Blackberry, Maloberry, Blightberry, Duskberry, Skyberry and Stingberry to the regrowing/bush family.
- Improved persisted planting-item identity so similarly named crops from different mods remain distinct and dismantle/tooltips resolve the correct item.
- Fixed Jade crop naming for modded crops by preferring the actual crop/harvest item display name instead of raw block registry IDs.

### Cutter and integration polish

- Expanded Cutter Cutting Board tool parity to Knife, Pickaxe, Axe, Shovel, Hoe and Shears families.
- Generalized Cutter tool detection and Jade requirement reporting while retaining Axe transformation fallback behavior.
- Removed redundant Rich Soil status text from Rich Farmer and Rich Paddy Farmer Jade tooltips.
- Re-audited Harvest Tool/Fortune routing, Cutter sided automation and crop-specific tool requirements.
- Integrated the final pixel-grid-aligned Grafting Support base model.

### Release validation

- `1.4.3-dev.7` was validated in-game on the NeoForge line for the final crop compatibility fixes, including Deep Aether Squash harvesting, human-readable crop tooltips, Hearth & Harvest regrowing crops and Eternal Starlight Forgotten Nocturnal Millet.
- The final `1.4.3` promotion contains no gameplay/runtime changes beyond versioning and release documentation.

## 1.4.2 — 2026-09-03

### Grafting Support

- Added the new **Grafting Support** block.
- Added **Apple Orchard** support for **Oak Leaves** and **Dark Oak Leaves**.
- Added Grafting Support and Orchard support to the **Rich Farmer**.
- Added Grafting Support and Orchard information to **Jade, JEI, and EMI**.

### Croptopia compatibility

- Added support for **Croptopia**.
- Added support for Croptopia ground crops in **Rich Farmers**.
- Added support for all Croptopia fruit-tree leaves in the **Grafting Support**.
- Added support for Croptopia fruit-tree Orchards inside **Rich Farmers**.
- Added **Croptopia Cinnamon Log and Cinnamon Wood** support to the Cutter.

## 1.4.0 — 2026-08-31

**Release boundary:** 1.3.2 was the last public release before this update and contained the Noise Switch lighting/shape fix. The 1.4.0 delta starts with the Easy Mob Farm Noise Switch and includes every subsequent change documented in this section.

## Major farming expansion

- Added **Rich Farmer Log Mode** with two independent host logs and up to eight attached crop faces.
- Added vanilla **Cocoa** support with the correct Jungle Log-only planting rule.
- Added explicit optional **Ars Nouveau** support for:
  - Magebloom;
  - Sourceberry;
  - Bombegranate on Blazing Archwood;
  - Mendosteen on Flourishing Archwood;
  - Frostaya on Cascading Archwood;
  - Bastion Fruit on Vexing Archwood.
- Attached crops respect their host family just like Cocoa: incompatible logs reject the seed instead of consuming it.
- Lower and upper host logs can be different, allowing two attached-crop families in one Rich Farmer.
- Added data-driven attached-crop and regrowing-crop definition systems for future datapack/mod interoperability.
- Added **Sweet Berry Bush** and **Sourceberry** regrowing behavior: mature harvest resets the bush to its post-harvest age instead of destroying/replanting it.
- Rich Soil accelerates supported normal, Tomato, bush and attached-crop growth without directly multiplying drops.
- Rich Paddy continues to accelerate Rice; **Sugar Cane intentionally receives no Rich Soil speed bonus**.

## Farmer quality of life

- Farmer items now show the planted crop in their inventory tooltip.
- Mixed attached-crop Farmers show each distinct stored crop, making configured machines easy to sort before placement.
- Changed the Creative tab icon to **Rich Farmer**.
- Added the `/farm` operator/QA command for creating configured Farmer grids with villager, crop and setup arguments.
- `/farm` supports Tomato `rope=0..2`, Paddy Sugar Cane `sand`, and attached-crop `logs=1..2` setup.

## Performance and reliability

- Reworked Farmer harvest scheduling around event-driven sleep/wake states instead of repeated blocked-work polling.
- Output-full harvests wake when capacity actually increases, including manual GUI extraction and automated extraction.
- Pending blocked drops are reused where possible instead of rerolling loot.
- Mixed attached crops harvest independently: one blocked fruit type no longer freezes another fruit that still fits in output.
- Tool-blocked and villager-blocked Farmers wake only on relevant state changes.
- Optimized Rich Soil cadence, reflection lookup caching, villager/crop caching and renderer-side stable lookups.
- Cutter idle/full-output work is parked until a relevant inventory/tool/villager event occurs.
- Dense Farmer arrays now avoid the previous background work cost; the remaining extreme-density FPS impact is primarily visible-model rendering and benefits from normal Minecraft occlusion culling.

## Cutter expansion

- Cutter work surfaces are now **dynamic** instead of a hardcoded vanilla wood list.
- Any compatible unstripped base log/stem from any mod can work when it participates in Minecraft's standard log tags.
- Modded Cutter items persist the exact selected log registry ID.
- The Cutter renders the owning mod's real block model, preserving native textures and animations such as Crimson/Warped stems and Ars Nouveau Archwood.
- Cutter tooltips/Jade use the source block's translated name.
- Existing Oak/default Cutter behavior remains compatible, with Oak as the safe fallback when a saved external log disappears.

## Noise Switches and integrations

- Added the optional **Easy Mob Farm Noise Switch** with six-stage Rotten Flesh Zombie assembly and client-local display-mob muting.
- Kept Villager and Iron Farm Noise Switch state client-local and narrowly scoped to their intended sounds/entities.
- Expanded Jade/JEI/EMI guidance for Farmer tools, crops, attached hosts, Cutter behavior and Noise Switches.
- Optional integrations remain absent-safe.

## Project identity and resources

- Renamed the public project to **Easy Farmer's Delight** to reflect that it is now an expansion rather than only a compatibility layer.
- The technical namespace remains `easyfarmersdelightcompat` to preserve existing worlds, registry IDs and NBT.
- Build artifacts now use `easy-farmers-delight` in their filename.
- Machine resources no longer depend on Easy Villagers visual assets; dynamic Villagers/crops/logs are rendered from their owning game/mod resources instead of redistributing those assets.

## Scope note

- **Bamboo is not treated as a Farmer crop in 1.4.0.** Its vertical structural growth remains outside the implemented crop families.

---

## 1.3.2 — 2026-08-29

- Fixed Villager Noise Switch and Iron Farm Noise Switch interior lighting next to opaque blocks and local light sources.
- Both switches now use the same hollow 1/16 shell shape as Easy Farmer's Delight Farmer enclosures, matching the visible enclosure instead of behaving like a logical full cube.
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
