package dev.celerbi.easyfarmersdelightcompat.blockentity;

import dev.celerbi.easyfarmersdelightcompat.integration.AxeActionResolver;
import dev.celerbi.easyfarmersdelightcompat.integration.CutterLogVariant;
import dev.celerbi.easyfarmersdelightcompat.integration.CutterOperationProbe;
import dev.celerbi.easyfarmersdelightcompat.integration.CutterVillagerAdapter;
import dev.celerbi.easyfarmersdelightcompat.integration.CuttingRecipeResolver;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmerToolSupport;
import dev.celerbi.easyfarmersdelightcompat.integration.OutputSimulator;
import dev.celerbi.easyfarmersdelightcompat.integration.ToolRequirement;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public final class CutterBlockEntity extends BlockEntity {
    public static final int PROCESS_TICKS = 10;
    public static final int INPUT_SLOTS = 4;
    public static final int OUTPUT_SLOTS = 4;

    private static final String KEY_VILLAGER = "CutterVillager";
    private static final String KEY_TOOL = "CutterTool";
    private static final String KEY_INPUT = "CutterInput";
    private static final String KEY_OUTPUT = "CutterOutput";
    private static final String KEY_PROGRESS = "CutterProgress";
    private final CutterVillagerAdapter villagerAdapter = new CutterVillagerAdapter(this);
    private ItemStack villager = ItemStack.EMPTY;
    private Block logVariant = Blocks.OAK_LOG;
    private int progress;
    private boolean loadingState;
    private boolean itemPreview;
    private boolean workPlanDirty = true;
    private boolean workPlanAvailable;
    private boolean waitingForOutputSpace;
    private boolean mutatingWorkContents;
    private boolean pendingToolRequirementDirty = true;
    private ToolRequirement cachedPendingToolRequirement = ToolRequirement.NONE;

    private final ItemStackHandler tool = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return FarmerToolSupport.isCuttingTool(stack);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!loadingState)
                onInputOrToolContentsChanged();
        }
    };

    private final ItemStackHandler input = new ItemStackHandler(INPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            if (!loadingState)
                onInputOrToolContentsChanged();
        }
    };

    private final ItemStackHandler output = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            if (!loadingState)
                onOutputContentsChanged();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack extracted = super.extractItem(slot, amount, simulate);
            if (!simulate && !loadingState && !extracted.isEmpty())
                onOutputReduced();
            return extracted;
        }
    };

    private final IItemHandler topAutomation = new TopInsertHandler(tool, input);
    private final IItemHandler sideAutomation = new InputOnlyHandler(input);
    private final IItemHandler bottomAutomation = new OutputOnlyHandler(output);
    private LazyOptional<IItemHandler> topCapability = LazyOptional.of(() -> topAutomation);
    private LazyOptional<IItemHandler> sideCapability = LazyOptional.of(() -> sideAutomation);
    private LazyOptional<IItemHandler> bottomCapability = LazyOptional.of(() -> bottomAutomation);

    public CutterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CUTTER.get(), pos, state);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, CutterBlockEntity cutter) {
        if (cutter.hasVillager()) {
            boolean becameAdult = cutter.villagerAdapter.advanceAge();
            if (becameAdult) {
                cutter.villagerAdapter.flushToOwner();
                cutter.invalidateWorkPlan();
                cutter.setChangedAndSync();
            } else if (level.getGameTime() % 20L == 0L) {
                cutter.villagerAdapter.flushToOwner();
                cutter.setChanged();
            }
        }

        if (cutter.progress == 0 && !cutter.workPlanDirty && !cutter.workPlanAvailable)
            return;

        if (!cutter.hasBasicWorkPrerequisites() || !cutter.hasProcessableWork(level)) {
            cutter.setProgress(0);
            cutter.parkUntilContentsChange();
            return;
        }

        cutter.setProgress(cutter.progress + 1);
        if (cutter.progress < PROCESS_TICKS)
            return;

        cutter.setProgress(0);
        if (!cutter.tryProcess(level))
            cutter.parkUntilContentsChange();
    }

    private boolean hasBasicWorkPrerequisites() {
        return villagerAdapter.hasAdultVillager()
                 && FarmerToolSupport.isCuttingTool(tool.getStackInSlot(0))
                 && hasAnyInput();
    }

    private boolean hasAnyInput() {
        for (int i = 0; i < input.getSlots(); i++)
            if (!input.getStackInSlot(i).isEmpty())
                return true;
        return false;
    }

    private boolean hasProcessableWork(Level level) {
        if (!workPlanDirty)
            return workPlanAvailable;
        workPlanAvailable = findProcessableInput(level, tool.getStackInSlot(0));
        workPlanDirty = false;
        return workPlanAvailable;
    }

    private boolean findProcessableInput(Level level, ItemStack equipped) {
        if (level == null || equipped.isEmpty())
            return false;
        List<ItemStack> concreteTool = List.of(equipped.copyWithCount(1));
        for (int slot = 0; slot < input.getSlots(); slot++) {
            ItemStack source = input.getStackInSlot(slot);
            if (source.isEmpty())
                continue;
            if (CuttingRecipeResolver.hasMatchingRecipe(level, source, concreteTool))
                return true;
            if (AxeActionResolver.resolve(source, equipped).isPresent())
                return true;
        }
        return false;
    }

    private void invalidateWorkPlan() {
        workPlanDirty = true;
        workPlanAvailable = false;
        waitingForOutputSpace = false;
    }

    private void parkUntilContentsChange() {
        workPlanDirty = false;
        workPlanAvailable = false;
    }

    private void invalidatePendingToolRequirement() {
        pendingToolRequirementDirty = true;
        cachedPendingToolRequirement = ToolRequirement.NONE;
    }

    private void onInputOrToolContentsChanged() {
        if (mutatingWorkContents)
            return;
        invalidateWorkPlan();
        invalidatePendingToolRequirement();
        setChangedAndSync();
    }

    private void onOutputContentsChanged() {
        if (!mutatingWorkContents)
            setChanged();
    }

    private void onOutputReduced() {
        if (mutatingWorkContents || !waitingForOutputSpace)
            return;
        invalidateWorkPlan();
    }

    private boolean tryProcess(ServerLevel level) {
        ItemStack equipped = tool.getStackInSlot(0);
        int fortune = fortuneLevel(level, equipped);
        boolean blockedByOutput = false;

        for (int slot = 0; slot < input.getSlots(); slot++) {
            ItemStack source = input.getStackInSlot(slot);
            if (source.isEmpty())
                continue;

            Optional<CuttingRecipeResolver.Result> cutting = CuttingRecipeResolver.resolve(
                    level,
                    source,
                    equipped,
                    fortune
            );
            if (cutting.isPresent()) {
                CuttingRecipeResolver.Result result = cutting.get();
                if (OutputSimulator.canFitAll(output, result.outputs())) {
                    waitingForOutputSpace = false;
                    return completeOperation(
                            level,
                            slot,
                            result.outputs(),
                            result.sound().orElse(SoundEvents.VILLAGER_WORK_BUTCHER)
                    );
                }
                blockedByOutput = true;
                continue;
            }

            Optional<AxeActionResolver.Result> axe = AxeActionResolver.resolve(source, equipped);
            if (axe.isPresent()) {
                AxeActionResolver.Result result = axe.get();
                List<ItemStack> results = result.outputs();
                if (OutputSimulator.canFitAll(output, results)) {
                    waitingForOutputSpace = false;
                    return completeOperation(level, slot, results, result.sound());
                }
                blockedByOutput = true;
            }
        }

        waitingForOutputSpace = blockedByOutput;
        return false;
    }

    private boolean completeOperation(ServerLevel level, int inputSlot, List<ItemStack> results, SoundEvent sound) {
        ItemStack source = input.getStackInSlot(inputSlot);
        if (source.isEmpty())
            return false;

        ItemStack sourceBefore = source.copy();
        List<ItemStack> outputBefore = snapshotHandler(output);

        mutatingWorkContents = true;
        try {
            source.shrink(1);
            input.setStackInSlot(inputSlot, source);

            if (!OutputSimulator.insertAllAfterSuccessfulSimulation(output, results)) {
                input.setStackInSlot(inputSlot, sourceBefore);
                restoreHandler(output, outputBefore);
                return false;
            }

            ItemStack equipped = tool.getStackInSlot(0);
            if (!equipped.isEmpty() && equipped.isDamageableItem()) {
                if (equipped.hurt(1, level.random, null)) {
                    equipped.shrink(1);
                    level.playSound(
                            null,
                            worldPosition,
                            SoundEvents.ITEM_BREAK,
                            SoundSource.BLOCKS,
                            .8F,
                            1F
                    );
                }
                tool.setStackInSlot(0, equipped);
            }
        } finally {
            mutatingWorkContents = false;
        }

        level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, .8F, 1F);
        invalidateWorkPlan();
        invalidatePendingToolRequirement();
        setChangedAndSync();
        return true;
    }

    private static List<ItemStack> snapshotHandler(ItemStackHandler handler) {
        List<ItemStack> snapshot = new ArrayList<>(handler.getSlots());
        for (int slot = 0; slot < handler.getSlots(); slot++)
            snapshot.add(handler.getStackInSlot(slot).copy());
        return snapshot;
    }

    private static void restoreHandler(ItemStackHandler handler, List<ItemStack> snapshot) {
        for (int slot = 0; slot < handler.getSlots(); slot++)
            handler.setStackInSlot(slot, snapshot.get(slot).copy());
    }

    private static int fortuneLevel(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty())
            return 0;
        try {
            return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stack);
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public boolean isVillagerItem(ItemStack stack) {
        return villagerAdapter.isVillagerItem(stack);
    }

    public boolean hasVillager() {
        return !villager.isEmpty();
    }

    public boolean insertVillager(ItemStack stack) {
        if (hasVillager() || !isVillagerItem(stack))
            return false;
        villager = stack.copyWithCount(1);
        villagerAdapter.reset();
        invalidateWorkPlan();
        setChangedAndSync();
        return true;
    }

    public ItemStack removeVillager() {
        if (villager.isEmpty())
            return ItemStack.EMPTY;
        villagerAdapter.flushToOwner();
        ItemStack r = villager.copyWithCount(1);
        villager = ItemStack.EMPTY;
        villagerAdapter.reset();
        invalidateWorkPlan();
        setProgress(0);
        setChangedAndSync();
        return r;
    }

    public ItemStack getStoredVillager() {
        return villager;
    }

    public void updateVillagerFromAdapter(ItemStack stack) {
        if (stack != null && !stack.isEmpty())
            villager = stack.copyWithCount(1);
    }

    public CutterVillagerAdapter villagerAdapter() {
        return villagerAdapter;
    }

    public boolean isItemPreview() {
        return itemPreview;
    }

    public void setItemPreview(boolean itemPreview) {
        this.itemPreview = itemPreview;
    }

    public Block logVariant() {
        return logVariant;
    }

    public void setLogVariant(Block log) {
        Block normalized = log == null ? Blocks.OAK_LOG : log;
        if (logVariant != normalized) {
            logVariant = normalized;
            setChangedAndSync();
        }
    }

    public boolean hasStoredContents() {
        return !villager.isEmpty() || progress != 0 || !handlerEmpty(tool) || !handlerEmpty(input)
                || !handlerEmpty(output);
    }

    public ItemStackHandler toolHandler() {
        return tool;
    }

    public ItemStackHandler inputHandler() {
        return input;
    }

    public ItemStackHandler outputHandler() {
        return output;
    }

    public IItemHandler getAutomationHandler(Direction direction) {
        return direction == Direction.DOWN
                ? bottomAutomation
                : direction == Direction.UP ? topAutomation : sideAutomation;
    }

    public ItemStack displayInput() {
        if (!hasVillager() || !FarmerToolSupport.isCuttingTool(tool.getStackInSlot(0)))
            return ItemStack.EMPTY;
        for (int i = 0; i < input.getSlots(); i++) {
            ItemStack s = input.getStackInSlot(i);
            if (!s.isEmpty())
                return s.copyWithCount(1);
        }
        return ItemStack.EMPTY;
    }

    public ToolRequirement pendingToolRequirement(Level level) {
        if (!pendingToolRequirementDirty)
            return cachedPendingToolRequirement;
        cachedPendingToolRequirement = blockingToolRequirement(level, tool.getStackInSlot(0));
        pendingToolRequirementDirty = false;
        return cachedPendingToolRequirement;
    }

    public ToolRequirement blockingToolRequirement(Level level, ItemStack equipped) {
        if (level == null) {
            return ToolRequirement.NONE;
        }

        CutterOperationProbe.ToolSamples toolSamples = CutterOperationProbe.representativeTools();
        java.util.EnumSet<ToolRequirement> missing = java.util.EnumSet.noneOf(ToolRequirement.class);
        for (int slot = 0; slot < input.getSlots(); slot++) {
            ItemStack source = input.getStackInSlot(slot);
            if (source.isEmpty()) {
                continue;
            }

            CutterOperationProbe.Result probe = CutterOperationProbe.probe(level, source, toolSamples);
            if (!probe.processable()) {
                continue;
            }
            if (probe.supports(equipped)) {
                return ToolRequirement.NONE;
            }
            missing.addAll(probe.supportedRequirements());
        }

        if (missing.isEmpty()) {
            return ToolRequirement.NONE;
        }
        if (missing.size() == 1) {
            return missing.iterator().next();
        }
        if (missing.size() == 2
                && missing.contains(ToolRequirement.KNIFE)
                && missing.contains(ToolRequirement.AXE)) {
            return ToolRequirement.KNIFE_OR_AXE;
        }
        return ToolRequirement.CUTTING_TOOL;
    }

    public int progress() {
        return progress;
    }

    private void setProgress(int value) {
        int v = Math.max(0, Math.min(PROCESS_TICKS, value));
        if (progress != v) {
            progress = v;
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        villagerAdapter.flushToOwner();
        CutterLogVariant.write(tag, logVariant);
        if (!villager.isEmpty())
            tag.put(KEY_VILLAGER, villager.save(new CompoundTag()));
        if (!handlerEmpty(tool))
            tag.put(KEY_TOOL, tool.serializeNBT());
        if (!handlerEmpty(input))
            tag.put(KEY_INPUT, input.serializeNBT());
        if (!handlerEmpty(output))
            tag.put(KEY_OUTPUT, output.serializeNBT());
        if (progress != 0)
            tag.putInt(KEY_PROGRESS, progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        logVariant = CutterLogVariant.read(tag);
        villager = tag.contains(KEY_VILLAGER, Tag.TAG_COMPOUND) ? ItemStack.of(tag
                .getCompound(KEY_VILLAGER)) : ItemStack.EMPTY;
        loadingState = true;
        try {
            clear(tool);
            clear(input);
            clear(output);
            if (tag.contains(KEY_TOOL, Tag.TAG_COMPOUND))
                tool.deserializeNBT(tag.getCompound(KEY_TOOL));
            if (tag.contains(KEY_INPUT, Tag.TAG_COMPOUND))
                input.deserializeNBT(tag.getCompound(KEY_INPUT));
            if (tag.contains(KEY_OUTPUT, Tag.TAG_COMPOUND))
                output.deserializeNBT(tag.getCompound(KEY_OUTPUT));
        } finally {
            loadingState = false;
        }
        progress = Math.max(0, Math.min(PROCESS_TICKS, tag.getInt(KEY_PROGRESS)));
        villagerAdapter.reset();
        invalidateWorkPlan();
        invalidatePendingToolRequirement();
    }
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (!remove && capability == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.DOWN)
                return bottomCapability.cast();
            if (side == Direction.UP)
                return topCapability.cast();
            return sideCapability.cast();
        }
        return super.getCapability(capability, side);
    }
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        topCapability.invalidate();
        sideCapability.invalidate();
        bottomCapability.invalidate();
    }
    @Override
    public void reviveCaps() {
        super.reviveCaps();
        topCapability = LazyOptional.of(() -> topAutomation);
        sideCapability = LazyOptional.of(() -> sideAutomation);
        bottomCapability = LazyOptional.of(() -> bottomAutomation);
    }
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    private static boolean handlerEmpty(ItemStackHandler h) {
        for (int i = 0; i < h.getSlots(); i++)
            if (!h.getStackInSlot(i).isEmpty())
                return false;
        return true;
    }

    private static void clear(ItemStackHandler h) {
        for (int i = 0; i < h.getSlots(); i++)
            h.setStackInSlot(i, ItemStack.EMPTY);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void setChangedAndSync() {
        setChanged();
        syncBlock();
    }

    private void syncBlock() {
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    private static final class InputOnlyHandler implements IItemHandler{
        private final IItemHandler d;
        InputOnlyHandler(IItemHandler d) {
            this.d = d;
        }

        public int getSlots() {
            return d.getSlots();
        }

        public ItemStack getStackInSlot(int s) {
            return d.getStackInSlot(s);
        }

        public ItemStack insertItem(int s, ItemStack stack, boolean sim) {
            return FarmerToolSupport.isCuttingTool(stack) ? stack : d.insertItem(s, stack, sim);
        }

        public ItemStack extractItem(int s, int a, boolean sim) {
            return ItemStack.EMPTY;
        }

        public int getSlotLimit(int s) {
            return d.getSlotLimit(s);
        }

        public boolean isItemValid(int s, ItemStack stack) {
            return !FarmerToolSupport.isCuttingTool(stack) && d.isItemValid(s, stack);
        }
    }

    private static final class TopInsertHandler implements IItemHandler{
        private final IItemHandler tool, input;
        TopInsertHandler(IItemHandler tool, IItemHandler input) {
            this.tool = tool;
            this.input = input;
        }

        public int getSlots() {
            return 1 + input.getSlots();
        }

        public ItemStack getStackInSlot(int s) {
            return s == 0 ? tool.getStackInSlot(0) : input.getStackInSlot(s - 1);
        }

        public ItemStack insertItem(int s, ItemStack stack, boolean sim) {
            if (s == 0)
                return tool.insertItem(0, stack, sim);
            if (s < 1 || s >= getSlots() || FarmerToolSupport.isCuttingTool(stack))
                return stack;
            return input.insertItem(s - 1, stack, sim);
        }

        public ItemStack extractItem(int s, int a, boolean sim) {
            return ItemStack.EMPTY;
        }

        public int getSlotLimit(int s) {
            return s == 0 ? tool.getSlotLimit(0) : input.getSlotLimit(s - 1);
        }

        public boolean isItemValid(int s, ItemStack stack) {
            return s == 0 ? tool.isItemValid(0, stack) : s >= 1 && s<getSlots() && !FarmerToolSupport
                    .isCuttingTool(stack) && input.isItemValid(s - 1, stack);
        }
    }

    private static final class OutputOnlyHandler implements IItemHandler{
        private final IItemHandler d;
        OutputOnlyHandler(IItemHandler d) {
            this.d = d;
        }

        public int getSlots() {
            return d.getSlots();
        }

        public ItemStack getStackInSlot(int s) {
            return d.getStackInSlot(s);
        }

        public ItemStack insertItem(int s, ItemStack stack, boolean sim) {
            return stack;
        }

        public ItemStack extractItem(int s, int a, boolean sim) {
            return d.extractItem(s, a, sim);
        }

        public int getSlotLimit(int s) {
            return d.getSlotLimit(s);
        }

        public boolean isItemValid(int s, ItemStack stack) {
            return false;
        }
    }
}
