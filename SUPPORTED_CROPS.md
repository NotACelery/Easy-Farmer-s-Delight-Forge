# Supported Crops and Farming Rules — Easy Farmer's Delight 1.4.0

This document is the user-facing source of truth for crop support in **Easy Farmer's Delight 1.4.0**.
The public project name changed in 1.4.0, but the technical registry namespace remains
`easyfarmersdelightcompat` so existing worlds and saved machine items keep their identity.

## Farmer capability overview

| Mechanic | Paddy Farmer | Rich Farmer | Rich Paddy Farmer |
| --- | --- | --- | --- |
| Easy Villagers normal crop lifecycle | No | Yes | No |
| Rich Soil growth acceleration | No | Yes, where allowed | Rice only |
| Farmer's Delight Rice | Yes | No | Yes, accelerated |
| Sugar Cane on installed Sand | Yes | No | Yes, normal speed |
| Tomato + Rope | No | Yes | No |
| Mushroom Colonies | No | Yes | No |
| Melon / Pumpkin stems | No | Yes | No |
| Regrowing bushes | No | Yes | No |
| Attached log crops | No | Yes | No |
| Harvest Tool slot | No | Yes | Yes |

The base Easy Villagers Farmer remains owned by Easy Villagers. Easy Farmer's Delight does not add the new
special-crop mechanics to that upstream block.

## Normal crops inherited through Easy Villagers

The **Rich Farmer** keeps the normal Easy Villagers Farmer crop path. Anything Easy Villagers recognizes as a valid
seed/crop can use that path unless a special Easy Farmer's Delight mechanic handles it first.

Easy Farmer's Delight also adds optional entries to Minecraft's `villager_plantable_seeds` tag so these crops can be
recognized without making their source mods mandatory:

- **Ars Nouveau:** `ars_nouveau:magebloom_crop`
- **Argentum:** `argentum:yerba_semilla`
- **Argentum:** `argentum:te_semilla`
- **Argentum:** `argentum:batata`
- **Argentum:** `argentum:membrillo_semilla`

Missing optional items are ignored by the datapack loader. No Ars Nouveau or Argentum Java classes are required for
this seed-tag bridge.

For normal crops, Rich Soil follows Farmer's Delight's exclusion rule: a crop tagged
`farmersdelight:unaffected_by_rich_soil` is not accelerated.

## Farmer's Delight Tomatoes and Rope

**Farmer:** Rich Farmer only.

- Insert **Tomato Seeds** to start the Tomato lifecycle.
- The base Tomato plant remains planted after harvest.
- Up to **two Rope sections** can be installed.
- Each Tomato/Rope section keeps independent growth progress.
- Rich Soil can accelerate supported Tomato growth.
- A Hoe is optional and may contribute Fortune where the real crop loot supports it.
- Removing Rope uses the normal top-down interaction: the highest installed Rope is returned first.
- A blocked harvest waits without rerolling or losing its pending output.

## Mushroom Colonies

**Farmer:** Rich Farmer only.

- Red Mushroom selects the Red Mushroom Colony.
- Brown Mushroom selects the Brown Mushroom Colony.
- The colony grows without a tool.
- A mature colony waits for a **Knife** before harvesting.
- The Knife is a requirement but is not damaged by the colony harvest.
- After harvest, the colony returns to its growing state instead of being removed permanently.

## Melons and Pumpkins

**Farmer:** Rich Farmer only.

- Insert Melon Seeds or Pumpkin Seeds.
- Rich Soil accelerates the **stem-growth phase**.
- Fruit appearance remains on the normal Farmer work cadence.
- A mature Melon/Pumpkin waits for an **Axe**.
- Fortune/Silk Touch behavior comes from the real vanilla block loot behavior where applicable.
- Axe durability is consumed only after a successful harvest.
- Full output never consumes the fruit or damages the Axe.

## Regrowing bushes

Regrowing crops use data files under:

```text
data/<namespace>/efdc_regrowing_crops/*.json
```

They are deliberately opt-in. Easy Farmer's Delight does **not** assume every `BushBlock` is safe to automate.

### Sweet Berry Bush

**Farmer:** Rich Farmer only.

- Plant with `minecraft:sweet_berries`.
- Grows to age 3.
- Mature harvest yields the vanilla-style **2–3 Sweet Berries**.
- The bush resets to age 1 after harvest instead of being destroyed/replanted.
- Rich Soil can accelerate bush growth but never increases the berry roll directly.

### Ars Nouveau Sourceberry

**Farmer:** Rich Farmer only; Ars Nouveau remains optional.

- Plant with `ars_nouveau:sourceberry_bush`.
- Grows to age 3.
- Mature harvest yields **2–3 Sourceberries** using the configured full-age semantics.
- The bush resets to age 1 after harvest.
- Rich Soil can accelerate growth but does not directly increase the harvest count.

## Attached log crops — Rich Farmer Log Mode

