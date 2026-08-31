# Third-Party Notices

Easy Farmer's Delight Compat is an independent interoperability addon. Third-party mods remain separate works owned and distributed by their respective authors.

No third-party mod binaries are bundled with this project. Third-party textures, models, GUI images and other artistic assets are not redistributed by this project. Runtime references to registry identifiers, tags and public/compatibility-facing behavior are used only to interoperate with separately installed mods.

## Interoperability Summary

| Project | Relationship | License / distribution status | Use in this project |
| --- | --- | --- | --- |
| Farmer's Delight | Required | MIT | Registry/content interoperability and Farmer's Delight gameplay recipes. No Farmer's Delight code or artistic assets are redistributed. |
| Easy Villagers | Required | All Rights Reserved on its public distribution pages | Registry identifiers and runtime interoperability with its machines and stored Villager data. Easy Villagers models, textures, GUI images and binaries are not redistributed. |
| Easy Mob Farm | Optional | MIT for repository code; images, models and other assets are explicitly excluded | Runtime-only optional integration for the Easy Mob Farm Noise Switch. No Easy Mob Farm assets or binaries are redistributed. |
| Ars Nouveau | Optional | GNU LGPL v3 | Magebloom is recognized through its registry identifier/tag behavior. Compatible log variants are discovered through Minecraft log tags; Ars Nouveau assets remain in Ars Nouveau. |
| Argentum | Optional | All Rights Reserved on its public distribution page | Crop seed/item registry identifiers are recognized so compatible Farmers can plant them. No Argentum assets or binaries are redistributed. |
| Pale Garden - Update | Optional | Academic Free License v3.0 | Compatible logs are discovered through Minecraft log tags. No project-specific code or assets are copied. |
| Jade | Optional | CC BY-NC-SA 4.0 repository/API | Optional diagnostic adapter compiled against Jade's public plugin API. Jade is not embedded in the distributed JAR. |
| Just Enough Items (JEI) | Optional | MIT | Optional recipe-viewer adapter compiled against the public API. JEI is not embedded. |
| EMI | Optional | MIT | Optional recipe-viewer adapter compiled against the public API. EMI is not embedded. |

## Resource Independence

All resources physically distributed under `assets/easyfarmersdelightcompat` are maintained by this project. Model/resource identifiers may point at separately installed dependencies when runtime interoperability requires the dependency to supply its own resource. For example, Rich Farmer models may request Farmer's Delight Rich Soil by resource identifier; the Rich Soil texture itself is not copied into this project. Easy Villagers visual assets are not used as model parents, textures or GUI backgrounds.

The Cutter stores only the registry identifier of the log selected by the player. Rendering a modded Cutter variant asks Minecraft to render that installed block normally; the source mod keeps ownership and distribution of its own model and textures.

## No Endorsement

Names of third-party projects are used only to identify compatibility targets. Easy Farmer's Delight Compat is not affiliated with, sponsored by or officially endorsed by those projects unless explicitly stated otherwise by their authors.

## Scope of This Notice

This file records how Easy Farmer's Delight Compat interacts with third-party projects. Each third-party project remains governed by its own license and distribution terms; this notice does not replace or modify those terms.
