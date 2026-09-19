# Easy Farmer's Delight 1.4.4 — Development Reference

This document describes the **current 1.4.4 architecture and invariants**. Straightforward implementation details are
kept in code; cross-class lifecycle rules, persistence contracts, compatibility boundaries and non-obvious behavior
belong here so Java sources can stay minimally commented.

## 1. Release identity

- Public name: **Easy Farmer's Delight**.
- Public version: **1.4.4** (Forge 1.20.1).
- Technical mod ID / registry namespace: `easyfarmersdelightcompat`.
- Java package root: `dev.celerbi.easyfarmersdelightcompat`.
- Artifact stem: `easy-farmers-delight`.
- Loader target: **Minecraft 1.20.1 / Forge 47.4.x / Java 17**.

The technical namespace is intentionally retained. It is persisted in world registry IDs, recipes, tags, saved
BlockEntity data and existing item stacks. Public rebranding must never be implemented by silently renaming those
IDs.

## 2. Source/style rules

- Java uses 4 spaces, no tabs and no trailing whitespace.
- Wildcard imports are not allowed.
- Keep Java lines at or below 120 characters where practical.
- Avoid compressed multi-statement lines.
- Inline comments are reserved for invariants that cannot be expressed clearly in code structure or this document.
- JSON is pretty-printed and parse-valid.
- Loader trees remain behaviorally equivalent unless Minecraft/loader API differences require divergence.
- Build/cache/log/run/IDE artifacts do not belong in source snapshots.

`.editorconfig` carries the whitespace baseline for editors.

## 3. World and item compatibility contract

Existing registered identities are stable. In particular, do not casually rename:

- Paddy Farmer, Rich Farmer, Rich Paddy Farmer, Cutter or Noise Switch registry IDs.
- the established Farmer BlockEntity type ID `compat_farmer`;
- persisted `Efdc*` NBT keys;
- `CutterLog`;
- client preference file/key identities;
- historical Jade provider UIDs used by existing integrations.

New NBT fields require safe defaults. Missing optional mods/datapack definitions must not make existing stored
machines unreadable.

Empty Farmer machine items normalize back to clean stackable items. Meaningful BlockEntity state makes a Farmer
non-stackable so stored villagers/inventories cannot be duplicated. Upgrade recipes preserve meaningful source
Farmer data while stripping transient representation-only state.

## 4. Farmer architecture

`CompatFarmerBlock` is the shared block shell for Paddy, Rich and Rich Paddy variants.
`CompatFarmerBlockEntity` owns Easy Farmer's Delight-specific state and coordinates the Easy Villagers adapter,
virtual crop families, output inventory, Harvest Tool, attached hosts, persistence and client synchronization.

Easy Villagers remains the owner of its stored Villager/Farmer payload. The adapter mutates that delegate; the Easy
Farmer's Delight BlockEntity owns persistence and sync so one logical transition does not emit duplicate updates.

The base Easy Villagers Farmer is not patched to gain special crop families. New mechanics belong to the Easy
Farmer's Delight variants.

## 5. Event-driven harvest scheduler

The 1.4.0 Farmer lifecycle avoids repeated expensive blocked-harvest polling.

A mature crop attempts harvest once. If blocked, it records the blocker and parks:

- **OUTPUT_FULL** — wake after output capacity genuinely increases.
- **TOOL** — wake when the Harvest Tool changes.
- **VILLAGER** — wake when stored villager state changes in a way that may allow work, including baby → adult.
- world/load reconciliation performs one readiness pass after state restoration.

When a harvest was already rolled before discovering insufficient output capacity, the pending drop set is retained
transiently and reused for the capacity retry rather than rerolling loot.

The output wrapper tracks the four output slots so manual GUI removal, shift-click removal and automation extraction
all generate the same capacity-increase event. Output insertion does not wake a Farmer waiting for more space.

Attached crops deliberately use a generic output wake rather than one globally cached fruit requirement: different
faces can produce different items. A blocked face is skipped while the scan continues, allowing any other mature
face whose drops fit to harvest independently.

## 6. Rich Soil scheduling

Rich Soil acceleration is a **growth opportunity**, never a direct harvest multiplier.

