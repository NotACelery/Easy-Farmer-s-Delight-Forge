package dev.celerbi.easyfarmersdelightcompat.integration;

import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

public final class EasyVillagersFarmerAdapter {
    private static final ResourceLocation EASY_FARMER_ID = new ResourceLocation("easy_villagers",
            "farmer");
    private static final ResourceLocation RICE_CROP_ID = new ResourceLocation("farmersdelight",
            "rice");

    private static final String FARMER_TILEENTITY = "de.maxhenkel.easyvillagers.blocks.tileentity.FarmerTileentity";
    private static final String VILLAGER_ITEM = "de.maxhenkel.easyvillagers.items.VillagerItem";
    private static final String OUTPUT_CONTAINER = "de.maxhenkel.easyvillagers.gui.OutputContainer";
    private static final String MAIN_CLASS = "de.maxhenkel.easyvillagers.Main";
    private static final int DEFAULT_FARM_SPEED = 10;
    private static boolean farmSpeedFallbackWarned;
    private static boolean farmSpeedResolved;
    private static boolean farmSpeedResolutionFailed;
    private static Object farmSpeedValue;
    private static Method farmSpeedGetMethod;

    private final CompatFarmerBlockEntity owner;
    private BlockEntity delegate;
    private Container trackedOutputInventory;
    private IItemHandler trackedItemHandler;
    private Boolean serverHasVillagerCache;
    private Villager serverVillagerEntity;
    private boolean serverCropResolved;
    private BlockState serverCrop;
    private Villager clientVisualVillager;
    private BlockState clientVisualCrop;
    private boolean clientVisualVillagerResolved;
    private boolean clientVisualCropResolved;
    private boolean failed;

    public EasyVillagersFarmerAdapter(CompatFarmerBlockEntity owner) {
        this.owner = owner;
    }

    public void reset() {
        if (clientVisualVillager != null) {
            clientVisualVillager.setTradingPlayer(null);
        }
        delegate = null;
        trackedOutputInventory = null;
        trackedItemHandler = null;
        serverHasVillagerCache = null;
        serverVillagerEntity = null;
        serverCropResolved = false;
        serverCrop = null;
        clientVisualVillager = null;
        clientVisualCrop = null;
        clientVisualVillagerResolved = false;
        clientVisualCropResolved = false;
        failed = false;
    }

    public boolean isVillagerItem(ItemStack stack) {
        try {
            return ReflectionCache.type(VILLAGER_ITEM).isInstance(stack.getItem());
        } catch (ClassNotFoundException | LinkageError e) {
            fail(e);
            return false;
        }
    }

