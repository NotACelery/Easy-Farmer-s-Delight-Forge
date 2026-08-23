package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.PlayLevelSoundEvent;

/** Client-only final pass for Villager audio, after Easy Villagers applies its own volume. */
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
        // PlayLevelSoundEvent is also fired on the logical server before sounds are
        // broadcast. A physical-client-only subscriber is not enough in singleplayer:
        // the integrated server lives in the same process. Never let a local mute
        // preference cancel or re-route the server event for other players.
        if (!event.getLevel().isClientSide) return;
        if (event.getSound() == null || !isVillagerSound(event.getSound().value())) return;

        if (ClientPreferences.villagersMuted()) {
            event.setCanceled(true);
            return;
        }

        // Easy Villagers emits its contained-villager voices as BLOCKS. Re-route
        // those voices to Minecraft's Friendly Creatures slider while leaving the
        // volume Easy Villagers already calculated untouched.
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
