# Easy Farmer's Delight Compat 1.4.0 - Internal Development Roadmap and Handoff

> Internal development/debug document. This file is intentionally gitignored and must not be published in the public repository, CurseForge changelog, Modrinth page, release JAR or public source package. It exists so development can continue across chats without losing implementation context, ordering, constraints or QA decisions.

## 0. Handoff summary

### Public and development version line

- Latest public CurseForge release: **1.3.2**.
- Current large-update target: **1.4.0**.
- Current working source version: **1.4.0-dev.2**.
- Any previous internal ZIP that was temporarily called `1.4.0` before this roadmap is a development checkpoint only and must not be treated as a public release.
- Do not skip directly to final `1.4.0`. Continue using `1.4.0-dev.N`, then RC builds, then final only after every phase gate below passes.

### Loader targets

- NeoForge: Minecraft **1.21.1**, NeoForge **21.1.x**.
- Forge: Minecraft **1.20.1**, Forge **47.4.x**.
- The two source trees should remain functionally equivalent except for loader/API differences that are genuinely required.

### Immediate next action in a new chat

1. Start at **Phase 0**.
2. Build NeoForge from a completely clean extraction of this ZIP.
3. Build Forge from a completely clean extraction of its ZIP with the appropriate JDK/toolchain.
4. If either game instance closes at runtime, capture `latest.log`, launcher console output or a crash report before changing gameplay code.
5. Do not begin performance work, JEI/EMI work or new crops until the Phase 0 build/runtime baseline is understood.

### Permanent development rules

- Preserve existing registry IDs, BlockEntity IDs, NBT keys, inventories and placed-world identity unless an explicit migration is designed and tested.
- Every meaningful editing pass must end with a **full-source recoverable ZIP snapshot for both loaders**, even if the pass is incomplete.
- Do not build new features on top of a known broken baseline.
- Prefer standard Minecraft/loader tags and registries over hardcoded mod IDs when an ecosystem standard exists.
- Optional integrations must degrade safely when the optional mod is absent.
- Third-party mod binaries/assets are not to be copied into EFDC unless their license and the project decision explicitly permit it. Prefer runtime interoperability, tags, registries and APIs.
- Maintain clean code and formatting throughout development: 4 spaces, no tabs, no trailing whitespace, no compressed multi-statement lines, no unnecessary comments, valid pretty JSON, intentional line endings and no build/cache junk in snapshots.
- The internal reasons for rebuilding visuals/resources are not public changelog material. Public changelogs describe user-visible improvements only.

---

# PHASE 0 - Re-establish a trustworthy 1.4.0-dev.2 baseline

**Priority: absolute. No feature work begins until this phase is closed.**

## Goal

Prove that both source trees are internally coherent and establish whether the recent close/crash is a source/build problem, a stale-overlay problem, a dependency mismatch or a runtime-only problem.

## Current known history

- A previous NeoForge compilation failure came from an obsolete moved Jade source surviving when a newer ZIP was extracted on top of an older working directory.
- The stale source called the removed `CutterLogVariant.translationKey(Block)` API.
- Migration cleanup helpers were added so moved/retired files can be deleted before builds.
- Static re-audits of the current dev.2 trees found no duplicate stale Jade package, no direct visual Easy Villagers assets, valid JSON and valid ZIP CRCs.
- A later instance still closed before the exact runtime message was captured, so the runtime cause is **not yet proven**.

## 0.1 Clean extraction build procedure

- [ ] Create a brand-new empty directory for NeoForge; do not reuse the previous development folder.
- [ ] Extract the NeoForge `1.4.0-dev.2` source into it.
- [ ] Run the provided build helper.
- [ ] Record the exact JDK selected, Gradle version, NeoForge version and final build result.
- [ ] Repeat from a brand-new empty directory for Forge.
- [ ] Use the JDK/toolchain expected by the Forge 1.20.1 project rather than assuming the NeoForge JDK is appropriate.
- [ ] Preserve build logs for any failure instead of only the final line.

## 0.2 Overlay-migration safety

- [ ] Separately simulate the real user workflow: extract dev.2 over an older working tree.
- [ ] Run `cleanup-1.4.0-migration.*` or the build helper that invokes it.
- [ ] Confirm obsolete `compat/jade/` sources are removed.
- [ ] Confirm retired internal Cutter tag files are removed where intended.
- [ ] Confirm the post-cleanup tree matches a clean dev.2 extraction for files that should be authoritative.
- [ ] Any future rename/removal must update the migration helper in the same pass.

## 0.3 Static source integrity checks

For both loaders:

- [ ] No duplicate fully-qualified Java classes.
- [ ] No unresolved references to removed helper methods/classes.
- [ ] No wrong-loader imports (`net.minecraftforge...` in NeoForge or NeoForge-only APIs in Forge).
- [ ] No stale `compat/jade` implementation after the move to `integration/jade`.
- [ ] All JSON/resource metadata parses.
- [ ] Models reference only valid vanilla/EFDC resources or deliberately supported runtime resources.
- [ ] No `build/`, `.gradle/`, `.class`, logs, crash reports, temporary files or IDE caches inside source snapshots.
- [ ] ZIP CRC test passes.

## 0.4 Runtime smoke test

After a successful build:

