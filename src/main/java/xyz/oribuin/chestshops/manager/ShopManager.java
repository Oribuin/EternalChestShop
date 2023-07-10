package xyz.oribuin.chestshops.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.block.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.oribuin.chestshops.model.Shop;
import xyz.oribuin.chestshops.model.ShopDataKeys;
import xyz.oribuin.chestshops.model.ShopType;
import xyz.oribuin.chestshops.util.ShopUtils;

import java.util.UUID;

public class ShopManager extends Manager {

    public ShopManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    @Override
    public void reload() {

    }

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

    @Override
    public void disable() {

    }

}
