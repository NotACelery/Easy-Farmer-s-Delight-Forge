# QA — Easy Farmer's Delight 1.4.3 (Forge 1.20.1)

**Release status:** the Forge tree is kept behaviorally aligned with the NeoForge 1.4.3 source. The final promotion from `1.4.3-dev.7` to `1.4.3` changes only versioning and release documentation.

## Parity checklist

- Generic modded `CropBlock` fallback.
- Deep Aether Squash stem/fruit handling where the target mod version exposes the same integration IDs.
- Eternal Starlight Nocturnal Millet structural handling where available on the target loader/version.
- Hearth & Harvest Cotton and regrowing berry behavior.
- Regions Unexplored Salmonberry behavior where available.
- Twilight Forest berry-bush definitions where available.
- Human-readable Jade crop naming and exact planting-item persistence.
- Cutter Knife/Pickaxe/Axe/Shovel/Hoe/Shears tool-family parity.
- Existing 1.4.x Farmer, Paddy, Orchard, Cutter and integration regressions.

## Validation note

The packaging environment cannot currently resolve the Gradle distribution, so a fresh Forge compile/runtime pass was not performed during this source promotion. Loader-specific runtime validation should be performed before publishing the Forge binary if it has not already been tested locally.
