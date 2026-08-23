package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * Client-only final pass that silences only the synthetic Zombie/Iron Golem
 * sounds emitted from Easy Villagers' Iron Farm block position.
 */
@EventBusSubscriber(modid = EasyFarmersDelightCompat.MOD_ID, value = Dist.CLIENT)
public final class IronFarmSoundEvents {
    private static final ResourceLocation EASY_VILLAGERS_IRON_FARM =
            new ResourceLocation("easy_villagers", "iron_farm");

    private IronFarmSoundEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPositionSound(PlayLevelSoundEvent.AtPosition event) {
        if (!event.getLevel().isClientSide) return;
        if (!ClientPreferences.ironFarmSoundsMuted()) return;
        if (event.getSource() != SoundSource.BLOCKS || event.getSound() == null) return;

        SoundEvent sound = event.getSound().value();
        if (!isIronFarmNoise(sound)) return;

        BlockPos pos = BlockPos.containing(event.getPosition());
        BlockState state = event.getLevel().getBlockState(pos);
        if (EASY_VILLAGERS_IRON_FARM.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) {
            event.setCanceled(true);
        }
    }

    private static boolean isIronFarmNoise(SoundEvent sound) {
        return sound.equals(SoundEvents.ZOMBIE_AMBIENT)
                || sound.equals(SoundEvents.IRON_GOLEM_HURT)
                || sound.equals(SoundEvents.IRON_GOLEM_DEATH);
    }
}