    public boolean hasVillager(RegistryAccess registries) {
        Level ownerLevel = owner.getLevel();
        boolean server = ownerLevel != null && !ownerLevel.isClientSide;
        if (server && serverHasVillagerCache != null) {
            return serverHasVillagerCache;
        }

        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            if (server) {
                serverHasVillagerCache = false;
            }
            return false;
        }
        try {
            boolean hasVillager = (boolean) ReflectionCache.publicMethod(farmer.getClass(), "hasVillager")
                    .invoke(farmer);
            if (server) {
                serverHasVillagerCache = hasVillager;
            }
            return hasVillager;
        } catch (ReflectiveOperationException e) {
            fail(e);
            if (server) {
                serverHasVillagerCache = false;
            }
            return false;
        }
    }

    public void insertVillager(ItemStack stack, RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return;
        }
        try {
            Field villagerField = ReflectionCache.field(farmer.getClass(), "villager");
            Field villagerEntityField = ReflectionCache.field(farmer.getClass(), "villagerEntity");
            villagerField.set(farmer, stack.copyWithCount(1));
            villagerEntityField.set(farmer, null);
            serverHasVillagerCache = true;
            serverVillagerEntity = null;

            Villager villager = getVillagerEntity(registries);
            if (villager != null && villager.getVillagerXp() <= 0 && villager.getVillagerData()
                    .getProfession() != VillagerProfession.NITWIT) {
                villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.FARMER));
            }
        } catch (ReflectiveOperationException e) {
            fail(e);
        }
    }

    public ItemStack removeVillager(RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return ItemStack.EMPTY;
        }
        try {
            Method getVillager = ReflectionCache.publicMethod(farmer.getClass(), "getVillager");
            ItemStack result = ((ItemStack) getVillager.invoke(farmer)).copy();

            Field villagerEntityField = ReflectionCache.field(farmer.getClass(), "villagerEntity");
            Object entity = villagerEntityField.get(farmer);
            if (entity instanceof Villager villager) {
                villager.setTradingPlayer(null);
            }

            ReflectionCache.field(farmer.getClass(), "villager").set(farmer, ItemStack.EMPTY);
            villagerEntityField.set(farmer, null);
            serverHasVillagerCache = false;
            serverVillagerEntity = null;
            return result;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return ItemStack.EMPTY;
        }
    }

    public Villager getVillagerEntity(RegistryAccess registries) {
        Level ownerLevel = owner.getLevel();
        boolean clientVisual = ownerLevel != null && ownerLevel.isClientSide;
        if (clientVisual && clientVisualVillagerResolved) {
            return clientVisualVillager;
        }
        if (!clientVisual && serverVillagerEntity != null) {
            return serverVillagerEntity;
        }
        if (!clientVisual && Boolean.FALSE.equals(serverHasVillagerCache)) {
            return null;
        }

        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            if (clientVisual) {
                clientVisualVillagerResolved = true;
                clientVisualVillager = null;
            } else {
                serverHasVillagerCache = false;
            }
            return null;
        }
        try {
            Object result = ReflectionCache.publicMethod(farmer.getClass(), "getVillagerEntity").invoke(farmer);
            Villager villager = result instanceof Villager value ? value : null;
            if (clientVisual) {
                clientVisualVillager = villager;
                clientVisualVillagerResolved = true;
            } else {
                serverVillagerEntity = villager;
                if (villager != null) {
                    serverHasVillagerCache = true;
                }
            }
            return villager;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return null;
        }
    }

    public boolean advanceVillagerAge(RegistryAccess registries) {
        Villager villager = getVillagerEntity(registries);
        if (villager == null) {
            return false;
        }
        int previousAge = villager.getAge();
        int age = previousAge + 1;
        villager.setAge(age);
        return previousAge < 0 && age >= 0;
    }

    public BlockState getCrop(RegistryAccess registries) {
        Level ownerLevel = owner.getLevel();
        boolean clientVisual = ownerLevel != null && ownerLevel.isClientSide;
        if (clientVisual && clientVisualCropResolved) {
            return clientVisualCrop;
        }
        if (!clientVisual && serverCropResolved) {
            return serverCrop;
        }

        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            if (clientVisual) {
                clientVisualCropResolved = true;
                clientVisualCrop = null;
            } else {
                serverCropResolved = true;
                serverCrop = null;
            }
            return null;
        }
        try {
            Object result = ReflectionCache.publicMethod(farmer.getClass(), "getCrop").invoke(farmer);
            BlockState crop = result instanceof BlockState state ? state : null;
            if (clientVisual) {
                clientVisualCrop = crop;
                clientVisualCropResolved = true;
            } else {
                serverCrop = crop;
                serverCropResolved = true;
            }
            return crop;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return null;
        }
    }

    public boolean hasRiceCrop(RegistryAccess registries) {
        BlockState crop = getCrop(registries);
        return crop != null && RICE_CROP_ID.equals(BuiltInRegistries.BLOCK.getKey(crop.getBlock()));
    }

    public boolean isValidSeed(ItemStack stack, RegistryAccess registries) {
        if (stack.isEmpty()) {
            return false;
        }
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return false;
        }
        try {
            Object result = ReflectionCache.publicMethod(farmer.getClass(), "isValidSeed", Item.class).invoke(farmer, stack.getItem());
            return result instanceof Boolean valid && valid;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return false;
        }
    }

    public boolean setCropFromSeed(ItemStack stack, RegistryAccess registries) {
        if (stack.isEmpty()) {
            return false;
        }
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return false;
        }
        try {
            Object result = ReflectionCache.publicMethod(farmer.getClass(), "getSeedCrop", Item.class).invoke(farmer, stack.getItem());
            if (!(result instanceof BlockState crop)) {
                return false;
            }
            setCropState(farmer, crop);
            return true;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return false;
        }
    }

    public void setRiceCrop(RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return;
        }
        Block riceCrop = BuiltInRegistries.BLOCK.get(RICE_CROP_ID);
        setCropState(farmer, riceCrop.defaultBlockState());
    }

    public void setRiceCropState(BlockState state, RegistryAccess registries) {
        setCropState(state, registries);
    }

    public void setCropState(BlockState state, RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer != null) {
            setCropState(farmer, state);
        }
    }

    public ItemStack removeCrop(RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return ItemStack.EMPTY;
        }
        try {
            Field cropField = ReflectionCache.field(farmer.getClass(), "crop");
            Object value = cropField.get(farmer);
            cropField.set(farmer, null);
            Level ownerLevel = owner.getLevel();
            if (ownerLevel != null && ownerLevel.isClientSide) {
                clientVisualCrop = null;
                clientVisualCropResolved = true;
            } else {
                serverCrop = null;
                serverCropResolved = true;
            }
            if (value instanceof BlockState crop) {
                return new ItemStack(crop.getBlock());
            }
        } catch (ReflectiveOperationException e) {
            fail(e);
        }
        return ItemStack.EMPTY;
    }

    public IItemHandler getItemHandler(RegistryAccess registries) {
        if (trackedItemHandler != null) {
            return trackedItemHandler;
        }

        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return null;
        }
        try {
            Object result = ReflectionCache.field(farmer.getClass(), "itemHandler").get(farmer);
            if (!(result instanceof IItemHandler handler)) {
                return null;
            }
            trackedItemHandler = new DirtyTrackingItemHandler(handler);
            return trackedItemHandler;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return null;
        }
    }

    public Container getOutputInventory(RegistryAccess registries) {
        if (trackedOutputInventory != null) {
            return trackedOutputInventory;
        }

        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return null;
        }
        try {
            Object result = ReflectionCache.publicMethod(farmer.getClass(), "getOutputInventory").invoke(farmer);
            if (!(result instanceof Container container)) {
                return null;
            }
            trackedOutputInventory = new DirtyTrackingContainer(container);
            return trackedOutputInventory;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return null;
        }
    }

    public AbstractContainerMenu createOutputMenu(
            int id,
            Inventory inventory,
            Level level,
            Block block,
            RegistryAccess registries
    ) {
        Container output = getOutputInventory(registries);
        if (output == null) {
            return null;
        }
        try {
            Class<?> clazz = ReflectionCache.type(OUTPUT_CONTAINER);
            Constructor<?> constructor = ReflectionCache.constructor(clazz,
                    int.class,
                    Inventory.class,
                    Container.class,
                    ContainerLevelAccess.class,
                    Supplier.class
            );
            Supplier<Block> blockSupplier = () -> block;
            return (AbstractContainerMenu) constructor.newInstance(
                    id,
                    inventory,
                    output,
                    ContainerLevelAccess.create(level, owner.getBlockPos()),
                    blockSupplier
            );
        } catch (ReflectiveOperationException e) {
            fail(e);
            return null;
        }
    }

    public int farmSpeed() {
        resolveFarmSpeed();
        if (farmSpeedResolutionFailed || farmSpeedValue == null) {
            return DEFAULT_FARM_SPEED;
        }

        try {
            if (farmSpeedValue instanceof IntSupplier intSupplier) {
                return Math.max(1, intSupplier.getAsInt());
            }
            if (farmSpeedValue instanceof Supplier<?> supplier) {
                Object value = supplier.get();
                return value instanceof Number number ? Math.max(1, number.intValue()) : DEFAULT_FARM_SPEED;
            }
            Object value = farmSpeedGetMethod.invoke(farmSpeedValue);
            return value instanceof Number number ? Math.max(1, number.intValue()) : DEFAULT_FARM_SPEED;
        } catch (ReflectiveOperationException | RuntimeException e) {
            farmSpeedResolutionFailed = true;
            warnFarmSpeedFallback(e);
            return DEFAULT_FARM_SPEED;
        }
    }

    private static synchronized void resolveFarmSpeed() {
        if (farmSpeedResolved) {
            return;
        }
        farmSpeedResolved = true;
        try {
            Class<?> main = ReflectionCache.type(MAIN_CLASS);
            Object serverConfig = ReflectionCache.field(main, "SERVER_CONFIG").get(null);
            if (serverConfig == null) {
                farmSpeedResolutionFailed = true;
                warnFarmSpeedFallback(null);
                return;
            }
            farmSpeedValue = ReflectionCache.field(serverConfig.getClass(), "farmSpeed").get(serverConfig);
            if (farmSpeedValue == null) {
                farmSpeedResolutionFailed = true;
                warnFarmSpeedFallback(null);
                return;
            }
            if (!(farmSpeedValue instanceof IntSupplier) && !(farmSpeedValue instanceof Supplier<?>)) {
                farmSpeedGetMethod = ReflectionCache.publicMethod(farmSpeedValue.getClass(), "get");
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            farmSpeedResolutionFailed = true;
            warnFarmSpeedFallback(e);
        }
    }

    private static void warnFarmSpeedFallback(Throwable e) {
        if (farmSpeedFallbackWarned)
            return;
        farmSpeedFallbackWarned = true;
        System.err.println(
                "[Easy Farmer's Delight Compat] Could not read Easy Villagers farmer.farm_speed; "
                        + "using default 10 without disabling the Farmer adapter."
        );
        if (e != null)
            e.printStackTrace();
    }

    public CompoundTag snapshot(CompoundTag fallback, RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return fallback.copy();
        }

        CompoundTag merged = fallback.copy();

        merged.remove("Villager");
        merged.remove("Crop");
        merged.remove("Items");
        merged.merge(farmer.saveWithoutMetadata());
        return merged;
    }

    private BlockEntity getDelegate(RegistryAccess registries) {
        if (failed) {
            return null;
        }
        if (delegate != null) {
            Level level = owner.getLevel();
            if (level != null && delegate.getLevel() != level) {
                delegate.setLevel(level);
            }
            return delegate;
        }

        try {
            Block easyFarmer = BuiltInRegistries.BLOCK.get(EASY_FARMER_ID);
            Class<?> clazz = ReflectionCache.type(FARMER_TILEENTITY);
            Constructor<?> constructor = ReflectionCache.constructor(clazz, net.minecraft.core.BlockPos.class, BlockState.class);
            delegate = (BlockEntity) constructor.newInstance(owner.getBlockPos(), easyFarmer.defaultBlockState());
            Level level = owner.getLevel();
            if (level != null) {
                delegate.setLevel(level);
            }
            delegate.load(owner.passthroughDataCopy());
            return delegate;
        } catch (ReflectiveOperationException | RuntimeException e) {
            fail(e);
            return null;
        }
    }

    private void setCropState(BlockEntity farmer, BlockState state) {
        try {
            ReflectionCache.field(farmer.getClass(), "crop").set(farmer, state);
            Level ownerLevel = owner.getLevel();
            if (ownerLevel != null && ownerLevel.isClientSide) {
                clientVisualCrop = state;
                clientVisualCropResolved = true;
            } else {
                serverCrop = state;
                serverCropResolved = true;
            }
        } catch (ReflectiveOperationException e) {
            fail(e);
        }
    }

    private final class DirtyTrackingContainer implements Container {
        private final Container delegateContainer;

        private DirtyTrackingContainer(Container delegateContainer) {
            this.delegateContainer = delegateContainer;
        }

        @Override
        public int getContainerSize() {
            return delegateContainer.getContainerSize();
        }

        @Override
        public boolean isEmpty() {
            return delegateContainer.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return delegateContainer.getItem(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack removed = delegateContainer.removeItem(slot, amount);
            if (!removed.isEmpty()) {
                owner.onOutputInventoryReduced();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack removed = delegateContainer.removeItemNoUpdate(slot);
            if (!removed.isEmpty()) {
                owner.onOutputInventoryReduced();
            }
            return removed;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            delegateContainer.setItem(slot, stack);
            owner.onOutputInventoryChanged();
        }

        @Override
        public int getMaxStackSize() {
            return delegateContainer.getMaxStackSize();
        }

        @Override
        public void setChanged() {
            delegateContainer.setChanged();
            owner.onOutputInventoryChanged();
        }

        @Override
        public boolean stillValid(net.minecraft.world.entity.player.Player player) {
            return delegateContainer.stillValid(player);
        }

        @Override
        public void startOpen(net.minecraft.world.entity.player.Player player) {
            delegateContainer.startOpen(player);
        }

        @Override
        public void stopOpen(net.minecraft.world.entity.player.Player player) {
            delegateContainer.stopOpen(player);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return delegateContainer.canPlaceItem(slot, stack);
        }

        @Override
        public boolean canTakeItem(Container target, int slot, ItemStack stack) {
            return delegateContainer.canTakeItem(target, slot, stack);
        }

        @Override
        public void clearContent() {
            delegateContainer.clearContent();
            owner.onOutputInventoryReduced();
        }
    }

    private final class DirtyTrackingItemHandler implements IItemHandler {
        private final IItemHandler delegateHandler;

        private DirtyTrackingItemHandler(IItemHandler delegateHandler) {
            this.delegateHandler = delegateHandler;
        }

        @Override
        public int getSlots() {
            return delegateHandler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return delegateHandler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            ItemStack remainder = delegateHandler.insertItem(slot, stack, simulate);
            if (!simulate && remainder.getCount() != stack.getCount()) {
                owner.onOutputInventoryChanged();
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack extracted = delegateHandler.extractItem(slot, amount, simulate);
            if (!simulate && !extracted.isEmpty()) {
                owner.onOutputInventoryReduced();
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return delegateHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return delegateHandler.isItemValid(slot, stack);
        }
    }

    private void fail(Throwable e) {
        if (!failed) {
            System.err.println(
                    "[Easy Farmer's Delight Compat] Easy Villagers Farmer adapter failed; "
                            + "Paddy Farmer integration is disabled for this block entity."
            );
            e.printStackTrace();
        }
        failed = true;
    }
}
