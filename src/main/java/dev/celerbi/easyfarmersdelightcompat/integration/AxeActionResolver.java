package dev.celerbi.easyfarmersdelightcompat.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public final class AxeActionResolver {
    public enum Action { STRIP, SCRAPE, WAX_OFF }
    public record Result(Action action, ItemStack output, List<ItemStack> byproducts, SoundEvent sound) {
        public List<ItemStack> outputs() {
            if (byproducts == null || byproducts.isEmpty()) {
                return List.of(output);
            }
            List<ItemStack> results = new ArrayList<>(1 + byproducts.size());
            results.add(output);
            for (ItemStack byproduct : byproducts) {
                if (byproduct != null && !byproduct.isEmpty()) {
                    results.add(byproduct);
                }
            }
            return List.copyOf(results);
        }
    }

    private AxeActionResolver() {
    }

    public static Optional<Result> resolve(ItemStack input, ItemStack tool) {
        if (input == null || input.isEmpty() || !FarmerToolSupport.isAxe(tool)
                 || !(input.getItem() instanceof BlockItem blockItem)) {
            return Optional.empty();
        }
        Optional<Result> cinnamon = resolveCroptopiaCinnamon(input);
        if (cinnamon.isPresent()) {
            return cinnamon;
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

    private static Optional<Result> resolveCroptopiaCinnamon(ItemStack input) {
        ResourceLocation inputId = BuiltInRegistries.ITEM.getKey(input.getItem());
        if (inputId == null || !"croptopia".equals(inputId.getNamespace())) {
            return Optional.empty();
        }

        String outputPath = switch (inputId.getPath()) {
            case "cinnamon_log" -> "stripped_cinnamon_log";
            case "cinnamon_wood" -> "stripped_cinnamon_wood";
            default -> null;
        };
        if (outputPath == null) {
            return Optional.empty();
        }

        Item stripped = BuiltInRegistries.ITEM.get(new ResourceLocation("croptopia", outputPath));
        Item cinnamon = BuiltInRegistries.ITEM.get(new ResourceLocation("croptopia", "cinnamon"));
        if (stripped == null || stripped == Items.AIR || cinnamon == null || cinnamon == Items.AIR) {
            return Optional.empty();
        }
        return Optional.of(new Result(
                Action.STRIP,
                new ItemStack(stripped),
                List.of(new ItemStack(cinnamon)),
                SoundEvents.AXE_STRIP
        ));
    }

    private static Optional<Result> output(Action action, Block block, SoundEvent sound) {
        Item item = block.asItem();
        if (item == null || item.getDefaultInstance().isEmpty())
            return Optional.empty();
        return Optional.of(new Result(action, new ItemStack(item), List.of(), sound));
    }
}
