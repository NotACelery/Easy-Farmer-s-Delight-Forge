# Easy Farmer's Delight Compat — Developer Reference

This document is the technical reference for the Forge 1.20.1 backport codebase.

The source intentionally keeps inline comments to a minimum. Cross-class behavior, compatibility decisions, persistence rules and non-obvious invariants belong here instead of being duplicated throughout implementation files.

## 1. Project scope

Easy Farmer's Delight Compat is an independent compatibility addon between Easy Villagers and Farmer's Delight. It adds three Farmer variants, an automated Cutter, two client-local noise controls, and optional in-game documentation through Jade, JEI and EMI.

Current release line:

- Minecraft: `1.20.1`
- Forge baseline: `47.4.23`
- Forge minimum: `47.2.0`
- Java: `17`
- Easy Villagers minimum: `1.1.39`
- Farmer's Delight minimum: `1.3.3`
- Mod version: `1.3.2`

Optional integrations:

- Jade
- JEI
- EMI
- Ars Nouveau
- Argentum

## 2. Source tree

The main package is `dev.celerbi.easyfarmersdelightcompat`.

### `block`

World interaction and block-level state:

- `CompatFarmerBlock` — common block for Paddy, Rich and Rich Paddy Farmers.
- `FarmerVariant` — compact capability model for `PADDY`, `RICH` and `RICH_PADDY`.
- `CutterBlock` — Cutter placement, interaction, state preservation and menu access.
- `VillagerNoiseSwitchBlock` — physical Villager Noise Switch interactions.
- `IronFarmNoiseSwitchBlock` — Iron Farm Noise Switch assembly and interaction.

### `blockentity`

Persistent machine state and server-side work:

- `CompatFarmerBlockEntity` — Farmer crop state, harvest logic, virtual crop lifecycle, tool state and Easy Villagers payload preservation.
- `CutterBlockEntity` — Cutter inventory, Villager state, work planning, processing and automation handlers.
- `VillagerNoiseSwitchBlockEntity` — stored Villager state for the Villager Noise Switch.
- `IronFarmNoiseSwitchBlockEntity` — Iron Block assembly stage and completed Golem state.

### `integration`

Compatibility boundaries and viewer-neutral domain data:

- `EasyVillagersFarmerAdapter` — reflection bridge to the Easy Villagers Farmer surface used by the addon.
- `CutterVillagerAdapter` — Easy Villagers Villager serialization/aging bridge for the Cutter.
- `NoiseSwitchVillagerAdapter` — Easy Villagers VillagerData bridge for the Villager Noise Switch.
- `FarmersDelightAdapter` — narrow bridge for Farmer's Delight configuration values.
- `CuttingRecipeResolver` — runtime access to Farmer's Delight cutting recipes.
- `AxeActionResolver` — Axe transformations used as Cutter fallback operations.
- `OutputSimulator` — lossless output-capacity simulation.
- `FarmerToolSupport` — Knife/Hoe/Axe classification and representative tool stacks.
- `ToolRequirement` — live hard requirement for the operation currently waiting to run.
- `ToolUse` — viewer-only semantic role of a tool in documentation.
- `RecipeViewerData` — shared JEI/EMI documentation dataset.
- `BlockGuideInfo`, `FarmerHarvestInfo`, `GuideIngredient`, `CutterAxeInfo` and `CutterAxeActionRow` — viewer-neutral presentation models.

### `integration/jei` and `integration/emi`

Viewer-specific rendering and recipe-transfer adapters. Gameplay rules do not originate here.

### `compat/jade`

Read-only machine diagnostics for Jade. Providers surface state that already exists in gameplay classes; they do not own gameplay behavior.

### `client`

Screens, renderers, client preferences and final sound-event filtering.

### `recipe`

State-preserving custom recipes and recipe utilities.

### `registry`

Deferred registrations for blocks, items, block entities, menus, serializers and creative-tab integration.

### `event`

Compatibility/migration event handling for legacy Farmer items.

## 3. Farmer variants

`FarmerVariant` models two independent capabilities:

| Variant | Rich | Aquatic |
| --- | --- | --- |
| Paddy | No | Yes |
| Rich | Yes | No |
| Rich Paddy | Yes | Yes |

The block and block entity use those flags instead of maintaining three unrelated implementations.

### Paddy capability

