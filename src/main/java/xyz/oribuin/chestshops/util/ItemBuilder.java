package xyz.oribuin.chestshops.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unused", "deprecation"})
public class ItemBuilder {

    private final ItemStack item;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
    }

    /**
     * Create an empty ItemStack with a Material.
     *
     * @param material The Material.
     * @return The ItemStack.
     */
    public static ItemStack empty(Material material) {
        return new ItemBuilder(material).name(" ").build();
    }

    /**
     * Set the ItemStack's Material.
     *
     * @param material The Material.
     * @return Item.Builder.
     */
    public ItemBuilder setMaterial(Material material) {
        this.item.setType(material);
        return this;
    }

    /**
     * Set the ItemStack's Display Name.
     *
     * @param text The text.
     * @return Item.Builder.
     */
    public ItemBuilder name(@Nullable String text) {
        ItemMeta meta = this.item.getItemMeta();
        if (meta == null || text == null)
            return this;

        meta.setDisplayName(text);
        this.item.setItemMeta(meta);

        return this;
    }

    /**
     * Finalize the Item Builder and create the stack.
     *
     * @return The ItemStack
     */
    public ItemStack build() {
        return this.item;
    }

}