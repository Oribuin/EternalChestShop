package xyz.oribuin.chestshops.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.oribuin.chestshops.model.gui.PreviewHolder;

public class InventoryListener implements Listener {

    /**
     * Disable any form of interaction with the inventory holder
     *
     * @param event The InventoryClickEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PreviewHolder holder))
            return;

        event.setCancelled(true);
    }

}
