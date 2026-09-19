# Easy Farmer's Delight 1.4.4-dev.7 — Forge Grafting interaction + VNS action-sound fixes

Date: 2026-09-19
Target: Minecraft 1.20.1 / Forge 47.4.x

## Grafting Support
- Restored selection/collision for the central rootstock, five roots, four side stakes and both rope bands.
- Hanging Roots surfaces now sample the whole vanilla `minecraft:block/hanging_roots` sprite instead of transparent-prone tiny UV slices.
- Client-side leaf insertion is consumed before `BlockItem` adjacent-placement prediction, eliminating the one-tick side-placement flash.
- Canopy placement uses the real installed leaf block's place sound.
- Upper-marker and lower-support canopy removal share one synchronized path.
- The canopy marker uses a fully transparent proxy model with actual quads so vanilla can draw destroy cracks and terrain particles while the visible leaf model remains block-entity rendered.

## Villager Noise Switch
- Muting villager sounds now also suppresses positional block-action sounds emitted by EFD Farmer/Cutter blocks.
- This includes automatic harvest/replant, berry/orchard actions, dynamic crop break sounds and Cutter work noise.
- Standalone player-operated Grafting Support sounds remain audible.

## Compatibility
Cantaloupe, Salmonberry, Fruits Delight backport and generic Cutting Board recipe support are unchanged from the previous Forge snapshots.

## QA status
Static regression gates PASS. Requires JDK 17 / Forge Windows build + in-game QA before build-clean status.
