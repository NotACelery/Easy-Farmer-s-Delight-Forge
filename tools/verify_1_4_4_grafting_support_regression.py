#!/usr/bin/env python3
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[1]

def text(rel):
    p = ROOT / rel
    if not p.is_file():
        raise SystemExit(f"FAIL missing: {rel}")
    return p.read_text(encoding="utf-8")

def need(hay, needle, label):
    if needle not in hay:
        raise SystemExit(f"FAIL {label}: missing {needle!r}")

support = text("src/main/java/dev/celerbi/easyfarmersdelightcompat/block/GraftingSupportBlock.java")
canopy = text("src/main/java/dev/celerbi/easyfarmersdelightcompat/block/GraftingCanopyBlock.java")
renderer = text("src/main/java/dev/celerbi/easyfarmersdelightcompat/client/GraftingSupportBlockEntityRenderer.java")
events = text("src/main/java/dev/celerbi/easyfarmersdelightcompat/event/GraftingSupportEvents.java")
main = text("src/main/java/dev/celerbi/easyfarmersdelightcompat/EasyFarmersDelightCompat.java")
sounds = text("src/main/java/dev/celerbi/easyfarmersdelightcompat/client/VillagerSoundEvents.java")
registry = text("src/main/java/dev/celerbi/easyfarmersdelightcompat/registry/ModBlocks.java")
orchard_defs = text(
    "src/main/java/dev/celerbi/easyfarmersdelightcompat/integration/orchard/OrchardCropDefinitions.java"
)
orchard_reload = text(
    "src/main/java/dev/celerbi/easyfarmersdelightcompat/integration/orchard/OrchardCropReloadListener.java"
)
support_be = text("src/main/java/dev/celerbi/easyfarmersdelightcompat/blockentity/GraftingSupportBlockEntity.java")
viewer = text("src/main/java/dev/celerbi/easyfarmersdelightcompat/integration/RecipeViewerData.java")
farmer_be = text("src/main/java/dev/celerbi/easyfarmersdelightcompat/blockentity/CompatFarmerBlockEntity.java")

# Ghost canopy / standby sync.
need(canopy, 'BooleanProperty.create("active")', "canopy active state")
need(canopy, 'state.getValue(ACTIVE)', "canopy shape gate")
need(support, 'setCanopyMarkerActive(level, supportPos, true)', "insertion activates marker")
need(support, 'support.canAcceptLeaves(heldItem)', "marker-before-BE insertion order")
need(canopy, 'setCanopyMarkerActive(level, canopyPos.below(), false)', "removal deactivates marker")
need(renderer, 'support.hasClientCanopyPreview()', "renderer client-preview gate")
need(events, 'GraftingCanopyBlock.removeCanopyForPlayer', "centralized canopy removal")

# Lateral leaf placement must be intercepted at the event layer before BlockItem#useOn.
need(events, 'onRightClickBlock(PlayerInteractEvent.RightClickBlock event)', "right-click interception")
need(events, 'GraftingSupportBlock.isLeavesItem(event.getItemStack())', "leaf item interception")
need(events, 'event.setCancellationResult(InteractionResult.SUCCESS)', "successful cancellation result")
need(events, 'event.setUseItem(Event.Result.DENY)', "Forge item-use denial")
need(events, 'event.setCanceled(true)', "right-click cancellation")
need(events, 'support.previewCanopy(event.getItemStack())', "client canopy preview")
need(support_be, 'public boolean previewCanopy(ItemStack stack)', "client preview method")
need(events, 'if (!support.hasCanopy())', "empty-support insertion gate")
need(main, 'EventPriority.HIGHEST', "early right-click listener")
need(support, 'sound.getPlaceSound()', "real leaf placement sound")

# Lower support hits may not leak wood particles while a canopy is loaded.
need(events, 'event.setCanceled(true)', "loaded lower support attack cancellation")

# Real leaf hardness/tool speed and real leaf particles.
need(canopy, 'rendered.getDestroyProgress(player, level, pos)', "real leaf destroy speed")
need(canopy, 'manager.destroy(pos, rendered)', "real leaf destroy particles")
need(canopy, 'IClientBlockExtensions.of(rendered)', "real leaf client extension")
need(canopy, '.updateSprite(rendered, hit.getBlockPos())', "real leaf hit particle sprite")
need(canopy, 'level.levelEvent(player, 2001, canopyPos, Block.getId(renderedCanopy))', "real leaf final break event")

