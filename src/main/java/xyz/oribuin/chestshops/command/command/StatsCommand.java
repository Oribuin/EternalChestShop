package xyz.oribuin.chestshops.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.manager.ShopManager;
import xyz.oribuin.chestshops.model.Shop;

import java.util.List;

public class StatsCommand extends RoseCommand {

    public StatsCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        if (!(context.getSender() instanceof Player player))
            return;

        Block target = player.getTargetBlockExact(5);
        if (target == null) {
            this.rosePlugin.getManager(LocaleManager.class).sendMessage(player, "command-stats-invalid-block");
            return;
        }

        Shop shop = this.rosePlugin.getManager(ShopManager.class).getShop(target);
        if (shop != null) {
            List<String> message = List.of(
                    "Stats for the shop:",
                    "- Owner: %owner%",
                    "- Price: %price%",
                    "- Item: %item%",
                    "- Type: %type%",
                    "- Stock: %stock%"
            );

            this.rosePlugin.getManager(LocaleManager.class).sendCustomMessage(player, message, shop.getPlaceholders());
        }

    }

    @Override
    protected String getDefaultName() {
        return "stats";
    }

    @Override
    public String getDescriptionKey() {
        return "command-stats-description";
    }

    @Override
    public String getRequiredPermission() {
        return "eternalchestshops.create";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

}
