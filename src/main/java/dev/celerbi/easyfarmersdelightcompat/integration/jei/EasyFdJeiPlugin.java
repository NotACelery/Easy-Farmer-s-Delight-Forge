package dev.celerbi.easyfarmersdelightcompat.integration.jei;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.integration.BlockGuideInfo;
import dev.celerbi.easyfarmersdelightcompat.integration.CutterAxeInfo;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmerHarvestInfo;
import dev.celerbi.easyfarmersdelightcompat.integration.RecipeViewerData;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import vectorwing.farmersdelight.integration.jei.FDRecipeTypes;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public final class EasyFdJeiPlugin implements IModPlugin {

    public static final RecipeType<FarmerHarvestInfo> FARMER_HARVEST =
            RecipeType.create(EasyFarmersDelightCompat.MOD_ID, "farmer_harvest", FarmerHarvestInfo.class);
    public static final RecipeType<FarmerHarvestInfo> PADDY_HARVEST =
            RecipeType.create(EasyFarmersDelightCompat.MOD_ID, "paddy_harvest", FarmerHarvestInfo.class);
    public static final RecipeType<FarmerHarvestInfo> RICH_FARMER_HARVEST =
            RecipeType.create(EasyFarmersDelightCompat.MOD_ID, "rich_farmer_harvest", FarmerHarvestInfo.class);
    public static final RecipeType<CutterAxeInfo> CUTTER_AXE =
            RecipeType.create(EasyFarmersDelightCompat.MOD_ID, "cutter_axe", CutterAxeInfo.class);
    public static final RecipeType<BlockGuideInfo> BLOCK_GUIDE =
            RecipeType.create(EasyFarmersDelightCompat.MOD_ID, "block_guide", BlockGuideInfo.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new FarmerHarvestJeiCategory(
                        gui,
                        FARMER_HARVEST,
                        "jei.easyfarmersdelightcompat.farmer_harvest",
                        new ItemStack(ModBlocks.RICH_FARMER_ITEM.get())
                ),
                new FarmerHarvestJeiCategory(
                        gui,
                        PADDY_HARVEST,
                        "jei.easyfarmersdelightcompat.paddy_harvest",
                        new ItemStack(ModBlocks.PADDY_FARMER_ITEM.get())
                ),
                new FarmerHarvestJeiCategory(
                        gui,
                        RICH_FARMER_HARVEST,
                        "jei.easyfarmersdelightcompat.rich_farmer_harvest",
                        new ItemStack(ModBlocks.RICH_FARMER_ITEM.get())
                ),
                new CutterAxeJeiCategory(gui),
                new BlockGuideJeiCategory(gui)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(FARMER_HARVEST, RecipeViewerData.FARMER_TOOL_GUIDES);
        registration.addRecipes(PADDY_HARVEST, RecipeViewerData.PADDY_HARVESTS);
        registration.addRecipes(RICH_FARMER_HARVEST, RecipeViewerData.RICH_FARMER_HARVESTS);
        registration.addRecipes(CUTTER_AXE, RecipeViewerData.cutterAxeActions());
        registration.addRecipes(BLOCK_GUIDE, RecipeViewerData.BLOCK_GUIDES);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RICH_FARMER_ITEM.get()), FARMER_HARVEST);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RICH_PADDY_FARMER_ITEM.get()), FARMER_HARVEST);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PADDY_FARMER_ITEM.get()), PADDY_HARVEST);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RICH_PADDY_FARMER_ITEM.get()), PADDY_HARVEST);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RICH_FARMER_ITEM.get()), RICH_FARMER_HARVEST);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CUTTER_ITEM.get()), CUTTER_AXE);
        registerCutting(registration);

        registration.addRecipeCatalyst(new ItemStack(ModBlocks.PADDY_FARMER_ITEM.get()), BLOCK_GUIDE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RICH_FARMER_ITEM.get()), BLOCK_GUIDE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.RICH_PADDY_FARMER_ITEM.get()), BLOCK_GUIDE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.CUTTER_ITEM.get()), BLOCK_GUIDE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.VILLAGER_NOISE_SWITCH_ITEM.get()), BLOCK_GUIDE);
    }
    private static void registerCutting(IRecipeCatalystRegistration registration) {
            registration.addRecipeCatalyst(
                    new ItemStack(ModBlocks.CUTTER_ITEM.get()),
                FDRecipeTypes.CUTTING
            );
        }
    }
