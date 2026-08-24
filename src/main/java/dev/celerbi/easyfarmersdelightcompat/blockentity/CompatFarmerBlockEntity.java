package dev.celerbi.easyfarmersdelightcompat.blockentity;

import dev.celerbi.easyfarmersdelightcompat.block.CompatFarmerBlock;
import dev.celerbi.easyfarmersdelightcompat.block.FarmerVariant;
import dev.celerbi.easyfarmersdelightcompat.integration.EasyVillagersFarmerAdapter;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmersDelightAdapter;
import dev.celerbi.easyfarmersdelightcompat.integration.FarmerToolSupport;
import dev.celerbi.easyfarmersdelightcompat.integration.ToolRequirement;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlockEntities;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.core.Direction;
import javax.annotation.Nullable;

/** Farmer state plus the opaque Easy Villagers payload we need to preserve. */
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

    private static final ResourceLocation RICE_ITEM_ID = new ResourceLocation("farmersdelight", "rice");
    private static final ResourceLocation RICE_CROP_ID = new ResourceLocation("farmersdelight", "rice");
    private static final ResourceLocation RICE_PANICLES_ID = new ResourceLocation("farmersdelight", "rice_panicles");
    private static final ResourceLocation TOMATO_SEEDS_ID = new ResourceLocation("farmersdelight", "tomato_seeds");
    private static final ResourceLocation BUDDING_TOMATO_ID = new ResourceLocation("farmersdelight", "budding_tomatoes");
    private static final ResourceLocation TOMATO_CROP_ID = new ResourceLocation("farmersdelight", "tomatoes");
    private static final ResourceLocation TOMATO_ON_ROPE_ID = new ResourceLocation("farmersdelight", "tomatoes_on_rope");
    private static final ResourceLocation ROPE_ITEM_ID = new ResourceLocation("farmersdelight", "rope");
    private static final ResourceLocation RED_MUSHROOM_ITEM_ID = new ResourceLocation("red_mushroom");
    private static final ResourceLocation BROWN_MUSHROOM_ITEM_ID = new ResourceLocation("brown_mushroom");
    private static final ResourceLocation RED_MUSHROOM_COLONY_ID = new ResourceLocation("farmersdelight", "red_mushroom_colony");
    private static final ResourceLocation BROWN_MUSHROOM_COLONY_ID = new ResourceLocation("farmersdelight", "brown_mushroom_colony");
    private static final TagKey<Block> UNAFFECTED_BY_RICH_SOIL = TagKey.create(
            Registries.BLOCK,
            new ResourceLocation("farmersdelight", "unaffected_by_rich_soil")
    );

    /** Rice stages: 0..3 lower crop, 4..7 panicles; harvest returns to stage 3. */
    private static final int MAX_PADDY_GROWTH = 7;
    private static final int MAX_SUGAR_CANE_AGE = 15;

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
    private boolean itemPreview;
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

    /** Transient client flag used only by the inventory renderer. */
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
        if (!ItemStack.isSameItemSameTags(harvestTool, normalized) || harvestTool.getCount() != normalized.getCount()) {
            harvestTool = normalized;
            setChanged();
        }
    }

    /** Normal crops only receive a Hoe in their loot context so Fortune on an Axe/Knife cannot leak into them. */
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

    /** Reports only tools that are hard blockers for the current operation. */
    public ToolRequirement currentToolRequirement() {
        if (!variant().isRich() || level == null) return ToolRequirement.NONE;

        BlockState crop = easyVillagers.getCrop(level.registryAccess());
        if (crop == null) return ToolRequirement.NONE;

        if (isMushroomColonyState(crop) && getAge(crop) >= maxAge(crop)) {
            return ToolRequirement.KNIFE;
        }
        if (isStemState(crop) && fruitReady) {
            return ToolRequirement.AXE;
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
        if (!variant().isAquatic() || paddySand) return false;
        if (level != null && easyVillagers.getCrop(level.registryAccess()) != null) return false;
        paddySand = true;
        sugarCaneHeight = 0;
        sugarCaneAge = 0;
        paddyGrowth = 0;
        setChanged();
        return true;
    }

    public boolean plantSugarCane() {
        if (!variant().isAquatic() || !paddySand || sugarCaneHeight != 0) return false;
        sugarCaneHeight = 1;
        sugarCaneAge = 0;
        setChanged();
        return true;
    }

    /** Returns the Sand and persistent base cane that must be returned to the player. */
    public List<ItemStack> dismantleSugarCaneMode() {
        if (!variant().isAquatic() || !paddySand) return List.of();
        List<ItemStack> returned = new ArrayList<>(2);
        returned.add(new ItemStack(Items.SAND));
        if (sugarCaneHeight > 0) returned.add(new ItemStack(Items.SUGAR_CANE, sugarCaneHeight));
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

    /** Selects vanilla Melon/Pumpkin stems explicitly; Easy Villagers does not tag their seeds as villager-plantable. */
    public boolean selectStem(ItemStack seedStack, RegistryAccess registries) {
        if (!variant().isRich() || variant().isAquatic() || seedStack == null || seedStack.isEmpty()) return false;
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

    /** Clears transient state after a normal Easy Villagers seed has been selected. */
    public void onNormalCropSelected() {
        fruitReady = false;
        baseProgress = 0;
        ropeOneProgress = 0;
        ropeTwoProgress = 0;
        ropeCount = 0;
        setChanged();
    }

    public ItemStack removeSelectedCrop(RegistryAccess registries) {
        BlockState selected = easyVillagers.getCrop(registries);
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
        setChanged();

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

    /** Empty Farmers stay stackable; any real machine state must be preserved. */
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
                || ropeOneProgress > 0 || ropeTwoProgress > 0 || fruitReady) {
            return true;
        }

        // Unknown payload still counts as machine state after known empty keys are stripped.
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

        // Keep Easy Villagers' stored-villager aging behaviour for every variant.
        if (farmer.easyVillagers.hasVillager(registries)) {
            farmer.easyVillagers.advanceVillagerAge(registries);
            farmer.setChanged();
        }

        // Stem Rich Soil uses Minecraft's random-tick cadence, not Easy Villagers' farmSpeed.
        if (farmer.variant().isRich() && !farmer.variant().isAquatic()) {
            farmer.tryVirtualStemRichSoilRandomTick(level, registries);
        }

        // Ordinary work runs once per second; farmSpeed still gates crop growth.
        if (level.getGameTime() % 20L != 0L) {
            return;
        }

        int farmSpeed = farmer.easyVillagers.farmSpeed();

        if (farmer.variant().isAquatic() && farmer.paddySand) {
            if (farmer.sugarCaneHeight <= 0) return;

            Villager villager = farmer.easyVillagers.getVillagerEntity(registries);
            boolean canHarvest = villager != null
                    && !villager.isBaby()
                    && villager.getVillagerData().getProfession() == VillagerProfession.FARMER;

            if (farmer.sugarCaneHeight >= 3 && canHarvest) {
                if (farmer.harvestMatureSugarCane(registries)) {
                    farmer.sugarCaneHeight = 1;
                    farmer.sugarCaneAge = 0;
                    farmer.setChanged();
                    level.playSound(null, pos, SoundEvents.VILLAGER_WORK_FARMER, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return;
            }

            // Sugar Cane advances its vanilla 0..15 age before adding another section.
            if (farmer.sugarCaneHeight < 3 && level.random.nextInt(farmSpeed) == 0) {
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
            if (!farmer.easyVillagers.hasRiceCrop(registries)) {
                return;
            }

            Villager villager = farmer.easyVillagers.getVillagerEntity(registries);
            boolean canHarvest = villager != null
                    && !villager.isBaby()
                    && villager.getVillagerData().getProfession() == VillagerProfession.FARMER;

            // Mature panicles harvest on the next work tick; growth alone uses farmSpeed RNG.
            if (farmer.paddyGrowth >= MAX_PADDY_GROWTH && canHarvest) {
                if (farmer.harvestMatureRice(level, registries)) {
                    // Harvest keeps the mature submerged rice; only panicles regrow.
                    farmer.paddyGrowth = 3;
                    farmer.syncRiceCropState(registries);
                    farmer.setChanged();
                    level.playSound(null, pos, SoundEvents.VILLAGER_WORK_FARMER, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                // Do not reset mature rice until its whole harvest fits.
                return;
            }

            // Normal Paddy growth roll.
            if (farmer.paddyGrowth < MAX_PADDY_GROWTH
                    && level.random.nextInt(farmSpeed) == 0) {
                farmer.paddyGrowth++;
                farmer.syncRiceCropState(registries);
                farmer.setChanged();
            }

            // Rich Paddy gets a separate virtual Rich Soil opportunity.
            if (farmer.variant().isRich()
                    && level.random.nextInt(farmSpeed) == 0) {
                farmer.tryRichPaddyBoost(level, registries);
            }
            return;
        }

        BlockState crop = farmer.easyVillagers.getCrop(registries);
        if (crop == null) {
            return;
        }

        // Ready stem fruit retries harvest each work tick while waiting on Axe/output/adult state.
        if (isStemState(crop) && farmer.fruitReady) {
            if (farmer.harvestReadyStem(level, registries)) {
                farmer.setChanged();
            }
            return;
        }

        // Growth uses farmSpeed RNG; once mature, harvest is retried every work tick until it succeeds.
        boolean baseHandledThisCadence = false;
        if (!isStemState(crop) && isMatureAgeState(crop)) {
            ResourceLocation cropId = BuiltInRegistries.BLOCK.getKey(crop.getBlock());

            // Budding tomatoes age 3 still needs one normal growth step into the persistent vine.
            boolean finalTomatoStage = TOMATO_CROP_ID.equals(cropId);
            if (!isTomatoState(crop) || finalTomatoStage) {
                boolean changed;
                if (isMushroomColonyState(crop)) {
                    changed = farmer.ageMushroomColony(level, registries);
                } else if (finalTomatoStage) {
                    changed = farmer.ageTomato(level, registries);
                } else {
                    changed = farmer.ageNormalCropSafely(level, registries);
                }

                baseHandledThisCadence = true;
                if (changed) {
                    farmer.setChanged();
                }

                // Non-Tomato crops have no rope work left this tick.
                if (!finalTomatoStage) {
                    return;
                }
            }
        }

        // Only non-mature base crops receive a growth roll.
        if (!baseHandledThisCadence && level.random.nextInt(farmSpeed) == 0) {
            boolean changed;
            if (isTomatoState(crop)) {
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

        // Tomato rope sections grow independently; mature sections harvest without another RNG gate.
        BlockState afterBase = farmer.easyVillagers.getCrop(registries);
        if (afterBase != null && TOMATO_CROP_ID.equals(BuiltInRegistries.BLOCK.getKey(afterBase.getBlock()))) {
            boolean ropeOneHandled = farmer.ropeCount >= 1 && farmer.ropeOneProgress >= 3;
            boolean ropeTwoHandled = farmer.ropeCount >= 2 && farmer.ropeTwoProgress >= 3;

            if (ropeOneHandled) {
                farmer.ageTomatoRopeSection(level, registries, 1);
            }
            if (ropeTwoHandled) {
                farmer.ageTomatoRopeSection(level, registries, 2);
            }

            if (farmer.ropeCount >= 1 && !ropeOneHandled && level.random.nextInt(farmSpeed) == 0) {
                farmer.ageTomatoRopeSection(level, registries, 1);
            }
            if (farmer.ropeCount >= 2 && !ropeTwoHandled && level.random.nextInt(farmSpeed) == 0) {
                farmer.ageTomatoRopeSection(level, registries, 2);
            }
        }

        // Non-stem Rich Farmer crops keep the existing boost; stems use the random-tick path above.
        BlockState richAfterBase = farmer.easyVillagers.getCrop(registries);
        if (farmer.variant().isRich()
                && !isStemState(richAfterBase)
                && level.random.nextInt(farmSpeed) == 0) {
            farmer.tryRichSoilBoost(level, registries);
        }
    }

    /** Simulates selection of one virtual Rich Soil block by Minecraft's random-tick clock. */
    private void tryVirtualStemRichSoilRandomTick(ServerLevel level, RegistryAccess registries) {
        if (fruitReady) return;

        BlockState crop = easyVillagers.getCrop(registries);
        if (!isStemState(crop)) return;

        int age = getAge(crop);
        int maxAge = maxAge(crop);
        if (age >= maxAge) return;

        int randomTickSpeed = Math.max(0, level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING));
        if (randomTickSpeed <= 0) return;

        double selectedChance = 1.0D - Math.pow(4095.0D / 4096.0D, randomTickSpeed);
        if (level.random.nextDouble() >= selectedChance) return;

        double boostChance = farmersDelight.richSoilBoostChance();
        if (boostChance <= 0.0D || level.random.nextDouble() >= boostChance) return;

        int increment = 2 + level.random.nextInt(4); // vanilla StemBlock bonemeal: +2..+5
        easyVillagers.setCropState(withAge(crop, Math.min(maxAge, age + increment)), registries);
        fruitReady = false;
        setChanged();
    }

    /** Applies the same random-tick and Rich Soil boost rolls to the stored crop state. */
    private void tryRichSoilBoost(ServerLevel level, RegistryAccess registries) {
        BlockState crop = easyVillagers.getCrop(registries);
        if (crop == null || crop.is(UNAFFECTED_BY_RICH_SOIL)) {
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
            if (currentAge >= maxAge) return false;
            // Rich Soil accelerates stem age only; fruit generation stays on the normal work roll.
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

        easyVillagers.setCropState(crop.setValue(integerProperty, Math.min(maxAge, currentAge + increment)), registries);
        setChanged();
        return true;
    }

    /** Uses each crop implementation's own protected bone-meal increment when available. */
    private static int getBoneMealAgeIncrease(Block block, Level level) {
        Class<?> type = block.getClass();
        while (type != null) {
            try {
                Method method = type.getDeclaredMethod("getBonemealAgeIncrease", Level.class);
                method.setAccessible(true);
                Object result = method.invoke(block, level);
                return result instanceof Number number ? Math.max(0, number.intValue()) : 0;
            } catch (NoSuchMethodException ignored) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException | RuntimeException e) {
                return 0;
            }
        }
        return 0;
    }

    /** Generic crop lifecycle; mature crops reset only after all generated loot fits. */
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

        Villager villager = easyVillagers.getVillagerEntity(registries);
        if (villager == null || villager.isBaby() || villager.getVillagerData().getProfession() != VillagerProfession.FARMER) {
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

    /** Persistent Mushroom Colony lifecycle for Rich Farmers. */
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

        Villager villager = easyVillagers.getVillagerEntity(registries);
        if (villager == null || villager.isBaby() || villager.getVillagerData().getProfession() != VillagerProfession.FARMER) {
            return false;
        }

        // Mature colonies wait for a Knife; growth itself is never blocked.
        if (variant().isRich() && !FarmerToolSupport.isKnife(harvestTool)) {
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

        // Full knife harvest returns colonyAge mushrooms and resets the persistent colony.
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

        // Farmer's Delight starts tomatoes as budding_tomatoes (age 0..3), then
        // transitions into the persistent tomatoes vine at age 0.
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

        Villager villager = easyVillagers.getVillagerEntity(registries);
        if (villager == null || villager.isBaby() || villager.getVillagerData().getProfession() != VillagerProfession.FARMER) {
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

        Villager villager = easyVillagers.getVillagerEntity(registries);
        boolean canHarvest = villager != null
                && !villager.isBaby()
                && villager.getVillagerData().getProfession() == VillagerProfession.FARMER;

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

        if (!canHarvest) {
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

        // Use Farmer's Delight's real mature Tomato loot table so a Hoe with
        // Fortune affects Tomato yield exactly as the mod defines it. The base
        // persistent vine is evaluated as rope-logged to suppress the seed drop
        // that belongs to breaking the whole plant; our Farmer only harvests fruit.
        // Keep this compatible with the declared Farmer's Delight 1.2.9 minimum.
        // In 1.2.x, rope-grown tomatoes are represented by the normal `tomatoes`
        // block with `ropelogged=true`; the separate `tomatoes_on_rope` block was
        // only introduced later. The legacy property is still retained by newer FD
        // builds specifically for migration, so this state works across both lines.
        Block tomato = BuiltInRegistries.BLOCK.get(ropeSection ? TOMATO_ON_ROPE_ID : TOMATO_CROP_ID);
        if (tomato == Blocks.AIR) return false;
        BlockState harvestState = withAge(tomato.defaultBlockState(), 3);
        if (!ropeSection) harvestState = withBooleanProperty(harvestState, "ropelogged", true);

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
        if (!isStemState(stem)) return false;

        int age = getAge(stem);
        int maxAge = maxAge(stem);
        if (age < maxAge) {
            easyVillagers.setCropState(withAge(stem, age + 1), registries);
            fruitReady = false;
            return true;
        }

        // Fruit generation is a separate normal-speed phase. Rich Soil only boosts
        // the stem itself and never accelerates this transition. Harvesting is
        // handled separately once fruitReady is true, so a missing Axe/output room
        // blocks on the real requirement instead of demanding another RNG success.
        if (!fruitReady) {
            fruitReady = true;
            setChanged();
            return true;
        }
        return false;
    }

    private boolean harvestReadyStem(ServerLevel level, RegistryAccess registries) {
        BlockState stem = easyVillagers.getCrop(registries);
        if (!fruitReady || !isStemState(stem)) return false;

        Villager villager = easyVillagers.getVillagerEntity(registries);
        if (villager == null || villager.isBaby() || villager.getVillagerData().getProfession() != VillagerProfession.FARMER) {
            return false;
        }
        if (!FarmerToolSupport.isAxe(harvestTool)) {
            return false;
        }

        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) return false;

        Block fruit = fruitBlockForStem(stem);
        if (fruit == null || fruit == Blocks.AIR) return false;
        BlockState fruitState = fruit.defaultBlockState();
        LootParams.Builder context = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition))
                .withParameter(LootContextParams.BLOCK_STATE, fruitState)
                .withParameter(LootContextParams.TOOL, stemHarvestTool());
        List<ItemStack> drops = fruitState.getDrops(context);
        if (!canFitAll(output, drops)) return false;

        for (ItemStack drop : drops) insertIntoOutput(output, drop.copy());
        output.setChanged();
        fruitReady = false;
        damageHarvestTool(level);
        level.playSound(null, worldPosition, fruitState.getSoundType().getBreakSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
        return true;
    }

    private boolean harvestMatureSugarCane(RegistryAccess registries) {
        Container output = easyVillagers.getOutputInventory(registries);
        if (output == null) return false;
        ItemStack harvest = new ItemStack(Items.SUGAR_CANE, 2);
        if (!canFitAll(output, List.of(harvest))) return false;
        insertIntoOutput(output, harvest);
        output.setChanged();
        return true;
    }

    private void damageHarvestTool(ServerLevel level) {
        if (harvestTool.isEmpty() || !harvestTool.isDamageableItem()) return;
        if (harvestTool.hurt(1, level.random, null)) {
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
        if (state == null) return null;
        if (state.is(Blocks.MELON_STEM)) return Items.MELON_SEEDS;
        if (state.is(Blocks.PUMPKIN_STEM)) return Items.PUMPKIN_SEEDS;
        return null;
    }

    private static Block fruitBlockForStem(BlockState state) {
        if (state == null) return null;
        if (state.is(Blocks.MELON_STEM)) return Blocks.MELON;
        if (state.is(Blocks.PUMPKIN_STEM)) return Blocks.PUMPKIN;
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

    /** Checks the full harvest against output stacking rules without mutating inventory. */
    private static boolean canFitAll(Container output, List<ItemStack> stacks) {
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
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        passthroughData=tag.copy();stripMetadata(passthroughData);easyVillagers.reset();itemCapability.invalidate();itemCapability=LazyOptional.empty();
        if(tag.contains(KEY_PADDY_GROWTH))paddyGrowth=Math.max(0,Math.min(MAX_PADDY_GROWTH,tag.getInt(KEY_PADDY_GROWTH)));else paddyGrowth=inferLegacyRiceGrowth(tag);
        baseProgress=Math.max(0,tag.getInt(KEY_BASE_PROGRESS));ropeOneProgress=Math.max(0,tag.getInt(KEY_ROPE_ONE_PROGRESS));ropeTwoProgress=Math.max(0,tag.getInt(KEY_ROPE_TWO_PROGRESS));ropeCount=Math.max(0,Math.min(2,tag.getInt(KEY_ROPE_COUNT)));
        fruitReady=tag.getBoolean(KEY_FRUIT_READY);paddySand=variant().isAquatic()&&tag.getBoolean(KEY_PADDY_SAND);sugarCaneHeight=paddySand?Math.max(0,Math.min(3,tag.getInt(KEY_SUGAR_CANE_HEIGHT))):0;sugarCaneAge=paddySand&&sugarCaneHeight>0&&sugarCaneHeight<3?Math.max(0,Math.min(MAX_SUGAR_CANE_AGE,tag.getInt(KEY_SUGAR_CANE_AGE))):0;
        CompoundTag toolTag=null;if(tag.contains(KEY_HARVEST_TOOL,net.minecraft.nbt.Tag.TAG_COMPOUND))toolTag=tag.getCompound(KEY_HARVEST_TOOL);else if(tag.contains(LEGACY_EFDC_KNIFE,net.minecraft.nbt.Tag.TAG_COMPOUND))toolTag=tag.getCompound(LEGACY_EFDC_KNIFE);
        harvestTool=variant().isRich()&&toolTag!=null?FarmerToolSupport.normalizeHarvestTool(ItemStack.of(toolTag)):ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);RegistryAccess registries=level!=null?level.registryAccess():null;passthroughData=easyVillagers.snapshot(passthroughData,registries);CompoundTag preserved=passthroughData.copy();stripMetadata(preserved);stripAddonKeys(preserved);tag.merge(preserved);
        tag.putInt(KEY_SCHEMA,5);tag.putInt(KEY_PADDY_GROWTH,paddyGrowth);tag.putInt(KEY_BASE_PROGRESS,baseProgress);tag.putInt(KEY_ROPE_ONE_PROGRESS,ropeOneProgress);tag.putInt(KEY_ROPE_TWO_PROGRESS,ropeTwoProgress);tag.putInt(KEY_ROPE_COUNT,ropeCount);tag.putBoolean(KEY_FRUIT_READY,fruitReady);tag.putBoolean(KEY_PADDY_SAND,variant().isAquatic()&&paddySand);tag.putInt(KEY_SUGAR_CANE_HEIGHT,variant().isAquatic()&&paddySand?sugarCaneHeight:0);tag.putInt(KEY_SUGAR_CANE_AGE,variant().isAquatic()&&paddySand?sugarCaneAge:0);tag.remove(LEGACY_EFDC_KNIFE);
        if(variant().isRich()&&FarmerToolSupport.isHarvestTool(harvestTool))tag.put(KEY_HARVEST_TOOL,harvestTool.save(new CompoundTag()));else tag.remove(KEY_HARVEST_TOOL);
    }

    @Override public <T> LazyOptional<T> getCapability(Capability<T> capability,@Nullable Direction side){
        if(!remove&&capability==ForgeCapabilities.ITEM_HANDLER){IItemHandler handler=getItemHandler();if(handler!=null){if(!itemCapability.isPresent())itemCapability=LazyOptional.of(()->handler);return itemCapability.cast();}}return super.getCapability(capability,side);
    }
    @Override public void invalidateCaps(){super.invalidateCaps();itemCapability.invalidate();itemCapability=LazyOptional.empty();}
    @Override public void reviveCaps(){super.reviveCaps();itemCapability=LazyOptional.empty();}

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
    }

    private static void stripMetadata(CompoundTag tag) {
        tag.remove("id");
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
    }
}
