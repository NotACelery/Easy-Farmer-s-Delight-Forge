# Easy Farmer's Delight 1.4.4-forge-dev.12

Visual-only Grafting Support roots correction on top of forge-dev.11.

- Uses `minecraft:block/hanging_roots` directly.
- No custom Grafting Support roots texture remains.
- Every visible root face uses a 1x1 UV rectangle pointing at an opaque pixel from the vanilla Hanging Roots texture.
- Faces hidden against the trunk and the internal east-root joint are omitted to prevent z-fighting.
- Root geometry/hitboxes, Forge build fixes and all orchard behavior are preserved.
