package dev.celerbi.easyfarmersdelightcompat.recipe;

import dev.celerbi.easyfarmersdelightcompat.integration.CutterLogVariant;
import dev.celerbi.easyfarmersdelightcompat.registry.ModRecipeSerializers;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;

public final class CutterRecipe extends ShapedRecipe {
    public CutterRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(
                id,
                "efdc_cutter",
                category,
                3,
                3,
                ingredients(),
                CutterLogVariant.createCutter(Blocks.OAK_LOG),
                false);
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        return input.getContainerSize() < 8
                ? ItemStack.EMPTY
                : CutterLogVariant.createCutter(
                        CutterLogVariant.fromIngredient(input.getItem(7)));
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return CutterLogVariant.createCutter(Blocks.OAK_LOG);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CUTTER.get();
    }

    private static NonNullList<Ingredient> ingredients() {
        return NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.of(Items.GLASS_PANE),
                Ingredient.of(Items.GLASS_PANE),
                Ingredient.of(Items.GLASS_PANE),
                Ingredient.of(Items.GLASS_PANE),
                Ingredient.of(cuttingBoard()),
                Ingredient.of(Items.GLASS_PANE),
                Ingredient.of(Items.BRICKS),
                Ingredient.of(CutterLogVariant.ALLOWED_LOGS),
                Ingredient.of(Items.BRICKS));
    }

    private static Item cuttingBoard() {
        return BuiltInRegistries.ITEM.get(
                new ResourceLocation("farmersdelight", "cutting_board"));
    }
}
