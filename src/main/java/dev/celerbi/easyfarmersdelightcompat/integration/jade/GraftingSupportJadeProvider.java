package dev.celerbi.easyfarmersdelightcompat.integration.jade;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.blockentity.GraftingSupportBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum GraftingSupportJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, "grafting_support_status");
    private static final String ROOT = "EfdcGraftingSupport";
    private static final String HAS_CANOPY = "HasCanopy";
    private static final String PRODUCTIVE = "Productive";
    private static final String MATURE = "Mature";
    private static final String HARVEST = "Harvest";
    private static final String CANOPY = "Canopy";
    private static final String AGE = "Age";
    private static final String MAX_AGE = "MaxAge";
    private static final String RICH_SOIL = "RichSoil";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        GraftingSupportBlockEntity support = resolveSupport(accessor);
        if (support == null) {
            return;
        }
        CompoundTag status = new CompoundTag();
        status.putBoolean(HAS_CANOPY, support.hasCanopy());
        status.putBoolean(PRODUCTIVE, support.isProductive());
        status.putBoolean(MATURE, support.isMature());
        status.putBoolean(RICH_SOIL, support.isOnRichSoil());
        status.putInt(AGE, support.orchardAge());
        status.putInt(MAX_AGE, support.orchardMatureAge());

        ItemStack canopy = support.canopyStack();
        if (!canopy.isEmpty()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(canopy.getItem());
            if (id != null) {
                status.putString(CANOPY, id.toString());
            }
        }
        ItemStack harvest = support.harvestDisplayStack();
        if (!harvest.isEmpty()) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(harvest.getItem());
            if (id != null) {
                status.putString(HARVEST, id.toString());
            }
        }
        data.put(ROOT, status);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(ROOT)) {
            return;
        }
        CompoundTag status = data.getCompound(ROOT);
        if (!status.getBoolean(HAS_CANOPY)) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.grafting_support.empty")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        if (!status.getBoolean(PRODUCTIVE)) {
            Component canopyName = itemName(status.getString(CANOPY));
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.grafting_support.decorative", canopyName)
                    .withStyle(ChatFormatting.WHITE));
            return;
        }

        Component fruitName = itemName(status.getString(HARVEST));
        tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.orchard.name", fruitName)
                .withStyle(ChatFormatting.WHITE));
        int age = Math.max(0, status.getInt(AGE));
        int maxAge = Math.max(1, status.getInt(MAX_AGE));
        if (status.getBoolean(MATURE)) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.orchard.ready")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.waiting.shears")
                    .withStyle(ChatFormatting.RED));
        } else if (!status.getBoolean(RICH_SOIL)) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.grafting_support.needs_rich_soil")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            int percent = Math.max(0, Math.min(100, Math.round((age * 100.0F) / maxAge)));
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.growth", percent)
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.rich_soil.active")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    private static GraftingSupportBlockEntity resolveSupport(BlockAccessor accessor) {
        BlockPos pos = accessor.getPosition();
        if (accessor.getBlockState().is(ModBlocks.GRAFTING_CANOPY.get())) {
            pos = pos.below();
        }
        return accessor.getLevel().getBlockEntity(pos) instanceof GraftingSupportBlockEntity support
                ? support : null;
    }

    private static Component itemName(String idString) {
        ResourceLocation id = idString == null || idString.isBlank() ? null : ResourceLocation.tryParse(idString);
        if (id == null) {
            return Component.translatable("jade.easyfarmersdelightcompat.unknown");
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == Items.AIR) {
            return Component.translatable("jade.easyfarmersdelightcompat.unknown");
        }
        return new ItemStack(item).getHoverName();
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
