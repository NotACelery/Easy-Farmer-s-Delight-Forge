package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.menu.PaddyFarmerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class PaddyFarmerScreen extends AbstractContainerScreen<PaddyFarmerMenu> {
    private static final ResourceLocation BACKGROUND=new ResourceLocation("easy_villagers","textures/gui/container/output.png");
    public PaddyFarmerScreen(PaddyFarmerMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=176;imageHeight=133;inventoryLabelY=40;titleLabelY=9;}
    @Override public void render(GuiGraphics g,int mx,int my,float pt){super.render(g,mx,my,pt);renderTooltip(g,mx,my);}
    @Override protected void renderBg(GuiGraphics g,float pt,int mx,int my){g.blit(BACKGROUND,leftPos,topPos,0,0,imageWidth,imageHeight);}
}
