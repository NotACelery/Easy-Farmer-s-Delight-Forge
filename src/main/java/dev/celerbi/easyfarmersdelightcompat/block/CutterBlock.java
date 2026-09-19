package dev.celerbi.easyfarmersdelightcompat.block;

import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.integration.CutterLogVariant;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmerToolSupport;
import dev.celerbi.easyfarmersdelightcompat.menu.CutterMenu;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;

public final class CutterBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Match the hollow cage geometry used by Farmers and Noise Switches.
    // Leaving Cutter on Block's default full-cube shape makes the light engine
    // treat the cell differently when a solid block touches one of its faces,
    // which darkens the corresponding interior face of the cage.
    private static final VoxelShape CUTTER_SHAPE = Shapes.or(
            Block.box(0D, 0D, 0D, 16D, 1D, 16D),
            Block.box(0D, 15D, 0D, 16D, 16D, 16D),
            Block.box(0D, 0D, 0D, 1D, 16D, 16D),
            Block.box(15D, 0D, 0D, 16D, 16D, 16D),
            Block.box(0D, 0D, 0D, 16D, 16D, 1D),
            Block.box(0D, 0D, 15D, 16D, 16D, 16D)
    );
    public CutterBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext c) {
        return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState s, Rotation r) {
        return s.setValue(FACING, r.rotate(s.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState s, Mirror m) {
        return s.rotate(m.getRotation(s.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CUTTER_SHAPE;
    }

    @Override
    public float getShadeBrightness(BlockState s, BlockGetter l, BlockPos p) {
        return 1F;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p, BlockState s) {
        return new CutterBlockEntity(p, s);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l, BlockState s, BlockEntityType<T> t) {
        if (l.isClientSide || t != ModBlockEntities.CUTTER.get())
            return null;
        return (level, pos, state, be) -> {
            if (level instanceof net.minecraft.server.level.ServerLevel sl && be instanceof CutterBlockEntity c)
                CutterBlockEntity.serverTick(sl, pos, state, c);
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(level.getBlockEntity(pos) instanceof CutterBlockEntity cutter))
            return InteractionResult.PASS;
        if (!player.isShiftKeyDown() && FarmerToolSupport.isCuttingTool(held) && cutter.toolHandler().getStackInSlot(0)
                .isEmpty()) {
            if (!level.isClientSide) {
                ItemStack one = held.copyWithCount(1);
                if (cutter.toolHandler().insertItem(0, one, false).isEmpty()) {
                    if (!player.getAbilities().instabuild)
                        held.shrink(1);
                    level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, .7F, 1F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player.isShiftKeyDown() && cutter.hasVillager()) {
            if (!level.isClientSide) {
                ItemStack removed = cutter.removeVillager();
                if (!removed.isEmpty())
                    ItemHandlerHelper.giveItemToPlayer(player, removed);
                level.playSound(null, pos, SoundEvents.VILLAGER_CELEBRATE, SoundSource.BLOCKS, 1F, 1F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!cutter.hasVillager() && cutter.isVillagerItem(held)) {
            if (!level.isClientSide && cutter.insertVillager(held)) {
                if (!player.getAbilities().instabuild)
                    held.shrink(1);
                level.playSound(null, pos, SoundEvents.VILLAGER_YES, SoundSource.BLOCKS, 1F, 1F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        openMenu(level, pos, player, cutter);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void openMenu(Level level, BlockPos pos, Player player, CutterBlockEntity cutter) {
        if (level.isClientSide || !(player instanceof ServerPlayer sp))
            return;
        NetworkHooks.openScreen(
                sp,
                new net.minecraft.world.SimpleMenuProvider(
                        (id, inv, ignored) -> new CutterMenu(id, inv, cutter),
                        Component.translatable("container.easyfarmersdelightcompat.cutter")),
                pos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
            Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        return be instanceof CutterBlockEntity c
                ? CutterLogVariant.createCutter(c.logVariant())
                : new ItemStack(ModBlocks.CUTTER_ITEM.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack dropped = new ItemStack(ModBlocks.CUTTER_ITEM.get());
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof CutterBlockEntity cutter) {
            CompoundTag data = cutter.saveWithoutMetadata();
            if (!data.isEmpty())
                BlockItem.setBlockEntityData(dropped, ModBlockEntities.CUTTER.get(), data);
        }
        return List.of(dropped);
    }
}
