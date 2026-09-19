package dev.celerbi.easyfarmersdelightcompat.block;

import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmerToolSupport;
import dev.celerbi.easyfarmersdelightcompat.menu.PaddyFarmerMenu;
import dev.celerbi.easyfarmersdelightcompat.menu.RichFarmerMenu;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraftforge.network.NetworkHooks;

public final class CompatFarmerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape FARMER_SHAPE = Shapes.or(
            Block.box(0D, 0D, 0D, 16D, 1D, 16D),
            Block.box(0D, 15D, 0D, 16D, 16D, 16D),
            Block.box(0D, 0D, 0D, 1D, 16D, 16D),
            Block.box(15D, 0D, 0D, 16D, 16D, 16D),
            Block.box(0D, 0D, 0D, 16D, 16D, 1D),
            Block.box(0D, 0D, 15D, 16D, 16D, 16D));

    private static final ResourceLocation RICE_ITEM_ID = new ResourceLocation(
            "farmersdelight", "rice");
    private static final ResourceLocation TOMATO_SEEDS_ID = new ResourceLocation(
            "farmersdelight", "tomato_seeds");
    private static final ResourceLocation ROPE_ITEM_ID = new ResourceLocation(
            "farmersdelight", "rope");
    private static final ResourceLocation RED_MUSHROOM_ITEM_ID = new ResourceLocation(
            "minecraft", "red_mushroom");
    private static final ResourceLocation BROWN_MUSHROOM_ITEM_ID = new ResourceLocation(
            "minecraft", "brown_mushroom");
    private final FarmerVariant variant;

    public CompatFarmerBlock(Properties properties, FarmerVariant variant) {
        super(properties);
        this.variant = variant;
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

    public FarmerVariant variant() {
        return variant;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FARMER_SHAPE;
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CompatFarmerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.COMPAT_FARMER.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) -> {
            if (tickLevel instanceof ServerLevel serverLevel
                    && blockEntity instanceof CompatFarmerBlockEntity farmer) {
                CompatFarmerBlockEntity.serverTick(serverLevel, tickPos, tickState, farmer);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack heldItem = player.getItemInHand(hand);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof CompatFarmerBlockEntity farmer)) {
            return InteractionResult.PASS;
        }

        var registries = level.registryAccess();

        if (player.isShiftKeyDown()) {
            handleShiftDismantle(level, pos, player, farmer);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (variant.isRich() && !variant.isAquatic()
                && farmer.canInstallGraftingSupport(heldItem)) {
            if (!level.isClientSide && farmer.installGraftingSupport(heldItem)) {
                consumeOne(heldItem, player);
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (variant.isRich() && !variant.isAquatic()
                && farmer.canPlantOrchardCrop(heldItem)) {
            if (!level.isClientSide && farmer.plantOrchardCrop(heldItem)) {
                consumeOne(heldItem, player);
                level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (variant.isRich() && !variant.isAquatic()
                && farmer.canInstallAttachedHost(heldItem)) {
            if (!level.isClientSide && farmer.installAttachedHost(heldItem)) {
                consumeOne(heldItem, player);
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (variant.isRich() && !variant.isAquatic()
                && farmer.canPlantAttachedCrop(heldItem)) {
            if (!level.isClientSide && farmer.plantAttachedCrop(heldItem)) {
                consumeOne(heldItem, player);
                level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (variant.isRich() && !variant.isAquatic()
                && farmer.shouldWarnIncompatibleAttachedCrop(heldItem)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(
                        "message.easyfarmersdelightcompat.attached_crop.incompatible_host"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (variant.isRich() && FarmerToolSupport.isHarvestTool(heldItem) && farmer
                .getHarvestTool().isEmpty()) {
            if (!level.isClientSide) {
                farmer.setHarvestTool(heldItem.copyWithCount(1));
                consumeOne(heldItem, player);
                level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 0.7F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (variant.isAquatic() && !farmer.hasPaddySand() && farmer.easyVillagers().getCrop(
                    registries) == null && isSand(heldItem)) {
            if (!level.isClientSide && farmer.installPaddySand()) {
                consumeOne(heldItem, player);
                level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (variant.isAquatic() && farmer.hasPaddySand() && farmer.sugarCaneHeight() == 0
                && isSugarCane(heldItem)) {
            if (!level.isClientSide && farmer.plantSugarCane()) {
                consumeOne(heldItem, player);
                level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!farmer.easyVillagers().hasVillager(registries) && farmer.easyVillagers().isVillagerItem(heldItem)) {
            if (!level.isClientSide) {
                farmer.easyVillagers().insertVillager(heldItem, registries);
                consumeOne(heldItem, player);
                farmer.setChanged();
                level.playSound(null, pos, SoundEvents.VILLAGER_YES, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!variant.isAquatic() && variant.isRich() && farmer.hasTomatoCrop(registries)) {
            if (isRope(heldItem) && farmer.ropeCount() < 2) {
                if (!level.isClientSide && farmer.addRope()) {
                    consumeOne(heldItem, player);
                    level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.8F, 1.0F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        if (variant.isRich() && !variant.isAquatic()
                && !farmer.hasGraftingSupport() && farmer.canSelectTallCrop(heldItem)) {
            if (!level.isClientSide && farmer.selectTallCrop(heldItem, registries)) {
                consumeOne(heldItem, player);
                level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (variant.isRich() && !variant.isAquatic()
                && !farmer.hasGraftingSupport() && farmer.canSelectRegrowingCrop(heldItem)) {
            if (!level.isClientSide && farmer.selectRegrowingCrop(heldItem, registries)) {
                consumeOne(heldItem, player);
                level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (farmer.easyVillagers().getCrop(registries) == null
                && !farmer.hasAttachedSetup()
                && !farmer.hasGraftingSupport()) {
            boolean validCrop = variant.isAquatic() ? (!farmer.hasPaddySand() && isRice(heldItem)) : (variant.isRich()
                    && (isTomatoSeeds(heldItem) || isMushroom(heldItem)
                    || farmer.canSelectStem(heldItem) || farmer.canSelectGenericCrop(heldItem))) || farmer
                .easyVillagers().isValidSeed(heldItem, registries);
            if (validCrop) {
                if (!level.isClientSide) {
                    boolean selected;
                    if (variant.isAquatic()) {
                        farmer.selectRice(registries);
                        selected = true;
                    } else if (variant.isRich() && isTomatoSeeds(heldItem)) {
                        farmer.selectTomato(registries);
                        selected = true;
                    } else if (variant.isRich() && isMushroom(heldItem)) {
                        selected = farmer.selectMushroom(heldItem, registries);
                    } else if (variant.isRich() && farmer.canSelectStem(heldItem)) {
                        selected = farmer.selectStem(heldItem, registries);
                    } else {
                        selected = farmer.easyVillagers().setCropFromSeed(heldItem, registries);
                        if (selected) {
                            farmer.onNormalCropSelected(heldItem);
                        } else if (variant.isRich() && farmer.canSelectGenericCrop(heldItem)) {
                            selected = farmer.selectGenericCrop(heldItem, registries);
                        }
                    }
                    if (selected) {
                        consumeOne(heldItem, player);
                        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        openOutput(level, pos, player, farmer, state);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void handleShiftDismantle(
            Level level,
            BlockPos pos,
            Player player,
            CompatFarmerBlockEntity farmer
    ) {
        var registries = level.registryAccess();

        if (variant.isRich() && !variant.isAquatic() && farmer.hasGraftingSupport()) {
            if (!level.isClientSide) {
                for (ItemStack returned : farmer.dismantleOrchardStep()) {
                    giveOrDrop(player, returned);
                }
                level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return;
        }

        if (variant.isRich() && !variant.isAquatic() && farmer.hasAttachedSetup()) {
            if (!level.isClientSide) {
                for (ItemStack returned : farmer.dismantleAttachedStep()) {
                    giveOrDrop(player, returned);
                }
                level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return;
        }

        if (variant.isAquatic()) {
            if (farmer.hasPaddySand()) {
                if (!level.isClientSide) {
                    for (ItemStack returned : farmer.dismantleSugarCaneMode()) {
                        giveOrDrop(player, returned);
                    }
                    level.playSound(null, pos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
                }
                return;
            }
            if (farmer.easyVillagers().getCrop(registries) != null) {
                if (!level.isClientSide) {
                    giveOrDrop(player, farmer.removeSelectedCrop(registries));
                    level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return;
            }
            if (farmer.easyVillagers().hasVillager(registries)) {
                if (!level.isClientSide) {
                    giveOrDrop(player, farmer.easyVillagers().removeVillager(registries));
                    farmer.setChanged();
                    level.playSound(null, pos, SoundEvents.VILLAGER_CELEBRATE, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return;
            }
            return;
        }

        if (variant.isRich() && farmer.hasTomatoCrop(registries) && farmer.ropeCount() > 0) {
            if (!level.isClientSide) {
                giveOrDrop(player, farmer.removeTopRope());
                level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
            }
            return;
        }

        if (farmer.easyVillagers().getCrop(registries) != null) {
            if (!level.isClientSide) {
                giveOrDrop(player, farmer.removeSelectedCrop(registries));
                level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return;
        }

        if (farmer.easyVillagers().hasVillager(registries) && !level.isClientSide) {
            giveOrDrop(player, farmer.easyVillagers().removeVillager(registries));
            farmer.setChanged();
            level.playSound(null, pos, SoundEvents.VILLAGER_CELEBRATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStack remainder = stack.copy();
        player.getInventory().add(remainder);
        if (!remainder.isEmpty()) {
            player.drop(remainder, false);
        }
    }

    private void openOutput(
            Level level,
            BlockPos pos,
            Player player,
            CompatFarmerBlockEntity farmer,
            BlockState state) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        SimpleMenuProvider provider = new SimpleMenuProvider(
                (id, inventory, menuPlayer) -> farmer.variant().isRich()
                        ? new RichFarmerMenu(id, inventory, farmer)
                        : new PaddyFarmerMenu(id, inventory, farmer),
                Component.translatable(state.getBlock().getDescriptionId()));
        NetworkHooks.openScreen(serverPlayer, provider, pos);
    }

    private static boolean isRice(ItemStack stack) {
        return RICE_ITEM_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static boolean isTomatoSeeds(ItemStack stack) {
        return TOMATO_SEEDS_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static boolean isRope(ItemStack stack) {
        return ROPE_ITEM_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static boolean isStemSeed(ItemStack stack) {
        return stack.is(Items.MELON_SEEDS) || stack.is(Items.PUMPKIN_SEEDS);
    }

    private static boolean isSand(ItemStack stack) {
        return stack.is(Items.SAND);
    }

    private static boolean isSugarCane(ItemStack stack) {
        return stack.is(Items.SUGAR_CANE);
    }

    private static boolean isMushroom(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return RED_MUSHROOM_ITEM_ID.equals(itemId) || BROWN_MUSHROOM_ITEM_ID.equals(itemId);
    }

    private static void consumeOne(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof CompatFarmerBlockEntity compatFarmer
                && compatFarmer.hasStoredContents(params.getLevel().registryAccess())) {
            compatFarmer.saveToItem(stack);
        }
        return List.of(stack);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
            Player player) {
        return new ItemStack(this);
    }
}
