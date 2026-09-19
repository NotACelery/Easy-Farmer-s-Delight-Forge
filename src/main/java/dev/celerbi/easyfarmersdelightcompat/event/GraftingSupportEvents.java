package dev.celerbi.easyfarmersdelightcompat.event;

import dev.celerbi.easyfarmersdelightcompat.block.GraftingCanopyBlock;
import dev.celerbi.easyfarmersdelightcompat.block.GraftingSupportBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.GraftingSupportBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;

public final class GraftingSupportEvents {
    private GraftingSupportEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        BlockPos supportPos = GraftingSupportBlock.resolveSupportPos(event.getLevel(), event.getPos());
        if (supportPos == null || !GraftingSupportBlock.isLeavesItem(event.getItemStack())) {
            return;
        }
        if (!(event.getLevel().getBlockEntity(supportPos) instanceof GraftingSupportBlockEntity support)) {
            return;
        }

        // Claim leaf use before vanilla BlockItem placement can predict an adjacent leaf.
        if (!support.hasCanopy()) {
            if (event.getLevel().isClientSide) {
                support.previewCanopy(event.getItemStack());
            } else {
                GraftingSupportBlock.interactAt(
                        event.getLevel(), supportPos, event.getEntity(), event.getHand(), event.getItemStack());
            }
        }
        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START
                || !event.getLevel().getBlockState(event.getPos()).is(ModBlocks.GRAFTING_SUPPORT.get())) {
            return;
        }

        if (event.getLevel().getBlockEntity(event.getPos()) instanceof GraftingSupportBlockEntity support
                && support.hasCanopy()) {
            event.setCanceled(true);
        }
    }

    public static void onCanopyBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()
                || !event.getState().is(ModBlocks.GRAFTING_CANOPY.get())
                || !(event.getLevel() instanceof Level level)
                || level.isClientSide) {
            return;
        }
        if (GraftingCanopyBlock.removeCanopyForPlayer(level, event.getPos(), event.getPlayer())) {
            event.setCanceled(true);
        }
    }
}
