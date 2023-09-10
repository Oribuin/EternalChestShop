package xyz.oribuin.chestshops.model;

import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
    private BlockFace signDirection; // The direction the sign is facing

    public Shop(@NotNull UUID owner, @NotNull Location location, @NotNull ItemStack item, double price) {
        this.owner = owner;
        this.location = location;
        this.item = item;
        this.price = Math.max(price, 0);
        this.type = ShopType.SELLING;
        this.offlineOwner = Bukkit.getOfflinePlayer(owner);
        this.signDirection = null;
    }

    /**
     * Buy items from the shop
     *
     * @param who    The player buying the items
     * @param amount The amount of items to buy
     */
    public void buy(Player who, int amount) {
        LocaleManager locale = EternalChestShops.getInstance().getManager(LocaleManager.class);

        if (this.type != ShopType.SELLING || !(this.location.getBlock().getState() instanceof Container container) || price <= 0) {
            locale.sendMessage(who, "command-buy-invalid-shop");
            return;
        }

        int totalItems = Math.min(this.getStock(), amount);
        int totalCost = (int) (this.price * totalItems);
        int stock = this.getStock();

        // Check if the shop has enough items to sell
        if (totalItems == 0 || stock < amount) {
            locale.sendMessage(who, "command-buy-not-enough-items", StringPlaceholders.of(
                    "amount", String.valueOf(totalItems),
                    "stock", String.valueOf(stock)
            ));
            return;
        }

        // Check if the player has enough money to purchase the items
        VaultProvider provider = VaultProvider.getInstance();
        if (!provider.has(who, totalCost)) {
            locale.sendMessage(who, "command-buy-not-enough-money", StringPlaceholders.of(
                    "amount", String.valueOf(totalItems),
                    "cost", String.valueOf(totalCost)
            ));
            return;
        }

        // Check if the player has enough space to purchase the items
        int playerSpace = ShopUtils.getSpareSlotsForItem(who.getInventory(), this.item);
        if (playerSpace < totalItems) {
            locale.sendMessage(who, "command-buy-not-enough-space", StringPlaceholders.of(
                    "amount", String.valueOf(totalItems),
                    "space", String.valueOf(playerSpace)
            ));
            return;
        }

        // Take the money from the player and give it to the shop owner
        provider.take(who, totalCost);
        provider.give(this.offlineOwner, totalCost * (1 - Setting.BUYING_TAX.getDouble()));

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

        locale.sendMessage(who, "command-buy-success", StringPlaceholders.builder()
                .addAll(this.getPlaceholders())
                .add("amount", String.valueOf(totalItems))
                .add("cost", String.valueOf(totalCost))
                .build()
        );
    }

    /**
     * Sell items to the shop
     *
     * @param who    The player selling the items
     * @param amount The amount of items to sell
     */
    public void sell(Player who, int amount) {
        LocaleManager locale = EternalChestShops.getInstance().getManager(LocaleManager.class);

        if (this.type != ShopType.BUYING || !(this.location.getBlock().getState() instanceof Container container) || price <= 0) {
            locale.sendMessage(who, "command-sell-invalid-shop");
            return;
        }

        int itemsToSell = Math.min(ShopUtils.getAmountOfItem(who.getInventory(), this.item), amount);
        int totalCost = (int) (this.price * itemsToSell);

        // Player does not have enough items to sell
        if (itemsToSell <= 0) {
            locale.sendMessage(who, "command-sell-not-enough-items", StringPlaceholders.of(
                    "amount", String.valueOf(amount),
                    "stock", String.valueOf(itemsToSell)
            ));
            return;
        }

        VaultProvider provider = VaultProvider.getInstance();
        if (!provider.has(this.offlineOwner, totalCost)) {
            locale.sendMessage(who, "command-sell-not-enough-money", StringPlaceholders.of(
                    "amount", String.valueOf(amount),
                    "cost", String.valueOf(totalCost)
            ));
            return;
        }

        // Shop does not have enough space to purchase the item
        int space = ShopUtils.getSpareSlotsForItem(container.getInventory(), this.item);
        if (space < itemsToSell) {
            locale.sendMessage(who, "command-sell-not-enough-space", StringPlaceholders.of(
                    "amount", String.valueOf(amount),
                    "space", String.valueOf(space)
            ));
            return;
        }

        provider.take(this.offlineOwner, totalCost);
        provider.give(who, totalCost * (1 - Setting.SELLING_TAX.getDouble()));

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
        locale.sendMessage(who, "command-sell-success", StringPlaceholders.builder()
                .addAll(this.getPlaceholders())
                .add("amount", String.valueOf(itemsToSell))
                .add("cost", String.valueOf(totalCost))
                .build()
        );

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

        LocaleManager locale = EternalChestShops.getInstance().getManager(LocaleManager.class);

        // get player direction as block face
        BlockFace face = ShopUtils.getEmptyFace(container.getBlock(), who.getFacing().getOppositeFace());
        if (face == BlockFace.SELF) {
            locale.sendMessage(who, "command-create-no-sign-space");
            return false;
        }

        Block signBlock = this.location.getBlock().getRelative(face);
        Material signType = ShopUtils.getEnum(Material.class, Setting.SIGN_SETTINGS_MATERIAL.getString());
        if (signType == null || !Tag.WALL_SIGNS.isTagged(signType)) {
            signType = Material.OAK_WALL_SIGN;
        }

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
        data.set(ShopDataKeys.SHOP_SIGN, PersistentDataType.STRING, this.signDirection.name());

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

    /**
     * Remove the chestshop ids from the container
     */
    public void remove() {
        if (!(this.location.getBlock().getState() instanceof Container container))
            return;

        PersistentDataContainer data = container.getPersistentDataContainer();
        data.remove(ShopDataKeys.SHOP_OWNER);
        data.remove(ShopDataKeys.SHOP_TYPE);
        data.remove(ShopDataKeys.SHOP_ITEM);
        data.remove(ShopDataKeys.SHOP_PRICE);
        data.remove(ShopDataKeys.SHOP_OWNER_NAME);
        data.remove(ShopDataKeys.SHOP_SIGN);
        container.update();

        // Remove the sign attached to the shop
        Sign attached = this.getAttached();
        if (attached != null) {
            attached.getBlock().setType(Material.AIR);

            this.signDirection = null;
        }

        EternalChestShops.getInstance().getManager(ShopManager.class).getCachedShop().remove(this.location);
    }

    @Nullable
    public Sign getAttached() {
        if (this.signDirection == null)
            this.signDirection = ShopUtils.getEnum(BlockFace.class, ((Container) this.location.getBlock().getState())
                    .getPersistentDataContainer()
                    .get(ShopDataKeys.SHOP_SIGN, PersistentDataType.STRING)
            );

        // If there is no sign direction, return null and remove the shop
        if (this.signDirection == null) {
            this.remove();
            return null;
        }

        Block signBlock = this.location.getBlock().getRelative(this.signDirection);
        if (!(signBlock.getState() instanceof Sign sign))
            return null;

        Integer shopId = sign.getPersistentDataContainer().get(ShopDataKeys.SHOP_SIGN, PersistentDataType.INTEGER);
        String shopOwner = sign.getPersistentDataContainer().get(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING);

        if (shopId == null || !shopId.equals(1))
            return null;

        if (shopOwner == null || !shopOwner.equals(this.owner.toString()))
            return null;

        return sign;
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
                .add("price", this.price)
                .add("price_short", ShopUtils.formatShorthand(this.price))
                .add("item", ShopUtils.getItemName(this.item))
                .add("type", this.type.name().toLowerCase())
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

    public BlockFace getSignDirection() {
        return signDirection;
    }

    public void setSignDirection(BlockFace signDirection) {
        this.signDirection = signDirection;
    }

}

