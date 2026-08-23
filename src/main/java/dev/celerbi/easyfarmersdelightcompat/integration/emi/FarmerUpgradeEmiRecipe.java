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

/**
 * EMI-only representation of the three stateful Farmer upgrade recipes.
 *
 * <p>The gameplay recipes remain the real recipes. This wrapper exists only so
 * EMI does not route those recipes through its generic crafting filler, which
 * compares concrete ItemStack components while moving ingredients. Easy
 * Villagers Farmer items may legitimately carry BLOCK_ENTITY_DATA and other
 * components, so the generic filler can recognize the Ingredient but then fail
 * to move the actual Farmer stack.</p>
 */
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

    /**
     * Prevent EMI's vanilla crafting handler from claiming this recipe. The
     * dedicated FarmerUpgradeEmiRecipeHandler still provides the Fill button.
     */
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
