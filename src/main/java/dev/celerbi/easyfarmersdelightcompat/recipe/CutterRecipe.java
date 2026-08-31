package dev.celerbi.easyfarmersdelightcompat.recipe;

import dev.celerbi.easyfarmersdelightcompat.integration.CutterLogVariant;
import dev.celerbi.easyfarmersdelightcompat.registry.ModRecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
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
    public boolean matches(CraftingContainer input, Level level) {
        return input.getContainerSize() >= 9
                && input.getItem(0).is(Items.GLASS_PANE)
                && input.getItem(1).is(Items.GLASS_PANE)
                && input.getItem(2).is(Items.GLASS_PANE)
                && input.getItem(3).is(Items.GLASS_PANE)
                && input.getItem(4).is(cuttingBoard())
                && input.getItem(5).is(Items.GLASS_PANE)
                && input.getItem(6).is(Items.BRICKS)
                && CutterLogVariant.isAllowed(input.getItem(7))
                && input.getItem(8).is(Items.BRICKS);
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        return input.getContainerSize() < 8
                ? ItemStack.EMPTY
                : CutterLogVariant.createCutter(CutterLogVariant.fromIngredient(input.getItem(7)));
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
                Ingredient.of(ItemTags.LOGS),
                Ingredient.of(Items.BRICKS));
    }

    private static Item cuttingBoard() {
        return BuiltInRegistries.ITEM.get(new ResourceLocation("farmersdelight", "cutting_board"));
    }
}
