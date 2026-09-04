package dev.celerbi.easyfarmersdelightcompat.integration.jade;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum FarmerHarvestToolJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(
            EasyFarmersDelightCompat.MOD_ID,
            "farmer_knife"
    );
    private static final String KEY = "EfdcFarmerHarvestTool";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        data.remove(KEY);
        if (accessor.getBlockEntity() instanceof CompatFarmerBlockEntity farmer && farmer.variant().isRich()) {
            ItemStack tool = farmer.getHarvestTool();
            if (!tool.isEmpty())
                data.put(KEY, tool.save(new net.minecraft.nbt.CompoundTag()));
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.contains(KEY, Tag.TAG_COMPOUND))
            return;
        ItemStack tool = ItemStack.of(data.getCompound(KEY));
        if (!tool.isEmpty()) {
            tooltip.add(Component.translatable(
                    "jade.easyfarmersdelightcompat.farmer.tool",
                    tool.getHoverName()
            ));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
