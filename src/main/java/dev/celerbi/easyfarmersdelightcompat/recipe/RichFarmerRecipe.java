package dev.celerbi.easyfarmersdelightcompat.recipe;

import dev.celerbi.easyfarmersdelightcompat.registry.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;

public final class RichFarmerRecipe extends ShapedRecipe {
    private static final ResourceLocation EASY_FARMER = new ResourceLocation(
            "easy_villagers",
            "farmer");
    private static final ResourceLocation RICH_SOIL = new ResourceLocation(
            "farmersdelight",
            "rich_soil");

    public RichFarmerRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(
                id,
                "",
                category,
                3,
                3,
                FarmerUpgradeRecipeDefinitions.rich().ingredients(),
                new ItemStack(ModBlocks.RICH_FARMER_ITEM.get()),
                false);
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        return RecipeUtil.upgradeFarmer(input.getItem(4), ModBlocks.RICH_FARMER.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.RICH_FARMER.get();
    }

}
