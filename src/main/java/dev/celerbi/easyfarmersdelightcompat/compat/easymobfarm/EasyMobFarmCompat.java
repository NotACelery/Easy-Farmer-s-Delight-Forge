package dev.celerbi.easyfarmersdelightcompat.compat.easymobfarm;

import net.minecraftforge.fml.ModList;

public final class EasyMobFarmCompat {
    public static final String MOD_ID = "easy_mob_farm";

    private EasyMobFarmCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}
