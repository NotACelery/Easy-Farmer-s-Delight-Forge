package dev.celerbi.easyfarmersdelightcompat.block;

import dev.celerbi.easyfarmersdelightcompat.blockentity.GraftingSupportBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class GraftingSupportBlock extends Block implements EntityBlock {
    // Rope is visual-only for collision but needs a tiny thickness for ray picking.
    private static final double ROPE_EPSILON = 2.0E-6D;

    private static final VoxelShape SOLID_SHAPE = Shapes.or(
            Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D),
            Block.box(5.0D, 0.0D, 8.0D, 6.0D, 1.0D, 9.0D),
            Block.box(10.0D, 0.0D, 7.0D, 11.0D, 2.0D, 9.0D),
            Block.box(11.0D, 0.0D, 7.0D, 12.0D, 1.0D, 10.0D),
            Block.box(7.0D, 0.0D, 3.0D, 9.0D, 1.0D, 6.0D),
            Block.box(6.0D, 0.0D, 10.0D, 7.0D, 1.0D, 13.0D),
            Block.box(2.0D, 0.0D, 2.0D, 4.0D, 10.0D, 4.0D),
            Block.box(12.0D, 0.0D, 2.0D, 14.0D, 10.0D, 4.0D),
            Block.box(2.0D, 0.0D, 12.0D, 4.0D, 10.0D, 14.0D),
            Block.box(12.0D, 0.0D, 12.0D, 14.0D, 10.0D, 14.0D)
    );

    private static final VoxelShape OUTLINE_SHAPE = Shapes.or(
            SOLID_SHAPE,
            Block.box(4.0D, 6.0D, 3.0D - ROPE_EPSILON, 12.0D, 7.0D, 3.0D + ROPE_EPSILON),
            Block.box(4.0D, 6.0D, 13.0D - ROPE_EPSILON, 12.0D, 7.0D, 13.0D + ROPE_EPSILON),
            Block.box(4.0D, 8.0D, 3.0D - ROPE_EPSILON, 12.0D, 9.0D, 3.0D + ROPE_EPSILON),
            Block.box(4.0D, 8.0D, 13.0D - ROPE_EPSILON, 12.0D, 9.0D, 13.0D + ROPE_EPSILON),
            Block.box(3.0D - ROPE_EPSILON, 6.0D, 4.0D, 3.0D + ROPE_EPSILON, 7.0D, 12.0D),
            Block.box(13.0D - ROPE_EPSILON, 6.0D, 4.0D, 13.0D + ROPE_EPSILON, 7.0D, 12.0D),
            Block.box(3.0D - ROPE_EPSILON, 8.0D, 4.0D, 3.0D + ROPE_EPSILON, 9.0D, 12.0D),
            Block.box(13.0D - ROPE_EPSILON, 8.0D, 4.0D, 13.0D + ROPE_EPSILON, 9.0D, 12.0D)
    );

    public GraftingSupportBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GraftingSupportBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!context.getLevel().getBlockState(context.getClickedPos().above()).canBeReplaced()) {
            return null;
        }
        return super.getStateForPlacement(context);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(this)) {
            ensureCanopyMarker(level, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide
                && level.getBlockState(pos.above()).is(ModBlocks.GRAFTING_CANOPY.get())) {
            level.removeBlock(pos.above(), false);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SOLID_SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return OUTLINE_SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!ensureCanopyMarker(level, pos)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof GraftingSupportBlockEntity support) {
            support.randomGrowthTick(level, random);
        }
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        return interactAt(level, pos, player, hand, player.getItemInHand(hand));
    }

    public static boolean isLeavesItem(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().is(BlockTags.LEAVES);
    }

    public static InteractionResult interactAt(
            Level level,
            BlockPos supportPos,
            Player player,
            InteractionHand hand,
            ItemStack heldItem
    ) {
        if (!level.getBlockState(supportPos).is(ModBlocks.GRAFTING_SUPPORT.get())
                || !(level.getBlockEntity(supportPos) instanceof GraftingSupportBlockEntity support)) {
            return InteractionResult.PASS;
        }

        if (!support.hasCanopy() && isLeavesItem(heldItem)) {
            if (level.isClientSide) {
                return InteractionResult.sidedSuccess(true);
            }
            if (!ensureCanopyMarker(level, supportPos)) {
                return InteractionResult.PASS;
            }
            if (support.canAcceptLeaves(heldItem)) {
                setCanopyMarkerActive(level, supportPos, true);
                if (support.insertCanopy(heldItem)) {
                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                    BlockState canopyState = support.renderState();
                    var sound = canopyState.getSoundType();
                    level.playSound(
                            null,
                            supportPos.above(),
                            sound.getPlaceSound(),
                            SoundSource.BLOCKS,
                            Math.max(0.6F, sound.getVolume() * 0.8F),
                            sound.getPitch()
                    );
                } else {
                    setCanopyMarkerActive(level, supportPos, false);
                }
            }
            return InteractionResult.sidedSuccess(false);
        }

        if (!ensureCanopyMarker(level, supportPos)) {
            return InteractionResult.PASS;
        }
        if (support.canHarvestWith(heldItem)) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                for (ItemStack stack : support.harvest(serverLevel, player, hand, heldItem)) {
                    if (!stack.isEmpty()) {
                        Block.popResource(level, supportPos.above(), stack);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static BlockPos resolveSupportPos(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(ModBlocks.GRAFTING_SUPPORT.get())) {
            return pos;
        }
        if (state.is(ModBlocks.GRAFTING_CANOPY.get())
                && level.getBlockState(pos.below()).is(ModBlocks.GRAFTING_SUPPORT.get())) {
            return pos.below();
        }
        return null;
    }

    public static boolean ensureCanopyMarker(Level level, BlockPos supportPos) {
        boolean active = level.getBlockEntity(supportPos) instanceof GraftingSupportBlockEntity support
                && support.hasCanopy();
        BlockPos canopyPos = supportPos.above();
        BlockState canopyState = level.getBlockState(canopyPos);
        if (canopyState.is(ModBlocks.GRAFTING_CANOPY.get())) {
            if (!level.isClientSide && canopyState.getValue(GraftingCanopyBlock.ACTIVE) != active) {
                level.setBlock(canopyPos, canopyState.setValue(GraftingCanopyBlock.ACTIVE, active), 3);
            }
            return true;
        }
        if (!canopyState.canBeReplaced()) {
            return false;
        }
        if (!level.isClientSide) {
            level.setBlock(
                    canopyPos,
                    ModBlocks.GRAFTING_CANOPY.get().defaultBlockState().setValue(GraftingCanopyBlock.ACTIVE, active),
                    3
            );
        }
        return true;
    }

    public static void setCanopyMarkerActive(Level level, BlockPos supportPos, boolean active) {
        if (level.isClientSide) {
            return;
        }
        BlockPos canopyPos = supportPos.above();
        BlockState canopyState = level.getBlockState(canopyPos);
        if (canopyState.is(ModBlocks.GRAFTING_CANOPY.get())) {
            if (canopyState.getValue(GraftingCanopyBlock.ACTIVE) != active) {
                level.setBlock(canopyPos, canopyState.setValue(GraftingCanopyBlock.ACTIVE, active), 3);
            }
            return;
        }
        if (canopyState.canBeReplaced()) {
            level.setBlock(
                    canopyPos,
                    ModBlocks.GRAFTING_CANOPY.get().defaultBlockState().setValue(GraftingCanopyBlock.ACTIVE, active),
                    3
            );
        }
    }

    @Override
    public ItemStack getCloneItemStack(
            BlockState state,
            HitResult target,
            BlockGetter level,
            BlockPos pos,
            Player player
    ) {
        return new ItemStack(ModBlocks.GRAFTING_SUPPORT_ITEM.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(ModBlocks.GRAFTING_SUPPORT_ITEM.get()));
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof GraftingSupportBlockEntity support && support.hasCanopy()) {
            ItemStack canopy = support.canopyStack();
            if (!canopy.isEmpty()) {
                drops.add(canopy);
            }
        }
        return List.copyOf(drops);
    }
}
