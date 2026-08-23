package dev.celerbi.easyfarmersdelightcompat.compat.jade;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.blockentity.IronFarmNoiseSwitchBlockEntity;
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

public enum IronFarmNoiseSwitchJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(EasyFarmersDelightCompat.MOD_ID, "iron_farm_noise_switch");
    private static final String STAGE = "EfdcIronFarmNoiseSwitchStage";
    private static final String HAS_GOLEM = "EfdcIronFarmNoiseSwitchHasGolem";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof IronFarmNoiseSwitchBlockEntity noiseSwitch) {
            data.putInt(STAGE, noiseSwitch.assemblyStage());
            data.putBoolean(HAS_GOLEM, noiseSwitch.hasGolem());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        int stage = data.contains(STAGE) ? data.getInt(STAGE) : 0;
        boolean hasGolem = data.contains(HAS_GOLEM) && data.getBoolean(HAS_GOLEM);

        if (!data.contains(STAGE) && accessor.getBlockEntity() instanceof IronFarmNoiseSwitchBlockEntity noiseSwitch) {
            stage = noiseSwitch.assemblyStage();
            hasGolem = noiseSwitch.hasGolem();
        }

        if (!hasGolem) {
            if (stage < IronFarmNoiseSwitchBlockEntity.REQUIRED_IRON_BLOCKS) {
                tooltip.add(Component.translatable(
                        "jade.easyfarmersdelightcompat.iron_farm_noise_switch.assembly",
                        stage, IronFarmNoiseSwitchBlockEntity.REQUIRED_IRON_BLOCKS
                ).withStyle(ChatFormatting.YELLOW));
                tooltip.add(Component.translatable(
                        "jade.easyfarmersdelightcompat.iron_farm_noise_switch.add_iron"
                ).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable(
                        "jade.easyfarmersdelightcompat.iron_farm_noise_switch.add_pumpkin"
                ).withStyle(ChatFormatting.YELLOW));
            }
            return;
        }

        boolean muted = ClientPreferences.ironFarmSoundsMuted();
        tooltip.add(Component.translatable(muted
                        ? "jade.easyfarmersdelightcompat.iron_farm_noise_switch.muted"
                        : "jade.easyfarmersdelightcompat.iron_farm_noise_switch.enabled")
                .withStyle(muted ? ChatFormatting.RED : ChatFormatting.GREEN));
    }

    @Override
    public ResourceLocation getUid() { return UID; }
}
