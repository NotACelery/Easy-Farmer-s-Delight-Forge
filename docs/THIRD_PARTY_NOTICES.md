# Third-Party Notices

Easy Farmer's Delight is an independent expansion built to interoperate with separately installed mods. Third-party
mods remain separate works owned and distributed by their respective authors.

No third-party mod binaries are bundled with this project. Third-party textures, models, GUI images and other
artistic assets are not redistributed by this project. Runtime registry IDs, tags, public APIs and compatibility
behavior are used to interoperate with separately installed mods.

## Interoperability summary

| Project | Relationship | License / distribution status | Use in Easy Farmer's Delight |
| --- | --- | --- | --- |
| Farmer's Delight | Required | MIT | Crops, Rich Soil behavior and Cutting Board recipe interoperability. Its assets are referenced from the installed mod rather than copied. |
| Easy Villagers | Required | All Rights Reserved on its public distribution pages | Stored Villager/Farmer runtime interoperability and upgrade source items. Easy Villagers models, textures, GUI images and binaries are not redistributed or used as resource parents. |
| Ars Nouveau | Optional | GNU LGPL v3 | Explicit Magebloom, Sourceberry and four Archfruit definitions; Archwood host tags; Archwood Cutter variants through standard log tags. Ars models/textures/animations stay in Ars Nouveau. |
| Easy Mob Farm | Optional | MIT for repository code; project artwork/assets have separate restrictions | Optional Noise Switch registration and client-side display-entity muting. No Easy Mob Farm model/texture is copied. |
| Argentum | Optional | All Rights Reserved on its public distribution page | Optional seed IDs are added to the normal villager plantable-seed tag. No Argentum assets/binaries are redistributed. |
| Pale Garden - Update | Optional | Academic Free License v3.0 | Logs can be discovered generically through Minecraft log tags for Cutter work surfaces. No project-specific assets/code are copied. |
| Jade | Optional | CC BY-NC-SA 4.0 repository/API | Optional diagnostic plugin compiled against the public API; Jade is not embedded. |
| Just Enough Items (JEI) | Optional | MIT | Optional recipe/viewer integration compiled against the public API; JEI is not embedded. |
| EMI | Optional | MIT | Optional recipe/viewer integration compiled against the public API; EMI is not embedded. |

## Resource independence

All resources physically distributed under `assets/easyfarmersdelightcompat` are maintained by this project or
reference vanilla/dependency resources at runtime without copying them.

Easy Villagers visual resources are deliberately excluded from Easy Farmer's Delight resource definitions. Farmer
Villagers use Minecraft's vanilla Villager renderer. Crop/log displays ask Minecraft to render the real registered
block/model supplied by the owning mod. This preserves resource packs and source animations while keeping the source
asset in its original project.

The Cutter stores only the selected log/stem registry ID. A modded Cutter therefore renders the installed block
normally instead of shipping a duplicate texture/model.

## No endorsement

Third-party names are used only to identify interoperability targets. Easy Farmer's Delight is not affiliated with,
sponsored by or officially endorsed by those projects unless their authors explicitly state otherwise.

## Scope

This notice records interoperability/resource boundaries only. Each dependency remains governed by its own license
and distribution terms.
