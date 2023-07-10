package dev.rosewood.myplugin;

import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.myplugin.manager.CommandManager;
import dev.rosewood.myplugin.manager.ConfigurationManager;
import dev.rosewood.myplugin.manager.DataManager;
import dev.rosewood.myplugin.manager.LocaleManager;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;

import java.util.List;

public class MyPlugin extends RosePlugin {

    private static MyPlugin instance;
    private GuiFramework framework;

    public static MyPlugin getInstance() {
        return instance;
    }

    public MyPlugin() {
        super(-1, -1, ConfigurationManager.class, DataManager.class, LocaleManager.class, CommandManager.class);

        instance = this;
    }

    @Override
    protected void enable() {
        this.framework = GuiFramework.instantiate(this);
    }

    @Override
    protected void disable() {

    }

    @Override
    protected List<Class<? extends Manager>> getManagerLoadPriority() {
        return List.of();
    }

    public GuiFramework getFramework() {
        return framework;
    }

}
