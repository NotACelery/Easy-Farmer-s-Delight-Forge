package dev.celerbi.easyfarmersdelightcompat.integration.jade;

import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import dev.celerbi.easyfarmersdelightcompat.blockentity.VillagerNoiseSwitchBlockEntity;
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

public enum NoiseSwitchJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(
            EasyFarmersDelightCompat.MOD_ID,
            "noise_switch"
    );
    private static final String HAS_VILLAGER = "EfdcNoiseSwitchHasVillager";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof VillagerNoiseSwitchBlockEntity noiseSwitch) {
            data.putBoolean(HAS_VILLAGER, noiseSwitch.hasVillager());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        boolean muted = ClientPreferences.villagersMuted();
        tooltip.add(Component.translatable(muted
                        ? "jade.easyfarmersdelightcompat.noise_switch.muted"
                        : "jade.easyfarmersdelightcompat.noise_switch.enabled")
                .withStyle(muted ? ChatFormatting.RED : ChatFormatting.GREEN));

        boolean hasVillager;
        CompoundTag data = accessor.getServerData();
        if (data.contains(HAS_VILLAGER)) {
            hasVillager = data.getBoolean(HAS_VILLAGER);
        } else if (accessor.getBlockEntity() instanceof VillagerNoiseSwitchBlockEntity noiseSwitch) {
            hasVillager = noiseSwitch.hasVillager();
        } else {
            return;
        }

        if (!hasVillager) {
            tooltip.add(Component.translatable("jade.easyfarmersdelightcompat.villager_required")
                    .withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
