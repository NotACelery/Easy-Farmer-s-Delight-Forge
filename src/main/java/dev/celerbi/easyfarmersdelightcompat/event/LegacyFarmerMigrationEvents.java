package dev.celerbi.easyfarmersdelightcompat.event;

import dev.celerbi.easyfarmersdelightcompat.item.CompatFarmerItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

public final class LegacyFarmerMigrationEvents {
    private LegacyFarmerMigrationEvents() {
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        Inventory inventory = player.getInventory();
        boolean changed = false;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack current = inventory.getItem(slot);
            ItemStack normalized = CompatFarmerItem.normalizeLoadedStack(current, player.level());
            if (normalized != current) {
                inventory.setItem(slot, normalized);
                changed = true;
            }
        }
        if (changed) {
            inventory.setChanged();
        }
    }
}
