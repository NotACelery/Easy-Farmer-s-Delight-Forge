# Integrity audit — Easy Farmer's Delight 1.4.3-dev.4 (Forge 1.20.1)

## Scope

This pass intentionally adds no new gameplay features. It verifies that the 1.4.3 work is integrated cleanly across Cutter tool parity, Harvest Tool/Fortune routing, Tomato + Rope growth, Grafting Support, Jade and sided automation.

## Findings and corrections

- Removed the redundant `Rich Soil: Active` Jade line from Rich Farmer Orchard status. Rich Soil remains explicit only where it is contextual information, such as a standalone Grafting Support checking the block below it.
- Confirmed Tomato ropes keep separate installed/planted state: installing Rope does not create a tomato plant, Rich Soil does not boost an empty Rope, and extension proceeds base -> Rope 1 -> Rope 2.
- Confirmed old saves without the planted-state keys migrate existing Rope sections as planted, while new 1.4.3 saves preserve empty Rope sections correctly.
- Confirmed Cutter tool handling remains generalized for Knife, Pickaxe, Axe, Shovel, Hoe and Shears while Harvest Tool storage remains limited to farming-relevant Knife/Hoe/Axe/Shears.
- Confirmed normal farmland crop loot can receive an optional Hoe for Fortune, while Rice, Mushroom Colonies, Orchards and Sugar Cane keep their specialized paths.
- Confirmed sided Cutter automation still protects the tool slot from normal extraction and keeps top/side/bottom routing intact.
- Removed obsolete intermediate 1.4.2 development QA documents; the final 1.4.2 QA remains as release history.
- Updated current support/development documentation for the 1.4.3 line.
- Scanned Java sources for compressed multi-statement lines, excessively long lines and obviously unused explicit imports; none remain.

## QA focus

1. Build the loader target from a clean checkout.
2. Re-test Tomato with two pre-installed Ropes and Rich Soil acceleration.
3. Verify Rich Farmer Orchard Jade no longer shows `Rich Soil: Active`.
4. Verify standalone Grafting Support still reports Rich Soil state.
5. Exercise all six Cutter Cutting Board tool families.
6. Re-test hopper insertion/extraction and protected tool slots.