# Shape parity: rootstock + 5 roots + 4 stakes collide, ropes are selection-only planes.
for coords in [
    'Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D)',
    'Block.box(5.0D, 0.0D, 8.0D, 6.0D, 1.0D, 9.0D)',
    'Block.box(10.0D, 0.0D, 7.0D, 11.0D, 2.0D, 9.0D)',
    'Block.box(11.0D, 0.0D, 7.0D, 12.0D, 1.0D, 10.0D)',
    'Block.box(7.0D, 0.0D, 3.0D, 9.0D, 1.0D, 6.0D)',
    'Block.box(6.0D, 0.0D, 10.0D, 7.0D, 1.0D, 13.0D)',
    'Block.box(2.0D, 0.0D, 2.0D, 4.0D, 10.0D, 4.0D)',
    'Block.box(12.0D, 0.0D, 12.0D, 14.0D, 10.0D, 14.0D)',
]:
    need(support, coords, "solid grafting geometry")
need(support, 'ROPE_EPSILON = 2.0E-6D', "effectively planar rope picker")
need(support, 'return SOLID_SHAPE;', "rope-free collision")
need(support, 'return OUTLINE_SHAPE;', "rope-aware selection")

# Roots: dev.5 visual assignment restored exactly; geometry/hitboxes remain from the corrected branch.
model_path = ROOT / "src/main/resources/assets/easyfarmersdelightcompat/models/block/grafting_support.json"
model = json.loads(model_path.read_text(encoding="utf-8"))
if model.get("textures", {}).get("roots") != "minecraft:block/hanging_roots":
    raise SystemExit("FAIL roots must use vanilla minecraft:block/hanging_roots directly")

OPAQUE_HANGING_ROOT_TEXELS = {
    (0, 0), (1, 1), (1, 3), (1, 4), (2, 0), (2, 1), (2, 3), (2, 6), (2, 7),
    (3, 1), (3, 2), (3, 3), (3, 5), (3, 6), (4, 0), (4, 3), (4, 4), (4, 5),
    (5, 2), (5, 3), (5, 6), (5, 7), (5, 8), (6, 0), (6, 1), (6, 2), (6, 6),
    (7, 0), (7, 1), (7, 2), (7, 3), (7, 4), (7, 5), (7, 6), (7, 8), (7, 9),
    (8, 0), (8, 3), (8, 4), (8, 5), (8, 7), (8, 8), (8, 10), (9, 2), (9, 3),
    (9, 5), (9, 6), (9, 7), (10, 0), (10, 1), (10, 4), (11, 1), (11, 2),
    (11, 3), (11, 4), (11, 5), (11, 6), (11, 8), (11, 9), (12, 0), (12, 3),
    (12, 6), (12, 7), (13, 0), (13, 1), (13, 2), (13, 7), (13, 8), (14, 2),
    (14, 3), (14, 4), (14, 5), (15, 0), (15, 1), (15, 6),
}
EXPECTED_ROOT_FACES = {"north", "east", "south", "west", "up", "down"}
for element_index in range(1, 6):
    faces = model["elements"][element_index]["faces"]
    if set(faces) != EXPECTED_ROOT_FACES:
        raise SystemExit(f"FAIL root element {element_index} must keep all six faces; got {sorted(faces)}")
    for face_name, face in faces.items():
        uv = face.get("uv")
        if not (isinstance(uv, list) and len(uv) == 4 and uv[2] - uv[0] == 1 and uv[3] - uv[1] == 1):
            raise SystemExit(f"FAIL root face {element_index}:{face_name} must sample exactly one texture pixel")
        if (uv[0], uv[1]) not in OPAQUE_HANGING_ROOT_TEXELS:
            raise SystemExit(
                f"FAIL root face {element_index}:{face_name} samples a transparent hanging-roots texel: {uv}"
            )
        if face.get("texture") != "#roots":
            raise SystemExit(f"FAIL root face {element_index}:{face_name} is not using #roots")

