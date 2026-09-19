package dev.celerbi.easyfarmersdelightcompat;

import com.mojang.logging.LogUtils;
import dev.celerbi.easyfarmersdelightcompat.command.FarmCommand;
import dev.celerbi.easyfarmersdelightcompat.event.GraftingSupportEvents;
import dev.celerbi.easyfarmersdelightcompat.event.LegacyFarmerMigrationEvents;
import dev.celerbi.easyfarmersdelightcompat.integration.attached.AttachedCropReloadListener;
import dev.celerbi.easyfarmersdelightcompat.integration.orchard.OrchardCropReloadListener;
import dev.celerbi.easyfarmersdelightcompat.integration.regrowing.RegrowingCropReloadListener;
import dev.celerbi.easyfarmersdelightcompat.integration.stem.StemCropReloadListener;
import dev.celerbi.easyfarmersdelightcompat.integration.tall.TallCropReloadListener;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import dev.celerbi.easyfarmersdelightcompat.registry.ModMenus;
import dev.celerbi.easyfarmersdelightcompat.registry.ModRecipeSerializers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(EasyFarmersDelightCompat.MOD_ID)
public final class EasyFarmersDelightCompat {
    public static final String MOD_ID = "easyfarmersdelightcompat";
    public static final Logger LOGGER = LogUtils.getLogger();

    public EasyFarmersDelightCompat() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(LegacyFarmerMigrationEvents::onPlayerLoggedIn);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, GraftingSupportEvents::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(GraftingSupportEvents::onLeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(GraftingSupportEvents::onCanopyBreak);
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                FarmCommand.register(event.getDispatcher()));
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) ->
                event.addListener(AttachedCropReloadListener.INSTANCE));
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) ->
                event.addListener(RegrowingCropReloadListener.INSTANCE));
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) ->
                event.addListener(StemCropReloadListener.INSTANCE));
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) ->
                event.addListener(TallCropReloadListener.INSTANCE));
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) ->
                event.addListener(OrchardCropReloadListener.INSTANCE));
    }
}
