package dev.celerbi.easyfarmersdelightcompat.item;

import dev.celerbi.easyfarmersdelightcompat.client.VillagerNoiseSwitchItemRenderer;
import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class VillagerNoiseSwitchItem extends BlockItem {
    public VillagerNoiseSwitchItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        CompoundTag d = stack.getTagElement("BlockEntityTag");
        return d != null && !d.isEmpty() ? 1 : super.getMaxStackSize(stack);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null)
                    renderer = new VillagerNoiseSwitchItemRenderer();
                return renderer;
            }
        });
    }
}
