package dev.celerbi.easyfarmersdelightcompat.block;

import dev.celerbi.easyfarmersdelightcompat.blockentity.GraftingSupportBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmerToolSupport;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public final class GraftingCanopyBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final VoxelShape CANOPY_SHAPE = Block.box(2.5D, 0.0D, 2.5D, 13.5D, 7.0D, 13.5D);

    public GraftingCanopyBlock(Properties properties) {
        super(properties);
        // Keep old marker states active by default; new empty supports explicitly place inactive markers.
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    private static VoxelShape canopyShape(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getValue(ACTIVE)
                && level.getBlockEntity(pos.below()) instanceof GraftingSupportBlockEntity support
                && support.hasCanopy()) {
            return CANOPY_SHAPE;
        }
        return Shapes.empty();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return canopyShape(state, level, pos);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return canopyShape(state, level, pos);
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return canopyShape(state, level, pos);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return canopyShape(state, level, pos);
    }

    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return canopyShape(state, level, pos);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private static BlockState renderedCanopyState(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos.below()) instanceof GraftingSupportBlockEntity support && support.hasCanopy()) {
            return support.renderState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        BlockState rendered = renderedCanopyState(level, pos);
        return rendered.isAir()
                ? super.getDestroyProgress(state, player, level, pos)
                : rendered.getDestroyProgress(player, level, pos);
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                BlockState rendered = renderedCanopyState(level, pos);
                if (!rendered.isAir()) {
                    manager.destroy(pos, rendered);
                }
                return true;
            }

            @Override
            public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
                if (!(level instanceof ClientLevel clientLevel) || !(target instanceof BlockHitResult hit)) {
                    return true;
                }
                BlockState rendered = renderedCanopyState(level, hit.getBlockPos());
                if (rendered.isAir()) {
                    return true;
                }

                IClientBlockExtensions leafExtensions = IClientBlockExtensions.of(rendered);
                if (leafExtensions.addHitEffects(rendered, level, target, manager)) {
                    return true;
                }

                AABB bounds = CANOPY_SHAPE.bounds();
                RandomSource random = level.random;
                double margin = 0.1D;
                double x = hit.getBlockPos().getX() + bounds.minX + margin
                        + random.nextDouble() * Math.max(0.0D, bounds.getXsize() - margin * 2.0D);
                double y = hit.getBlockPos().getY() + bounds.minY + margin
                        + random.nextDouble() * Math.max(0.0D, bounds.getYsize() - margin * 2.0D);
                double z = hit.getBlockPos().getZ() + bounds.minZ + margin
                        + random.nextDouble() * Math.max(0.0D, bounds.getZsize() - margin * 2.0D);

                switch (hit.getDirection()) {
                    case DOWN -> y = hit.getBlockPos().getY() + bounds.minY - margin;
                    case UP -> y = hit.getBlockPos().getY() + bounds.maxY + margin;
                    case NORTH -> z = hit.getBlockPos().getZ() + bounds.minZ - margin;
                    case SOUTH -> z = hit.getBlockPos().getZ() + bounds.maxZ + margin;
                    case WEST -> x = hit.getBlockPos().getX() + bounds.minX - margin;
                    case EAST -> x = hit.getBlockPos().getX() + bounds.maxX + margin;
                }

                TerrainParticle particle = new TerrainParticle(
                        clientLevel, x, y, z, 0.0D, 0.0D, 0.0D, rendered, hit.getBlockPos());
                // Forge 1.20.1 returns Particle from updateSprite, so update the concrete instance separately.
                particle.updateSprite(rendered, hit.getBlockPos());
                manager.add(particle.setPower(0.2F).scale(0.6F));
                return true;
            }

            @Override
            public boolean areBreakingParticlesTinted(BlockState state, ClientLevel level, BlockPos pos) {
                BlockState rendered = renderedCanopyState(level, pos);
                return !rendered.isAir()
                        && IClientBlockExtensions.of(rendered).areBreakingParticlesTinted(rendered, level, pos);
            }
        });
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
        return GraftingSupportBlock.interactAt(level, pos.below(), player, hand, player.getItemInHand(hand));
    }

    public static boolean removeCanopyForPlayer(Level level, BlockPos canopyPos, Player player) {
        if (level.isClientSide
                || !(level.getBlockEntity(canopyPos.below()) instanceof GraftingSupportBlockEntity support)
                || !support.hasCanopy()) {
            return false;
        }
        BlockState renderedCanopy = support.renderState();
        ItemStack canopy = support.removeCanopy();
        GraftingSupportBlock.setCanopyMarkerActive(level, canopyPos.below(), false);
        if (!renderedCanopy.isAir()) {
            level.levelEvent(player, 2001, canopyPos, Block.getId(renderedCanopy));
        }
        ItemStack tool = player.getMainHandItem();
        if (!player.getAbilities().instabuild && !canopy.isEmpty() && canRecoverCanopy(tool)) {
            Block.popResource(level, canopyPos, canopy);
        }
        return true;
    }

    public static boolean canRecoverCanopy(ItemStack tool) {
        if (FarmerToolSupport.isShears(tool)) {
            return true;
        }
        return tool != null
                && !tool.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (direction == Direction.DOWN && !neighborState.is(ModBlocks.GRAFTING_SUPPORT.get())) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public ItemStack getCloneItemStack(
            BlockState state,
            HitResult target,
            BlockGetter level,
            BlockPos pos,
            Player player
    ) {
        if (level.getBlockEntity(pos.below()) instanceof GraftingSupportBlockEntity support && support.hasCanopy()) {
            return support.canopyStack();
        }
        return new ItemStack(ModBlocks.GRAFTING_SUPPORT_ITEM.get());
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        return List.of();
    }
}
