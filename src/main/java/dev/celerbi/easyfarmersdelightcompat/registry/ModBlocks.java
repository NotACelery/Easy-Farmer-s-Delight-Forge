package dev.celerbi.easyfarmersdelightcompat.registry;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.block.CompatFarmerBlock;
import dev.celerbi.easyfarmersdelightcompat.block.CutterBlock;
import dev.celerbi.easyfarmersdelightcompat.block.EasyMobFarmNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.block.FarmerVariant;
import dev.celerbi.easyfarmersdelightcompat.block.IronFarmNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.block.VillagerNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.compat.easymobfarm.EasyMobFarmCompat;
import dev.celerbi.easyfarmersdelightcompat.item.CompatFarmerItem;
import dev.celerbi.easyfarmersdelightcompat.item.CutterItem;
import dev.celerbi.easyfarmersdelightcompat.item.EasyMobFarmNoiseSwitchItem;
import dev.celerbi.easyfarmersdelightcompat.item.IronFarmNoiseSwitchItem;
import dev.celerbi.easyfarmersdelightcompat.item.VillagerNoiseSwitchItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            ForgeRegistries.BLOCKS,
            EasyFarmersDelightCompat.MOD_ID
    );

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            ForgeRegistries.ITEMS,
            EasyFarmersDelightCompat.MOD_ID
    );

    public static final RegistryObject<CompatFarmerBlock> PADDY_FARMER = BLOCKS.register(
            "paddy_farmer",
            () -> new CompatFarmerBlock(properties(), FarmerVariant.PADDY)
    );

    public static final RegistryObject<CompatFarmerBlock> RICH_FARMER = BLOCKS.register(
            "rich_farmer",
            () -> new CompatFarmerBlock(properties(), FarmerVariant.RICH)
    );

    public static final RegistryObject<CompatFarmerBlock> RICH_PADDY_FARMER = BLOCKS.register(
            "rich_paddy_farmer",
            () -> new CompatFarmerBlock(properties(), FarmerVariant.RICH_PADDY)
    );

    public static final RegistryObject<CutterBlock> CUTTER = BLOCKS.register(
            "cutter",
            () -> new CutterBlock(properties())
    );

    public static final RegistryObject<VillagerNoiseSwitchBlock> VILLAGER_NOISE_SWITCH = BLOCKS.register(
            "villager_noise_switch",
            () -> new VillagerNoiseSwitchBlock(properties())
    );

    public static final RegistryObject<IronFarmNoiseSwitchBlock> IRON_FARM_NOISE_SWITCH = BLOCKS.register(
            "iron_farm_noise_switch",
            () -> new IronFarmNoiseSwitchBlock(properties())
    );

    public static final RegistryObject<EasyMobFarmNoiseSwitchBlock> EASY_MOB_FARM_NOISE_SWITCH =
            EasyMobFarmCompat.isLoaded()
                    ? BLOCKS.register(
                            "easy_mob_farm_noise_switch",
                            () -> new EasyMobFarmNoiseSwitchBlock(properties())
                    )
                    : null;

    public static final RegistryObject<CutterItem> CUTTER_ITEM = ITEMS.register(
            "cutter",
            () -> new CutterItem(CUTTER.get(), new Item.Properties())
    );

    public static final RegistryObject<VillagerNoiseSwitchItem> VILLAGER_NOISE_SWITCH_ITEM = ITEMS.register(
            "villager_noise_switch",
            () -> new VillagerNoiseSwitchItem(
                    VILLAGER_NOISE_SWITCH.get(),
                    new Item.Properties().stacksTo(1)
            )
    );

    public static final RegistryObject<IronFarmNoiseSwitchItem> IRON_FARM_NOISE_SWITCH_ITEM = ITEMS.register(
            "iron_farm_noise_switch",
            () -> new IronFarmNoiseSwitchItem(
                    IRON_FARM_NOISE_SWITCH.get(),
                    new Item.Properties().stacksTo(1)
            )
    );

    public static final RegistryObject<EasyMobFarmNoiseSwitchItem> EASY_MOB_FARM_NOISE_SWITCH_ITEM =
            EasyMobFarmCompat.isLoaded()
                    ? ITEMS.register(
                            "easy_mob_farm_noise_switch",
                            () -> new EasyMobFarmNoiseSwitchItem(
                                    EASY_MOB_FARM_NOISE_SWITCH.get(),
                                    new Item.Properties().stacksTo(1)
                            )
                    )
                    : null;

    public static final RegistryObject<CompatFarmerItem> PADDY_FARMER_ITEM = ITEMS.register(
            "paddy_farmer",
            () -> new CompatFarmerItem(PADDY_FARMER.get(), new Item.Properties())
    );

    public static final RegistryObject<CompatFarmerItem> RICH_FARMER_ITEM = ITEMS.register(
            "rich_farmer",
            () -> new CompatFarmerItem(RICH_FARMER.get(), new Item.Properties())
    );

    public static final RegistryObject<CompatFarmerItem> RICH_PADDY_FARMER_ITEM = ITEMS.register(
            "rich_paddy_farmer",
            () -> new CompatFarmerItem(RICH_PADDY_FARMER.get(), new Item.Properties())
    );

    private ModBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ModCreativeTabs.register(bus);
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of()
                .strength(2.5F)
                .sound(SoundType.METAL)
                .noOcclusion();
    }
}
