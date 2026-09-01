package dev.celerbi.easyfarmersdelightcompat.menu;

import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class PaddyFarmerMenu extends AbstractContainerMenu {
    private static final int OUTPUT_SLOT_COUNT = 4;
    private static final int PLAYER_START = 4;
    private static final int PLAYER_END = 40;

    private final BlockPos blockPos;

    public static PaddyFarmerMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos position = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(position);
        CompatFarmerBlockEntity farmer = blockEntity instanceof CompatFarmerBlockEntity compatFarmer
                ? compatFarmer
                : null;
        return new PaddyFarmerMenu(id, inventory, position, farmer);
    }

    public PaddyFarmerMenu(int id, Inventory inventory, CompatFarmerBlockEntity farmer) {
        this(id, inventory, farmer.getBlockPos(), farmer);
    }

    private PaddyFarmerMenu(int id, Inventory inventory, BlockPos position, CompatFarmerBlockEntity farmer) {
        super(ModMenus.PADDY_FARMER.get(), id);
        blockPos = position;

        Container output = createOutputContainer(inventory, farmer);
        for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
            addSlot(new Slot(output, slot, 52 + slot * 18, 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 51 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 109));
        }
    }

    private static Container createOutputContainer(Inventory inventory, CompatFarmerBlockEntity farmer) {
        if (farmer == null) {
            return new SimpleContainer(OUTPUT_SLOT_COUNT);
        }

        Container output = farmer.easyVillagers().getOutputInventory(inventory.player.level().registryAccess());
        if (output == null || output.getContainerSize() < OUTPUT_SLOT_COUNT) {
            return new SimpleContainer(OUTPUT_SLOT_COUNT);
        }
        return output;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index >= PLAYER_START || !moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.distanceToSqr(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) > 64) {
            return false;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(blockPos);
        return blockEntity instanceof CompatFarmerBlockEntity farmer
                && farmer.variant().isAquatic()
                && !farmer.variant().isRich();
    }
}
