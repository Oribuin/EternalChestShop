package xyz.oribuin.chestshops;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
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

    }

    @Override
    protected void disable() {

    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return List.of(ShopManager.class);
    }

}
