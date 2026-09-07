# Easy Farmer's Delight 1.4.3-dev.6

## Changes in this snapshot

- Fixes modded `StemBlock` handling so mature stems are never harvested/replanted as ordinary crops.
- Deep Aether Squash now follows the Melon/Pumpkin lifecycle: stem grows to maturity, waits for a Blue/Green Squash, renders an attached stem + fruit, and only the fruit is harvested.
- The chosen Squash fruit is persisted so rendering and harvested output agree.
- Adds the Deep Aether `attached_squash_stem` to the data-driven stem definition.
- Adds hardcoded virtual support for Eternal Starlight Nocturnal Millet: stalk 0..7, panicle 0..2, harvest panicle, regrow panicle to age 1.
- Jade crop labels now prefer a real item display name and fall back to a humanized registry path instead of exposing untranslated `block.namespace:path` keys.
- Adds Twilight Forest food berry bushes to the existing data-driven regrowing/bush category: Raspberry, Blueberry, Blackberry, Maloberry, Blightberry, Duskberry, Skyberry and Stingberry. Their native bush lifecycle is age 0..3 and right-click harvest returns the bush to age 2.
- Twilight Forest berries themselves are not treated as planting items because upstream registers them as ordinary food Items; the matching `*_bush` block item is the native planting/placement object.

## Validation status

Source snapshot created. Full Gradle compilation still requires validation in the target Forge/NeoForge environments.

## Deep Aether squash hard fallback

- `deep_aether:squash_stem` is explicitly treated as a vanilla-style stem lifecycle, never as a normal harvestable crop.
- A mature stem stays at max age until a fruit is produced.
- Fruit is chosen once (Blue or Green Squash), persisted, rendered beside an attached stem, and that same fruit is harvested.
- `deep_aether:attached_squash_stem`, `blue_squash` and `green_squash` have direct fallback IDs in code in addition to the data-driven definition.

## Twilight Forest bush category

Food berry bushes are registered through the regrowing/bush path: Raspberry, Blueberry, Blackberry, Maloberry, Blightberry, Duskberry, Skyberry and Stingberry. They use age `0..3`, harvest at `3`, and return to `2`, matching Twilight Forest's `TFBushBlock` harvest lifecycle. The virtual farmer currently models one bush position; Twilight Forest's optional upward second bush segment is intentionally not simulated yet.
