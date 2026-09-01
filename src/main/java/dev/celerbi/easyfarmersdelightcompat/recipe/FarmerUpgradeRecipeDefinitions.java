package dev.celerbi.easyfarmersdelightcompat.recipe;

import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public final class FarmerUpgradeRecipeDefinitions {
    private static final ResourceLocation EASY_FARMER = new ResourceLocation("easy_villagers", "farmer");
    private static final ResourceLocation RICH_SOIL = new ResourceLocation("farmersdelight", "rich_soil");

    public record Definition(NonNullList<Ingredient> ingredients, boolean waterBucketRemainder) {
    }

    private FarmerUpgradeRecipeDefinitions() {
    }

    public static Definition paddy() {
        Ingredient glass = Ingredient.of(Items.GLASS_PANE);
        Ingredient farmer = Ingredient.of(item(EASY_FARMER));
        Ingredient iron = Ingredient.of(Items.IRON_INGOT);
        Ingredient water = Ingredient.of(Items.WATER_BUCKET);
        return new Definition(ingredients(glass, glass, glass, glass, farmer, glass, iron, water, iron), true);
    }

    public static Definition rich() {
        Ingredient glass = Ingredient.of(Items.GLASS_PANE);
        Ingredient farmer = Ingredient.of(item(EASY_FARMER));
        Ingredient ironBlock = Ingredient.of(Items.IRON_BLOCK);
        Ingredient richSoil = Ingredient.of(item(RICH_SOIL));
        return new Definition(ingredients(glass, glass, glass, glass, farmer, glass, ironBlock, richSoil, ironBlock),
                false);
    }

    public static Definition richPaddy() {
        Ingredient glass = Ingredient.of(Items.GLASS_PANE);
        Ingredient farmer = Ingredient.of(ModBlocks.PADDY_FARMER_ITEM.get());
        Ingredient ironBlock = Ingredient.of(Items.IRON_BLOCK);
        Ingredient richSoil = Ingredient.of(item(RICH_SOIL));
        return new Definition(ingredients(glass, glass, glass, glass, farmer, glass, ironBlock, richSoil, ironBlock),
                false);
    }

    private static NonNullList<Ingredient> ingredients(Ingredient... values) {
        return NonNullList.of(Ingredient.EMPTY, values);
    }

    private static Item item(ResourceLocation id) {
        return BuiltInRegistries.ITEM.get(id);
    }
}
