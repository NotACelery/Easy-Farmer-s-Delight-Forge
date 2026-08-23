package dev.celerbi.easyfarmersdelightcompat.compat.jade;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.integration.CutterLogVariant;
import dev.celerbi.easyfarmersdelightcompat.integration.ToolRequirement;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

/** Jade status for Cutter storage plus non-RNG processing diagnostics. */
public enum CutterJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(
            EasyFarmersDelightCompat.MOD_ID,
            "cutter_info"
    );
    private static final String VARIANT = "Variant";
    private static final String TOOL = "Tool";
    private static final String OUTPUTS = "Outputs";
    private static final String STACK = "Stack";
    private static final String COUNT = "Count";
    private static final String HAS_VILLAGER = "HasVillager";
    private static final String WAITING_TOOL = "WaitingTool";
    private static final String WRONG_TOOL = "WrongTool";
    private static final String PROGRESS = "Progress";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof CutterBlockEntity cutter)) return;

        data.putString(VARIANT, BuiltInRegistries.BLOCK.getKey(cutter.logVariant()).toString());
        data.putBoolean(HAS_VILLAGER, cutter.hasVillager());
        data.putInt(PROGRESS, cutter.progress());

        data.remove(TOOL);
        ItemStack tool = cutter.toolHandler().getStackInSlot(0);
        if (!tool.isEmpty()) data.put(TOOL, tool.save(new net.minecraft.nbt.CompoundTag()));

        data.remove(WAITING_TOOL);
        data.remove(WRONG_TOOL);
        ToolRequirement requirement = cutter.blockingToolRequirement(accessor.getLevel(), tool);
        if (cutter.hasVillager() && requirement.isRequired()) {
            data.putString(WAITING_TOOL, requirement.name());
            data.putBoolean(WRONG_TOOL, !tool.isEmpty());
        }

        data.remove(OUTPUTS);
        List<OutputEntry> outputs = aggregate(cutter);
        if (!outputs.isEmpty()) {
            ListTag list = new ListTag();
            for (OutputEntry entry : outputs) {
                CompoundTag output = new CompoundTag();
                output.put(STACK, entry.stack.save(new net.minecraft.nbt.CompoundTag()));
                output.putInt(COUNT, entry.count);
                list.add(output);
            }
            data.put(OUTPUTS, list);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        String variant = data.getString(VARIANT);
        if (!variant.isEmpty()) {
            ResourceLocation id = ResourceLocation.tryParse(variant);
            Block block = id == null ? null : BuiltInRegistries.BLOCK.get(id);
            if (block != null) {
                tooltip.add(Component.translatable(
                        "jade.easyfarmersdelightcompat.cutter.variant",
                        Component.translatable(CutterLogVariant.translationKey(block))
                ));
            }
        }

        if (data.contains(TOOL, Tag.TAG_COMPOUND)) {
            ItemStack tool = ItemStack.of(data.getCompound(TOOL));
            if (!tool.isEmpty()) {
                tooltip.add(Component.translatable(
                        "jade.easyfarmersdelightcompat.cutter.tool",
                        tool.getHoverName()
                ));
            }
        }

        if (!data.getBoolean(HAS_VILLAGER)) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.villager_required")
                    .withStyle(ChatFormatting.RED));
        } else {
            appendWaitingTool(tooltip, data.getString(WAITING_TOOL), data.getBoolean(WRONG_TOOL));
            int progress = Math.max(0, Math.min(10, data.getInt(PROGRESS)));
            if (progress > 0) {
                tooltip.add(Component.translatable(
                                "jade.easyfarmersdelightcompat.cutter.processing",
                                progress * 10
                        )
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        if (data.contains(OUTPUTS, Tag.TAG_LIST)) {
            ListTag list = data.getList(OUTPUTS, Tag.TAG_COMPOUND);
            IElementHelper elements = IElementHelper.get();
            IDisplayHelper display = IDisplayHelper.get();
            for (int i = 0; i < list.size(); i++) {
                CompoundTag output = list.getCompound(i);
                ItemStack stack = ItemStack.of(output.getCompound(STACK));
                int count = Math.max(0, output.getInt(COUNT));
                if (stack.isEmpty() || count <= 0) continue;
                Component line = Component.literal(display.humanReadableNumber(count, "", false, null))
                        .append("× ")
                        .append(display.stripColor(stack.getHoverName()));
                List<IElement> row = List.of(
                        elements.smallItem(stack).clearCachedMessage(),
                        elements.text(line).message(null)
                );
                tooltip.add(row);
            }
        }
    }

    private static void appendWaitingTool(ITooltip tooltip, String waiting, boolean wrongTool) {
        String key = switch (waiting) {
            case "KNIFE" -> wrongTool
                    ? "jade.easyfarmersdelightcompat.wrong_tool.knife"
                    : "jade.easyfarmersdelightcompat.waiting.knife";
            case "AXE" -> wrongTool
                    ? "jade.easyfarmersdelightcompat.wrong_tool.axe"
                    : "jade.easyfarmersdelightcompat.waiting.axe";
            case "KNIFE_OR_AXE" -> wrongTool
                    ? "jade.easyfarmersdelightcompat.wrong_tool.knife_or_axe"
                    : "jade.easyfarmersdelightcompat.waiting.knife_or_axe";
            default -> null;
        };
        if (key != null) tooltip.add(Component.translatable(key).withStyle(ChatFormatting.RED));
    }

    private static List<OutputEntry> aggregate(CutterBlockEntity cutter) {
        List<OutputEntry> outputs = new ArrayList<>();
        for (int slot = 0; slot < cutter.outputHandler().getSlots(); slot++) {
            ItemStack stack = cutter.outputHandler().getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            boolean merged = false;
            for (int i = 0; i < outputs.size(); i++) {
                OutputEntry entry = outputs.get(i);
                if (ItemStack.isSameItemSameTags(entry.stack, stack)) {
                    outputs.set(i, new OutputEntry(entry.stack, entry.count + stack.getCount()));
                    merged = true;
                    break;
                }
            }
            if (!merged) outputs.add(new OutputEntry(stack.copyWithCount(1), stack.getCount()));
        }
        return outputs;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    private record OutputEntry(ItemStack stack, int count) {
    }
}
