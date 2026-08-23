package dev.celerbi.easyfarmersdelightcompat.compat.jade;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.block.CompatFarmerBlock;
import dev.celerbi.easyfarmersdelightcompat.block.CutterBlock;
import dev.celerbi.easyfarmersdelightcompat.block.VillagerNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.block.IronFarmNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.VillagerNoiseSwitchBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.IronFarmNoiseSwitchBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

/** Optional Jade integration for Farmer family, Cutter and Villager Noise Switch. */
@WailaPlugin("jade")
public final class EfdcJadePlugin implements IWailaPlugin {
    private static final ResourceLocation UID = new ResourceLocation(
            EasyFarmersDelightCompat.MOD_ID,
            "farmer_status"
    );

    private static final String ROOT = "EfdcFarmerStatus";
    private static final String CROP = "Crop";
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

    private static final ResourceLocation RICE = new ResourceLocation("farmersdelight", "rice");
    private static final ResourceLocation BUDDING_TOMATOES = new ResourceLocation("farmersdelight", "budding_tomatoes");
    private static final ResourceLocation TOMATOES = new ResourceLocation("farmersdelight", "tomatoes");
    private static final ResourceLocation RED_MUSHROOM_COLONY = new ResourceLocation("farmersdelight", "red_mushroom_colony");
    private static final ResourceLocation BROWN_MUSHROOM_COLONY = new ResourceLocation("farmersdelight", "brown_mushroom_colony");
    private static final ResourceLocation MELON_STEM = new ResourceLocation("melon_stem");
    private static final ResourceLocation PUMPKIN_STEM = new ResourceLocation("pumpkin_stem");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(ServerDataProvider.INSTANCE, CompatFarmerBlockEntity.class);
        registration.registerBlockDataProvider(FarmerHarvestToolJadeProvider.INSTANCE, CompatFarmerBlockEntity.class);
        registration.registerBlockDataProvider(CutterJadeProvider.INSTANCE, CutterBlockEntity.class);
        registration.registerBlockDataProvider(NoiseSwitchJadeProvider.INSTANCE, VillagerNoiseSwitchBlockEntity.class);
        registration.registerBlockDataProvider(IronFarmNoiseSwitchJadeProvider.INSTANCE, IronFarmNoiseSwitchBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(ClientProvider.INSTANCE, CompatFarmerBlock.class);
        registration.registerBlockComponent(FarmerHarvestToolJadeProvider.INSTANCE, CompatFarmerBlock.class);
        registration.registerBlockComponent(CutterJadeProvider.INSTANCE, CutterBlock.class);
        registration.registerBlockComponent(NoiseSwitchJadeProvider.INSTANCE, VillagerNoiseSwitchBlock.class);
        registration.registerBlockComponent(IronFarmNoiseSwitchJadeProvider.INSTANCE, IronFarmNoiseSwitchBlock.class);
    }

    private enum ServerDataProvider implements IServerDataProvider<BlockAccessor> {
        INSTANCE;

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof CompatFarmerBlockEntity farmer) {
                data.put(ROOT, buildStatus(farmer, accessor.getLevel()));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }

    private enum ClientProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            CompoundTag status = status(accessor);
            if (status == null) return;

            if (status.getBoolean(PADDY_SAND)) {
                appendSugarCane(tooltip, status);
                appendRichSoil(tooltip, status, null);
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
                            cropName(cropId)
                    )
                    .withStyle(ChatFormatting.WHITE));

            if (isStem(cropId)) {
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
            appendRichSoil(tooltip, status, cropId);
            appendWaitingTool(tooltip, status);
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }

    private static CompoundTag status(BlockAccessor accessor) {
        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains(ROOT)) return serverData.getCompound(ROOT);
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
            AgeInfo ageInfo = ageInfo(crop);
            status.putInt(AGE, ageInfo.age());
            status.putInt(MAX_AGE, ageInfo.maxAge());
        }
        return status;
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

    private static void appendRichSoil(ITooltip tooltip, CompoundTag status, ResourceLocation cropId) {
        if (!status.getBoolean(RICH)) return;
        if (status.getBoolean(PADDY_SAND)) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.rich_soil.no_effect_sugar_cane")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        if (cropId == null) return;
        if (isStem(cropId) && status.getInt(AGE) >= status.getInt(MAX_AGE)) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.rich_soil.stem_only")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        if (!status.getBoolean(AQUATIC) && !isGrowthPhaseActive(status, cropId)) {
            return;
        }
        tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.rich_soil.active")
                .withStyle(ChatFormatting.GREEN));
    }

    private static boolean isGrowthPhaseActive(CompoundTag status, ResourceLocation cropId) {
        if (BUDDING_TOMATOES.equals(cropId)) {
            return true;
        }
        if (TOMATOES.equals(cropId)) {
            if (status.getInt(BASE_PROGRESS) < 3) return true;
            int ropes = Math.max(0, Math.min(2, status.getInt(ROPE_COUNT)));
            if (ropes >= 1 && status.getInt(ROPE_ONE_PROGRESS) < 3) return true;
            return ropes >= 2 && status.getInt(ROPE_TWO_PROGRESS) < 3;
        }
        int maxAge = status.getInt(MAX_AGE);
        return maxAge > 0 && status.getInt(AGE) < maxAge;
    }

    private static void appendWaitingTool(ITooltip tooltip, CompoundTag status) {
        String waiting = status.getString(WAITING_TOOL);
        if (waiting.isEmpty()) return;
        String key = switch (waiting) {
            case "KNIFE" -> "jade.easyfarmersdelightcompat.waiting.knife";
            case "AXE" -> "jade.easyfarmersdelightcompat.waiting.axe";
            case "KNIFE_OR_AXE" -> "jade.easyfarmersdelightcompat.waiting.knife_or_axe";
            default -> null;
        };
        if (key != null) tooltip.add(Component.translatable(key).withStyle(ChatFormatting.RED));
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

    private static Component cropName(ResourceLocation cropId) {
        if (cropId == null) return Component.translatable("jade.easyfarmersdelightcompat.unknown");
        if (RICE.equals(cropId)) return Component.translatable("item.farmersdelight.rice");
        if (isTomato(cropId)) return Component.translatable("item.farmersdelight.tomato");
        if (RED_MUSHROOM_COLONY.equals(cropId)) return Component.translatable("block.minecraft.red_mushroom");
        if (BROWN_MUSHROOM_COLONY.equals(cropId)) return Component.translatable("block.minecraft.brown_mushroom");
        if (MELON_STEM.equals(cropId)) return Blocks.MELON.getName();
        if (PUMPKIN_STEM.equals(cropId)) return Blocks.PUMPKIN.getName();
        Block crop = BuiltInRegistries.BLOCK.get(cropId);
        return crop.getName();
    }

    private static boolean isTomato(ResourceLocation cropId) {
        return BUDDING_TOMATOES.equals(cropId) || TOMATOES.equals(cropId);
    }

    private static boolean isStem(ResourceLocation cropId) {
        return MELON_STEM.equals(cropId) || PUMPKIN_STEM.equals(cropId);
    }

    private static int percent(int progress, int maximum) {
        if (maximum <= 0) return 0;
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
