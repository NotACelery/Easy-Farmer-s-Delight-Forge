# Easy Farmer's Delight 1.4.4-forge-dev.8

## Grafting Support historical behavior fixes

This snapshot follows up the 1.4.4 compatibility wave with behavior/visual fixes for the standalone Grafting Support.

- Root cuboids keep their existing geometry but now use an opaque custom texture derived from the vanilla Hanging Roots colour family; the vanilla transparent hanging-roots cross sprite is no longer mapped directly onto cuboids.
- Support-stake collision remains physical. Rope bands are excluded from collision and use only a near-zero numerical selection thickness required by Minecraft's VoxelShape ray picking.
- Lateral insertion of leaf blocks is intercepted at `RightClickBlock` with highest priority on both logical sides, explicitly denying normal item placement before the leaf `BlockItem` can predict a temporary adjacent block.
- Canopy mining speed delegates to the actual installed leaf BlockState, restoring normal shears efficiency.
- Hit and destroy particles use the actual installed leaf BlockState/model/tint instead of the invisible canopy marker. Custom leaf client particle hooks are honored before the vanilla terrain-particle fallback.
- Final break feedback uses vanilla destroy event 2001 with the actual installed leaf state ID.
- Loaded supports still protect the lower trunk/rootstock from attack while the canopy marker is the intended break target, preventing stray wood particles when removing foliage.
- The Villager Noise Switch automatic EFD action-sound suppression from the previous snapshot is preserved.

## QA status

Static regression checks only in this environment. Requires a real Windows build and in-game verification before being considered build-clean.
