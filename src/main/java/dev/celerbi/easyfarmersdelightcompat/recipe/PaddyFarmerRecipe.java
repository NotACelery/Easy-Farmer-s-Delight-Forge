package dev.celerbi.easyfarmersdelightcompat.recipe;

import dev.celerbi.easyfarmersdelightcompat.registry.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;

public final class PaddyFarmerRecipe extends ShapedRecipe {
    private static final ResourceLocation EASY_FARMER = new ResourceLocation(
            "easy_villagers",
            "farmer");

    public PaddyFarmerRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(
                id,
                "",
                category,
                3,
                3,
                ingredients(),
                new ItemStack(ModBlocks.PADDY_FARMER_ITEM.get()),
                false);
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        return RecipeUtil.upgradeFarmer(input.getItem(4), ModBlocks.PADDY_FARMER.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.PADDY_FARMER.get();
    }

    private static NonNullList<Ingredient> ingredients() {
        return NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.of(Items.GLASS_PANE),
                Ingredient.of(Items.GLASS_PANE),
                Ingredient.of(Items.GLASS_PANE),
                Ingredient.of(Items.GLASS_PANE),
                Ingredient.of(item(EASY_FARMER)),
                Ingredient.of(Items.GLASS_PANE),
                Ingredient.of(Items.IRON_INGOT),
                Ingredient.of(Items.WATER_BUCKET),
                Ingredient.of(Items.IRON_INGOT));
    }

    private static Item item(ResourceLocation id) {
        return BuiltInRegistries.ITEM.get(id);
    }
}
