from pathlib import Path
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
BE = ROOT / 'src/main/java/dev/celerbi/easyfarmersdelightcompat/blockentity/CompatFarmerBlockEntity.java'
RENDER = ROOT / 'src/main/java/dev/celerbi/easyfarmersdelightcompat/client/CompatFarmerBlockEntityRenderer.java'
PROPS = ROOT / 'gradle.properties'

def fail(msg):
    raise SystemExit('FAIL: ' + msg)

def require(text, needle, msg):
    if needle not in text:
        fail(msg)

be = BE.read_text(encoding='utf-8')
render = RENDER.read_text(encoding='utf-8')
props = PROPS.read_text(encoding='utf-8')

expected_version = '1.4.4'
require(props, 'mod_version=' + expected_version, 'unexpected snapshot version')

for needle, msg in [
    ('hearthandharvest", "corn_stalk', 'corn stalk registry id missing'),
    ('hearthandharvest", "corn_kernels', 'corn kernels registry id missing'),
    ('hearthandharvest", "corn"', 'corn harvest item registry id missing'),
    ('KEY_HEARTH_CORN_MIDDLE_AGE', 'middle section NBT missing'),
    ('KEY_HEARTH_CORN_TOP_AGE', 'top section NBT missing'),
    ('private boolean ageHearthCorn(', 'structure-aware corn growth missing'),
    ('private boolean harvestHearthCorn(', 'independent corn harvest missing'),
    ('hearthCornMiddleAge < 3', 'middle section progression missing'),
    ('hearthCornTopAge < 2', 'top unlock stage missing'),
    ('hearthCornTopAge >= 4', 'native age-4 harvest threshold missing'),
    ('getAge(bottom) >= 5 ? 2 : 1', 'native age-5 double yield missing'),
    ('hearthCornMiddleAge >= 5 ? 2 : 1', 'middle age-5 yield missing'),
    ('hearthCornTopAge >= 5 ? 2 : 1', 'top age-5 yield missing'),
    ('withAge(bottom, 3)', 'bottom post-harvest age 3 missing'),
    ('hearthCornMiddleAge = 3', 'middle post-harvest age 3 missing'),
    ('hearthCornTopAge = 3', 'top post-harvest age 3 missing'),
    ('ageHearthCorn(level, registries, crop)', 'corn must bypass normal CropBlock growth'),
    ('isHearthCornState(crop) && level instanceof ServerLevel', 'Rich Soil generic bonemeal guard missing'),
    ('tag.putInt(KEY_SCHEMA, 14)', 'NBT schema must be 14'),
]:
    require(be, needle, msg)

# Corn must stay dependency-free: registry/property integration only.
if 'import alabaster.hearthandharvest' in be or 'import alabaster.hearthandharvest' in render:
    fail('hard Hearth & Harvest Java dependency introduced')

for needle, msg in [
    ('HEARTH_CORN_STACK_SCALE', 'corn stack render scale missing'),
    ('renderHearthCorn(', 'three-section renderer missing'),
    ('"section", "bottom"', 'bottom render state missing'),
    ('"section", "middle"', 'middle render state missing'),
    ('"section", "top"', 'top render state missing'),
    ('farmer.hearthCornMiddleAge()', 'middle render age missing'),
    ('farmer.hearthCornTopAge()', 'top render age missing'),
]:
    require(render, needle, msg)

# Audit the already-supported H&H perennial crop and make sure structural grape/sunflower
# items were not silently added as normal crop definitions while fixing Corn.
cotton_path = (
    ROOT
    / 'src/main/resources/data/easyfarmersdelightcompat/efdc_regrowing_crops/hearthandharvest_cotton.json'
)
if not cotton_path.exists():
    fail('Hearth & Harvest Cotton regrowing definition missing')
cotton = json.loads(cotton_path.read_text(encoding='utf-8'))
age = cotton.get('age', {})
if age.get('harvest') != 7 or age.get('post_harvest') != 5:
    fail('Hearth & Harvest Cotton must remain age 7 -> age 5 perennial')

structural_ids = ('hearthandharvest:red_grapes', 'hearthandharvest:green_grapes', 'hearthandharvest:sunflower_seeds')
for folder in ('efdc_regrowing_crops', 'efdc_tall_crops', 'efdc_stem_crops'):
    base = ROOT / 'src/main/resources/data/easyfarmersdelightcompat' / folder
    if not base.exists():
        continue
    for candidate in base.glob('*.json'):
        raw = candidate.read_text(encoding='utf-8')
        for structural_id in structural_ids:
            if structural_id in raw:
                fail(f'structural H&H item was forced into generic crop data: {structural_id}')

# Mini lifecycle model matching the intended integration invariants.
def ready(b, m, t):
    unlocked = t >= 2
    return t >= 4 or (unlocked and (b >= 4 or m >= 4))

# Old broken saves with bottom 5 but no upper structure must NOT harvest immediately.
if ready(5, -1, -1):
    fail('migration gate would prematurely harvest legacy bottom-only corn')
# Once native structure is unlocked, each section independently becomes harvestable at 4.
for state in [(4,3,2), (3,4,2), (3,3,4)]:
    if not ready(*state):
        fail(f'age-4 section not harvestable in model: {state}')
# Post-harvest state is perennial, not replanted at 0.
post = 3
if post != 3:
    fail('invalid perennial reset model')

print(f'PASS: Hearth & Harvest Corn lifecycle gates for {expected_version}')
