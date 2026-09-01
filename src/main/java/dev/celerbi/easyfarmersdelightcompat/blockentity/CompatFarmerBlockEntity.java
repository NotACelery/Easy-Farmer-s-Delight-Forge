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
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
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

    private ItemStack normalCropHarvestTool() {
        return variant().isRich() && FarmerToolSupport.isHoe(harvestTool) ? harvestTool : ItemStack.EMPTY;
    }

    private ItemStack riceHarvestTool() {
        return variant().isRich() && FarmerToolSupport.isKnife(harvestTool) ? harvestTool : ItemStack.EMPTY;
    }

    private ItemStack stemHarvestTool() {
        return variant().isRich() && FarmerToolSupport.isAxe(harvestTool) ? harvestTool : ItemStack.EMPTY;
    }

    public boolean fruitReady() {
        return fruitReady;
    }

    public ToolRequirement currentToolRequirement() {
        if (!variant().isRich() || level == null) {
            return ToolRequirement.NONE;
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
        } else {
            ropeTwoProgress = 0;
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
        } else {
            ropeOneProgress = 0;
        }
        ropeCount--;
        setChanged();

        Item rope = BuiltInRegistries.ITEM.get(ROPE_ITEM_ID);
        return new ItemStack(rope);
    }

    public void selectRice(RegistryAccess registries) {
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
        fruitReady = false;
        Block buddingTomato = BuiltInRegistries.BLOCK.get(BUDDING_TOMATO_ID);
        easyVillagers.setCropState(withAge(buddingTomato.defaultBlockState(), 0), registries);
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
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

        easyVillagers.setCropState(withAge(colony.defaultBlockState(), 0), registries);
        fruitReady = false;
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
        ropeCount = 0;
        setChanged();
        return true;
    }

    public boolean selectStem(ItemStack seedStack, RegistryAccess registries) {
        if (!variant().isRich() || variant().isAquatic() || seedStack == null || seedStack.isEmpty())
            return false;
        Block stem;
        if (seedStack.is(Items.MELON_SEEDS)) {
            stem = Blocks.MELON_STEM;
        } else if (seedStack.is(Items.PUMPKIN_SEEDS)) {
            stem = Blocks.PUMPKIN_STEM;
        } else {
            return false;
        }
        easyVillagers.setCropState(withAge(stem.defaultBlockState(), 0), registries);
        fruitReady = false;
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
        ropeCount = 0;
        setChanged();
        return true;
    }

    public boolean canSelectRegrowingCrop(ItemStack stack) {
        if (!variant().isRich() || variant().isAquatic() || hasAttachedSetup()
                || stack == null || stack.isEmpty() || level == null) {
            return false;
        }
        if (easyVillagers.getCrop(level.registryAccess()) != null) {
            return false;
        }
        return RegrowingCropDefinitions.findPlanting(stack).isPresent();
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
        if (definition == null) {
            return false;
        }

        BlockState crop = definition.initialState();
        if (crop == null || crop.isAir()) {
            return false;
        }

        easyVillagers.setCropState(crop, registries);
        regrowingDefinitionId = definition.id();
        regrowingPlantingItemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        fruitReady = false;
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
        ropeCount = 0;
        setChanged();
        return true;
    }

    public void onNormalCropSelected() {
        regrowingDefinitionId = null;
        regrowingPlantingItemId = null;
        fruitReady = false;
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
        ropeCount = 0;
        setChanged();
    }

    public ItemStack removeSelectedCrop(RegistryAccess registries) {
        BlockState selected = easyVillagers.getCrop(registries);
        RegrowingCropDefinition regrowingDefinition = selected == null ? null : currentRegrowingDefinition(selected);
        ResourceLocation storedRegrowingPlantingItem = regrowingPlantingItemId;
        boolean rice = selected != null && RICE_CROP_ID.equals(BuiltInRegistries.BLOCK.getKey(selected.getBlock()));
        boolean tomato = isTomatoState(selected);
        Item stemSeedItem = seedItemForStem(selected);
        ResourceLocation mushroomItemId = mushroomItemForColony(selected);
        ItemStack removed = easyVillagers.removeCrop(registries);
        paddyGrowth = 0;
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
        fruitReady = false;
        regrowingDefinitionId = null;
        regrowingPlantingItemId = null;
        setChanged();

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
        if (mushroomItemId != null) {
            Item mushroom = BuiltInRegistries.ITEM.get(mushroomItemId);
            return new ItemStack(mushroom);
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

        ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(selected.getBlock());
        if (RICE_CROP_ID.equals(cropId)) {
            return new ItemStack(BuiltInRegistries.ITEM.get(RICE_ITEM_ID));
        }
        if (isTomatoState(selected)) {
            return new ItemStack(BuiltInRegistries.ITEM.get(TOMATO_SEEDS_ID));
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
                 || ropeOneProgress > 0 || ropeTwoProgress > 0 || fruitReady || hasAttachedSetup()) {
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
            RegrowingCropDefinition regrowingDefinition = farmer.currentRegrowingDefinition(crop);
            if (regrowingDefinition != null) {
                changed = farmer.ageRegrowingCrop(registries, regrowingDefinition, crop);
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
                    && farmer.ropeOneProgress < 3
                    && level.random.nextInt(farmSpeed) == 0) {
                farmer.ageTomatoRopeSection(level, registries, 1);
            }
            if (farmer.ropeCount >= 2
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
        if (hasMatureAttachedCrop()) {
            return true;
        }

        BlockState crop = easyVillagers.getCrop(registries);
        if (crop == null) {
            return false;
        }

        RegrowingCropDefinition regrowingDefinition = currentRegrowingDefinition(crop);
        if (regrowingDefinition != null
                && regrowingDefinition.age(crop) >= regrowingDefinition.harvestAge()) {
            return true;
        }
        if (isStemState(crop) && fruitReady) {
            return true;
        }

        ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(crop.getBlock());
        if (!BUDDING_TOMATO_ID.equals(cropId) && isMatureAgeState(crop)) {
            return true;
        }
        return TOMATO_CROP_ID.equals(cropId)
                && ((ropeCount >= 1 && ropeOneProgress >= 3)
                || (ropeCount >= 2 && ropeTwoProgress >= 3));
    }

    private boolean isBaseHarvestReady(BlockState crop) {
        if (crop == null) {
            return false;
        }
        RegrowingCropDefinition regrowingDefinition = currentRegrowingDefinition(crop);
        if (regrowingDefinition != null) {
            return regrowingDefinition.age(crop) >= regrowingDefinition.harvestAge();
        }
        if (isStemState(crop)) {
            return fruitReady;
        }
        if (BUDDING_TOMATO_ID.equals(BuiltInRegistries.BLOCK.getKey(crop.getBlock()))) {
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

            harvestMatureAttachedCrops(level, registries);

            BlockState crop = easyVillagers.getCrop(registries);
            if (crop == null) {
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

            if (isStemState(crop) && fruitReady) {
                if (harvestReadyStem(level, registries)) {
                    setChanged();
                }
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
                if (ropeCount >= 1 && ropeOneProgress >= 3) {
                    ageTomatoRopeSection(level, registries, 1);
                }
                if (ropeCount >= 2 && ropeTwoProgress >= 3) {
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

            if (ropeCount >= 1 && ropeOneProgress < 3) {
                ropeOneProgress = Math.min(3, ropeOneProgress + increment);
                setChanged();
                return true;
            }
            if (ropeCount >= 2 && ropeTwoProgress < 3) {
                ropeTwoProgress = Math.min(3, ropeTwoProgress + increment);
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

        int progress = ropeIndex == 1 ? ropeOneProgress : ropeTwoProgress;
        if (progress < 3) {
            if (ropeIndex == 1) {
                ropeOneProgress++;
            } else {
                ropeTwoProgress++;
            }
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

        Block fruit = fruitBlockForStem(stem);
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
        damageHarvestTool(level);
        level.playSound(null, worldPosition, fruitState.getSoundType().getBreakSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
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
        return state != null && (state.is(Blocks.MELON_STEM) || state.is(Blocks.PUMPKIN_STEM));
    }

    private static Item seedItemForStem(BlockState state) {
        if (state == null)
            return null;
        if (state.is(Blocks.MELON_STEM))
            return Items.MELON_SEEDS;
        if (state.is(Blocks.PUMPKIN_STEM))
            return Items.PUMPKIN_SEEDS;
        return null;
    }

    private static Block fruitBlockForStem(BlockState state) {
        if (state == null)
            return null;
        if (state.is(Blocks.MELON_STEM))
            return Blocks.MELON;
        if (state.is(Blocks.PUMPKIN_STEM))
            return Blocks.PUMPKIN;
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

        tag.putInt(KEY_SCHEMA, 8);
        tag.putInt(KEY_PADDY_GROWTH, paddyGrowth);
        tag.putInt(KEY_BASE_PROGRESS, baseProgress);
        tag.putInt(KEY_ROPE_ONE_PROGRESS, ropeOneProgress);
        tag.putInt(KEY_ROPE_TWO_PROGRESS, ropeTwoProgress);
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
    }

    private static void stripMetadata(CompoundTag tag) {
        tag.remove("id");
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
    }
}
