package dev.celerbi.easyfarmersdelightcompat.integration.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import java.util.List;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;

public final class FarmerUpgradeEmiRecipeHandler implements StandardRecipeHandler<CraftingMenu> {
    private static final int CRAFT_START = 1;
    private static final int CRAFT_END_EXCLUSIVE = 10;
    private static final int INVENTORY_END_EXCLUSIVE = 46;

    @Override
    public List<Slot> getInputSources(CraftingMenu handler) {
        return List.copyOf(handler.slots.subList(CRAFT_START, INVENTORY_END_EXCLUSIVE));
    }

    @Override
    public List<Slot> getCraftingSlots(CraftingMenu handler) {
        return List.copyOf(handler.slots.subList(CRAFT_START, CRAFT_END_EXCLUSIVE));
    }

    @Override
    public Slot getOutputSlot(CraftingMenu handler) {
        return handler.getSlot(0);
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof FarmerUpgradeEmiRecipe;
    }
}
