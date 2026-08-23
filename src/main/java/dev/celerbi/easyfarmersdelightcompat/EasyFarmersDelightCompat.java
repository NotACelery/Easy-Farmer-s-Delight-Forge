package dev.celerbi.easyfarmersdelightcompat;

import dev.celerbi.easyfarmersdelightcompat.event.LegacyFarmerMigrationEvents;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import dev.celerbi.easyfarmersdelightcompat.registry.ModMenus;
import dev.celerbi.easyfarmersdelightcompat.registry.ModRecipeSerializers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(EasyFarmersDelightCompat.MOD_ID)
public final class EasyFarmersDelightCompat {
    public static final String MOD_ID = "easyfarmersdelightcompat";
    public EasyFarmersDelightCompat() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);
        MinecraftForge.EVENT_BUS.addListener(LegacyFarmerMigrationEvents::onPlayerLoggedIn);
    }
}