Aquatic variants support the Paddy-specific state machine used for Rice and Sugar Cane.

### Rich capability

Rich variants expose the protected Harvest Tool slot and Rich Soil-aware crop behavior.

## 4. Farmer persistence model

`CompatFarmerBlockEntity` owns addon-specific state while preserving the Easy Villagers payload it does not own.

Addon NBT keys currently include:

- `EfdcSchema`
- `EfdcPaddyGrowth`
- `EfdcBaseProgress`
- `EfdcRopeOneProgress`
- `EfdcRopeTwoProgress`
- `EfdcRopeCount`
- `EfdcHarvestTool`
- legacy `EfdcKnife`
- `EfdcFruitReady`
- `EfdcPaddySand`
- `EfdcSugarCaneHeight`
- `EfdcSugarCaneAge`

Unknown Easy Villagers/future payload is retained in `passthroughData`. Known metadata and known empty values may be stripped when deciding whether a machine is actually stateful, but unknown data must not be silently discarded just to make an item stackable.

### Item stacking invariant

A completely empty Farmer may remain a normal stackable item.

A Farmer with meaningful machine state is serialized into the dropped item and forced to stack size `1`. This prevents placing multiple copies of one stateful ItemStack and duplicating its Villager, crop or inventory state.

### Creative Pick Block

Creative Pick Block intentionally returns a clean machine item. Normal block drops are the state-preserving path.

### Upgrade recipes

`RecipeUtil.upgradeFarmer` starts from the canonical target item and copies only meaningful state:

- custom name;
- lore;
- meaningful block-entity data.

Transient Easy Villagers client/render cache is removed from the upgraded result. Structurally present but semantically empty machine payload is not enough to make the upgraded item stateful.

## 5. Easy Villagers compatibility boundary

The addon deliberately keeps Easy Villagers implementation access concentrated in adapter classes.

`EasyVillagersFarmerAdapter` is responsible for the Farmer surface used by the addon, including crop selection, stored Villager behavior, inventory access, aging and `farm_speed` lookup.

`CutterVillagerAdapter` and `NoiseSwitchVillagerAdapter` use narrower bridges because those blocks do not need the whole Farmer integration.

### Failure behavior

Reflection failures are contained inside their adapter. A failed reflective lookup must not turn into undefined state in unrelated classes.

A failed `farm_speed` lookup must not silently become a one-tick machine. The Farmer adapter falls back to the expected default behavior instead of creating accidental extreme acceleration.

### Payload synchronization

Easy Villagers-owned keys must be mirrored including removals. Keeping only additions would leave stale data in persistent machine state.

## 6. Farmer server work cadence

Ordinary Farmer machine work runs on a one-second cadence. Easy Villagers `farmSpeed` remains the common growth gate for normal crop progression.

Growth and harvest are intentionally separate phases:

1. a crop uses its growth cadence until mature;
2. once mature, the machine retries the concrete harvest on each normal work cadence;
3. missing tools, an invalid Villager state or insufficient output space leave the mature crop waiting;
4. successful harvest is what resets or advances the relevant persistent crop state.

This avoids the old behavior where a visibly mature crop could wait for another unrelated random growth roll before harvesting.

## 7. Rice lifecycle

The internal Paddy Rice state uses `0..7`:

- `0..3` — submerged lower Rice ages `0..3`;
- `4..7` — upper panicles ages `0..3`, while the lower Rice remains mature.

When mature panicles are successfully harvested, the state returns to `3`. The mature submerged plant remains and only the upper half regrows.

A mature Rice harvest waits if the complete output cannot fit. The machine does not consume/reset panicles and then lose overflow.

### Rich Paddy Rice

Rich Paddy receives a separate virtual Rich Soil opportunity. Rice harvesting itself still follows the same persistence and output-safety rules.

A Knife is optional for Rice; it affects Knife-sensitive loot behavior but is not a hard blocker for normal Rice harvesting.

## 8. Sugar Cane lifecycle

Sugar Cane uses two pieces of virtual state:

- stored height;
- vanilla-style age `0..15`.

A successful Easy Villagers growth opportunity advances the internal age. Reaching the end of the age cycle creates the next Cane section, up to the normal three-block height.

Rich Soil does not accelerate Sugar Cane in this addon.

Removing Paddy Sugar Cane mode returns the Sand and the persistent base Cane that belong to the player.

## 9. Tomato lifecycle

