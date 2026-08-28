package dev.celerbi.easyfarmersdelightcompat.item;

import dev.celerbi.easyfarmersdelightcompat.block.*;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.client.CompatFarmerItemRenderer;
import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class CompatFarmerItem extends BlockItem {
    private static final String BET = "BlockEntityTag", DISPLAY = "display";

    public CompatFarmerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        CompoundTag d = stack.getTagElement(BET);
        return d != null && !d.isEmpty() ? 1 : super.getMaxStackSize(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide)
            return;

        ItemStack n = normalizeLoadedStack(stack, level);
        if (n != stack && entity instanceof Player p) {
            p.getInventory().setItem(slot, n);
            p.getInventory().setChanged();
        }
    }

    public static ItemStack normalizeLoadedStack(ItemStack stack, Level level) {
        if (stack.isEmpty()
                || !(stack.getItem() instanceof CompatFarmerItem item)
                || !(item.getBlock() instanceof CompatFarmerBlock block))
            return stack;

        CompoundTag d = stack.getTagElement(BET);
        if (d != null && !d.isEmpty()) {
            try {
                CompatFarmerBlockEntity probe = new CompatFarmerBlockEntity(
                        BlockPos.ZERO,
                        block.defaultBlockState());
                probe.setLevel(level);
                probe.load(d);
                if (probe.hasStoredContents(level.registryAccess()))
                    return stack;
            } catch (RuntimeException e) {
                return stack;
            }
        }

        ItemStack c = new ItemStack(stack.getItem(), stack.getCount());
        CompoundTag root = stack.getTag();
        if (root != null && root.contains(DISPLAY, Tag.TAG_COMPOUND))
            c.addTagElement(DISPLAY, root.getCompound(DISPLAY).copy());

        return ItemStack.isSameItemSameTags(stack, c) ? stack : c;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null)
                    renderer = new CompatFarmerItemRenderer();
                return renderer;
            }
        });
    }
}
