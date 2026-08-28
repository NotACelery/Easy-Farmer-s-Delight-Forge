package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import dev.celerbi.easyfarmersdelightcompat.registry.ModMenus;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = EasyFarmersDelightCompat.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {
    private static final int DEFAULT_WATER_COLOR = 0x3F76E4;

    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.COMPAT_FARMER.get(), CompatFarmerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CUTTER.get(), CutterBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VILLAGER_NOISE_SWITCH.get(),
                VillagerNoiseSwitchBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.IRON_FARM_NOISE_SWITCH.get(),
                IronFarmNoiseSwitchBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.RICH_FARMER.get(), RichFarmerScreen::new);
            MenuScreens.register(ModMenus.PADDY_FARMER.get(), PaddyFarmerScreen::new);
            MenuScreens.register(ModMenus.CUTTER.get(), CutterScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> tintIndex == 0 ? waterColor(level, pos) : -1,
                ModBlocks.PADDY_FARMER.get(),
                ModBlocks.RICH_PADDY_FARMER.get()
        );
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? DEFAULT_WATER_COLOR : -1,
                ModBlocks.PADDY_FARMER.get(),
                ModBlocks.RICH_PADDY_FARMER.get()
        );
    }

    private static int waterColor(BlockAndTintGetter level, BlockPos pos) {
        if (level == null || pos == null) {
            return DEFAULT_WATER_COLOR;
        }
        return BiomeColors.getAverageWaterColor(level, pos);
    }
}
