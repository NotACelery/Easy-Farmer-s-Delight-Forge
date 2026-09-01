package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = EasyFarmersDelightCompat.MOD_ID, value = Dist.CLIENT)
public final class VillagerSoundEvents {
    private VillagerSoundEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntitySound(PlayLevelSoundEvent.AtEntity event) {
        handle(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPositionSound(PlayLevelSoundEvent.AtPosition event) {
        handle(event);
    }

    private static void handle(PlayLevelSoundEvent event) {

        if (!event.getLevel().isClientSide)
            return;
        if (event.getSound() == null || !isVillagerSound(event.getSound().value()))
            return;

        if (ClientPreferences.villagersMuted()) {
            event.setCanceled(true);
            return;
        }

        if (event.getSource() == SoundSource.BLOCKS) {
            event.setSource(SoundSource.NEUTRAL);
        }
    }

    private static boolean isVillagerSound(SoundEvent event) {
        return event.equals(SoundEvents.VILLAGER_NO)
                 || event.equals(SoundEvents.VILLAGER_CELEBRATE)
                 || event.equals(SoundEvents.VILLAGER_DEATH)
                 || event.equals(SoundEvents.VILLAGER_AMBIENT)
                 || event.equals(SoundEvents.VILLAGER_HURT)
                 || event.equals(SoundEvents.VILLAGER_TRADE)
                 || event.equals(SoundEvents.VILLAGER_WORK_ARMORER)
                 || event.equals(SoundEvents.VILLAGER_WORK_BUTCHER)
                 || event.equals(SoundEvents.VILLAGER_WORK_CARTOGRAPHER)
                 || event.equals(SoundEvents.VILLAGER_WORK_CLERIC)
                 || event.equals(SoundEvents.VILLAGER_WORK_FARMER)
                 || event.equals(SoundEvents.VILLAGER_WORK_FISHERMAN)
                 || event.equals(SoundEvents.VILLAGER_WORK_FLETCHER)
                 || event.equals(SoundEvents.VILLAGER_WORK_LEATHERWORKER)
                 || event.equals(SoundEvents.VILLAGER_WORK_LIBRARIAN)
                 || event.equals(SoundEvents.VILLAGER_WORK_MASON)
                 || event.equals(SoundEvents.VILLAGER_WORK_SHEPHERD)
                 || event.equals(SoundEvents.VILLAGER_WORK_TOOLSMITH)
                 || event.equals(SoundEvents.VILLAGER_WORK_WEAPONSMITH)
                 || event.equals(SoundEvents.VILLAGER_YES);
    }
}
