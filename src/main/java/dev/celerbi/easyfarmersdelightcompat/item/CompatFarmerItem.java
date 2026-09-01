package dev.celerbi.easyfarmersdelightcompat.item;

import dev.celerbi.easyfarmersdelightcompat.block.CompatFarmerBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.client.CompatFarmerItemRenderer;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class CompatFarmerItem extends BlockItem {
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String DISPLAY_TAG = "display";

    public CompatFarmerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        CompoundTag data = stack.getTagElement(BLOCK_ENTITY_TAG);
        return data != null && !data.isEmpty() ? 1 : super.getMaxStackSize(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide) {
            return;
        }

        ItemStack normalized = normalizeLoadedStack(stack, level);
        if (normalized != stack && entity instanceof Player player) {
            player.getInventory().setItem(slot, normalized);
            player.getInventory().setChanged();
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        List<Component> crops = storedCropNames(stack, level);
        if (crops.isEmpty()) {
            tooltip.add(Component.translatable(
                            "tooltip.easyfarmersdelightcompat.farmer.crop.none")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        for (Component crop : crops) {
            tooltip.add(Component.translatable(
                            "tooltip.easyfarmersdelightcompat.farmer.crop", crop)
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private List<Component> storedCropNames(ItemStack stack, @Nullable Level level) {
        if (level == null || !(getBlock() instanceof CompatFarmerBlock farmerBlock)) {
            return List.of();
        }

        CompoundTag data = stack.getTagElement(BLOCK_ENTITY_TAG);
        if (data == null || data.isEmpty()) {
            return List.of();
        }

        try {
            CompatFarmerBlockEntity probe = new CompatFarmerBlockEntity(
                    BlockPos.ZERO,
                    farmerBlock.defaultBlockState()
            );
            probe.setLevel(level);
            probe.load(data);
            return probe.plantedCropNames(level.registryAccess());
        } catch (RuntimeException malformedData) {
            return List.of();
        }
    }

    public static ItemStack normalizeLoadedStack(ItemStack stack, Level level) {
        if (stack.isEmpty()
                || !(stack.getItem() instanceof CompatFarmerItem farmerItem)
                || !(farmerItem.getBlock() instanceof CompatFarmerBlock farmerBlock)) {
            return stack;
        }

        CompoundTag data = stack.getTagElement(BLOCK_ENTITY_TAG);
        if (data != null && !data.isEmpty()) {
            try {
                CompatFarmerBlockEntity probe = new CompatFarmerBlockEntity(
                        BlockPos.ZERO,
                        farmerBlock.defaultBlockState()
                );
                probe.setLevel(level);
                probe.load(data);
                if (probe.hasStoredContents(level.registryAccess())) {
                    return stack;
                }
            } catch (RuntimeException malformedLegacyData) {
                return stack;
            }
        }

        ItemStack canonical = new ItemStack(stack.getItem(), stack.getCount());
        CompoundTag root = stack.getTag();
        if (root != null && root.contains(DISPLAY_TAG, Tag.TAG_COMPOUND)) {
            canonical.addTagElement(DISPLAY_TAG, root.getCompound(DISPLAY_TAG).copy());
        }

        return ItemStack.isSameItemSameTags(stack, canonical) ? stack : canonical;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new CompatFarmerItemRenderer();
                }
                return renderer;
            }
        });
    }
}