- [ ] Launch with only the minimum required dependencies first.
- [ ] Reach the title screen.
- [ ] Create/open a throwaway world.
- [ ] Place one Rich Farmer, Rich Paddy Farmer, Cutter and each Noise Switch available with the loaded optional mods.
- [ ] Open each GUI once.
- [ ] Break/re-place each block once.
- [ ] Quit cleanly.
- [ ] Then add optional integrations one group at a time: Jade, JEI/EMI, Easy Mob Farm, Ars Nouveau, Argentum, Pale Garden Update.
- [ ] If a close/crash happens, retain the exact log before making changes.

### Phase 0 acceptance gate

- [ ] **NeoForge clean build PASS.**
- [ ] **Forge clean build PASS.**
- [ ] **Minimal runtime smoke test PASS on both loaders.**
- [ ] **Exact cause of any remaining close/crash identified or proven external.**
- [ ] **Static integrity PASS on both loaders.**
- [ ] Only then proceed to Phase 1.

---

# PHASE 1 - Finish visual/resource ownership and integration isolation

**Goal: finish the non-gameplay regularization before deeper behavioral changes.**

## Non-negotiable compatibility constraint

This phase must not rename or replace existing gameplay registrations. A Rich Farmer already placed in a 1.3.2 world must remain the same block/BlockEntity and retain its inventory/NBT after resource/model changes.

## 1.1 EFDC-owned machine visuals

Already performed in dev.2, but requires final QA:

- [x] Remove direct model inheritance such as `easy_villagers:block/farm` from EFDC resources.
- [x] Replace direct Easy Villagers GUI texture references with EFDC-owned/vanilla-drawn presentation.
- [x] Preserve machine dimensions and internal render composition rather than changing gameplay identity.
- [ ] Audit every `assets/easyfarmersdelightcompat/models/**` file for external artistic-resource references.
- [ ] Audit GUI classes for `easy_villagers:textures/...` or equivalent runtime artistic-resource references.
- [ ] Confirm only standard registry identifiers/API interoperability remain where needed.

### Visual invariants for the Noise Switch family

Do not regress the fixes already established:

- [ ] Parent/shell is EFDC-owned after regularization.
- [ ] `.noOcclusion()` remains.
- [ ] Hollow 1/16 enclosure VoxelShape remains unchanged unless an explicit visual QA change is made.
- [ ] `getShadeBrightness() == 1` remains.
- [ ] `itemPreview` behavior remains.
- [ ] Interior neighbor-light sampling fix remains intact so entities/interior do not become unnaturally dark.
- [ ] Villager/Iron Golem/Zombie render positions remain inside the enclosure.
- [ ] Easy Mob Farm mossy floor remains at corrected height.

## 1.2 Jade isolation

Current direction: all Jade-specific code lives under `integration/jade/`.

- [x] Jade package moved away from mixed general compat code.
- [x] Jade remains optional/compile-only.
- [ ] Core Farmer/Cutter/Noise Switch gameplay packages must not import `snownee.jade.*`.
- [ ] If Jade needs calculated information, expose neutral EFDC state/query methods and let the Jade adapter translate them into tooltip components.
- [ ] Keep registration guarded so the game starts normally without Jade.
- [ ] Verify no Jade class is loaded early from a common class path when Jade is absent.

## 1.3 Third-party notices

- [x] `THIRD_PARTY_NOTICES.md` exists.
- [ ] Verify dependency names, ownership and license summaries against the versions actually integrated before final release.
- [ ] State clearly that third-party binaries and artistic resources are not bundled by EFDC unless explicitly documented.
- [ ] Keep legal/engineering notes factual; do not claim a legal guarantee.

## 1.4 Existing-world regression

Test a copy of a real 1.3.2 world if possible.

Required placed objects:

- [ ] Paddy Farmer.
- [ ] Rich Farmer.
- [ ] Rich Paddy Farmer.
- [ ] Cutter, including a non-Oak variant if the old world contains one.
- [ ] Villager Noise Switch.
- [ ] Iron Farm Noise Switch.
- [ ] Easy Mob Farm Noise Switch from the archived internal checkpoint where relevant.

For each:

- [ ] Registry identity survives.
- [ ] BlockEntity loads.
- [ ] Inventory survives.
- [ ] Stored villager survives.
- [ ] Crop state survives.
- [ ] Harvest tool survives.
- [ ] Cutter `CutterLog` survives.
- [ ] Switch assembly stage survives.
- [ ] Visual state is sensible after resource replacement.

### Phase 1 acceptance gate

- [ ] Zero unintended direct third-party artistic-resource references.
- [ ] Jade fully optional and isolated.
- [ ] `THIRD_PARTY_NOTICES.md` accurate enough for the current integration set.
- [ ] Existing 1.3.2 world regression PASS.
- [ ] Only then proceed to Phase 2.

---

# PHASE 2 - Farmer performance overhaul

**Goal: eliminate the severe cost seen when approximately 40 Rich Farmers enter visual range, while preserving deterministic harvest behavior.**

## Observed symptom to reproduce

- A room with roughly 40 Rich Farmers can fall from about 60 FPS to about 20 FPS when the machines become visible.
- Treat this as both a possible server/synchronization issue and a client-render issue; measure both FPS and TPS/MSPT.

## 2.1 Separate persistence from client synchronization

### Problem

The current architecture has historically overloaded `setChanged()` so a persistence mark can also trigger `sendBlockUpdated`. If called every tick for villager aging, many Farmers can produce a large stream of unnecessary BlockEntity updates.

### Target design

Introduce two clearly named concepts, loader-appropriate in implementation:

- **Dirty/persist:** server state changed and must be saved.
- **Sync/visible:** client-visible state changed and the client actually needs an update.

Requirements:

- [ ] `setChanged()`/equivalent persistence path must not automatically broadcast every call.
- [ ] Add/retain an explicit sync helper for visible state changes.
- [ ] Do not send a BlockEntity packet just because internal villager age increments by one tick.
- [ ] Synchronize baby -> adult transition because that can alter visible/functional state.
- [ ] Synchronize crop age/state only when the client-visible crop changes.
- [ ] Synchronize tool slot if its visible representation changes.
- [ ] Synchronize rope/fruit/log-side crop state only when it changes.
- [ ] Batch changes where practical rather than sending multiple packets from one logical action.

## 2.2 Event-driven harvest state machine

### Problem

A mature crop that cannot currently harvest should not be re-evaluated every second forever. Forty blocked Farmers amplify that waste.

### Required state philosophy

A crop is evaluated when something capable of changing the harvest result happens.

Growth path:

`growing -> age changes -> mature transition -> one harvest attempt`

Blocked path:

`mature -> attempt -> blocked(reason) -> sleep -> relevant event -> retry`

### Block reasons and wake-up events

**Missing/invalid tool**

- [ ] Mature crop attempts once.
- [ ] If the crop requires a tool and no valid tool exists, store/derive a waiting state.
- [ ] Do not retry on a timer.
- [ ] Retry when Harvest Tool slot content changes.

**Output full / insufficient room**

- [ ] Mature crop attempts once.
- [ ] If output cannot accept the complete harvest according to current semantics, sleep.
- [ ] Retry when output inventory changes.
- [ ] Hopper/external extraction must count as an inventory-change wake-up.

**Villager unavailable/invalid**

- [ ] Sleep until villager state changes in a way that may unblock farming.
- [ ] Baby -> adult should wake affected work.

**World/chunk load**

- [ ] A legacy Farmer loaded with an already-mature crop must receive one safe reconciliation attempt.
- [ ] Avoid creating an every-tick reconciliation loop.

### Recursion guard

Harvest itself changes inventory.

- [ ] An inventory callback caused by the harvest transaction must not recursively trigger another harvest before the first completes.
- [ ] Use a scoped guard/transaction flag or equivalent clean mechanism.

## 2.3 Apply event-driven behavior to every special crop path

Do not optimize only the normal `age` crop and leave hidden polling elsewhere.

Audit and convert as appropriate:

- [ ] Normal age-property crops.
- [ ] Tomato / rope behavior.
- [ ] Rice / paddy behavior.
- [ ] Sugar cane height/harvest behavior.
- [ ] Virtual stem/fruit behavior.
- [ ] Future Sourceberry/regrowth and attached-log crops must be designed on this system from day one.

## 2.4 Rich Soil cadence cleanup

- [ ] Identify special Rich Soil logic currently running before the slower farm-growth cadence.
- [ ] Do not inspect irrelevant crop types every tick.
- [ ] If random-tick probability is being simulated many times per second, replace it with a lower-frequency statistically equivalent or deliberately balanced check.
- [ ] Preserve expected average growth behavior and document any intentional balance change.
- [ ] Ensure Rich Soil optimization does not reintroduce broad registry/reflection scans per Farmer per tick.

## 2.5 Reflection/adapter caching

Easy Villagers integration may require reflection for compatibility, but reflection discovery should not happen in hot rendering/ticking paths.

- [ ] Cache resolved `Class`, `Method`, `Field` handles once per supported runtime shape.
- [ ] Cache negative lookup/failure state so a failed optional lookup is not retried every frame.
- [ ] Fail safely if upstream internals change.
- [ ] Never broaden reflection-based sound/entity logic so it can affect real world mobs.

## 2.6 Client renderer optimization

Measure before introducing visual compromises.

First-line optimization:

- [ ] Cache visual villager state used by the renderer.
- [ ] Cache visual crop state.
- [ ] Invalidate caches when actual BlockEntity synchronization arrives.
- [ ] Avoid repeated reflective fetches every rendered frame if the data is unchanged.
- [ ] Avoid creating temporary objects/collections in the hottest render path where practical.

Only if still needed after measurement:

- [ ] Evaluate static/cheaper distant representation or LOD.
- [ ] Do not change visual behavior prematurely if sync/cache fixes solve the issue.

## 2.7 Performance benchmark protocol

Use the same world/camera/settings for before/after comparisons.

Record:

- [ ] FPS outside the Farmer room.
- [ ] FPS when all ~40 Rich Farmers enter view.
- [ ] Server TPS/MSPT if available.
- [ ] Network/packet symptoms if observable.
- [ ] CPU frame/tick profile if a profiler is available.

Functional stress checks:

- [ ] 40 Farmers with growing crops.
- [ ] 40 Farmers with mature crops and valid tools/output.
- [ ] 40 Farmers mature but blocked by missing tools.
- [ ] 40 Farmers mature but blocked by full output.
- [ ] Insert tools and verify immediate wake-up/harvest.
- [ ] Remove output through player and automation and verify immediate wake-up/harvest.

### Phase 2 acceptance gate

- [ ] No per-tick BlockEntity broadcast loop.
- [ ] Mature blocked crops do not poll indefinitely.
- [ ] All defined wake-up events work.
- [ ] No missed harvest when requirements become valid.
- [ ] Meaningful FPS/TPS improvement in the 40-Farmer room.
- [ ] Forge/NeoForge behavior remains equivalent.
- [ ] Only then proceed to Phase 3.

