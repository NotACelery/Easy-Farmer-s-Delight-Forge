package dev.celerbi.easyfarmersdelightcompat.menu;

import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmerToolSupport;
import dev.celerbi.easyfarmersdelightcompat.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public final class CutterMenu extends AbstractContainerMenu {
    public static final int TOOL_SLOT=0,INPUT_START=1,INPUT_END=5,OUTPUT_START=5,OUTPUT_END=9,PLAYER_START=9,PLAYER_END=45;
    private final BlockPos blockPos; private final ContainerData data;
    private CutterMenu(int id,Inventory inv,BlockPos pos,CutterBlockEntity cutter){this(id,inv,pos,cutter!=null?cutter.toolHandler():new ItemStackHandler(1),cutter!=null?cutter.inputHandler():new ItemStackHandler(4),cutter!=null?cutter.outputHandler():new ItemStackHandler(4),cutter!=null?serverData(cutter):new SimpleContainerData(1));}
    public CutterMenu(int id,Inventory inv,CutterBlockEntity cutter){this(id,inv,cutter.getBlockPos(),cutter);}
    private CutterMenu(int id,Inventory inv,BlockPos pos,ItemStackHandler tool,ItemStackHandler input,ItemStackHandler output,ContainerData data){
        super(ModMenus.CUTTER.get(),id);blockPos=pos;this.data=data;addDataSlots(data);
        addSlot(new SlotItemHandler(tool,0,142,20){@Override public boolean mayPlace(ItemStack s){return FarmerToolSupport.isCuttingTool(s);}@Override public int getMaxStackSize(){return 1;}});
        for(int i=0;i<4;i++)addSlot(new SlotItemHandler(input,i,52+i*18,20));
        for(int i=0;i<4;i++)addSlot(new SlotItemHandler(output,i,52+i*18,51){@Override public boolean mayPlace(ItemStack s){return false;}});
        for(int r=0;r<3;r++)for(int c=0;c<9;c++)addSlot(new Slot(inv,c+r*9+9,8+c*18,83+r*18));
        for(int c=0;c<9;c++)addSlot(new Slot(inv,c,8+c*18,141));
    }
    public static CutterMenu fromNetwork(int id,Inventory inv,FriendlyByteBuf buf){BlockPos pos=buf.readBlockPos();BlockEntity be=inv.player.level().getBlockEntity(pos);return new CutterMenu(id,inv,pos,be instanceof CutterBlockEntity c?c:null);}
    private static ContainerData serverData(CutterBlockEntity c){return new ContainerData(){public int get(int i){return i==0?c.progress():0;}public void set(int i,int v){}public int getCount(){return 1;}};}
    public int progress(){return data.get(0);}
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack stack=slot.getItem(),original=stack.copy();
        if(index<PLAYER_START){if(!moveItemStackTo(stack,PLAYER_START,PLAYER_END,true))return ItemStack.EMPTY;}
        else if(FarmerToolSupport.isCuttingTool(stack)){if(slots.get(TOOL_SLOT).hasItem()||!moveItemStackTo(stack,TOOL_SLOT,TOOL_SLOT+1,false))return ItemStack.EMPTY;}
        else if(!moveItemStackTo(stack,INPUT_START,INPUT_END,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return original;
    }
    @Override public boolean stillValid(Player player){return player.distanceToSqr(blockPos.getX()+.5,blockPos.getY()+.5,blockPos.getZ()+.5)<=64&&player.level().getBlockEntity(blockPos) instanceof CutterBlockEntity;}
}
