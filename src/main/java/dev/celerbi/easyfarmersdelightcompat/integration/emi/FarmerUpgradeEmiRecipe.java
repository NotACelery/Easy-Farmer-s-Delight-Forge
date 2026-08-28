package dev.celerbi.easyfarmersdelightcompat.integration.emi;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public final class FarmerUpgradeEmiRecipe extends EmiCraftingRecipe {
    private final ResourceLocation gameplayId;
    private final List<Ingredient> minecraftIngredients;

    public FarmerUpgradeEmiRecipe(
            ResourceLocation gameplayId,
            ResourceLocation emiId,
            List<Ingredient> minecraftIngredients,
            ItemStack output,
            boolean waterBucketRemainder
    ) {
        super(toEmiIngredients(minecraftIngredients, waterBucketRemainder), EmiStack.of(output), emiId, false);
        if (minecraftIngredients.size() != 9) {
            throw new IllegalArgumentException("Farmer upgrade EMI recipes must contain exactly 9 crafting slots");
        }
        this.gameplayId = gameplayId;
        this.minecraftIngredients = Collections.unmodifiableList(new ArrayList<>(minecraftIngredients));
    }

    public ResourceLocation gameplayId() {
        return gameplayId;
    }

    public List<Ingredient> minecraftIngredients() {
        return minecraftIngredients;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    private static List<EmiIngredient> toEmiIngredients(List<Ingredient> ingredients, boolean waterBucketRemainder) {
        List<EmiIngredient> result = new ArrayList<>(ingredients.size());
        for (Ingredient ingredient : ingredients) {
            result.add(EmiIngredient.of(ingredient));
        }

        if (waterBucketRemainder && result.size() > 7) {
            result.set(7, EmiStack.of(Items.WATER_BUCKET).setRemainder(EmiStack.of(Items.BUCKET)));
        }
        return result;
    }
}
