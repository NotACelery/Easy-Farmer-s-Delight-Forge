package dev.celerbi.easyfarmersdelightcompat.integration;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Shared tool classification for Rich Farmers, Paddy Farmers and the Cutter. */
public final class FarmerToolSupport {
    public static final TagKey<Item> KNIVES = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("forge", "tools/knives")
    );

    /**
     * Ordered category policy used by acceptance, empty-slot examples and later
     * viewer documentation. Keeping the category lists here prevents GUI/viewer
     * code from silently drifting away from the actual slot rules.
     */
    public static final List<TagKey<Item>> HARVEST_TOOL_CATEGORIES = List.of(
            KNIVES,
            ItemTags.HOES,
            ItemTags.AXES
    );
    public static final List<TagKey<Item>> CUTTING_TOOL_CATEGORIES = List.of(
            KNIVES,
            ItemTags.AXES
    );

    /** The slot keeps the familiar Knife silhouette even though it now accepts more tools. */
    public static final ResourceLocation EMPTY_HARVEST_TOOL_SLOT = new ResourceLocation(
            EasyFarmersDelightCompat.MOD_ID,
            "item/empty_knife_slot"
    );

    private FarmerToolSupport() {
    }

    public static boolean isKnife(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(KNIVES);
    }

    public static boolean isHoe(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(ItemTags.HOES);
    }

    public static boolean isAxe(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(ItemTags.AXES);
    }

    /** Rich Farmer / Rich Paddy protected slot. */
    public static boolean isHarvestTool(ItemStack stack) {
        return isKnife(stack) || isHoe(stack) || isAxe(stack);
    }

    /** Cutter protected slot. */
    public static boolean isCuttingTool(ItemStack stack) {
        return isKnife(stack) || isAxe(stack);
    }

    public static ItemStack normalizeHarvestTool(ItemStack stack) {
        return isHarvestTool(stack) ? stack.copyWithCount(1) : ItemStack.EMPTY;
    }

    public static ItemStack normalizeCuttingTool(ItemStack stack) {
        return isCuttingTool(stack) ? stack.copyWithCount(1) : ItemStack.EMPTY;
    }

    /**
     * Returns stable, de-duplicated one-count examples for a tool tag.
     *
     * The method intentionally does not cache globally: datapack/server tag
     * contents may differ between connections. GUI callers should snapshot the
     * result when their screen opens; server-side diagnostic callers can query
     * the current registry/tag state when needed.
     */
    public static List<ItemStack> taggedToolStacks(TagKey<Item> tag) {
        List<ItemStack> result = new ArrayList<>();
        BuiltInRegistries.ITEM.getTag(tag).ifPresent(set -> {
            for (Holder<Item> holder : set) {
                Item item = holder.value();
                if (item == null) continue;
                ItemStack stack = new ItemStack(item);
                if (stack.isEmpty()) continue;
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                boolean duplicate = result.stream().anyMatch(existing -> {
                    ResourceLocation existingId = BuiltInRegistries.ITEM.getKey(existing.getItem());
                    return id != null && id.equals(existingId);
                });
                if (!duplicate) result.add(stack);
            }
        });
        result.sort(Comparator.comparing(stack -> {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return id == null ? "" : id.toString();
        }));
        return List.copyOf(result);
    }
    /**
     * Server-side diagnostic samples. Normally the live tag provides every valid
     * tool; the vanilla Axe fallback keeps non-RNG Cutter diagnostics functional
     * even if a tag lookup is temporarily unavailable during a data reload.
     */
    public static List<ItemStack> representativeAxes() {
        List<ItemStack> axes = taggedToolStacks(ItemTags.AXES);
        return axes.isEmpty() ? List.of(new ItemStack(Items.IRON_AXE)) : axes;
    }

    /**
     * Same principle for knives. Farmer's Delight supplies the common knife tag;
     * if the tag lookup is temporarily unavailable, try stable FD registry ids
     * without creating a hard compile-time dependency on its item classes.
     */
    public static List<ItemStack> representativeKnives() {
        List<ItemStack> knives = taggedToolStacks(KNIVES);
        if (!knives.isEmpty()) return knives;
        for (String path : List.of("iron_knife", "flint_knife", "diamond_knife")) {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation("farmersdelight", path));
            if (item != null && item != Items.AIR) {
                ItemStack stack = new ItemStack(item);
                if (!stack.isEmpty()) return List.of(stack);
            }
        }
        return List.of();
    }

}
