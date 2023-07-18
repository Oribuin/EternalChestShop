package xyz.oribuin.chestshops.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import org.bukkit.entity.Player;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.manager.ShopManager;

public class BypassCommand extends RoseCommand {

    public BypassCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        if (!(context.getSender() instanceof Player player))
            return;

        ShopManager shopManager = this.rosePlugin.getManager(ShopManager.class);

        boolean bypass = shopManager.toggleBypassing(player.getUniqueId());
        this.rosePlugin.getManager(LocaleManager.class).sendMessage(player,
                bypass
                        ? "command-bypass-enabled"
                        : "command-bypass-disabled"
        );

    }

    @Override
    protected String getDefaultName() {
        return "bypass";
    }

    @Override
    public String getDescriptionKey() {
        return "command-bypass-description";
    }

    @Override
    public String getRequiredPermission() {
        return "eternalchestshops.bypass";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

}
