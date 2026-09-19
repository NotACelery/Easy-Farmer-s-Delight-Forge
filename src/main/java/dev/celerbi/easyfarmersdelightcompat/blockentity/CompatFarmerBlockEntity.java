package dev.celerbi.easyfarmersdelightcompat.blockentity;

import dev.celerbi.easyfarmersdelightcompat.block.CompatFarmerBlock;
import dev.celerbi.easyfarmersdelightcompat.block.FarmerVariant;
import dev.celerbi.easyfarmersdelightcompat.integration.EasyVillagersFarmerAdapter;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmerToolSupport;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmersDelightAdapter;
import dev.celerbi.easyfarmersdelightcompat.integration.ReflectionCache;
import dev.celerbi.easyfarmersdelightcompat.integration.ToolRequirement;
import dev.celerbi.easyfarmersdelightcompat.integration.attached.AttachedCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.attached.AttachedCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.integration.regrowing.RegrowingCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.regrowing.RegrowingCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.integration.stem.StemCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.stem.StemCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.integration.tall.TallCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.tall.TallCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.integration.orchard.OrchardCropDefinition;
import dev.celerbi.easyfarmersdelightcompat.integration.orchard.OrchardCropDefinitions;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public final class CompatFarmerBlockEntity extends BlockEntity {
    private static final String KEY_SCHEMA = "EfdcSchema";
    private static final String KEY_PADDY_GROWTH = "EfdcPaddyGrowth";
    private static final String KEY_BASE_PROGRESS = "EfdcBaseProgress";
    private static final String KEY_ROPE_ONE_PROGRESS = "EfdcRopeOneProgress";
    private static final String KEY_ROPE_TWO_PROGRESS = "EfdcRopeTwoProgress";
    private static final String KEY_ROPE_ONE_PLANTED = "EfdcRopeOnePlanted";
    private static final String KEY_ROPE_TWO_PLANTED = "EfdcRopeTwoPlanted";
    private static final String KEY_ROPE_COUNT = "EfdcRopeCount";
    private static final String KEY_HARVEST_TOOL = "EfdcHarvestTool";
    private static final String LEGACY_EFDC_KNIFE = "EfdcKnife";
    private static final String KEY_FRUIT_READY = "EfdcFruitReady";
    private static final String KEY_PADDY_SAND = "EfdcPaddySand";
    private static final String KEY_SUGAR_CANE_HEIGHT = "EfdcSugarCaneHeight";
    private static final String KEY_SUGAR_CANE_AGE = "EfdcSugarCaneAge";
    private static final String KEY_ATTACHED_HOSTS = "EfdcAttachedHosts";
    private static final String KEY_ATTACHED_CROPS = "EfdcAttachedCrops";
    private static final String KEY_REGROWING_DEFINITION = "EfdcRegrowingDefinition";
    private static final String KEY_REGROWING_PLANTING_ITEM = "EfdcRegrowingPlantingItem";
    private static final String KEY_SELECTED_PLANTING_ITEM = "EfdcSelectedPlantingItem";
    private static final String KEY_STEM_DEFINITION = "EfdcStemDefinition";
    private static final String KEY_TALL_DEFINITION = "EfdcTallDefinition";
    private static final String KEY_STEM_FRUIT = "EfdcStemFruit";
    private static final String KEY_NOCTURNAL_MILLET_PANICLE_AGE = "EfdcNocturnalMilletPanicleAge";
    private static final String KEY_HEARTH_CORN_MIDDLE_AGE = "EfdcHearthCornMiddleAge";
    private static final String KEY_HEARTH_CORN_TOP_AGE = "EfdcHearthCornTopAge";
    private static final String KEY_GRAFTING_SUPPORT = "EfdcGraftingSupport";
    private static final String KEY_ORCHARD_DEFINITION = "EfdcOrchardDefinition";
    private static final String KEY_ORCHARD_PLANTING_ITEM = "EfdcOrchardPlantingItem";
    private static final String KEY_ORCHARD_AGE = "EfdcOrchardAge";
    private static final String KEY_ORCHARD_RENDER_BLOCK = "EfdcOrchardRenderBlock";
    private static final String KEY_ORCHARD_AGE_PROPERTY = "EfdcOrchardAgeProperty";
    private static final String KEY_ORCHARD_RENDER_STYLE = "EfdcOrchardRenderStyle";
    private static final String KEY_ORCHARD_HARVEST_ITEM = "EfdcOrchardHarvestItem";
    private static final String KEY_ORCHARD_MATURE_AGE = "EfdcOrchardMatureAge";
    private static final String KEY_ORCHARD_PENDING_HARVEST = "EfdcOrchardPendingHarvest";

    private static final ResourceLocation RICE_ITEM_ID = new ResourceLocation("farmersdelight",
            "rice");
    private static final ResourceLocation RICE_CROP_ID = new ResourceLocation("farmersdelight",
            "rice");
    private static final ResourceLocation RICE_PANICLES_ID = new ResourceLocation("farmersdelight",
            "rice_panicles");
    private static final ResourceLocation TOMATO_SEEDS_ID = new ResourceLocation("farmersdelight",
            "tomato_seeds");
    private static final ResourceLocation BUDDING_TOMATO_ID = new ResourceLocation("farmersdelight",
            "budding_tomatoes");
    private static final ResourceLocation TOMATO_CROP_ID = new ResourceLocation("farmersdelight",
            "tomatoes");
    private static final ResourceLocation TOMATO_ON_ROPE_ID = new ResourceLocation("farmersdelight",
            "tomatoes_on_rope");
    private static final ResourceLocation ROPE_ITEM_ID = new ResourceLocation("farmersdelight",
            "rope");
    private static final ResourceLocation RED_MUSHROOM_ITEM_ID = new ResourceLocation("red_mushroom");
    private static final ResourceLocation BROWN_MUSHROOM_ITEM_ID = new ResourceLocation("brown_mushroom");
    private static final ResourceLocation RED_MUSHROOM_COLONY_ID = new ResourceLocation(
            "farmersdelight", "red_mushroom_colony");
    private static final ResourceLocation BROWN_MUSHROOM_COLONY_ID = new ResourceLocation(
            "farmersdelight", "brown_mushroom_colony");
    private static final ResourceLocation COCOA_DEFINITION_ID = new ResourceLocation(
            "easyfarmersdelightcompat", "cocoa");
    private static final ResourceLocation NOCTURNAL_MILLET_SEEDS_ID = new ResourceLocation(
            "eternal_starlight", "nocturnal_millet_seeds");
    private static final ResourceLocation NOCTURNAL_MILLET_STALK_ID = new ResourceLocation(
            "eternal_starlight", "nocturnal_millet_stalk");
    private static final ResourceLocation NOCTURNAL_MILLET_PANICLE_ID = new ResourceLocation(
            "eternal_starlight", "nocturnal_millet_panicle");
    private static final ResourceLocation NOCTURNAL_MILLET_ITEM_ID = new ResourceLocation(
            "eternal_starlight", "nocturnal_millet");
    private static final ResourceLocation HEARTH_CORN_STALK_ID = new ResourceLocation("hearthandharvest", "corn_stalk");
    private static final ResourceLocation HEARTH_CORN_ITEM_ID = new ResourceLocation("hearthandharvest", "corn");
    private static final ResourceLocation HEARTH_CORN_KERNELS_ID = new ResourceLocation(
            "hearthandharvest", "corn_kernels");
    private static final ResourceLocation FORGOTTEN_NOCTURNAL_MILLET_ITEM_ID = new ResourceLocation(
            "eternal_starlight", "forgotten_nocturnal_millet");
    private static final ResourceLocation DEEP_AETHER_SQUASH_STEM_ID = new ResourceLocation(
            "deep_aether", "squash_stem");
    private static final ResourceLocation DEEP_AETHER_ATTACHED_SQUASH_STEM_ID = new ResourceLocation(
            "deep_aether", "attached_squash_stem");
    private static final ResourceLocation DEEP_AETHER_BLUE_SQUASH_ID = new ResourceLocation(
            "deep_aether", "blue_squash");
    private static final ResourceLocation DEEP_AETHER_GREEN_SQUASH_ID = new ResourceLocation(
            "deep_aether", "green_squash");
    private static final TagKey<Block> UNAFFECTED_BY_RICH_SOIL = TagKey.create(
            Registries.BLOCK,
            new ResourceLocation("farmersdelight", "unaffected_by_rich_soil"));

    private static final int MAX_PADDY_GROWTH = 7;
    private static final int MAX_SUGAR_CANE_AGE = 15;
    private static final int ATTACHED_LEVEL_COUNT = 2;
    private static final int ATTACHED_FACE_COUNT = 4;
    private static final Direction[] ATTACHED_FACES = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST
    };

    private final EasyVillagersFarmerAdapter easyVillagers = new EasyVillagersFarmerAdapter(this);
    private final FarmersDelightAdapter farmersDelight = new FarmersDelightAdapter();

    private CompoundTag passthroughData = new CompoundTag();
    private int paddyGrowth;
    private int baseProgress;
    private int ropeOneProgress;
    private int ropeTwoProgress;
    private boolean ropeOnePlanted;
    private boolean ropeTwoPlanted;
    private int ropeCount;
    private boolean fruitReady;
    private boolean paddySand;
    private int sugarCaneHeight;
    private int sugarCaneAge;
    private ItemStack harvestTool = ItemStack.EMPTY;
    private final ResourceLocation[] attachedHostIds = new ResourceLocation[ATTACHED_LEVEL_COUNT];
    private final ResourceLocation[][] attachedDefinitionIds =
            new ResourceLocation[ATTACHED_LEVEL_COUNT][ATTACHED_FACE_COUNT];
    private final ResourceLocation[][] attachedCropIds =
            new ResourceLocation[ATTACHED_LEVEL_COUNT][ATTACHED_FACE_COUNT];
    private final ResourceLocation[][] attachedPlantingItemIds =
            new ResourceLocation[ATTACHED_LEVEL_COUNT][ATTACHED_FACE_COUNT];
    private final String[][] attachedAgeProperties = new String[ATTACHED_LEVEL_COUNT][ATTACHED_FACE_COUNT];
    private final String[][] attachedFacingProperties = new String[ATTACHED_LEVEL_COUNT][ATTACHED_FACE_COUNT];
    private final int[][] attachedCropAges = new int[ATTACHED_LEVEL_COUNT][ATTACHED_FACE_COUNT];
    private boolean itemPreview;
    private ResourceLocation regrowingDefinitionId;
    private ResourceLocation regrowingPlantingItemId;
    private ResourceLocation selectedPlantingItemId;
    private ResourceLocation stemDefinitionId;
    private ResourceLocation tallDefinitionId;
    private ResourceLocation stemFruitId;
    private int nocturnalMilletPanicleAge = -1;
    private int hearthCornMiddleAge = -1;
    private int hearthCornTopAge = -1;
    private boolean graftingSupport;
    private ResourceLocation orchardDefinitionId;
    private ResourceLocation orchardPlantingItemId;
    private ResourceLocation orchardRenderBlockId;
    private String orchardAgeProperty = "";
    private OrchardCropDefinition.RenderStyle orchardRenderStyle = OrchardCropDefinition.RenderStyle.BLOCK_AGE;
    private ResourceLocation orchardHarvestItemId;
    private int orchardMatureAge = 3;
    private int orchardAge;
    private ItemStack orchardPendingHarvest = ItemStack.EMPTY;
    private boolean harvestRetryRequested = true;
    private boolean harvestTransactionActive;
    private boolean harvestStateChanged;
    private boolean harvestWaitingForOutputSpace;
    private boolean harvestWaitingForTool;
    private boolean harvestWaitingForAdultVillager;
    private List<ItemStack> blockedOutputRequirement = List.of();
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.empty();

    public CompatFarmerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPAT_FARMER.get(), pos, state);
    }

    public FarmerVariant variant() {
        if (getBlockState().getBlock() instanceof CompatFarmerBlock block) {
            return block.variant();
        }
        return FarmerVariant.PADDY;
    }

    public EasyVillagersFarmerAdapter easyVillagers() {
        return easyVillagers;
    }

    public CompoundTag passthroughDataCopy() {
        return passthroughData.copy();
    }

    public boolean isItemPreview() {
        return itemPreview;
    }

    public void setItemPreview(boolean itemPreview) {
        this.itemPreview = itemPreview;
    }

    public IItemHandler getItemHandler() {
        if (level == null) {
            return null;
        }
        return easyVillagers.getItemHandler(level.registryAccess());
    }

    public void markPersistentStateChanged() {
        super.setChanged();
    }

    public void syncVisibleState() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void requestHarvestRetry() {
        if (!harvestTransactionActive) {
            harvestRetryRequested = true;
        }
    }

    private void requestHarvestRetryForToolChange() {
        if (harvestWaitingForTool || harvestWaitingForOutputSpace) {
            requestHarvestRetry();
        }
    }

    public void onOutputInventoryChanged() {
        markPersistentStateChanged();
    }

    public void onOutputInventoryReduced() {
        markPersistentStateChanged();
        if (harvestTransactionActive || !harvestWaitingForOutputSpace || level == null || level.isClientSide) {
            return;
        }

        Container output = easyVillagers.getOutputInventory(level.registryAccess());
        if (output == null) {
            return;
        }

        if (blockedOutputRequirement.isEmpty()
                || hasGuaranteedEmptySlotCapacity(output, blockedOutputRequirement)
                || canFitAllPure(output, blockedOutputRequirement)) {
            requestHarvestRetry();
        }
    }

    public boolean supportsOrchard() {
        return variant().isRich() && !variant().isAquatic();
    }

    public boolean hasGraftingSupport() {
        return supportsOrchard() && graftingSupport;
    }

    public boolean hasOrchardCrop() {
        return hasGraftingSupport() && orchardDefinitionId != null;
    }

    public int orchardAge() {
        return orchardAge;
    }

    public int orchardMatureAge() {
        OrchardCropDefinition definition = currentOrchardDefinition();
        return definition == null ? Math.max(1, orchardMatureAge) : definition.matureAge();
    }

    public ItemStack orchardHarvestDisplayStack() {
        OrchardCropDefinition definition = currentOrchardDefinition();
        if (definition != null) {
            return definition.harvestDisplayStack();
        }
        if (orchardHarvestItemId == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(orchardHarvestItemId);
        return item == null || item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public BlockState orchardRenderState() {
        OrchardCropDefinition definition = currentOrchardDefinition();
        if (definition != null) {
            return definition.renderState(orchardAge);
        }
        if (orchardRenderBlockId == null) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = BuiltInRegistries.BLOCK.get(orchardRenderBlockId);
        if (block == null || block == Blocks.AIR) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState state = block.defaultBlockState();
        if (orchardAgeProperty == null || orchardAgeProperty.isBlank()) {
            return state;
        }
        Property<?> raw = state.getProperties().stream()
                .filter(property -> property.getName().equals(orchardAgeProperty))
                .findFirst()
                .orElse(null);
        if (raw instanceof IntegerProperty integerProperty
                && integerProperty.getPossibleValues().contains(orchardAge)) {
            return state.setValue(integerProperty, orchardAge);
        }
        return state;
    }

    public OrchardCropDefinition.RenderStyle orchardRenderStyle() {
        OrchardCropDefinition definition = currentOrchardDefinition();
        return definition == null ? orchardRenderStyle : definition.renderStyle();
    }

    public boolean canInstallGraftingSupport(ItemStack stack) {
        if (!supportsOrchard() || graftingSupport || stack == null || stack.isEmpty()
                || stack.getItem() != ModBlocks.GRAFTING_SUPPORT_ITEM.get() || hasAttachedSetup()) {
            return false;
        }
        return level == null || easyVillagers.getCrop(level.registryAccess()) == null;
    }

    public boolean installGraftingSupport(ItemStack stack) {
        if (!canInstallGraftingSupport(stack)) {
            return false;
        }
        graftingSupport = true;
        clearOrchardCropState();
        setChanged();
        return true;
    }

    public boolean canPlantOrchardCrop(ItemStack stack) {
        if (!hasGraftingSupport() || hasOrchardCrop() || stack == null || stack.isEmpty() || hasAttachedSetup()) {
            return false;
        }
        if (level != null && easyVillagers.getCrop(level.registryAccess()) != null) {
            return false;
        }
        return OrchardCropDefinitions.findPlanting(stack).isPresent();
    }

    public boolean plantOrchardCrop(ItemStack stack) {
        OrchardCropDefinition definition = OrchardCropDefinitions.findPlanting(stack).orElse(null);
        if (!canPlantOrchardCrop(stack) || definition == null) {
            return false;
        }
        orchardDefinitionId = definition.id();
        orchardPlantingItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        orchardRenderBlockId = definition.renderBlockId();
        orchardAgeProperty = definition.ageProperty();
        orchardRenderStyle = definition.renderStyle();
        orchardHarvestItemId = definition.harvestItemId();
        orchardMatureAge = definition.matureAge();
        orchardAge = definition.minAge();
        setChanged();
        return true;
    }

    public List<ItemStack> dismantleOrchardStep() {
        if (!hasGraftingSupport()) {
            return List.of();
        }
        if (hasOrchardCrop()) {
            ItemStack planting = orchardPlantingStack();
            clearOrchardCropState();
            setChanged();
            return planting.isEmpty() ? List.of() : List.of(planting);
        }
        graftingSupport = false;
        setChanged();
        return List.of(new ItemStack(ModBlocks.GRAFTING_SUPPORT_ITEM.get()));
    }

    private void clearOrchardCropState() {
        orchardDefinitionId = null;
        orchardPlantingItemId = null;
        orchardRenderBlockId = null;
        orchardAgeProperty = "";
        orchardRenderStyle = OrchardCropDefinition.RenderStyle.BLOCK_AGE;
        orchardHarvestItemId = null;
        orchardMatureAge = 3;
        orchardAge = 0;
        orchardPendingHarvest = ItemStack.EMPTY;
    }

    private ItemStack orchardPlantingStack() {
        if (orchardPlantingItemId != null) {
            Item item = BuiltInRegistries.ITEM.get(orchardPlantingItemId);
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
        }
        OrchardCropDefinition definition = currentOrchardDefinition();
        return definition == null ? ItemStack.EMPTY : definition.canonicalPlantingStack();
    }

    private static OrchardCropDefinition.RenderStyle parseOrchardRenderStyle(String value) {
        if (value == null || value.isBlank()) {
            return OrchardCropDefinition.RenderStyle.BLOCK_AGE;
        }
        try {
            return OrchardCropDefinition.RenderStyle.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return OrchardCropDefinition.RenderStyle.BLOCK_AGE;
        }
    }

    private OrchardCropDefinition currentOrchardDefinition() {
        return OrchardCropDefinitions.get(orchardDefinitionId).orElse(null);
    }

    public boolean supportsAttachedCrops() {
        return variant().isRich() && !variant().isAquatic();
    }

    public boolean hasAttachedSetup() {
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            if (attachedHostIds[levelIndex] != null) {
                return true;
            }
            for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                if (attachedCropIds[levelIndex][faceIndex] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public int attachedLevelCount() {
        return ATTACHED_LEVEL_COUNT;
    }

    public int attachedFaceCount() {
        return ATTACHED_FACE_COUNT;
    }

    public Direction attachedFace(int faceIndex) {
        if (faceIndex < 0 || faceIndex >= ATTACHED_FACE_COUNT) {
            return Direction.NORTH;
        }
        return ATTACHED_FACES[faceIndex];
    }

    public BlockState attachedHostState(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= ATTACHED_LEVEL_COUNT) {
            return Blocks.AIR.defaultBlockState();
        }
        ResourceLocation id = attachedHostIds[levelIndex];
        if (id == null) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block == null ? Blocks.AIR.defaultBlockState() : block.defaultBlockState();
    }

    public BlockState attachedCropState(int levelIndex, int faceIndex) {
        if (levelIndex < 0 || levelIndex >= ATTACHED_LEVEL_COUNT
                || faceIndex < 0 || faceIndex >= ATTACHED_FACE_COUNT) {
            return Blocks.AIR.defaultBlockState();
        }
        ResourceLocation cropId = attachedCropIds[levelIndex][faceIndex];
        if (cropId == null) {
            return Blocks.AIR.defaultBlockState();
        }
        Block cropBlock = BuiltInRegistries.BLOCK.get(cropId);
        if (cropBlock == null || cropBlock == Blocks.AIR) {
            return Blocks.AIR.defaultBlockState();
        }
        BlockState state = cropBlock.defaultBlockState();
        String ageProperty = attachedAgeProperties[levelIndex][faceIndex];
        if (ageProperty != null && !ageProperty.isBlank()) {
            state = withIntegerProperty(state, ageProperty, attachedCropAges[levelIndex][faceIndex]);
        }
        String facingProperty = attachedFacingProperties[levelIndex][faceIndex];
        if (facingProperty != null && !facingProperty.isBlank()) {
            state = withDirectionProperty(state, facingProperty, ATTACHED_FACES[faceIndex].getOpposite());
        }
        return state;
    }

    public int attachedCropAge(int levelIndex, int faceIndex) {
        if (levelIndex < 0 || levelIndex >= ATTACHED_LEVEL_COUNT
                || faceIndex < 0 || faceIndex >= ATTACHED_FACE_COUNT) {
            return 0;
        }
        return attachedCropAges[levelIndex][faceIndex];
    }

    public boolean canInstallAttachedHost(ItemStack stack) {
        if (!supportsAttachedCrops() || attachedHostFromItem(stack) == null) {
            return false;
        }
        if (level != null && easyVillagers.getCrop(level.registryAccess()) != null) {
            return false;
        }
        for (ResourceLocation id : attachedHostIds) {
            if (id == null) {
                return true;
            }
        }
        return false;
    }

    public boolean installAttachedHost(ItemStack stack) {
        Block host = attachedHostFromItem(stack);
        if (!canInstallAttachedHost(stack) || host == null) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(host);
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            if (attachedHostIds[levelIndex] == null) {
                attachedHostIds[levelIndex] = id;
                clearAttachedLevelCrops(levelIndex);
                setChanged();
                return true;
            }
        }
        return false;
    }

    public boolean canPlantAttachedCrop(ItemStack stack) {
        if (!supportsAttachedCrops() || stack == null || stack.isEmpty()) {
            return false;
        }
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            BlockState host = attachedHostState(levelIndex);
            if (host.isAir()) {
                continue;
            }
            if (AttachedCropDefinitions.findPlanting(stack, host).isEmpty()) {
                continue;
            }
            for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                if (attachedCropIds[levelIndex][faceIndex] == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean shouldWarnIncompatibleAttachedCrop(ItemStack stack) {
        if (!supportsAttachedCrops() || !AttachedCropDefinitions.isPlantingItem(stack)) {
            return false;
        }
        if (canPlantAttachedCrop(stack)) {
            return false;
        }
        return hasOpenAttachedFace();
    }

    private boolean hasOpenAttachedFace() {
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            if (attachedHostIds[levelIndex] == null) {
                continue;
            }
            for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                if (attachedCropIds[levelIndex][faceIndex] == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean plantAttachedCrop(ItemStack stack) {
        if (!canPlantAttachedCrop(stack)) {
            return false;
        }
        ResourceLocation plantingItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            BlockState host = attachedHostState(levelIndex);
            if (host.isAir()) {
                continue;
            }
            AttachedCropDefinition definition = AttachedCropDefinitions.findPlanting(stack, host).orElse(null);
            if (definition == null) {
                continue;
            }
            for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                if (attachedCropIds[levelIndex][faceIndex] != null) {
                    continue;
                }
                attachedDefinitionIds[levelIndex][faceIndex] = definition.id();
                attachedCropIds[levelIndex][faceIndex] = definition.cropBlockId();
                attachedPlantingItemIds[levelIndex][faceIndex] = plantingItemId;
                attachedAgeProperties[levelIndex][faceIndex] = definition.ageProperty();
                attachedFacingProperties[levelIndex][faceIndex] = definition.facingProperty();
                attachedCropAges[levelIndex][faceIndex] = definition.minAge();
                setChanged();
                return true;
            }
        }
        return false;
    }

    public List<ItemStack> dismantleAttachedStep() {
        if (!supportsAttachedCrops() || !hasAttachedSetup()) {
            return List.of();
        }

        for (int levelIndex = ATTACHED_LEVEL_COUNT - 1; levelIndex >= 0; levelIndex--) {
            if (attachedHostIds[levelIndex] == null) {
                clearAttachedLevelCrops(levelIndex);
                continue;
            }

            List<ItemStack> plantings = new ArrayList<>();
            for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                if (attachedCropIds[levelIndex][faceIndex] != null) {
                    ItemStack planting = attachedPlantingItem(levelIndex, faceIndex);
                    if (!planting.isEmpty()) {
                        plantings.add(planting);
                    }
                }
            }
            if (!plantings.isEmpty()) {
                clearAttachedLevelCrops(levelIndex);
                setChanged();
                return plantings;
            }

            Block host = BuiltInRegistries.BLOCK.get(attachedHostIds[levelIndex]);
            attachedHostIds[levelIndex] = null;
            clearAttachedLevelCrops(levelIndex);
            setChanged();
            return host == null || host == Blocks.AIR ? List.of() : List.of(new ItemStack(host));
        }
        return List.of();
    }

    private Block attachedHostFromItem(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        return AttachedCropDefinitions.acceptsHost(block.defaultBlockState()) ? block : null;
    }

    private ItemStack attachedPlantingItem(int levelIndex, int faceIndex) {
        ResourceLocation plantingId = attachedPlantingItemIds[levelIndex][faceIndex];
        if (plantingId != null) {
            Item item = BuiltInRegistries.ITEM.get(plantingId);
            if (item != null && item != Items.AIR) {
                return new ItemStack(item);
            }
        }
        ResourceLocation definitionId = attachedDefinitionIds[levelIndex][faceIndex];
        return AttachedCropDefinitions.get(definitionId)
                .map(AttachedCropDefinition::canonicalPlantingStack)
                .orElse(ItemStack.EMPTY);
    }

    private void clearAttachedLevelCrops(int levelIndex) {
        if (levelIndex < 0 || levelIndex >= ATTACHED_LEVEL_COUNT) {
            return;
        }
        for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
            attachedDefinitionIds[levelIndex][faceIndex] = null;
            attachedCropIds[levelIndex][faceIndex] = null;
            attachedPlantingItemIds[levelIndex][faceIndex] = null;
            attachedAgeProperties[levelIndex][faceIndex] = null;
            attachedFacingProperties[levelIndex][faceIndex] = null;
            attachedCropAges[levelIndex][faceIndex] = 0;
        }
    }

    public int paddyGrowth() {
        return paddyGrowth;
    }

    public int baseProgress() {
        return baseProgress;
    }

    public int ropeOneProgress() {
        return ropeOneProgress;
    }

    public int ropeTwoProgress() {
        return ropeTwoProgress;
    }

    public boolean ropeOnePlanted() {
        return ropeOnePlanted;
    }

    public boolean ropeTwoPlanted() {
        return ropeTwoPlanted;
    }

    public int ropeCount() {
        return ropeCount;
    }

    public ItemStack getHarvestTool() {
        return harvestTool.copy();
    }

    public void setHarvestTool(ItemStack stack) {
        ItemStack normalized = variant().isRich() ? FarmerToolSupport.normalizeHarvestTool(stack) : ItemStack.EMPTY;
        if (!ItemStack.isSameItemSameTags(harvestTool, normalized) || harvestTool.getCount() != normalized
                .getCount()) {
            harvestTool = normalized;
            syncVisibleState();
            requestHarvestRetryForToolChange();
        }
    }

    // Normal farmland crops (including Tomatoes and compatible modded crops) may use an optional Hoe.
    // Passing the Hoe into the loot context lets vanilla loot tables apply Fortune without making it required.
    private ItemStack normalCropHarvestTool() {
        return variant().isRich() && FarmerToolSupport.isHoe(harvestTool) ? harvestTool : ItemStack.EMPTY;
    }

    // Rice has its own Knife-aware harvest path and intentionally never falls back to Hoe Fortune.
    private ItemStack riceHarvestTool() {
        return variant().isRich() && FarmerToolSupport.isKnife(harvestTool) ? harvestTool : ItemStack.EMPTY;
    }

    // Melons and Pumpkins use their required Axe so their normal Fortune/Silk Touch loot rules are preserved.
    private ItemStack stemHarvestTool() {
        return variant().isRich() && FarmerToolSupport.isAxe(harvestTool) ? harvestTool : ItemStack.EMPTY;
    }

    public boolean fruitReady() {
        return fruitReady;
    }

    public int nocturnalMilletPanicleAge() {
        return nocturnalMilletPanicleAge;
    }

    public int hearthCornMiddleAge() {
        return hearthCornMiddleAge;
    }

    public int hearthCornTopAge() {
        return hearthCornTopAge;
    }

    public Block nocturnalMilletPanicleBlock() {
        Block block = BuiltInRegistries.BLOCK.get(NOCTURNAL_MILLET_PANICLE_ID);
        return block == null ? Blocks.AIR : block;
    }

    public Block stemFruitBlockForRender(BlockState stem) {
        if (!isStemState(stem)) {
            return Blocks.AIR;
        }
        if (stemFruitId != null) {
            Block stored = BuiltInRegistries.BLOCK.get(stemFruitId);
            if (stored != null && stored != Blocks.AIR) {
                return stored;
            }
        }
        if (stem.is(Blocks.MELON_STEM)) {
            return Blocks.MELON;
        }
        if (stem.is(Blocks.PUMPKIN_STEM)) {
            return Blocks.PUMPKIN;
        }
        StemCropDefinition definition = currentStemDefinition(stem);
        return definition == null ? Blocks.AIR : definition.firstFruit();
    }

    public Block attachedStemBlockForRender(BlockState stem) {
        if (!isStemState(stem)) {
            return Blocks.AIR;
        }
        if (stem.is(Blocks.MELON_STEM)) {
            return Blocks.ATTACHED_MELON_STEM;
        }
        if (stem.is(Blocks.PUMPKIN_STEM)) {
            return Blocks.ATTACHED_PUMPKIN_STEM;
        }
        if (DEEP_AETHER_SQUASH_STEM_ID.equals(BuiltInRegistries.BLOCK.getKey(stem.getBlock()))) {
            Block attached = BuiltInRegistries.BLOCK.get(DEEP_AETHER_ATTACHED_SQUASH_STEM_ID);
            if (attached != null && attached != Blocks.AIR) {
                return attached;
            }
        }
        StemCropDefinition definition = currentStemDefinition(stem);
        return definition == null ? Blocks.AIR : definition.attachedStemBlock();
    }

    public ItemStack cropDisplayStack(RegistryAccess registries) {
        BlockState crop = easyVillagers.getCrop(registries);
        if (crop == null) {
            return ItemStack.EMPTY;
        }
        if (isNocturnalMilletState(crop)) {
            ResourceLocation harvestId = isForgottenNocturnalMillet(crop)
                    ? FORGOTTEN_NOCTURNAL_MILLET_ITEM_ID
                    : NOCTURNAL_MILLET_ITEM_ID;
            Item millet = BuiltInRegistries.ITEM.get(harvestId);
            return millet == null || millet == Items.AIR ? ItemStack.EMPTY : new ItemStack(millet);
        }
        TallCropDefinition tall = currentTallDefinition(crop);
        if (tall != null) {
            ItemStack harvest = tall.harvestDisplayStack();
            if (!harvest.isEmpty()) {
                return harvest;
            }
        }
        RegrowingCropDefinition regrowing = currentRegrowingDefinition(crop);
        if (regrowing != null) {
            ItemStack harvest = regrowing.harvestDisplayStack();
            if (!harvest.isEmpty()) {
                return harvest;
            }
        }
        if (isStemState(crop)) {
            Block fruit = Blocks.AIR;
            if (crop.is(Blocks.MELON_STEM)) {
                fruit = Blocks.MELON;
            } else if (crop.is(Blocks.PUMPKIN_STEM)) {
                fruit = Blocks.PUMPKIN;
            } else if (fruitReady && stemFruitId != null) {
                fruit = BuiltInRegistries.BLOCK.get(stemFruitId);
            }
            if (fruit != null && fruit != Blocks.AIR && fruit.asItem() != Items.AIR) {
                return new ItemStack(fruit.asItem());
            }
        }
        if (selectedPlantingItemId != null) {
            Item planting = BuiltInRegistries.ITEM.get(selectedPlantingItemId);
            if (planting != null && planting != Items.AIR) {
                return new ItemStack(planting);
            }
        }
        Item blockItem = crop.getBlock().asItem();
        return blockItem == Items.AIR ? ItemStack.EMPTY : new ItemStack(blockItem);
    }

    public ToolRequirement currentToolRequirement() {
        if (!variant().isRich() || level == null) {
            return ToolRequirement.NONE;
        }

        OrchardCropDefinition orchardDefinition = currentOrchardDefinition();
        if (orchardDefinition != null && orchardAge >= orchardDefinition.matureAge()) {
            return ToolRequirement.SHEARS;
        }

        ToolRequirement attachedRequirement = currentAttachedToolRequirement();
        if (attachedRequirement.isRequired()) {
            return attachedRequirement;
        }

        BlockState crop = easyVillagers.getCrop(level.registryAccess());
        if (crop == null) {
            return ToolRequirement.NONE;
        }

        if (isMushroomColonyState(crop) && getAge(crop) >= maxAge(crop)) {
            return ToolRequirement.KNIFE;
        }
        if (isStemState(crop) && fruitReady) {
            return ToolRequirement.AXE;
        }
        return ToolRequirement.NONE;
    }

    private ToolRequirement currentAttachedToolRequirement() {
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                AttachedCropDefinition definition = AttachedCropDefinitions
                        .get(attachedDefinitionIds[levelIndex][faceIndex])
                        .orElse(null);
                if (definition == null
                        || attachedCropAges[levelIndex][faceIndex] < definition.matureAge()
                        || attachedToolSatisfied(definition)) {
                    continue;
                }
                return switch (definition.tool()) {
                    case NONE -> ToolRequirement.NONE;
                    case KNIFE -> ToolRequirement.KNIFE;
                    case HOE -> ToolRequirement.HOE;
                    case AXE -> ToolRequirement.AXE;
                };
            }
        }
        return ToolRequirement.NONE;
    }

    public boolean hasPaddySand() {
        return paddySand;
    }

    public int sugarCaneHeight() {
        return sugarCaneHeight;
    }

    public int sugarCaneAge() {
        return sugarCaneAge;
    }

    public boolean installPaddySand() {
        if (!variant().isAquatic() || paddySand)
            return false;
        if (level != null && easyVillagers.getCrop(level.registryAccess()) != null)
            return false;
        paddySand = true;
        sugarCaneHeight = 0;
        sugarCaneAge = 0;
        paddyGrowth = 0;
        setChanged();
        return true;
    }

    public boolean plantSugarCane() {
        if (!variant().isAquatic() || !paddySand || sugarCaneHeight != 0)
            return false;
        sugarCaneHeight = 1;
        sugarCaneAge = 0;
        setChanged();
        return true;
    }

    public List<ItemStack> dismantleSugarCaneMode() {
        if (!variant().isAquatic() || !paddySand)
            return List.of();
        List<ItemStack> returned = new ArrayList<>(2);
        returned.add(new ItemStack(Items.SAND));
        if (sugarCaneHeight > 0)
            returned.add(new ItemStack(Items.SUGAR_CANE, sugarCaneHeight));
        paddySand = false;
        sugarCaneHeight = 0;
        sugarCaneAge = 0;
        paddyGrowth = 0;
        setChanged();
        return returned;
    }

    public boolean hasTomatoCrop(RegistryAccess registries) {
        return isTomatoState(easyVillagers.getCrop(registries));
    }

    public boolean hasMushroomColony(RegistryAccess registries) {
        return isMushroomColonyState(easyVillagers.getCrop(registries));
    }

    public boolean addRope() {
        if (variant() != FarmerVariant.RICH || ropeCount >= 2) {
            return false;
        }
        ropeCount++;
        if (ropeCount == 1) {
            ropeOneProgress = 0;
            ropeOnePlanted = false;
        } else {
            ropeTwoProgress = 0;
            ropeTwoPlanted = false;
        }
        setChanged();
        return true;
    }

    public ItemStack removeTopRope() {
        if (ropeCount <= 0) {
            return ItemStack.EMPTY;
        }

        if (ropeCount == 2) {
            ropeTwoProgress = 0;
            ropeTwoPlanted = false;
        } else {
            ropeOneProgress = 0;
            ropeOnePlanted = false;
        }
        ropeCount--;
        setChanged();

        Item rope = BuiltInRegistries.ITEM.get(ROPE_ITEM_ID);
        return new ItemStack(rope);
    }

    public void selectRice(RegistryAccess registries) {
        tallDefinitionId = null;
        fruitReady = false;
        paddySand = false;
        sugarCaneHeight = 0;
        sugarCaneAge = 0;
        easyVillagers.setRiceCrop(registries);
        paddyGrowth = 0;
        syncRiceCropState(registries);
        setChanged();
    }

    public void selectTomato(RegistryAccess registries) {
        tallDefinitionId = null;
        fruitReady = false;
        Block buddingTomato = BuiltInRegistries.BLOCK.get(BUDDING_TOMATO_ID);
        easyVillagers.setCropState(withAge(buddingTomato.defaultBlockState(), 0), registries);
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
        ropeOnePlanted = false;
        ropeTwoPlanted = false;
        setChanged();
    }

    public boolean selectMushroom(ItemStack mushroomStack, RegistryAccess registries) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(mushroomStack.getItem());
        ResourceLocation colonyId;
        if (RED_MUSHROOM_ITEM_ID.equals(itemId)) {
            colonyId = RED_MUSHROOM_COLONY_ID;
        } else if (BROWN_MUSHROOM_ITEM_ID.equals(itemId)) {
            colonyId = BROWN_MUSHROOM_COLONY_ID;
        } else {
            return false;
        }

        Block colony = BuiltInRegistries.BLOCK.get(colonyId);
        if (BuiltInRegistries.BLOCK.getKey(colony).equals(new ResourceLocation("air"))) {
            return false;
        }

        tallDefinitionId = null;
        easyVillagers.setCropState(withAge(colony.defaultBlockState(), 0), registries);
        fruitReady = false;
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
        ropeOnePlanted = false;
        ropeTwoPlanted = false;
        ropeCount = 0;
        setChanged();
        return true;
    }

    public boolean canSelectStem(ItemStack seedStack) {
        if (!variant().isRich() || variant().isAquatic() || seedStack == null || seedStack.isEmpty()) {
            return false;
        }
        return seedStack.is(Items.MELON_SEEDS)
                || seedStack.is(Items.PUMPKIN_SEEDS)
                || StemCropDefinitions.findPlanting(seedStack).isPresent()
                || genericStemStateFromPlanting(seedStack) != null;
    }

    public boolean selectStem(ItemStack seedStack, RegistryAccess registries) {
        if (!canSelectStem(seedStack)) {
            return false;
        }

        BlockState stemState;
        StemCropDefinition definition = null;
        if (seedStack.is(Items.MELON_SEEDS)) {
            stemState = withAge(Blocks.MELON_STEM.defaultBlockState(), 0);
        } else if (seedStack.is(Items.PUMPKIN_SEEDS)) {
            stemState = withAge(Blocks.PUMPKIN_STEM.defaultBlockState(), 0);
        } else {
            definition = StemCropDefinitions.findPlanting(seedStack).orElse(null);
            stemState = definition == null ? genericStemStateFromPlanting(seedStack) : definition.initialState();
            if (stemState == null) {
                return false;
            }
        }

        if (stemState == null || stemState.isAir() || !(stemState.getBlock() instanceof StemBlock)) {
            return false;
        }

        easyVillagers.setCropState(stemState, registries);
        stemDefinitionId = definition == null ? null : definition.id();
        stemFruitId = null;
        selectedPlantingItemId = itemId(seedStack);
        regrowingDefinitionId = null;
        regrowingPlantingItemId = null;
        tallDefinitionId = null;
        resetCropProgress();
        setChanged();
        return true;
    }

    public boolean canSelectTallCrop(ItemStack stack) {
        if (!variant().isRich() || variant().isAquatic() || hasAttachedSetup()
                || stack == null || stack.isEmpty() || level == null) {
            return false;
        }
        if (easyVillagers.getCrop(level.registryAccess()) != null) {
            return false;
        }
        return TallCropDefinitions.findPlanting(stack).isPresent();
    }

    public boolean selectTallCrop(ItemStack stack, RegistryAccess registries) {
        if (!variant().isRich() || variant().isAquatic() || hasAttachedSetup()
                || stack == null || stack.isEmpty() || easyVillagers.getCrop(registries) != null) {
            return false;
        }
        TallCropDefinition definition = TallCropDefinitions.findPlanting(stack).orElse(null);
        if (definition == null) {
            return false;
        }
        BlockState crop = definition.initialState();
        if (crop == null || crop.isAir()) {
            return false;
        }
        easyVillagers.setCropState(crop, registries);
        tallDefinitionId = definition.id();
        selectedPlantingItemId = itemId(stack);
        regrowingDefinitionId = null;
        regrowingPlantingItemId = null;
        stemDefinitionId = null;
        resetCropProgress();
        setChanged();
        return true;
    }

    public BlockState tallCropUpperState(BlockState lowerState) {
        TallCropDefinition definition = currentTallDefinition(lowerState);
        return definition == null ? Blocks.AIR.defaultBlockState() : definition.upperState(lowerState);
    }

    public boolean canSelectRegrowingCrop(ItemStack stack) {
        if (!variant().isRich() || variant().isAquatic() || hasAttachedSetup()
                || stack == null || stack.isEmpty() || level == null) {
            return false;
        }
        if (easyVillagers.getCrop(level.registryAccess()) != null) {
            return false;
        }
        return RegrowingCropDefinitions.findPlanting(stack).isPresent()
                || genericRegrowingStateFromPlanting(stack) != null;
    }

    public boolean selectRegrowingCrop(ItemStack stack, RegistryAccess registries) {
        if (!variant().isRich() || variant().isAquatic() || hasAttachedSetup()
                || stack == null || stack.isEmpty()) {
            return false;
        }
        if (easyVillagers.getCrop(registries) != null) {
            return false;
        }

        RegrowingCropDefinition definition = RegrowingCropDefinitions.findPlanting(stack).orElse(null);
        BlockState crop = definition == null ? genericRegrowingStateFromPlanting(stack) : definition.initialState();
        if (crop == null || crop.isAir()) {
            return false;
        }

        easyVillagers.setCropState(crop, registries);
        regrowingDefinitionId = definition == null ? null : definition.id();
        regrowingPlantingItemId = itemId(stack);
        selectedPlantingItemId = regrowingPlantingItemId;
        stemDefinitionId = null;
        tallDefinitionId = null;
        resetCropProgress();
        setChanged();
        return true;
    }

    public boolean canSelectGenericCrop(ItemStack stack) {
        if (!variant().isRich() || variant().isAquatic() || hasAttachedSetup()
                || stack == null || stack.isEmpty() || level == null) {
            return false;
        }
        if (easyVillagers.getCrop(level.registryAccess()) != null) {
            return false;
        }
        return genericCropStateFromPlanting(stack) != null;
    }

    public boolean selectGenericCrop(ItemStack stack, RegistryAccess registries) {
        if (!variant().isRich() || variant().isAquatic() || stack == null || stack.isEmpty()) {
            return false;
        }
        BlockState crop = genericCropStateFromPlanting(stack);
        if (crop == null || crop.isAir()) {
            return false;
        }
        easyVillagers.setCropState(crop, registries);
        selectedPlantingItemId = itemId(stack);
        stemDefinitionId = null;
        tallDefinitionId = null;
        regrowingDefinitionId = null;
        regrowingPlantingItemId = null;
        resetCropProgress();
        setChanged();
        return true;
    }

    public void onNormalCropSelected() {
        onNormalCropSelected(ItemStack.EMPTY);
    }

    public void onNormalCropSelected(ItemStack plantingStack) {
        selectedPlantingItemId = itemId(plantingStack);
        if (selectedPlantingItemId == null && level != null) {
            BlockState selected = easyVillagers.getCrop(level.registryAccess());
            if (isHearthCornState(selected)) {
                selectedPlantingItemId = HEARTH_CORN_KERNELS_ID;
            }
        }
        stemDefinitionId = null;
        tallDefinitionId = null;
        regrowingDefinitionId = null;
        regrowingPlantingItemId = null;
        resetCropProgress();
        setChanged();
    }

    private void resetCropProgress() {
        fruitReady = false;
        stemFruitId = null;
        nocturnalMilletPanicleAge = -1;
        hearthCornMiddleAge = -1;
        hearthCornTopAge = -1;
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
        ropeOnePlanted = false;
        ropeTwoPlanted = false;
        ropeCount = 0;
    }

    private static ResourceLocation itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null || stack.getItem() == Items.AIR ? null : id;
    }

    private static BlockState genericCropStateFromPlanting(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        ResourceLocation plantingId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (NOCTURNAL_MILLET_SEEDS_ID.equals(plantingId)) {
            Block stalk = BuiltInRegistries.BLOCK.get(NOCTURNAL_MILLET_STALK_ID);
            if (stalk == null || stalk == Blocks.AIR) {
                return null;
            }
            return withAge(stalk.defaultBlockState(), 0);
        }
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        if (!(block instanceof CropBlock)) {
            return null;
        }
        return withAge(block.defaultBlockState(), 0);
    }

    private static BlockState genericRegrowingStateFromPlanting(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        if (!(block instanceof SweetBerryBushBlock)) {
            return null;
        }
        return withAge(block.defaultBlockState(), 0);
    }

    private static BlockState genericStemStateFromPlanting(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }
        Block block = blockItem.getBlock();
        if (!(block instanceof StemBlock)) {
            return null;
        }
        return withAge(block.defaultBlockState(), 0);
    }

    public ItemStack removeSelectedCrop(RegistryAccess registries) {
        BlockState selected = easyVillagers.getCrop(registries);
        TallCropDefinition tallDefinition = selected == null ? null : currentTallDefinition(selected);
        RegrowingCropDefinition regrowingDefinition = selected == null ? null : currentRegrowingDefinition(selected);
        StemCropDefinition stemDefinition = selected == null ? null : currentStemDefinition(selected);
        ResourceLocation storedRegrowingPlantingItem = regrowingPlantingItemId;
        ResourceLocation storedSelectedPlantingItem = selectedPlantingItemId;
        if (storedSelectedPlantingItem == null && selected != null) {
            Item inferred = selected.getBlock().asItem();
            if (inferred != null && inferred != Items.AIR) {
                storedSelectedPlantingItem = BuiltInRegistries.ITEM.getKey(inferred);
            }
        }
        boolean rice = selected != null && RICE_CROP_ID.equals(BuiltInRegistries.BLOCK.getKey(selected.getBlock()));
        boolean tomato = isTomatoState(selected);
        boolean hearthCorn = isHearthCornState(selected);
        Item stemSeedItem = seedItemForStem(selected);
        ResourceLocation mushroomItemId = mushroomItemForColony(selected);
        ItemStack removed = easyVillagers.removeCrop(registries);
        paddyGrowth = 0;
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
        ropeOnePlanted = false;
        ropeTwoPlanted = false;
        fruitReady = false;
        stemFruitId = null;
        nocturnalMilletPanicleAge = -1;
        hearthCornMiddleAge = -1;
        hearthCornTopAge = -1;
        regrowingDefinitionId = null;
        regrowingPlantingItemId = null;
        selectedPlantingItemId = null;
        stemDefinitionId = null;
        tallDefinitionId = null;
        setChanged();

        if (tallDefinition != null && storedSelectedPlantingItem == null) {
            ItemStack canonical = tallDefinition.canonicalPlantingStack();
            if (!canonical.isEmpty()) {
                return canonical;
            }
        }
        if (regrowingDefinition != null) {
            if (storedRegrowingPlantingItem != null) {
                Item planting = BuiltInRegistries.ITEM.get(storedRegrowingPlantingItem);
                if (planting != null && planting != Items.AIR) {
                    return new ItemStack(planting);
                }
            }
            ItemStack canonical = regrowingDefinition.canonicalPlantingStack();
            if (!canonical.isEmpty()) {
                return canonical;
            }
        }
        if (rice) {
            Item riceItem = BuiltInRegistries.ITEM.get(RICE_ITEM_ID);
            return new ItemStack(riceItem);
        }
        if (tomato) {
            Item tomatoSeeds = BuiltInRegistries.ITEM.get(TOMATO_SEEDS_ID);
            return new ItemStack(tomatoSeeds);
        }
        if (hearthCorn) {
            Item kernels = BuiltInRegistries.ITEM.get(HEARTH_CORN_KERNELS_ID);
            if (kernels != null && kernels != Items.AIR) {
                return new ItemStack(kernels);
            }
        }
        if (mushroomItemId != null) {
            Item mushroom = BuiltInRegistries.ITEM.get(mushroomItemId);
            return new ItemStack(mushroom);
        }
        if (storedSelectedPlantingItem != null) {
            Item planting = BuiltInRegistries.ITEM.get(storedSelectedPlantingItem);
            if (planting != null && planting != Items.AIR) {
                return new ItemStack(planting);
            }
        }
        if (stemDefinition != null) {
            ItemStack canonical = stemDefinition.canonicalPlantingStack();
            if (!canonical.isEmpty()) {
                return canonical;
            }
        }
        if (stemSeedItem != null) {
            return new ItemStack(stemSeedItem);
        }
        return removed;
    }

    public List<Component> plantedCropNames(RegistryAccess registries) {
        List<Component> names = new ArrayList<>();
        Set<ResourceLocation> seenItems = new LinkedHashSet<>();
        Set<ResourceLocation> seenBlocks = new LinkedHashSet<>();

        if (variant().isAquatic() && paddySand && sugarCaneHeight > 0) {
            addPlantingItemName(names, seenItems, BuiltInRegistries.ITEM.getKey(Items.SUGAR_CANE));
        }

        if (supportsAttachedCrops()) {
            for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
                for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                    ResourceLocation plantingId = attachedPlantingItemIds[levelIndex][faceIndex];
                    if (plantingId != null) {
                        addPlantingItemName(names, seenItems, plantingId);
                        continue;
                    }

                    ResourceLocation cropId = attachedCropIds[levelIndex][faceIndex];
                    if (cropId != null && seenBlocks.add(cropId)) {
                        Block cropBlock = BuiltInRegistries.BLOCK.get(cropId);
                        if (cropBlock != null && cropBlock != Blocks.AIR) {
                            names.add(cropBlock.getName());
                        }
                    }
                }
            }
        }

        if (hasOrchardCrop()) {
            ItemStack planting = orchardPlantingStack();
            if (!planting.isEmpty()) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(planting.getItem());
                if (itemId != null && seenItems.add(itemId)) {
                    names.add(planting.getHoverName());
                }
            }
        }

        BlockState selected = easyVillagers.getCrop(registries);
        if (selected != null) {
            ItemStack planting = plantingStackForTooltip(selected);
            if (!planting.isEmpty()) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(planting.getItem());
                if (itemId != null && seenItems.add(itemId)) {
                    names.add(planting.getHoverName());
                }
            } else {
                ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(selected.getBlock());
                if (cropId != null && seenBlocks.add(cropId)) {
                    names.add(selected.getBlock().getName());
                }
            }
        }

        return List.copyOf(names);
    }

    private ItemStack plantingStackForTooltip(BlockState selected) {
        if (regrowingPlantingItemId != null) {
            Item planting = BuiltInRegistries.ITEM.get(regrowingPlantingItemId);
            if (planting != null && planting != Items.AIR) {
                return new ItemStack(planting);
            }
        }
        if (selectedPlantingItemId != null) {
            Item planting = BuiltInRegistries.ITEM.get(selectedPlantingItemId);
            if (planting != null && planting != Items.AIR) {
                return new ItemStack(planting);
            }
        }

        ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(selected.getBlock());
        if (RICE_CROP_ID.equals(cropId)) {
            return new ItemStack(BuiltInRegistries.ITEM.get(RICE_ITEM_ID));
        }
        if (isTomatoState(selected)) {
            return new ItemStack(BuiltInRegistries.ITEM.get(TOMATO_SEEDS_ID));
        }
        if (isHearthCornState(selected)) {
            Item kernels = BuiltInRegistries.ITEM.get(HEARTH_CORN_KERNELS_ID);
            return kernels == null || kernels == Items.AIR ? ItemStack.EMPTY : new ItemStack(kernels);
        }

        ResourceLocation mushroomId = mushroomItemForColony(selected);
        if (mushroomId != null) {
            return new ItemStack(BuiltInRegistries.ITEM.get(mushroomId));
        }

        Item stemSeed = seedItemForStem(selected);
        if (stemSeed != null) {
            return new ItemStack(stemSeed);
        }

        Item blockItem = selected.getBlock().asItem();
        return blockItem == Items.AIR ? ItemStack.EMPTY : new ItemStack(blockItem);
    }

    private static void addPlantingItemName(
            List<Component> names,
            Set<ResourceLocation> seenItems,
            ResourceLocation itemId
    ) {
        if (itemId == null || !seenItems.add(itemId)) {
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item != null && item != Items.AIR) {
            names.add(new ItemStack(item).getHoverName());
        }
    }

    public void setBaseProgress(int value) {
        baseProgress = Math.max(0, value);
        setChanged();
    }

    public void setRopeProgress(int ropeIndex, int value) {
        int safe = Math.max(0, value);
        if (ropeIndex == 1) {
            ropeOneProgress = safe;
        } else if (ropeIndex == 2) {
            ropeTwoProgress = safe;
        } else {
            throw new IllegalArgumentException("Rope index must be 1 or 2");
        }
        setChanged();
    }

    public void setRopeCount(int value) {
        ropeCount = Math.max(0, Math.min(2, value));
        setChanged();
    }

    public boolean hasStoredContents(RegistryAccess registries) {
        if (easyVillagers.hasVillager(registries) || easyVillagers.getCrop(registries) != null) {
            return true;
        }

        Container output = easyVillagers.getOutputInventory(registries);
        if (output != null) {
            for (int slot = 0; slot < output.getContainerSize(); slot++) {
                if (!output.getItem(slot).isEmpty()) {
                    return true;
                }
            }
        }

        if (!harvestTool.isEmpty() || paddySand || sugarCaneHeight > 0 || sugarCaneAge > 0
                 || ropeCount > 0 || paddyGrowth > 0 || baseProgress > 0
                 || ropeOneProgress > 0 || ropeTwoProgress > 0 || fruitReady || hasAttachedSetup()
                 || graftingSupport || orchardDefinitionId != null || orchardAge > 0) {
            return true;
        }

        CompoundTag unknown = passthroughData.copy();
        stripMetadata(unknown);
        stripAddonKeys(unknown);
        unknown.remove("Villager");
        unknown.remove("Crop");
        unknown.remove("Items");
        return !unknown.isEmpty();
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, CompatFarmerBlockEntity farmer) {
        RegistryAccess registries = level.registryAccess();

        if (farmer.easyVillagers.hasVillager(registries)) {
            boolean becameAdult = farmer.easyVillagers.advanceVillagerAge(registries);
            if (becameAdult) {
                farmer.syncVisibleState();
                if (farmer.harvestWaitingForAdultVillager) {
                    farmer.requestHarvestRetry();
                }
            } else {
                farmer.markPersistentStateChanged();
            }
        }

        if (farmer.harvestStateChanged) {
            farmer.harvestStateChanged = false;
            if (farmer.hasHarvestReadyState(registries)) {
                farmer.requestHarvestRetry();
            }
        }

        if (farmer.harvestRetryRequested) {
            farmer.tryRequestedHarvests(level, registries);
        }

        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        if (farmer.variant().isRich() && !farmer.variant().isAquatic()) {
            farmer.tryVirtualStemRichSoilPulse(level, registries);
        }

        int farmSpeed = farmer.easyVillagers.farmSpeed();

        if (farmer.hasGraftingSupport()) {
            farmer.growOrchard(level, farmSpeed);
            return;
        }

        if (farmer.supportsAttachedCrops() && farmer.hasAttachedSetup()) {
            farmer.growAttachedCrops(level, farmSpeed);
        }

        if (farmer.variant().isAquatic() && farmer.paddySand) {
            if (farmer.sugarCaneHeight <= 0 || farmer.sugarCaneHeight >= 3) {
                return;
            }

            if (level.random.nextInt(farmSpeed) == 0) {
                if (farmer.sugarCaneAge >= MAX_SUGAR_CANE_AGE) {
                    farmer.sugarCaneHeight++;
                    farmer.sugarCaneAge = 0;
                } else {
                    farmer.sugarCaneAge++;
                }
                farmer.setChanged();
            }
            return;
        }

        if (farmer.variant().isAquatic()) {
            if (!farmer.easyVillagers.hasRiceCrop(registries) || farmer.paddyGrowth >= MAX_PADDY_GROWTH) {
                return;
            }

            if (level.random.nextInt(farmSpeed) == 0) {
                farmer.paddyGrowth++;
                farmer.syncRiceCropState(registries);
                farmer.setChanged();
            }

            if (farmer.variant().isRich()
                    && farmer.paddyGrowth < MAX_PADDY_GROWTH
                    && level.random.nextInt(farmSpeed) == 0) {
                farmer.tryRichPaddyBoost(level, registries);
            }
            return;
        }

        BlockState crop = farmer.easyVillagers.getCrop(registries);
        if (crop == null) {
            return;
        }

        if (!farmer.isBaseHarvestReady(crop) && level.random.nextInt(farmSpeed) == 0) {
            boolean changed;
            TallCropDefinition tallDefinition = farmer.currentTallDefinition(crop);
            RegrowingCropDefinition regrowingDefinition = farmer.currentRegrowingDefinition(crop);
            if (farmer.isHearthCornState(crop)) {
                changed = farmer.ageHearthCorn(level, registries, crop);
            } else if (farmer.isNocturnalMilletState(crop)) {
                changed = farmer.ageNocturnalMillet(registries, crop);
            } else if (tallDefinition != null) {
                changed = farmer.ageTallCrop(registries, tallDefinition, crop);
            } else if (regrowingDefinition != null) {
                changed = farmer.ageRegrowingCrop(registries, regrowingDefinition, crop);
            } else if (farmer.isGenericRegrowingCrop(crop)) {
                changed = farmer.ageGenericRegrowingCrop(registries, crop);
            } else if (isTomatoState(crop)) {
                changed = farmer.ageTomato(level, registries);
            } else if (isMushroomColonyState(crop)) {
                changed = farmer.ageMushroomColony(level, registries);
            } else if (isStemState(crop)) {
                changed = farmer.ageStemCrop(level, registries);
            } else {
                changed = farmer.ageNormalCropSafely(level, registries);
            }
            if (changed) {
                farmer.setChanged();
            }
        }

        BlockState afterBase = farmer.easyVillagers.getCrop(registries);
        if (afterBase != null && TOMATO_CROP_ID.equals(BuiltInRegistries.BLOCK.getKey(afterBase.getBlock()))) {
            if (farmer.ropeCount >= 1
                    && (farmer.ropeOnePlanted || farmer.shouldExtendTomatoToRopeOne(afterBase))
                    && (farmer.ropeOneProgress < 3 || farmer.shouldExtendTomatoToRopeTwo())
                    && level.random.nextInt(farmSpeed) == 0) {
                farmer.ageTomatoRopeSection(level, registries, 1);
            }
            if (farmer.ropeCount >= 2
                    && farmer.ropeTwoPlanted
                    && farmer.ropeTwoProgress < 3
                    && level.random.nextInt(farmSpeed) == 0) {
                farmer.ageTomatoRopeSection(level, registries, 2);
            }
        }

        BlockState richAfterBase = farmer.easyVillagers.getCrop(registries);
        if (farmer.variant().isRich()
                && richAfterBase != null
                && !isStemState(richAfterBase)
                && level.random.nextInt(farmSpeed) == 0) {
            farmer.tryRichSoilBoost(level, registries);
        }
    }

    private boolean hasHarvestReadyState(RegistryAccess registries) {
        if (variant().isAquatic() && paddySand) {
            return sugarCaneHeight >= 3;
        }
        if (variant().isAquatic()) {
            return paddyGrowth >= MAX_PADDY_GROWTH && easyVillagers.hasRiceCrop(registries);
        }
        OrchardCropDefinition orchardDefinition = currentOrchardDefinition();
        if (orchardDefinition != null && orchardAge >= orchardDefinition.matureAge()) {
            return true;
        }
        if (hasMatureAttachedCrop()) {
            return true;
        }

        BlockState crop = easyVillagers.getCrop(registries);
        if (crop == null) {
            return false;
        }

        if (isHearthCornState(crop)) {
            return hasHarvestReadyHearthCorn(crop);
        }

        if (isNocturnalMilletState(crop)) {
            return isForgottenNocturnalMillet(crop)
                    && getAge(crop) >= maxAge(crop)
                    && nocturnalMilletPanicleAge >= 2;
        }

        TallCropDefinition tallDefinition = currentTallDefinition(crop);
        if (tallDefinition != null && tallDefinition.age(crop) >= tallDefinition.harvestAge()) {
            return true;
        }
        RegrowingCropDefinition regrowingDefinition = currentRegrowingDefinition(crop);
        if (regrowingDefinition != null
                && regrowingDefinition.age(crop) >= regrowingDefinition.harvestAge()) {
            return true;
        }
        if (isGenericRegrowingCrop(crop) && getAge(crop) >= maxAge(crop)) {
            return true;
        }
        if (isStemState(crop) && fruitReady) {
            return true;
        }

        ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(crop.getBlock());
        if (TOMATO_CROP_ID.equals(cropId)) {
            if (isMatureAgeState(crop) && !shouldExtendTomatoToRopeOne(crop)) {
                return true;
            }
            if (ropeOnePlanted && ropeOneProgress >= 3 && !shouldExtendTomatoToRopeTwo()) {
                return true;
            }
            return ropeTwoPlanted && ropeTwoProgress >= 3;
        }
        return !BUDDING_TOMATO_ID.equals(cropId) && isMatureAgeState(crop);
    }

    private boolean isBaseHarvestReady(BlockState crop) {
        if (crop == null) {
            return false;
        }
        if (isHearthCornState(crop)) {
            return hasHarvestReadyHearthCorn(crop);
        }
        if (isNocturnalMilletState(crop)) {
            return isForgottenNocturnalMillet(crop)
                    && getAge(crop) >= maxAge(crop)
                    && nocturnalMilletPanicleAge >= 2;
        }
        TallCropDefinition tallDefinition = currentTallDefinition(crop);
        if (tallDefinition != null) {
            return tallDefinition.age(crop) >= tallDefinition.harvestAge();
        }
        RegrowingCropDefinition regrowingDefinition = currentRegrowingDefinition(crop);
        if (regrowingDefinition != null) {
            return regrowingDefinition.age(crop) >= regrowingDefinition.harvestAge();
        }
        if (isGenericRegrowingCrop(crop)) {
            return getAge(crop) >= maxAge(crop);
        }
        if (isStemState(crop)) {
            return fruitReady;
        }
        ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(crop.getBlock());
        if (BUDDING_TOMATO_ID.equals(cropId)) {
            return false;
        }
        if (TOMATO_CROP_ID.equals(cropId) && shouldExtendTomatoToRopeOne(crop)) {
            return false;
        }
        return isMatureAgeState(crop);
    }

    private void tryRequestedHarvests(ServerLevel level, RegistryAccess registries) {
        if (!harvestRetryRequested || harvestTransactionActive) {
            return;
        }

        harvestRetryRequested = false;
        harvestWaitingForOutputSpace = false;
        harvestWaitingForTool = false;
        harvestWaitingForAdultVillager = false;
        blockedOutputRequirement = List.of();
        harvestTransactionActive = true;
        try {
            if (variant().isAquatic() && paddySand) {
                if (sugarCaneHeight < 3 || !hasAdultFarmerVillager(registries)) {
                    return;
                }
                if (harvestMatureSugarCane(registries)) {
                    sugarCaneHeight = 1;
                    sugarCaneAge = 0;
                    setChanged();
                    level.playSound(
                            null, worldPosition, SoundEvents.VILLAGER_WORK_FARMER,
                            SoundSource.BLOCKS, 1.0F, 1.0F
                    );
                }
                return;
            }

            if (variant().isAquatic()) {
                if (paddyGrowth < MAX_PADDY_GROWTH
                        || !easyVillagers.hasRiceCrop(registries)
                        || !hasAdultFarmerVillager(registries)) {
                    return;
                }
                if (harvestMatureRice(level, registries)) {
                    paddyGrowth = 3;
                    syncRiceCropState(registries);
                    setChanged();
                    level.playSound(
                            null, worldPosition, SoundEvents.VILLAGER_WORK_FARMER,
                            SoundSource.BLOCKS, 1.0F, 1.0F
                    );
                }
                return;
            }

            OrchardCropDefinition orchardDefinition = currentOrchardDefinition();
            if (orchardDefinition != null && orchardAge >= orchardDefinition.matureAge()) {
                harvestMatureOrchard(level, registries, orchardDefinition);
                return;
            }

            harvestMatureAttachedCrops(level, registries);

            BlockState crop = easyVillagers.getCrop(registries);
            if (crop == null) {
                return;
            }

            if (isHearthCornState(crop)) {
                if (hasHarvestReadyHearthCorn(crop) && harvestHearthCorn(level, registries, crop)) {
                    setChanged();
                }
                return;
            }

            if (isNocturnalMilletState(crop)) {
                if (nocturnalMilletPanicleAge >= 2 && harvestNocturnalMillet(registries)) {
                    setChanged();
                }
                return;
            }

            TallCropDefinition tallDefinition = currentTallDefinition(crop);
            if (tallDefinition != null && tallDefinition.age(crop) >= tallDefinition.harvestAge()) {
                if (harvestTallCrop(level, registries, tallDefinition, crop)) {
                    setChanged();
                }
                return;
            }

            RegrowingCropDefinition regrowingDefinition = currentRegrowingDefinition(crop);
            if (regrowingDefinition != null
                    && regrowingDefinition.age(crop) >= regrowingDefinition.harvestAge()) {
                if (harvestRegrowingCrop(level, registries, regrowingDefinition, crop)) {
                    setChanged();
                }
                return;
            }
            if (isGenericRegrowingCrop(crop) && getAge(crop) >= maxAge(crop)) {
                if (harvestGenericRegrowingCrop(level, registries, crop)) {
                    setChanged();
                }
                return;
            }

            if (isStemState(crop)) {
                if (fruitReady && harvestReadyStem(level, registries)) {
                    setChanged();
                }
                // A mature stem is not itself a harvest target. It must remain planted until its fruit appears.
                return;
            }

            ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(crop.getBlock());
            if (!BUDDING_TOMATO_ID.equals(cropId) && isMatureAgeState(crop)) {
                boolean changed;
                if (isMushroomColonyState(crop)) {
                    changed = ageMushroomColony(level, registries);
                } else if (TOMATO_CROP_ID.equals(cropId)) {
                    changed = ageTomato(level, registries);
                } else {
                    changed = ageNormalCropSafely(level, registries);
                }
                if (changed) {
                    setChanged();
                }
            }

            BlockState afterBase = easyVillagers.getCrop(registries);
            if (afterBase != null && TOMATO_CROP_ID.equals(BuiltInRegistries.BLOCK.getKey(afterBase.getBlock()))) {
                if (ropeOnePlanted && ropeOneProgress >= 3) {
                    ageTomatoRopeSection(level, registries, 1);
                }
                if (ropeTwoPlanted && ropeTwoProgress >= 3) {
                    ageTomatoRopeSection(level, registries, 2);
                }
            }
        } finally {
            harvestTransactionActive = false;
        }
    }

    private boolean hasAdultFarmerVillager(RegistryAccess registries) {
        Villager villager = easyVillagers.getVillagerEntity(registries);
        boolean ready = villager != null
                && !villager.isBaby()
                && villager.getVillagerData().getProfession() == VillagerProfession.FARMER;
        if (!ready && harvestTransactionActive) {
            harvestWaitingForAdultVillager = true;
        }
        return ready;
    }

    private TallCropDefinition currentTallDefinition(BlockState crop) {
        if (!variant().isRich() || variant().isAquatic() || crop == null) {
            return null;
        }
        TallCropDefinition stored = TallCropDefinitions.get(tallDefinitionId).orElse(null);
        if (stored != null && stored.matchesCrop(crop)) {
            return stored;
        }
        return TallCropDefinitions.findCrop(crop).orElse(null);
    }

    private boolean ageTallCrop(
            RegistryAccess registries,
            TallCropDefinition definition,
            BlockState crop
    ) {
        int age = definition.age(crop);
        if (age >= definition.harvestAge()) {
            return false;
        }
        BlockState next = definition.lowerState(definition.withAge(crop, Math.min(definition.harvestAge(), age + 1)));
        easyVillagers.setCropState(next, registries);
        return true;
    }

    private boolean harvestTallCrop(
            ServerLevel level,
            RegistryAccess registries,
            TallCropDefinition definition,
            BlockState crop
    ) {
        if (!hasAdultFarmerVillager(registries)) {
            return false;
        }
        int age = definition.age(crop);
        if (age < definition.harvestAge()) {
            return false;
        }
        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return false;
        }

        List<ItemStack> drops;
        if (definition.usesBlockLoot()) {
            BlockState harvestState = definition.lowerState(definition.withAge(crop, definition.harvestAge()));
            LootParams.Builder context = new LootParams.Builder(level)
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                    .withParameter(LootContextParams.BLOCK_STATE, harvestState)
                    .withParameter(LootContextParams.TOOL, normalCropHarvestTool());
            drops = harvestState.getDrops(context);
        } else {
            ItemStack harvest = definition.rollHarvest(level.random, age);
            drops = harvest.isEmpty() ? List.of() : List.of(harvest);
        }

        if (!canFitAll(output, drops)) {
            return false;
        }
        for (ItemStack drop : drops) {
            if (!drop.isEmpty()) {
                insertIntoOutput(output, drop.copy());
            }
        }
        if (!drops.isEmpty()) {
            output.setChanged();
        }
        BlockState reset = definition.lowerState(definition.withAge(crop, definition.postHarvestAge()));
        easyVillagers.setCropState(reset, registries);
        level.playSound(null, worldPosition, SoundEvents.VILLAGER_WORK_FARMER,
                SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
        return true;
    }

    private RegrowingCropDefinition currentRegrowingDefinition(BlockState crop) {
        if (!variant().isRich() || variant().isAquatic() || crop == null) {
            return null;
        }
        RegrowingCropDefinition stored = RegrowingCropDefinitions.get(regrowingDefinitionId).orElse(null);
        if (stored != null && stored.matchesCrop(crop)) {
            return stored;
        }
        return RegrowingCropDefinitions.findCrop(crop).orElse(null);
    }

    private boolean isGenericRegrowingCrop(BlockState crop) {
        return variant().isRich()
                && !variant().isAquatic()
                && crop != null
                && currentRegrowingDefinition(crop) == null
                && crop.getBlock() instanceof SweetBerryBushBlock;
    }

    private boolean ageGenericRegrowingCrop(RegistryAccess registries, BlockState crop) {
        if (!isGenericRegrowingCrop(crop)) {
            return false;
        }
        int age = getAge(crop);
        int max = maxAge(crop);
        if (age >= max) {
            return false;
        }
        easyVillagers.setCropState(withAge(crop, age + 1), registries);
        return true;
    }

    private boolean harvestGenericRegrowingCrop(
            ServerLevel level,
            RegistryAccess registries,
            BlockState crop
    ) {
        if (!isGenericRegrowingCrop(crop) || getAge(crop) < maxAge(crop)
                || !hasAdultFarmerVillager(registries)) {
            return false;
        }
        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return false;
        }
        LootParams.Builder context = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.BLOCK_STATE, crop)
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);
        List<ItemStack> drops = crop.getDrops(context);
        if (!canFitAll(output, drops)) {
            return false;
        }
        for (ItemStack drop : drops) {
            insertIntoOutput(output, drop.copy());
        }
        output.setChanged();
        easyVillagers.setCropState(withAge(crop, Math.min(1, maxAge(crop))), registries);
        level.playSound(null, worldPosition, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
        return true;
    }

    private boolean ageRegrowingCrop(
            RegistryAccess registries,
            RegrowingCropDefinition definition,
            BlockState crop
    ) {
        int age = definition.age(crop);
        if (age >= definition.harvestAge()) {
            return false;
        }
        easyVillagers.setCropState(definition.withAge(crop, Math.min(definition.harvestAge(), age + 1)), registries);
        return true;
    }

    private boolean harvestRegrowingCrop(
            ServerLevel level,
            RegistryAccess registries,
            RegrowingCropDefinition definition,
            BlockState crop
    ) {
        if (!hasAdultFarmerVillager(registries)) {
            return false;
        }

        int age = definition.age(crop);
        if (age < definition.harvestAge()) {
            return false;
        }

        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return false;
        }

        ItemStack harvest = definition.rollHarvest(level.random, age);
        List<ItemStack> drops = harvest.isEmpty() ? List.of() : List.of(harvest);
        if (!canFitAll(output, drops)) {
            return false;
        }

        if (!harvest.isEmpty()) {
            insertIntoOutput(output, harvest.copy());
            output.setChanged();
        }
        easyVillagers.setCropState(definition.withAge(crop, definition.postHarvestAge()), registries);
        level.playSound(null, worldPosition, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
        return true;
    }

    private void growOrchard(ServerLevel level, int farmSpeed) {
        OrchardCropDefinition definition = currentOrchardDefinition();
        if (definition == null || orchardAge >= definition.matureAge()) {
            return;
        }
        int safeFarmSpeed = Math.max(1, farmSpeed);
        int nextAge = orchardAge;
        if (level.random.nextInt(safeFarmSpeed) == 0) {
            nextAge++;
        }
        double boostChance = farmersDelight.richSoilBoostChance();
        if (definition.richSoil() && nextAge < definition.matureAge()
                && boostChance > 0.0D
                && level.random.nextInt(safeFarmSpeed) == 0
                && level.random.nextDouble() < boostChance) {
            nextAge++;
        }
        nextAge = Math.min(definition.matureAge(), Math.min(definition.maxAge(), nextAge));
        if (nextAge != orchardAge) {
            orchardAge = nextAge;
            setChanged();
        }
    }

    private boolean harvestMatureOrchard(
            ServerLevel level,
            RegistryAccess registries,
            OrchardCropDefinition definition
    ) {
        if (definition == null || orchardAge < definition.matureAge()) {
            return false;
        }
        if (!hasAdultFarmerVillager(registries)) {
            return false;
        }
        if (!FarmerToolSupport.isShears(harvestTool)) {
            if (harvestTransactionActive) {
                harvestWaitingForTool = true;
            }
            return false;
        }
        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return false;
        }
        if (orchardPendingHarvest.isEmpty()) {
            orchardPendingHarvest = definition.harvestStack(level.random);
        }
        ItemStack harvest = orchardPendingHarvest.copy();
        List<ItemStack> drops = harvest.isEmpty() ? List.of() : List.of(harvest);
        if (!canFitAllPure(output, drops)) {
            if (harvestTransactionActive) {
                harvestWaitingForOutputSpace = true;
                blockedOutputRequirement = drops.stream().map(ItemStack::copy).toList();
            }
            return false;
        }
        if (!harvest.isEmpty()) {
            insertIntoOutput(output, harvest.copy());
            output.setChanged();
        }
        orchardAge = definition.postHarvestAge();
        orchardPendingHarvest = ItemStack.EMPTY;
        damageHarvestTool(level);
        setChanged();
        level.playSound(null, worldPosition, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                SoundSource.BLOCKS, 1.0F, 0.95F + level.random.nextFloat() * 0.1F);
        return true;
    }

    private void growAttachedCrops(ServerLevel level, int farmSpeed) {
        if (!supportsAttachedCrops() || !hasAttachedSetup()) {
            return;
        }

        double richSoilBoostChance = farmersDelight.richSoilBoostChance();
        boolean changed = false;
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            BlockState host = attachedHostState(levelIndex);
            if (host.isAir()) {
                continue;
            }
            for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                ResourceLocation definitionId = attachedDefinitionIds[levelIndex][faceIndex];
                AttachedCropDefinition definition = AttachedCropDefinitions.get(definitionId).orElse(null);
                if (definition == null || !definition.matchesHost(host)) {
                    continue;
                }
                int age = attachedCropAges[levelIndex][faceIndex];
                if (age >= definition.matureAge()) {
                    continue;
                }

                int nextAge = age;
                if (level.random.nextInt(farmSpeed) == 0) {
                    nextAge++;
                }
                if (definition.richSoil()
                        && nextAge < definition.matureAge()
                        && richSoilBoostChance > 0.0D
                        && level.random.nextInt(farmSpeed) == 0
                        && level.random.nextDouble() < richSoilBoostChance) {
                    nextAge++;
                }
                nextAge = Math.min(definition.matureAge(), Math.min(definition.maxAge(), nextAge));
                if (nextAge != age) {
                    attachedCropAges[levelIndex][faceIndex] = nextAge;
                    changed = true;
                }
            }
        }
        if (changed) {
            setChanged();
        }
    }

    private void harvestMatureAttachedCrops(ServerLevel level, RegistryAccess registries) {
        if (!supportsAttachedCrops() || !hasMatureAttachedCrop()) {
            return;
        }
        if (!hasAdultFarmerVillager(registries)) {
            return;
        }

        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return;
        }

        boolean changed = false;
        boolean blockedByOutput = false;
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            BlockState host = attachedHostState(levelIndex);
            for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                AttachedCropDefinition definition = AttachedCropDefinitions
                        .get(attachedDefinitionIds[levelIndex][faceIndex])
                        .orElse(null);
                if (definition == null
                        || !definition.matchesHost(host)
                        || attachedCropAges[levelIndex][faceIndex] < definition.matureAge()) {
                    continue;
                }
                if (!attachedToolSatisfied(definition)) {
                    continue;
                }

                BlockState mature = attachedCropState(levelIndex, faceIndex);
                LootParams.Builder context = new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                        .withParameter(LootContextParams.BLOCK_STATE, mature)
                        .withParameter(LootContextParams.TOOL, attachedLootTool(definition));
                List<ItemStack> drops = mature.getDrops(context);
                if (!canFitAllPure(output, drops)) {
                    blockedByOutput = true;
                    continue;
                }

                for (ItemStack drop : drops) {
                    insertIntoOutput(output, drop.copy());
                }
                attachedCropAges[levelIndex][faceIndex] = definition.postHarvestAge();
                if (definition.tool() != AttachedCropDefinition.Tool.NONE) {
                    damageHarvestTool(level);
                }
                changed = true;
            }
        }

        if (blockedByOutput && harvestTransactionActive) {
            harvestWaitingForOutputSpace = true;
            blockedOutputRequirement = List.of();
        }

        if (changed) {
            output.setChanged();
            setChanged();
            level.playSound(null, worldPosition, SoundEvents.VILLAGER_WORK_FARMER,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private boolean hasMatureAttachedCrop() {
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                AttachedCropDefinition definition = AttachedCropDefinitions
                        .get(attachedDefinitionIds[levelIndex][faceIndex])
                        .orElse(null);
                if (definition != null && attachedCropAges[levelIndex][faceIndex] >= definition.matureAge()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean attachedToolSatisfied(AttachedCropDefinition definition) {
        boolean satisfied = switch (definition.tool()) {
            case NONE -> true;
            case KNIFE -> FarmerToolSupport.isKnife(harvestTool);
            case HOE -> FarmerToolSupport.isHoe(harvestTool);
            case AXE -> FarmerToolSupport.isAxe(harvestTool);
        };
        if (!satisfied && harvestTransactionActive) {
            harvestWaitingForTool = true;
        }
        return satisfied;
    }

    private ItemStack attachedLootTool(AttachedCropDefinition definition) {
        return definition.tool() == AttachedCropDefinition.Tool.NONE ? ItemStack.EMPTY : harvestTool;
    }

    private void tryVirtualStemRichSoilPulse(ServerLevel level, RegistryAccess registries) {
        if (fruitReady)
            return;

        BlockState crop = easyVillagers.getCrop(registries);
        if (!isStemState(crop))
            return;

        int age = getAge(crop);
        int maxAge = maxAge(crop);
        if (age >= maxAge)
            return;

        int randomTickSpeed = Math.max(0, level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING));
        if (randomTickSpeed <= 0)
            return;

        double selectedChancePerTick = 1.0D - Math.pow(4095.0D / 4096.0D, randomTickSpeed);
        double boostChance = farmersDelight.richSoilBoostChance();
        if (boostChance <= 0.0D)
            return;

        double successChancePerTick = Math.min(1.0D, selectedChancePerTick * boostChance);
        int successfulBoosts = sampleBinomial20(level, successChancePerTick);
        if (successfulBoosts <= 0)
            return;

        int totalIncrement = 0;
        for (int attempt = 0; attempt < successfulBoosts; attempt++) {
            totalIncrement += 2 + level.random.nextInt(4);
        }

        int nextAge = Math.min(maxAge, age + totalIncrement);
        if (nextAge == age)
            return;

        easyVillagers.setCropState(withAge(crop, nextAge), registries);
        fruitReady = false;
        setChanged();
    }

    private static int sampleBinomial20(Level level, double probability) {
        if (probability <= 0.0D)
            return 0;
        if (probability >= 1.0D)
            return 20;

        double failureChance = 1.0D - probability;
        double probabilityMass = Math.pow(failureChance, 20);
        double cumulative = probabilityMass;
        double roll = level.random.nextDouble();
        int successes = 0;

        while (roll > cumulative && successes < 20) {
            successes++;
            probabilityMass *= ((21.0D - successes) / successes) * (probability / failureChance);
            cumulative += probabilityMass;
        }
        return successes;
    }

    private void tryRichSoilBoost(ServerLevel level, RegistryAccess registries) {
        BlockState crop = easyVillagers.getCrop(registries);
        if (crop == null) {
            return;
        }

        if (isHearthCornState(crop)) {
            if (hasHarvestReadyHearthCorn(crop)) {
                return;
            }
            double boostChance = farmersDelight.richSoilBoostChance();
            if (boostChance > 0.0D && level.random.nextDouble() <= boostChance
                    && ageHearthCorn(level, registries, crop)) {
                setChanged();
            }
            return;
        }

        TallCropDefinition tallDefinition = currentTallDefinition(crop);
        if (tallDefinition != null) {
            if (!tallDefinition.richSoil() || tallDefinition.age(crop) >= tallDefinition.harvestAge()) {
                return;
            }
            double boostChance = farmersDelight.richSoilBoostChance();
            if (boostChance > 0.0D && level.random.nextDouble() <= boostChance
                    && ageTallCrop(registries, tallDefinition, crop)) {
                setChanged();
            }
            return;
        }

        RegrowingCropDefinition regrowingDefinition = currentRegrowingDefinition(crop);
        if (regrowingDefinition != null) {
            if (!regrowingDefinition.richSoil()
                    || regrowingDefinition.age(crop) >= regrowingDefinition.harvestAge()) {
                return;
            }
            double boostChance = farmersDelight.richSoilBoostChance();
            if (boostChance > 0.0D && level.random.nextDouble() <= boostChance
                    && ageRegrowingCrop(registries, regrowingDefinition, crop)) {
                setChanged();
            }
            return;
        }
        if (isGenericRegrowingCrop(crop)) {
            if (getAge(crop) >= maxAge(crop)) {
                return;
            }
            double boostChance = farmersDelight.richSoilBoostChance();
            if (boostChance > 0.0D && level.random.nextDouble() <= boostChance
                    && ageGenericRegrowingCrop(registries, crop)) {
                setChanged();
            }
            return;
        }

        if (crop.is(UNAFFECTED_BY_RICH_SOIL)) {
            return;
        }

        double boostChance = farmersDelight.richSoilBoostChance();
        if (boostChance <= 0.0D || level.random.nextDouble() > boostChance) {
            return;
        }

        applyRichSoilBoneMeal(level, registries);
    }

    private void tryRichPaddyBoost(ServerLevel level, RegistryAccess registries) {
        if (paddyGrowth >= MAX_PADDY_GROWTH) {
            return;
        }

        double boostChance = farmersDelight.richSoilBoostChance();
        if (boostChance <= 0.0D || level.random.nextDouble() > boostChance) {
            return;
        }

        Block rice = BuiltInRegistries.BLOCK.get(RICE_CROP_ID);
        int increment = getBoneMealAgeIncrease(rice, level);
        if (increment <= 0) {
            return;
        }

        paddyGrowth = Math.min(MAX_PADDY_GROWTH, paddyGrowth + increment);
        syncRiceCropState(registries);
        setChanged();
    }

    private boolean applyRichSoilBoneMeal(Level level, RegistryAccess registries) {
        BlockState crop = easyVillagers.getCrop(registries);
        if (crop == null || crop.is(UNAFFECTED_BY_RICH_SOIL)) {
            return false;
        }

        if (isHearthCornState(crop) && level instanceof ServerLevel serverLevel) {
            return ageHearthCorn(serverLevel, registries, crop);
        }

        if (BUDDING_TOMATO_ID.equals(BuiltInRegistries.BLOCK.getKey(crop.getBlock()))) {
            int currentAge = getAge(crop);
            int ageGrowth = Math.min(currentAge + 1 + level.random.nextInt(4), 7);
            if (ageGrowth <= 3) {
                easyVillagers.setCropState(withAge(crop, ageGrowth), registries);
                baseProgress = ageGrowth;
            } else {
                Block tomato = BuiltInRegistries.BLOCK.get(TOMATO_CROP_ID);
                int remainingGrowth = ageGrowth - 4;
                easyVillagers.setCropState(withAge(tomato.defaultBlockState(), remainingGrowth), registries);
                baseProgress = remainingGrowth;
            }
            setChanged();
            return true;
        }

        if (TOMATO_CROP_ID.equals(BuiltInRegistries.BLOCK.getKey(crop.getBlock()))) {
            int increment = getBoneMealAgeIncrease(crop.getBlock(), level);
            if (increment <= 0) {
                return false;
            }

            int baseAge = getAge(crop);
            if (baseAge < 3) {
                int nextAge = Math.min(3, baseAge + increment);
                easyVillagers.setCropState(withAge(crop, nextAge), registries);
                baseProgress = nextAge;
                setChanged();
                return true;
            }

            if (ropeOnePlanted && ropeOneProgress < 3) {
                ropeOneProgress = Math.min(3, ropeOneProgress + increment);
                setChanged();
                return true;
            }
            if (ropeTwoPlanted && ropeTwoProgress < 3) {
                ropeTwoProgress = Math.min(3, ropeTwoProgress + increment);
                setChanged();
                return true;
            }
            return false;
        }

        if (isNocturnalMilletState(crop)) {
            int currentAge = getAge(crop);
            int maxAge = maxAge(crop);
            if (currentAge < maxAge) {
                int increment = 2 + level.random.nextInt(4);
                int newAge = Math.min(maxAge, currentAge + increment);
                BlockState next = withAge(crop, newAge);
                if (newAge >= maxAge) {
                    next = withBooleanProperty(next, "forgotten", true);
                }
                easyVillagers.setCropState(next, registries);
                nocturnalMilletPanicleAge = -1;
                setChanged();
                return true;
            }
            if (!isForgottenNocturnalMillet(crop)) {
                easyVillagers.setCropState(withBooleanProperty(crop, "forgotten", true), registries);
                setChanged();
                return true;
            }
            if (nocturnalMilletPanicleAge < 0) {
                nocturnalMilletPanicleAge = 0;
                setChanged();
                return true;
            }
            if (nocturnalMilletPanicleAge < 2) {
                nocturnalMilletPanicleAge++;
                setChanged();
                return true;
            }
            return false;
        }

        if (isStemState(crop)) {
            int currentAge = getAge(crop);
            int maxAge = maxAge(crop);
            if (currentAge >= maxAge)
                return false;

            int increment = 2 + level.random.nextInt(4);
            easyVillagers.setCropState(withAge(crop, Math.min(maxAge, currentAge + increment)), registries);
            fruitReady = false;
            setChanged();
            return true;
        }

        Optional<Property<?>> ageProperty = crop.getProperties().stream()
                .filter(property -> property.getName().equals("age"))
                .findFirst();
        if (ageProperty.isEmpty() || !(ageProperty.get() instanceof IntegerProperty integerProperty)) {
            return false;
        }

        int currentAge = crop.getValue(integerProperty);
        int maxAge = integerProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(currentAge);
        if (currentAge >= maxAge) {
            return false;
        }

        int increment = getBoneMealAgeIncrease(crop.getBlock(), level);
        if (increment <= 0) {
            return false;
        }

        easyVillagers.setCropState(crop.setValue(integerProperty, Math.min(maxAge, currentAge + increment)),
                registries);
        setChanged();
        return true;
    }

    private static int getBoneMealAgeIncrease(Block block, Level level) {
        try {
            Method method = ReflectionCache.declaredMethodByArity(block.getClass(), "getBonemealAgeIncrease", 1);
            Object result = method.invoke(block, level);
            return result instanceof Number number ? Math.max(0, number.intValue()) : 0;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return 0;
        }
    }

    private boolean ageNormalCropSafely(ServerLevel level, RegistryAccess registries) {
        BlockState crop = easyVillagers.getCrop(registries);
        if (crop == null) {
            return false;
        }
        if (isHearthCornState(crop)) {
            return ageHearthCorn(level, registries, crop);
        }

        Optional<Property<?>> ageProperty = crop.getProperties().stream()
                .filter(property -> property.getName().equals("age"))
                .findFirst();
        if (ageProperty.isEmpty() || !(ageProperty.get() instanceof IntegerProperty integerProperty)) {
            return false;
        }

        int age = crop.getValue(integerProperty);
        int maxAge = integerProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(age);
        if (age < maxAge) {
            easyVillagers.setCropState(crop.setValue(integerProperty, age + 1), registries);
            return true;
        }

        if (!hasAdultFarmerVillager(registries)) {
            return false;
        }

        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return false;
        }

        LootParams.Builder context = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.BLOCK_STATE, crop)
                .withParameter(LootContextParams.TOOL, normalCropHarvestTool());
        List<ItemStack> drops = crop.getDrops(context);
        if (!canFitAll(output, drops)) {
            return false;
        }

        for (ItemStack drop : drops) {
            insertIntoOutput(output, drop.copy());
        }
        output.setChanged();
        easyVillagers.setCropState(crop.setValue(integerProperty, 0), registries);
        level.playSound(null, worldPosition, SoundEvents.VILLAGER_WORK_FARMER, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private boolean ageMushroomColony(ServerLevel level, RegistryAccess registries) {
        BlockState crop = easyVillagers.getCrop(registries);
        if (!isMushroomColonyState(crop)) {
            return false;
        }

        int age = getAge(crop);
        int maxAge = maxAge(crop);
        if (age < maxAge) {
            easyVillagers.setCropState(withAge(crop, age + 1), registries);
            return true;
        }

        if (!hasAdultFarmerVillager(registries)) {
            return false;
        }

        if (variant().isRich() && !FarmerToolSupport.isKnife(harvestTool)) {
            if (harvestTransactionActive) {
                harvestWaitingForTool = true;
            }
            return false;
        }

        ResourceLocation mushroomItemId = mushroomItemForColony(crop);
        if (mushroomItemId == null) {
            return false;
        }

        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return false;
        }

        Item mushroom = BuiltInRegistries.ITEM.get(mushroomItemId);
        ItemStack harvest = new ItemStack(mushroom, maxAge);
        if (!canFitAll(output, List.of(harvest))) {
            return false;
        }
        insertIntoOutput(output, harvest.copy());
        output.setChanged();
        easyVillagers.setCropState(withAge(crop, 0), registries);
        level.playSound(null, worldPosition, crop.getSoundType().getBreakSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
        return true;
    }

    private boolean ageTomato(ServerLevel level, RegistryAccess registries) {
        BlockState crop = easyVillagers.getCrop(registries);
        if (!isTomatoState(crop)) {
            return false;
        }

        ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(crop.getBlock());
        int age = getAge(crop);

        if (BUDDING_TOMATO_ID.equals(cropId)) {
            if (age < 3) {
                easyVillagers.setCropState(withAge(crop, age + 1), registries);
                baseProgress = age + 1;
            } else {
                Block tomato = BuiltInRegistries.BLOCK.get(TOMATO_CROP_ID);
                easyVillagers.setCropState(withAge(tomato.defaultBlockState(), 0), registries);
                baseProgress = 0;
            }
            return true;
        }

        if (!TOMATO_CROP_ID.equals(cropId)) {
            return false;
        }

        if (age < 3) {
            easyVillagers.setCropState(withAge(crop, age + 1), registries);
            baseProgress = age + 1;
            return true;
        }

        if (shouldExtendTomatoToRopeOne(crop)) {
            ropeOnePlanted = true;
            ropeOneProgress = 0;
            setChanged();
            return true;
        }

        if (!hasAdultFarmerVillager(registries)) {
            return false;
        }

        if (!harvestTomatoSection(level, registries, false)) {
            return false;
        }
        easyVillagers.setCropState(withAge(crop, 0), registries);
        baseProgress = 0;
        level.playSound(null, worldPosition, SoundEvents.VILLAGER_WORK_FARMER, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private boolean ageTomatoRopeSection(ServerLevel level, RegistryAccess registries, int ropeIndex) {
        if (ropeIndex < 1 || ropeIndex > ropeCount) {
            return false;
        }

        if (ropeIndex == 1 && !ropeOnePlanted) {
            BlockState crop = easyVillagers.getCrop(registries);
            if (!shouldExtendTomatoToRopeOne(crop)) {
                return false;
            }
            ropeOnePlanted = true;
            ropeOneProgress = 0;
            setChanged();
            return true;
        }
        if (ropeIndex == 2 && !ropeTwoPlanted) {
            if (!shouldExtendTomatoToRopeTwo()) {
                return false;
            }
            ropeTwoPlanted = true;
            ropeTwoProgress = 0;
            setChanged();
            return true;
        }

        int progress = ropeIndex == 1 ? ropeOneProgress : ropeTwoProgress;
        if (progress < 3) {
            if (ropeIndex == 1) {
                ropeOneProgress++;
                if (ropeOneProgress >= 3 && shouldExtendTomatoToRopeTwo()) {
                    ropeTwoPlanted = true;
                    ropeTwoProgress = 0;
                }
            } else {
                ropeTwoProgress++;
            }
            setChanged();
            return true;
        }

        if (ropeIndex == 1 && shouldExtendTomatoToRopeTwo()) {
            ropeTwoPlanted = true;
            ropeTwoProgress = 0;
            setChanged();
            return true;
        }

        if (!hasAdultFarmerVillager(registries)) {
            return false;
        }

        if (!harvestTomatoSection(level, registries, true)) {
            return false;
        }
        if (ropeIndex == 1) {
            ropeOneProgress = 0;
        } else {
            ropeTwoProgress = 0;
        }
        setChanged();
        return true;
    }

    private boolean shouldExtendTomatoToRopeOne(BlockState crop) {
        return crop != null
                && ropeCount >= 1
                && !ropeOnePlanted
                && TOMATO_CROP_ID.equals(BuiltInRegistries.BLOCK.getKey(crop.getBlock()))
                && isMatureAgeState(crop);
    }

    private boolean shouldExtendTomatoToRopeTwo() {
        return ropeCount >= 2
                && ropeOnePlanted
                && ropeOneProgress >= 3
                && !ropeTwoPlanted;
    }

    private boolean harvestTomatoSection(ServerLevel level, RegistryAccess registries, boolean ropeSection) {
        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return false;
        }

        Block tomato = BuiltInRegistries.BLOCK.get(ropeSection ? TOMATO_ON_ROPE_ID :TOMATO_CROP_ID);
        if (tomato == Blocks.AIR)
            return false;
        BlockState harvestState =withAge(tomato.defaultBlockState(), 3);
        if (!ropeSection)
            harvestState = withBooleanProperty(harvestState, "ropelogged", true);

        LootParams.Builder context = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.BLOCK_STATE, harvestState)
                .withParameter(LootContextParams.TOOL, normalCropHarvestTool());
        List<ItemStack> drops = harvestState.getDrops(context);
        if (!canFitAll(output, drops)) {
            return false;
        }
        for (ItemStack drop : drops) {
            insertIntoOutput(output, drop.copy());
        }
        output.setChanged();
        return true;
    }

    private static boolean isMushroomColonyState(BlockState state) {
        return mushroomItemForColony(state) != null;
    }

    private static ResourceLocation mushroomItemForColony(BlockState state) {
        if (state == null) {
            return null;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (RED_MUSHROOM_COLONY_ID.equals(id)) {
            return RED_MUSHROOM_ITEM_ID;
        }
        if (BROWN_MUSHROOM_COLONY_ID.equals(id)) {
            return BROWN_MUSHROOM_ITEM_ID;
        }
        return null;
    }

    private boolean ageStemCrop(ServerLevel level, RegistryAccess registries) {
        BlockState stem = easyVillagers.getCrop(registries);
        if (!isStemState(stem))
            return false;

        int age = getAge(stem);
        int maxAge = maxAge(stem);
        if (age < maxAge) {
            easyVillagers.setCropState(withAge(stem, age + 1), registries);
            fruitReady = false;
            return true;
        }

        if (!fruitReady) {
            Block fruit = fruitBlockForStem(stem, level.random);
            if (fruit == null || fruit == Blocks.AIR) {
                return false;
            }
            stemFruitId = BuiltInRegistries.BLOCK.getKey(fruit);
            fruitReady = true;
            setChanged();
            return true;
        }
        return false;
    }

    private boolean harvestReadyStem(ServerLevel level, RegistryAccess registries) {
        BlockState stem = easyVillagers.getCrop(registries);
        if (!fruitReady || !isStemState(stem))
            return false;

        if (!hasAdultFarmerVillager(registries)) {
            return false;
        }
        if (!FarmerToolSupport.isAxe(harvestTool)) {
            if (harvestTransactionActive) {
                harvestWaitingForTool = true;
            }
            return false;
        }

        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null)
            return false;

        Block fruit = stemFruitId == null ? null : BuiltInRegistries.BLOCK.get(stemFruitId);
        if (fruit == null || fruit == Blocks.AIR) {
            fruit = fruitBlockForStem(stem, level.random);
        }
        if (fruit == null || fruit == Blocks.AIR)
            return false;
        BlockState fruitState = fruit.defaultBlockState();
        LootParams.Builder context = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.BLOCK_STATE, fruitState)
                .withParameter(LootContextParams.TOOL, stemHarvestTool());
        List<ItemStack> drops = fruitState.getDrops(context);
        if (!canFitAll(output, drops))
            return false;

        for (ItemStack drop : drops)
            insertIntoOutput(output, drop.copy());
        output.setChanged();
        fruitReady = false;
        stemFruitId = null;
        damageHarvestTool(level);
        level.playSound(null, worldPosition, fruitState.getSoundType().getBreakSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
        return true;
    }

    private boolean isNocturnalMilletState(BlockState state) {
        return state != null && NOCTURNAL_MILLET_STALK_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private static boolean isForgottenNocturnalMillet(BlockState state) {
        if (state == null) {
            return false;
        }
        Optional<Property<?>> property = state.getProperties().stream()
                .filter(candidate -> candidate.getName().equals("forgotten"))
                .findFirst();
        return property.isPresent()
                && property.get() instanceof BooleanProperty booleanProperty
                && state.getValue(booleanProperty);
    }

    private static boolean isHearthCornState(BlockState state) {
        return state != null && HEARTH_CORN_STALK_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private boolean hasHarvestReadyHearthCorn(BlockState bottom) {
        if (!isHearthCornState(bottom)) {
            return false;
        }
        // Lower sections may mature only after the top reaches age 2.
        boolean structureUnlocked = hearthCornTopAge >= 2;
        return hearthCornTopAge >= 4
                || (structureUnlocked && (getAge(bottom) >= 4 || hearthCornMiddleAge >= 4));
    }

    private boolean ageHearthCorn(ServerLevel level, RegistryAccess registries, BlockState bottom) {
        if (!isHearthCornState(bottom) || hasHarvestReadyHearthCorn(bottom)) {
            return false;
        }

        int bottomAge = getAge(bottom);
        if (bottomAge < 3) {
            int nextAge = bottomAge + 1;
            easyVillagers.setCropState(
                    withSerializedProperty(withAge(bottom, nextAge), "section", "bottom"), registries);
            if (nextAge == 3 && hearthCornMiddleAge < 0) {
                hearthCornMiddleAge = 0;
            }
            return true;
        }

        // Repair old one-block Corn saves by rebuilding the missing upper sections first.
        if (hearthCornMiddleAge < 0) {
            hearthCornMiddleAge = 0;
            return true;
        }
        if (hearthCornMiddleAge < 3) {
            hearthCornMiddleAge++;
            if (hearthCornMiddleAge == 3 && hearthCornTopAge < 0) {
                hearthCornTopAge = 0;
            }
            return true;
        }
        if (hearthCornTopAge < 0) {
            hearthCornTopAge = 0;
            return true;
        }
        if (hearthCornTopAge < 2) {
            hearthCornTopAge++;
            return true;
        }

        // After top age 2, advance one random unfinished section per growth pulse.
        int candidates = 0;
        if (bottomAge < 5) candidates++;
        if (hearthCornMiddleAge < 5) candidates++;
        if (hearthCornTopAge < 5) candidates++;
        if (candidates == 0) {
            return false;
        }
        int pick = level.random.nextInt(candidates);
        if (bottomAge < 5) {
            if (pick == 0) {
                easyVillagers.setCropState(
                        withSerializedProperty(withAge(bottom, bottomAge + 1), "section", "bottom"), registries);
                return true;
            }
            pick--;
        }
        if (hearthCornMiddleAge < 5) {
            if (pick == 0) {
                hearthCornMiddleAge++;
                return true;
            }
            pick--;
        }
        if (hearthCornTopAge < 5) {
            hearthCornTopAge++;
            return true;
        }
        return false;
    }

    private boolean harvestHearthCorn(ServerLevel level, RegistryAccess registries, BlockState bottom) {
        if (!isHearthCornState(bottom) || !hasAdultFarmerVillager(registries)) {
            return false;
        }
        boolean structureUnlocked = hearthCornTopAge >= 2;
        boolean harvestBottom = structureUnlocked && getAge(bottom) >= 4;
        boolean harvestMiddle = structureUnlocked && hearthCornMiddleAge >= 4;
        boolean harvestTop = hearthCornTopAge >= 4;
        if (!harvestBottom && !harvestMiddle && !harvestTop) {
            return false;
        }

        int count = 0;
        if (harvestBottom) count += getAge(bottom) >= 5 ? 2 : 1;
        if (harvestMiddle) count += hearthCornMiddleAge >= 5 ? 2 : 1;
        if (harvestTop) count += hearthCornTopAge >= 5 ? 2 : 1;

        Item corn = BuiltInRegistries.ITEM.get(HEARTH_CORN_ITEM_ID);
        if (corn == null || corn == Items.AIR) {
            return false;
        }
        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return false;
        }
        ItemStack harvest = new ItemStack(corn, count);
        if (!canFitAll(output, List.of(harvest))) {
            return false;
        }
        insertIntoOutput(output, harvest);
        output.setChanged();

        // Each harvested section returns independently to age 3.
        if (harvestBottom) {
            easyVillagers.setCropState(withSerializedProperty(withAge(bottom, 3), "section", "bottom"), registries);
        }
        if (harvestMiddle) hearthCornMiddleAge = 3;
        if (harvestTop) hearthCornTopAge = 3;
        level.playSound(null, worldPosition, SoundEvents.VILLAGER_WORK_FARMER, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private boolean ageNocturnalMillet(RegistryAccess registries, BlockState crop) {
        if (!isNocturnalMilletState(crop)) {
            return false;
        }
        int age = getAge(crop);
        int maxAge = maxAge(crop);
        if (age < maxAge) {
            int newAge = Math.min(maxAge, age + 1);
            BlockState next = withAge(crop, newAge);
            // Rich Farmers cannot expose Eternal Starlight's Dusted Gravel + bone-meal
            // interaction. Once the stalk itself is fully mature, virtual farming
            // performs that conversion and only then starts growing the panicle.
            if (newAge >= maxAge) {
                next = withBooleanProperty(next, "forgotten", true);
            }
            easyVillagers.setCropState(next, registries);
            nocturnalMilletPanicleAge = -1;
            return true;
        }
        if (!isForgottenNocturnalMillet(crop)) {
            easyVillagers.setCropState(withBooleanProperty(crop, "forgotten", true), registries);
            return true;
        }
        if (nocturnalMilletPanicleAge < 0) {
            nocturnalMilletPanicleAge = 0;
            return true;
        }
        if (nocturnalMilletPanicleAge < 2) {
            nocturnalMilletPanicleAge++;
            return true;
        }
        return false;
    }

    private boolean harvestNocturnalMillet(RegistryAccess registries) {
        if (nocturnalMilletPanicleAge < 2 || !hasAdultFarmerVillager(registries)) {
            return false;
        }
        BlockState crop = easyVillagers.getCrop(registries);
        if (!isForgottenNocturnalMillet(crop)) {
            return false;
        }
        Item millet = BuiltInRegistries.ITEM.get(FORGOTTEN_NOCTURNAL_MILLET_ITEM_ID);
        if (millet == null || millet == Items.AIR) {
            return false;
        }
        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return false;
        }
        ItemStack harvest = new ItemStack(millet);
        if (!canFitAll(output, List.of(harvest))) {
            return false;
        }
        insertIntoOutput(output, harvest);
        output.setChanged();
        // Forgotten panicles survive harvesting and return to age 1, matching
        // Eternal Starlight's persistent Forgotten crop cycle.
        nocturnalMilletPanicleAge = 1;
        return true;
    }

    private boolean harvestMatureSugarCane(RegistryAccess registries) {
        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null)
            return false;
        ItemStack harvest = new ItemStack(Items.SUGAR_CANE, 2);
        if (!canFitAll(output, List.of(harvest)))
            return false;
        insertIntoOutput(output, harvest);
        output.setChanged();
        return true;
    }

    private void damageHarvestTool(ServerLevel level) {
        if (harvestTool.isEmpty() || !harvestTool.isDamageableItem())
            return;
        if (
        harvestTool.hurt(1, level.random, null)) {
            harvestTool.shrink(1);
            harvestTool.setDamageValue(0);
                level.playSound(null, worldPosition, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        setChanged();
    }

    private static boolean isStemState(BlockState state) {
        return state != null && state.getBlock() instanceof StemBlock;
    }

    private StemCropDefinition currentStemDefinition(BlockState state) {
        if (!variant().isRich() || variant().isAquatic() || state == null) {
            return null;
        }
        StemCropDefinition stored = StemCropDefinitions.get(stemDefinitionId).orElse(null);
        if (stored != null && stored.matchesStem(state)) {
            return stored;
        }
        return StemCropDefinitions.findStem(state).orElse(null);
    }

    private Item seedItemForStem(BlockState state) {
        if (state == null)
            return null;
        if (state.is(Blocks.MELON_STEM))
            return Items.MELON_SEEDS;
        if (state.is(Blocks.PUMPKIN_STEM))
            return Items.PUMPKIN_SEEDS;
        if (selectedPlantingItemId != null) {
            Item item = BuiltInRegistries.ITEM.get(selectedPlantingItemId);
            if (item != null && item != Items.AIR) {
                return item;
            }
        }
        StemCropDefinition definition = currentStemDefinition(state);
        if (definition != null) {
            ItemStack planting = definition.canonicalPlantingStack();
            if (!planting.isEmpty()) {
                return planting.getItem();
            }
        }
        Item inferred = state.getBlock().asItem();
        return inferred == Items.AIR ? null : inferred;
    }

    private Block fruitBlockForStem(BlockState state, net.minecraft.util.RandomSource random) {
        if (state == null)
            return null;
        if (state.is(Blocks.MELON_STEM))
            return Blocks.MELON;
        if (state.is(Blocks.PUMPKIN_STEM))
            return Blocks.PUMPKIN;
        if (DEEP_AETHER_SQUASH_STEM_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()))) {
            ResourceLocation fruitId = random.nextBoolean() ? DEEP_AETHER_BLUE_SQUASH_ID : DEEP_AETHER_GREEN_SQUASH_ID;
            Block fruit = BuiltInRegistries.BLOCK.get(fruitId);
            if (fruit != null && fruit != Blocks.AIR) {
                return fruit;
            }
        }
        StemCropDefinition definition = currentStemDefinition(state);
        if (definition != null) {
            return definition.randomFruit(random);
        }
        return reflectedFruitBlockForStem(state.getBlock());
    }

    private static Block reflectedFruitBlockForStem(Block stemBlock) {
        if (!(stemBlock instanceof StemBlock)) {
            return null;
        }
        try {
            Field fruitField = StemBlock.class.getDeclaredField("fruit");
            if (!fruitField.canAccess(stemBlock)) {
                fruitField.setAccessible(true);
            }
            Object value = fruitField.get(stemBlock);
            if (value instanceof Block block) {
                return block;
            }
            if (value instanceof ResourceKey<?> key) {
                Block block = BuiltInRegistries.BLOCK.get(key.location());
                return block == Blocks.AIR ? null : block;
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // A data-driven StemCropDefinition remains available for stems whose internals differ.
        }
        return null;
    }

    private static int maxAge(BlockState state) {
        return state.getProperties().stream()
                .filter(property -> property.getName().equals("age"))
                .filter(IntegerProperty.class::isInstance)
                .map(IntegerProperty.class::cast)
                .findFirst()
                .map(property -> property.getPossibleValues().stream().max(Integer::compareTo).orElse(0))
                .orElse(0);
    }

    private static boolean isTomatoState(BlockState state) {
        if (state == null) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return BUDDING_TOMATO_ID.equals(id) || TOMATO_CROP_ID.equals(id);
    }

    private static boolean isMatureAgeState(BlockState state) {
        if (state == null) {
            return false;
        }
        return state.getProperties().stream()
                .filter(property -> property.getName().equals("age"))
                .filter(IntegerProperty.class::isInstance)
                .map(IntegerProperty.class::cast)
                .findFirst()
                .map(property -> {
                    int max = property.getPossibleValues().stream().max(Integer::compareTo).orElse(Integer.MAX_VALUE);
                    return state.getValue(property) >= max;
                })
                .orElse(false);
    }

    private static int getAge(BlockState state) {
        return state.getProperties().stream()
                .filter(property -> property.getName().equals("age"))
                .filter(IntegerProperty.class::isInstance)
                .map(IntegerProperty.class::cast)
                .findFirst()
                .map(state::getValue)
                .orElse(0);
    }

    private boolean harvestMatureRice(ServerLevel level, RegistryAccess registries) {
        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) {
            return false;
        }

        Block panicles = BuiltInRegistries.BLOCK.get(RICE_PANICLES_ID);
        BlockState mature = withAge(panicles.defaultBlockState(), 3);

        LootParams.Builder context = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.BLOCK_STATE, mature)
                .withParameter(LootContextParams.TOOL, riceHarvestTool());

        List<ItemStack> drops = mature.getDrops(context);
        if (!canFitAll(output, drops)) {
            return false;
        }
        for (ItemStack drop : drops) {
            insertIntoOutput(output, drop.copy());
        }
        output.setChanged();
        return true;
    }

    private void syncRiceCropState(RegistryAccess registries) {
        if (!easyVillagers.hasRiceCrop(registries)) {
            return;
        }
        Block riceCrop = BuiltInRegistries.BLOCK.get(RICE_CROP_ID);
        BlockState lower = withAge(riceCrop.defaultBlockState(), Math.min(3, paddyGrowth));
        easyVillagers.setRiceCropState(lower, registries);
    }

    private static BlockState withBooleanProperty(BlockState state, String name, boolean value) {
        Optional<Property<?>> property = state.getProperties().stream()
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst();
        if (property.isPresent() && property.get() instanceof BooleanProperty booleanProperty) {
            return state.setValue(booleanProperty, value);
        }
        return state;
    }

    private static BlockState withIntegerProperty(BlockState state, String name, int value) {
        Optional<Property<?>> property = state.getProperties().stream()
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst();
        if (property.isPresent() && property.get() instanceof IntegerProperty integerProperty) {
            int min = integerProperty.getPossibleValues().stream().min(Integer::compareTo).orElse(value);
            int max = integerProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(value);
            return state.setValue(integerProperty, Math.max(min, Math.min(max, value)));
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState setSerializedProperty(
            BlockState state, Property<T> property, String serializedValue
    ) {
        return property.getValue(serializedValue)
                .map(value -> state.setValue(property, value))
                .orElse(state);
    }

    private static BlockState withSerializedProperty(BlockState state, String name, String serializedValue) {
        Optional<Property<?>> property = state.getProperties().stream()
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst();
        return property.map(candidate -> setSerializedProperty(state, candidate, serializedValue)).orElse(state);
    }

    private static BlockState withDirectionProperty(BlockState state, String name, Direction value) {
        Optional<Property<?>> property = state.getProperties().stream()
                .filter(candidate -> candidate.getName().equals(name))
                .findFirst();
        if (property.isPresent()
                && property.get() instanceof DirectionProperty directionProperty) {
            return state.setValue(directionProperty, value);
        }
        return state;
    }

    private static BlockState withAge(BlockState state, int age) {
        Optional<Property<?>> ageProperty = state.getProperties().stream()
                .filter(property -> property.getName().equals("age"))
                .findFirst();
        if (ageProperty.isEmpty() || !(ageProperty.get() instanceof IntegerProperty integerProperty)) {
            return state;
        }

        int max = integerProperty.getPossibleValues().stream().max(Integer::compareTo).orElse(0);
        int safeAge = Math.max(0, Math.min(max, age));
        return state.setValue(integerProperty, safeAge);
    }

    private boolean canFitAll(Container output, List<ItemStack> stacks) {
        boolean fits = canFitAllPure(output, stacks);
        if (!fits && harvestTransactionActive) {
            harvestWaitingForOutputSpace = true;
            blockedOutputRequirement = stacks.stream()
                    .filter(stack -> stack != null && !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
        }
        return fits;
    }

    private static boolean hasGuaranteedEmptySlotCapacity(Container output, List<ItemStack> stacks) {
        int emptySlots = 0;
        for (int slot = 0; slot < output.getContainerSize(); slot++) {
            if (output.getItem(slot).isEmpty()) {
                emptySlots++;
            }
        }

        int requiredSlots = 0;
        int containerLimit = Math.max(1, output.getMaxStackSize());
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            int perSlot = Math.max(1, Math.min(stack.getMaxStackSize(), containerLimit));
            requiredSlots += (stack.getCount() + perSlot - 1) / perSlot;
            if (requiredSlots > emptySlots) {
                return false;
            }
        }
        return true;
    }

    private static boolean canFitAllPure(Container output, List<ItemStack> stacks) {
        ItemStack[] simulated = new ItemStack[output.getContainerSize()];
        for (int slot = 0; slot < simulated.length; slot++) {
            simulated[slot] = output.getItem(slot).copy();
        }

        for (ItemStack source : stacks) {
            ItemStack remaining = source.copy();
            for (int slot = 0; slot < simulated.length && !remaining.isEmpty(); slot++) {
                ItemStack existing = simulated[slot];
                if (existing.isEmpty()) {
                    int move = Math.min(
                            remaining.getCount(),
                            Math.min(remaining.getMaxStackSize(), output.getMaxStackSize())
                    );
                    simulated[slot] = remaining.copyWithCount(move);
                    remaining.shrink(move);
                    continue;
                }

                if (!ItemStack.isSameItemSameTags(existing, remaining)) {
                    continue;
                }

                int max = Math.min(existing.getMaxStackSize(), output.getMaxStackSize());
                int room = max - existing.getCount();
                if (room <= 0) {
                    continue;
                }

                int move = Math.min(room, remaining.getCount());
                existing.grow(move);
                remaining.shrink(move);
            }

            if (!remaining.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void insertIntoOutput(Container output, ItemStack stack) {
        for (int slot = 0; slot < output.getContainerSize() && !stack.isEmpty(); slot++) {
            ItemStack existing = output.getItem(slot);
            if (existing.isEmpty()) {
                int move = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), output.getMaxStackSize()));
                output.setItem(slot, stack.copyWithCount(move));
                stack.shrink(move);
                continue;
            }

            if (!ItemStack.isSameItemSameTags(existing, stack)) {
                continue;
            }

            int max = Math.min(existing.getMaxStackSize(), output.getMaxStackSize());
            int room = max - existing.getCount();
            if (room <= 0) {
                continue;
            }

            int move = Math.min(room, stack.getCount());
            existing.grow(move);
            stack.shrink(move);
        }
    }

    @Override
    public void setChanged() {
        syncVisibleState();
        if (!harvestTransactionActive) {
            harvestStateChanged = true;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        passthroughData = tag.copy();
        stripMetadata(passthroughData);
        easyVillagers.reset();
        itemCapability.invalidate();
        itemCapability = LazyOptional.empty();

        if (tag.contains(KEY_PADDY_GROWTH))
            paddyGrowth = Math.max(0, Math.min(MAX_PADDY_GROWTH, tag.getInt(KEY_PADDY_GROWTH))); else
            paddyGrowth = inferLegacyRiceGrowth(tag);

        baseProgress = Math.max(0, tag.getInt(KEY_BASE_PROGRESS));
        ropeOneProgress = Math.max(0, tag.getInt(KEY_ROPE_ONE_PROGRESS));
        ropeTwoProgress = Math.max(0, tag.getInt(KEY_ROPE_TWO_PROGRESS));
        ropeCount = Math.max(0, Math.min(2, tag.getInt(KEY_ROPE_COUNT)));
        ropeOnePlanted = ropeCount >= 1 && (tag.contains(KEY_ROPE_ONE_PLANTED)
                ? tag.getBoolean(KEY_ROPE_ONE_PLANTED)
                : true);
        ropeTwoPlanted = ropeCount >= 2 && (tag.contains(KEY_ROPE_TWO_PLANTED)
                ? tag.getBoolean(KEY_ROPE_TWO_PLANTED)
                : true);

        fruitReady = tag.getBoolean(KEY_FRUIT_READY);
        paddySand = variant().isAquatic() && tag.getBoolean(KEY_PADDY_SAND);
        sugarCaneHeight = paddySand ? Math.max(0, Math.min(3, tag.getInt(KEY_SUGAR_CANE_HEIGHT))) : 0;
        sugarCaneAge = paddySand && sugarCaneHeight > 0 && sugarCaneHeight < 3
                ? Math.max(0, Math.min(MAX_SUGAR_CANE_AGE, tag.getInt(KEY_SUGAR_CANE_AGE)))
                : 0;

        CompoundTag toolTag = null;
        if (tag.contains(KEY_HARVEST_TOOL, net.minecraft.nbt.Tag.TAG_COMPOUND))
            toolTag = tag.getCompound(KEY_HARVEST_TOOL);
        else if (tag.contains(LEGACY_EFDC_KNIFE, net.minecraft.nbt.Tag.TAG_COMPOUND))
            toolTag = tag.getCompound(LEGACY_EFDC_KNIFE);
        harvestTool = variant().isRich() && toolTag != null
                ? FarmerToolSupport.normalizeHarvestTool(ItemStack.of(toolTag))
                : ItemStack.EMPTY;
        loadAttachedState(tag);
        regrowingDefinitionId = tag.contains(KEY_REGROWING_DEFINITION)
                ? ResourceLocation.tryParse(tag.getString(KEY_REGROWING_DEFINITION))
                : null;
        regrowingPlantingItemId = tag.contains(KEY_REGROWING_PLANTING_ITEM)
                ? ResourceLocation.tryParse(tag.getString(KEY_REGROWING_PLANTING_ITEM))
                : null;
        selectedPlantingItemId = tag.contains(KEY_SELECTED_PLANTING_ITEM)
                ? ResourceLocation.tryParse(tag.getString(KEY_SELECTED_PLANTING_ITEM))
                : regrowingPlantingItemId;
        stemDefinitionId = tag.contains(KEY_STEM_DEFINITION)
                ? ResourceLocation.tryParse(tag.getString(KEY_STEM_DEFINITION))
                : null;
        tallDefinitionId = tag.contains(KEY_TALL_DEFINITION)
                ? ResourceLocation.tryParse(tag.getString(KEY_TALL_DEFINITION))
                : null;
        stemFruitId = tag.contains(KEY_STEM_FRUIT)
                ? ResourceLocation.tryParse(tag.getString(KEY_STEM_FRUIT))
                : null;
        nocturnalMilletPanicleAge = tag.contains(KEY_NOCTURNAL_MILLET_PANICLE_AGE)
                ? Math.max(-1, Math.min(2, tag.getInt(KEY_NOCTURNAL_MILLET_PANICLE_AGE)))
                : -1;
        hearthCornMiddleAge = tag.contains(KEY_HEARTH_CORN_MIDDLE_AGE)
                ? Math.max(-1, Math.min(5, tag.getInt(KEY_HEARTH_CORN_MIDDLE_AGE)))
                : -1;
        hearthCornTopAge = tag.contains(KEY_HEARTH_CORN_TOP_AGE)
                ? Math.max(-1, Math.min(5, tag.getInt(KEY_HEARTH_CORN_TOP_AGE)))
                : -1;
        graftingSupport = supportsOrchard() && tag.getBoolean(KEY_GRAFTING_SUPPORT);
        orchardDefinitionId = graftingSupport && tag.contains(KEY_ORCHARD_DEFINITION)
                ? ResourceLocation.tryParse(tag.getString(KEY_ORCHARD_DEFINITION))
                : null;
        orchardPlantingItemId = orchardDefinitionId != null && tag.contains(KEY_ORCHARD_PLANTING_ITEM)
                ? ResourceLocation.tryParse(tag.getString(KEY_ORCHARD_PLANTING_ITEM))
                : null;
        OrchardCropDefinition loadedOrchard = OrchardCropDefinitions.get(orchardDefinitionId).orElse(null);
        orchardRenderBlockId = orchardDefinitionId != null && tag.contains(KEY_ORCHARD_RENDER_BLOCK)
                ? ResourceLocation.tryParse(tag.getString(KEY_ORCHARD_RENDER_BLOCK))
                : loadedOrchard == null ? null : loadedOrchard.renderBlockId();
        orchardAgeProperty = orchardDefinitionId != null && tag.contains(KEY_ORCHARD_AGE_PROPERTY)
                ? tag.getString(KEY_ORCHARD_AGE_PROPERTY)
                : loadedOrchard == null ? "" : loadedOrchard.ageProperty();
        orchardRenderStyle = loadedOrchard == null
                ? parseOrchardRenderStyle(tag.getString(KEY_ORCHARD_RENDER_STYLE))
                : loadedOrchard.renderStyle();
        orchardHarvestItemId = orchardDefinitionId != null && tag.contains(KEY_ORCHARD_HARVEST_ITEM)
                ? ResourceLocation.tryParse(tag.getString(KEY_ORCHARD_HARVEST_ITEM))
                : loadedOrchard == null ? null : loadedOrchard.harvestItemId();
        orchardMatureAge = loadedOrchard == null
                ? Math.max(1, tag.contains(KEY_ORCHARD_MATURE_AGE) ? tag.getInt(KEY_ORCHARD_MATURE_AGE) : 3)
                : loadedOrchard.matureAge();
        int orchardMin = loadedOrchard == null ? 0 : loadedOrchard.minAge();
        int orchardMax = loadedOrchard == null ? Math.max(orchardMatureAge, 3) : loadedOrchard.maxAge();
        orchardAge = orchardDefinitionId == null ? 0
                : Math.max(orchardMin, Math.min(orchardMax, tag.getInt(KEY_ORCHARD_AGE)));
        orchardPendingHarvest = orchardDefinitionId != null
                && tag.contains(KEY_ORCHARD_PENDING_HARVEST, Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound(KEY_ORCHARD_PENDING_HARVEST))
                : ItemStack.EMPTY;

        // Remove orchard states that are no longer productive under the current compatibility set.
        // The Grafting Support itself remains installed. Durian is structurally unsupported, while
        // vanilla Oak/Dark Oak Apple orchards are only a fallback when no dedicated Apple mod is loaded.
        if (OrchardCropDefinitions.isExplicitlyExcludedDefinition(orchardDefinitionId)
                || OrchardCropDefinitions.isExplicitlyExcludedResource(orchardPlantingItemId)
                || OrchardCropDefinitions.isExplicitlyExcludedResource(orchardRenderBlockId)
                || OrchardCropDefinitions.isRuntimeSuppressedDefinition(orchardDefinitionId)
                || OrchardCropDefinitions.isRuntimeSuppressedResource(orchardPlantingItemId)
                || OrchardCropDefinitions.isRuntimeSuppressedResource(orchardRenderBlockId)) {
            clearOrchardCropState();
        }
        harvestRetryRequested = true;
        harvestStateChanged = false;
        harvestWaitingForOutputSpace = false;
        harvestWaitingForTool = false;
        harvestWaitingForAdultVillager = false;
        blockedOutputRequirement = List.of();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        RegistryAccess registries = level != null ? level.registryAccess() : null;

        passthroughData = easyVillagers.snapshot(passthroughData, registries);
        CompoundTag preserved = passthroughData.copy();
        stripMetadata(preserved);
        stripAddonKeys(preserved);
        tag.merge(preserved);

        tag.putInt(KEY_SCHEMA, 14);
        tag.putInt(KEY_PADDY_GROWTH, paddyGrowth);
        tag.putInt(KEY_BASE_PROGRESS, baseProgress);
        tag.putInt(KEY_ROPE_ONE_PROGRESS, ropeOneProgress);
        tag.putInt(KEY_ROPE_TWO_PROGRESS, ropeTwoProgress);
        tag.putBoolean(KEY_ROPE_ONE_PLANTED, ropeCount >= 1 && ropeOnePlanted);
        tag.putBoolean(KEY_ROPE_TWO_PLANTED, ropeCount >= 2 && ropeTwoPlanted);
        tag.putInt(KEY_ROPE_COUNT, ropeCount);
        tag.putBoolean(KEY_FRUIT_READY, fruitReady);
        tag.putBoolean(KEY_PADDY_SAND, variant().isAquatic() && paddySand);
        tag.putInt(KEY_SUGAR_CANE_HEIGHT, variant().isAquatic() && paddySand ? sugarCaneHeight : 0);
        tag.putInt(KEY_SUGAR_CANE_AGE, variant().isAquatic() && paddySand ? sugarCaneAge : 0);
        if (variant().isRich() && !variant().isAquatic() && regrowingDefinitionId != null) {
            tag.putString(KEY_REGROWING_DEFINITION, regrowingDefinitionId.toString());
        } else {
            tag.remove(KEY_REGROWING_DEFINITION);
        }
        if (variant().isRich() && !variant().isAquatic() && regrowingPlantingItemId != null) {
            tag.putString(KEY_REGROWING_PLANTING_ITEM, regrowingPlantingItemId.toString());
        } else {
            tag.remove(KEY_REGROWING_PLANTING_ITEM);
        }
        if (variant().isRich() && !variant().isAquatic() && selectedPlantingItemId != null) {
            tag.putString(KEY_SELECTED_PLANTING_ITEM, selectedPlantingItemId.toString());
        } else {
            tag.remove(KEY_SELECTED_PLANTING_ITEM);
        }
        if (variant().isRich() && !variant().isAquatic() && stemDefinitionId != null) {
            tag.putString(KEY_STEM_DEFINITION, stemDefinitionId.toString());
        } else {
            tag.remove(KEY_STEM_DEFINITION);
        }
        if (variant().isRich() && !variant().isAquatic() && tallDefinitionId != null) {
            tag.putString(KEY_TALL_DEFINITION, tallDefinitionId.toString());
        } else {
            tag.remove(KEY_TALL_DEFINITION);
        }
        if (variant().isRich() && !variant().isAquatic() && stemFruitId != null) {
            tag.putString(KEY_STEM_FRUIT, stemFruitId.toString());
        } else {
            tag.remove(KEY_STEM_FRUIT);
        }
        BlockState persistedCrop = easyVillagers.getCrop(registries);
        if (variant().isRich() && !variant().isAquatic()
                && isNocturnalMilletState(persistedCrop) && nocturnalMilletPanicleAge >= 0) {
            tag.putInt(KEY_NOCTURNAL_MILLET_PANICLE_AGE, nocturnalMilletPanicleAge);
        } else {
            tag.remove(KEY_NOCTURNAL_MILLET_PANICLE_AGE);
        }
        if (variant().isRich() && !variant().isAquatic() && isHearthCornState(persistedCrop)) {
            tag.putInt(KEY_HEARTH_CORN_MIDDLE_AGE, hearthCornMiddleAge);
            tag.putInt(KEY_HEARTH_CORN_TOP_AGE, hearthCornTopAge);
        } else {
            tag.remove(KEY_HEARTH_CORN_MIDDLE_AGE);
            tag.remove(KEY_HEARTH_CORN_TOP_AGE);
        }
        if (supportsOrchard() && graftingSupport) {
            tag.putBoolean(KEY_GRAFTING_SUPPORT, true);
        } else {
            tag.remove(KEY_GRAFTING_SUPPORT);
        }
        if (supportsOrchard() && graftingSupport && orchardDefinitionId != null) {
            tag.putString(KEY_ORCHARD_DEFINITION, orchardDefinitionId.toString());
            tag.putInt(KEY_ORCHARD_AGE, orchardAge);
        } else {
            tag.remove(KEY_ORCHARD_DEFINITION);
            tag.remove(KEY_ORCHARD_AGE);
        }
        if (supportsOrchard() && graftingSupport && orchardPlantingItemId != null) {
            tag.putString(KEY_ORCHARD_PLANTING_ITEM, orchardPlantingItemId.toString());
        } else {
            tag.remove(KEY_ORCHARD_PLANTING_ITEM);
        }
        if (supportsOrchard() && graftingSupport && orchardDefinitionId != null && orchardRenderBlockId != null) {
            tag.putString(KEY_ORCHARD_RENDER_BLOCK, orchardRenderBlockId.toString());
            tag.putString(KEY_ORCHARD_AGE_PROPERTY, orchardAgeProperty == null ? "" : orchardAgeProperty);
            tag.putString(KEY_ORCHARD_RENDER_STYLE, orchardRenderStyle.name());
            if (orchardHarvestItemId != null) {
                tag.putString(KEY_ORCHARD_HARVEST_ITEM, orchardHarvestItemId.toString());
            } else {
                tag.remove(KEY_ORCHARD_HARVEST_ITEM);
            }
            tag.putInt(KEY_ORCHARD_MATURE_AGE, orchardMatureAge);
        } else {
            tag.remove(KEY_ORCHARD_RENDER_BLOCK);
            tag.remove(KEY_ORCHARD_AGE_PROPERTY);
            tag.remove(KEY_ORCHARD_RENDER_STYLE);
            tag.remove(KEY_ORCHARD_HARVEST_ITEM);
            tag.remove(KEY_ORCHARD_MATURE_AGE);
        }
        if (supportsOrchard() && graftingSupport && orchardDefinitionId != null && !orchardPendingHarvest.isEmpty()) {
            tag.put(KEY_ORCHARD_PENDING_HARVEST, orchardPendingHarvest.save(new CompoundTag()));
        } else {
            tag.remove(KEY_ORCHARD_PENDING_HARVEST);
        }
        saveAttachedState(tag);
        tag.remove(LEGACY_EFDC_KNIFE);
        if (variant().isRich() && FarmerToolSupport.isHarvestTool(harvestTool))
            tag.put(KEY_HARVEST_TOOL, harvestTool.save(new CompoundTag())); else
            tag.remove(KEY_HARVEST_TOOL);
        }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (!remove && capability == ForgeCapabilities.ITEM_HANDLER) {
            IItemHandler handler = getItemHandler();
            if (handler != null) {
                if (!itemCapability.isPresent())
                    itemCapability = LazyOptional.of(() -> handler);
                return itemCapability.cast();
            }
        }
        return super.getCapability(capability, side);
    }
    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemCapability.invalidate();
        itemCapability = LazyOptional.empty();
    }
    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemCapability = LazyOptional.empty();
    }

    private void loadAttachedState(CompoundTag tag) {
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            attachedHostIds[levelIndex] = null;
            clearAttachedLevelCrops(levelIndex);
        }
        if (!supportsAttachedCrops()) {
            return;
        }

        ListTag hosts = tag.getList(KEY_ATTACHED_HOSTS, Tag.TAG_COMPOUND);
        for (int index = 0; index < hosts.size(); index++) {
            CompoundTag entry = hosts.getCompound(index);
            int levelIndex = entry.getInt("Level");
            ResourceLocation id = ResourceLocation.tryParse(entry.getString("Block"));
            if (levelIndex < 0 || levelIndex >= ATTACHED_LEVEL_COUNT || id == null) {
                continue;
            }
            Block block = BuiltInRegistries.BLOCK.get(id);
            if (block != null && block != Blocks.AIR) {
                attachedHostIds[levelIndex] = id;
            }
        }

        ListTag crops = tag.getList(KEY_ATTACHED_CROPS, Tag.TAG_COMPOUND);
        for (int index = 0; index < crops.size(); index++) {
            CompoundTag entry = crops.getCompound(index);
            int levelIndex = entry.getInt("Level");
            int faceIndex = entry.getInt("Face");
            ResourceLocation cropId = ResourceLocation.tryParse(entry.getString("Crop"));
            ResourceLocation definitionId = ResourceLocation.tryParse(entry.getString("Definition"));
            if (definitionId == null && cropId != null
                    && "minecraft:cocoa".equals(cropId.toString())) {
                definitionId = COCOA_DEFINITION_ID;
            }
            AttachedCropDefinition definition = AttachedCropDefinitions.get(definitionId).orElse(null);
            if (levelIndex < 0 || levelIndex >= ATTACHED_LEVEL_COUNT
                    || faceIndex < 0 || faceIndex >= ATTACHED_FACE_COUNT
                    || attachedHostIds[levelIndex] == null
                    || cropId == null) {
                continue;
            }
            attachedDefinitionIds[levelIndex][faceIndex] = definitionId;
            attachedCropIds[levelIndex][faceIndex] = cropId;
            ResourceLocation plantingId = ResourceLocation.tryParse(entry.getString("PlantingItem"));
            attachedPlantingItemIds[levelIndex][faceIndex] = plantingId;
            attachedAgeProperties[levelIndex][faceIndex] = entry.contains("AgeProperty")
                    ? entry.getString("AgeProperty")
                    : definition != null ? definition.ageProperty() : "age";
            attachedFacingProperties[levelIndex][faceIndex] = entry.contains("FacingProperty")
                    ? entry.getString("FacingProperty")
                    : definition != null ? definition.facingProperty() : "facing";
            int minAge = definition != null ? definition.minAge() : 0;
            int maxAge = definition != null ? definition.maxAge() : Math.max(minAge, entry.getInt("Age"));
            attachedCropAges[levelIndex][faceIndex] = Math.max(minAge, Math.min(maxAge, entry.getInt("Age")));
        }
    }

    private void saveAttachedState(CompoundTag tag) {
        tag.remove(KEY_ATTACHED_HOSTS);
        tag.remove(KEY_ATTACHED_CROPS);
        if (!supportsAttachedCrops() || !hasAttachedSetup()) {
            return;
        }

        ListTag hosts = new ListTag();
        ListTag crops = new ListTag();
        for (int levelIndex = 0; levelIndex < ATTACHED_LEVEL_COUNT; levelIndex++) {
            ResourceLocation hostId = attachedHostIds[levelIndex];
            if (hostId == null) {
                continue;
            }
            CompoundTag host = new CompoundTag();
            host.putInt("Level", levelIndex);
            host.putString("Block", hostId.toString());
            hosts.add(host);

            for (int faceIndex = 0; faceIndex < ATTACHED_FACE_COUNT; faceIndex++) {
                ResourceLocation cropId = attachedCropIds[levelIndex][faceIndex];
                if (cropId == null) {
                    continue;
                }
                CompoundTag crop = new CompoundTag();
                crop.putInt("Level", levelIndex);
                crop.putInt("Face", faceIndex);
                ResourceLocation definitionId = attachedDefinitionIds[levelIndex][faceIndex];
                if (definitionId != null) {
                    crop.putString("Definition", definitionId.toString());
                }
                crop.putString("Crop", cropId.toString());
                ResourceLocation plantingId = attachedPlantingItemIds[levelIndex][faceIndex];
                if (plantingId != null) {
                    crop.putString("PlantingItem", plantingId.toString());
                }
                String ageProperty = attachedAgeProperties[levelIndex][faceIndex];
                if (ageProperty != null) {
                    crop.putString("AgeProperty", ageProperty);
                }
                String facingProperty = attachedFacingProperties[levelIndex][faceIndex];
                if (facingProperty != null) {
                    crop.putString("FacingProperty", facingProperty);
                }
                crop.putInt("Age", attachedCropAges[levelIndex][faceIndex]);
                crops.add(crop);
            }
        }
        if (!hosts.isEmpty()) {
            tag.put(KEY_ATTACHED_HOSTS, hosts);
        }
        if (!crops.isEmpty()) {
            tag.put(KEY_ATTACHED_CROPS, crops);
        }
    }

    private static int inferLegacyRiceGrowth(CompoundTag tag) {
        if (!tag.contains("Crop")) {
            return 0;
        }
        try {
            BlockState crop = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("Crop"));
            if (!RICE_CROP_ID.equals(BuiltInRegistries.BLOCK.getKey(crop.getBlock()))) {
                return 0;
            }
            return crop.getProperties().stream()
                    .filter(property -> property.getName().equals("age"))
                    .filter(IntegerProperty.class::isInstance)
                    .map(IntegerProperty.class::cast)
                    .findFirst()
                    .map(crop::getValue)
                    .orElse(0);
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    private static void stripAddonKeys(CompoundTag tag) {
        tag.remove(KEY_SCHEMA);
        tag.remove(KEY_PADDY_GROWTH);
        tag.remove(KEY_BASE_PROGRESS);
        tag.remove(KEY_ROPE_ONE_PROGRESS);
        tag.remove(KEY_ROPE_TWO_PROGRESS);
        tag.remove(KEY_ROPE_ONE_PLANTED);
        tag.remove(KEY_ROPE_TWO_PLANTED);
        tag.remove(KEY_ROPE_COUNT);
        tag.remove(KEY_HARVEST_TOOL);
        tag.remove(LEGACY_EFDC_KNIFE);
        tag.remove(KEY_FRUIT_READY);
        tag.remove(KEY_PADDY_SAND);
        tag.remove(KEY_SUGAR_CANE_HEIGHT);
        tag.remove(KEY_SUGAR_CANE_AGE);
        tag.remove(KEY_ATTACHED_HOSTS);
        tag.remove(KEY_ATTACHED_CROPS);
        tag.remove(KEY_REGROWING_DEFINITION);
        tag.remove(KEY_REGROWING_PLANTING_ITEM);
        tag.remove(KEY_SELECTED_PLANTING_ITEM);
        tag.remove(KEY_STEM_DEFINITION);
        tag.remove(KEY_TALL_DEFINITION);
        tag.remove(KEY_STEM_FRUIT);
        tag.remove(KEY_NOCTURNAL_MILLET_PANICLE_AGE);
        tag.remove(KEY_HEARTH_CORN_MIDDLE_AGE);
        tag.remove(KEY_HEARTH_CORN_TOP_AGE);
        tag.remove(KEY_GRAFTING_SUPPORT);
        tag.remove(KEY_ORCHARD_DEFINITION);
        tag.remove(KEY_ORCHARD_PLANTING_ITEM);
        tag.remove(KEY_ORCHARD_AGE);
        tag.remove(KEY_ORCHARD_RENDER_BLOCK);
        tag.remove(KEY_ORCHARD_AGE_PROPERTY);
        tag.remove(KEY_ORCHARD_RENDER_STYLE);
        tag.remove(KEY_ORCHARD_HARVEST_ITEM);
        tag.remove(KEY_ORCHARD_MATURE_AGE);
        tag.remove(KEY_ORCHARD_PENDING_HARVEST);
    }

    private static void stripMetadata(CompoundTag tag) {
        tag.remove("id");
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
    }
}