The Rich Farmer can hold **two independent host logs**, a lower host and an upper host. Each host exposes the four
horizontal faces — north, south, east and west — so one Farmer can maintain up to **8 attached crop faces**.

The two logs do not need to be the same type. This allows one Rich Farmer to grow two different attached-crop
families at the same time.

Attached-crop definitions are loaded from:

```text
data/<namespace>/efdc_attached_crops/*.json
```

A definition controls the planting item/tag, crop block, valid host block/tag, age/facing properties, mature and
post-harvest ages, loot strategy, Rich Soil eligibility and optional tool requirement.

### Cocoa

- Planting item: **Cocoa Beans**.
- Required host: a block in `minecraft:jungle_logs`.
- A non-Jungle log is rejected exactly like an invalid Cocoa host in normal Minecraft.
- One Jungle Log supports 4 Cocoa faces; two Jungle Logs support 8.
- Mature Cocoa uses the real block loot behavior.
- Rich Soil accelerates growth only; it does not multiply the loot roll.

### Ars Nouveau Archfruits

Easy Farmer's Delight ships explicit optional definitions for all four Ars Nouveau attached fruits:

| Planting item | Required host family |
| --- | --- |
| **Bombegranate Pod** | `ars_nouveau:blazing_logs` |
| **Mendosteen Pod** | `ars_nouveau:flourishing_logs` |
| **Frostaya Pod** | `ars_nouveau:cascading_logs` |
| **Bastion Pod** | `ars_nouveau:vexing_logs` |

The same host rule used for Cocoa applies here: a fruit pod cannot be planted on the wrong Archwood family. When an
attached planting item is recognized and an installed host still has a free face but the host is incompatible, the
interaction is rejected and the player receives the **"This seed cannot be planted on this log"** message. The item
is not consumed.

Ars Nouveau is not a hard dependency. If it is absent, the definitions referencing its missing registry entries are
skipped safely.

### Mixed hosts and independent output

- Lower and upper hosts are persisted separately.
- Every face stores its own definition, crop identity, planting item and age.
- A full/blocked output for one fruit type does not freeze another attached fruit that still fits in the shared
  output inventory.
- When output capacity increases, blocked attached faces are retried event-by-event rather than polled every tick.
- If a datapack definition disappears later, persisted render/planting identity is retained so the existing machine
  can still display and dismantle its stored crop safely.

### Dismantling order

Sneak-use dismantling is intentionally top-down and lossless:

1. Remove all planted crops from the **upper** host together.
2. Remove the upper host log.
3. Remove all planted crops from the **lower** host together.
4. Remove the lower host log.

Dismantling returns the planting item for each face. It does not grant an extra mature-harvest bonus.

## Paddy crops

### Rice

- Paddy Farmer and Rich Paddy Farmer accept Farmer's Delight Rice.
- Rice keeps its paddy-specific lower-plant/panicle lifecycle.
- Rich Paddy Farmer applies Rich Soil acceleration to Rice.
- On Rich Paddy, a Knife is optional for Knife-sensitive Rice drops and is not damaged by this harvest path.

### Sugar Cane

- Paddy Farmer and Rich Paddy Farmer support Sugar Cane after **Sand** is installed.
- Sugar Cane grows to three blocks tall; the top two sections are harvested and the base remains planted.
- **Rich Paddy does not accelerate Sugar Cane.** Sugar Cane is intentionally Sand-based compatibility behavior, so
  using the Rich variant for it provides no growth-speed advantage.
- Sneak-use dismantling returns the installed Sand and currently stored Sugar Cane state.

## Rich Soil summary

| Crop/mechanic | Rich Soil acceleration |
| --- | --- |
| Normal Easy Villagers-compatible crop | Yes, unless in `farmersdelight:unaffected_by_rich_soil` |
| Magebloom through the normal crop path | Yes, unless excluded by the same tag |
| Tomato / Rope | Yes |
| Sweet Berry Bush | Yes |
| Sourceberry | Yes |
| Cocoa | Yes |
| Ars Nouveau Archfruits | Yes |
| Melon/Pumpkin stem | Yes |
| Melon/Pumpkin fruit appearance | No |
| Rice in Rich Paddy | Yes |
| Sugar Cane in Rich Paddy | **No** |

Rich Soil changes growth opportunities, not the harvest quantity itself.

## Crop item tooltips

Mined Easy Farmer's Delight Farmer items preserve their machine state. Hovering a Paddy, Rich or Rich Paddy Farmer
shows the planted crop information without requiring the machine to be placed first.

For a mixed attached setup, distinct stored crops are shown as separate tooltip lines instead of repeating one line
for every occupied face.

## Deliberate exclusions

**Bamboo is not treated as a Farmer crop in 1.4.0.** Its vertical structural growth is outside the crop families
implemented by Easy Farmer's Delight.

This does not prevent Bamboo Block from participating in unrelated Cutter Axe/log behavior when Minecraft exposes
it through the relevant standard log/axe transformation systems.