# Transparent proxy supplies destroy-stage geometry; actual particles are overridden above.
need(canopy, 'RenderShape.MODEL', "canopy destroy-stage proxy")
proxy_path = ROOT / "src/main/resources/assets/easyfarmersdelightcompat/models/block/grafting_canopy.json"
proxy = json.loads(proxy_path.read_text(encoding="utf-8"))
if not proxy.get("elements"):
    raise SystemExit("FAIL canopy proxy model has no destroy-stage geometry")
transparent = ROOT / "src/main/resources/assets/easyfarmersdelightcompat/textures/block/grafting_canopy_invisible.png"
if not transparent.is_file():
    raise SystemExit("FAIL transparent canopy proxy texture missing")

# Apple ownership: vanilla Oak/Dark Oak are fallback-only when a dedicated provider is loaded.
for mod_id in ['regions_unexplored', 'croptopia', 'fruitsdelight']:
    need(orchard_defs, f'isLoaded("{mod_id}")', f"dedicated apple provider {mod_id}")
need(orchard_defs, 'VANILLA_APPLE_DEFINITION_IDS', "vanilla apple definition suppression")
need(orchard_defs, 'VANILLA_APPLE_LEAF_IDS', "vanilla apple planting suppression")
need(orchard_defs, 'isRuntimeSuppressedPlanting(stack)', "runtime vanilla apple planting suppression")
need(orchard_defs, 'isRuntimeSuppressedResource(ResourceLocation id)', "runtime vanilla apple resource suppression")
need(orchard_reload, 'isRuntimeSuppressedDefinition(id)', "reload suppression")
need(support_be, 'isRuntimeSuppressedDefinition(orchardDefinitionId)', "persisted vanilla apple cleanup")
need(support_be, 'isRuntimeSuppressedPlanting(canopy)', "persisted standalone vanilla leaf cleanup")
need(farmer_be, 'isRuntimeSuppressedDefinition(orchardDefinitionId)', "persisted Rich Farmer vanilla apple cleanup")
need(farmer_be, 'isRuntimeSuppressedResource(orchardPlantingItemId)', "persisted Rich Farmer vanilla leaf cleanup")
need(viewer, '!OrchardCropDefinitions.hasDedicatedAppleProvider()', "viewer vanilla fallback gate")
need(viewer, 'stack("regions_unexplored", "apple_oak_leaves")', "Regions Unexplored viewer guide")

ru_path = ROOT / "src/main/resources/data/easyfarmersdelightcompat/efdc_orchard_crops/regions_unexplored_apple_oak.json"
ru = json.loads(ru_path.read_text(encoding="utf-8"))
if ru.get("planting", {}).get("item") != "regions_unexplored:apple_oak_leaves":
    raise SystemExit("FAIL Regions Unexplored Apple Oak planting item")
if ru.get("render_block") != "regions_unexplored:apple_oak_leaves":
    raise SystemExit("FAIL Regions Unexplored Apple Oak render block")
age = ru.get("age", {})
apple_age = (
    age.get("property"), age.get("min"), age.get("max"), age.get("mature"), age.get("post_harvest")
)
if apple_age != ("age", 0, 4, 4, 0):
    raise SystemExit("FAIL Regions Unexplored Apple Oak age lifecycle")
harvest = ru.get("harvest", {})
if (harvest.get("item"), harvest.get("min"), harvest.get("max")) != ("minecraft:apple", 1, 1):
    raise SystemExit("FAIL Regions Unexplored Apple Oak harvest")

# VNS automatic machine-action mute remains present.
need(sounds, 'ClientPreferences.villagersMuted()', "VNS preference gate")
need(sounds, 'block instanceof CompatFarmerBlock || block instanceof CutterBlock', "EFD action sound suppression")

if '.noTerrainParticles()' in registry:
    raise SystemExit('FAIL grafting canopy still disables terrain particles')

print(
    "PASS: grafting placement preview, vanilla-behavior, direct vanilla hanging-roots opaque-pixel UVs with all "
    "root faces restored, apple ownership/RU orchard, particles/speed and VNS gates"
)
