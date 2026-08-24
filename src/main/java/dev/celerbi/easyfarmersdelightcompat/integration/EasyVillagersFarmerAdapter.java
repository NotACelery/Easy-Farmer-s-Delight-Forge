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

/** Reflection bridge for the small Easy Villagers Farmer surface this addon needs. */
public final class EasyVillagersFarmerAdapter {
    private static final ResourceLocation EASY_FARMER_ID = new ResourceLocation("easy_villagers", "farmer");
    private static final ResourceLocation RICE_CROP_ID = new ResourceLocation("farmersdelight", "rice");

    private static final String FARMER_TILEENTITY = "de.maxhenkel.easyvillagers.blocks.tileentity.FarmerTileentity";
    private static final String VILLAGER_ITEM = "de.maxhenkel.easyvillagers.items.VillagerItem";
    private static final String OUTPUT_CONTAINER = "de.maxhenkel.easyvillagers.gui.OutputContainer";
    private static final String MAIN_CLASS = "de.maxhenkel.easyvillagers.Main";
    private static final int DEFAULT_FARM_SPEED = 10;
    private static boolean farmSpeedFallbackWarned;

    private final CompatFarmerBlockEntity owner;
    private BlockEntity delegate;
    private Container trackedOutputInventory;
    private IItemHandler trackedItemHandler;
    private boolean failed;

    public EasyVillagersFarmerAdapter(CompatFarmerBlockEntity owner) {
        this.owner = owner;
    }

    public void reset() {
        delegate = null;
        trackedOutputInventory = null;
        trackedItemHandler = null;
        failed = false;
    }

    public boolean isVillagerItem(ItemStack stack) {
        try {
            return Class.forName(VILLAGER_ITEM).isInstance(stack.getItem());
        } catch (ClassNotFoundException e) {
            fail(e);
            return false;
        }
    }

