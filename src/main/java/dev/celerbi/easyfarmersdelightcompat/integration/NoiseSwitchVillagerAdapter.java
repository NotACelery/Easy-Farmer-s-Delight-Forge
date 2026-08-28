package dev.celerbi.easyfarmersdelightcompat.integration;

import dev.celerbi.easyfarmersdelightcompat.blockentity.VillagerNoiseSwitchBlockEntity;
import java.lang.reflect.Method;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class NoiseSwitchVillagerAdapter {
    private static final ResourceLocation VILLAGER_ITEM_ID = new ResourceLocation(
            "easy_villagers",
            "villager");

    private final VillagerNoiseSwitchBlockEntity owner;
    private Villager cachedVillager;
    private boolean failed;

    public NoiseSwitchVillagerAdapter(VillagerNoiseSwitchBlockEntity owner) {
        this.owner = owner;
    }

    public void reset() {
        if (cachedVillager != null)
            cachedVillager.setTradingPlayer(null);
        cachedVillager = null;
        failed = false;
    }

    public boolean isVillagerItem(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && VILLAGER_ITEM_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public Villager getVillagerEntity() {
        if (failed || owner.getStoredVillager().isEmpty())
            return null;

        Level level = owner.getLevel();
        if (level == null)
            return null;
        if (cachedVillager != null && cachedVillager.level() == level)
            return cachedVillager;

        try {
            ItemStack source = owner.getStoredVillager();
            Method m = source.getItem().getClass().getMethod(
                    "getVillager",
                    Level.class,
                    ItemStack.class);
            Object o = m.invoke(source.getItem(), level, source);
            if (!(o instanceof Villager v))
                throw new IllegalStateException("Easy Villagers getVillager did not return a Villager");
            return cachedVillager = v;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            fail(e);
            return null;
        }
    }

    public boolean advanceAge() {
        Villager v = getVillagerEntity();
        if (v == null)
            return false;

        int old = v.getAge();
        v.setAge(old + 1);
        return old < 0 && v.getAge() >= 0;
    }

    public void flushToOwner() {
        if (failed || cachedVillager == null || owner.getStoredVillager().isEmpty())
            return;

        try {
            ItemStack updated = owner.getStoredVillager();
            Method chosen = null;

            for (Method m : updated.getItem().getClass().getMethods())
                if (m.getName().equals("setVillager")
                        && m.getParameterCount() == 2
                        && m.getParameterTypes()[0] == ItemStack.class
                        && m.getParameterTypes()[1].isInstance(cachedVillager)) {
                    chosen = m;
                    break;
                }

            if (chosen == null)
                throw new NoSuchMethodException("setVillager");

            chosen.invoke(updated.getItem(), updated, cachedVillager);
            owner.updateVillagerFromAdapter(updated);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            fail(e);
        }
    }

    private void fail(Throwable e) {
        if (!failed) {
            System.err.println("[Easy Farmer's Delight Compat] Easy Villagers VillagerItem adapter failed.");
            e.printStackTrace();
        }
        failed = true;
        if (cachedVillager != null)
            cachedVillager.setTradingPlayer(null);
        cachedVillager = null;
    }
}
