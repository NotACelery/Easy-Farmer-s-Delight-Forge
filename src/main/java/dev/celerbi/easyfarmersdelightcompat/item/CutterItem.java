package dev.celerbi.easyfarmersdelightcompat.item;

import dev.celerbi.easyfarmersdelightcompat.client.CutterItemRenderer;
import dev.celerbi.easyfarmersdelightcompat.integration.CutterLogVariant;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class CutterItem extends BlockItem {
    public CutterItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        CompoundTag data = stack.getTagElement("BlockEntityTag");
        if (data == null || data.isEmpty())
            return super.getMaxStackSize(stack);

        CompoundTag copy = data.copy();
        copy.remove("id");
        copy.remove("x");
        copy.remove("y");
        copy.remove("z");
        copy.remove(CutterLogVariant.NBT_KEY);
        return copy.isEmpty() ? super.getMaxStackSize(stack) : 1;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Block variant = CutterLogVariant.fromStack(stack);
        tooltip.add(Component.translatable(
                        "tooltip.easyfarmersdelightcompat.cutter.variant",
                        Component.translatable(CutterLogVariant.translationKey(variant)))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null)
                    renderer = new CutterItemRenderer();
                return renderer;
            }
        });
    }
}
