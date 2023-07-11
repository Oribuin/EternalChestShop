package xyz.oribuin.chestshops.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.manager.ShopManager;
import xyz.oribuin.chestshops.model.Shop;

public class BuyCommand extends RoseCommand {

    public BuyCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, int amount) {
        if (!(context.getSender() instanceof Player player))
            return;

        Block target = player.getTargetBlockExact(5);
        if (target == null || !(target.getState() instanceof Container container)) {
            this.rosePlugin.getManager(LocaleManager.class).sendMessage(player, "command-buy-invalid-block");
            return;
        }

        if (amount < 1) {
            this.rosePlugin.getManager(LocaleManager.class).sendMessage(player, "command-buy-invalid-amount");
            return;
        }

        Shop shop = this.rosePlugin.getManager(ShopManager.class).getShop(container);

        if (shop != null) {
            shop.buy(player, amount);
            player.sendMessage(Component.text("You bought " + amount + " items from the shop!"));
        }

    }

    @Override
    protected String getDefaultName() {
        return "buy";
    }

    @Override
    public String getDescriptionKey() {
        return "command-buy-description";
    }

    @Override
    public String getRequiredPermission() {
        return "eternalchestshops.buy";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

}
