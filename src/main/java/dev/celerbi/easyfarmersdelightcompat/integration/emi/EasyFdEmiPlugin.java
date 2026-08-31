package dev.celerbi.easyfarmersdelightcompat.integration.emi;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.integration.RecipeViewerData;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

@EmiEntrypoint
public final class EasyFdEmiPlugin implements EmiPlugin {

    public static final EmiRecipeCategory FARMER_HARVEST = new EmiRecipeCategory(
            new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, "farmer_harvest"),
            EmiStack.of(ModBlocks.RICH_FARMER_ITEM.get())
    );
    public static final EmiRecipeCategory PADDY_HARVEST = new EmiRecipeCategory(
            new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, "paddy_harvest"),
            EmiStack.of(ModBlocks.PADDY_FARMER_ITEM.get())
    );
    public static final EmiRecipeCategory RICH_FARMER_HARVEST = new EmiRecipeCategory(
            new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, "rich_farmer_harvest"),
            EmiStack.of(ModBlocks.RICH_FARMER_ITEM.get())
    );
    public static final EmiRecipeCategory CUTTER_AXE = new EmiRecipeCategory(
            new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, "cutter_axe"),
            EmiStack.of(ModBlocks.CUTTER_ITEM.get())
    );
    public static final EmiRecipeCategory BLOCK_GUIDE = new EmiRecipeCategory(
            new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, "block_guide"),
            EmiStack.of(ModBlocks.RICH_FARMER_ITEM.get())
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(FARMER_HARVEST);
        registry.addCategory(PADDY_HARVEST);
        registry.addCategory(RICH_FARMER_HARVEST);
        registry.addCategory(CUTTER_AXE);
        registry.addCategory(BLOCK_GUIDE);

        registry.addWorkstation(FARMER_HARVEST, EmiStack.of(ModBlocks.RICH_FARMER_ITEM.get()));
        registry.addWorkstation(FARMER_HARVEST, EmiStack.of(ModBlocks.RICH_PADDY_FARMER_ITEM.get()));

        registry.addWorkstation(PADDY_HARVEST, EmiStack.of(ModBlocks.PADDY_FARMER_ITEM.get()));
        registry.addWorkstation(PADDY_HARVEST, EmiStack.of(ModBlocks.RICH_PADDY_FARMER_ITEM.get()));
        registry.addWorkstation(RICH_FARMER_HARVEST, EmiStack.of(ModBlocks.RICH_FARMER_ITEM.get()));

        registry.addWorkstation(CUTTER_AXE, EmiStack.of(ModBlocks.CUTTER_ITEM.get()));
        if (ModBlocks.EASY_MOB_FARM_NOISE_SWITCH_ITEM != null) {
            registry.addWorkstation(
                    BLOCK_GUIDE,
                    EmiStack.of(ModBlocks.EASY_MOB_FARM_NOISE_SWITCH_ITEM.get())
            );
        }

        registerFarmerUpgradeTransfers(registry);
        registerCutting(registry);

        for (var info : RecipeViewerData.FARMER_TOOL_GUIDES) {
            registry.addRecipe(new FarmerHarvestEmiRecipe(info, FARMER_HARVEST));
        }
        for (var info : RecipeViewerData.PADDY_HARVESTS) {
            registry.addRecipe(new FarmerHarvestEmiRecipe(info, PADDY_HARVEST));
        }
        for (var info : RecipeViewerData.RICH_FARMER_HARVESTS) {
            registry.addRecipe(new FarmerHarvestEmiRecipe(info, RICH_FARMER_HARVEST));
        }
        for (var info : RecipeViewerData.cutterAxeActions()) {
            registry.addRecipe(new CutterAxeEmiRecipe(info));
        }
        for (var info : RecipeViewerData.blockGuides()) {
            registry.addRecipe(new BlockGuideEmiRecipe(info));
        }
    }

    private static void registerFarmerUpgradeTransfers(EmiRegistry registry) {
        ResourceLocation paddyId = modId("paddy_farmer");
        ResourceLocation richId = modId("rich_farmer");
        ResourceLocation richPaddyId = modId("rich_paddy_farmer");

        registry.removeRecipes(paddyId);
        registry.removeRecipes(richId);
        registry.removeRecipes(richPaddyId);

        Item easyFarmer = item("easy_villagers", "farmer");
        Item richSoil = item("farmersdelight", "rich_soil");

        registry.addRecipe(new FarmerUpgradeEmiRecipe(
                paddyId,
                syntheticId("paddy_farmer"),
                List.of(
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(easyFarmer),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.IRON_INGOT),
                        Ingredient.of(Items.WATER_BUCKET),
                        Ingredient.of(Items.IRON_INGOT)
                ),
                new ItemStack(ModBlocks.PADDY_FARMER_ITEM.get()),
                true
        ));

        registry.addRecipe(new FarmerUpgradeEmiRecipe(
                richId,
                syntheticId("rich_farmer"),
                List.of(
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(easyFarmer),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.IRON_BLOCK),
                        Ingredient.of(richSoil),
                        Ingredient.of(Items.IRON_BLOCK)
                ),
                new ItemStack(ModBlocks.RICH_FARMER_ITEM.get()),
                false
        ));

        registry.addRecipe(new FarmerUpgradeEmiRecipe(
                richPaddyId,
                syntheticId("rich_paddy_farmer"),
                List.of(
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(ModBlocks.PADDY_FARMER_ITEM.get()),
                        Ingredient.of(Items.GLASS_PANE),
                        Ingredient.of(Items.IRON_BLOCK),
                        Ingredient.of(richSoil),
                        Ingredient.of(Items.IRON_BLOCK)
                ),
                new ItemStack(ModBlocks.RICH_PADDY_FARMER_ITEM.get()),
                false
        ));

        registry.addRecipeHandler(MenuType.CRAFTING, new FarmerUpgradeEmiRecipeHandler());
    }

    private static ResourceLocation modId(String path) {
        return new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, path);
    }

    private static ResourceLocation syntheticId(String path) {
        return new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, "/emi/farmer_upgrade/" + path);
    }

    private static Item item(String namespace, String path) {
        return BuiltInRegistries.ITEM.get(new ResourceLocation(namespace, path));
    }

    private static void registerCutting(EmiRegistry registry) {
        Class<?> categories = load("vectorwing.farmersdelight.integration.emi.FDRecipeCategories");
        if (categories == null)
            return;

        try {
            var field = categories.getDeclaredField("CUTTING");
            field.trySetAccessible();
            Object value = field.get(null);
            if (value instanceof EmiRecipeCategory category) {
                registry.addWorkstation(category, EmiStack.of(ModBlocks.CUTTER_ITEM.get()));
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
    }

    private static Class<?> load(String name) {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if (context != null) {
            try {
                return Class.forName(name, true, context);
            } catch (ClassNotFoundException | LinkageError ignored) {
            }
        }
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }
}
