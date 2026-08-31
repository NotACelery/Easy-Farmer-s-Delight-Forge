package dev.celerbi.easyfarmersdelightcompat.integration;

import java.util.Optional;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.HoneycombItem;

public final class AxeActionResolver {
    public enum Action { STRIP, SCRAPE, WAX_OFF }
    public record Result(Action action, ItemStack output, SoundEvent sound) {}

    private AxeActionResolver() {
    }

    public static Optional<Result> resolve(ItemStack input, ItemStack tool) {
        if (input == null || input.isEmpty() || !FarmerToolSupport.isAxe(tool)
                 || !(input.getItem() instanceof BlockItem blockItem)) {
            return Optional.empty();
        }
        BlockState original = blockItem.getBlock().defaultBlockState();
        BlockState stripped = AxeItem.getAxeStrippingState(original);
        if (stripped != null && stripped.getBlock() != original.getBlock()) {
            return output(Action.STRIP, stripped.getBlock(), SoundEvents.AXE_STRIP);
        }
        Optional<BlockState> scraped = WeatheringCopper.getPrevious(original);
        if (scraped.isPresent() && scraped.get().getBlock() != original.getBlock()) {
            return output(Action.SCRAPE, scraped.get().getBlock(), SoundEvents.AXE_SCRAPE);
        }
        Block unwaxed = HoneycombItem.WAX_OFF_BY_BLOCK.get().get(original.getBlock());
        if (unwaxed != null && unwaxed != original.getBlock()) {
            return output(Action.WAX_OFF, unwaxed, SoundEvents.AXE_WAX_OFF);
        }
        return Optional.empty();
    }

    private static Optional<Result> output(Action action, Block block, SoundEvent sound) {
        Item item = block.asItem();
        if (item == null || item.getDefaultInstance().isEmpty())
            return Optional.empty();
        return Optional.of(new Result(action, new ItemStack(item), sound));
    }
}
