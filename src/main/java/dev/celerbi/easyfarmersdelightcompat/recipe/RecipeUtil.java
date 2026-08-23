package dev.celerbi.easyfarmersdelightcompat.recipe;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
final class RecipeUtil {
 private static final String BLOCK_ENTITY_TAG="BlockEntityTag", DISPLAY_TAG="display";
 private RecipeUtil(){}
 static boolean isBlock(ItemStack stack,ItemLike expected){return !stack.isEmpty()&&stack.is(expected.asItem());}
 static ItemStack upgradeFarmer(ItemStack source,Block target){
  ItemStack result=new ItemStack(target);result.setCount(1);CompoundTag sourceTag=source.getTag();
  if(sourceTag!=null&&sourceTag.contains(DISPLAY_TAG,Tag.TAG_COMPOUND))result.addTagElement(DISPLAY_TAG,sourceTag.getCompound(DISPLAY_TAG).copy());
  CompoundTag data=source.getTagElement(BLOCK_ENTITY_TAG);if(hasMeaningful(data))BlockItem.setBlockEntityData(result,ModBlockEntities.COMPAT_FARMER.get(),data.copy());return result;
 }
 static boolean hasMeaningfulBlockEntityData(ItemStack stack){return hasMeaningful(stack.getTagElement(BLOCK_ENTITY_TAG));}
 private static boolean hasMeaningful(CompoundTag data){if(data==null||data.isEmpty())return false;CompoundTag c=data.copy();for(String k:new String[]{"id","x","y","z"})c.remove(k);return !c.isEmpty();}
}
