package dev.celerbi.easyfarmersdelightcompat.blockentity;

import dev.celerbi.easyfarmersdelightcompat.integration.NoiseSwitchVillagerAdapter;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Stores only the physical Villager. Mute state is deliberately client-global and never stored here. */
public final class VillagerNoiseSwitchBlockEntity extends BlockEntity {
    private static final String KEY_VILLAGER = "NoiseSwitchVillager";

    private final NoiseSwitchVillagerAdapter villagerAdapter = new NoiseSwitchVillagerAdapter(this);
    private ItemStack villager = ItemStack.EMPTY;

    public VillagerNoiseSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VILLAGER_NOISE_SWITCH.get(), pos, state);
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, VillagerNoiseSwitchBlockEntity blockEntity) {
        if (!blockEntity.hasVillager()) return;

        // Match Easy Villagers' stored-villager aging semantics without spamming a
        // block-entity update packet every second. Mark the chunk dirty as age moves,
        // periodically flush the cached entity back into the VillagerItem for crash-
        // safe persistence, and only sync clients when the baby/adult visual state
        // can actually change.
        boolean becameAdult = blockEntity.villagerAdapter.advanceAge();
        blockEntity.setChanged();
        if (level.getGameTime() % 20L == 0L) {
            blockEntity.villagerAdapter.flushToOwner();
        }
        if (becameAdult) {
            blockEntity.villagerAdapter.flushToOwner();
            blockEntity.syncBlock();
        }
    }

    public NoiseSwitchVillagerAdapter villagerAdapter() {
        return villagerAdapter;
    }

    public boolean hasVillager() {
        return !villager.isEmpty();
    }

    public boolean isVillagerItem(ItemStack stack) {
        return villagerAdapter.isVillagerItem(stack);
    }

    public boolean insertVillager(ItemStack stack) {
        if (hasVillager() || !isVillagerItem(stack)) return false;
        villager = stack.copyWithCount(1);
        villagerAdapter.reset();
        setChangedAndSync();
        return true;
    }

    public ItemStack removeVillager() {
        if (villager.isEmpty()) return ItemStack.EMPTY;
        villagerAdapter.flushToOwner();
        ItemStack result = villager.copyWithCount(1);
        villager = ItemStack.EMPTY;
        villagerAdapter.reset();
        setChangedAndSync();
        return result;
    }

    public ItemStack getStoredVillager() {
        return villager.copy();
    }

    public void updateVillagerFromAdapter(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && villagerAdapter.isVillagerItem(stack)) {
            villager = stack.copyWithCount(1);
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);villagerAdapter.flushToOwner();if (!villager.isEmpty()) tag.put(KEY_VILLAGER, villager.save(new CompoundTag()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ItemStack loadedVillager = tag.contains(KEY_VILLAGER, Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound(KEY_VILLAGER))
                : ItemStack.EMPTY;
        villager = villagerAdapter.isVillagerItem(loadedVillager)
                ? loadedVillager.copyWithCount(1)
                : ItemStack.EMPTY;
        villagerAdapter.reset();
    }


    @Override
    public void setRemoved() {
        villagerAdapter.reset();
        super.setRemoved();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

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
}
