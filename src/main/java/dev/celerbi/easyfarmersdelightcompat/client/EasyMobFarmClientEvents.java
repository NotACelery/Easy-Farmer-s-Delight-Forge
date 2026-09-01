package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EasyFarmersDelightCompat.MOD_ID, value = Dist.CLIENT)
public final class EasyMobFarmClientEvents {
    private EasyMobFarmClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            EasyMobFarmSoundController.tick();
        }
    }
}
