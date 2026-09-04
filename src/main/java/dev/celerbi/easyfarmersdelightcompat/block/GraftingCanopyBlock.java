package dev.celerbi.easyfarmersdelightcompat.block;

import dev.celerbi.easyfarmersdelightcompat.blockentity.GraftingSupportBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmerToolSupport;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class GraftingCanopyBlock extends Block {
    private static final VoxelShape CANOPY_SHAPE = Block.box(2.5D, 0.0D, 2.5D, 13.5D, 7.0D, 13.5D);

    public GraftingCanopyBlock(Properties properties) { super(properties); }

    private static VoxelShape canopyShape(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos.below()) instanceof GraftingSupportBlockEntity support && support.hasCanopy()) return CANOPY_SHAPE;
        return Shapes.empty();
    }

    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return canopyShape(level, pos); }
    @Override public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return canopyShape(level, pos); }
    @Override public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) { return canopyShape(level, pos); }
    @Override public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return canopyShape(level, pos); }
    @Override public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) { return canopyShape(level, pos); }
    @Override public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) { return Shapes.empty(); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return GraftingSupportBlock.interactAt(level, pos.below(), player, hand, player.getItemInHand(hand));
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos.below()) instanceof GraftingSupportBlockEntity support && support.hasCanopy()) {
            ItemStack canopy = support.removeCanopy();
            ItemStack tool = player.getMainHandItem();
            if (!player.getAbilities().instabuild && !canopy.isEmpty() && canRecoverCanopy(tool)) {
                Block.popResource(level, pos, canopy);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    public static boolean canRecoverCanopy(ItemStack tool) {
        if (FarmerToolSupport.isShears(tool)) return true;
        return tool != null && !tool.isEmpty() && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !neighborState.is(ModBlocks.GRAFTING_SUPPORT.get())) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos.below()) instanceof GraftingSupportBlockEntity support && support.hasCanopy()) return support.canopyStack();
        return new ItemStack(ModBlocks.GRAFTING_SUPPORT_ITEM.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) { return List.of(); }
}
