# Easy Farmer's Delight 1.4.3-dev.7

## Nocturnal Millet Forgotten lifecycle fix

- Fixes Eternal Starlight Nocturnal Millet in Rich Farmers so the crop no longer harvests ordinary Nocturnal Millet as soon as the panicle matures.
- The virtual stalk first grows normally to its real maximum age (`age=7`).
- Once the stalk reaches full maturity, the Rich Farmer flips the real `forgotten=true` BlockState property before beginning the panicle stage.
- The Forgotten panicle then grows through its own `age=0..2` lifecycle.
- Harvest only becomes available when the stalk is mature, `forgotten=true`, and the panicle reaches `age=2`.
- Harvest output is `eternal_starlight:forgotten_nocturnal_millet`, after which the panicle returns to `age=1` and remains Forgotten for subsequent harvests.
- Renderer propagation now copies the stalk's `forgotten` state to the virtual panicle so Eternal Starlight's Forgotten visuals are used instead of only changing the output item.
- Crop/Jade display automatically switches from Nocturnal Millet to Forgotten Nocturnal Millet once the real Forgotten state is reached.

## Why the conversion is virtualized

Eternal Starlight normally enters the Forgotten state when Nocturnal Millet is planted on a block in `eternal_starlight:converts_nocturnal_millet` (Dusted Gravel family) and receives bone meal. A Rich Farmer does not expose a physical soil block or a bone-meal interaction, so EFDC performs the equivalent conversion only after the stalk itself has fully matured. This preserves the important ordering: mature stalk -> Forgotten state -> panicle growth -> Forgotten harvest.

## Validation status

Source snapshot created. A Gradle build was attempted, but the environment could not resolve `services.gradle.org`, so compilation never started. Full Gradle compilation and in-game QA are still pending.
