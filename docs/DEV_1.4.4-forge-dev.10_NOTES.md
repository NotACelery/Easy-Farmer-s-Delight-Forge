# Easy Farmer's Delight 1.4.4-forge-dev.10

Build-fix snapshot over `1.4.4-forge-dev.9`.

## Fixes

- `GraftingCanopyBlock#getDestroyProgress(...)` is now `public`, as required by Forge/Minecraft 1.20.1 `BlockBehaviour`.
- `TerrainParticle#updateSprite(...)` is invoked as a separate statement because Forge 1.20.1 types its return as `Particle`; the concrete `TerrainParticle` reference is retained for subsequent particle configuration.

## Scope preserved

This snapshot intentionally does **not** change the recovered dev.9 behavior: Grafting Support geometry/hitboxes, rooted-dirt visual roots, lateral leaf insertion handling, modded-apple priority, Regions Unexplored Apple Oak orchard behavior, Rich Farmer migration, and JEI/EMI filtering remain unchanged.
