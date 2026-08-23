package dev.celerbi.easyfarmersdelightcompat.integration;

import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Narrow bridge reusing Easy Villagers' exact VillagerItem serialization/aging semantics. */
public final class CutterVillagerAdapter {
    private static final ResourceLocation EASY_FARMER_ID = new ResourceLocation("easy_villagers", "farmer");
    private static final String FARMER_TILEENTITY = "de.maxhenkel.easyvillagers.blocks.tileentity.FarmerTileentity";
    private static final String VILLAGER_ITEM = "de.maxhenkel.easyvillagers.items.VillagerItem";
    private final CutterBlockEntity owner;
    private BlockEntity delegate;
    private boolean failed;
    public CutterVillagerAdapter(CutterBlockEntity owner) { this.owner=owner; }
    public void reset(){delegate=null;failed=false;}
    public boolean isVillagerItem(ItemStack stack){
        if(stack==null||stack.isEmpty())return false;
        try{return Class.forName(VILLAGER_ITEM).isInstance(stack.getItem());}catch(ClassNotFoundException|LinkageError e){return false;}
    }
    public Villager getVillagerEntity(){
        BlockEntity farmer=getDelegate(); if(farmer==null)return null;
        try{Object r=farmer.getClass().getMethod("getVillagerEntity").invoke(farmer);return r instanceof Villager v?v:null;}catch(ReflectiveOperationException e){fail();return null;}
    }
    public boolean hasAdultVillager(){Villager v=getVillagerEntity();return v!=null&&!v.isBaby();}
    public void advanceAge(){BlockEntity f=getDelegate();if(f==null)return;try{f.getClass().getMethod("advanceAge").invoke(f);}catch(ReflectiveOperationException e){fail();}}
    public void flushToOwner(){BlockEntity f=getDelegate();if(f==null)return;try{Object v=f.getClass().getMethod("getVillager").invoke(f);if(v instanceof ItemStack s&&!s.isEmpty())owner.updateVillagerFromAdapter(s.copyWithCount(1));}catch(ReflectiveOperationException e){fail();}}
    private BlockEntity getDelegate(){
        if(failed||owner.getStoredVillager().isEmpty())return null; Level level=owner.getLevel();
        if(delegate!=null){if(level!=null&&delegate.getLevel()!=level)delegate.setLevel(level);return delegate;}
        try{
            Block easyFarmer=BuiltInRegistries.BLOCK.get(EASY_FARMER_ID); Class<?> clazz=Class.forName(FARMER_TILEENTITY);
            Constructor<?> ctor=clazz.getConstructor(net.minecraft.core.BlockPos.class, BlockState.class);
            delegate=(BlockEntity)ctor.newInstance(owner.getBlockPos(),easyFarmer.defaultBlockState()); if(level!=null)delegate.setLevel(level);
            findField(clazz,"villager").set(delegate,owner.getStoredVillager().copyWithCount(1)); findField(clazz,"villagerEntity").set(delegate,null); return delegate;
        }catch(ReflectiveOperationException|RuntimeException|LinkageError e){fail();return null;}
    }
    private static Field findField(Class<?> type,String name)throws NoSuchFieldException{for(Class<?> c=type;c!=null;c=c.getSuperclass())try{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}catch(NoSuchFieldException ignored){}throw new NoSuchFieldException(name);}
    private void fail(){failed=true;delegate=null;}
}
