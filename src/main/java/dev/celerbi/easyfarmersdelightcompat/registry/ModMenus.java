package dev.celerbi.easyfarmersdelightcompat.registry;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.menu.CutterMenu;
import dev.celerbi.easyfarmersdelightcompat.menu.PaddyFarmerMenu;
import dev.celerbi.easyfarmersdelightcompat.menu.RichFarmerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            ForgeRegistries.MENU_TYPES,
            EasyFarmersDelightCompat.MOD_ID
    );

    public static final RegistryObject<MenuType<RichFarmerMenu>> RICH_FARMER = MENUS.register(
            "rich_farmer_output",
            () -> IForgeMenuType.create(RichFarmerMenu::fromNetwork)
    );

    public static final RegistryObject<MenuType<PaddyFarmerMenu>> PADDY_FARMER = MENUS.register(
            "paddy_farmer_output",
            () -> IForgeMenuType.create(PaddyFarmerMenu::fromNetwork)
    );

    public static final RegistryObject<MenuType<CutterMenu>> CUTTER = MENUS.register(
            "cutter",
            () -> IForgeMenuType.create(CutterMenu::fromNetwork)
    );

    private ModMenus() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
