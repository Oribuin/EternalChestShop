package xyz.oribuin.chestshops.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xyz.oribuin.chestshops.model.Shop;
import xyz.oribuin.chestshops.model.ShopDataKeys;
import xyz.oribuin.chestshops.model.ShopType;
import xyz.oribuin.chestshops.util.ShopUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ShopManager extends Manager {

    private final Map<Location, Shop> cachedShops = new HashMap<>();
    private final Cache<UUID, Shop> awaitingResponse = CacheBuilder.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();

    public ShopManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    public void reload() {
        this.cachedShops.clear();
    }

    /**
     * Get a shop from either a container or a sign.
     *
     * @param block The block to get the shop from
     * @return The shop
     */
    public Shop getShop(@NotNull Block block) {
        if (block.getState() instanceof Container container)
            return this.getShop(container);

        if (block.getState() instanceof Sign sign)
            return this.getShop(sign);

        return null;
    }

    /**
     * Get a shop from a container
     *
     * @param container The container to get the shop from
     * @return The shop
     */
    public Shop getShop(Container container) {
        if (this.cachedShops.containsKey(container.getLocation()))
            return this.cachedShops.get(container.getLocation());

        PersistentDataContainer data = container.getPersistentDataContainer();
        if (!data.has(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING))
            return null;

        // Load the shop data
        String owner = data.get(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING);
        String type = data.get(ShopDataKeys.SHOP_TYPE, PersistentDataType.STRING);
        ItemStack item = ShopUtils.deserializeItem(data.get(ShopDataKeys.SHOP_ITEM, PersistentDataType.BYTE_ARRAY));
        Double price = data.get(ShopDataKeys.SHOP_PRICE, PersistentDataType.DOUBLE);

        if (owner == null || type == null || item == null || price == null)
            return null;

        Shop shop = new Shop(UUID.fromString(owner), container.getLocation(), item, price);
        shop.setType(ShopUtils.getEnum(ShopType.class, type));

        this.cachedShops.put(container.getLocation(), shop);
        return shop;
    }

    /**
     * Get the shop attached to a sign if it exists
     *
     * @param sign The sign to get the shop from
     * @return The shop
     */
    public Shop getShop(Sign sign) {
        if (sign == null || !(sign.getBlockData() instanceof WallSign attachable)) {
            return null;
        }

        PersistentDataContainer signContainer = sign.getPersistentDataContainer();
        if (!signContainer.has(ShopDataKeys.SHOP_SIGN, PersistentDataType.INTEGER)) {
            return null;
        }

        // Get attached block
        Block block = sign.getBlock().getRelative(attachable.getFacing().getOppositeFace());
        if (!(block.getState() instanceof Container container)) {
            return null;
        }

        PersistentDataContainer containerContainer = container.getPersistentDataContainer();
        String signOwner = signContainer.get(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING);
        String containerOwner = containerContainer.get(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING);

        if (signOwner == null || !signOwner.equals(containerOwner))
            return null;

        return this.getShop(container); // Get the shop from the container
    }

    /**
     * Check if a block is a shop
     *
     * @param block The block to check
     * @return If the block is a shop
     */
    public boolean isShop(Block block) {
        if (this.cachedShops.containsKey(block.getLocation()))
            return true;

        if (!(block.getState() instanceof Container container))
            return false;

        return container.getPersistentDataContainer().has(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING);
    }

    /**
     * Get the owner of a shop
     *
     * @param block The block to check
     * @return The owner of the shop
     */
    public UUID getShopOwner(Block block) {
        if (!(block.getState() instanceof Container container))
            return null;

        PersistentDataContainer data = container.getPersistentDataContainer();
        String owner = data.get(ShopDataKeys.SHOP_OWNER, PersistentDataType.STRING);
        if (owner == null)
            return null;

        return UUID.fromString(owner);
    }

    @Override
    public void disable() {

    }

    public Map<Location, Shop> getCachedShop() {
        return cachedShops;
    }

    public Cache<UUID, Shop> getAwaitingResponse() {
        return this.awaitingResponse;
    }

}
