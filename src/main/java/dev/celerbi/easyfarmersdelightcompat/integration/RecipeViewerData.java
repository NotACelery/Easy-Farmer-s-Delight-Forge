package dev.celerbi.easyfarmersdelightcompat.integration;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.compat.easymobfarm.EasyMobFarmCompat;
import dev.celerbi.easyfarmersdelightcompat.integration.attached.AttachedCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.attached.AttachedCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.integration.regrowing.RegrowingCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.regrowing.RegrowingCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class RecipeViewerData {

    public static final List<FarmerHarvestInfo> FARMER_TOOL_GUIDES = List.of(
            new FarmerHarvestInfo(
                    id("farmer_harvest/tools/knife"),
                    Ingredient.of(FarmerToolSupport.KNIVES),
                    Ingredient.EMPTY,
                    ToolUse.NONE,
                    false,
                    false,
                    List.of(),
                    Component.translatable("easyfarmersdelightcompat.viewer.farmer_tools.knife")
            ),
            new FarmerHarvestInfo(
                    id("farmer_harvest/tools/hoe"),
                    Ingredient.of(ItemTags.HOES),
                    Ingredient.EMPTY,
                    ToolUse.NONE,
                    false,
                    false,
                    List.of(),
                    Component.translatable("easyfarmersdelightcompat.viewer.farmer_tools.hoe")
            ),
            new FarmerHarvestInfo(
                    id("farmer_harvest/tools/axe"),
                    Ingredient.of(ItemTags.AXES),
                    Ingredient.EMPTY,
                    ToolUse.NONE,
                    false,
                    false,
                    List.of(),
                    Component.translatable("easyfarmersdelightcompat.viewer.farmer_tools.axe")
            ),
            new FarmerHarvestInfo(
                    id("farmer_harvest/tools/shears"),
                    Ingredient.of(Items.SHEARS),
                    Ingredient.EMPTY,
                    ToolUse.NONE,
                    false,
                    false,
                    List.of(),
                    Component.translatable("easyfarmersdelightcompat.viewer.farmer_tools.shears")
            )
    );

    public static final List<FarmerHarvestInfo> PADDY_HARVESTS = List.of(
            new FarmerHarvestInfo(
                    id("paddy_harvest/rice"),
                    ingredient("farmersdelight", "rice"),
                    Ingredient.of(FarmerToolSupport.KNIVES),
                    ToolUse.OPTIONAL,
                    false,
                    true,
                    List.of(stack("farmersdelight", "rice")),
                    Component.translatable("easyfarmersdelightcompat.viewer.paddy_harvest.rice")
            ),
            new FarmerHarvestInfo(
                    id("paddy_harvest/sugar_cane"),
                    Ingredient.of(Items.SUGAR_CANE),
                    Ingredient.EMPTY,
                    ToolUse.NONE,
                    false,
                    false,
                    List.of(new ItemStack(Items.SUGAR_CANE, 2)),
                    Component.translatable("easyfarmersdelightcompat.viewer.paddy_harvest.sugar_cane")
            )
    );

    public static final List<FarmerHarvestInfo> RICH_FARMER_HARVESTS = List.of(
            new FarmerHarvestInfo(
                    id("rich_farmer_harvest/normal_crops"),
                    Ingredient.of(Items.CARROT, Items.POTATO, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS),
                    Ingredient.of(ItemTags.HOES),
                    ToolUse.OPTIONAL,
                    false,
                    true,
                    List.of(new ItemStack(Items.CARROT)),
                    Component.translatable("easyfarmersdelightcompat.viewer.rich_farmer_harvest.normal_crops")
            ),
            new FarmerHarvestInfo(
                    id("rich_farmer_harvest/tomato"),
                    ingredient("farmersdelight", "tomato_seeds"),
                    Ingredient.of(ItemTags.HOES),
                    ToolUse.OPTIONAL,
                    false,
                    true,
                    List.of(stack("farmersdelight", "tomato")),
                    Component.translatable("easyfarmersdelightcompat.viewer.rich_farmer_harvest.tomato")
            ),
            new FarmerHarvestInfo(
                    id("rich_farmer_harvest/mushroom_colonies"),
                    Ingredient.of(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM),
                    Ingredient.of(FarmerToolSupport.KNIVES),
                    ToolUse.REQUIRED,
                    false,
                    false,
                    List.of(new ItemStack(Items.RED_MUSHROOM, 3), new ItemStack(Items.BROWN_MUSHROOM, 3)),
                    Component.translatable("easyfarmersdelightcompat.viewer.rich_farmer_harvest.mushrooms")
            ),
            new FarmerHarvestInfo(
                    id("rich_farmer_harvest/melon"),
                    Ingredient.of(Items.MELON_SEEDS),
                    Ingredient.of(ItemTags.AXES),
                    ToolUse.REQUIRED,
                    true,
                    true,
                    List.of(new ItemStack(Items.MELON_SLICE)),
                    Component.translatable("easyfarmersdelightcompat.viewer.rich_farmer_harvest.melon")
            ),
            new FarmerHarvestInfo(
                    id("rich_farmer_harvest/pumpkin"),
                    Ingredient.of(Items.PUMPKIN_SEEDS),
                    Ingredient.of(ItemTags.AXES),
                    ToolUse.REQUIRED,
                    true,
                    true,
                    List.of(new ItemStack(Items.PUMPKIN)),
                    Component.translatable("easyfarmersdelightcompat.viewer.rich_farmer_harvest.pumpkin")
            )
    );

    public static final List<BlockGuideInfo> BLOCK_GUIDES = List.of(
            new BlockGuideInfo(
                    id("block_guide/paddy_rice"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.paddy_rice.title"),
                    List.of(
                            catalyst(
                                    Ingredient.of(ModBlocks.PADDY_FARMER_ITEM.get(), ModBlocks.RICH_PADDY_FARMER_ITEM
                                            .get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"
                            ),
                            input(ingredient("farmersdelight", "rice"), "easyfarmersdelightcompat.viewer.label.rice"),
                            output(ingredient("farmersdelight", "rice_panicle"), Component
                                    .translatable("easyfarmersdelightcompat.viewer.label.rice"))
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.paddy_rice", 4)
            ),
            new BlockGuideInfo(
                    id("block_guide/paddy_sugar_cane"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.paddy_sugar_cane.title"),
                    List.of(
                            catalyst(
                                    Ingredient.of(ModBlocks.PADDY_FARMER_ITEM.get(), ModBlocks.RICH_PADDY_FARMER_ITEM
                                            .get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"
                            ),
                            input(Ingredient.of(Items.SAND), "easyfarmersdelightcompat.viewer.label.sand"),
                            input(Ingredient.of(Items.SUGAR_CANE), "easyfarmersdelightcompat.viewer.label.sugar_cane"),
                            output(Ingredient.of(new ItemStack(Items.SUGAR_CANE, 2)), Component
                                    .translatable("easyfarmersdelightcompat.viewer.label.sugar_cane"))
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.paddy_sugar_cane", 6)
            ),
            new BlockGuideInfo(
                    id("block_guide/rich_normal_crops"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.rich_normal_crops.title"),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.RICH_FARMER_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(
                                    Ingredient.of(Items.CARROT, Items.POTATO, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS),
                                    "easyfarmersdelightcompat.viewer.label.normal_crop"
                            ),
                            tool(Ingredient.of(ItemTags.HOES), "easyfarmersdelightcompat.viewer.label.hoe")
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.rich_normal_crops", 4)
            ),
            new BlockGuideInfo(
                    id("block_guide/rich_tomatoes_rope"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.rich_tomatoes_rope.title"),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.RICH_FARMER_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(ingredient("farmersdelight", "tomato_seeds"),
                                    "easyfarmersdelightcompat.viewer.label.tomato_seeds"),
                            input(ingredient("farmersdelight", "rope"), "easyfarmersdelightcompat.viewer.label.rope"),
                            tool(Ingredient.of(ItemTags.HOES), "easyfarmersdelightcompat.viewer.label.hoe"),
                            output(ingredient("farmersdelight", "tomato"), stack("farmersdelight", "tomato")
                                    .getHoverName())
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.rich_tomatoes_rope", 5)
            ),
            new BlockGuideInfo(
                    id("block_guide/rich_mushroom_colonies"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.rich_mushroom_colonies.title"),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.RICH_FARMER_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(Ingredient.of(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM),
                                    "easyfarmersdelightcompat.viewer.label.mushroom"),
                            tool(Ingredient.of(FarmerToolSupport.KNIVES),
                                    "easyfarmersdelightcompat.viewer.label.knife"),
                            output(
                                    Ingredient.of(
                                            new ItemStack(Items.RED_MUSHROOM, 3),
                                            new ItemStack(Items.BROWN_MUSHROOM, 3)
                                    ),
                                    Component.translatable("easyfarmersdelightcompat.viewer.label.mushroom")
                            )
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.rich_mushroom_colonies", 5)
            ),
            new BlockGuideInfo(
                    id("block_guide/rich_melon"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.rich_melon.title"),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.RICH_FARMER_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(Ingredient.of(Items.MELON_SEEDS),
                                    "easyfarmersdelightcompat.viewer.label.melon_seeds"),
                            tool(Ingredient.of(ItemTags.AXES), "easyfarmersdelightcompat.viewer.label.axe"),
                            output(Ingredient.of(Items.MELON_SLICE, Items.MELON), new ItemStack(Items.MELON)
                                    .getHoverName())
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.rich_melon", 7)
            ),
            new BlockGuideInfo(
                    id("block_guide/rich_pumpkin"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.rich_pumpkin.title"),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.RICH_FARMER_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(Ingredient.of(Items.PUMPKIN_SEEDS),
                                    "easyfarmersdelightcompat.viewer.label.pumpkin_seeds"),
                            tool(Ingredient.of(ItemTags.AXES), "easyfarmersdelightcompat.viewer.label.axe"),
                            output(Ingredient.of(Items.PUMPKIN), new ItemStack(Items.PUMPKIN).getHoverName())
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.rich_pumpkin", 7)
            ),
            new BlockGuideInfo(
                    id("block_guide/rich_attached_crops"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.rich_attached_crops.title"),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.RICH_FARMER_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(Ingredient.of(Items.JUNGLE_LOG),
                                    "easyfarmersdelightcompat.viewer.label.host_log"),
                            input(Ingredient.of(Items.COCOA_BEANS),
                                    "easyfarmersdelightcompat.viewer.label.planting_item"),
                            output(Ingredient.of(Items.COCOA_BEANS), new ItemStack(Items.COCOA_BEANS).getHoverName())
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.rich_attached_crops", 5)
            ),
            new BlockGuideInfo(
                    id("block_guide/cutter"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.cutter.title"),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.CUTTER_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(ingredient("easy_villagers", "villager"),
                                    "easyfarmersdelightcompat.viewer.label.villager"),
                            input(Ingredient.of(Items.SPRUCE_LOG),
                                    "easyfarmersdelightcompat.viewer.label.cutter_input"),
                            tool(Ingredient.of(FarmerToolSupport.KNIVES),
                                    "easyfarmersdelightcompat.viewer.label.knife"),
                            tool(Ingredient.of(ItemTags.AXES), "easyfarmersdelightcompat.viewer.label.axe"),
                            output(Ingredient.of(Items.STRIPPED_SPRUCE_LOG), new ItemStack(Items.STRIPPED_SPRUCE_LOG)
                                    .getHoverName())
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.cutter", 7)
            ),
            new BlockGuideInfo(
                    id("block_guide/noise_switch"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.noise_switch.title"),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.VILLAGER_NOISE_SWITCH_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(ingredient("easy_villagers", "villager"),
                                    "easyfarmersdelightcompat.viewer.label.villager")
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.noise_switch", 7)
            ),
            new BlockGuideInfo(
                    id("block_guide/iron_farm_noise_switch"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.iron_farm_noise_switch.title"),
                    List.of(
                            catalystPreview(
                                    Ingredient.of(ModBlocks.IRON_FARM_NOISE_SWITCH_ITEM.get()),
                                    emptyIronFarmNoiseSwitch(),
                                    "easyfarmersdelightcompat.viewer.label.machine"
                            ),
                            inputPreview(
                                    Ingredient.of(Items.IRON_BLOCK),
                                    new ItemStack(Items.IRON_BLOCK, 4),
                                    "easyfarmersdelightcompat.viewer.label.iron_block"
                            ),
                            input(Ingredient.of(Items.CARVED_PUMPKIN),
                                    "easyfarmersdelightcompat.viewer.label.carved_pumpkin"),
                            outputPreview(
                                    Ingredient.of(ModBlocks.IRON_FARM_NOISE_SWITCH_ITEM.get()),
                                    completedIronFarmNoiseSwitch(),
                                    Component.translatable(
                                            "easyfarmersdelightcompat.viewer.guide.iron_farm_noise_switch.completed")
                            )
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.iron_farm_noise_switch", 3)
            )
    );

    private RecipeViewerData() {
    }

    public static List<BlockGuideInfo> blockGuides() {
        List<BlockGuideInfo> guides = new ArrayList<>(BLOCK_GUIDES);
        if (EasyMobFarmCompat.isLoaded()
                && ModBlocks.EASY_MOB_FARM_NOISE_SWITCH_ITEM != null
                && ModBlockEntities.EASY_MOB_FARM_NOISE_SWITCH != null) {
            guides.add(new BlockGuideInfo(
                    id("block_guide/easy_mob_farm_noise_switch"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.easy_mob_farm_noise_switch.title"),
                    List.of(
                            catalystPreview(
                                    Ingredient.of(ModBlocks.EASY_MOB_FARM_NOISE_SWITCH_ITEM.get()),
                                    emptyEasyMobFarmNoiseSwitch(),
                                    "easyfarmersdelightcompat.viewer.label.machine"
                            ),
                            inputPreview(
                                    Ingredient.of(Items.ROTTEN_FLESH),
                                    new ItemStack(Items.ROTTEN_FLESH, 6),
                                    "easyfarmersdelightcompat.viewer.label.rotten_flesh"
                            ),
                            outputPreview(
                                    Ingredient.of(ModBlocks.EASY_MOB_FARM_NOISE_SWITCH_ITEM.get()),
                                    completedEasyMobFarmNoiseSwitch(),
                                    Component.translatable(
                                            "easyfarmersdelightcompat.viewer.guide.easy_mob_farm_noise_switch.completed"
                                    )
                            )
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.easy_mob_farm_noise_switch", 3)
            ));
        }

        addOrchardGuides(guides);
        addCinnamonGuide(guides);
        addAttachedDefinitionGuides(guides);
        addRegrowingDefinitionGuides(guides);
        return List.copyOf(guides);
    }

    private static void addOrchardGuides(List<BlockGuideInfo> guides) {
        guides.add(new BlockGuideInfo(
                id("block_guide/orchard/apple"),
                Component.translatable("easyfarmersdelightcompat.viewer.guide.orchard.title"),
                List.of(
                        catalyst(Ingredient.of(ModBlocks.RICH_FARMER_ITEM.get()),
                                "easyfarmersdelightcompat.viewer.label.machine"),
                        input(Ingredient.of(ModBlocks.GRAFTING_SUPPORT_ITEM.get()),
                                "easyfarmersdelightcompat.viewer.label.grafting_support"),
                        input(Ingredient.of(Items.OAK_LEAVES, Items.DARK_OAK_LEAVES),
                                "easyfarmersdelightcompat.viewer.label.orchard_leaves"),
                        tool(Ingredient.of(Items.SHEARS),
                                "easyfarmersdelightcompat.viewer.label.shears"),
                        output(Ingredient.of(Items.APPLE), new ItemStack(Items.APPLE).getHoverName())
                ),
                lines("easyfarmersdelightcompat.viewer.guide.orchard", 6)
        ));

        List<ItemStack> croptopiaLeaves = new ArrayList<>();
        List<ItemStack> croptopiaFruit = new ArrayList<>();
        for (String fruit : List.of("banana", "cherry", "lemon", "coconut", "apple", "mango")) {
            ItemStack leaves = stack("croptopia", fruit + "_crop");
            ItemStack output = stack("croptopia", fruit);
            if (!leaves.isEmpty() && !output.isEmpty()) {
                croptopiaLeaves.add(leaves);
                croptopiaFruit.add(output);
            }
        }
        if (!croptopiaLeaves.isEmpty() && !croptopiaFruit.isEmpty()) {
            guides.add(new BlockGuideInfo(
                    id("block_guide/orchard/croptopia"),
                    Component.translatable("easyfarmersdelightcompat.viewer.guide.croptopia_orchard.title"),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.RICH_FARMER_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(Ingredient.of(ModBlocks.GRAFTING_SUPPORT_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.grafting_support"),
                            input(Ingredient.of(croptopiaLeaves.stream()),
                                    "easyfarmersdelightcompat.viewer.label.orchard_leaves"),
                            tool(Ingredient.of(Items.SHEARS),
                                    "easyfarmersdelightcompat.viewer.label.shears"),
                            output(Ingredient.of(croptopiaFruit.stream()),
                                    Component.translatable("easyfarmersdelightcompat.viewer.label.orchard_fruit"))
                    ),
                    lines("easyfarmersdelightcompat.viewer.guide.croptopia_orchard", 5)
            ));
        }
    }

    private static void addCinnamonGuide(List<BlockGuideInfo> guides) {
        ItemStack cinnamonLog = stack("croptopia", "cinnamon_log");
        ItemStack cinnamonWood = stack("croptopia", "cinnamon_wood");
        ItemStack strippedLog = stack("croptopia", "stripped_cinnamon_log");
        ItemStack strippedWood = stack("croptopia", "stripped_cinnamon_wood");
        ItemStack cinnamon = stack("croptopia", "cinnamon");
        if (cinnamon.isEmpty() || (cinnamonLog.isEmpty() && cinnamonWood.isEmpty())) {
            return;
        }

        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> stripped = new ArrayList<>();
        if (!cinnamonLog.isEmpty() && !strippedLog.isEmpty()) {
            inputs.add(cinnamonLog);
            stripped.add(strippedLog);
        }
        if (!cinnamonWood.isEmpty() && !strippedWood.isEmpty()) {
            inputs.add(cinnamonWood);
            stripped.add(strippedWood);
        }
        if (inputs.isEmpty()) {
            return;
        }

        guides.add(new BlockGuideInfo(
                id("block_guide/cutter/croptopia_cinnamon"),
                Component.translatable("easyfarmersdelightcompat.viewer.guide.cinnamon.title"),
                List.of(
                        catalyst(Ingredient.of(ModBlocks.CUTTER_ITEM.get()),
                                "easyfarmersdelightcompat.viewer.label.machine"),
                        input(Ingredient.of(inputs.stream()),
                                "easyfarmersdelightcompat.viewer.label.cinnamon_wood"),
                        tool(Ingredient.of(ItemTags.AXES),
                                "easyfarmersdelightcompat.viewer.label.axe"),
                        output(Ingredient.of(stripped.stream()),
                                Component.translatable("easyfarmersdelightcompat.viewer.label.stripped_cinnamon_wood")),
                        output(Ingredient.of(cinnamon), cinnamon.getHoverName())
                ),
                lines("easyfarmersdelightcompat.viewer.guide.cinnamon", 3)
        ));
    }

    private static void addAttachedDefinitionGuides(List<BlockGuideInfo> guides) {
        for (AttachedCropDefinition definition : viewerAttachedDefinitions()) {
            if ("easyfarmersdelightcompat".equals(definition.id().getNamespace())
                    && "cocoa".equals(definition.id().getPath())) {
                continue;
            }

            List<ItemStack> planting = matchingItems(definition::matchesPlanting, 16);
            List<ItemStack> hosts = matchingBlocks(definition::matchesHost, 16);
            Block crop = BuiltInRegistries.BLOCK.get(definition.cropBlockId());
            if (planting.isEmpty()
                    || hosts.isEmpty()
                    || crop == null
                    || crop == Blocks.AIR) {
                continue;
            }

            guides.add(new BlockGuideInfo(
                    id("block_guide/attached/" + safeViewerPath(definition.id())),
                    Component.translatable(
                            "easyfarmersdelightcompat.viewer.guide.attached_definition.title",
                            crop.getName()
                    ),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.RICH_FARMER_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(Ingredient.of(hosts.stream()),
                                    "easyfarmersdelightcompat.viewer.label.host_log"),
                            input(Ingredient.of(planting.stream()),
                                    "easyfarmersdelightcompat.viewer.label.planting_item"),
                            output(Ingredient.of(planting.stream()), crop.getName())
                    ),
                    List.of(
                            Component.translatable("easyfarmersdelightcompat.viewer.guide.attached_definition.line1",
                                    crop.getName()),
                            Component.translatable("easyfarmersdelightcompat.viewer.guide.attached_definition.line2"),
                            Component.translatable("easyfarmersdelightcompat.viewer.guide.attached_definition.line3"),
                            Component.translatable("easyfarmersdelightcompat.viewer.guide.attached_definition.line4")
                    )
            ));
        }
    }

    private static void addRegrowingDefinitionGuides(List<BlockGuideInfo> guides) {
        for (RegrowingCropDefinition definition : viewerRegrowingDefinitions()) {
            List<ItemStack> planting = matchingItems(definition::matchesPlanting, 16);
            Block crop = BuiltInRegistries.BLOCK.get(definition.cropBlockId());
            if (planting.isEmpty() || crop == null || crop == Blocks.AIR) {
                continue;
            }

            guides.add(new BlockGuideInfo(
                    id("block_guide/regrowing/" + safeViewerPath(definition.id())),
                    Component.translatable(
                            "easyfarmersdelightcompat.viewer.guide.regrowing_definition.title",
                            crop.getName()
                    ),
                    List.of(
                            catalyst(Ingredient.of(ModBlocks.RICH_FARMER_ITEM.get()),
                                    "easyfarmersdelightcompat.viewer.label.machine"),
                            input(Ingredient.of(planting.stream()),
                                    "easyfarmersdelightcompat.viewer.label.planting_item"),
                            output(Ingredient.of(planting.stream()), crop.getName())
                    ),
                    List.of(
                            Component.translatable("easyfarmersdelightcompat.viewer.guide.regrowing_definition.line1",
                                    crop.getName()),
                            Component.translatable("easyfarmersdelightcompat.viewer.guide.regrowing_definition.line2",
                                    definition.harvestAge(), definition.postHarvestAge()),
                            Component.translatable("easyfarmersdelightcompat.viewer.guide.regrowing_definition.line3"),
                            Component.translatable("easyfarmersdelightcompat.viewer.guide.regrowing_definition.line4")
                    )
            ));
        }
    }

    private static List<AttachedCropDefinition> viewerAttachedDefinitions() {
        LinkedHashMap<ResourceLocation, AttachedCropDefinition> definitions = new LinkedHashMap<>();
        for (AttachedCropDefinition definition : AttachedCropDefinitions.all()) {
            definitions.put(definition.id(), definition);
        }
        loadBundledAttachedDefinition(definitions, "cocoa");
        loadBundledAttachedDefinition(definitions, "ars_bombegranate");
        loadBundledAttachedDefinition(definitions, "ars_mendosteen");
        loadBundledAttachedDefinition(definitions, "ars_frostaya");
        loadBundledAttachedDefinition(definitions, "ars_bastion");
        return List.copyOf(definitions.values());
    }

    private static List<RegrowingCropDefinition> viewerRegrowingDefinitions() {
        LinkedHashMap<ResourceLocation, RegrowingCropDefinition> definitions = new LinkedHashMap<>();
        for (RegrowingCropDefinition definition : RegrowingCropDefinitions.all()) {
            definitions.put(definition.id(), definition);
        }
        loadBundledRegrowingDefinition(definitions, "ars_sourceberry");
        return List.copyOf(definitions.values());
    }

    private static void loadBundledAttachedDefinition(
            Map<ResourceLocation, AttachedCropDefinition> definitions,
            String path
    ) {
        ResourceLocation id = id(path);
        if (definitions.containsKey(id)) {
            return;
        }
        String resourcePath = "data/" + EasyFarmersDelightCompat.MOD_ID + "/efdc_attached_crops/" + path + ".json";
        try (java.io.InputStream stream = RecipeViewerData.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return;
            }
            com.google.gson.JsonElement root = com.google.gson.JsonParser.parseReader(
                    new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)
            );
            if (!root.isJsonObject()) {
                return;
            }
            AttachedCropDefinition definition = AttachedCropDefinition.parse(id, root.getAsJsonObject());
            if (definition != null) {
                definitions.put(id, definition);
            }
        } catch (java.io.IOException | RuntimeException ignored) {
        }
    }

    private static void loadBundledRegrowingDefinition(
            Map<ResourceLocation, RegrowingCropDefinition> definitions,
            String path
    ) {
        ResourceLocation id = id(path);
        if (definitions.containsKey(id)) {
            return;
        }
        String resourcePath = "data/" + EasyFarmersDelightCompat.MOD_ID + "/efdc_regrowing_crops/" + path + ".json";
        try (java.io.InputStream stream = RecipeViewerData.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return;
            }
            com.google.gson.JsonElement root = com.google.gson.JsonParser.parseReader(
                    new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)
            );
            if (!root.isJsonObject()) {
                return;
            }
            RegrowingCropDefinition definition = RegrowingCropDefinition.parse(id, root.getAsJsonObject());
            if (definition != null) {
                definitions.put(id, definition);
            }
        } catch (java.io.IOException | RuntimeException ignored) {
        }
    }

    private static List<ItemStack> matchingItems(
            Predicate<ItemStack> predicate,
            int limit
    ) {
        List<ItemStack> result = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            if (stack.isEmpty() || !predicate.test(stack)) {
                continue;
            }
            result.add(stack.copyWithCount(1));
            if (result.size() >= limit) {
                break;
            }
        }
        return List.copyOf(result);
    }

    private static List<ItemStack> matchingBlocks(
            Predicate<BlockState> predicate,
            int limit
    ) {
        List<ItemStack> result = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!predicate.test(block.defaultBlockState())) {
                continue;
            }
            ItemStack stack = new ItemStack(block);
            if (stack.isEmpty()) {
                continue;
            }
            result.add(stack);
            if (result.size() >= limit) {
                break;
            }
        }
        return List.copyOf(result);
    }

    private static String safeViewerPath(ResourceLocation id) {
        return (id.getNamespace() + "/" + id.getPath()).replace(':', '/');
    }

    public static List<CutterAxeInfo> cutterAxeActions() {
        Ingredient axes = Ingredient.of(ItemTags.AXES);

        CutterAxeActionRow copper = new CutterAxeActionRow(
                Ingredient.of(Items.WAXED_COPPER_BLOCK),
                Ingredient.of(Items.COPPER_BLOCK),
                Component.translatable("easyfarmersdelightcompat.viewer.cutter_axe.any_copper")
        );

        List<ItemStack> logInputs = new ArrayList<>();
        List<ItemStack> strippedOutputs = new ArrayList<>();
        addStripPair(logInputs, strippedOutputs, Items.OAK_LOG, Items.STRIPPED_OAK_LOG);
        addStripPair(logInputs, strippedOutputs, Items.SPRUCE_LOG, Items.STRIPPED_SPRUCE_LOG);
        addStripPair(logInputs, strippedOutputs, Items.BIRCH_LOG, Items.STRIPPED_BIRCH_LOG);
        addStripPair(logInputs, strippedOutputs, Items.JUNGLE_LOG, Items.STRIPPED_JUNGLE_LOG);
        addStripPair(logInputs, strippedOutputs, Items.ACACIA_LOG, Items.STRIPPED_ACACIA_LOG);
        addStripPair(logInputs, strippedOutputs, Items.DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_LOG);
        addStripPair(logInputs, strippedOutputs, Items.MANGROVE_LOG, Items.STRIPPED_MANGROVE_LOG);
        addStripPair(logInputs, strippedOutputs, Items.CHERRY_LOG, Items.STRIPPED_CHERRY_LOG);
        addStripPair(logInputs, strippedOutputs, Items.BAMBOO_BLOCK, Items.STRIPPED_BAMBOO_BLOCK);

        ItemStack probeAxe = new ItemStack(Items.IRON_AXE);
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            if ("minecraft".equals(itemId.getNamespace()))
                continue;

            ItemStack input = item.getDefaultInstance();
            if (input.isEmpty() || !isModdedLogLike(input, itemId))
                continue;

            AxeActionResolver.resolve(input, probeAxe).ifPresent(result -> {
                if (result.action() != AxeActionResolver.Action.STRIP)
                    return;
                if (containsItem(logInputs, input.getItem()))
                    return;
                addStripPair(logInputs, strippedOutputs, input, result.output());
            });
        }

        CutterAxeActionRow logs = new CutterAxeActionRow(
                Ingredient.of(logInputs.stream()),
                Ingredient.of(strippedOutputs.stream()),
                Component.translatable("easyfarmersdelightcompat.viewer.cutter_axe.any_log")
        );

        return List.of(new CutterAxeInfo(
                id("cutter_axe/summary"),
                axes,
                List.of(copper, logs)
        ));
    }

    private static void addStripPair(List<ItemStack> inputs, List<ItemStack> outputs, Item input, Item output) {
        addStripPair(inputs, outputs, new ItemStack(input), new ItemStack(output));
    }

    private static void addStripPair(List<ItemStack> inputs, List<ItemStack> outputs, ItemStack input,
            ItemStack output) {
        if (input.isEmpty() || output.isEmpty())
            return;
        inputs.add(input.copyWithCount(1));
        outputs.add(output.copyWithCount(1));
    }

    private static boolean containsItem(List<ItemStack> stacks, Item item) {
        for (ItemStack stack : stacks) {
            if (stack.getItem() == item)
                return true;
        }
        return false;
    }

    private static boolean isModdedLogLike(ItemStack stack, ResourceLocation id) {
        if (stack.is(ItemTags.LOGS))
            return true;
        String path = id.getPath();
        return path.contains("log") || path.contains("wood") || path.contains("stem") || path.contains("hyphae");
    }

    private static ItemStack emptyIronFarmNoiseSwitch() {
        return new ItemStack(ModBlocks.IRON_FARM_NOISE_SWITCH_ITEM.get());
    }

    private static ItemStack completedIronFarmNoiseSwitch() {
        ItemStack stack = emptyIronFarmNoiseSwitch();
        CompoundTag data = new CompoundTag();
        data.putInt("AssemblyStage", 4);
        data.putBoolean("HasGolem", true);
        BlockItem.setBlockEntityData(stack, ModBlockEntities.IRON_FARM_NOISE_SWITCH.get(), data);
        return stack;
    }

    private static ItemStack emptyEasyMobFarmNoiseSwitch() {
        return new ItemStack(ModBlocks.EASY_MOB_FARM_NOISE_SWITCH_ITEM.get());
    }

    private static ItemStack completedEasyMobFarmNoiseSwitch() {
        ItemStack stack = emptyEasyMobFarmNoiseSwitch();
        CompoundTag data = new CompoundTag();
        data.putInt("AssemblyStage", 6);
        BlockItem.setBlockEntityData(stack, ModBlockEntities.EASY_MOB_FARM_NOISE_SWITCH.get(), data);
        return stack;
    }

    private static GuideIngredient inputPreview(Ingredient ingredient, ItemStack displayStack, String labelKey) {
        return new GuideIngredient(ingredient, GuideIngredient.Role.INPUT, Component.translatable(labelKey),
                displayStack);
    }

    private static GuideIngredient outputPreview(Ingredient ingredient, ItemStack displayStack, Component label) {
        return new GuideIngredient(ingredient, GuideIngredient.Role.OUTPUT, label, displayStack);
    }

    private static GuideIngredient catalystPreview(Ingredient ingredient, ItemStack displayStack, String labelKey) {
        return new GuideIngredient(ingredient, GuideIngredient.Role.CATALYST, Component.translatable(labelKey),
                displayStack);
    }

    private static GuideIngredient input(Ingredient ingredient, String labelKey) {
        return input(ingredient, Component.translatable(labelKey));
    }

    private static GuideIngredient input(Ingredient ingredient, Component label) {
        return new GuideIngredient(ingredient, GuideIngredient.Role.INPUT, label);
    }

    private static GuideIngredient output(Ingredient ingredient, Component label) {
        return new GuideIngredient(ingredient, GuideIngredient.Role.OUTPUT, label);
    }

    private static GuideIngredient tool(Ingredient ingredient, String labelKey) {
        return new GuideIngredient(
                ingredient,
                GuideIngredient.Role.TOOL,
                Component.translatable(labelKey)
        );
    }

    private static GuideIngredient catalyst(Ingredient ingredient, String labelKey) {
        return new GuideIngredient(
                ingredient,
                GuideIngredient.Role.CATALYST,
                Component.translatable(labelKey)
        );
    }

    private static List<Component> lines(String baseKey, int count) {
        List<Component> lines = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            lines.add(Component.translatable(baseKey + ".line" + i));
        }
        return List.copyOf(lines);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, path);
    }

    private static Ingredient ingredient(String namespace, String path) {
        ItemStack stack = stack(namespace, path);
        return stack.isEmpty() ? Ingredient.EMPTY : Ingredient.of(stack);
    }

    private static ItemStack stack(String namespace, String path) {
        ResourceLocation id = new ResourceLocation(namespace, path);
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }
}
