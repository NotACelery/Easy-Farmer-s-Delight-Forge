package dev.celerbi.easyfarmersdelightcompat.integration;

import dev.celerbi.easyfarmersdelightcompat.blockentity.VillagerNoiseSwitchBlockEntity;
import java.lang.reflect.Method;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class NoiseSwitchVillagerAdapter {
    private static final ResourceLocation VILLAGER_ITEM_ID = new ResourceLocation("easy_villagers", "villager");

    private final VillagerNoiseSwitchBlockEntity owner;
    private Villager cachedVillager;
    private boolean failed;

    public NoiseSwitchVillagerAdapter(VillagerNoiseSwitchBlockEntity owner) {
        this.owner = owner;
    }

    public void reset() {
        if (cachedVillager != null) {
            cachedVillager.setTradingPlayer(null);
        }
        cachedVillager = null;
        failed = false;
    }

    public boolean isVillagerItem(ItemStack stack) {
        return stack != null
                && !stack.isEmpty()
                && VILLAGER_ITEM_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public Villager getVillagerEntity() {
        if (failed || owner.getStoredVillager().isEmpty()) {
            return null;
        }

        Level level = owner.getLevel();
        if (level == null) {
            return null;
        }
        if (cachedVillager != null && cachedVillager.level() == level) {
            return cachedVillager;
        }

        try {
            ItemStack source = owner.getStoredVillager();
            Method getVillager = source.getItem().getClass().getMethod("getVillager", Level.class, ItemStack.class);
            Object result = getVillager.invoke(source.getItem(), level, source);
            if (!(result instanceof Villager villager)) {
                throw new IllegalStateException("Easy Villagers getVillager did not return a Villager");
            }
            cachedVillager = villager;
            return villager;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            fail(error);
            return null;
        }
    }

    public boolean advanceAge() {
        Villager villager = getVillagerEntity();
        if (villager == null) {
            return false;
        }

        int previousAge = villager.getAge();
        villager.setAge(previousAge + 1);
        return previousAge < 0 && villager.getAge() >= 0;
    }

    public void flushToOwner() {
        if (failed || cachedVillager == null || owner.getStoredVillager().isEmpty()) {
            return;
        }

        try {
            ItemStack updated = owner.getStoredVillager();
            Method setVillager = null;

            for (Method method : updated.getItem().getClass().getMethods()) {
                if (method.getName().equals("setVillager")
                        && method.getParameterCount() == 2
                        && method.getParameterTypes()[0] == ItemStack.class
                        && method.getParameterTypes()[1].isInstance(cachedVillager)) {
                    setVillager = method;
                    break;
                }
            }

            if (setVillager == null) {
                throw new NoSuchMethodException("setVillager");
            }

            setVillager.invoke(updated.getItem(), updated, cachedVillager);
            owner.updateVillagerFromAdapter(updated);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError error) {
            fail(error);
        }
    }

    private void fail(Throwable error) {
        if (!failed) {
            System.err.println("[Easy Farmer's Delight] Easy Villagers VillagerItem adapter failed.");
            error.printStackTrace();
        }
        failed = true;
        if (cachedVillager != null) {
            cachedVillager.setTradingPlayer(null);
        }
        cachedVillager = null;
    }
}
