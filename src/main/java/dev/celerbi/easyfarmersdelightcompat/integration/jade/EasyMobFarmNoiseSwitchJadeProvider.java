package dev.celerbi.easyfarmersdelightcompat.integration.jade;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.blockentity.EasyMobFarmNoiseSwitchBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.client.ClientPreferences;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum EasyMobFarmNoiseSwitchJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(
            EasyFarmersDelightCompat.MOD_ID, "easy_mob_farm_noise_switch"
    );
    private static final String STAGE = "EfdcEasyMobFarmNoiseSwitchStage";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof EasyMobFarmNoiseSwitchBlockEntity noiseSwitch) {
            data.putInt(STAGE, noiseSwitch.assemblyStage());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        int stage = data.contains(STAGE) ? data.getInt(STAGE) : 0;

        if (!data.contains(STAGE)
                && accessor.getBlockEntity() instanceof EasyMobFarmNoiseSwitchBlockEntity noiseSwitch) {
            stage = noiseSwitch.assemblyStage();
        }

        if (stage < EasyMobFarmNoiseSwitchBlockEntity.REQUIRED_ROTTEN_FLESH) {
            tooltip.add(Component.translatable(
                    "jade.easyfarmersdelightcompat.easy_mob_farm_noise_switch.assembly",
                    stage, EasyMobFarmNoiseSwitchBlockEntity.REQUIRED_ROTTEN_FLESH
            ).withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.translatable(
                    "jade.easyfarmersdelightcompat.easy_mob_farm_noise_switch.add_rotten_flesh"
            ).withStyle(ChatFormatting.GRAY));
            return;
        }

        boolean muted = ClientPreferences.easyMobFarmSoundsMuted();
        tooltip.add(Component.translatable(muted
                        ? "jade.easyfarmersdelightcompat.easy_mob_farm_noise_switch.muted"
                        : "jade.easyfarmersdelightcompat.easy_mob_farm_noise_switch.enabled")
                .withStyle(muted ? ChatFormatting.RED : ChatFormatting.GREEN));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
