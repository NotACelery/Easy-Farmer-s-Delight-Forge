# Easy Farmer's Delight

**Easy Farmer's Delight** is an independent expansion for **Easy Villagers** and **Farmer's Delight**, focused on compact villager-powered farming, specialized crop automation, orchard farming, Cutting Board processing, and quality-of-life features.

The mod expands the familiar Easy Villagers system with new Farmer variants capable of handling crops that need mechanics beyond a normal farmland plot.

If you already know how Easy Villagers works, most machines should feel familiar: store a Villager inside, provide the required crop or materials, and let the machine handle the repetitive work.

Easy Farmer's Delight does **not** replace or modify the standard Easy Villagers Farmer.

***

## Farming

### Paddy Farmer

The **Paddy Farmer** is designed for crops that need a special growing environment.

It currently supports:

* **Farmer's Delight Rice**
* **Sugar Cane**

Rice can be inserted directly and will be grown and harvested automatically.

Sugar Cane requires **Sand** before it can be planted. It grows normally and is harvested from the upper sections while leaving the base planted.

***

### Rich Farmer

The **Rich Farmer** is an advanced Farmer built around **Farmer's Delight Rich Soil**.

It keeps support for normal crops recognized by Easy Villagers while adding specialized farming mechanics for:

* Tomatoes
* Red and Brown Mushroom Colonies
* Melons
* Pumpkins
* Sweet Berry Bushes
* Cocoa
* Grafting Support Orchards
* Ars Nouveau crops when available
* Croptopia crops when available
* Supported crops from other optional integrations

Crops that can benefit from Rich Soil grow faster while keeping their normal harvesting behavior.

The Rich Farmer also includes a dedicated **Harvest Tool slot** for crops that require a Knife, Hoe, Axe, or Shears.

#### Tomatoes and Rope

Insert **Tomato Seeds** to grow Farmer's Delight Tomatoes.

Up to **two Rope sections** can then be added, allowing the plant to grow vertically like a normal Tomato plant.

Each section grows and is harvested independently.

#### Regrowing Bushes

The Rich Farmer supports plants that are harvested without destroying the original crop.

Supported bushes include:

* **Sweet Berry Bushes**
* **Ars Nouveau Sourceberry**, when Ars Nouveau is installed

Once harvested, the bush returns to its normal post-harvest state and begins growing again.

#### Mushroom Colonies

Both **Red Mushroom Colonies** and **Brown Mushroom Colonies** are supported.

Insert the corresponding Mushroom to begin growing a colony.

Once mature, a **Knife** is required in the Harvest Tool slot. The colony remains planted after harvesting and begins growing again.

#### Melons and Pumpkins

The Rich Farmer can automate both **Melons** and **Pumpkins**.

Insert the corresponding seeds and the Farmer will handle the stem and resulting fruit.

An **Axe** is required to harvest the fruit. The stem remains planted for future harvests.

***

### Rich Farmer Log Mode

The Rich Farmer can also grow crops attached directly to logs.

It can store up to **two separate host logs**:

* Lower Log
* Upper Log

Each log provides four usable sides, allowing up to **8 attached crops** in a single Rich Farmer.

The two logs can be different, allowing multiple crop types to grow inside the same machine.

#### Cocoa

**Cocoa Beans** require Jungle Logs, following the same restriction as vanilla Minecraft.

Each Jungle Log can support four Cocoa plants.

#### Ars Nouveau Archfruits

When **Ars Nouveau** is installed, Log Mode also supports its Archfruits:

| Fruit | Required Log |
| --- | --- |
| **Bombegranate** | Blazing Archwood |
| **Mendosteen** | Flourishing Archwood |
| **Frostaya** | Cascading Archwood |
| **Bastion Fruit** | Vexing Archwood |

Each fruit requires its corresponding Archwood family.

Different compatible logs can be combined inside the same Rich Farmer, and attached crops are grown and harvested independently.

#### Reconfiguring Log Mode

Logs and attached crops can be removed using **Sneak + Right Click**.

Upper contents are removed before lower contents, allowing the Farmer to be reconfigured without breaking the machine.

***

### Grafting Support and Orchards

The **Grafting Support** adds a new way to grow fruit directly from leaves.

