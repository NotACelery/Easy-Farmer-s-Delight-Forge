# Easy Farmer's Delight 1.4.4 — Forge 1.20.1

1.4.4 is a compatibility and reliability release focused on Rich Farmer crop lifecycles and the Grafting Support.

## Compatibility

- Added/backported expanded **Fruits Delight 1.1.3** compatibility for Forge 1.20.1.
- Added optional **Delightful 3.8.x** compatibility.
- Expanded/updated optional compatibility with **Hearth & Harvest**, **Regions Unexplored**, **Croptopia**,
  **Twilight Forest**, **Deep Aether** and **Eternal Starlight**.
- Improved handling for mods whose crops use tall, stem, regrowing, orchard or persistent multi-section lifecycles
  instead of a normal harvest-and-replant crop.
- Improved Apple Orchard ownership and migration when a supported dedicated apple-tree provider is installed.

## Fixes

- Fixed the Grafting Support's historical ghost/transient leaf placement and corrected canopy hitboxes/interactions.
- Fixed canopy mining speed, particles, placement synchronization and root visuals.
- Fixed **Hearth & Harvest** persistent structural crops being harvested/replanted before the full plant could finish
  growing; mature sections are now handled independently and old machine state is migrated safely.
- Fixed Cutter interior-light sampling next to opaque blocks.
- Villager Noise Switch now also covers automated Easy Farmer's Delight Farmer/Cutter action sounds.

## Release cleanup

- Finalized source formatting, logging, documentation and Forge-specific compatibility adaptations for 1.4.4.
