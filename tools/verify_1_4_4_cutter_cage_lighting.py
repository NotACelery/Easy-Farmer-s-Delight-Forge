#!/usr/bin/env python3
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
CUTTER = ROOT / "src/main/java/dev/celerbi/easyfarmersdelightcompat/block/CutterBlock.java"
FARMER = ROOT / "src/main/java/dev/celerbi/easyfarmersdelightcompat/block/CompatFarmerBlock.java"

def fail(msg):
    print(f"FAIL: {msg}")
    sys.exit(1)

c = CUTTER.read_text(encoding="utf-8")
f = FARMER.read_text(encoding="utf-8")
shape_tokens = (
    "private static final VoxelShape CUTTER_SHAPE = Shapes.or(",
    "return CUTTER_SHAPE;",
    "CollisionContext context",
)
for token in shape_tokens:
    if token not in c:
        fail(f"Cutter cage shape token missing: {token}")

boxes = [
    "Block.box(0D, 0D, 0D, 16D, 1D, 16D)",
    "Block.box(0D, 15D, 0D, 16D, 16D, 16D)",
    "Block.box(0D, 0D, 0D, 1D, 16D, 16D)",
    "Block.box(15D, 0D, 0D, 16D, 16D, 16D)",
    "Block.box(0D, 0D, 0D, 16D, 16D, 1D)",
    "Block.box(0D, 0D, 15D, 16D, 16D, 16D)",
]
for box in boxes:
    if box not in c:
        fail(f"Cutter missing cage box: {box}")
    if box not in f:
        fail(f"Farmer baseline missing expected cage box: {box}")

renderer = ROOT / "src/main/java/dev/celerbi/easyfarmersdelightcompat/client/CutterBlockEntityRenderer.java"
r = renderer.read_text(encoding="utf-8")
if "resolveInteriorLight" not in r:
    fail("Renderer interior light sampling regressed")

print("PASS: Cutter uses the same hollow cage shape as Farmer and keeps renderer interior-light sampling")
