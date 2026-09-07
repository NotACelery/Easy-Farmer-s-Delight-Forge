package dev.celerbi.easyfarmersdelightcompat.integration.jade;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.integration.ToolRequirement;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum FarmerStatusJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;
    private static final ResourceLocation UID = new ResourceLocation(
            EasyFarmersDelightCompat.MOD_ID,
            "farmer_status"
    );

    private static final String ROOT = "EfdcFarmerStatus";
    private static final String CROP = "Crop";
    private static final String CROP_DISPLAY_ITEM = "CropDisplayItem";
    private static final String NOCTURNAL_MILLET_PANICLE_AGE = "NocturnalMilletPanicleAge";
    private static final String AGE = "Age";
    private static final String MAX_AGE = "MaxAge";
    private static final String PADDY_GROWTH = "PaddyGrowth";
    private static final String BASE_PROGRESS = "BaseProgress";
    private static final String ROPE_ONE_PROGRESS = "RopeOneProgress";
    private static final String ROPE_TWO_PROGRESS = "RopeTwoProgress";
    private static final String ROPE_COUNT = "RopeCount";
    private static final String AQUATIC = "Aquatic";
    private static final String RICH = "Rich";
    private static final String FRUIT_READY = "FruitReady";
    private static final String PADDY_SAND = "PaddySand";
    private static final String SUGAR_CANE_HEIGHT = "SugarCaneHeight";
    private static final String SUGAR_CANE_AGE = "SugarCaneAge";
    private static final String HARVEST_READY = "HarvestReady";
    private static final String WAITING_TOOL = "WaitingTool";
    private static final String ATTACHED = "Attached";
    private static final String LOWER_HOST = "LowerHost";
    private static final String UPPER_HOST = "UpperHost";
    private static final String LOWER_OCCUPIED = "LowerOccupied";
    private static final String UPPER_OCCUPIED = "UpperOccupied";
    private static final String ORCHARD_SUPPORT = "OrchardSupport";
    private static final String ORCHARD_HARVEST = "OrchardHarvest";
    private static final String ORCHARD_AGE = "OrchardAge";
    private static final String ORCHARD_MAX_AGE = "OrchardMaxAge";

    private static final ResourceLocation RICE = new ResourceLocation("farmersdelight", "rice");
    private static final ResourceLocation BUDDING_TOMATOES = new ResourceLocation("farmersdelight",
            "budding_tomatoes");
    private static final ResourceLocation TOMATOES = new ResourceLocation("farmersdelight",
            "tomatoes");
    private static final ResourceLocation RED_MUSHROOM_COLONY = new ResourceLocation("farmersdelight",
            "red_mushroom_colony");
    private static final ResourceLocation BROWN_MUSHROOM_COLONY = new ResourceLocation(
            "farmersdelight", "brown_mushroom_colony");
    private static final ResourceLocation NOCTURNAL_MILLET_STALK = new ResourceLocation(
            "eternal_starlight", "nocturnal_millet_stalk");
    private static final ResourceLocation MELON_STEM = new ResourceLocation("melon_stem");
    private static final ResourceLocation PUMPKIN_STEM = new ResourceLocation("pumpkin_stem");

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof CompatFarmerBlockEntity farmer) {
            data.put(ROOT, buildStatus(farmer, accessor.getLevel()));
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag status = status(accessor);
        if (status == null)
            return;

        if (status.getBoolean(PADDY_SAND)) {
            appendSugarCane(tooltip, status);
            appendWaitingTool(tooltip, status);
            return;
        }

        if (status.getBoolean(ORCHARD_SUPPORT)) {
            appendOrchard(tooltip, status);
            appendWaitingTool(tooltip, status);
            return;
        }

        if (status.getBoolean(ATTACHED)) {
            appendAttached(tooltip, status);
            appendWaitingTool(tooltip, status);
            return;
        }

        String cropIdString = status.getString(CROP);
        ResourceLocation cropId = cropIdString.isEmpty() ? null : ResourceLocation.tryParse(cropIdString);
        if (cropId == null) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.crop.none")
                    .withStyle(ChatFormatting.GRAY));
            appendWaitingTool(tooltip, status);
            return;
        }

        tooltip.add(Component.translatable(
                        "jade.easyfarmersdelightcompat.crop",
                        cropName(cropId, status.getString(CROP_DISPLAY_ITEM))
                )
                .withStyle(ChatFormatting.WHITE));

        if (NOCTURNAL_MILLET_STALK.equals(cropId)) {
            int baseAge = status.getInt(AGE);
            int baseMax = Math.max(1, status.getInt(MAX_AGE));
            int panicleAge = status.getInt(NOCTURNAL_MILLET_PANICLE_AGE);
            int progress = Math.min(baseMax + 3, baseAge + (panicleAge < 0 ? 0 : panicleAge + 1));
            tooltip.add(Component.translatable(
                            "jade.easyfarmersdelightcompat.growth",
                            percent(progress, baseMax + 3)
                    )
                    .withStyle(ChatFormatting.GRAY));
        } else if (isStem(cropId)) {
            appendStemStatus(tooltip, status);
        } else if (isTomato(cropId)) {
            appendTomatoGrowth(tooltip, status);
        } else if (status.getBoolean(AQUATIC)) {
            tooltip.add(Component.translatable(
                            "jade.easyfarmersdelightcompat.growth",
                            percent(status.getInt(PADDY_GROWTH), 7)
                    )
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable(
                            "jade.easyfarmersdelightcompat.growth",
                            percent(status.getInt(AGE), status.getInt(MAX_AGE))
                    )
                    .withStyle(ChatFormatting.GRAY));
        }

        if (status.getBoolean(HARVEST_READY) && !isStem(cropId)) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.harvest.ready")
                    .withStyle(ChatFormatting.GREEN));
        }
        appendWaitingTool(tooltip, status);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private static CompoundTag status(BlockAccessor accessor) {
        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains(ROOT))
            return serverData.getCompound(ROOT);
        if (accessor.getBlockEntity() instanceof CompatFarmerBlockEntity farmer) {
            return buildStatus(farmer, accessor.getLevel());
        }
        return null;
    }

    private static CompoundTag buildStatus(CompatFarmerBlockEntity farmer, net.minecraft.world.level.Level level) {
        CompoundTag status = new CompoundTag();
        status.putBoolean(AQUATIC, farmer.variant().isAquatic());
        status.putBoolean(RICH, farmer.variant().isRich());
        status.putInt(PADDY_GROWTH, farmer.paddyGrowth());
        status.putInt(BASE_PROGRESS, farmer.baseProgress());
        status.putInt(ROPE_ONE_PROGRESS, farmer.ropeOneProgress());
        status.putInt(ROPE_TWO_PROGRESS, farmer.ropeTwoProgress());
        status.putInt(ROPE_COUNT, farmer.ropeCount());
        status.putBoolean(FRUIT_READY, farmer.fruitReady());
        status.putBoolean(PADDY_SAND, farmer.hasPaddySand());
        status.putInt(SUGAR_CANE_HEIGHT, farmer.sugarCaneHeight());
        status.putInt(SUGAR_CANE_AGE, farmer.sugarCaneAge());

        if (farmer.hasGraftingSupport()) {
            status.putBoolean(ORCHARD_SUPPORT, true);
            ItemStack harvest = farmer.orchardHarvestDisplayStack();
            if (!harvest.isEmpty()) {
                ResourceLocation harvestId = BuiltInRegistries.ITEM.getKey(harvest.getItem());
                if (harvestId != null) {
                    status.putString(ORCHARD_HARVEST, harvestId.toString());
                }
                status.putInt(ORCHARD_AGE, farmer.orchardAge());
                status.putInt(ORCHARD_MAX_AGE, farmer.orchardMatureAge());
            }
        }

        if (farmer.hasAttachedSetup()) {
            status.putBoolean(ATTACHED, true);
            appendAttachedLevelStatus(status, farmer, 0, LOWER_HOST, LOWER_OCCUPIED);
            appendAttachedLevelStatus(status, farmer, 1, UPPER_HOST, UPPER_OCCUPIED);
        }

        ToolRequirement requirement = farmer.currentToolRequirement();
        ItemStack tool = farmer.getHarvestTool();
        if (requirement.isRequired()) {
            status.putBoolean(HARVEST_READY, true);
            if (!requirement.isSatisfiedBy(tool)) {
                status.putString(WAITING_TOOL, requirement.name());
            }
        }

        BlockState crop = farmer.easyVillagers().getCrop(level.registryAccess());
        if (crop != null) {
            ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(crop.getBlock());
            status.putString(CROP, cropId.toString());
            ItemStack display = farmer.cropDisplayStack(level.registryAccess());
            if (!display.isEmpty()) {
                ResourceLocation displayId = BuiltInRegistries.ITEM.getKey(display.getItem());
                if (displayId != null) {
                    status.putString(CROP_DISPLAY_ITEM, displayId.toString());
                }
            }
            if (NOCTURNAL_MILLET_STALK.equals(cropId)) {
                status.putInt(NOCTURNAL_MILLET_PANICLE_AGE, farmer.nocturnalMilletPanicleAge());
            }
            AgeInfo ageInfo = ageInfo(crop);
            status.putInt(AGE, ageInfo.age());
            status.putInt(MAX_AGE, ageInfo.maxAge());
        }
        return status;
    }

    private static void appendOrchard(ITooltip tooltip, CompoundTag status) {
        String harvestIdString = status.getString(ORCHARD_HARVEST);
        ResourceLocation harvestId = harvestIdString.isEmpty() ? null : ResourceLocation.tryParse(harvestIdString);
        if (harvestId == null) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.orchard.support")
                    .withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.orchard.waiting_leaves")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        net.minecraft.world.item.Item harvestItem = BuiltInRegistries.ITEM.get(harvestId);
        ItemStack harvest = harvestItem == null ? ItemStack.EMPTY : new ItemStack(harvestItem);
        Component fruitName = harvest.isEmpty()
                ? Component.translatable("jade.easyfarmersdelightcompat.unknown")
                : harvest.getHoverName();
        tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.orchard.name", fruitName)
                .withStyle(ChatFormatting.WHITE));

        int age = status.getInt(ORCHARD_AGE);
        int maxAge = Math.max(1, status.getInt(ORCHARD_MAX_AGE));
        if (age >= maxAge) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.orchard.ready")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable(
                            "jade.easyfarmersdelightcompat.growth",
                            percent(age, maxAge)
                    )
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static void appendAttached(ITooltip tooltip, CompoundTag status) {
        appendAttachedLevel(tooltip, status, LOWER_HOST, LOWER_OCCUPIED,
                "jade.easyfarmersdelightcompat.attached.lower");
        appendAttachedLevel(tooltip, status, UPPER_HOST, UPPER_OCCUPIED,
                "jade.easyfarmersdelightcompat.attached.upper");
    }

    private static void appendAttachedLevel(
            ITooltip tooltip,
            CompoundTag status,
            String hostKey,
            String occupiedKey,
            String translationKey
    ) {
        String hostId = status.getString(hostKey);
        if (hostId.isEmpty()) {
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(hostId);
        Block block = id == null ? null : BuiltInRegistries.BLOCK.get(id);
        if (block == null || block == Blocks.AIR) {
            return;
        }
        int occupied = Math.max(0, Math.min(4, status.getInt(occupiedKey)));
        tooltip.add(Component.translatable(translationKey, block.getName(), occupied)
                .withStyle(ChatFormatting.GRAY));
    }

    private static void appendAttachedLevelStatus(
            CompoundTag status,
            CompatFarmerBlockEntity farmer,
            int levelIndex,
            String hostKey,
            String occupiedKey
    ) {
        BlockState host = farmer.attachedHostState(levelIndex);
        if (host == null || host.isAir()) {
            return;
        }
        status.putString(hostKey, BuiltInRegistries.BLOCK.getKey(host.getBlock()).toString());
        int occupied = 0;
        for (int faceIndex = 0; faceIndex < farmer.attachedFaceCount(); faceIndex++) {
            if (!farmer.attachedCropState(levelIndex, faceIndex).isAir()) {
                occupied++;
            }
        }
        status.putInt(occupiedKey, occupied);
    }

    private static void appendSugarCane(ITooltip tooltip, CompoundTag status) {
        int height = Math.max(0, Math.min(3, status.getInt(SUGAR_CANE_HEIGHT)));
        if (height == 0) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.sugar_cane.mode")
                    .withStyle(ChatFormatting.WHITE));
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.sugar_cane.substrate")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.sugar_cane.ready")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable(
                        "jade.easyfarmersdelightcompat.crop",
                        Blocks.SUGAR_CANE.getName()
                )
                .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.sugar_cane.growth", height, 3)
                .withStyle(ChatFormatting.GRAY));
        if (height < 3) {
            int age = Math.max(0, Math.min(15, status.getInt(SUGAR_CANE_AGE)));
            tooltip.add(Component.translatable(
                            "jade.easyfarmersdelightcompat.sugar_cane.next_segment",
                            Math.round((age * 100.0F) / 16.0F)
                    )
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static void appendStemStatus(ITooltip tooltip, CompoundTag status) {
        int age = status.getInt(AGE);
        int maxAge = Math.max(1, status.getInt(MAX_AGE));
        tooltip.add(Component.translatable(
                        "jade.easyfarmersdelightcompat.stem_growth",
                        percent(age, maxAge)
                )
                .withStyle(ChatFormatting.GRAY));
        if (age >= maxAge) {
            tooltip.add(Component.translatable(status.getBoolean(FRUIT_READY)
                            ? "jade.easyfarmersdelightcompat.fruit.ready"
                            : "jade.easyfarmersdelightcompat.fruit.growing")
                    .withStyle(status.getBoolean(FRUIT_READY) ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
    }

    private static void appendWaitingTool(ITooltip tooltip, CompoundTag status) {
        String waiting = status.getString(WAITING_TOOL);
        if (waiting.isEmpty())
            return;
        String key = switch (waiting) {
            case "KNIFE" -> "jade.easyfarmersdelightcompat.waiting.knife";
            case "HOE" -> "jade.easyfarmersdelightcompat.waiting.hoe";
            case "AXE" -> "jade.easyfarmersdelightcompat.waiting.axe";
            case "SHEARS" -> "jade.easyfarmersdelightcompat.waiting.shears";
            case "KNIFE_OR_AXE" -> "jade.easyfarmersdelightcompat.waiting.knife_or_axe";
            default -> null;
        };
        if (key != null)
            tooltip.add(Component.translatable(key).withStyle(ChatFormatting.RED));
    }

    private static void appendTomatoGrowth(ITooltip tooltip, CompoundTag status) {
        int base = percent(status.getInt(BASE_PROGRESS), 3);
        int ropes = Math.max(0, Math.min(2, status.getInt(ROPE_COUNT)));

        MutableComponent line;
        if (ropes >= 2) {
            line = Component.translatable(
                    "jade.easyfarmersdelightcompat.growth.tomato.two_ropes",
                    base,
                    percent(status.getInt(ROPE_ONE_PROGRESS), 3),
                    percent(status.getInt(ROPE_TWO_PROGRESS), 3)
            );
        } else if (ropes == 1) {
            line = Component.translatable(
                    "jade.easyfarmersdelightcompat.growth.tomato.one_rope",
                    base,
                    percent(status.getInt(ROPE_ONE_PROGRESS), 3)
            );
        } else {
            line = Component.translatable(
                    "jade.easyfarmersdelightcompat.growth.tomato.base",
                    base
            );
        }
        tooltip.add(line.withStyle(ChatFormatting.GRAY));
    }

    private static Component cropName(ResourceLocation cropId, String displayItemIdString) {
        if (cropId == null)
            return Component.translatable("jade.easyfarmersdelightcompat.unknown");
        ResourceLocation displayItemId = displayItemIdString == null || displayItemIdString.isEmpty()
                ? null : ResourceLocation.tryParse(displayItemIdString);
        if (displayItemId != null) {
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(displayItemId);
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                return new ItemStack(item).getHoverName();
            }
        }
        if (RICE.equals(cropId))
            return Component.translatable("item.farmersdelight.rice");
        if (isTomato(cropId))
            return Component.translatable("item.farmersdelight.tomato");
        if (RED_MUSHROOM_COLONY.equals(cropId))
            return Component.translatable("block.minecraft.red_mushroom");
        if (BROWN_MUSHROOM_COLONY.equals(cropId))
            return Component.translatable("block.minecraft.brown_mushroom");
        if (MELON_STEM.equals(cropId))
            return Blocks.MELON.getName();
        if (PUMPKIN_STEM.equals(cropId))
            return Blocks.PUMPKIN.getName();
        Block crop = BuiltInRegistries.BLOCK.get(cropId);
        if (crop != null && crop != Blocks.AIR && crop.asItem() != net.minecraft.world.item.Items.AIR) {
            return new ItemStack(crop.asItem()).getHoverName();
        }
        return Component.literal(humanizeCropPath(cropId.getPath()));
    }

    private static String humanizeCropPath(String path) {
        String cleaned = path;
        for (String suffix : new String[]{"_crop", "_bush", "_stem", "_vines", "_plant", "_stalk"}) {
            if (cleaned.endsWith(suffix)) {
                cleaned = cleaned.substring(0, cleaned.length() - suffix.length());
                break;
            }
        }
        StringBuilder result = new StringBuilder();
        for (String part : cleaned.split("_")) {
            if (part.isEmpty()) continue;
            if (result.length() > 0) result.append(' ');
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.length() == 0 ? path : result.toString();
    }

    private static boolean isTomato(ResourceLocation cropId) {
        return BUDDING_TOMATOES.equals(cropId) || TOMATOES.equals(cropId);
    }

    private static boolean isStem(ResourceLocation cropId) {
        if (cropId == null) return false;
        Block block = BuiltInRegistries.BLOCK.get(cropId);
        return block instanceof StemBlock;
    }

    private static int percent(int progress, int maximum) {
        if (maximum <= 0)
            return 0;
        int safeProgress = Math.max(0, Math.min(maximum, progress));
        return Math.round((safeProgress * 100.0F) / maximum);
    }

    private static AgeInfo ageInfo(BlockState state) {
        Optional<Property<?>> ageProperty = state.getProperties().stream()
                .filter(property -> property.getName().equals("age"))
                .findFirst();
        if (ageProperty.isEmpty() || !(ageProperty.get() instanceof IntegerProperty integerProperty)) {
            return new AgeInfo(0, 0);
        }
        int age = state.getValue(integerProperty);
        int maxAge = integerProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(age);
        return new AgeInfo(age, maxAge);
    }

    private record AgeInfo(int age, int maxAge) {
    }
}