Farmer's Delight Tomatoes begin through `budding_tomatoes`, then transition into the persistent Tomato vine.

The Rich Farmer stores independent progress for:

- base Tomato section;
- first Rope section;
- second Rope section.

Rope sections grow independently. Mature sections can harvest without waiting for another growth RNG roll.

For mature Tomato loot, gameplay uses Farmer's Delight's real loot behavior so Hoe/Fortune-sensitive results stay authoritative. Compatibility with the declared Farmer's Delight 1.2.9 minimum is preserved through the legacy rope-logged Tomato state where required.

## 10. Mushroom Colonies

Red and Brown Mushroom Colonies use a persistent Rich Farmer lifecycle.

Growth is allowed without a Knife. A mature Colony waits for a Knife before the harvest itself.

The Knife is a hard operation requirement but is not damaged by the Colony harvesting behavior implemented by the Farmer.

## 11. Melon and Pumpkin stems

Vanilla Melon/Pumpkin seeds are recognized explicitly because Easy Villagers does not treat their stems like ordinary villager-plantable crops.

The stored lifecycle separates:

1. stem growth;
2. fruit generation;
3. ready-fruit harvest.

Rich Soil accelerates stem growth only. Fruit generation remains on the normal work path.

Once fruit exists, the machine retries harvest on the normal work cadence while waiting for an adult Villager, an Axe and enough output space. It does not demand another growth RNG success.

The virtual Rich Soil behavior for stems follows a random-tick-style opportunity rather than directly multiplying the normal Easy Villagers `farmSpeed` work loop.

## 12. Harvest Tool semantics

`FarmerToolSupport` defines accepted tool categories:

Rich Farmer / Rich Paddy Harvest Tool slot:

- Knife tag: `forge:tools/knives`
- vanilla Hoe tag (`ItemTags.HOES`)
- vanilla Axe tag (`ItemTags.AXES`)

Cutter Cutting Tool slot:

- Knife
- Axe

`ToolRequirement` is deliberately narrower than “accepted tool”. It describes what is blocking the operation that is ready right now.

Examples:

- a Rich Farmer accepts Knife, Hoe and Axe;
- a mature Mushroom Colony specifically requires a Knife;
- a ready Melon/Pumpkin specifically requires an Axe;
- normal crops may use a Hoe without making that Hoe a mandatory blocker.

## 13. Output safety

Outputs must be simulated before mutation whenever a multi-stack operation could overflow.

`OutputSimulator` implements the common rule:

1. copy the current output state;
2. simulate stacking every generated ItemStack;
3. reject the operation if any remainder survives;
4. only mutate the real inventory after the whole operation is known to fit.

The Farmer has equivalent capacity checks for its container-backed output path.

This rule is important for random loot, multi-output cutting and stateful harvests. Partial insertion followed by reset would cause silent item loss.

## 14. Cutter architecture

The Cutter owns:

- one stored Villager;
- one Cutting Tool slot;
- four input slots;
- four output slots;
- a persisted work-surface/log variant;
- processing progress.

The normal process time is `10` ticks.

### Work-plan caching

The Cutter does not continuously roll recipes or blindly advance its animation.

A work plan is probed non-destructively. It is cached until tool/input/output contents change. Progress starts only after a concrete input/tool pair has been shown to be processable.

If output capacity or another prerequisite changes between probing and completion, the machine parks until contents change instead of repeatedly executing a failing operation.

### Cutting recipes

`CuttingRecipeResolver` uses Farmer's Delight cutting recipes as the authoritative runtime rules.

Its probe path does not roll outputs or consume RNG. The execution path is the place that may produce chance outputs.

### Axe fallback actions

When no Farmer's Delight cutting recipe applies, supported Axe actions are resolved through `AxeActionResolver`, including normal stripping/scraping/wax-removal transformations supplied through Minecraft/Forge behavior.

### Tool durability

A damageable Cutter tool loses durability only after an operation succeeds. Failed probes, output-full states and invalid inputs do not damage the tool.

### Automation sides

`CutterBlockEntity` exposes specialized handlers for top, side and bottom automation rather than one unrestricted inventory interface. Tool/input/output rules remain authoritative regardless of whether an item arrives through the GUI or automation.

## 15. Cutter Villager state

The Cutter reuses Easy Villagers VillagerItem serialization and aging semantics through `CutterVillagerAdapter`.