---

# PHASE 3 - JEI/EMI recipe display and transfer reliability

**Goal: make Farmer upgrade recipes display and auto-fill reliably instead of occasionally requiring manual ingredient placement.**

## Known symptom

At times JEI/EMI can show the recipe but the user still has to manually place some ingredients for Rich Farmer or Rich Paddy Farmer crafting/upgrading.

Treat JEI and EMI separately until evidence proves they share a root cause.

## 3.1 Reproduction matrix

For **Paddy Farmer**, **Rich Farmer** and **Rich Paddy Farmer**:

- [ ] Recipe visible from output lookup.
- [ ] Recipe visible from ingredient lookup.
- [ ] Search/focus works.
- [ ] Transfer with completely empty crafting grid.
- [ ] Transfer with unrelated items already in the grid.
- [ ] Transfer when ingredients exist as partial stacks.
- [ ] Transfer when ingredients are split across multiple inventory slots.
- [ ] Transfer when source Farmer/Paddy Farmer contains real saved villager/crop/inventory data.
- [ ] Transfer with insufficient ingredients fails cleanly.
- [ ] Repeated/multiple-craft transfer behaves sensibly.
- [ ] Water Bucket remainder is correct where required.

## 3.2 Stateful Farmer item preservation

These upgrade recipes are not ordinary stateless item substitutions.

- [ ] Source Farmer data must not be stripped during transfer or craft.
- [ ] Villager data survives.
- [ ] Crop selection/state survives where design says it should.
- [ ] Existing inventory/tool data survives according to recipe semantics.
- [ ] No viewer handler creates a fake replacement Farmer stack that loses components/NBT.

## 3.3 EMI audit

Audit `FarmerUpgradeEmiRecipeHandler` closely.

- [ ] Determine why it currently simulates/manual-manages inventory/grid actions.
- [ ] Prefer EMI/vanilla standard transfer if it can preserve component-bearing source items correctly.
- [ ] If custom transfer remains required, make the operation deterministic and effectively transactional.
- [ ] Compute the full intended move before mutating the real grid when possible.
- [ ] On failure, leave inventory/grid/cursor unchanged.
- [ ] Avoid sequences that can stop halfway after some `PICKUP`/`QUICK_MOVE` operations.

## 3.4 JEI audit

- [ ] Confirm whether JEI standard crafting transfer handles the stateful Farmer ingredient.
- [ ] Add custom logic only where genuinely necessary.
- [ ] Do not duplicate a second recipe truth just to accommodate transfer unless unavoidable.

## 3.5 Single source of truth for viewer recipes

- [ ] Gameplay recipe definitions are authoritative.
- [ ] Viewer code should query/derive from registered recipes when practical.
- [ ] Remove duplicated hardcoded 3x3 ingredient layouts that can drift from real crafting recipes.
- [ ] If a synthetic guide is needed for non-recipe mechanics, clearly separate it from actual crafting recipe objects.

### Phase 3 acceptance gate

- [ ] JEI transfer PASS for Paddy/Rich/Rich Paddy recipes across the reproduction matrix.
- [ ] EMI transfer PASS for the same matrix.
- [ ] Stateful source Farmer data preserved.
- [ ] No half-completed transfer states.
- [ ] Recipe display cannot silently drift from gameplay recipe definitions.
- [ ] Only then proceed to Phase 4.

---

# PHASE 4 - Dynamic Cutter wood variants

**Goal: one data-driven/generic Cutter mechanism that automatically works with valid logs from vanilla and other mods.**

## Current implementation already present in dev.2

- [x] Existing NBT/data key `CutterLog` stores the selected source block ID.
- [x] Oak remains the fallback for old/stateless Cutter data.
- [x] The renderer can render the actual registered source block instead of copying its texture.
- [x] Display naming can come from the source block's own translated name.
- [x] Standard log tags are the preferred discovery path.

## 4.1 Base-log eligibility

Accept the base structural log/stem form, not every bark-related block indiscriminately.

Required positive cases:

- [ ] Overworld base logs.
- [ ] `minecraft:crimson_stem`.
- [ ] `minecraft:warped_stem`.
- [ ] Ars Nouveau base Archwood logs.
- [ ] Pale Garden Update base log.
- [ ] Unknown third-party base log that EFDC has no special Java branch for.

Default exclusions unless deliberately changed later:

- [ ] Stripped logs/stems.
- [ ] `*_wood` bark blocks.
- [ ] Hyphae.
- [ ] Decorative blocks incorrectly tagged outside the intended base-log rule.

## 4.2 Craft identity and stacking

- [ ] Crafting with a given log writes that exact registered block ID into the Cutter item.
- [ ] Two otherwise-identical Cutters with the same `CutterLog` may stack according to the intended max stack size.
- [ ] Cutters with different `CutterLog` values must never merge.
- [ ] No normalization that turns modded logs into Oak after inventory movement.

## 4.3 Persistence lifecycle

Test every transition:

`craft -> item -> place -> BlockEntity -> break -> dropped item -> inventory -> place`

- [ ] Exact variant survives each transition.
- [ ] Pick Block/clone preserves the variant.
- [ ] Any recipe viewer guide item can intentionally produce a representative/default Cutter without corrupting real variant behavior.
- [ ] Old Cutter with no `CutterLog` becomes Oak safely.

## 4.4 Tooltip/Jade

