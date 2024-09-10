package xyz.oribuin.chestshops.api.impl;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import xyz.oribuin.chestshops.api.ShopContainer;

public class BasicContainer implements ShopContainer {

    private final Inventory inventory;


    @Override
    public int getStock(ItemStack item) {
        return
        return 0;
    }

    @Override
    public int getRemainingSpace(ItemStack item) {
        return 0;
    }

    @Override
    public boolean takeItem(ItemStack item, int amount) {
        return false;
    }

    @Override
    public boolean addItem(ItemStack item, int amount) {
        return false;
    }

}
