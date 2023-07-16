package xyz.oribuin.chestshops;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.Bukkit;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import xyz.oribuin.chestshops.listener.BlockListeners;
import xyz.oribuin.chestshops.manager.CommandManager;
import xyz.oribuin.chestshops.manager.ConfigurationManager;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.manager.ShopManager;
import xyz.oribuin.chestshops.model.ShopDataKeys;

import java.util.List;

public class EternalChestShops extends RosePlugin {

    private static EternalChestShops instance;

    public EternalChestShops() {
        super(-1, -1, ConfigurationManager.class, null, LocaleManager.class, CommandManager.class);

        instance = this;
    }

    public static EternalChestShops getInstance() {
        return instance;
    }

    @Override
    protected void enable() {

        // Register Listeners
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new BlockListeners(this), this);

    }

    @Override
    protected void disable() {
        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(ItemDisplay.class).forEach(itemDisplay -> {
            if (itemDisplay.getPersistentDataContainer().has(ShopDataKeys.SHOP_DISPLAY_ENTITY, PersistentDataType.INTEGER))
                itemDisplay.remove();
        }));
    }


    @Override
    public void reload() {
        super.reload();

        Bukkit.getWorlds().forEach(world -> world.getEntitiesByClass(ItemDisplay.class).forEach(itemDisplay -> {
            if (itemDisplay.getPersistentDataContainer().has(ShopDataKeys.SHOP_DISPLAY_ENTITY, PersistentDataType.INTEGER))
                itemDisplay.remove();
        }));
    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return List.of(ShopManager.class);
    }

}
