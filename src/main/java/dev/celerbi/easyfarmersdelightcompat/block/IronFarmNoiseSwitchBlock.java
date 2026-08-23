package dev.celerbi.easyfarmersdelightcompat.block;

import dev.celerbi.easyfarmersdelightcompat.blockentity.IronFarmNoiseSwitchBlockEntity;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class IronFarmNoiseSwitchBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public IronFarmNoiseSwitchBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }

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
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) { return 1.0F; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IronFarmNoiseSwitchBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(level.getBlockEntity(pos) instanceof IronFarmNoiseSwitchBlockEntity noiseSwitch)) {
            return InteractionResult.PASS;
        }

        if (noiseSwitch.hasGolem()) {
            toggleClient(level, player, noiseSwitch);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.is(Items.IRON_BLOCK) && noiseSwitch.needsIronBlock()) {
            if (!level.isClientSide && noiseSwitch.insertIronBlock()) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, Blocks.IRON_BLOCK.defaultBlockState().getSoundType().getPlaceSound(),
                        SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (held.is(Items.CARVED_PUMPKIN) && noiseSwitch.needsCarvedPumpkin()) {
            if (!level.isClientSide && noiseSwitch.completeGolem()) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, Blocks.CARVED_PUMPKIN.defaultBlockState().getSoundType().getPlaceSound(),
                        SoundSource.BLOCKS, 0.8F, 1.0F);
                level.playSound(null, pos, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.BLOCKS, 1.0F, 0.9F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        showAssemblyRequirement(level, player, noiseSwitch);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void showAssemblyRequirement(Level level, Player player, IronFarmNoiseSwitchBlockEntity noiseSwitch) {
        if (!level.isClientSide) return;
        if (noiseSwitch.needsIronBlock()) {
            player.displayClientMessage(Component.translatable(
                    "message.easyfarmersdelightcompat.iron_farm_noise_switch.iron_required",
                    noiseSwitch.assemblyStage(), IronFarmNoiseSwitchBlockEntity.REQUIRED_IRON_BLOCKS
            ), true);
        } else {
            player.displayClientMessage(Component.translatable(
                    "message.easyfarmersdelightcompat.iron_farm_noise_switch.pumpkin_required"
            ), true);
        }
    }

    private static void toggleClient(Level level, Player player, IronFarmNoiseSwitchBlockEntity noiseSwitch) {
        if (!level.isClientSide) return;
        boolean muted = ClientPreferences.toggleIronFarmSoundsMuted();
        player.displayClientMessage(Component.translatable(muted
                ? "message.easyfarmersdelightcompat.iron_farm_noise_switch.muted"
                : "message.easyfarmersdelightcompat.iron_farm_noise_switch.enabled"), true);

        BlockPos pos = noiseSwitch.getBlockPos();
        level.playLocalSound(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, muted ? 0.6F : 0.5F, false);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(ModBlocks.IRON_FARM_NOISE_SWITCH_ITEM.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack dropped = new ItemStack(ModBlocks.IRON_FARM_NOISE_SWITCH_ITEM.get());
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof IronFarmNoiseSwitchBlockEntity noiseSwitch
                && (noiseSwitch.hasGolem() || noiseSwitch.assemblyStage() > 0)) {
            CompoundTag data = noiseSwitch.saveWithoutMetadata();
            if (!data.isEmpty()) {
                BlockItem.setBlockEntityData(dropped, ModBlockEntities.IRON_FARM_NOISE_SWITCH.get(), data);
            }
        }
        return List.of(dropped);
    }
}
