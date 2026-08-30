package dev.celerbi.easyfarmersdelightcompat.blockentity;

import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class IronFarmNoiseSwitchBlockEntity extends BlockEntity {
    public static final int REQUIRED_IRON_BLOCKS = 4;
    private static final String KEY_ASSEMBLY_STAGE = "AssemblyStage";
    private static final String KEY_HAS_GOLEM = "HasGolem";

    private int assemblyStage;
    private boolean hasGolem;
    private boolean itemPreview;

    public IronFarmNoiseSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IRON_FARM_NOISE_SWITCH.get(), pos, state);
    }

    public boolean isItemPreview() {
        return itemPreview;
    }

    public void setItemPreview(boolean itemPreview) {
        this.itemPreview = itemPreview;
    }

    public int assemblyStage() {
        return assemblyStage;
    }

    public boolean hasGolem() {
        return hasGolem;
    }

    public boolean needsIronBlock() {
        return !hasGolem && assemblyStage < REQUIRED_IRON_BLOCKS;
    }

    public boolean needsCarvedPumpkin() {
        return !hasGolem && assemblyStage >= REQUIRED_IRON_BLOCKS;
    }

    public boolean insertIronBlock() {
        if (!needsIronBlock())
            return false;
        assemblyStage++;
        setChangedAndSync();
        return true;
    }

    public boolean completeGolem() {
        if (!needsCarvedPumpkin())
            return false;
        assemblyStage = REQUIRED_IRON_BLOCKS;
        hasGolem = true;
        setChangedAndSync();
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(KEY_ASSEMBLY_STAGE, assemblyStage);
        tag.putBoolean(KEY_HAS_GOLEM, hasGolem);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        assemblyStage = Math.max(0, Math.min(REQUIRED_IRON_BLOCKS, tag.getInt(KEY_ASSEMBLY_STAGE)));
        hasGolem = tag.getBoolean(KEY_HAS_GOLEM);
        if (hasGolem)
            assemblyStage = REQUIRED_IRON_BLOCKS;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    private void setChangedAndSync() {
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }
}
