package xyz.oribuin.chestshops.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.model.Shop;

import java.util.ArrayList;
import java.util.List;

public class CreateCommand extends RoseCommand {

    public CreateCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, Double price) {
        if (!(context.getSender() instanceof Player player))
            return;

        ItemStack item = player.getInventory().getItemInMainHand().clone();
        if (item.getType().isAir() || item.getAmount() == 0) {
            this.rosePlugin.getManager(LocaleManager.class).sendMessage(player, "command-create-invalid-item");
            return;
        }

        item.setAmount(1);

        Block target = player.getTargetBlockExact(5);
        if (target == null || !(target.getState() instanceof Container container)) {
            this.rosePlugin.getManager(LocaleManager.class).sendMessage(player, "command-create-invalid-block");
            return;
        }

        Shop shop = new Shop(player.getUniqueId(), container.getLocation(), item, price);

        if (shop.create(player)) {
            List<String> message = List.of(
                    "You have successfully created a shop.",
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
        return "create";
    }

    @Override
    public String getDescriptionKey() {
        return "command-create-description";
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