    public boolean hasVillager(RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return false;
        }
        try {
            return (boolean) farmer.getClass().getMethod("hasVillager").invoke(farmer);
        } catch (ReflectiveOperationException e) {
            fail(e);
            return false;
        }
    }

    public void insertVillager(ItemStack stack, RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return;
        }
        try {
            Field villagerField = findField(farmer.getClass(), "villager");
            Field villagerEntityField = findField(farmer.getClass(), "villagerEntity");
            villagerField.set(farmer, stack.copyWithCount(1));
            villagerEntityField.set(farmer, null);

            Villager villager = getVillagerEntity(registries);
            if (villager != null && villager.getVillagerXp() <= 0 && villager.getVillagerData().getProfession() != VillagerProfession.NITWIT) {
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
            Method getVillager = farmer.getClass().getMethod("getVillager");
            ItemStack result = ((ItemStack) getVillager.invoke(farmer)).copy();

            Field villagerEntityField = findField(farmer.getClass(), "villagerEntity");
            Object entity = villagerEntityField.get(farmer);
            if (entity instanceof Villager villager) {
                villager.setTradingPlayer(null);
            }

            findField(farmer.getClass(), "villager").set(farmer, ItemStack.EMPTY);
            villagerEntityField.set(farmer, null);
            return result;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return ItemStack.EMPTY;
        }
    }

    public Villager getVillagerEntity(RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return null;
        }
        try {
            Object result = farmer.getClass().getMethod("getVillagerEntity").invoke(farmer);
            return result instanceof Villager villager ? villager : null;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return null;
        }
    }

    public boolean advanceVillagerAge(RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return false;
        }
        try {
            return (boolean) farmer.getClass().getMethod("advanceAge").invoke(farmer);
        } catch (ReflectiveOperationException e) {
            fail(e);
            return false;
        }
    }

    public BlockState getCrop(RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return null;
        }
        try {
            return (BlockState) farmer.getClass().getMethod("getCrop").invoke(farmer);
        } catch (ReflectiveOperationException e) {
            fail(e);
            return null;
        }
    }

    public boolean hasRiceCrop(RegistryAccess registries) {
        BlockState crop = getCrop(registries);
        return crop != null && RICE_CROP_ID.equals(BuiltInRegistries.BLOCK.getKey(crop.getBlock()));
    }

    /** Delegates seed validation to Easy Villagers. */
    public boolean isValidSeed(ItemStack stack, RegistryAccess registries) {
        if (stack.isEmpty()) {
            return false;
        }
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return false;
        }
        try {
            Object result = farmer.getClass().getMethod("isValidSeed", Item.class).invoke(farmer, stack.getItem());
            return result instanceof Boolean valid && valid;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return false;
        }
    }

    /** Resolves a seed with Easy Villagers, then stores the crop without delegate sync. */
    public boolean setCropFromSeed(ItemStack stack, RegistryAccess registries) {
        if (stack.isEmpty()) {
            return false;
        }
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return false;
        }
        try {
            Object result = farmer.getClass().getMethod("getSeedCrop", Item.class).invoke(farmer, stack.getItem());
            if (!(result instanceof BlockState crop)) {
                return false;
            }
            setCropState(farmer, crop);
            owner.setChanged();
            return true;
        } catch (ReflectiveOperationException e) {
            fail(e);
            return false;
        }
    }

    /** Runs Easy Villagers crop aging without ticking/syncing the unplaced delegate. */
    public boolean ageCrop(RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return false;
        }
        try {
            Object villager = farmer.getClass().getMethod("getVillagerEntity").invoke(farmer);
            Method ageCrop = null;
            Class<?> current = farmer.getClass();
            while (current != null && ageCrop == null) {
                for (Method method : current.getDeclaredMethods()) {
                    if (method.getName().equals("ageCrop") && method.getParameterCount() == 1) {
                        ageCrop = method;
                        break;
                    }
                }
                current = current.getSuperclass();
            }
            if (ageCrop == null) {
                throw new NoSuchMethodException("ageCrop");
            }
            ageCrop.setAccessible(true);
            Object result = ageCrop.invoke(farmer, villager);
            return result instanceof Boolean changed && changed;
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
            owner.setChanged();
        }
    }

    public ItemStack removeCrop(RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return ItemStack.EMPTY;
        }
        try {
            Field cropField = findField(farmer.getClass(), "crop");
            Object value = cropField.get(farmer);
            cropField.set(farmer, null);
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
            Object result = findField(farmer.getClass(), "itemHandler").get(farmer);
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
            Object result = farmer.getClass().getMethod("getOutputInventory").invoke(farmer);
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
            Class<?> clazz = Class.forName(OUTPUT_CONTAINER);
            Constructor<?> constructor = clazz.getConstructor(
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
        try {
            Class<?> main = Class.forName(MAIN_CLASS);
            Object serverConfig = main.getField("SERVER_CONFIG").get(null);
            if (serverConfig == null) {
                warnFarmSpeedFallback(null);
                return DEFAULT_FARM_SPEED;
            }
            Object farmSpeed = serverConfig.getClass().getField("farmSpeed").get(serverConfig);
            if (farmSpeed instanceof IntSupplier intSupplier) {
                return Math.max(1, intSupplier.getAsInt());
            }
            if (farmSpeed instanceof Supplier<?> supplier) {
                Object value = supplier.get();
                if (value instanceof Number number) {
                    return Math.max(1, number.intValue());
                }
            }
            Object value = farmSpeed.getClass().getMethod("get").invoke(farmSpeed);
            if (value instanceof Number number) {
                return Math.max(1, number.intValue());
            }
        } catch (ReflectiveOperationException | RuntimeException e) {
            // A failed farmSpeed lookup must not silently turn the Farmer into a 1-tick-speed machine.
            warnFarmSpeedFallback(e);
            return DEFAULT_FARM_SPEED;
        }
        warnFarmSpeedFallback(null);
        return DEFAULT_FARM_SPEED;
    }

    private static void warnFarmSpeedFallback(Exception e) {
        if (farmSpeedFallbackWarned) return;
        farmSpeedFallbackWarned = true;
        System.err.println("[Easy Farmer's Delight Compat] Could not read Easy Villagers farmer.farm_speed; using default 10 without disabling the Farmer adapter.");
        if (e != null) e.printStackTrace();
    }

    public CompoundTag snapshot(CompoundTag fallback, RegistryAccess registries) {
        BlockEntity farmer = getDelegate(registries);
        if (farmer == null) {
            return fallback.copy();
        }

        CompoundTag merged = fallback.copy();
        // Mirror Easy Villagers-owned keys, including removals.
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
            Class<?> clazz = Class.forName(FARMER_TILEENTITY);
            Constructor<?> constructor = clazz.getConstructor(net.minecraft.core.BlockPos.class, BlockState.class);
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
            findField(farmer.getClass(), "crop").set(farmer, state);
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
                owner.setChanged();
            }
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack removed = delegateContainer.removeItemNoUpdate(slot);
            if (!removed.isEmpty()) {
                owner.setChanged();
            }
            return removed;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            delegateContainer.setItem(slot, stack);
            owner.setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return delegateContainer.getMaxStackSize();
        }
        @Override
        public void setChanged() {
            delegateContainer.setChanged();
            owner.setChanged();
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
            owner.setChanged();
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
                owner.setChanged();
            }
            return remainder;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack extracted = delegateHandler.extractItem(slot, amount, simulate);
            if (!simulate && !extracted.isEmpty()) {
                owner.setChanged();
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

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private void fail(Exception e) {
        if (!failed) {
            System.err.println("[Easy Farmer's Delight Compat] Easy Villagers Farmer adapter failed; Paddy Farmer integration is disabled for this block entity.");
            e.printStackTrace();
        }
        failed = true;
    }
}
