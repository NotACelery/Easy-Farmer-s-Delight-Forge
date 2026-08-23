package dev.celerbi.easyfarmersdelightcompat.recipe;
import dev.celerbi.easyfarmersdelightcompat.registry.*;
import net.minecraft.core.*;import net.minecraft.core.registries.BuiltInRegistries;import net.minecraft.resources.ResourceLocation;import net.minecraft.world.inventory.CraftingContainer;import net.minecraft.world.item.*;import net.minecraft.world.item.crafting.*;
public final class RichPaddyFarmerRecipe extends ShapedRecipe {
 
 private static final ResourceLocation RICH_SOIL = new ResourceLocation("farmersdelight", "rich_soil");
 public RichPaddyFarmerRecipe(ResourceLocation id,CraftingBookCategory category){super(id,"",category,3,3,ingredients(),new ItemStack(ModBlocks.RICH_PADDY_FARMER_ITEM.get()),false);}
 @Override public ItemStack assemble(CraftingContainer input,RegistryAccess registries){return RecipeUtil.upgradeFarmer(input.getItem(4),ModBlocks.RICH_PADDY_FARMER.get());}
 @Override public RecipeSerializer<?> getSerializer(){return ModRecipeSerializers.RICH_PADDY_FARMER.get();}
 private static NonNullList<Ingredient> ingredients(){return NonNullList.of(Ingredient.EMPTY,
 Ingredient.of(Items.GLASS_PANE),Ingredient.of(Items.GLASS_PANE),Ingredient.of(Items.GLASS_PANE),
 Ingredient.of(Items.GLASS_PANE),Ingredient.of(ModBlocks.PADDY_FARMER_ITEM.get()),Ingredient.of(Items.GLASS_PANE),
 Ingredient.of(Items.IRON_BLOCK), Ingredient.of(item(RICH_SOIL)), Ingredient.of(Items.IRON_BLOCK));}
 private static Item item(ResourceLocation id){return BuiltInRegistries.ITEM.get(id);}
}
