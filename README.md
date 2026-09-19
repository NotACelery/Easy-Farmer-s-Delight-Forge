# Easy Farmer's Delight

Current release: **1.4.4** (Minecraft 1.20.1 / Forge 47.4.x / Java 17)

<p align="center">
  <img src="easy-farmers-delight-cover.png" alt="Easy Farmer's Delight" width="320">
</p>

**Easy Farmer's Delight** is an independent, unofficial expansion for **Easy Villagers** and **Farmer's Delight**.
It extends villager-powered farming beyond normal field crops with Paddy farming, Rich Soil mechanics, attached-log
fruit, regrowing bushes, dynamic Cutting Board automation and client-local sound controls.

> This project is not affiliated with or endorsed by the authors of Easy Villagers, Farmer's Delight, Ars Nouveau,
> Easy Mob Farm, Jade, JEI, EMI or other supported mods.

> **World compatibility:** the public project name is **Easy Farmer's Delight**, but the technical mod/registry
> namespace remains `easyfarmersdelightcompat`. Existing registered blocks/items, BlockEntity IDs, NBT keys, recipes
> and saved worlds therefore retain their established identity.

For the complete crop matrix and host rules, see **[SUPPORTED_CROPS.md](docs/SUPPORTED_CROPS.md)**.
For architecture and persistence details, see **[DEVELOPMENT.md](docs/DEVELOPMENT.md)**.
For third-party interoperability/resource boundaries, see **[THIRD_PARTY_NOTICES.md](docs/THIRD_PARTY_NOTICES.md)**.

## Main features

- **Paddy Farmer** for Farmer's Delight Rice and Sand-based Sugar Cane.
- **Rich Farmer** with Rich Soil, Harvest Tools, Tomatoes/Rope, Mushroom Colonies, Melon/Pumpkin, regrowing bushes,
  attached-log crops and productive **Orchards** built with the Grafting Support.
- **Rich Paddy Farmer** combining the Paddy lifecycle with Rich Soil acceleration for Rice.
- **Rich Farmer Log Mode** with two independent host logs and up to 8 attached crop faces.
- Built-in optional **Ars Nouveau** support for Magebloom, Sourceberry and all four Archfruits.
- Optional **Argentum** seed integration through the normal villager seed tag.
- Optional compatibility with **Hearth & Harvest**, **Regions Unexplored**, **Twilight Forest**, **Deep Aether**,
  **Eternal Starlight** and **Croptopia** through the Rich Farmer crop/orchard systems.
- Optional **Delightful 3.8.x** compatibility through the Rich Farmer regrowing-crop lifecycle.
- Optional **Fruits Delight 1.1.3** compatibility across Rich Farmer crop and Orchard lifecycles.
- **Cutter** automation for Farmer's Delight Cutting Board recipes plus Axe transformations, including Croptopia Cinnamon bark recovery when available.
- Dynamic Cutter work surfaces for **any compatible unstripped base log from any mod** that follows Minecraft's
  standard log tags; the selected block is rendered using its own source model/texture/animation.
- **Villager Noise Switch**, **Iron Farm Noise Switch**, and optional **Easy Mob Farm Noise Switch**.
- Optional **Jade**, **JEI**, and **EMI** integrations.
- `/farm` operator command for reproducible configured Farmer grids.
- Event-driven Farmer/Cutter standby designed to avoid repeated blocked-work polling.

## Farmer variants

### Paddy Farmer

The Paddy Farmer handles crop lifecycles that need a flooded/specialized enclosure.

**Rice** is inserted directly and follows the complete Farmer's Delight Rice lifecycle.

**Sugar Cane** requires an empty Paddy Farmer, then Sand, then Sugar Cane. It grows to three blocks high and
harvests only the upper sections, leaving the base planted. Sugar Cane is deliberately **not** accelerated by Rich
Soil, including in a Rich Paddy Farmer.

### Rich Farmer

The Rich Farmer keeps the normal Easy Villagers crop route and adds specialized mechanics:

- Rich Soil acceleration where allowed by Farmer's Delight.
- Tomatoes with 0–2 independent Rope sections.
- Red/Brown Mushroom Colonies.
- Melon/Pumpkin virtual stem + fruit handling.
- Sweet Berry Bush and Ars Nouveau Sourceberry regrowth.
- Cocoa on Jungle Logs.
- Ars Nouveau Bombegranate, Mendosteen, Frostaya and Bastion fruit on their matching Archwood host families.
- Two independently selectable attached host logs, allowing mixed crops in one Farmer.
- **Grafting Support / Orchard mode:** productive leaf canopies are data-driven across vanilla and supported
  optional fruit-tree providers, with safe ownership/migration rules for Apple Orchards.
- **Croptopia ground-crop compatibility:** its 58 normal farmland crops use the standard Rich Farmer crop lifecycle and harvest correctly without bespoke per-crop handlers.

The base Easy Villagers Farmer itself is not modified to gain these special mechanics.

### Rich Paddy Farmer

The Rich Paddy Farmer keeps Paddy Rice/Sugar Cane behavior and adds the Harvest Tool slot. Rich Soil speeds Rice,
but **not Sugar Cane**.

## Harvest Tools

Rich Farmer and Rich Paddy Farmer expose a protected Harvest Tool slot.

| Tool | Use |
| --- | --- |
| **Knife** | Required for mature Mushroom Colonies; optional for Rich Paddy Rice |
| **Hoe** | Optional for compatible normal crops and Tomatoes; may carry Fortune into real crop loot |
| **Axe** | Required for mature Melons/Pumpkins |
| **Shears** | Required for mature Orchards; normal durability and Unbreaking apply |

A tool is only consumed/damaged when the relevant mechanic says it should be. Blocked output does not consume the
crop or damage the tool.

## Orchards and the Grafting Support

The **Grafting Support** is a permanent Easy Farmer's Delight mechanic and does not require Croptopia. Craft it from
Farmer's Delight Rope, Sticks, any Minecraft log and Hanging Roots. It can either be placed directly in the world or
installed into an empty Rich Farmer.

- A placed support accepts **any block in `minecraft:leaves`**. Unsupported leaves remain a decorative canopy forever.
- A productive canopy grows only while the support is directly above **Farmer's Delight Rich Soil**.
- **Oak Leaves** and **Dark Oak Leaves** become an Apple Orchard on Rich Soil and follow four internal fruit stages: bud → flower → young fruit → mature fruit. Mature harvest yields 2 Apples with a 30% chance for a third.
- A mature placed Orchard is harvested manually by right-clicking with **Shears**; fruit drops into the world beside the support.
- The canopy is a real breakable upper block: Shears or Silk Touch recover the inserted leaves, while other tools/hand destroy the leaves. The lower Grafting Support remains in place.
- In a **Rich Farmer**, the same Orchard lifecycle is automated. Mature fruit waits for Shears in the Harvest Tool slot and output capacity.
- Shears lose durability only after a successful harvest and normal **Unbreaking** behavior applies.
- Rich Soil accelerates Orchard fruit growth without multiplying the configured harvest roll.
- **Croptopia**, **Fruits Delight** and **Regions Unexplored** orchard definitions retain their native visual
  progression where supported.
- Dedicated apple-tree providers take ownership over vanilla Oak/Dark Oak production when installed; the vanilla
  Apple Orchard remains the fallback when no provider is present.
- Provider-specific non-orchard fruit mechanics remain decorative rather than being forced into Orchard automation.

The Grafting Support itself is always available. Optional orchard integrations are resolved through registry IDs and
data definitions rather than hard Java dependencies.

## Attached log crops

A Rich Farmer can install a **lower** and **upper** host log. Each one has four horizontal crop faces, giving up to
8 independently growing attached crops.

The host check is strict and data-driven:

- Cocoa Beans require `minecraft:jungle_logs`.
- Bombegranate Pod requires `ars_nouveau:blazing_logs`.
- Mendosteen Pod requires `ars_nouveau:flourishing_logs`.
- Frostaya Pod requires `ars_nouveau:cascading_logs`.
- Bastion Pod requires `ars_nouveau:vexing_logs`.

Just like vanilla Cocoa, putting the right seed against the wrong log does not plant it. The same rule extends to
modded attached crops. Lower and upper logs can be different, so one Rich Farmer can grow two attached-crop families
at once.

Sneak-use dismantles in this order: upper crops → upper log → lower crops → lower log.

See [SUPPORTED_CROPS.md](docs/SUPPORTED_CROPS.md) for the complete behavior, Rich Soil rules and datapack format.

