package dev.celerbi.easyfarmersdelightcompat.integration;

import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public final class OutputSimulator {
    private OutputSimulator() {}

    public static boolean canFitAll(IItemHandler output, List<ItemStack> stacks) {
        if (output == null)
            return false;
        ItemStack[] simulated = new ItemStack[output.getSlots()];
        for (int slot = 0; slot < simulated.length; slot++)
            simulated[slot] = output.getStackInSlot(slot).copy();
        for (ItemStack source : stacks) {
            if (source == null || source.isEmpty())
                continue;
            ItemStack remaining = source.copy();
            insertIntoSimulation(output, simulated, remaining);
            if (!remaining.isEmpty())
                return false;
        }
        return true;
    }

    public static boolean insertAll(IItemHandler output, List<ItemStack> stacks) {
        if (!canFitAll(output, stacks))
            return false;
        for (ItemStack source : stacks) {
            if (source == null || source.isEmpty())
                continue;
            ItemStack remaining = source.copy();
            for (int slot = 0; slot < output.getSlots() && !remaining.isEmpty(); slot++) {
                remaining = output.insertItem(slot, remaining, false);
            }
            if (!remaining.isEmpty())
                throw new IllegalStateException("Output changed after successful Cutter simulation");
        }
        return true;
    }

    private static void insertIntoSimulation(IItemHandler output, ItemStack[] simulated, ItemStack remaining) {
        for (int slot = 0; slot < simulated.length && !remaining.isEmpty(); slot++) {
            if (!output.isItemValid(slot, remaining))
                continue;
            ItemStack existing = simulated[slot];
            int max = Math.min(output.getSlotLimit(slot), remaining.getMaxStackSize());
            if (existing.isEmpty()) {
                int move = Math.min(max, remaining.getCount());
                simulated[slot] = remaining.copyWithCount(move);
                remaining.shrink(move);
                continue;
            }
            if (!ItemStack.isSameItemSameTags(existing, remaining))
                continue;
            max = Math.min(max, existing.getMaxStackSize());
            int room = max - existing.getCount();
            if (room <= 0)
                continue;
            int move = Math.min(room, remaining.getCount());
            existing.grow(move);
            remaining.shrink(move);
        }
    }

    public static boolean canFitAll(Container output, List<ItemStack> stacks) {
        if (output == null)
            return false;
        ItemStack[] simulated = new ItemStack[output.getContainerSize()];
        for (int slot = 0; slot < simulated.length; slot++)
            simulated[slot] = output.getItem(slot).copy();
        for (ItemStack source : stacks) {
            if (source == null || source.isEmpty())
                continue;
            ItemStack remaining = source.copy();
            for (int slot = 0; slot < simulated.length && !remaining.isEmpty(); slot++) {
                if (!output.canPlaceItem(slot, remaining))
                    continue;
                ItemStack existing = simulated[slot];
                if (existing.isEmpty()) {
                    int move = Math.min(remaining.getCount(), Math.min(remaining.getMaxStackSize(), output
                            .getMaxStackSize()));
                    simulated[slot] = remaining.copyWithCount(move);
                    remaining.shrink(move);
                } else if (ItemStack.isSameItemSameTags(existing, remaining)) {
                    int max = Math.min(existing.getMaxStackSize(), output.getMaxStackSize());
                    int move = Math.min(max - existing.getCount(), remaining.getCount());
                    if (move > 0) {
                        existing.grow(move);
                        remaining.shrink(move);
                    }
                }
            }
            if (!remaining.isEmpty())
                return false;
        }
        return true;
    }
}
