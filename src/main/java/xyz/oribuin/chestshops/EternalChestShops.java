package xyz.oribuin.chestshops;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import org.bukkit.plugin.PluginManager;
import xyz.oribuin.chestshops.hook.VaultProvider;
import xyz.oribuin.chestshops.listener.BlockListeners;
import xyz.oribuin.chestshops.manager.CommandManager;
import xyz.oribuin.chestshops.manager.ConfigurationManager;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.manager.ShopManager;

import java.util.List;

public class EternalChestShops extends RosePlugin {

    private static EternalChestShops instance;

    public static EternalChestShops getInstance() {
        return instance;
    }

    public EternalChestShops() {
        super(-1, -1, ConfigurationManager.class, null, LocaleManager.class, CommandManager.class);

        instance = this;
    }

    @Override
    protected void enable() {

        // Register Listeners
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new BlockListeners(this), this);

    }

    @Override
    protected void disable() {

    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return List.of(ShopManager.class);
    }

}
