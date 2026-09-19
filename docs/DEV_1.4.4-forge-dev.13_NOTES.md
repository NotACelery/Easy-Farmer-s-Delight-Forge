# Easy Farmer's Delight 1.4.4-dev.13

Root-face restoration hotfix on top of the previous hanging-roots UV snapshot.

## Change
- Keep all 6 faces on each of the 5 Grafting Support root cuboids (30 root faces total).
- Each face uses a 1x1 UV sample from a fully opaque texel of vanilla `minecraft:block/hanging_roots`.
- No face culling is performed for trunk-facing or coplanar root faces. Hidden z-fighting is accepted by design because those planes are not normally visible.
- Root geometry and every gameplay behavior remain unchanged.