- [ ] Tooltip clearly identifies the work-surface/log variant.
- [ ] Jade shows the same source block name.
- [ ] Use the owning mod's translated block name rather than maintaining our own translation list for every external wood.
- [ ] No dependency-specific Java switch for Ars/Pale Garden merely to name the wood.

## 4.5 Legacy datapack compatibility

- [ ] Preserve support for legacy `easyfarmersdelightcompat:cutter_logs` additions as a compatibility fallback where current code promises it.
- [ ] Do not require new packs to use that EFDC-only tag when standard Minecraft logs are sufficient.

### Phase 4 acceptance gate

- [ ] Variant identity/stacking PASS.
- [ ] Full persistence lifecycle PASS.
- [ ] Vanilla, Nether stems, all four Archwoods, Pale Garden and one unknown modded log PASS.
- [ ] No per-mod hardcoded wood enumeration needed.
- [ ] Only then proceed to Phase 5.

---

# PHASE 5 - Rich Farmer attached-log crop core, Cocoa first

**Goal: implement a new Rich Farmer-only virtual planting system for crops that grow on the sides of logs, without modifying Easy Villagers base Farmer mechanics.**

## 5.1 Feature boundary

- [ ] Attached logs exist only in EFDC Rich Farmer variants.
- [ ] Do not patch Easy Villagers base Farmer to gain the two-log/eight-face mechanic.
- [ ] Existing base-Farmer interoperability through standard plantable-seed tags (for example Magebloom/Argentum crops already supported through standard data) remains separate from this new system.

## 5.2 Virtual host-log model

Support exactly two vertical host levels:

- **Level 1 / lower log**
- **Level 2 / upper log**

Each log supports four horizontal faces:

- North
- South
- East
- West

Total capacity: **8 independently simulated attached crops**.

Required stored state per log/face:

- [ ] Host log block ID per level.
- [ ] Plant/crop identity per face.
- [ ] Facing implicit from slot or explicitly stored in a stable format.
- [ ] Age/growth state.
- [ ] Mature/regrowth state if needed by the crop family.
- [ ] Any optional crop-specific data that is safe and intentionally supported.

## 5.3 Planting rules

- [ ] A host log can be inserted only if at least one registered attached crop can validly use it.
- [ ] A planting item can be inserted only onto a compatible host log.
- [ ] One planting item occupies one free face.
- [ ] Fill ordering must be deterministic so UI/render/state never reshuffles unpredictably.
- [ ] Do not consume an item if no compatible face is available.
- [ ] Lower and upper logs may be different compatible log types if the design/renderer can represent that safely.

## 5.4 Independent growth

- [ ] Every face ages independently.
- [ ] Reaching maturity on one face must not force the other faces to mature or harvest.
- [ ] The Phase 2 event-driven scheduler is mandatory; do not introduce eight constant harvest polls.
- [ ] Growth checks can be batched efficiently, but the state result of each face remains independent.

## 5.5 Harvest semantics

For Cocoa and Ars Archfruit-style pods:

- [ ] No harvest tool requirement.
- [ ] When a face becomes mature, harvest according to that crop's defined semantics.
- [ ] Harvest only the mature face.
- [ ] Respect output capacity and event-driven waiting if output is full.
- [ ] Do not duplicate planting items/drops during state reset.

## 5.6 Shift-right-click dismantling contract

This interaction is intentionally distinct from harvest.

Process top-down:

1. Inspect upper log level.
2. If upper level contains any 1-4 attached plantings, one Shift-right-click removes **all planted items from that level at once**.
3. Mature pods removed through dismantling return the planting/crop item according to teardown rules only; do not roll normal mature harvest bonus drops.
4. After upper level is empty, the next Shift-right-click removes the upper log.
5. Then perform the same sequence for the lower level.
6. If a log level is already completely empty, it can be removed immediately on its turn.

Safety:

- [ ] Never void returned plant items/logs because player inventory is full.
- [ ] Use safe insertion then world-drop fallback.
- [ ] One interaction should be atomic enough that state and returned items cannot diverge.

## 5.7 Rich Soil decision for attached crops

Current design decision to implement and test:

- Rich Soil **may accelerate growth** of attached crops because their host log is conceptually rooted in the Rich Farmer substrate.
- Rich Soil **must not multiply harvest drops** for attached crops.
- This is deliberately different from unrelated cases such as Sugar Cane using sand in a Rich Paddy Farmer.

Requirements:

- [ ] Acceleration occurs through the optimized growth cadence, not extra high-frequency polling.
- [ ] Acceleration applies only to crop definitions that permit it.
- [ ] Balance should be measured against vanilla-like growth, not guessed from render tick frequency.

## 5.8 Cocoa reference implementation

Implement Cocoa before modded pods because it gives a vanilla reference contract.

- [ ] Host: Jungle Log family as appropriate to vanilla survival semantics.
- [ ] Planting item: Cocoa Beans.
- [ ] Age stages mirror vanilla Cocoa as closely as practical.
- [ ] Render pod on correct face of the virtual log.
- [ ] Four simultaneous pods on one log render without overlap beyond intended vanilla-like geometry.
- [ ] Eight simultaneous pods across two logs render correctly.
- [ ] Mature harvest is independent per face.
- [ ] Dismantling returns plantings without mature harvest bonus.

### Phase 5 acceptance gate

