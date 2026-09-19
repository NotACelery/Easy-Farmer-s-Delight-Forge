# Easy Farmer's Delight 1.4.4-forge-dev.11

Hotfix snapshot on top of 1.4.4-forge-dev.10.

## What changed
- Restored the original Grafting Support roots look using `easyfarmersdelightcompat:block/grafting_support_roots`.
- Restored `grafting_support_roots.png` so each root cuboid samples the intended hanging-roots-style pixel palette instead of reading rooted dirt as full block texels.
- Preserved all current root geometry, hitboxes, Orchard/Rich Farmer logic, Apple Oak support, and Forge build fixes from dev.10.

## Goal
Return the visual appearance of the roots to the pre-regression style seen before the rooted-dirt texture swap, while keeping the corrected shape and interaction behavior.
