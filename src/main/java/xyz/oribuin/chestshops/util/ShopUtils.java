package xyz.oribuin.chestshops.util;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class ShopUtils {

    private ShopUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert a location to the center of the block
     *
     * @param location The location to convert
     * @return The center of the block
     */
    public static Location center(Location location) {
        final Location loc = location.getBlock().getLocation().clone();
        loc.add(0.5, 0.5, 0.5);
        loc.setYaw(180f);
        loc.setPitch(0f);
        return loc;
    }

    /**
     * Get a bukkit color from a hex code
     *
     * @param hex The hex code
     * @return The bukkit color
     */
    public static Color fromHex(String hex) {
        if (hex == null)
            return Color.BLACK;

        java.awt.Color awtColor;
        try {
            awtColor = java.awt.Color.decode(hex);
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }

        return Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
    }

    /**
     * Get the total number of spare slots in a player's inventory
     *
     * @param player The player
     * @return The amount of empty slots.
     */
    public static int getSpareSlots(Player player) {
        final List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; i++)
            slots.add(i);

        return (int) slots.stream().map(integer -> player.getInventory().getItem(integer))
                .filter(itemStack -> itemStack == null || itemStack.getType() == Material.AIR)
                .count();
    }

    /**
     * Gets a location as a string key
     *
     * @param location The location
     * @return the location as a string key
     * @author Esophose
     */
    public static String locationAsKey(Location location) {
        return String.format("%s;%.2f;%.2f;%.2f", location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }

    /**
     * Get a location from a string key
     *
     * @param key The key
     * @return The location
     */
    public static Location locationFromKey(String key) {
        if (key == null || key.isEmpty())
            return null;

        // format is world;x;y;z
        final String[] split = key.split(";");
        if (split.length != 4)
            return null;

        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
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
     * Parse an integer from an object safely
     *
     * @param object The object
     * @return The integer
     */
    private static int parseInteger(Object object) {
        try {
            if (object instanceof Integer)
                return (int) object;

            return Integer.parseInt(object.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
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
     * Create a file from the plugin's resources
     *
     * @param rosePlugin The plugin
     * @param fileName   The file name
     * @return The file
     */
    @NotNull
    public static File createFile(@NotNull RosePlugin rosePlugin, @NotNull String fileName) {
        File file = new File(rosePlugin.getDataFolder(), fileName); // Create the file

        if (file.exists())
            return file;

        try (InputStream inStream = rosePlugin.getResource(fileName)) {
            if (inStream == null) {
                file.createNewFile();
                return file;
            }

            Files.copy(inStream, Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    /**
     * Create a file in a folder from the plugin's resources
     *
     * @param rosePlugin The plugin
     * @param folderName The folder name
     * @param fileName   The file name
     * @return The file
     */
    @NotNull
    public static File createFile(@NotNull RosePlugin rosePlugin, @NotNull String folderName, @NotNull String fileName) {
        File folder = new File(rosePlugin.getDataFolder(), folderName); // Create the folder
        File file = new File(folder, fileName); // Create the file
        if (!folder.exists())
            folder.mkdirs();

        if (file.exists())
            return file;

        try (InputStream stream = rosePlugin.getResource(folderName + "/" + fileName)) {
            if (stream == null) {
                file.createNewFile();
                return file;
            }

            Files.copy(stream, Paths.get(file.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
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
                BlockFace.WEST,
                BlockFace.UP
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

    public static void setLines(Sign sign, List<String> lines, StringPlaceholders placeholders) {
        if (placeholders == null)
            placeholders = StringPlaceholders.empty();

        // TODO: Add support for 1.16-1.19 signs and 1.20+ signs
//        for (int i = 0; i < lines.size(); i++) {
//            if
//        }
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
            if (stack == null || stack.getType() == Material.AIR)
                continue;

            if (stack.isSimilar(item))
                total += stack.getAmount();
        }

        return total;
    }

}
