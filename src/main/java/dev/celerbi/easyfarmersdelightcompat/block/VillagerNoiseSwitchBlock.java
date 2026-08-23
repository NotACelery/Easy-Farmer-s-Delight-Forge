package dev.celerbi.easyfarmersdelightcompat.block;

import dev.celerbi.easyfarmersdelightcompat.blockentity.VillagerNoiseSwitchBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.client.ClientPreferences;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Physical control surface for a global, client-only Villager mute preference.
 *
 * The block intentionally has no POWERED property and never emits redstone. Lever
 * and dust state are renderer-only, so observers and other players cannot see or
 * exploit another client's preference.
 */
public final class VillagerNoiseSwitchBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public VillagerNoiseSwitchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VillagerNoiseSwitchBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.VILLAGER_NOISE_SWITCH.get()) return null;
        return (tickerLevel, pos, tickerState, blockEntity) -> {
            if (tickerLevel instanceof net.minecraft.server.level.ServerLevel serverLevel
                    && blockEntity instanceof VillagerNoiseSwitchBlockEntity noiseSwitch) {
                VillagerNoiseSwitchBlockEntity.serverTick(serverLevel, pos, tickerState, noiseSwitch);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(level.getBlockEntity(pos) instanceof VillagerNoiseSwitchBlockEntity noiseSwitch)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown() && noiseSwitch.hasVillager()) {
            if (!level.isClientSide) {
                ItemStack removed = noiseSwitch.removeVillager();
                if (!removed.isEmpty()) ItemHandlerHelper.giveItemToPlayer(player, removed);
                level.playSound(null, pos, SoundEvents.VILLAGER_CELEBRATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!noiseSwitch.hasVillager() && noiseSwitch.isVillagerItem(held)) {
            if (!level.isClientSide && noiseSwitch.insertVillager(held)) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, SoundEvents.VILLAGER_YES, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        toggleClient(level, player, noiseSwitch);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void toggleClient(Level level, Player player, VillagerNoiseSwitchBlockEntity noiseSwitch) {
        if (!level.isClientSide) return;
        if (!noiseSwitch.hasVillager()) {
            player.displayClientMessage(
                    Component.translatable("message.easyfarmersdelightcompat.noise_switch.villager_required"),
                    true
            );
            return;
        }

        boolean muted = ClientPreferences.toggleVillagersMuted();
        player.displayClientMessage(
                Component.translatable(muted
                        ? "message.easyfarmersdelightcompat.noise_switch.muted"
                        : "message.easyfarmersdelightcompat.noise_switch.enabled"),
                true
        );
        BlockPos pos = noiseSwitch.getBlockPos();
        level.playLocalSound(
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                SoundEvents.LEVER_CLICK, SoundSource.BLOCKS,
                0.3F, muted ? 0.6F : 0.5F,
                false
        );
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        // NeoForge routes Creative Pick Block through IBlockExtension's contextual
        // overload. Keep it clean: local mute state never belongs to an item and the
        // stored Villager must not be cloned in Creative.
        return new ItemStack(ModBlocks.VILLAGER_NOISE_SWITCH_ITEM.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack dropped = new ItemStack(ModBlocks.VILLAGER_NOISE_SWITCH_ITEM.get());
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof VillagerNoiseSwitchBlockEntity noiseSwitch && noiseSwitch.hasVillager()) {
            CompoundTag data = noiseSwitch.saveWithoutMetadata();
            if (!data.isEmpty()) {
                BlockItem.setBlockEntityData(dropped, ModBlockEntities.VILLAGER_NOISE_SWITCH.get(), data);
            }
        }
        return List.of(dropped);
    }
}
