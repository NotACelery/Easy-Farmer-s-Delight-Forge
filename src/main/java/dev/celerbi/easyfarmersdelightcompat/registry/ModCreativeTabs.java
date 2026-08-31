package dev.celerbi.easyfarmersdelightcompat.registry;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.compat.easymobfarm.EasyMobFarmCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
            Registries.CREATIVE_MODE_TAB,
            EasyFarmersDelightCompat.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.easyfarmersdelightcompat"))
                    .icon(() -> new ItemStack(ModBlocks.PADDY_FARMER_ITEM.get()))
                    .displayItems((p, o) -> {
                        o.accept(ModBlocks.PADDY_FARMER_ITEM.get());
                        o.accept(ModBlocks.RICH_FARMER_ITEM.get());
                        o.accept(ModBlocks.RICH_PADDY_FARMER_ITEM.get());
                        o.accept(ModBlocks.CUTTER_ITEM.get());
                        o.accept(ModBlocks.VILLAGER_NOISE_SWITCH_ITEM.get());
                        o.accept(ModBlocks.IRON_FARM_NOISE_SWITCH_ITEM.get());
                        if (EasyMobFarmCompat.isLoaded() && ModBlocks.EASY_MOB_FARM_NOISE_SWITCH_ITEM != null) {
                            o.accept(ModBlocks.EASY_MOB_FARM_NOISE_SWITCH_ITEM.get());
                        }
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
