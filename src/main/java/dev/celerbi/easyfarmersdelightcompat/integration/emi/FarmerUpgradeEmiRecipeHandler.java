package dev.celerbi.easyfarmersdelightcompat.integration.emi;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Component-safe EMI transfer for the stateful Farmer upgrade recipes.
 *
 * <p>EMI's generic transfer resolves concrete ItemStacks with component equality,
 * while our central Farmer ingredient intentionally matches by Ingredient so a
 * real Farmer carrying machine data can still be upgraded. This handler moves the
 * exact inventory stack and, for Shift/max fills, fills every recipe slot with as
 * many mutually-stackable copies as the grid can physically hold.</p>
 */
public final class FarmerUpgradeEmiRecipeHandler implements EmiRecipeHandler<CraftingMenu> {
    private static final int CRAFT_START = 1;
    private static final int CRAFT_END_EXCLUSIVE = 10;
    private static final int INVENTORY_START = 10;
    private static final int INVENTORY_END_EXCLUSIVE = 46;
    private static final int MAX_GRID_BATCH = 64;

    @Override
    public EmiPlayerInventory getInventory(AbstractContainerScreen<CraftingMenu> screen) {
        CraftingMenu menu = screen.getMenu();
        List<EmiStack> stacks = new ArrayList<>();
        for (int slotId = CRAFT_START; slotId < INVENTORY_END_EXCLUSIVE; slotId++) {
            ItemStack stack = menu.getSlot(slotId).getItem();
            if (!stack.isEmpty()) {
                stacks.add(EmiStack.of(stack));
            }
        }
        return new EmiPlayerInventory(stacks);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof FarmerUpgradeEmiRecipe;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<CraftingMenu> context) {
        if (!(recipe instanceof FarmerUpgradeEmiRecipe upgrade)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        CraftingMenu menu = context.getScreenHandler();
        if (player == null || minecraft.gameMode == null || !menu.getCarried().isEmpty()) {
            return false;
        }

        return calculateTransferAmount(menu, upgrade.minecraftIngredients(), context.getAmount()) > 0;
    }

    @Override
    public boolean craft(EmiRecipe recipe, EmiCraftContext<CraftingMenu> context) {
        if (!(recipe instanceof FarmerUpgradeEmiRecipe upgrade)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        CraftingMenu menu = context.getScreenHandler();
        if (player == null || gameMode == null || !menu.getCarried().isEmpty()) {
            return false;
        }

        List<Ingredient> ingredients = upgrade.minecraftIngredients();
        int amount = calculateTransferAmount(menu, ingredients, context.getAmount());
        if (amount <= 0) {
            return false;
        }

        if (!clearGrid(menu, gameMode, player)) {
            return false;
        }

        for (int recipeSlot = 0; recipeSlot < ingredients.size(); recipeSlot++) {
            Ingredient ingredient = ingredients.get(recipeSlot);
            if (ingredient.isEmpty()) {
                continue;
            }

            int targetSlot = CRAFT_START + recipeSlot;
            if (!moveAmount(menu, gameMode, player, targetSlot, ingredient, amount)) {
                return false;
            }
        }

        // CURSOR/INVENTORY destinations intentionally degrade to FILL. The normal
        // crafting menu remains authoritative for consuming inputs/remainders.
        return true;
    }

    /**
     * Calculates how many complete recipe layers can coexist in the 3x3 grid.
     * Each target slot is locked to one exact ItemStack identity after its first
     * ingredient is selected. This is what lets clean Farmers batch while keeping
     * stateful Farmers (different villager/data components, max stack 1) isolated.
     */
    private static int calculateTransferAmount(CraftingMenu menu, List<Ingredient> ingredients, int requestedAmount) {
        List<ItemStack> virtualInventory = snapshotPlayerInventory(menu);
        if (!virtuallyClearGrid(menu, virtualInventory)) {
            return 0;
        }

        int requested = Math.max(1, Math.min(requestedAmount, MAX_GRID_BATCH));
        ItemStack[] targetTemplates = new ItemStack[ingredients.size()];
        int completed = 0;

        for (int craft = 0; craft < requested; craft++) {
            boolean complete = true;
            for (int recipeSlot = 0; recipeSlot < ingredients.size(); recipeSlot++) {
                Ingredient ingredient = ingredients.get(recipeSlot);
                if (ingredient.isEmpty()) {
                    continue;
                }

                ItemStack template = targetTemplates[recipeSlot];
                if (template == null || template.isEmpty()) {
                    template = bestVirtualTemplate(virtualInventory, ingredient);
                    if (template.isEmpty()) {
                        complete = false;
                        break;
                    }
                    targetTemplates[recipeSlot] = template.copyWithCount(1);
                }

                int targetSlot = CRAFT_START + recipeSlot;
                int stackLimit = Math.min(template.getMaxStackSize(), menu.getSlot(targetSlot).getMaxStackSize(template));
                if (craft + 1 > stackLimit || !consumeExactVirtual(virtualInventory, ingredient, template)) {
                    complete = false;
                    break;
                }
            }

            if (!complete) {
                break;
            }
            completed++;
        }

        return completed;
    }

    private static List<ItemStack> snapshotPlayerInventory(CraftingMenu menu) {
        List<ItemStack> virtual = new ArrayList<>(INVENTORY_END_EXCLUSIVE - INVENTORY_START);
        for (int slotId = INVENTORY_START; slotId < INVENTORY_END_EXCLUSIVE; slotId++) {
            virtual.add(menu.getSlot(slotId).getItem().copy());
        }
        return virtual;
    }

    /** Simulates QUICK_MOVE of every current grid stack back into inventory. */
    private static boolean virtuallyClearGrid(CraftingMenu menu, List<ItemStack> virtualInventory) {
        for (int slotId = CRAFT_START; slotId < CRAFT_END_EXCLUSIVE; slotId++) {
            ItemStack gridStack = menu.getSlot(slotId).getItem();
            if (gridStack.isEmpty()) {
                continue;
            }
            ItemStack remaining = gridStack.copy();
            if (!insertIntoVirtualInventory(virtualInventory, remaining)) {
                return false;
            }
        }
        return true;
    }

    private static boolean insertIntoVirtualInventory(List<ItemStack> inventory, ItemStack remaining) {
        for (ItemStack target : inventory) {
            if (remaining.isEmpty()) {
                return true;
            }
            if (target.isEmpty() || !ItemStack.isSameItemSameTags(target, remaining)) {
                continue;
            }
            int max = Math.min(target.getMaxStackSize(), remaining.getMaxStackSize());
            int room = max - target.getCount();
            if (room <= 0) {
                continue;
            }
            int moved = Math.min(room, remaining.getCount());
            target.grow(moved);
            remaining.shrink(moved);
        }

        for (int i = 0; i < inventory.size() && !remaining.isEmpty(); i++) {
            if (!inventory.get(i).isEmpty()) {
                continue;
            }
            int moved = Math.min(remaining.getCount(), remaining.getMaxStackSize());
            ItemStack placed = remaining.copy();
            placed.setCount(moved);
            inventory.set(i, placed);
            remaining.shrink(moved);
        }

        return remaining.isEmpty();
    }

    private static ItemStack bestVirtualTemplate(List<ItemStack> inventory, Ingredient ingredient) {
        ItemStack best = ItemStack.EMPTY;
        int bestAvailable = 0;

        for (ItemStack candidate : inventory) {
            if (candidate.isEmpty() || !ingredient.test(candidate)) {
                continue;
            }
            int available = exactCount(inventory, candidate);
            if (available > bestAvailable) {
                bestAvailable = available;
                best = candidate;
            }
        }
        return best.isEmpty() ? ItemStack.EMPTY : best.copyWithCount(1);
    }

    private static int exactCount(List<ItemStack> inventory, ItemStack template) {
        int count = 0;
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, template)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean consumeExactVirtual(List<ItemStack> inventory, Ingredient ingredient, ItemStack template) {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()
                    && ingredient.test(stack)
                    && ItemStack.isSameItemSameTags(stack, template)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private static boolean clearGrid(CraftingMenu menu, MultiPlayerGameMode gameMode, Player player) {
        for (int slotId = CRAFT_START; slotId < CRAFT_END_EXCLUSIVE; slotId++) {
            Slot slot = menu.getSlot(slotId);
            if (slot.getItem().isEmpty()) {
                continue;
            }
            gameMode.handleInventoryMouseClick(menu.containerId, slotId, 0, ClickType.QUICK_MOVE, player);
            if (!menu.getSlot(slotId).getItem().isEmpty()) {
                return false;
            }
        }
        return menu.getCarried().isEmpty();
    }

    private static ItemStack bestRealTemplate(CraftingMenu menu, Ingredient ingredient) {
        ItemStack best = ItemStack.EMPTY;
        int bestAvailable = 0;

        for (int slotId = INVENTORY_START; slotId < INVENTORY_END_EXCLUSIVE; slotId++) {
            ItemStack candidate = menu.getSlot(slotId).getItem();
            if (candidate.isEmpty() || !ingredient.test(candidate)) {
                continue;
            }

            int available = 0;
            for (int other = INVENTORY_START; other < INVENTORY_END_EXCLUSIVE; other++) {
                ItemStack stack = menu.getSlot(other).getItem();
                if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, candidate)) {
                    available += stack.getCount();
                }
            }
            if (available > bestAvailable) {
                bestAvailable = available;
                best = candidate;
            }
        }
        return best.isEmpty() ? ItemStack.EMPTY : best.copyWithCount(1);
    }

    private static int findExactSourceSlot(CraftingMenu menu, Ingredient ingredient, ItemStack template) {
        for (int slotId = INVENTORY_START; slotId < INVENTORY_END_EXCLUSIVE; slotId++) {
            ItemStack stack = menu.getSlot(slotId).getItem();
            if (!stack.isEmpty()
                    && ingredient.test(stack)
                    && ItemStack.isSameItemSameTags(stack, template)) {
                return slotId;
            }
        }
        return -1;
    }

    /** Moves {@code amount} exact, mutually-stackable items into one recipe slot. */
    private static boolean moveAmount(
            CraftingMenu menu,
            MultiPlayerGameMode gameMode,
            Player player,
            int targetSlot,
            Ingredient expected,
            int amount
    ) {
        if (!menu.getCarried().isEmpty() || !menu.getSlot(targetSlot).getItem().isEmpty()) {
            return false;
        }

        ItemStack template = bestRealTemplate(menu, expected);
        if (template.isEmpty()) {
            return false;
        }

        int stackLimit = Math.min(template.getMaxStackSize(), menu.getSlot(targetSlot).getMaxStackSize(template));
        if (amount > stackLimit) {
            return false;
        }

        int remaining = amount;
        while (remaining > 0) {
            int sourceSlot = findExactSourceSlot(menu, expected, template);
            if (sourceSlot < 0) {
                return false;
            }

            gameMode.handleInventoryMouseClick(menu.containerId, sourceSlot, 0, ClickType.PICKUP, player);
            if (menu.getCarried().isEmpty()) {
                return false;
            }

            int placeNow = Math.min(remaining, menu.getCarried().getCount());
            for (int i = 0; i < placeNow; i++) {
                gameMode.handleInventoryMouseClick(menu.containerId, targetSlot, 1, ClickType.PICKUP, player);
            }
            remaining -= placeNow;

            // Return unused items from this source stack to the exact source slot.
            if (!menu.getCarried().isEmpty()) {
                gameMode.handleInventoryMouseClick(menu.containerId, sourceSlot, 0, ClickType.PICKUP, player);
            }
            if (!menu.getCarried().isEmpty()) {
                return false;
            }
        }

        ItemStack placed = menu.getSlot(targetSlot).getItem();
        return !placed.isEmpty() && placed.getCount() == amount && expected.test(placed);
    }
}
