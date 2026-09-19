# Easy Farmer's Delight 1.4.4-dev.6 — Forge Grafting Support historical fixes

Target: Minecraft 1.20.1 / Forge 47.4.x.

## Fixed

- Removed the historical ghost-canopy state after manually breaking the upper leaves. The invisible canopy marker now has an explicit synchronized `active` blockstate instead of relying only on BlockEntity NBT.
- Hand, Shears, Silk Touch and Creative canopy removal immediately disables the canopy marker, leaf renderer and stripped-oak graft branch.
- Existing worlds are migration-safe: old canopy marker states default active, while new empty supports explicitly create inactive markers.
- Rebuilt the Grafting Support selection/collision shape around the real rootstock: full central trunk plus visible roots. The four outer stakes/rope frame are decorative and no longer steal the ray trace.
- Root struts now use vanilla `minecraft:block/hanging_roots`.
- Renderer gating now requires an active upper marker as well as stored canopy NBT, preventing stale client state from reappearing as ghost leaves/hitboxes.

## Compatibility scope

Cantaloupe, Salmonberry, Fruits Delight backport, Orchard/Grafting compatibility and Cutter recipe behavior are unchanged.

## Build status

Static gates pass. Full Forge build still needs the normal Windows/JDK 17 environment.