Normal/regrowing crops respect Farmer's Delight's `farmersdelight:unaffected_by_rich_soil` exclusion where relevant.
Attached definitions carry their own `rich_soil` boolean. Rice has its dedicated Rich Paddy boost. Sugar Cane is
explicitly excluded because Paddy Sugar Cane is Sand-based compatibility behavior.

Melon/Pumpkin Rich Soil applies to stem progression only; fruit generation remains on the normal Farmer cadence.

The Rich Soil hot path is batched around the machine's one-second work pulse and uses statistically equivalent
opportunity sampling rather than running unnecessary expensive checks every server tick.

## 7. Crop families

### 7.1 Normal Easy Villagers-compatible crops

Rich Farmer delegates ordinary seed recognition/crop state to Easy Villagers. Easy Farmer's Delight extends the
logical `minecraft:villager_plantable_seeds` tag with optional Magebloom/Argentum entries. Missing optional entries
are safe.

### 7.2 Paddy Rice

Paddy Farmer and Rich Paddy Farmer maintain the Farmer's Delight Rice lower-plant/panicle lifecycle. Rich Paddy may
advance Rice through Rich Soil. Knife use is optional on Rich Paddy and forwards the real Knife-sensitive Rice loot
behavior without artificial durability damage.

### 7.3 Paddy Sugar Cane

Sugar Cane mode stores installed Sand, base/height/progress state and leaves the bottom cane section planted while
harvesting upper sections. Rich Paddy does not accelerate it. Sneak-use dismantling returns the installed materials.

### 7.4 Tomatoes and Rope

Rich Farmer stores the base Tomato state plus up to two Rope section progress values. Sections advance and harvest
independently. Gameplay harvest continues to use Farmer's Delight loot behavior so compatible Hoe/Fortune semantics
remain authoritative.

### 7.5 Mushroom Colonies

Rich Farmer maps Red/Brown Mushroom to the matching Farmer's Delight colony. Growth does not require a Knife;
mature harvest does. The Knife is a blocker only and is not damaged by this harvest.

### 7.6 Melon/Pumpkin

Rich Farmer models stem progress and fruit-ready state explicitly. An Axe is a hard mature-harvest requirement.
Actual fruit drops use the vanilla loot path so tool enchantments keep normal meaning.

### 7.7 Regrowing crops

Forge 1.20.1 includes an optional **Delightful Cantaloupe** definition. It models Delightful 3.8.x as a four-stage
regrowing crop (`age=0..3`), harvests one Cantaloupe at age 3 and returns the virtual plant to age 0. No Delightful
Java classes are linked; absent registry entries simply skip the definition.

Definitions live under `data/<namespace>/efdc_regrowing_crops/*.json` and declare planting item/tag, crop block,
age property/range, harvest age, post-harvest age, harvest strategy/count and Rich Soil eligibility.

Built-ins:

- `sweet_berries` — Sweet Berry Bush, age 3 harvest, reset to age 1, 2–3 berries.
- `ars_sourceberry` — Ars Nouveau Sourceberry, age 3 harvest, reset to age 1, configured 2–3 berry semantics.

Support is explicit; no broad superclass such as `BushBlock` is automatically accepted.

### 7.8 Attached crops / log mode

Definitions live under `data/<namespace>/efdc_attached_crops/*.json`. Each definition specifies:

- planting item or item tag;
- rendered crop block;
- host block or host tag;
- age property, min/max/mature/post-harvest values;
- facing property;
- loot strategy;
- Rich Soil eligibility;
- optional tool category.

The Rich Farmer stores two host levels × four horizontal faces. Host blocks and each face's definition/crop/planting
identity/age are persisted independently.

Built-ins:

- Cocoa Beans → `minecraft:jungle_logs`.
- Bombegranate Pod → `ars_nouveau:blazing_logs`.
- Mendosteen Pod → `ars_nouveau:flourishing_logs`.
- Frostaya Pod → `ars_nouveau:cascading_logs`.
- Bastion Pod → `ars_nouveau:vexing_logs`.

Host compatibility is authoritative. A recognized attached seed against an installed incompatible host with a free
face is rejected without consuming the item and reports the translated incompatible-host message. A completely full
host does not emit that warning because there is no open planting target.