It is crafted using **Farmer's Delight Rope, Sticks, any Log, and Hanging Roots**, and can be used either as a standalone orchard in the world or inside a Rich Farmer.

#### Standalone Grafting Support

Place a Grafting Support with enough room above it, then insert a block of leaves into the support.

Any normal leaf block can be placed in the support for decoration. Productive leaves need **Farmer's Delight Rich Soil directly underneath the Grafting Support** in order to grow fruit.

By default:

* **Oak Leaves** become an **Apple Orchard**
* **Dark Oak Leaves** become an **Apple Orchard**

The leaves visibly progress through their fruit-growing stages until the orchard is ready to harvest. A mature Apple Orchard produces **2 Apples**, with a **30% chance of a third**, and then begins growing again.

Use **Shears** to harvest a mature standalone orchard. The fruit is dropped into the world while the leaves remain planted for the next cycle.

If the Rich Soil underneath is removed, productive growth pauses until Rich Soil is restored.

The leaf canopy is also a real part of the structure: you can stand on it and interact with it normally. If you want to remove or replace the canopy, breaking it with **Shears** or a **Silk Touch** tool returns the inserted leaf block. Breaking it with another tool or by hand destroys the leaves while leaving the Grafting Support itself in place.

#### Rich Farmer Orchards

The Grafting Support can also be installed directly into an empty **Rich Farmer**.

After inserting the support, add compatible leaves to create an Orchard inside the Farmer. The Rich Farmer then handles the complete fruit-growing cycle automatically.

Place **Shears** in the Harvest Tool slot so the Rich Farmer can harvest mature Orchards. The same Shears behave like normal Minecraft tools, including normal durability and Unbreaking behavior.

Oak and Dark Oak Apple Orchards are available without any additional mods. Optional integrations can add many more productive leaf types.

***

### Rich Paddy Farmer

The **Rich Paddy Farmer** combines Paddy Farmer mechanics with Rich Soil and a Harvest Tool slot.

It supports:

* **Rice**
* **Sugar Cane**

Rice can benefit from the Rich Farmer environment and may optionally use a Knife when appropriate.

Sugar Cane still requires Sand and keeps its normal growth speed.

***

### Harvest Tools

Rich Farmers and Rich Paddy Farmers include a protected **Harvest Tool slot**.

| Tool | Common Uses |
| --- | --- |
| **Knife** | Mushroom Colonies and supported harvesting recipes |
| **Hoe** | Compatible crops and Tomatoes |
| **Axe** | Melons and Pumpkins |
| **Shears** | Grafting Support Orchards |

Tools are only used when the current crop requires them.

If harvesting cannot complete, such as when the output is full, the tool is not unnecessarily damaged.

***

## The Cutter

The **Cutter** is a villager-powered automated version of the **Farmer's Delight Cutting Board**.

It provides:

* **4 input slots**
* **4 output slots**
* **1 protected Cutting Tool slot**
* **1 stored Villager**

Insert an adult Villager, provide the appropriate Knife or Axe, and supply compatible materials.

The Cutter automatically performs supported **Farmer's Delight Cutting Board recipes**.

It can also perform familiar Axe interactions such as:

* Stripping Logs
* Scraping oxidized Copper
* Removing wax from Copper

If the required tool is missing or the output is full, the Cutter waits until it can continue.

### Modded Logs

The Cutter is not limited to vanilla wood types.

Compatible Logs and Stems from other mods can be used when they behave like normal Minecraft logs.

This includes, for example:

* Vanilla Logs
* Crimson and Warped Stems
* Ars Nouveau Archwood
* Compatible Logs from other mods

The selected Log is displayed directly as the Cutter's work surface, preserving its original appearance and animations.

***

## Noise Control

Easy Farmer's Delight includes several blocks for reducing repetitive sounds produced by automated bases.

Noise settings affect **your own client**, so players on the same multiplayer server can use different preferences.

### Villager Noise Switch

The **Villager Noise Switch** can mute or restore Villager voices.

Insert an Easy Villagers Villager and activate the switch.

The preference is remembered when changing worlds or servers.

### Iron Farm Noise Switch

The **Iron Farm Noise Switch** suppresses the repetitive sounds produced by Easy Villagers Iron Farms, including their displayed Iron Golem and Zombie activity.

It does not change how the Iron Farm itself works.

