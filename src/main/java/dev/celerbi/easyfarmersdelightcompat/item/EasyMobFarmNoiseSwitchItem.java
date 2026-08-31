package dev.celerbi.easyfarmersdelightcompat.item;

import dev.celerbi.easyfarmersdelightcompat.client.EasyMobFarmNoiseSwitchItemRenderer;
import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class EasyMobFarmNoiseSwitchItem extends BlockItem {
    public EasyMobFarmNoiseSwitchItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new EasyMobFarmNoiseSwitchItemRenderer();
                }
                return renderer;
            }
        });
    }
}
