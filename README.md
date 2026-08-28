# Easy Farmer's Delight Compat — Forge 1.20.1

Current release: **1.3.1** (Forge 1.20.1)

**Easy Farmer's Delight Compat** is an independent, unofficial compatibility addon that brings **Farmer's Delight** farming and cutting mechanics into **Easy Villagers** automation.

> **Backport notice:** this repository is the **Forge / Minecraft 1.20.1 backport** of Easy Farmer's Delight Compat.  
> The main edition targets **NeoForge / Minecraft 1.21.1** and is maintained separately at:
> https://github.com/NotACelery/Easy-Farmer-s-Delight-Compat

The Forge edition keeps the same gameplay goals and feature set wherever the 1.20.1 versions of the dependencies allow it, while using the native Forge 1.20.1 APIs and data formats.

For implementation architecture, persistence invariants, compatibility boundaries and regression checks, see [`DEVELOPMENT.md`](DEVELOPMENT.md).

## Target versions

- Minecraft **1.20.1**
- Forge **47.2.0+**
- Development baseline: Forge **47.4.23**
- Java / JDK **17**
- Easy Villagers **1.1.39+**
- Farmer's Delight **1.3.3+**

Optional integrations:

- Jade 11.x
- JEI 15.x
- EMI 1.1.x for Minecraft 1.20.1

## Features

### Farmer variants

- **Paddy Farmer**
  - Farmer's Delight Rice support, including the full rice crop cycle.
  - Sugar Cane support.
- **Rich Farmer**
  - Rich Soil accelerated farming.
  - Tomato and rope handling.
  - Mushroom Colony support.
  - Compatible crop automation.
- **Rich Paddy Farmer**
  - Paddy Farmer behavior combined with Rich Soil acceleration.
- Farmer machine data is preserved when upgrading variants.
- Empty Farmer items can stack normally, while Farmers carrying villager or inventory state are kept non-stackable.

### Harvest tools

Compatible Farmer variants can use the appropriate harvest tools for special crops.

- Farmer's Delight Knives are supported through the Forge knife tag.
- Hoes and Fortune-enchanted tools retain their intended harvesting behavior.
- Tool durability, Fortune effects and break behavior are preserved.

### Cutter

The **Cutter** automates Farmer's Delight Cutting Board recipes while following the 1.20.1 recipe behavior.

- Dedicated villager-powered machine.
- Input, tool and output handling.
- Knife/tool requirements are respected.
- Fortune-aware Cutting Board outputs.
- Cutting recipe sounds.
- Axe transformations such as stripping, scraping and wax removal where applicable.
- Machine contents and villager state are preserved correctly.

### Villager Noise Switch

A client-local **Villager Noise Switch** is included for controlling villager sounds without altering the stored villager or machine behavior. Its recipe and internal pedestal now use an Emerald Block to distinguish it from the Iron Farm variant. Like its Iron Farm sibling, the Villager Noise Switch is always non-stackable.

### Iron Farm Noise Switch

The **Iron Farm Noise Switch** uses the former Iron Block recipe and must be assembled after placement with four additional Iron Blocks, followed by a Carved Pumpkin. The four blocks appear inside in the vanilla Iron Golem construction order before the pumpkin permanently completes the miniature Golem.

Once assembled, right-clicking toggles a persistent client-local mute that cancels only Zombie Ambient and Iron Golem Hurt/Death sounds emitted from the exact position of Easy Villagers Iron Farm blocks. Normal Zombies and Iron Golems remain audible.

The Iron Farm Noise Switch is always non-stackable and preserves its assembly/Golem state when mined. Its Lever and Redstone presentation is client-personal and emits no real Redstone.

### Recipe viewer and HUD integrations

Optional support is included for:

- **Jade**
- **JEI**
- **EMI**

These integrations expose machine contents, harvest tools, outputs and custom recipe information without making the optional mods hard dependencies.

## Forge 1.20.1 backport notes

This edition is a source-level backport, not a loader shim. The original NeoForge 1.21.1 implementation was adapted to Minecraft 1.20.1 conventions, including:

- NeoForge registries, events and menu hooks → Forge equivalents.
- Data Components → classic `BlockEntityTag` / NBT persistence.
- NeoForge item capabilities → Forge `ForgeCapabilities.ITEM_HANDLER` and `LazyOptional`.
- Minecraft 1.21 crafting APIs → 1.20.1 crafting containers and serializers.
- Farmer's Delight 1.20.1 Cutting Board recipe API and tool matching.
- `forge:tools/knives` for knife compatibility.
- Farmer's Delight 1.20.1 `tomatoes_on_rope` behavior.
- 1.20.1 datapack directory names and resource formats.
- Java 21-only code paths replaced with Java 17-compatible equivalents.

The Forge port has been compiled successfully with ForgeGradle and validated in-game, including Farmer recipes/upgrades, harvest tools and Fortune behavior, Villager Noise Switch functionality, Cutter processing and Cutting Board outputs.

## Building from source

A **JDK 17** installation is required.

This repository intentionally does not include local Gradle caches or generated build output. With a suitable Gradle installation, run:

```bash
gradle clean build
```

The finished mod JAR is written to:

```text
build/libs/
```

If you add a Gradle Wrapper to your checkout, the equivalent commands are:

```bash
./gradlew clean build
```

or on Windows:

```bat
gradlew.bat clean build
```

## Dependencies

The mod depends on:

- **Easy Villagers**
- **Farmer's Delight**
- **Forge**

Jade, JEI and EMI integrations are optional.

Dependency mods are not redistributed as part of this repository or inside the built JAR.

## Project status

The Forge 1.20.1 branch is maintained as a dedicated backport. Changes from the NeoForge 1.21.1 edition may require loader- and version-specific adaptation instead of being copied directly.

## Disclaimer and attribution

This is an **independent, unofficial compatibility project**. It is not affiliated with, endorsed by, or maintained by the authors of Easy Villagers or Farmer's Delight.

- **Easy Villagers** — henkelmax
- **Farmer's Delight** — vectorwing

Their names, APIs and game content are referenced solely for interoperability.
