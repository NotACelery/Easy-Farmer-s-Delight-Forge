# Easy Farmer's Delight 1.4.4-dev.4 — Forge 1.20.1

Cutter interior-lighting follow-up after in-game QA.

## Fix

- The screenshots confirmed the Cutter could render its inner workstation much darker when an opaque block sat against the workstation cell.
- The root cause was isolated to `CutterBlockEntityRenderer`: unlike Rich Farmer and all three Noise Switch renderers, it still rendered every dynamic component with the raw block-entity `packedLight`.
- Cutter now resolves interior lighting from the same adjacent/upper-edge samples used by the already-correct renderers, then passes that value to the villager, work log, Cutting Board and displayed item.
- Item/inventory previews keep their original supplied light.

## Cross-branch audit

NeoForge 1.21.1 contained the same omission, so the same fix is mirrored in NeoForge 1.4.4-dev.7.

Build-clean status: pending Windows compile/in-game QA.
