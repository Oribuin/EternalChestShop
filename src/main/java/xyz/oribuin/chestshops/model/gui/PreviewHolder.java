package xyz.oribuin.chestshops.model.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.oribuin.chestshops.util.ItemBuilder;

public class PreviewHolder implements InventoryHolder {

    private final Inventory inventory;

    public PreviewHolder(ItemStack toDisplay) {
        this.inventory = Bukkit.createInventory(this, InventoryType.DISPENSER, "Preview");

        ItemStack empty = ItemBuilder.empty(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < this.inventory.getSize(); i++) {
            this.inventory.setItem(i, empty);
        }

        this.inventory.setItem(4, toDisplay);
    }

    /**
     * Open the inventory for the player if they are not sleeping
     *
     * @param player The player to open the inventory for
     */
    public void open(Player player) {
        if (player.isSleeping()) return;

        player.openInventory(this.inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }


}