- [ ] Two logs persist/render correctly.
- [ ] Eight independent face states persist/render correctly.
- [ ] Cocoa full lifecycle PASS.
- [ ] Shift-right-click teardown contract PASS.
- [ ] No new polling regression under a many-Farmer stress test.
- [ ] Only then proceed to Phase 6.

---

# PHASE 6 - Data-driven attached crops and berry-style Rich Farmer crops

**Goal: support Ars Nouveau and future mods without turning EFDC into a hardcoded Java compatibility table.**

## 6.1 EFDC attached-crop data format

Where Minecraft has no universal standard linking a planting item, a side-grown crop and compatible host logs, create a small EFDC data-driven definition.

A definition should be able to express at minimum:

- [ ] Planting item or item tag.
- [ ] Rendered crop block/state identity.
- [ ] Allowed host block or host block tag.
- [ ] Growth/age property and valid range.
- [ ] Mature condition.
- [ ] Post-harvest state/reset semantics.
- [ ] Loot/harvest strategy.
- [ ] Whether Rich Soil growth acceleration is allowed.
- [ ] Whether a tool is required; Cocoa/Archfruit definitions should say no.

Design requirements:

- [ ] Third-party datapacks can add mappings without Java edits.
- [ ] Missing optional blocks/items cause the definition to be skipped safely.
- [ ] Invalid definitions log a useful warning and do not consume player items.
- [ ] Do not copy another mod's models/textures into EFDC; render its registered block state through Minecraft when the mod is installed.

## 6.2 Ars Nouveau Archfruit support

Verified upstream behavior to preserve conceptually:

- `ArchfruitPod` extends Cocoa-like behavior with `FACING` and `AGE` and validates survival against an Archwood log tag.
- Current pairs:
  - **Blazing/Red Archwood -> Bombegranate**
  - **Flourishing/Green Archwood -> Mendosteen**
  - **Cascading/Blue Archwood -> Frostaya**
  - **Vexing/Purple Archwood -> Bastion Fruit**

Implementation rules:

- [ ] Define all four through the generic data layer, not an `if (ars_nouveau)` Java switch tree where avoidable.
- [ ] Use registered Ars blocks/items only when Ars is present.
- [ ] No copied Ars artistic assets.
- [ ] No harvest tool requirement.
- [ ] Correct host/fruit pair required; a fruit cannot be planted on the wrong Archwood family.
- [ ] Mixed valid log levels are supported if the generic system permits it.

QA:

- [ ] Four fruits on one valid log.
- [ ] Eight fruits across two logs.
- [ ] Wrong fruit/log pairing rejected without item loss.
- [ ] Ars absent -> EFDC still launches and definitions skip safely.

## 6.3 Sourceberry Bush / wild-berry crop family

Verified Sourceberry behavior is **not** an attached Archwood pod. It is a ground-planted bush with age stages and berry-picking regrowth semantics.

Rich Farmer-only design:

- [ ] Add Sourceberry Bush as a supported Rich Farmer crop.
- [ ] Preserve its regrowth family semantics rather than treating every harvest as destroy/replant.
- [ ] At mature/pickable age, harvest berries then return to the crop-defined post-pick age.
- [ ] Rich Soil may accelerate supported growth but must not change drop semantics.

Generic berry/wild-crop expansion:

- [ ] Investigate safe standards/tags or an EFDC data definition for Sweet Berry/Sourceberry-style regrowing bushes.
- [ ] Do not automatically classify all `BushBlock` instances as crops.
- [ ] Require an explicit safe definition or known crop contract.
- [ ] Keep this new crop family exclusive to Rich Farmer unless a separate deliberate decision is made later.

## 6.4 Base Farmer boundary

Current policy:

- Existing support that merely extends standard Minecraft plantable-seed tags for known crops can remain where already implemented.
- New EFDC-specific systems such as virtual logs, attached fruits and regrowing wild berries are **Rich Farmer-only**.
- Do not modify Easy Villagers base Farmer internals to provide these systems.

### Phase 6 acceptance gate

- [ ] Four Ars Archfruit pairs PASS.
- [ ] Sourceberry lifecycle PASS.
- [ ] At least one third-party/new mapping can be added through data only, without Java modification.
- [ ] Optional-mod absence PASS.
- [ ] Only then proceed to Phase 7.

---

# PHASE 7 - UI, Jade, JEI and EMI coverage for new 1.4.0 mechanics

**Goal: present already-stable gameplay clearly without making viewer/UI code another source of gameplay truth.**

## 7.1 Rich Farmer UI for logs/side crops

Decide the final interaction layout only after the underlying data model is stable.

Requirements:

- [ ] Show lower/upper host logs clearly.
- [ ] Show four face slots/states per log or another clear representation of all 8 positions.
- [ ] Keep virtual host/crop storage conceptually separate from harvested-output inventory.
- [ ] Avoid changing old inventory slot indices/NBT unless migration is explicitly designed.
- [ ] Tool slot remains clear and separate.
- [ ] GUI remains usable at normal GUI scales/resolutions.

## 7.2 Jade

- [ ] Jade code remains entirely in `integration/jade`.
- [ ] Show host log names using the owning block translation.
- [ ] Show useful crop/occupancy status without dumping internal debug state.
- [ ] For Cutter, show exact active log variant.
- [ ] For attached crops, summarize levels/faces sensibly rather than generating unreadable walls of text.

## 7.3 JEI/EMI guides for dynamic Cutter

Because variants are dynamic:

- [ ] Do not generate a Java-hardcoded recipe entry for every modded log family.
- [ ] Viewer representation should communicate that valid base logs determine the Cutter variant.
- [ ] If subtypes are enumerated dynamically, keep enumeration bounded and safe.
- [ ] Preserve actual crafting recipe transfer reliability fixed in Phase 3.

## 7.4 JEI/EMI guides for attached crops

- [ ] Add a guide/category explaining two host logs and four faces per level.
- [ ] Cocoa appears as the vanilla reference example.
- [ ] Ars definitions feed viewer information from the same data layer where practical.
- [ ] Sourceberry/wild-berry guide clearly communicates regrowth behavior.
- [ ] Viewer guides are descriptive; they must not silently redefine gameplay rules separately.

### Phase 7 acceptance gate

- [ ] UI understandable and regression-safe.
- [ ] Jade works with new mechanics and remains optional.
- [ ] JEI/EMI accurately describe new mechanics.
- [ ] No duplicate gameplay truth source introduced.
- [ ] Only then proceed to Phase 8.

---

# PHASE 8 - Full compatibility, migration and regression QA

**Goal: prove the update as a whole rather than testing each feature only in isolation.**

## 8.1 Loader/environment matrix

NeoForge 1.21.1 and Forge 1.20.1 should each be tested with relevant combinations:

- [ ] Farmer's Delight + Easy Villagers minimum base.
- [ ] Jade present / absent.
- [ ] JEI present / absent where appropriate.
- [ ] EMI present / absent where appropriate.
- [ ] Easy Mob Farm present / absent.
- [ ] Ars Nouveau present / absent.
- [ ] Argentum present / absent.
- [ ] Pale Garden Update present / absent.

Optional absence rule:

- EFDC must not reference optional classes/resources during startup in a way that crashes when the mod is absent.

## 8.2 Existing world migration matrix

Use copies, never the only copy of a real world.

From public 1.3.2:

- [ ] Existing Farmer variants load.
- [ ] Existing inventories/villagers/crops load.
- [ ] Existing Cutters load and fallback/preserve variant correctly.
- [ ] Existing Noise Switches load.

From archived internal checkpoint where useful:

- [ ] Easy Mob Farm Noise Switch assembly stage preserved.

New 1.4.0 state persistence:

- [ ] Dynamic Cutter variants survive save/reload.
- [ ] Host logs survive save/reload.
- [ ] All 8 attached-crop face states survive save/reload.
- [ ] Sourceberry/regrowth state survives save/reload.

## 8.3 Performance regression

- [ ] Repeat ~40 Rich Farmer visibility benchmark after all new crop systems are present.
- [ ] Ensure attached crops did not recreate high-frequency polling.
- [ ] Ensure new render complexity does not erase the Phase 2 improvement.

## 8.4 Gameplay regression

- [ ] Standard Rich Farmer crops.
- [ ] Rice/Paddy.
- [ ] Tomato/rope.
- [ ] Sugar Cane.
- [ ] Harvest tools/durability semantics.
- [ ] Full-output blocking/retry.
- [ ] JEI/EMI upgrade transfers.
- [ ] Dynamic Cutter crafting/stacking/persistence.
- [ ] Cocoa 8-face case.
- [ ] Four Ars Archfruit pairs.
- [ ] Sourceberry/wild berry lifecycle.
- [ ] Top-down dismantling: upper crops -> upper log -> lower crops -> lower log.

### Phase 8 acceptance gate

- [ ] NeoForge full matrix PASS.
- [ ] Forge full matrix PASS.
- [ ] Existing worlds PASS.
- [ ] Performance PASS after all features are combined.
- [ ] No severe regression/open crash remains.
- [ ] Only then proceed to Phase 9.

---

# PHASE 9 - Dedicated Prism Launcher QA instance and release candidate

**Goal: provide a minimal reproducible NeoForge QA environment and then produce RC builds.**

## 9.1 Prism instance target

Create an importable Prism Launcher instance:

- Minecraft **1.21.1**.
- NeoForge **21.1.x** compatible with the final EFDC build.
- Name suggestion: `Easy FD Compat 1.4.0 QA - NeoForge 1.21.1`.

Include only the ecosystem needed to test EFDC:

- [ ] EFDC 1.4.0 RC build.
- [ ] Farmer's Delight.
- [ ] Easy Villagers.
- [ ] Easy Mob Farm.
- [ ] Ars Nouveau.
- [ ] Argentum.
- [ ] Pale Garden Update.
- [ ] Jade.
- [ ] JEI.
- [ ] EMI.
- [ ] Only required transitive dependencies.

Do not add unrelated minimaps, performance mods, decoration, Create, voice chat, etc. The instance should make failures attributable.

## 9.2 Distribution hygiene

- [ ] Prefer Prism/CurseForge/Modrinth provider-managed third-party downloads when packaging rules allow.
- [ ] Do not blindly redistribute third-party JARs inside the instance package.
- [ ] EFDC local test JAR may be included as the artifact under test.

## 9.3 QA world

Create or document a small test world/layout with stations for:

- [ ] Standard Rich Farmer crops.
- [ ] 40-Farmer stress room.
- [ ] Paddy/Rich Paddy cases.
- [ ] Cutter variant wall: vanilla/Nether/Ars/Pale Garden/unknown.
- [ ] Cocoa lower/upper log test.
- [ ] Four Ars Archfruit host/fruit pair tests.
- [ ] Sourceberry/wild berry test.
- [ ] Noise Switch family.
- [ ] JEI/EMI transfer workbench.

