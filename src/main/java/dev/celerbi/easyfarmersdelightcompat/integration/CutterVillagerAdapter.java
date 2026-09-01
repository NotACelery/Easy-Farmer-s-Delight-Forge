package dev.celerbi.easyfarmersdelightcompat.integration;

import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import java.lang.reflect.Constructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CutterVillagerAdapter {
    private static final ResourceLocation EASY_FARMER_ID = new ResourceLocation("easy_villagers",
            "farmer");
    private static final String FARMER_TILEENTITY = "de.maxhenkel.easyvillagers.blocks.tileentity.FarmerTileentity";
    private static final String VILLAGER_ITEM = "de.maxhenkel.easyvillagers.items.VillagerItem";
    private final CutterBlockEntity owner;
    private BlockEntity delegate;
    private Villager cachedVillagerEntity;
    private boolean failed;

    public CutterVillagerAdapter(CutterBlockEntity owner) {
        this.owner = owner;
    }

    public void reset() {
        if (cachedVillagerEntity != null) {
            cachedVillagerEntity.setTradingPlayer(null);
        }
        delegate = null;
        cachedVillagerEntity = null;
        failed = false;
    }

    public boolean isVillagerItem(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return false;
        try {
            return ReflectionCache.type(VILLAGER_ITEM).isInstance(stack.getItem());
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    public Villager getVillagerEntity() {
        Level level = owner.getLevel();
        if (cachedVillagerEntity != null && (level == null || cachedVillagerEntity.level() == level)) {
            return cachedVillagerEntity;
        }

        BlockEntity farmer = getDelegate();
        if (farmer == null)
            return null;
        try {
            Object result = ReflectionCache.publicMethod(farmer.getClass(), "getVillagerEntity").invoke(farmer);
            cachedVillagerEntity = result instanceof Villager villager ? villager : null;
            return cachedVillagerEntity;
        } catch (ReflectiveOperationException e) {
            fail();
            return null;
        }
    }

    public boolean hasAdultVillager() {
        Villager v = getVillagerEntity();
        return v != null && !v.isBaby();
    }

    public boolean advanceAge() {
        Villager villager = getVillagerEntity();
        if (villager == null)
            return false;
        int previousAge = villager.getAge();
        villager.setAge(previousAge + 1);
        return previousAge < 0 && villager.getAge() >= 0;
    }

    public void flushToOwner() {
        BlockEntity f = getDelegate();
        if (f == null)
            return;
        try {
            Object v = ReflectionCache.publicMethod(f.getClass(), "getVillager").invoke(f);
            if (v instanceof ItemStack s && !s.isEmpty())
                owner.updateVillagerFromAdapter(s.copyWithCount(1));
        } catch (ReflectiveOperationException e) {
            fail();
        }
    }

    private BlockEntity getDelegate() {
        if (failed || owner.getStoredVillager().isEmpty())
            return null;
        Level level = owner.getLevel();
        if (delegate != null) {
            if (level != null && delegate.getLevel() != level)
                delegate.setLevel(level);
            return delegate;
        }
        try {
            Block easyFarmer = BuiltInRegistries.BLOCK.get(EASY_FARMER_ID);
            Class<?> clazz = ReflectionCache.type(FARMER_TILEENTITY);
            Constructor<?> constructor = ReflectionCache.constructor(clazz, BlockPos.class, BlockState.class);
            delegate = (BlockEntity) constructor.newInstance(owner.getBlockPos(), easyFarmer.defaultBlockState());
            if (level != null)
                delegate.setLevel(level);
            ReflectionCache.field(clazz, "villager").set(delegate, owner.getStoredVillager().copyWithCount(1));
            ReflectionCache.field(clazz, "villagerEntity").set(delegate, null);
            return delegate;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            fail();
            return null;
        }
    }

    private void fail() {
        failed = true;
        delegate = null;
        cachedVillagerEntity = null;
    }
}
