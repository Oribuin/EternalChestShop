package xyz.oribuin.chestshops.api;

import org.bukkit.inventory.ItemStack;

public interface ShopContainer {

    /**
     * Get the amount of items that are in the shop.
     *
     * @param item The item to check
     * @return The amount of items in the shop.
     */
    int getStock(ItemStack item);

    /**
     * Get the amount of space left in the shop.
     *
     * @param item The item to check
     * @return The amount of space left in the shop.
     */
    int getRemainingSpace(ItemStack item);

    /**
     * Take an item from the shop container.
     *
     * @param item   The item to take
     * @param amount The amount to take
     * @return If the item was successfully taken.
     */
    boolean takeItem(ItemStack item, int amount);

    /**
     * Add an item to the shop container.
     *
     * @param item   The item to add
     * @param amount The amount to add
     * @return If the item was successfully added.
     */
    boolean addItem(ItemStack item, int amount);

}
