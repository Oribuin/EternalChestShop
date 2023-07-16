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

public class SellCommand extends RoseCommand {

    public SellCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, int amount) {
        if (!(context.getSender() instanceof Player player))
            return;

        LocaleManager locale = this.rosePlugin.getManager(LocaleManager.class);

        Block target = player.getTargetBlockExact(5);
        if (target == null) {
            locale.sendMessage(player, "command-sell-invalid-block");
            return;
        }

        Shop shop = this.rosePlugin.getManager(ShopManager.class).getShop(target);

        if (amount < 1) {
            locale.sendMessage(player, "command-sell-invalid-amount");
            return;
        }

        if (shop == null) {
            locale.sendMessage(player, "command-sell-invalid-shop");
            return;
        }

        if (shop.getOwner().equals(player.getUniqueId())) {
            locale.sendMessage(player, "command-sell-is-owner");
            return;
        }

        shop.sell(player, amount);
    }

    @Override
    protected String getDefaultName() {
        return "sell";
    }

    @Override
    public String getDescriptionKey() {
        return "command-sell-description";
    }

    @Override
    public String getRequiredPermission() {
        return "eternalchestshops.sell";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

}
