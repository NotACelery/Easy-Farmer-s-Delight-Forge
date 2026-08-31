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

public final class EasyMobFarmNoiseSwitchBlockEntity extends BlockEntity {
    public static final int REQUIRED_ROTTEN_FLESH = 6;
    private static final String KEY_ASSEMBLY_STAGE = "AssemblyStage";

    private int assemblyStage;
    private boolean itemPreview;

    public EasyMobFarmNoiseSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EASY_MOB_FARM_NOISE_SWITCH.get(), pos, state);
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

    public boolean isComplete() {
        return assemblyStage >= REQUIRED_ROTTEN_FLESH;
    }

    public boolean needsRottenFlesh() {
        return !isComplete();
    }

    public boolean insertRottenFlesh() {
        if (!needsRottenFlesh()) {
            return false;
        }
        assemblyStage++;
        setChangedAndSync();
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(KEY_ASSEMBLY_STAGE, assemblyStage);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        assemblyStage = Math.max(0, Math.min(REQUIRED_ROTTEN_FLESH, tag.getInt(KEY_ASSEMBLY_STAGE)));
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