The displayed/serialized Villager remains owned by the Cutter block entity. The temporary reflected Easy Villagers delegate exists only to reuse the required Easy Villagers behavior.

## 16. Villager Noise Switch

The physical block does not have a real powered Redstone state.

The mute preference is client-local and stored by `ClientPreferences` in:

`config/easyfarmersdelightcompat-client.properties`

Relevant property:

- `villagersMuted`

The renderer reads the local preference, so two clients can look at the same physical block and legitimately see different local switch states.

### Sound routing

Villager sound filtering runs only on the logical client. This is critical in singleplayer because the integrated server exists in the same process; a client preference must never cancel a server event that belongs to other players.

Easy Villagers contained-Villager voices originate through the `BLOCKS` sound source. When not muted, the addon reroutes the recognized Villager sounds to `NEUTRAL` so the Friendly Creatures volume control behaves naturally while keeping the volume Easy Villagers already calculated.

## 17. Iron Farm Noise Switch

The Iron Farm Noise Switch stores assembly state until four Iron Blocks and the final Carved Pumpkin have completed the miniature Golem.

The completed Golem state is persistent and the item is always non-stackable.

Client preference key:

- `ironFarmSoundsMuted`

### Surgical sound filter

The filter only cancels:

- Zombie Ambient;
- Iron Golem Hurt;
- Iron Golem Death.

The sound must also:

- use the `BLOCKS` source;
- originate from the exact block position of `easy_villagers:iron_farm`;
- be processed on the client;
- have the local Iron Farm mute enabled.

Real Zombies and real Iron Golems elsewhere are unaffected.

## 18. Rendering

Placed machines and stateful inventory items use dedicated renderers.

Important rendering responsibilities include:

- keeping virtual Farmer crops inside the enclosure;
- matching Easy Villagers Villager transforms closely enough that stored Villagers look native to the machine family;
- rendering Rice halves, Tomato trellises, Sugar Cane and stem/fruit pairs from persistent virtual state;
- previewing stateful Farmer/Cutter/Noise Switch items in inventory;
- rendering the local client switch state without writing physical block state.

Renderers must remain presentation-only. They may read synchronized/persisted state but must not advance gameplay.

## 19. Jade architecture

Jade providers expose diagnostics such as:

- crop/growth state;
- Harvest Tool state;
- hard tool blockers;
- Cutter progress and output information;
- Villager Noise Switch local status;
- Iron Farm Noise Switch assembly and local status.

Diagnostics must use non-destructive probes. In particular, Cutter diagnostics must never execute a random cutting recipe merely to display what could happen.

The historical Jade provider UID for the old Knife-specific display is intentionally preserved by the generalized Harvest Tool provider so user-side Jade configuration does not reset.

## 20. JEI and EMI architecture

`RecipeViewerData` is the single viewer-neutral source for instructional data.

JEI and EMI map that shared data into their own APIs. Gameplay does not read `RecipeViewerData`, and viewer classes must not become a second gameplay ruleset.

Displayed outputs may be examples. Real gameplay loot tables remain authoritative where a crop has enchantment-sensitive or random output.

### EMI stateful Farmer upgrades

The Farmer upgrade recipes require a custom EMI transfer path because a real Easy Villagers Farmer may contain NBT/block-entity state.

The normal ingredient can correctly accept that Farmer, while a generic transfer routine based on concrete ItemStack NBT equality can still fail while moving it.

`FarmerUpgradeEmiRecipeHandler` therefore moves the exact inventory stack. Batch/max fill only groups mutually stackable ItemStacks, which keeps clean Farmers batchable while isolating stateful Farmers whose NBT differs or whose max stack size is one.

The crafting menu remains authoritative for actual recipe consumption and remainders.

## 21. Recipes and state preservation

Farmer upgrade recipes are real gameplay recipes rather than viewer-only approximations. JEI/EMI therefore see the same shaped inputs that crafting uses.

The source Farmer's meaningful state is preserved through the upgrade path.

The Cutter recipe also persists the selected compatible log/bamboo material as its Cutter variant.

## 22. Optional integrations

Jade, JEI and EMI are optional APIs. Their absence must not prevent the base addon from loading.

Ars Nouveau and Argentum compatibility is opportunistic and should continue to use normal registry/tag compatibility where possible instead of turning either mod into a mandatory dependency.

