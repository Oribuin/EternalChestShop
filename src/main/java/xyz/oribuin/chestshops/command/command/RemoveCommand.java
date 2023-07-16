package xyz.oribuin.chestshops.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.manager.ShopManager;
import xyz.oribuin.chestshops.model.Shop;

public class RemoveCommand extends RoseCommand {

    public RemoveCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context) {
        if (!(context.getSender() instanceof Player player))
            return;

        LocaleManager locale = this.rosePlugin.getManager(LocaleManager.class);

        Block target = player.getTargetBlockExact(5);
        if (target == null) {
            locale.sendMessage(player, "command-remove-invalid-shop");
            return;
        }


        Shop shop = this.rosePlugin.getManager(ShopManager.class).getShop(target);

        if (shop == null) {
            locale.sendMessage(player, "command-remove-invalid-shop");
            return;
        }

        // TODO: Implement bypass system
        if (!shop.getOwner().equals(player.getUniqueId())) {
            locale.sendMessage(player, "command-remove-not-owner");
            return;
        }

        shop.remove();
        locale.sendMessage(player, "command-remove-success", shop.getPlaceholders());
    }


    @Override
    protected String getDefaultName() {
        return "remove";
    }

    @Override
    public String getDescriptionKey() {
        return "command-remove-description";
    }

    @Override
    public String getRequiredPermission() {
        return "eternalchestshops.remove";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

}
