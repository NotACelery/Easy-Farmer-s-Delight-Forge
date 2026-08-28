package dev.celerbi.easyfarmersdelightcompat.integration;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.registry.*;
import java.util.List;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;

public final class CutterLogVariant {
    public static final String NBT_KEY = "CutterLog";
    public static final List<Block> SUPPORTED = List.of(
            Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG,
            Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG,
            Blocks.BAMBOO_BLOCK
    );
    public static final TagKey<Item> ALLOWED_LOGS = TagKey.create(
            Registries.ITEM,
            new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, "cutter_logs"));

    private CutterLogVariant() {
    }
    public static boolean isAllowed(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.is(ALLOWED_LOGS) && stack.getItem() instanceof BlockItem;
    }

    public static Block fromIngredient(ItemStack stack) {
        return isAllowed(stack) && stack.getItem() instanceof BlockItem bi ? bi.getBlock() : Blocks.OAK_LOG;
    }

    public static Block fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return Blocks.OAK_LOG;
        CompoundTag data = stack.getTagElement("BlockEntityTag");
        return data == null ? Blocks.OAK_LOG : read(data);
    }

    public static Block read(CompoundTag tag) {
        if (tag == null || !tag.contains(NBT_KEY))
            return Blocks.OAK_LOG;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(NBT_KEY));
        if (id == null)
            return Blocks.OAK_LOG;
        Block block = BuiltInRegistries.BLOCK.get(id);
        return isAllowed(new ItemStack(block.asItem())) ? block : Blocks.OAK_LOG;
    }

    public static void write(CompoundTag tag, Block log) {
        if (tag == null || log == null || log == Blocks.OAK_LOG || !isAllowed(new ItemStack(log.asItem())))
            return;
        tag.putString(NBT_KEY, BuiltInRegistries.BLOCK.getKey(log).toString());
    }

    public static ItemStack createCutter(Block log) {
        ItemStack result = new ItemStack(ModBlocks.CUTTER_ITEM.get());
        if (log == null || log == Blocks.OAK_LOG)
            return result;
        CompoundTag tag = new CompoundTag();
        write(tag, log);
        if (!tag.isEmpty())
            BlockItem.setBlockEntityData(result, ModBlockEntities.CUTTER.get(), tag);
        return result;
    }

    public static String translationKey(Block log) {
        String name = log == Blocks.SPRUCE_LOG ? "spruce"
                : log == Blocks.BIRCH_LOG ? "birch"
                : log == Blocks.JUNGLE_LOG ? "jungle"
                : log == Blocks.ACACIA_LOG ? "acacia"
                : log == Blocks.DARK_OAK_LOG ? "dark_oak"
                : log == Blocks.MANGROVE_LOG ? "mangrove"
                : log == Blocks.CHERRY_LOG ? "cherry"
                : log == Blocks.BAMBOO_BLOCK ? "bamboo"
                : "oak";
        return "variant." + EasyFarmersDelightCompat.MOD_ID + ".cutter." + name;
    }
}