### Easy Mob Farm Noise Switch

When **Easy Mob Farm** is installed, an additional **Easy Mob Farm Noise Switch** becomes available.

Insert **6 Rotten Flesh** to assemble the Zombie displayed inside the switch.

Once complete, the switch can silence the displayed mobs used by Easy Mob Farm for your client.

No real Zombie is spawned.

***

## Optional Integrations

### Ars Nouveau

When **Ars Nouveau** is installed, additional farming support becomes available.

Supported content includes:

* **Magebloom**
* **Sourceberry**
* **Bombegranate**
* **Mendosteen**
* **Frostaya**
* **Bastion Fruit**

Archfruits use Rich Farmer Log Mode together with their corresponding Archwood Logs.

Compatible Archwood Logs can also be used by the Cutter.

Ars Nouveau is completely optional.

### Argentum

When **Argentum** is installed, supported crops can be recognized by the Farmer system.

Currently supported:

* **Yerba**
* **Tea**
* **Sweet Potato**
* **Quince**

Argentum is not required to use Easy Farmer's Delight.

### Croptopia

When **Croptopia** is installed, Easy Farmer's Delight provides full support for both its normal farmland crops and its fruit-tree leaf crops.

* All **58 Croptopia ground crops** can be planted, grown, and harvested by the **Rich Farmer**.
* All **26 Croptopia fruit-tree crop leaves** can be used with the **Grafting Support**.
* Croptopia fruit-tree leaves can also be grown and harvested automatically as **Rich Farmer Orchards**.
* Croptopia fruit leaves keep their own visible growth stages and produce their corresponding fruit.
* Croptopia **Cinnamon Logs and Cinnamon Wood** are supported by the Cutter.

Croptopia is completely optional. The Grafting Support and vanilla Oak/Dark Oak Apple Orchards remain available without it.

### JEI and EMI

Optional **JEI** and **EMI** integration provides in-game information about supported crops, machines, harvesting requirements, Grafting Support Orchards, Cutter interactions, and other Easy Farmer's Delight mechanics.

### Jade

When **Jade** is installed, Easy Farmer's Delight machines can display useful information directly in the world.

Depending on the machine, this can include:

* Current crop
* Growth progress
* Harvest Tool
* Missing tool requirements
* Tomato and Rope progress
* Sugar Cane state
* Melon and Pumpkin progress
* Log Mode information
* Grafting Support and Orchard status
* Cutter status
* Noise Switch status

Jade is completely optional.

***

## Automation and Quality of Life

Easy Farmer's Delight machines are compatible with normal Minecraft item automation.

For the Cutter:

* **Top:** Tools and materials
* **Sides:** Materials
* **Bottom:** Finished products

Protected tool slots cannot be extracted through normal automation.

Machines safely stop when they cannot continue, such as when their output is full, and resume automatically once the problem is resolved.

### Farmer Item Tooltips

Configured Farmers remember their important farming state when picked up.

Hovering over the Farmer in your inventory shows the crop stored inside it.

Rich Farmers using Log Mode or Orchards can also display their stored farming configuration, making configured Farmers easier to identify and organize without placing them again.

***

## Requirements

### NeoForge

* **Minecraft 1.21.1**
* **NeoForge 21.1.235+**
* **Easy Villagers 1.1.42+**
* **Farmer's Delight 1.2.9+**

### Forge

* **Minecraft 1.20.1**
* **Forge 47.x**
* **Easy Villagers 1.1.39+**
* **Farmer's Delight 1.3.3+**

### Optional Mods

* JEI
* EMI
* Jade
* Ars Nouveau
* Argentum
* Croptopia
* Easy Mob Farm

Optional integrations are only enabled when their corresponding mods are installed.

Easy Farmer's Delight must be installed on both the **client and server** when playing multiplayer.

***

## About the Project

**Easy Farmer's Delight is an independent and unofficial project.**

It is not affiliated with, endorsed by, sponsored by, or maintained by the authors of Easy Villagers, Farmer's Delight, Easy Mob Farm, Ars Nouveau, Argentum, Croptopia, Jade, JEI, or EMI.

If you encounter a problem, have a question, or need help with the mod, you can contact me at **[notacelery@gmail.com](mailto:notacelery@gmail.com)** or leave a comment on the CurseForge page.
