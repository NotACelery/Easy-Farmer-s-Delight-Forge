# Supported Crops and Farming Rules — Easy Farmer's Delight 1.4.3

This document is the user-facing source of truth for crop support in **Easy Farmer's Delight 1.4.3**.
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
| Grafting Support / Orchard | No | Yes | No |
| Harvest Tool slot | No | Yes — Knife/Hoe/Axe/Shears | Yes — Knife/Hoe/Axe/Shears |

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

### Croptopia ground crops

Croptopia 4.0.1's **58 normal ground crops** use its shared farmland `CropBlock` lifecycle and are compatible with the
Rich Farmer through this normal Easy Villagers crop path. They do not need bespoke Rice/rope/trellis mechanics in
Easy Farmer's Delight. The supported ground-crop set is:

Artichoke, Asparagus, Barley, Basil, Bell Pepper, Black Bean, Blackberry, Blueberry, Broccoli, Cabbage, Cantaloupe,
Cauliflower, Celery, Chile Pepper, Coffee, Corn, Cranberry, Cucumber, Currant, Eggplant, Elderberry, Garlic, Ginger,
Grape, Green Bean, Green Onion, Honeydew, Hops, Kale, Kiwi, Leek, Lettuce, Mustard, Oat, Olive, Onion, Peanut, Pepper,
Pineapple, Radish, Raspberry, Rhubarb, Rice, Rutabaga, Saguaro, Soybean, Spinach, Squash, Strawberry, Sweet Potato,
Tea, Tomatillo, Tomato, Turmeric, Turnip, Vanilla, Yam and Zucchini.

Croptopia Rice, Tomato, Grape, Hops and similar crops remain **normal farmland crops** here because Croptopia itself
does not give them Farmer's Delight-style waterlogging, Rope or trellis requirements.

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


## Grafting Support and Orchards

**Available as:** a standalone placed block, or an automated Rich Farmer mode.

The Grafting Support represents a rooted stock/trunk held by four stakes and Farmer's Delight Rope. It is crafted
from 3 Rope, 4 Sticks, any `#minecraft:logs` item and 1 Hanging Roots. Any accepted log creates the same fixed
Grafting Support block/item; the ingredient does not create variants or NBT and the item stacks to 64.

### Standalone support

1. Place the **Grafting Support** with one free/replaceable block directly above it. Placement reserves that upper block for the canopy.
2. Right-click the lower support with any block in `minecraft:leaves` to install a canopy. The canopy reserves the upper block and exposes a dynamic selection/collision shape matching the visible leaves, including a walkable top surface.
3. Unsupported leaves are purely decorative and never gain fruit.
4. Compatible productive leaves only advance through their four fruit ages (`0..3`) while **Farmer's Delight Rich Soil** is directly below the support.
5. Mature fruit is harvested manually by right-clicking either the canopy or lower support with **Shears**. A successful harvest drops the fruit into the world beside the support, resets the fruit to its post-harvest age and applies normal Shears durability, including **Unbreaking**.
6. The canopy itself is breakable. Breaking it with **Shears** or a **Silk Touch** tool returns the exact inserted leaves; breaking it with the hand or another tool destroys the leaves. The lower Grafting Support remains placed. Attacking the lower support while a canopy exists follows the same recovery rule before the support itself can be mined.

Removing Rich Soil does not erase existing progress; growth simply stalls until Rich Soil is restored. Breaking a
support through another destruction path preserves both the support and its installed canopy as drops.

### Rich Farmer automation

1. Right-click an empty Rich Farmer with the **Grafting Support**.
2. Right-click with compatible productive leaves.
3. The installed leaves become that Farmer's Orchard and progress through the same four fruit ages (`0..3`).
4. At maturity the Orchard waits until **Shears** are present in the Harvest Tool slot and output capacity exists.
5. A successful harvest resets the fruit age and applies normal Shears durability; **Unbreaking applies normally**.
6. Sneak-use returns the leaves first; sneak-use again returns the Grafting Support.

A full output does not reset the fruit, reroll the harvest or damage the Shears. Rich Soil accelerates configured
Orchard growth but does not alter the harvest roll.

### Vanilla Apple Orchard

- `minecraft:oak_leaves` → Apples
- `minecraft:dark_oak_leaves` → Apples

The normal vanilla leaf blocks are not modified globally. Their Orchard age exists only while grafted into a
productive Grafting Support / Rich Farmer setup. The visual lifecycle mirrors Croptopia's four-stage fruit language:
bud, flower, young fruit and mature fruit. A vanilla Apple Orchard yields **2 Apples**, with a **30% chance** for a third Apple. Without Rich Soil, a newly placed standalone Oak/Dark Oak canopy remains visually ordinary and does not begin
the fruit lifecycle.

### Optional Croptopia Orchards

Croptopia is optional and is never imported as a hard Java dependency. When its registry entries exist, the Rich
Farmer accepts these 26 productive crop-leaf blocks:

Almond, Apple, Apricot, Avocado, Banana, Cashew, Cherry, Coconut, Date, Dragonfruit, Fig, Grapefruit, Kumquat, Lemon,
Lime, Mango, Nectarine, Nutmeg, Orange, Peach, Pear, Pecan, Persimmon, Plum, Starfruit and Walnut.

Each integration uses Croptopia's real `*_crop` block with its native age property `0..3`. Harvest output matches the
ripe fruit item from Croptopia's own 4.0.1 crop-leaf behavior; Croptopia Apple correctly produces the vanilla
`minecraft:apple`.

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

**Bamboo is not treated as a Farmer crop in 1.4.3.** Its vertical structural growth is outside the crop families
implemented by Easy Farmer's Delight.

This does not prevent Bamboo Block from participating in unrelated Cutter Axe/log behavior when Minecraft exposes
it through the relevant standard log/axe transformation systems.
