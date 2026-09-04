package dev.celerbi.easyfarmersdelightcompat.event;

import dev.celerbi.easyfarmersdelightcompat.block.GraftingCanopyBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.GraftingSupportBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class GraftingSupportEvents {
    private GraftingSupportEvents() {}

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide
                || event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START
                || !event.getLevel().getBlockState(event.getPos()).is(ModBlocks.GRAFTING_SUPPORT.get())) return;
        BlockPos supportPos = event.getPos();
        if (!(event.getLevel().getBlockEntity(supportPos) instanceof GraftingSupportBlockEntity support) || !support.hasCanopy()) return;
        ItemStack canopy = support.removeCanopy();
        ItemStack tool = event.getEntity().getMainHandItem();
        if (!event.getEntity().getAbilities().instabuild && !canopy.isEmpty() && GraftingCanopyBlock.canRecoverCanopy(tool)) {
            Block.popResource(event.getLevel(), supportPos.above(), canopy);
        }
        event.getLevel().playSound(null, supportPos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
        event.setCanceled(true);
    }
}
