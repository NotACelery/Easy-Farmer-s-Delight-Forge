# QA — Easy Farmer's Delight 1.4.2-dev.1 (Forge 1.20.1)

## Build and startup

- Build with Java 17 and the project Gradle configuration; candidate artifact must identify itself as `1.4.2-dev.1`.
- Start with Easy Villagers + Farmer's Delight only and confirm Croptopia is not a hard dependency.
- Start with Croptopia Forge 1.20.1-4.0.1.
- Confirm 2 vanilla Orchard definitions load without Croptopia and all 28 bundled definitions load with Croptopia.

## Grafting Support — standalone

- Craft using `RRR / SLS / SHS` (Rope / Stick+Log+Stick / Stick+Hanging Roots+Stick).
- Any valid `#minecraft:logs` ingredient must create the same stackable Grafting Support item/model.
- Placement requires the upper block to be replaceable and reserves the two-block structure.
- With no canopy, the upper marker has no selectable/collidable leaf shape and Jade shows waiting for leaves.
- Insert Oak/Dark Oak, Croptopia fruit leaves and at least one unsupported decorative leaf.
- The visible canopy must sit above the Rope/stake frame, connect through the slim stripped-log graft branch, and expose a dynamic hitbox limited to the visible leaves/top surface.
- The player must be able to stand on the canopy top without clipping into the leaves.
- Unsupported leaves remain decorative; productive leaves grow only with Farmer's Delight Rich Soil directly below.
- Removing Rich Soil stalls growth without resetting existing age.
- Right-click mature fruit with Shears while targeting either the leaves or lower support: fruit drops into the world, age resets, and normal Shears durability/Unbreaking applies.
- Vanilla Oak/Dark Oak harvest: exactly 2 Apples plus a 30% chance for a third.
- Break the canopy with Shears: recover the exact inserted leaves and keep the support.
- Break the canopy with a Silk Touch tool: recover the exact inserted leaves and keep the support.
- Break the canopy by hand/ordinary tool: leaves are destroyed and the support remains.
- Breaking the lower support through another destruction path must preserve the support and any installed canopy according to normal block-drop handling.

## Rich Farmer Orchard

- Install a Grafting Support only in an empty Rich Farmer; reject Paddy/Rich Paddy and conflicting modes.
- With support but no leaves, Jade reports Grafting Support / waiting for compatible leaves.
- Install Oak and Dark Oak separately; confirm visual stages `0→1→2→3` read as bud → flower → young fruit → mature fruit.
- Mature fruit without Shears remains mature and Jade reports Ready / Waiting for Shears.
- Insert Shears; confirm harvest resets correctly and output is inserted into the Farmer inventory.
- Full output must not reset fruit, reroll pending output or damage Shears.
- Repeated harvests with Unbreaking I/II/III must use normal durability prevention.
- Grafting Support, graft branch and canopy must remain proportionate, aligned and clear of the villager/frame.
- Dismantling returns the grafted leaves before the Grafting Support.

## Croptopia 4.0.1

- Confirm all **58 normal ground crops** can be planted, grown and harvested through the Rich Farmer normal crop path.
- Sweep all **26 `croptopia:*_crop` fruit leaves** through the Grafting Support/Rich Farmer Orchard path.
- Each Croptopia Orchard must use its native AGE 0/1/2/3 render states and harvest its matching fruit.
- Croptopia Apple must output `minecraft:apple`.
- Banana, Cherry, Coconut, Lemon, Apple and Walnut are minimum representative visual checks.
- Jade names the Orchard from the fruit (for example Banana Orchard / Cherry Orchard).
- Remove Croptopia in a disposable test instance and confirm Easy Farmer's Delight itself still starts.

## Cutter — Croptopia Cinnamon

- Cinnamon Log + Axe → Stripped Cinnamon Log + 1 Cinnamon.
- Cinnamon Wood + Axe → Stripped Cinnamon Wood + 1 Cinnamon.
- One operation consumes one source block and one Axe durability attempt.
- If both outputs cannot fit, input and Axe durability remain unchanged.
- Confirm generic vanilla/modded log stripping from 1.4.1 remains functional.

## Integrations and regressions

- Jade: empty support, decorative canopy, growing Orchard, mature Orchard with/without Shears, Rich Soil state.
- JEI/EMI: Grafting Support recipe, Apple Orchard guide, Croptopia Orchard guide when installed, Cinnamon guide when installed, Shears Harvest Tool guidance.
- Verify normal crops, Tomato/Rope, Mushroom Colonies, Melon/Pumpkin, regrowing crops and attached-log crops.
- Verify Rich Paddy Rice/Sugar Cane are unchanged.
- Verify Harvest Tool transfer accepts Knife/Hoe/Axe/Shears correctly.
