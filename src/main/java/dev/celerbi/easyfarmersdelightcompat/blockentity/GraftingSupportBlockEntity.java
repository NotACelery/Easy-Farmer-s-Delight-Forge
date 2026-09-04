package dev.celerbi.easyfarmersdelightcompat.blockentity;

import dev.celerbi.easyfarmersdelightcompat.integration.FarmerToolSupport;
import dev.celerbi.easyfarmersdelightcompat.integration.orchard.OrchardCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.orchard.OrchardCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;

public final class GraftingSupportBlockEntity extends BlockEntity {
    private static final ResourceLocation RICH_SOIL_ID = new ResourceLocation("farmersdelight", "rich_soil");
    private static final String KEY_CANOPY = "Canopy";
    private static final String KEY_DEFINITION = "OrchardDefinition";
    private static final String KEY_AGE = "OrchardAge";
    private static final String KEY_RENDER_BLOCK = "RenderBlock";
    private static final String KEY_AGE_PROPERTY = "AgeProperty";
    private static final String KEY_RENDER_STYLE = "RenderStyle";
    private static final String KEY_HARVEST_ITEM = "HarvestItem";
    private static final String KEY_MATURE_AGE = "MatureAge";

    private ItemStack canopy = ItemStack.EMPTY;
    private ResourceLocation orchardDefinitionId;
    private int orchardAge;
    private ResourceLocation renderBlockId;
    private String ageProperty = "";
    private OrchardCropDefinition.RenderStyle renderStyle = OrchardCropDefinition.RenderStyle.BLOCK_AGE;
    private ResourceLocation harvestItemId;
    private int matureAge = 3;