## Regrowing bushes

Regrowing crops are harvested without destroying the underlying plant and return to their native post-harvest age.
Built-in support includes Sweet Berry Bushes, Ars Nouveau Sourceberry, Hearth & Harvest Blueberry/Raspberry,
Regions Unexplored Salmonberry and the supported Twilight Forest food berry bushes. Rich Soil may accelerate
compatible bushes but does not directly increase their harvest roll.

### Delightful Cantaloupe (Forge 1.20.1 only)

When **Delightful 3.8.x** is installed, a Rich Farmer accepts `delightful:cantaloupe_seeds` as a regrowing crop.
The virtual plant follows Delightful's native four ages (`0..3`), produces exactly one `delightful:cantaloupe` at
maturity and resets to age `0` after harvest instead of being destroyed/replanted. Delightful remains optional and
no Delightful Java classes are referenced by Easy Farmer's Delight.

See `docs/SUPPORTED_CROPS.md` for exact rules.

### Fruits Delight crops (Forge 1.20.1)

With **Fruits Delight 1.1.3** installed, the Rich Farmer additionally supports its native crop families instead of
treating them as generic crops: Blueberry and Cranberry regrow after harvest; Pineapple is planted from
`fruitsdelight:pineapple_sapling`; Lemon is rendered as a staged two-block crop planted from
`fruitsdelight:lemon_seeds`; and Hamimelon uses its native stem/attached-stem/fruit lifecycle. The integration is
registry/data-driven and Fruits Delight remains optional.

## Ars Nouveau support

Ars Nouveau is optional and is never classloaded as a hard dependency for crop support. When installed, Easy
Farmer's Delight explicitly supports:

- **Magebloom** through the normal seed/crop path.
- **Sourceberry** through the regrowing-bush system.
- **Bombegranate**, **Mendosteen**, **Frostaya**, and **Bastion Fruit** through attached-log definitions.
- Ars Nouveau **Archwood logs as Cutter work surfaces** when they participate in the standard Minecraft log tags.
- Source Ars models/textures/animations are rendered by Minecraft directly; they are not copied into this project.

## Cutter

The Cutter is a villager-powered automated Farmer's Delight Cutting Board.

It has 4 input slots, a protected Cutting Tool slot, and 4 output slots. It processes real Farmer's Delight Cutting
recipes and can also perform familiar Axe transformations such as log stripping, Copper scraping and wax removal.

### Universal modded-log work surfaces

The Cutter does not maintain a hardcoded wood whitelist. Its crafting/work-surface ingredient accepts any
**unstripped base log/stem** exposed through Minecraft's standard log tags, plus the historical addon tag as a
legacy datapack fallback.

This means compatible logs from other mods can work automatically. The selected log registry ID is saved on the
Cutter item/BlockEntity. The renderer asks Minecraft to draw that actual block, so animated/connected/source-specific
visuals remain owned by and rendered from the original mod. Ars Nouveau Archwood and vanilla Crimson/Warped stems
therefore keep their source appearance and animation.

Stripped variants, `_wood` blocks and `_hyphae` blocks are intentionally excluded as Cutter base work surfaces.
If a saved modded log no longer exists, Oak Log is the safe fallback.

## Event-driven performance model

Farmer and Cutter blocked-work paths are designed to sleep instead of polling expensive state every tick.

- Mature harvest blocked by **output** waits for a real output-capacity increase.
- Harvest blocked by a **tool** wakes when the Harvest Tool changes.
- Harvest blocked by a **baby villager** wakes when the stored villager becomes adult or relevant state changes.
- Attached crop faces are independent: one blocked fruit type cannot freeze a different mature fruit that still fits
  in the shared output inventory.
- Pending output drops are reused where possible instead of rerolling loot after every failed capacity check.
- Cutter idle/full-output states similarly park until an input/tool/output/villager event can actually change work.

The shared 1.4.x architecture was stress-tested on the NeoForge counterpart with **513 Rich Farmers**, reaching normal 100–120 FPS while the machines remained loaded but
fully occluded, with the remaining slowdown tied primarily to rendering hundreds of visible villager/crop models.

## Farmer item state and tooltips

Machine items preserve meaningful stored state when mined. Empty Farmers can stack; stateful Farmers are kept
individual so contents cannot be duplicated.