## 23. Build configuration

The project uses ForgeGradle 6 with Java 17.

`build.gradle` intentionally keeps Jade, JEI and EMI as compile-only/API-side dependencies rather than embedding them in the mod JAR.

Windows build launcher:

```text
build.bat
```

The batch file delegates to `build.ps1`. The PowerShell helper locates or bootstraps a JDK 17, resolves Gradle `8.8`, runs `clean build --no-daemon --stacktrace --console=plain`, writes `build.log`, and verifies that a runtime JAR appears in `build/libs`.

## 24. Forge 1.20.1 backport boundaries

This repository is a source-level backport, not a loader shim. Keep loader/version adaptation explicit instead of trying to make NeoForge 1.21.1 source compile unchanged.

Important Forge-specific boundaries:

- registries use Forge `DeferredRegister` / `RegistryObject`;
- menus use `IForgeMenuType` and `NetworkHooks`;
- item automation uses Forge item handlers and `LazyOptional` capability exposure;
- stateful items use 1.20.1 `BlockEntityTag`/NBT semantics rather than 1.21 data components;
- custom shaped upgrade recipes use the 1.20.1 `CraftingContainer`, `NonNullList<Ingredient>` and serializer APIs;
- the Cutter resolves Farmer's Delight 1.20.1 cutting recipes through the runtime surface available in that release;
- Knife discovery uses the Forge `forge:tools/knives` tag;
- resource/datapack paths and metadata follow 1.20.1 Forge conventions;
- Java source must remain Java 17 compatible.

When porting a NeoForge change back here, preserve behavior first and translate APIs second. Never copy a 1.21-only type, data component, registry helper or Java 21 construct into this branch without a 1.20.1 equivalent.

## 25. Formatting and source conventions

The codebase is intentionally formatted for readability rather than compactness.

Rules for future edits:

- Java uses four-space indentation.
- Keep one logical statement per line.
- Do not compress multiple declarations, branches or method bodies into a single physical line merely to reduce line count.
- Prefer a practical line-width target of roughly 120 characters; long user-facing/debug strings may exceed it when splitting the literal would make the source worse.
- Keep explanatory source comments exceptional. If a behavior needs cross-class rationale, document it here.
- Comments are still appropriate when they are legally required, generated by external tooling, or when the code cannot communicate a tiny local constraint clearly on its own.
- JSON resources use stable pretty-print formatting and a final newline.
- `build.gradle`, TOML, shell and batch helpers should remain consistently indented and free of obsolete commentary.

The repository includes `.editorconfig` so compatible editors inherit the basic whitespace rules automatically.

## 26. Change discipline

Formatting-only passes and behavior changes should remain separate whenever possible.

For a formatting pass:

1. preserve Java tokens other than comments/whitespace;
2. verify every Java file still parses;
3. verify JSON resources still parse to the same data;
4. run the Gradle build when dependencies and network/cache are available;
5. review the diff for accidental logic changes.

For behavior changes, update this document when an invariant, persistence rule, compatibility boundary or machine lifecycle changes.

## 27. High-value regression checks

After changes to Farmer logic:

- empty vs stateful Farmer stacking;
- normal block drop state preservation;
- Creative Pick Block cleanliness;
- Paddy Rice lower/panicle lifecycle;
- Rich Paddy boost behavior;
- Sugar Cane age/height and teardown returns;
- Tomato base + Rope section growth;
- Mushroom Colony Knife blocker;
- Melon/Pumpkin Axe blocker and fruit-ready retry;
- output-full behavior without state reset/item loss;
- tool durability only on successful actions.

After changes to Cutter logic:

- Villager presence/adult requirement;
- Knife and Axe validation;
- cutting-recipe execution;
- Axe fallback actions;
- output simulation;
- progress reset/parking;
- hopper/automation sided behavior;
- stateful item persistence and inventory preview.

After changes to sound switches:

- client-local persistence;
- no server/global cancellation;
- Villager mute and Friendly Creatures routing;
- Iron Farm filter only at `easy_villagers:iron_farm` positions;
- real mobs remain audible;
- item/block render state remains synchronized with the intended local/persistent source.

After viewer changes:

- JEI and EMI show equivalent shared guide data;
- viewer probes do not consume RNG;
- stateful Farmer recipe transfer still uses the exact inventory stack;
- Jade remains diagnostic only.