Dismantling order is upper crops → upper log → lower crops → lower log. Dismantling returns planting items, not a
mature loot bonus.

Persisted face identity is sufficient to render/dismantle an existing crop even if its datapack definition later
vanishes.

### 7.9 Data-driven tall and stem crop families

Compatibility that needs more than a normal CropBlock is modeled explicitly instead of being forced through the
Easy Villagers harvest/replant path. Tall, stem and similar definitions keep their own stage/progression data, render
state and Rich Soil eligibility. Optional integrations resolve registry IDs at runtime so a missing source mod is
safe.

### 7.10 Persistent multi-section crop lifecycles

Some external crops grow as multiple persistent sections and are harvested without uprooting the plant. Rich Farmer
stores those sections independently, applies the source mod's growth gates, harvests only mature sections and resets
only the harvested section to its native post-harvest stage. Existing one-section saves are migrated forward before
normal harvesting resumes. This avoids the legacy behavior where the first mature section could cause the whole crop
to be harvested and replanted too early.

## 8. Farmer item crop tooltip

`CompatFarmerItem` reconstructs a lightweight BlockEntity view from the item's persisted state when a world/registry
context is available. It asks the BlockEntity for planted crop names and displays distinct crops only. Empty state
uses the translated `Crop: None` line.

This is presentation only; no duplicate tooltip-only NBT format exists.

## 9. Cutter architecture

The Cutter is a villager-powered machine with 4 input slots, 1 protected tool slot and 4 output slots.

It resolves Farmer's Delight Cutting recipes against the installed input/tool, forwards Fortune where supported,
and also supports Axe transformations such as stripping, scraping and unwaxing.

### Dynamic work-surface log selection

`CutterLogVariant` accepts item/block membership in standard Minecraft log tags plus the historical
`easyfarmersdelightcompat:cutter_logs` datapack fallback, then filters to unstripped base logs/stems. Names beginning
with `stripped_`, `_wood` blocks and `_hyphae` blocks are excluded.

Only the selected registry ID is stored under `CutterLog`. Missing/invalid stored blocks fall back safely to Oak Log.

The renderer uses the selected source block's actual model rather than copied addon textures. Modded woods,
Crimson/Warped stems and animated Ars Nouveau Archwood therefore retain their owning mod's visual behavior.

### Cutter standby

The Cutter parks when there is no processable input/tool combination or output is full. Input/tool changes wake work
planning; output-full waits wake only after output is reduced. A successful operation batches input consumption,
output insertion and tool damage into one visible BlockEntity update.

`CuttingRecipeResolver` still performs a bounded scan of the recipe manager because the indexed Cutting recipe API
differs between target versions. This is intentionally isolated so a future safe API-specific optimization can be
made without changing machine semantics.

## 10. Noise Switches

All player mute preferences are client-local and persistent. Lever visuals do not create real Redstone state,
neighbour updates or Observer signals.

### Villager Noise Switch

Stores one Easy Villagers Villager and controls local Villager voices. The block is non-stackable.

### Iron Farm Noise Switch

Assembly state is persisted. Four Iron Blocks build the miniature Golem body in stages; a Carved Pumpkin permanently
completes it. Sound cancellation is restricted to the configured synthetic Zombie/Iron Golem sounds whose source
position is exactly an Easy Villagers Iron Farm.

### Easy Mob Farm Noise Switch

Registration is guarded by `easy_mob_farm`. Six Rotten Flesh assemble a decorative vanilla Zombie model. The mute
controller targets Easy Mob Farm display entities only. No real Zombie is spawned.

## 11. Optional integration boundaries

Optional integrations must remain absent-safe:

- Jade code lives only under `integration/jade` and is registered through the Jade plugin boundary.
- JEI and EMI use viewer-neutral data from `RecipeViewerData` where possible.
- Easy Mob Farm registration/resources are mod-loaded guarded.
- Ars Nouveau crop definitions use registry IDs/tags and generic data loaders rather than Ars Java classes.
- Argentum seed entries are optional tag entries.

No optional API may be referenced from an unconditional classloading path when that mod can be absent.

