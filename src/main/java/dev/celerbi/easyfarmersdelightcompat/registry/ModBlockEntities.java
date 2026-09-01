package dev.celerbi.easyfarmersdelightcompat.registry;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.EasyMobFarmNoiseSwitchBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.IronFarmNoiseSwitchBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.VillagerNoiseSwitchBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.compat.easymobfarm.EasyMobFarmCompat;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES,
            EasyFarmersDelightCompat.MOD_ID
    );

    public static final RegistryObject<BlockEntityType<CompatFarmerBlockEntity>> COMPAT_FARMER =
            BLOCK_ENTITIES.register(
                    "compat_farmer",
                    () -> BlockEntityType.Builder.of(
                                    CompatFarmerBlockEntity::new,
                                    ModBlocks.PADDY_FARMER.get(),
                                    ModBlocks.RICH_FARMER.get(),
                                    ModBlocks.RICH_PADDY_FARMER.get()
                            )
                            .build(null)
            );

    public static final RegistryObject<BlockEntityType<CutterBlockEntity>> CUTTER = BLOCK_ENTITIES.register(
            "cutter",
            () -> BlockEntityType.Builder.of(CutterBlockEntity::new, ModBlocks.CUTTER.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<VillagerNoiseSwitchBlockEntity>> VILLAGER_NOISE_SWITCH =
            BLOCK_ENTITIES.register(
                    "villager_noise_switch",
                    () -> BlockEntityType.Builder.of(
                                    VillagerNoiseSwitchBlockEntity::new,
                                    ModBlocks.VILLAGER_NOISE_SWITCH.get()
                            )
                            .build(null)
            );

    public static final RegistryObject<BlockEntityType<IronFarmNoiseSwitchBlockEntity>> IRON_FARM_NOISE_SWITCH =
            BLOCK_ENTITIES.register(
                    "iron_farm_noise_switch",
                    () -> BlockEntityType.Builder.of(
                                    IronFarmNoiseSwitchBlockEntity::new,
                                    ModBlocks.IRON_FARM_NOISE_SWITCH.get()
                            )
                            .build(null)
            );

    public static final RegistryObject<BlockEntityType<EasyMobFarmNoiseSwitchBlockEntity>>
            EASY_MOB_FARM_NOISE_SWITCH = EasyMobFarmCompat.isLoaded()
                    ? BLOCK_ENTITIES.register(
                            "easy_mob_farm_noise_switch",
                            () -> BlockEntityType.Builder.of(
                                            EasyMobFarmNoiseSwitchBlockEntity::new,
                                            ModBlocks.EASY_MOB_FARM_NOISE_SWITCH.get()
                                    )
                                    .build(null)
                    )
                    : null;

    private ModBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
