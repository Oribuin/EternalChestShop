package xyz.oribuin.chestshops.util;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public final class ShopUtils {

    private ShopUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Format a material name through this long method
     *
     * @param material The material
     * @return The material name.
     */
    public static String format(Material material) {
        return StringUtils.capitalize(material.name().toLowerCase().replace("_", " "));
    }

    /**
     * Get an enum from a string value
     *
     * @param enumClass The enum class
     * @param name      The name of the enum
     * @param <T>       The enum type
     * @return The enum
     */
    public static <T extends Enum<T>> T getEnum(Class<T> enumClass, String name) {
        if (name == null)
            return null;

        try {
            return Enum.valueOf(enumClass, name.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }

        return null;
    }

    public static byte[] serializeItem(@Nullable ItemStack itemStack) {
        if (itemStack == null)
            return new byte[0];

        byte[] data = new byte[0];
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             BukkitObjectOutputStream oos = new BukkitObjectOutputStream(stream)) {
            oos.writeObject(itemStack);
            data = stream.toByteArray();
        } catch (IOException ignored) {
        }

        return data;
    }

    @Nullable
    public static ItemStack deserializeItem(byte[] data) {
        if (data == null || data.length == 0)
            return null;

        ItemStack itemStack = null;
        try (ByteArrayInputStream stream = new ByteArrayInputStream(data);
             BukkitObjectInputStream ois = new BukkitObjectInputStream(stream)) {
            itemStack = (ItemStack) ois.readObject();
        } catch (IOException | ClassNotFoundException ignored) {
        }

        return itemStack;
    }

    /**
     * Get the empty face of a block (the face that is air)
     *
     * @param block     The block
     * @param preferred The preferred face
     * @return The empty face
     */
    public static BlockFace getEmptyFace(Block block, BlockFace preferred) {
        if (preferred != null && block.getRelative(preferred).getType() == Material.AIR)
            return preferred;

        List<BlockFace> allowed = List.of(BlockFace.NORTH,
                BlockFace.EAST,
                BlockFace.SOUTH,
                BlockFace.WEST
        );

        for (BlockFace face : allowed) {
            Block relative = block.getRelative(face);
            if (relative.getType() == Material.AIR)
                return face;
        }

        return BlockFace.SELF;
    }

    /**
     * Get the item name of an item stack
     *
     * @param stack The item stack
     * @return The item name
     */
    @SuppressWarnings("deprecation")
    public static String getItemName(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR)
            return "Air";

        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasDisplayName())
            return format(stack.getType());

        return meta.getDisplayName();
    }


    /**
     * Get the spare slots for an item in an inventory
     *
     * @param inventory The inventory
     * @param item      The item
     * @return The spare slots
     */
    public static int getSpareSlotsForItem(Inventory inventory, ItemStack item) {
        int total = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (stack == null || stack.getType() == Material.AIR) {
                total += item.getMaxStackSize();
                continue;
            }

            if (stack.isSimilar(item))
                total += item.getMaxStackSize() - stack.getAmount();
        }

        return total;
    }

    /**
     * Get the amount of an item in an inventory
     *
     * @param inventory The inventory
     * @param item      The item
     * @return The amount
     */
    public static int getAmountOfItem(Inventory inventory, ItemStack item) {
        int total = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }

            if (stack.isSimilar(item))
                total += stack.getAmount();
        }

        return total;
    }

    /**
     * Format an amount of money into the shorthand format (e.g. 1.5k)
     *
     * @param currency The currency
     * @return The formatted currency
     */
    public static String formatShorthand(double currency) {
        DecimalFormat format = new DecimalFormat("0.#");

        if (currency >= 1000000000)
            return format.format(currency / 1000000000) + "b";

        if (currency >= 1000000)
            return format.format(currency / 1000000) + "m";

        if (currency >= 1000)
            return format.format(currency / 1000) + "k";

        return format.format(currency);
    }
}