Hovering an Easy Farmer's Delight Farmer item shows its planted crop. Mixed attached-crop Farmers show each distinct
stored crop, making configured machines easy to organize before placement.

## `/farm` operator command

Coordinates use vanilla **X Y Z** order:

```text
/farm <from> <to> <farm> <villager:true|false> <crop-or-none> [extra]
```

Supported Farmer names:

- `paddy_farmer`
- `rich_farmer`
- `rich_paddy_farmer`
- full legacy registry IDs are also accepted

Optional setup argument:

- `rope=0`, `rope=1`, `rope=2` — Tomato Rope count.
- `sand` — Paddy/Rich Paddy Sugar Cane mode.
- `logs=1`, `logs=2` — attached-log crops; omitted attached setup defaults to one canonical compatible log.

Example:

```text
/farm -18 129 -21 17 129 -57 rich_farmer true farmersdelight:tomato_seeds rope=2
```

The full target region and Farmer/crop combination are validated before placement. Vanilla `/fill` is untouched.

## Noise Switch family

### Villager Noise Switch

Stores an Easy Villagers Villager and toggles a persistent **client-local** Villager voice preference. It does not
create a real Redstone signal.

### Iron Farm Noise Switch

After placement, insert four Iron Blocks and then a Carved Pumpkin to assemble the permanent miniature Iron Golem.
The switch mutes only the synthetic Zombie Ambient / Iron Golem Hurt / Iron Golem Death sounds emitted from the exact
position of Easy Villagers Iron Farms. Real Zombies and Iron Golems remain audible.

### Easy Mob Farm Noise Switch

Registered only when **Easy Mob Farm** is installed. Insert six Rotten Flesh to assemble the decorative vanilla
Zombie model. The completed switch mutes only Easy Mob Farm's captured-mob display entities for the local client;
real world mobs are unchanged.

All Noise Switch assembly/state data is preserved when mined.

## JEI, EMI and Jade

These integrations are optional.

- JEI/EMI show Farmer harvesting/Harvest Tool guidance and contextual Block Guide pages.
- Farmer upgrade transfer uses the real gameplay recipes/state-preserving behavior.
- Cutter remains exposed as a Farmer's Delight Cutting workstation/catalyst where appropriate.
- Jade reports crop/growth state, attached host occupancy, Harvest Tool blockers, Paddy/Sugar Cane state, Cutter
  status and Noise Switch state/assembly progress.
- Optional integrations are isolated so their absence does not classload unavailable APIs.

## Visual/resource independence

Easy Farmer's Delight does not redistribute Easy Villagers models, textures or GUI images. Its machine enclosure
resources are project/vanilla based, while dynamic contents deliberately ask Minecraft to render the original
Villager/crop/log model from the owning game/mod.

This is why modded logs and animated stems can keep their native appearance without Easy Farmer's Delight copying
those assets.

## Bamboo scope

Bamboo is **not a supported Farmer crop** in 1.4.4. Its vertical structural growth is outside the crop families
implemented by this release. Bamboo Block can still participate in Cutter Axe/log behavior if exposed through the
standard game transformation/tag systems.

## Requirements

Required:

- Minecraft **1.20.1**
- Forge **47.2.0+** (development baseline 47.4.23)
- Easy Villagers **1.1.39+**
- Farmer's Delight **1.3.3+**

Optional:

- Croptopia **4.0.1**
- Fruits Delight **1.1.3+**
- Delightful **3.8.x**
- Hearth & Harvest
- Regions Unexplored
- Twilight Forest
- Deep Aether
- Eternal Starlight
- Ars Nouveau
- Argentum
- Easy Mob Farm
- Jade 11.x
- JEI 15.x
- EMI 1.1.x

Install Easy Farmer's Delight on both client and server for multiplayer gameplay. Client-only viewer/HUD mods remain
optional.

## Building from source

Java **17** is required.

Windows:

```text
build.bat
```

The helper delegates to `build.ps1`, resolves a JDK 17/Gradle environment, performs a clean build, writes
`build.log` and verifies that a runtime JAR exists. A normal local Gradle build is also supported.

The runtime JAR is written to `build/libs/` and uses **easy-farmers-delight** in its filename.

## License

Copyright © 2026 Celerbi. All rights reserved.