    public GraftingSupportBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRAFTING_SUPPORT.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1.0D, pos.getY() + 2.0D, pos.getZ() + 1.0D
        );
    }

    public boolean hasCanopy() { return !canopy.isEmpty(); }

    public ItemStack canopyStack() {
        return canopy.copyWithCount(canopy.isEmpty() ? 0 : 1);
    }

    public boolean canAcceptLeaves(ItemStack stack) {
        if (hasCanopy() || stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }
        return blockItem.getBlock().defaultBlockState().is(BlockTags.LEAVES);
    }

    public boolean insertCanopy(ItemStack stack) {
        if (!canAcceptLeaves(stack)) return false;
        canopy = stack.copyWithCount(1);
        OrchardCropDefinition definition = OrchardCropDefinitions.findPlanting(canopy).orElse(null);
        applyDefinition(definition);
        sync();
        return true;
    }

    public ItemStack removeCanopy() {
        if (!hasCanopy()) return ItemStack.EMPTY;
        ItemStack result = canopy.copyWithCount(1);
        clearCanopy();
        sync();
        return result;
    }

    public boolean isProductive() { return harvestItemId != null; }
    public boolean isMature() { return harvestItemId != null && orchardAge >= orchardMatureAge(); }
    public boolean canHarvestWith(ItemStack stack) {
        return hasCanopy() && isProductive() && isMature() && FarmerToolSupport.isShears(stack);
    }

    public List<ItemStack> harvest(ServerLevel level, Player player, InteractionHand hand, ItemStack shears) {
        OrchardCropDefinition definition = currentDefinition();
        if (definition == null || orchardAge < definition.matureAge() || !FarmerToolSupport.isShears(shears)) {
            return List.of();
        }
        ItemStack harvest = definition.harvestStack(level.random);
        orchardAge = definition.postHarvestAge();
        if (shears.isDamageableItem() && !player.getAbilities().instabuild) {
            shears.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(hand));
        }
        sync();
        level.playSound(null, worldPosition, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                SoundSource.BLOCKS, 1.0F, 0.95F + level.random.nextFloat() * 0.1F);
        return harvest.isEmpty() ? List.of() : List.of(harvest);
    }

    public void randomGrowthTick(ServerLevel level, RandomSource random) {
        OrchardCropDefinition definition = currentDefinition();
        if (definition == null || !isOnRichSoil() || orchardAge >= definition.matureAge()) return;
        orchardAge = Math.min(definition.matureAge(), Math.min(definition.maxAge(), orchardAge + 1));
        sync();
    }

    public int orchardAge() { return orchardAge; }
    public int orchardMatureAge() {
        OrchardCropDefinition definition = currentDefinition();
        return definition == null ? matureAge : definition.matureAge();
    }
    public OrchardCropDefinition.RenderStyle renderStyle() {
        OrchardCropDefinition definition = currentDefinition();
        return definition == null ? renderStyle : definition.renderStyle();
    }
    public ItemStack harvestDisplayStack() {
        OrchardCropDefinition definition = currentDefinition();
        if (definition != null) return definition.harvestDisplayStack();
        if (harvestItemId == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.get(harvestItemId);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public BlockState renderState() {
        OrchardCropDefinition definition = currentDefinition();
        if (definition != null) return definition.renderState(orchardAge);
        if (renderBlockId != null) {
            Block block = BuiltInRegistries.BLOCK.get(renderBlockId);
            if (block != null && block != Blocks.AIR) return withAgeProperty(block.defaultBlockState(), ageProperty, orchardAge);
        }
        if (canopy.getItem() instanceof BlockItem blockItem) return blockItem.getBlock().defaultBlockState();
        return Blocks.AIR.defaultBlockState();
    }

    public boolean isOnRichSoil() {
        Level level = getLevel();
        if (level == null) return false;
        Block richSoil = BuiltInRegistries.BLOCK.get(RICH_SOIL_ID);
        return richSoil != null && richSoil != Blocks.AIR && level.getBlockState(worldPosition.below()).is(richSoil);
    }

    private void applyDefinition(OrchardCropDefinition definition) {
        if (definition == null) {
            orchardDefinitionId = null;
            orchardAge = 0;
            renderBlockId = canopy.getItem() instanceof BlockItem blockItem
                    ? BuiltInRegistries.BLOCK.getKey(blockItem.getBlock()) : null;
            ageProperty = "";
            renderStyle = OrchardCropDefinition.RenderStyle.BLOCK_AGE;
            harvestItemId = null;
            matureAge = 3;
            return;
        }
        orchardDefinitionId = definition.id();
        orchardAge = definition.minAge();
        renderBlockId = definition.renderBlockId();
        ageProperty = definition.ageProperty();
        renderStyle = definition.renderStyle();
        harvestItemId = definition.harvestItemId();
        matureAge = definition.matureAge();
    }

    private OrchardCropDefinition currentDefinition() {
        return OrchardCropDefinitions.get(orchardDefinitionId).orElse(null);
    }

    private void clearCanopy() {
        canopy = ItemStack.EMPTY;
        orchardDefinitionId = null;
        orchardAge = 0;
        renderBlockId = null;
        ageProperty = "";
        renderStyle = OrchardCropDefinition.RenderStyle.BLOCK_AGE;
        harvestItemId = null;
        matureAge = 3;
    }

    private static BlockState withAgeProperty(BlockState state, String propertyName, int age) {
        if (propertyName == null || propertyName.isBlank()) return state;
        Property<?> raw = state.getProperties().stream()
                .filter(property -> property.getName().equals(propertyName)).findFirst().orElse(null);
        if (!(raw instanceof IntegerProperty integerProperty)) return state;
        int min = integerProperty.getPossibleValues().stream().min(Integer::compareTo).orElse(age);
        int max = integerProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(age);
        return state.setValue(integerProperty, Math.max(min, Math.min(max, age)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!canopy.isEmpty()) tag.put(KEY_CANOPY, canopy.save(new CompoundTag()));
        if (orchardDefinitionId != null) {
            tag.putString(KEY_DEFINITION, orchardDefinitionId.toString());
            tag.putInt(KEY_AGE, orchardAge);
        }
        if (renderBlockId != null) {
            tag.putString(KEY_RENDER_BLOCK, renderBlockId.toString());
            tag.putString(KEY_AGE_PROPERTY, ageProperty == null ? "" : ageProperty);
            tag.putString(KEY_RENDER_STYLE, renderStyle.name());
            tag.putInt(KEY_MATURE_AGE, matureAge);
            if (harvestItemId != null) tag.putString(KEY_HARVEST_ITEM, harvestItemId.toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        canopy = tag.contains(KEY_CANOPY, Tag.TAG_COMPOUND) ? ItemStack.of(tag.getCompound(KEY_CANOPY)) : ItemStack.EMPTY;
        orchardDefinitionId = tag.contains(KEY_DEFINITION) ? ResourceLocation.tryParse(tag.getString(KEY_DEFINITION)) : null;
        orchardAge = orchardDefinitionId == null ? 0 : Math.max(0, tag.getInt(KEY_AGE));
        renderBlockId = tag.contains(KEY_RENDER_BLOCK) ? ResourceLocation.tryParse(tag.getString(KEY_RENDER_BLOCK)) : null;
        ageProperty = tag.contains(KEY_AGE_PROPERTY) ? tag.getString(KEY_AGE_PROPERTY) : "";
        if (tag.contains(KEY_RENDER_STYLE)) {
            try {
                renderStyle = OrchardCropDefinition.RenderStyle.valueOf(tag.getString(KEY_RENDER_STYLE));
            } catch (IllegalArgumentException ignored) {
                renderStyle = OrchardCropDefinition.RenderStyle.BLOCK_AGE;
            }
        } else {
            renderStyle = OrchardCropDefinition.RenderStyle.BLOCK_AGE;
        }
        harvestItemId = tag.contains(KEY_HARVEST_ITEM) ? ResourceLocation.tryParse(tag.getString(KEY_HARVEST_ITEM)) : null;
        matureAge = tag.contains(KEY_MATURE_AGE) ? Math.max(1, tag.getInt(KEY_MATURE_AGE)) : 3;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    private void sync() {
        setChanged();
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }
}
