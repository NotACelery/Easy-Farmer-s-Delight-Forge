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
    private static final int OUTPUT_SLOTS = 4, PLAYER_START = 4, PLAYER_END = 40;

    private final BlockPos blockPos;

    public static PaddyFarmerMenu fromNetwork(int id, Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity be = inventory.player.level().getBlockEntity(pos);
        return new PaddyFarmerMenu(id, inventory, pos, be instanceof CompatFarmerBlockEntity f ? f : null);
    }

    public PaddyFarmerMenu(int id, Inventory inventory, CompatFarmerBlockEntity farmer) {
        this(id, inventory, farmer.getBlockPos(), farmer);
    }

    private PaddyFarmerMenu(int id, Inventory inventory, BlockPos pos, CompatFarmerBlockEntity farmer) {
        super(ModMenus.PADDY_FARMER.get(), id);
        blockPos = pos;
        Container output = farmer == null
                ? new SimpleContainer(4)
                : farmer.easyVillagers().getOutputInventory(
                        inventory.player.level().registryAccess());
        if (output == null || output.getContainerSize() < 4)
            output = new SimpleContainer(4);
        for (int i = 0; i < 4; i++)
            addSlot(new Slot(output, i, 52 + i * 18, 20) {
                @Override
                public boolean mayPlace(ItemStack s) {
                    return false;
                }
            });
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 9; c++)
                addSlot(new Slot(inventory, c + r * 9 + 9, 8 + c * 18, 51 + r * 18));
        for (int c = 0; c < 9; c++)
            addSlot(new Slot(inventory, c, 8 + c * 18, 109));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size())
            return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem())
            return ItemStack.EMPTY;
        ItemStack stack = slot.getItem(), original = stack.copy();
        if (index >= PLAYER_START || !moveItemStackTo(stack, PLAYER_START, PLAYER_END, true))
            return ItemStack.EMPTY;
        if (stack.isEmpty())
            slot.setByPlayer(ItemStack.EMPTY);
        else
            slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.distanceToSqr(blockPos.getX() + .5, blockPos.getY() + .5, blockPos.getZ() + .5) > 64)
            return false;
        BlockEntity be = player.level().getBlockEntity(blockPos);
        return be instanceof CompatFarmerBlockEntity f && f.variant().isAquatic() && !f.variant().isRich();
    }
}
