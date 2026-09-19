#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
JAVA = ROOT / "src/main/java/dev/celerbi/easyfarmersdelightcompat"
DATA = RES / "data/easyfarmersdelightcompat"

FRUITS = [
    "pear", "hawberry", "lychee", "mango", "persimmon", "peach", "orange",
    "apple", "mangosteen", "bayberry", "kiwi", "fig",
]

def fail(message: str) -> None:
    print(f"FAIL: {message}")
    raise SystemExit(1)

def check(condition: bool, message: str) -> None:
    if not condition:
        fail(message)

def read_json(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        fail(f"invalid JSON {path.relative_to(ROOT)}: {exc}")

def check_json_tree() -> int:
    count = 0
    for path in sorted(RES.rglob("*.json")):
        read_json(path)
        count += 1
    return count

def check_fruits_defs() -> None:
    orchard_dir = DATA / "efdc_orchard_crops"
    for fruit in FRUITS:
        path = orchard_dir / f"fruits_delight_{fruit}.json"
        check(path.is_file(), f"missing Fruits Delight orchard definition: {path.name}")
        data = read_json(path)
        check(data.get("planting", {}).get("item") == f"fruitsdelight:{fruit}_leaves",
              f"wrong {fruit} planting item")
        check(data.get("render_block") == f"fruitsdelight:{fruit}_leaves",
              f"wrong {fruit} render block")
        values = [stage.get("properties", {}).get("type") for stage in data.get("stages", [])]
        check(values == ["leaves", "flowers", "fruits"],
              f"unexpected {fruit} orchard stages: {values}")
        expected_output = "minecraft:apple" if fruit == "apple" else f"fruitsdelight:{fruit}"
        check(data.get("harvest", {}).get("item") == expected_output,
              f"wrong {fruit} harvest item")

    check(not (orchard_dir / "fruits_delight_durian.json").exists(),
          "Durian must remain absent from productive orchard resources")

    regrowing = DATA / "efdc_regrowing_crops"
    expected_regrowing = {
        "fruits_delight_blueberry.json": (
            "fruitsdelight:blueberry", "fruitsdelight:blueberry_bush", 4, 2,
            "fruitsdelight:blueberry", 1, 2),
        "fruits_delight_cranberry.json": (
            "fruitsdelight:cranberry", "fruitsdelight:cranberry_bush", 4, 2,
            "fruitsdelight:cranberry", 1, 2),
        "fruits_delight_pineapple.json": (
            "fruitsdelight:pineapple_sapling", "fruitsdelight:pineapple", 4, 0,
            "fruitsdelight:pineapple", 1, 1),
    }
    for name, expected in expected_regrowing.items():
        path = regrowing / name
        check(path.is_file(), f"missing Fruits Delight regrowing definition: {name}")
        data = read_json(path)
        planting, block, harvest_age, post_age, item, min_count, max_count = expected
        check(data.get("planting", {}).get("item") == planting, f"wrong planting item in {name}")
        check(data.get("crop_block") == block, f"wrong crop block in {name}")
        check(data.get("age", {}).get("harvest") == harvest_age, f"wrong harvest age in {name}")
        check(data.get("age", {}).get("post_harvest") == post_age, f"wrong post-harvest age in {name}")
        harvest = data.get("harvest", {})
        check(harvest.get("item") == item, f"wrong harvest item in {name}")
        check(harvest.get("min_count") == min_count and harvest.get("max_count") == max_count,
              f"wrong harvest count in {name}")

    lemon_path = DATA / "efdc_tall_crops/fruits_delight_lemon.json"
    check(lemon_path.is_file(), "missing Fruits Delight Lemon tall-crop definition")
    lemon = read_json(lemon_path)
    check(lemon.get("planting", {}).get("item") == "fruitsdelight:lemon_seeds",
          "wrong Lemon planting item")
    check(lemon.get("crop_block") == "fruitsdelight:lemon_tree", "wrong Lemon crop block")
    check(lemon.get("age") == {"property": "age", "min": 0, "max": 4, "harvest": 4, "post_harvest": 2},
          "unexpected Lemon age cycle")
    check(lemon.get("halves", {}).get("upper_from_age") == 2,
          "Lemon upper half must begin at age 2")

    hamimelon_path = DATA / "efdc_stem_crops/fruits_delight_hamimelon.json"
    check(hamimelon_path.is_file(), "missing Fruits Delight Hamimelon stem definition")
    hamimelon = read_json(hamimelon_path)
    check(hamimelon.get("planting", {}).get("item") == "fruitsdelight:hamimelon_seeds",
          "wrong Hamimelon planting item")
    check(hamimelon.get("stem_block") == "fruitsdelight:hamimelon_stem",
          "wrong Hamimelon stem block")
    check(hamimelon.get("attached_stem_block") == "fruitsdelight:attached_hamimelon_stem",
          "wrong Hamimelon attached stem")
    check(hamimelon.get("fruit_blocks") == ["fruitsdelight:hamimelon"],
          "wrong Hamimelon fruit block")

def check_delightful() -> None:
    path = DATA / "efdc_regrowing_crops/delightful_cantaloupe.json"
    check(path.is_file(), "missing Delightful Cantaloupe definition")
    data = read_json(path)
    check(data == {
        "planting": {"item": "delightful:cantaloupe_seeds"},
        "crop_block": "delightful:cantaloupe_plant",
        "age": {"property": "age", "min": 0, "max": 3, "harvest": 3, "post_harvest": 0},
        "harvest": {
            "strategy": "random_item",
            "item": "delightful:cantaloupe",
            "min_count": 1,
            "max_count": 1,
            "full_age_bonus": 0,
        },
        "rich_soil": True,
    }, "Delightful Cantaloupe definition changed unexpectedly")

    salmon_path = DATA / "efdc_regrowing_crops/delightful_salmonberry.json"
    check(salmon_path.is_file(), "missing Delightful Salmonberry Pips definition")
    salmon = read_json(salmon_path)
    check(salmon == {
        "planting": {"item": "delightful:salmonberry_pips"},
        "crop_block": "delightful:salmonberry_bush",
        "age": {"property": "age", "min": 0, "max": 4, "harvest": 4, "post_harvest": 1},
        "harvest": {"strategy": "random_item", "item": "delightful:salmonberries",
                    "min_count": 2, "max_count": 3, "full_age_bonus": 0},
        "rich_soil": True,
    }, "Delightful Salmonberry Pips definition changed unexpectedly")

def check_source_markers() -> None:
    props = (ROOT / "gradle.properties").read_text(encoding="utf-8")
    check("mod_version=1.4.4" in props, "unexpected Forge snapshot version")

    mods = (RES / "META-INF/mods.toml").read_text(encoding="utf-8")
    check('modId = "delightful"' in mods and 'versionRange = "[3.8,)"' in mods,
          "optional Delightful dependency metadata missing")
    check('modId = "fruitsdelight"' in mods and 'versionRange = "[1.1.3,)"' in mods,
          "optional Fruits Delight dependency metadata missing")

    orchard = (JAVA / "integration/orchard/OrchardCropDefinition.java").read_text(encoding="utf-8")
    check('json.has("stages")' in orchard and 'json.has("age")' in orchard,
          "Orchard property-stage/legacy parser missing")
    check("final String validatedAgeProperty" in orchard,
          "Orchard effectively-final age property hotfix missing")

    orchard_defs = (JAVA / "integration/orchard/OrchardCropDefinitions.java").read_text(encoding="utf-8")
    check('"fruitsdelight", "durian_leaves"' in orchard_defs,
          "runtime Durian hard exclusion missing")
    check("isExplicitlyExcludedPlanting" in orchard_defs,
          "Durian planting guard missing")

    tall_dir = JAVA / "integration/tall"
    for name in ("TallCropDefinition.java", "TallCropDefinitions.java", "TallCropReloadListener.java"):
        check((tall_dir / name).is_file(), f"missing tall crop source: {name}")

    main = (JAVA / "EasyFarmersDelightCompat.java").read_text(encoding="utf-8")
    check("TallCropReloadListener.INSTANCE" in main, "Tall crop reload listener not registered")
    check("GraftingSupportEvents::onCanopyBreak" in main, "canopy break fallback not registered")

    farmer = (JAVA / "blockentity/CompatFarmerBlockEntity.java").read_text(encoding="utf-8")
    for marker in (
        "KEY_TALL_DEFINITION", "selectTallCrop", "ageTallCrop", "harvestTallCrop",
        "tallCropUpperState", "TallCropDefinitions.findCrop",
    ):
        check(marker in farmer, f"Farmer tall-crop integration missing {marker}")
    check("tag.putInt(KEY_SCHEMA, 14)" in farmer, "Farmer NBT schema must be 14")
    check("isExplicitlyExcludedResource(orchardPlantingItemId)" in farmer,
          "legacy productive Durian Rich Farmer migration missing")

    graft_be = (JAVA / "blockentity/GraftingSupportBlockEntity.java").read_text(encoding="utf-8")
    check("isExplicitlyExcludedPlanting(canopy)" in graft_be and "applyDefinition(null)" in graft_be,
          "legacy productive Durian standalone migration missing")

    canopy = (JAVA / "block/GraftingCanopyBlock.java").read_text(encoding="utf-8")
    events = (JAVA / "event/GraftingSupportEvents.java").read_text(encoding="utf-8")
    check("removeCanopyForPlayer" in canopy, "centralized canopy removal helper missing")
    check("BlockEvent.BreakEvent" in events and "onCanopyBreak" in events,
          "server-side canopy break fallback missing")

    farmer_renderer = (JAVA / "client/CompatFarmerBlockEntityRenderer.java").read_text(encoding="utf-8")
    graft_renderer = (JAVA / "client/GraftingSupportBlockEntityRenderer.java").read_text(encoding="utf-8")
    for name, text in (("Rich Farmer", farmer_renderer), ("Grafting Support", graft_renderer)):
        check("model.getRenderTypes" in text and "renderBlockStateLayer" in text,
              f"{name} mixed/multipart render-layer support missing")
    check("renderTallCrop" in farmer_renderer and "tallCropUpperState" in farmer_renderer,
          "two-block Lemon renderer missing")

    block = (JAVA / "block/CompatFarmerBlock.java").read_text(encoding="utf-8")
    check("canSelectTallCrop" in block and "selectTallCrop" in block,
          "Rich Farmer tall-crop interaction path missing")

    command = (JAVA / "command/FarmCommand.java").read_text(encoding="utf-8")
    check("case TALL" in command and "TallCropDefinitions.findPlanting" in command,
          "/farm tall-crop support missing")

    viewer = (JAVA / "integration/RecipeViewerData.java").read_text(encoding="utf-8")
    expected_markers = (
        "fruits_delight_orchard",
        "fruits_delight_pineapple",
        "fruits_delight_lemon",
        "delightful_cantaloupe",
    )
    for marker in expected_markers:
        check(marker in viewer, f"JEI/EMI fallback missing {marker}")

    graft_item = read_json(RES / "assets/easyfarmersdelightcompat/models/item/grafting_support.json")
    check(graft_item.get("display", {}).get("gui", {}).get("scale") == [0.5, 0.5, 0.5],
          "Grafting Support GUI scale containment fix missing")

    cutter_renderer = (JAVA / "client/CutterBlockEntityRenderer.java").read_text(encoding="utf-8")
    for marker in ("resolveInteriorLight", "LevelRenderer.getLightColor", "LightTexture.pack",
                   "cutter.isItemPreview() || level == null", "interiorLight"):
        check(marker in cutter_renderer, f"Cutter interior-lighting regression: missing {marker}")

    cutter = (JAVA / "integration/CuttingRecipeResolver.java").read_text(encoding="utf-8")
    tools = (JAVA / "integration/FarmerToolSupport.java").read_text(encoding="utf-8")
    check('new ResourceLocation("farmersdelight", "cutting")' in cutter and "getRecipes()" in cutter,
          "Cutter must scan the runtime Farmer's Delight cutting recipe type")
    check("ingredient.test(tool)" in cutter,
          "Forge Cutter must delegate custom tool-action matching to the recipe Ingredient")
    for marker in ("isKnife", "isPickaxe", "isAxe", "isShovel", "isHoe", "isShears"):
        check(marker in tools, f"Cutter tool family support missing: {marker}")

    # Guard against accidentally copying NeoForge-only source into the Forge branch.
    for path in JAVA.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        check("net.neoforged" not in text, f"NeoForge import leaked into {path.relative_to(ROOT)}")
        resource_tokens = (
            "ResourceLocation.fromNamespaceAndPath",
            "ResourceLocation.withDefaultNamespace",
            "ItemStack.parseOptional",
        )
        for token in resource_tokens:
            check(token not in text, f"1.21-only API token {token} leaked into {path.relative_to(ROOT)}")

def check_delightful_jar(path: Path) -> None:
    with zipfile.ZipFile(path) as zf:
        names = set(zf.namelist())
        required = {
            "net/brnbrd/delightful/common/block/CantaloupePlantBlock.class",
            "net/brnbrd/delightful/common/block/SalmonberryBushBlock.class",
            "data/delightful/loot_tables/blocks/cantaloupe_plant.json",
            "data/delightful/loot_tables/blocks/salmonberry_bush.json",
            "data/delightful/recipes/cutting/green_tea_leaves.json",
            "data/forge/tags/items/tea_leaves/green.json",
            "assets/delightful/blockstates/cantaloupe_plant.json",
            "assets/delightful/blockstates/salmonberry_bush.json",
            "assets/delightful/models/item/salmonberry_pips.json",
        }
        missing = sorted(required - names)
        check(not missing, "Delightful audit JAR missing: " + ", ".join(missing))
        raw = zf.read("data/delightful/loot_tables/blocks/cantaloupe_plant.json").decode("utf-8")
        check("delightful:cantaloupe" in raw and "delightful:cantaloupe_seeds" in raw and '"age": "3"' in raw,
              "Delightful audit JAR no longer matches Cantaloupe lifecycle")
        salmon_loot = zf.read("data/delightful/loot_tables/blocks/salmonberry_bush.json").decode("utf-8")
        check("delightful:salmonberry_pips" in salmon_loot and "delightful:salmonberries" in salmon_loot
              and '"age": "4"' in salmon_loot,
              "Delightful audit JAR no longer matches Salmonberry lifecycle")
        matcha = zf.read("data/delightful/recipes/cutting/green_tea_leaves.json").decode("utf-8")
        green_tag = zf.read("data/forge/tags/items/tea_leaves/green.json").decode("utf-8")
        check('"type": "farmersdelight:cutting"' in matcha
              and '"type": "farmersdelight:tool_action"' in matcha
              and '"action": "shovel_dig"' in matcha
              and '"tag": "forge:tea_leaves/green"' in matcha,
              "Delightful Matcha cutting recipe no longer uses generic FD cutting/tool-action semantics")
        check('"croptopia:tea_leaves"' in green_tag,
              "Delightful green tea tag no longer includes Croptopia tea leaves")

def check_fruits_jar(path: Path) -> None:
    with zipfile.ZipFile(path) as zf:
        names = set(zf.namelist())
        for fruit in FRUITS:
            check(f"assets/fruitsdelight/blockstates/{fruit}_leaves.json" in names,
                  f"Fruits Delight JAR missing {fruit}_leaves")
        for asset in (
            "assets/fruitsdelight/models/item/pineapple_sapling.json",
            "assets/fruitsdelight/models/item/lemon_seeds.json",
            "assets/fruitsdelight/models/item/hamimelon_seeds.json",
            "assets/fruitsdelight/blockstates/blueberry_bush.json",
            "assets/fruitsdelight/blockstates/cranberry_bush.json",
            "assets/fruitsdelight/blockstates/pineapple.json",
            "assets/fruitsdelight/blockstates/lemon_tree.json",
            "assets/fruitsdelight/blockstates/hamimelon_stem.json",
            "assets/fruitsdelight/blockstates/attached_hamimelon_stem.json",
            "assets/fruitsdelight/blockstates/hamimelon.json",
        ):
            check(asset in names, f"Fruits Delight JAR missing {asset}")

        for fruit in FRUITS:
            raw = zf.read(f"assets/fruitsdelight/blockstates/{fruit}_leaves.json").decode("utf-8")
            states = set(re.findall(r"type=([a-z_]+)", raw))
            check({"leaves", "flowers", "fruits"} <= states,
                  f"Fruits Delight {fruit} states changed: {sorted(states)}")

        for crop in ("blueberry_bush", "cranberry_bush", "pineapple", "lemon_tree"):
            raw = zf.read(f"assets/fruitsdelight/blockstates/{crop}.json").decode("utf-8")
            ages = {int(v) for v in re.findall(r"age=(\d+)", raw)}
            check(set(range(5)) <= ages, f"Fruits Delight {crop} age states changed: {sorted(ages)}")
        lemon = zf.read("assets/fruitsdelight/blockstates/lemon_tree.json").decode("utf-8")
        halves = set(re.findall(r"half=([a-z]+)", lemon))
        check({"lower", "upper"} <= halves, f"Fruits Delight Lemon halves changed: {sorted(halves)}")

def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--fruits-jar", type=Path,
                        help="Optional Fruits Delight 1.1.3 audit JAR")
    parser.add_argument("--delightful-jar", type=Path,
                        help="Optional Delightful 3.8.1 audit JAR")
    args = parser.parse_args()

    json_count = check_json_tree()
    check_fruits_defs()
    check_delightful()
    check_source_markers()
    if args.fruits_jar:
        check_fruits_jar(args.fruits_jar)
    if args.delightful_jar:
        check_delightful_jar(args.delightful_jar)

    print(f"PASS: Forge 1.4.4 compatibility wave ({json_count} JSON files parsed)")
    if not args.fruits_jar:
        print(
            "NOTE: Fruits Delight external JAR verification skipped; "
            "pass --fruits-jar for byte-level resource checks."
        )
    if not args.delightful_jar:
        print(
            "NOTE: Delightful external JAR verification skipped; "
            "pass --delightful-jar for byte-level resource checks."
        )

if __name__ == "__main__":
    main()