### Phase 9 acceptance gate

- [ ] Prism QA instance imports and launches cleanly.
- [ ] NeoForge RC passes the complete test checklist.
- [ ] Forge RC passes equivalent checklist in the existing Forge QA environment.
- [ ] Only then proceed to Phase 10.

---

# PHASE 10 - Final 1.4.0 release preparation

## 10.1 Final versioning

- [ ] Change `mod_version` from `1.4.0-dev.N` / RC to exactly `1.4.0` only after all prior gates pass.
- [ ] Ensure NeoForge and Forge metadata use the same public semantic version.

## 10.2 Final source hygiene

For both loaders:

- [ ] Full clean-format scan.
- [ ] No tabs/trailing whitespace in Java/JSON/Markdown where not intentional.
- [ ] No compressed unreadable Java formatting.
- [ ] JSON/TOML parse.
- [ ] No generated build/cache/log/crash junk.
- [ ] No stale moved files.
- [ ] No unintended third-party artistic-resource references.
- [ ] Jade remains isolated/optional.
- [ ] `DEV_1.4.0_ROADMAP.md` remains gitignored.
- [ ] Public source/release package excludes this roadmap/internal debug artifacts.

## 10.3 Final deliverables

- [ ] NeoForge release JAR.
- [ ] Forge release JAR.
- [ ] Recoverable final full-source ZIP snapshot for NeoForge.
- [ ] Recoverable final full-source ZIP snapshot for Forge.
- [ ] CurseForge changelog in Markdown.
- [ ] Update README/CHANGELOG/version metadata coherently.

## 10.4 Public changelog boundary

User-facing topics suitable for the public 1.4.0 changelog include, if completed:

- Major Rich Farmer performance improvements.
- More reliable JEI/EMI Farmer upgrade recipe transfer.
- Dynamic Cutter wood variants including broader modded wood support.
- Nether stem Cutter support.
- Rich Farmer log-hosted Cocoa/attached crops.
- Ars Nouveau Archfruit support.
- Sourceberry/wild-berry Rich Farmer support.
- Easy Mob Farm Noise Switch if it is part of the final delta from public 1.3.2.
- Refreshed visuals/interfaces where worth mentioning.
- Compatibility fixes.

Do **not** expose in the public changelog:

- Internal legal-risk analysis.
- Copyright/licensing as the reason for rebuilding models/GUI.
- This roadmap.
- Debug-only migration reasoning.
- Failed development snapshots/build incidents.

---

# Cross-phase invariants

These requirements apply to every phase and are not optional cleanup for the end.

## World compatibility

- Do not rename existing registered blocks/items/BlockEntities casually.
- Preserve established NBT keys where practical.
- When adding data, use defaults so old worlds/items load safely.
- Every data-shape change must include an old-world load test.

## Optional integration safety

- Easy Mob Farm, Jade, JEI, EMI, Ars Nouveau, Argentum and Pale Garden behavior must be guarded according to whether each mod is actually required or optional in the relevant loader build.
- Optional absence must never cause classloading of unavailable APIs.

## Third-party boundary

- Prefer registry IDs/tags/APIs/reflection for interoperability over copying implementation/resources.
- Do not copy Ars/Easy Villagers/Easy Mob Farm visual assets into EFDC merely to reproduce appearance.
- Vanilla Minecraft resources may be referenced normally where permitted by the modding environment.
- `THIRD_PARTY_NOTICES.md` should remain a factual dependency/interoperability notice, not a claim of legal immunity.

## Clean code

- New complex behavior should be divided into coherent classes/helpers instead of extending one giant BlockEntity method indefinitely.
- Hot paths should be obvious and measurable.
- Avoid repeated reflection discovery, registry scans and allocation-heavy temporary structures in tick/render loops.
- Comments explain non-obvious invariants/compatibility reasons, not what straightforward code already says.

## Snapshot discipline

At the end of **every editing pass**:

1. Run static checks.
2. Create full NeoForge source ZIP.
3. Create full Forge source ZIP.
4. Provide both immediately, even if the pass is only partially complete.
5. If files were renamed/deleted, include/update cleanup migration helpers so overlay extraction cannot resurrect obsolete code.

---

# Definition of Done for Easy Farmer's Delight Compat 1.4.0

The release is not considered complete merely because it compiles.

It is complete only when:

- [ ] Both loaders build from clean source.
- [ ] Both loaders launch and pass smoke tests.
- [ ] Existing 1.3.2 worlds load without losing established machine state.
- [ ] Third-party visual/resource isolation is complete.
- [ ] Jade is optional and isolated.
- [ ] Dense Rich Farmer performance is materially improved and measured.
- [ ] Mature harvest is event-driven and reliable.
- [ ] JEI/EMI Farmer upgrade transfer is reliable.
- [ ] Dynamic Cutter variants work with vanilla, Nether stems and modded logs without per-mod hardcoding.
- [ ] Cocoa attached-log mechanic works with 2 logs / 8 independent faces.
- [ ] Ars Archfruit integration works through the generic compatibility layer.
- [ ] Sourceberry/wild-berry Rich Farmer behavior works with correct regrowth semantics.
- [ ] Full Forge/NeoForge QA matrix passes.
- [ ] Minimal Prism NeoForge QA instance passes.
- [ ] Final code/resource/format audit passes.
- [ ] Final public changelog contains only user-facing information.
