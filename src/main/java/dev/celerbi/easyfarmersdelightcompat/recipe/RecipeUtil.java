package dev.celerbi.easyfarmersdelightcompat.recipe;

import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import java.util.HashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

final class RecipeUtil {
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String DISPLAY_TAG = "display";

    private RecipeUtil() {
    }

    static boolean isBlock(ItemStack stack, ItemLike expected) {
        return !stack.isEmpty() && stack.is(expected.asItem());
    }

    static ItemStack upgradeFarmer(ItemStack source, Block target) {
        ItemStack result = new ItemStack(target);
        result.setCount(1);

        CompoundTag sourceTag = source.getTag();
        if (sourceTag != null && sourceTag.contains(DISPLAY_TAG, Tag.TAG_COMPOUND)) {
            result.addTagElement(DISPLAY_TAG, sourceTag.getCompound(DISPLAY_TAG).copy());
        }

        CompoundTag data = source.getTagElement(BLOCK_ENTITY_TAG);
        if (hasMeaningful(data)) {
            BlockItem.setBlockEntityData(
                    result,
                    ModBlockEntities.COMPAT_FARMER.get(),
                    data.copy()
            );
        }

        return result;
    }

    static boolean hasMeaningfulBlockEntityData(ItemStack stack) {
        return hasMeaningful(stack.getTagElement(BLOCK_ENTITY_TAG));
    }

    private static boolean hasMeaningful(CompoundTag data) {
        if (data == null || data.isEmpty()) {
            return false;
        }

        CompoundTag copy = data.copy();
        for (String key : new String[] {"id", "x", "y", "z"}) {
            copy.remove(key);
        }

        stripEmptyContainers(copy);
        copy.remove("EfdcSchema");

        for (String key : new String[] {
                "EfdcPaddyGrowth",
                "EfdcBaseProgress",
                "EfdcRopeOneProgress",
                "EfdcRopeTwoProgress",
                "EfdcRopeCount",
                "EfdcSugarCaneHeight",
                "EfdcSugarCaneAge"
        }) {
            if (copy.contains(key, Tag.TAG_ANY_NUMERIC) && copy.getInt(key) == 0) {
                copy.remove(key);
            }
        }

        for (String key : new String[] {"EfdcFruitReady", "EfdcPaddySand"}) {
            if (copy.contains(key, Tag.TAG_ANY_NUMERIC) && !copy.getBoolean(key)) {
                copy.remove(key);
            }
        }

        stripEmptyContainers(copy);
        return !copy.isEmpty();
    }

    private static void stripEmptyContainers(CompoundTag tag) {
        for (String key : new HashSet<>(tag.getAllKeys())) {
            Tag value = tag.get(key);
            if (value instanceof CompoundTag compound && compound.isEmpty()) {
                tag.remove(key);
            } else if (value instanceof ListTag list && list.isEmpty()) {
                tag.remove(key);
            }
        }
    }
}
