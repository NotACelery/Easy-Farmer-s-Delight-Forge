package dev.celerbi.easyfarmersdelightcompat.integration;

import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class CutterLogVariant {
    public static final String NBT_KEY = "CutterLog";
    private static final TagKey<Item> LEGACY_ALLOWED_LOGS = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("easyfarmersdelightcompat", "cutter_logs"));

    private CutterLogVariant() {
    }

    public static boolean isAllowed(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        Block block = blockItem.getBlock();
        return (stack.is(ItemTags.LOGS)
                        || stack.is(LEGACY_ALLOWED_LOGS)
                        || block.defaultBlockState().is(BlockTags.LOGS))
                && isBaseLog(block);
    }

    public static Block fromIngredient(ItemStack stack) {
        return isAllowed(stack) && stack.getItem() instanceof BlockItem blockItem
                ? blockItem.getBlock()
                : Blocks.OAK_LOG;
    }

    public static Block fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Blocks.OAK_LOG;
        }
        CompoundTag data = stack.getTagElement("BlockEntityTag");
        return data == null ? Blocks.OAK_LOG : read(data);
    }

    public static Block read(CompoundTag tag) {
        if (tag == null || !tag.contains(NBT_KEY)) {
            return Blocks.OAK_LOG;
        }
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(NBT_KEY));
        if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
            return Blocks.OAK_LOG;
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        return isAllowed(new ItemStack(block.asItem())) ? block : Blocks.OAK_LOG;
    }

    public static void write(CompoundTag tag, Block log) {
        if (tag == null || log == null || log == Blocks.OAK_LOG || !isAllowed(new ItemStack(log.asItem()))) {
            return;
        }
        tag.putString(NBT_KEY, BuiltInRegistries.BLOCK.getKey(log).toString());
    }

    public static ItemStack createCutter(Block log) {
        ItemStack result = new ItemStack(ModBlocks.CUTTER_ITEM.get());
        if (log == null || log == Blocks.OAK_LOG) {
            return result;
        }
        CompoundTag tag = new CompoundTag();
        write(tag, log);
        if (!tag.isEmpty()) {
            BlockItem.setBlockEntityData(result, ModBlockEntities.CUTTER.get(), tag);
        }
        return result;
    }

    private static boolean isBaseLog(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        String path = id.getPath();
        return !path.startsWith("stripped_")
                && !path.endsWith("_wood")
                && !path.endsWith("_hyphae");
    }
}
