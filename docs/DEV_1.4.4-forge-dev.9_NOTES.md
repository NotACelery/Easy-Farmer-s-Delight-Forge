# Easy Farmer's Delight 1.4.4-forge-dev.9

Recovery snapshot reconstructed from the final 2026-09-19 Grafting Support QA requirements on top of 1.4.4-forge-dev.8.

## Recovered corrections

- Preserve the corrected Grafting Support geometry and hitboxes from forge-dev.8.
- Restore only the roots visual assignment used by dev.5: `block/rooted_dirt`.
- Prevent the remaining lateral one-tick leaf placement/reconciliation flash by claiming leaf interactions at the event layer even after a canopy is present, plus an immediate client canopy preview and marker-before-BE authoritative sync.
- Treat vanilla Oak/Dark Oak Apple Orchards as fallback-only whenever Regions Unexplored, Croptopia or Fruits Delight is loaded.
- Keep vanilla Oak/Dark Oak leaves usable as decorative standalone Grafting canopies under that fallback suppression.
- Add productive Regions Unexplored Apple Oak Leaves with native `age=0..4`, maturity 4, one Apple, reset 0.
- Clean old persisted vanilla Apple Orchard state in both standalone Grafting Supports and Rich Farmers when a dedicated Apple provider is loaded.
- Keep JEI/EMI block guidance consistent with the same Apple ownership policy.

## Validation

- `verify_1_4_4_forge_compatibility.py`: PASS (121 JSON resources)
- `verify_1_4_4_forge_delightful.py`: PASS (121 JSON resources)
- `verify_1_4_4_cutter_cage_lighting.py`: PASS
- `verify_1_4_4_grafting_support_regression.py`: PASS
- JSON parse: PASS (121)
- Source-only hygiene: PASS
- Full Gradle/in-game validation: still required before marking build-clean.
