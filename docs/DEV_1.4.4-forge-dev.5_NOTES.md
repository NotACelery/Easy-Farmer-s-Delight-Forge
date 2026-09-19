# Easy Farmer's Delight 1.4.4-dev.5 — Forge 1.20.1

## Cutter cage lighting root-cause fix

The previous renderer-only light sampling fix did not address the dark interior cage face itself, because that face belongs to the normal block model rather than the Cutter BlockEntity renderer.

The actual parity bug was in `CutterBlock`: Farmers and all Noise Switch blocks expose the hollow six-shell cage `VoxelShape`, while Cutter was still inheriting `Block`'s default full-cube shape. When a solid block touched a Cutter face, Minecraft's world lighting treated that block cell differently from the other EFD cages and the opposite interior face could become nearly black.

This snapshot adds `CUTTER_SHAPE`, matching the cage geometry used by Farmers / Noise Switches, and returns it from `getShape(...)`. The renderer-side `resolveInteriorLight(...)` from the previous snapshot is retained for the villager, work log, Cutting Board and displayed item.

No crop, Cutter recipe, inventory, villager, Grafting Support or compatibility behavior was changed.