## 12. JEI / EMI viewer model

Farmer viewer information is split by mechanic rather than duplicating gameplay recipes. Gameplay recipes remain the
authoritative crafting source. Viewer guides describe Harvest Tools, Paddy behavior, Rich Farmer special crops,
Cutter behavior and Noise Switch usage.

Stateful Farmer recipe transfer must preserve the actual source ItemStack components/NBT instead of replacing it with
a synthetic clean Farmer. JEI uses standard transfer where reliable; EMI uses the dedicated state-preserving path
required by its component matching behavior.

## 13. Jade model

Jade provides diagnostics only; it does not roll loot or mutate machine state. It may report current crop/growth,
Rich Soil state, hard tool blockers, Sugar Cane state, Melon/Pumpkin phase, attached lower/upper host translated names
and occupied-face counts, Cutter status and Noise Switch status/assembly.

Attached Jade output intentionally summarizes host occupancy rather than exposing per-face internal NBT.

## 14. Rendering and third-party asset boundary

Resources distributed under `assets/easyfarmersdelightcompat` do not use Easy Villagers model parents, textures or
GUI backgrounds. Machine shell/GUI presentation is project/vanilla based.

Dynamic content is intentionally rendered from the owning game's/mod's live resources:

- stored Villagers use the vanilla Villager renderer;
- crops use their actual crop block model;
- attached hosts use their actual block model;
- Cutter variants use the installed log/stem model.

This gives correct resource-pack/mod animation behavior without redistributing third-party artistic assets.

## 15. `/farm` command

The operator command syntax is:

```text
/farm <from> <to> <farm> <villager:true|false> <crop-or-none> [extra]
```

Coordinates are vanilla `X Y Z`. Short Farmer names and full legacy IDs are accepted. Crop aliases are normalized
where appropriate. Extra modes are `rope=0..2`, `sand`, and `logs=1..2`.

Attached crop plans choose a canonical compatible host deterministically. A direct host ID is used exactly; for host
tags, unstripped `_log`/`_stem` candidates are preferred before other valid base blocks. One host fills four faces;
two hosts fill eight.

The command validates max volume, build height, loaded chunks, Farmer/crop compatibility and extra-mode validity
before modifying the target area. Vanilla `/fill` is not extended or replaced.

## 16. Performance/rendering notes

Server-side Farmer work is expected to remain cheap while machines are blocked or idle. Client FPS can still fall
when hundreds of full villager/crop models are visible because the renderer must draw that geometry. Vanilla
occlusion/frustum culling eliminates that visual cost when machines are behind opaque walls or out of view.

The cross-loader reference stress QA on the NeoForge counterpart used 513 Rich Farmers and recovered roughly normal 100–120 FPS while the machines were
loaded but occluded. This is treated as evidence that the remaining dense-array cost is predominantly rendering,
not the previous work scheduler.

## 17. Reference 1.4.0 runtime QA

The shared 1.4.0 architecture was exercised on the NeoForge counterpart with:

- Rich Farmer normal crops and newly supported special crops.
- Sweet Berry and Sourceberry mature harvest → post-harvest regrowth behavior.
- Magebloom normal crop support.
- mixed attached host logs/crops, including Ars Nouveau families.
- attached output independence after output-full waits and manual extraction.
- Paddy/Rich Paddy Rice and Sugar Cane behavior, including no Rich Soil Sugar Cane bonus.
- dynamic Cutter variants with vanilla, Nether stem and external modded logs; source animations remain intact.
- persistent Farmer inventory/state across save/load/item handling.
- `/farm` configured-grid creation.
- dense Farmer rendering/occlusion performance.

Forge maintains the equivalent feature design through 1.20.1 APIs/data formats. Loader-specific runtime QA should
still accompany any future code change even when the shared behavior is unchanged.

## 18. Loader-specific adaptation

The Forge backport uses Minecraft 1.20.1 Forge item-handler/NBT conventions and generated Forge metadata.
The Java toolchain is Java 17. Jade/JEI/EMI compile-time integrations use their Forge 1.20.1 API lines.

## 19. Release hygiene

A release source tree must pass:

