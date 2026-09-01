package dev.celerbi.easyfarmersdelightcompat.integration.jade;

import dev.celerbi.easyfarmersdelightcompat.block.CompatFarmerBlock;
import dev.celerbi.easyfarmersdelightcompat.block.CutterBlock;
import dev.celerbi.easyfarmersdelightcompat.block.EasyMobFarmNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.block.IronFarmNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.block.VillagerNoiseSwitchBlock;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.EasyMobFarmNoiseSwitchBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.IronFarmNoiseSwitchBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.blockentity.VillagerNoiseSwitchBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.compat.easymobfarm.EasyMobFarmCompat;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin("jade")
public final class EfdcJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(FarmerStatusJadeProvider.INSTANCE, CompatFarmerBlockEntity.class);
        registration.registerBlockDataProvider(FarmerHarvestToolJadeProvider.INSTANCE, CompatFarmerBlockEntity.class);
        registration.registerBlockDataProvider(CutterJadeProvider.INSTANCE, CutterBlockEntity.class);
        registration.registerBlockDataProvider(NoiseSwitchJadeProvider.INSTANCE, VillagerNoiseSwitchBlockEntity.class);
        registration.registerBlockDataProvider(
                IronFarmNoiseSwitchJadeProvider.INSTANCE,
                IronFarmNoiseSwitchBlockEntity.class);
        if (EasyMobFarmCompat.isLoaded()) {
            registration.registerBlockDataProvider(
                    EasyMobFarmNoiseSwitchJadeProvider.INSTANCE,
                    EasyMobFarmNoiseSwitchBlockEntity.class);
        }
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(FarmerStatusJadeProvider.INSTANCE, CompatFarmerBlock.class);
        registration.registerBlockComponent(FarmerHarvestToolJadeProvider.INSTANCE, CompatFarmerBlock.class);
        registration.registerBlockComponent(CutterJadeProvider.INSTANCE, CutterBlock.class);
        registration.registerBlockComponent(NoiseSwitchJadeProvider.INSTANCE, VillagerNoiseSwitchBlock.class);
        registration.registerBlockComponent(IronFarmNoiseSwitchJadeProvider.INSTANCE, IronFarmNoiseSwitchBlock.class);
        if (EasyMobFarmCompat.isLoaded()) {
            registration.registerBlockComponent(
                    EasyMobFarmNoiseSwitchJadeProvider.INSTANCE,
                    EasyMobFarmNoiseSwitchBlock.class);
        }
    }
}
