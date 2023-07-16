package xyz.oribuin.chestshops.model;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.oribuin.chestshops.EternalChestShops;
import xyz.oribuin.chestshops.hook.VaultProvider;
import xyz.oribuin.chestshops.manager.ConfigurationManager.Setting;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.manager.ShopManager;
import xyz.oribuin.chestshops.model.result.PurchaseResult;
import xyz.oribuin.chestshops.model.result.SellResult;
import xyz.oribuin.chestshops.util.ShopUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Shop {

    private final @NotNull UUID owner; // UUID of the shop owner
    private final @NotNull Location location; // Location of the shop
    private final @NotNull ItemStack item; // Item being sold or bought
    private double price; // Price of the item
    private @NotNull ShopType type;  // Type of shop, buying or selling
    private @NotNull OfflinePlayer offlineOwner; // Name of the shop owner, used for display purposes
    private @Nullable Sign attached; // The sign attached to the shop

    public Shop(@NotNull UUID owner, @NotNull Location location, @NotNull ItemStack item, double price) {
        this.owner = owner;
        this.location = location;
        this.item = item;
        this.price = Math.max(price, 0);
        this.type = ShopType.SELLING;
        this.offlineOwner = Bukkit.getOfflinePlayer(owner);
        this.attached = this.getAttached();
    }

    /**
     * Buy items from the shop
     *
     * @param who    The player buying the items
     * @param amount The amount of items to buy
     * @return If the purchase was successful
     */
    public PurchaseResult buy(Player who, int amount) {
        if (this.type != ShopType.SELLING || !(this.location.getBlock().getState() instanceof Container container) || price <= 0)
            return PurchaseResult.INVALID_SHOP;

        int totalItems = Math.min(this.getStock(), amount);
        int totalCost = (int) (this.price * totalItems);
        int stock = this.getStock();

        // Check if the shop has enough items to sell
        if (totalItems == 0 || stock < amount)
            return PurchaseResult.NOT_ENOUGH_ITEMS;

        // Check if the player has enough space to purchase the items
        int playerSpace = ShopUtils.getSpareSlotsForItem(who.getInventory(), this.item);
        if (playerSpace < totalItems)
            return PurchaseResult.NOT_ENOUGH_SPACE;

        // Check if the player has enough money to purchase the items
        VaultProvider provider = VaultProvider.getInstance();
        if (!provider.has(who, totalCost))
            return PurchaseResult.NOT_ENOUGH_MONEY;

        // Take the money from the player and give it to the shop owner
        provider.take(who, totalCost);
        provider.give(this.offlineOwner, totalCost);

        // Take each item in individual stacks and add them to the player's inventory
        int totalStacks = (int) Math.ceil(totalItems / 64D);
        List<ItemStack> stacks = new ArrayList<>();

        for (int i = 0; i < totalStacks; i++) {
            int size = Math.min(totalItems, 64);
            ItemStack stack = this.item.clone();
            stack.setAmount(size);
            stacks.add(stack);
            totalItems -= size;
        }

        // Add the items to the player's inventory and remove them from the shop
        ItemStack[] toTransfer = stacks.toArray(new ItemStack[0]);
        who.getInventory().addItem(toTransfer);
        container.getInventory().removeItem(toTransfer);

        // Update the shop data
        this.update();
        return PurchaseResult.SUCCESS;
    }

    /**
     * Sell items to the shop
     *
     * @param who    The player selling the items
     * @param amount The amount of items to sell
     * @return If the sale was successful
     */
    public SellResult sell(Player who, int amount) {
        if (this.type != ShopType.BUYING || !(this.location.getBlock().getState() instanceof Container container) || price <= 0)
            return SellResult.INVALID_SHOP;

        int itemsToSell = Math.min(ShopUtils.getAmountOfItem(who.getInventory(), this.item), amount);
        int totalCost = (int) (this.price * itemsToSell);

        if (itemsToSell <= 0)
            return SellResult.NOT_ENOUGH_ITEMS; // Player does not have enough items to sell

        int space = ShopUtils.getSpareSlotsForItem(container.getInventory(), this.item);
        if (space < itemsToSell)
            return SellResult.NOT_ENOUGH_SPACE; // Shop does not have enough space to purchase the item

        VaultProvider provider = VaultProvider.getInstance();
        if (!provider.has(this.offlineOwner, totalCost))
            return SellResult.NOT_ENOUGH_MONEY;

        provider.take(this.offlineOwner, totalCost);
        provider.give(who, totalCost);

        // Take each item in individual stacks and add them to the player's inventory
        int totalStacks = (int) Math.ceil(itemsToSell / 64D);
        List<ItemStack> stacks = new ArrayList<>();

        for (int i = 0; i < totalStacks; i++) {
            int size = Math.min(itemsToSell, 64);
            ItemStack stack = this.item.clone();
            stack.setAmount(size);
            stacks.add(stack);
            itemsToSell -= size;
        }

        // Add the items to the player's inventory and remove them from the shop
        ItemStack[] toTransfer = stacks.toArray(new ItemStack[0]);
        who.getInventory().removeItem(toTransfer);
        container.getInventory().addItem(toTransfer);

        // Update the shop data
        this.update();
        return SellResult.SUCCESS;
    }

    /**
     * Create the shop in the set location.
     *
     * @param who The player creating the shop
     * @return If the shop was created successfully
     */
    public boolean create(Player who) {
        if (!(this.location.getBlock().getState() instanceof Container container))
            return false;

        // get player direction as block face
        BlockFace face = ShopUtils.getEmptyFace(container.getBlock(), who.getFacing().getOppositeFace());
        if (face == BlockFace.SELF)
            return false;

        Block signBlock = this.location.getBlock().getRelative(face);
        Material signType = ShopUtils.getEnum(Material.class, Setting.SIGN_SETTINGS_MATERIAL.getString());
        if (signType == null || !Tag.WALL_SIGNS.isTagged(signType))
            return false;

        signBlock.setType(signType);
        Sign sign = (Sign) signBlock.getState();
        WallSign wallSign = (WallSign) signBlock.getBlockData();

        wallSign.setFacing(face);
        sign.setBlockData(wallSign);

        PersistentDataContainer signContainer = sign.getPersistentDataContainer();
        signContainer.set(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING, this.owner.toString());
        signContainer.set(ShopDataKeys.SHOP_SIGN, PersistentDataType.INTEGER, 1);
        sign.setWaxed(true);
        sign.update();

        // Update the shop data
        this.update();
        return true;
    }

    /**
     * Update the shop data in the container
     */
    @SuppressWarnings("deprecation")
    public void update() {
        if (!(this.location.getBlock().getState() instanceof Container container))
            return;

        PersistentDataContainer data = container.getPersistentDataContainer();
        data.set(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING, this.owner.toString());
        data.set(ShopDataKeys.SHOP_TYPE, PersistentDataType.STRING, this.type.name());
        data.set(ShopDataKeys.SHOP_ITEM, PersistentDataType.BYTE_ARRAY, ShopUtils.serializeItem(this.item));
        data.set(ShopDataKeys.SHOP_PRICE, PersistentDataType.DOUBLE, this.price);
        data.set(ShopDataKeys.SHOP_OWNER_NAME, PersistentDataType.STRING, Objects.requireNonNullElse(this.offlineOwner.getName(), "Unknown"));

        container.update();

        // Get connecting sign and update it
        Sign attachedSign = this.getAttached();
        if (attachedSign != null) {
            List<String> lines = new ArrayList<>(this.type == ShopType.SELLING
                    ? Setting.SIGN_TEXT_SETTINGS_SELLING.getStringList()
                    : Setting.SIGN_TEXT_SETTINGS_BUYING.getStringList()
            );

            LocaleManager locale = EternalChestShops.getInstance().getManager(LocaleManager.class);
            lines = lines.stream()
                    .map(s -> locale.format(null, s, this.getPlaceholders()))
                    .collect(Collectors.toList());

            for (int i = 0; i < lines.size(); i++)
                attachedSign.setLine(i, lines.get(i));

            attachedSign.update();
        }

        // Update the shop in the cache
        EternalChestShops.getInstance().getManager(ShopManager.class).getCachedShop().put(this.location, this);

    }

    public void remove() {
        if (!(this.location.getBlock().getState() instanceof Container container))
            return;

        System.out.println("Removing shop at " + this.location);

        PersistentDataContainer data = container.getPersistentDataContainer();
        data.remove(ShopDataKeys.SHOP_OWNER);
        data.remove(ShopDataKeys.SHOP_TYPE);
        data.remove(ShopDataKeys.SHOP_ITEM);
        data.remove(ShopDataKeys.SHOP_PRICE);
        data.remove(ShopDataKeys.SHOP_OWNER_NAME);
        container.update();

        // Remove the sign attached to the shop
        Sign attached = this.getAttached();
        if (attached != null) {
            attached.getBlock().setType(Material.AIR);
            this.attached = null;
        }

        EternalChestShops.getInstance().getManager(ShopManager.class).getCachedShop().remove(this.location);
    }

    @Nullable
    public Sign getAttached() {
        if (this.attached != null)
            return this.attached;

        List<BlockFace> allowed = List.of(BlockFace.NORTH,
                BlockFace.EAST,
                BlockFace.SOUTH,
                BlockFace.WEST
        );

        // TODO: Improve this system for finding the sign attached to the shop
        this.attached = allowed.stream()
                .map(f -> this.location.getBlock().getRelative(f))
                .filter(b ->
                        b.getState() instanceof Sign sign
                                && sign.getPersistentDataContainer().has(ShopDataKeys.SHOP_SIGN, PersistentDataType.INTEGER)
                                && sign.getPersistentDataContainer().has(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING)
                                && Objects.equals(sign.getPersistentDataContainer().get(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING), this.owner.toString())
                )
                .map(b -> (Sign) b.getState())
                .findFirst()
                .orElse(null);

        return this.attached;
    }

    /**
     * Get the amount of items that can be bought from the shop
     *
     * @return The stock total
     */
    public int getStock() {
        if (!(this.location.getBlock().getState() instanceof Container container))
            return 0;

        return ShopUtils.getAmountOfItem(container.getInventory(), this.item);
    }

    /**
     * Get the amount of items that can be sold to the shop
     *
     * @return The stock total
     */
    public int getSpace() {
        if (!(this.location.getBlock().getState() instanceof Container container))
            return 0;

        return ShopUtils.getSpareSlotsForItem(container.getInventory(), this.item);
    }

    /**
     * Get the placeholders for the shop
     *
     * @return The placeholders
     */
    public StringPlaceholders getPlaceholders() {
        return StringPlaceholders.builder()
                .add("owner", Objects.requireNonNullElse(this.offlineOwner.getName(), "Unknown"))
                .add("price", this.price) // TODO: Format price
                .add("item", ShopUtils.getItemName(this.item))
                .add("type", this.type)
                .add("stock", this.getStock())
                .add("space", this.getSpace())
                .build();
    }

    public @NotNull UUID getOwner() {
        return owner;
    }

    public @NotNull Location getLocation() {
        return location;
    }

    public @NotNull ItemStack getItem() {
        return item;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public @NotNull ShopType getType() {
        return type;
    }

    public void setType(@NotNull ShopType type) {
        this.type = type;
    }

    public @NotNull OfflinePlayer getOfflineOwner() {
        return offlineOwner;
    }

    public void setOfflineOwner(@NotNull OfflinePlayer offlineOwner) {
        this.offlineOwner = offlineOwner;
    }
}