- Java formatting scan: tabs/trailing whitespace/wildcard imports/overlong compressed lines.
- JSON parse and locale-key parity.
- TOML parse where applicable.
- no generated `build`, `.gradle`, `.gradle-dist`, `.jdk17`, `run`, logs, crash reports, classes or IDE metadata.
- no stale retired Jade/cutter tag sources.
- no Easy Villagers visual asset references.
- ZIP CRC verification.

## 20. Orchard definitions (1.4.2+)

Orchard definitions live under `data/easyfarmersdelightcompat/efdc_orchard_crops/*.json`. The runtime loader resolves
optional registry IDs without classloading the source mod. A definition supplies a planting item or tag, a render
block, age bounds/property, harvest item/count semantics, Rich Soil eligibility and render style.

`CompatFarmerBlockEntity` persists the definition ID plus planting item and a small render snapshot (render block,
age property/style, harvest item and mature age). The standalone `GraftingSupportBlockEntity` persists the exact
inserted canopy plus the same render snapshot. Both paths therefore synchronize enough information for dedicated
clients to render the Orchard without relying on the server datapack reload listener populating a client-side static
map.

A standalone Grafting Support accepts any `minecraft:leaves` BlockItem. If no Orchard definition matches, the canopy
is decorative. A matching canopy advances only while Farmer's Delight Rich Soil is directly beneath the support;
removing Rich Soil stalls the current age without resetting it. Mature standalone fruit is harvested manually with
Shears, while Rich Farmer Orchards use the Harvest Tool slot and automatic output insertion.

The standalone canopy is represented by an invisible reserved upper block whose dynamic outline/collision follows the
visible leaf mass. It is physically breakable: Shears or Silk Touch recover the exact installed leaves, while ordinary
breaking destroys them and leaves the lower Grafting Support intact. The visible graft branch is a slim stripped-oak
segment rendered only when a canopy is installed.

Vanilla Oak/Dark Oak use `render_style: apple`: their age is virtual and is represented by the Rich Farmer and
standalone Grafting Support renderers. Croptopia uses `render_style: block_age`, allowing both renderers to draw
Croptopia's real `*_crop` AGE 0..3 models. The source models/textures are never copied into Easy Farmer's Delight.
Croptopia's 58 ordinary ground crops remain on the standard Easy Villagers-compatible crop path; no special Java
handler is required for those crops.

Mature Rich Farmer Orchard output is rolled once and retained while output is blocked. The pending roll is persisted
so clearing output space cannot reroll a better/worse Apple result. Shears are damaged only after the pending output
is inserted. Standalone harvesting has no machine output buffer, so a successful Shears harvest drops the rolled fruit into the
world beside the support and then resets the fruit age.

Croptopia Cinnamon is intentionally handled as a narrow `AxeActionResolver` compatibility case because Croptopia
implements the bark drop through its tool-modification event rather than Minecraft's normal Axe stripping map. The
Cutter therefore resolves Cinnamon Log/Wood to the corresponding stripped block plus one Cinnamon as one atomic
operation.

## 21. 1.4.4 compatibility and corrections

The 1.4.4 Forge release expands/updates compatibility with **Fruits Delight 1.1.3**, **Delightful 3.8.x**,
**Hearth & Harvest**, **Regions Unexplored**, **Croptopia**, **Twilight Forest**, **Deep Aether** and
**Eternal Starlight** through the data-driven crop families and Forge 1.20.1 adapter layer.

The Grafting Support/Orchard path was hardened after earlier implementations exposed transient leaf placement,
incorrect canopy interaction geometry and inconsistent leaf mining/particle behavior. The final implementation owns
the canopy interaction cleanly, synchronizes its reserved upper block, uses the corrected selection/collision shape
and preserves the installed canopy/orchard snapshot for save/load and migration. Apple-provider priority also avoids
competing vanilla Apple Orchards when a dedicated supported apple-tree provider is installed.

Hearth & Harvest persistent structural crops no longer fall through the normal harvest-and-replant path. Their
independent sections can finish growing and be harvested/reset without uprooting the whole virtual plant.

Forge additionally retains the loader-specific **Delightful 3.8.x** compatibility definitions and viewer/audit
coverage that are not shipped on NeoForge 1.21.1.
