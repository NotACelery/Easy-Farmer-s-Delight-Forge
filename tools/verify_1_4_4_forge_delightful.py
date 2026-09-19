#!/usr/bin/env python3
from __future__ import annotations
import json, pathlib, sys, zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
DEF = ROOT / 'src/main/resources/data/easyfarmersdelightcompat/efdc_regrowing_crops/delightful_cantaloupe.json'
SALMON = ROOT / 'src/main/resources/data/easyfarmersdelightcompat/efdc_regrowing_crops/delightful_salmonberry.json'
VIEWER = ROOT / 'src/main/java/dev/celerbi/easyfarmersdelightcompat/integration/RecipeViewerData.java'
PROPS = ROOT / 'gradle.properties'
MODS = ROOT / 'src/main/resources/META-INF/mods.toml'

def fail(msg: str):
    raise SystemExit('FAIL: ' + msg)

if not DEF.is_file(): fail('missing Delightful Cantaloupe definition')
data = json.loads(DEF.read_text(encoding='utf-8'))
expected = {
    'planting': {'item': 'delightful:cantaloupe_seeds'},
    'crop_block': 'delightful:cantaloupe_plant',
    'age': {'property': 'age', 'min': 0, 'max': 3, 'harvest': 3, 'post_harvest': 0},
    'harvest': {
        'strategy': 'random_item',
        'item': 'delightful:cantaloupe',
        'min_count': 1,
        'max_count': 1,
        'full_age_bonus': 0,
    },
    'rich_soil': True,
}
if data != expected: fail('definition differs from audited Delightful 3.8.1 lifecycle')
if not SALMON.is_file(): fail('missing Delightful Salmonberry Pips definition')
salmon = json.loads(SALMON.read_text(encoding='utf-8'))
expected_salmon = {
    'planting': {'item': 'delightful:salmonberry_pips'},
    'crop_block': 'delightful:salmonberry_bush',
    'age': {'property': 'age', 'min': 0, 'max': 4, 'harvest': 4, 'post_harvest': 1},
    'harvest': {
        'strategy': 'random_item',
        'item': 'delightful:salmonberries',
        'min_count': 2,
        'max_count': 3,
        'full_age_bonus': 0,
    },
    'rich_soil': True,
}
if salmon != expected_salmon: fail('Salmonberry definition differs from audited Delightful lifecycle')
if 'delightful_cantaloupe' not in VIEWER.read_text(encoding='utf-8'): fail('viewer fallback missing Cantaloupe')
if 'mod_version=1.4.4' not in PROPS.read_text(encoding='utf-8'): fail('wrong snapshot version')
mods = MODS.read_text(encoding='utf-8')
if 'modId = "delightful"' not in mods or 'versionRange = "[3.8,)"' not in mods:
    fail('optional Delightful metadata missing')

# Validate every JSON resource in source tree.
count = 0
for path in (ROOT / 'src/main/resources').rglob('*.json'):
    try: json.loads(path.read_text(encoding='utf-8'))
    except Exception as exc: fail(f'invalid JSON {path.relative_to(ROOT)}: {exc}')
    count += 1

# If the supplied audit JAR is next to the project parent, verify the exact upstream resources too.
jar = ROOT.parent / 'Delightful-1.20.1-3.8.1.jar'
if jar.is_file():
    with zipfile.ZipFile(jar) as z:
        names = set(z.namelist())
        required = {
            'net/brnbrd/delightful/common/block/CantaloupePlantBlock.class',
            'data/delightful/loot_tables/blocks/cantaloupe_plant.json',
            'assets/delightful/blockstates/cantaloupe_plant.json',
            'net/brnbrd/delightful/common/block/SalmonberryBushBlock.class',
            'data/delightful/loot_tables/blocks/salmonberry_bush.json',
            'data/delightful/recipes/cutting/green_tea_leaves.json',
            'data/forge/tags/items/tea_leaves/green.json',
        }
        missing = sorted(required - names)
        if missing: fail('Delightful JAR missing audited entries: ' + ', '.join(missing))
        loot = json.loads(z.read('data/delightful/loot_tables/blocks/cantaloupe_plant.json'))
        raw = json.dumps(loot)
        if 'delightful:cantaloupe_seeds' not in raw or 'delightful:cantaloupe' not in raw or '"age": "3"' not in raw:
            fail('Delightful JAR loot table no longer matches audited Cantaloupe IDs/age')
        salmon_raw = z.read('data/delightful/loot_tables/blocks/salmonberry_bush.json').decode('utf-8')
        salmon_tokens = ('delightful:salmonberry_pips', 'delightful:salmonberries', '"age": "4"')
        if not all(token in salmon_raw for token in salmon_tokens):
            fail('Delightful JAR Salmonberry lifecycle no longer matches audited IDs/age')
        matcha = z.read('data/delightful/recipes/cutting/green_tea_leaves.json').decode('utf-8')
        green = z.read('data/forge/tags/items/tea_leaves/green.json').decode('utf-8')
        matcha_tokens = ('farmersdelight:cutting', 'farmersdelight:tool_action', 'shovel_dig')
        if not all(token in matcha for token in matcha_tokens):
            fail('Delightful Matcha cutting recipe no longer uses FD shovel tool action')
        if 'croptopia:tea_leaves' not in green:
            fail('Delightful green tea tag no longer includes Croptopia tea leaves')

print(f'PASS: Forge 1.4.4 Delightful Cantaloupe/Salmonberry + Cutter audit; {count} JSON resources parsed')
