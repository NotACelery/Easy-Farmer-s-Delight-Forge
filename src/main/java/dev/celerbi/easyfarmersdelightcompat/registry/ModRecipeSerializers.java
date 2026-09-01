package dev.celerbi.easyfarmersdelightcompat.registry;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.recipe.CutterRecipe;
import dev.celerbi.easyfarmersdelightcompat.recipe.PaddyFarmerRecipe;
import dev.celerbi.easyfarmersdelightcompat.recipe.RichFarmerRecipe;
import dev.celerbi.easyfarmersdelightcompat.recipe.RichPaddyFarmerRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.RECIPE_SERIALIZERS,
            EasyFarmersDelightCompat.MOD_ID
    );

    public static final RegistryObject<RecipeSerializer<PaddyFarmerRecipe>> PADDY_FARMER = SERIALIZERS.register(
            "paddy_farmer",
            () -> new SimpleCraftingRecipeSerializer<>(PaddyFarmerRecipe::new)
    );

    public static final RegistryObject<RecipeSerializer<RichFarmerRecipe>> RICH_FARMER = SERIALIZERS.register(
            "rich_farmer",
            () -> new SimpleCraftingRecipeSerializer<>(RichFarmerRecipe::new)
    );

    public static final RegistryObject<RecipeSerializer<RichPaddyFarmerRecipe>> RICH_PADDY_FARMER =
            SERIALIZERS.register(
                    "rich_paddy_farmer",
                    () -> new SimpleCraftingRecipeSerializer<>(RichPaddyFarmerRecipe::new)
            );

    public static final RegistryObject<RecipeSerializer<CutterRecipe>> CUTTER = SERIALIZERS.register(
            "cutter",
            () -> new SimpleCraftingRecipeSerializer<>(CutterRecipe::new)
    );

    private ModRecipeSerializers() {
    }

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
