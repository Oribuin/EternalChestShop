package xyz.oribuin.chestshops.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Attachable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.oribuin.chestshops.model.Shop;
import xyz.oribuin.chestshops.model.ShopDataKeys;
import xyz.oribuin.chestshops.model.ShopType;
import xyz.oribuin.chestshops.util.ShopUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ShopManager extends Manager {

    // Store who is buying what from where
    private final Cache<UUID, Shop> awaitingResponse = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build();

    public ShopManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    public void reload() {

    }

    /**
     * Get a shop from a block
     *
     * @param block The block to get the shop from
     * @return The shop
     */
    public Shop getShop(Block block) {
        Container container = null;

        // Check if the block is a container
        if (block.getState() instanceof Container casted)
            container = casted;

        // Check if the block is attached to a container
        if (block.getState() instanceof Attachable attachable && container == null) {
            Block relative = block.getRelative(attachable.getAttachedFace());
            if (relative.getState() instanceof Container casted)
                container = casted;
        }

        if (container == null)
            return null;

        return this.getShop(container);
    }

    /**
     * Get a shop from a container
     *
     * @param container The container to get the shop from
     * @return The shop
     */
    public Shop getShop(Container container) {
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

        return shop;
    }

    // TODO: Get shop via sign.

    /**
     * Check if a block is a shop
     *
     * @param block The block to check
     * @return If the block is a shop
     */
    public boolean isShop(Block block) {
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

    public Cache<UUID, Shop> getAwaitingResponse() {
        return this.awaitingResponse;
    }

}
