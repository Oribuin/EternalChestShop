package xyz.oribuin.chestshops.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.Optional;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.model.Shop;
import xyz.oribuin.chestshops.model.ShopType;

public class CreateCommand extends RoseCommand {

    public CreateCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, Double price, @Optional ShopType type) {
        if (!(context.getSender() instanceof Player player))
            return;

        final LocaleManager locale = this.rosePlugin.getManager(LocaleManager.class);

        ItemStack item = player.getInventory().getItemInMainHand().clone();
        if (item.getType().isAir() || item.getAmount() == 0) {
            locale.sendMessage(player, "command-create-invalid-item");
            return;
        }


        Block target = player.getTargetBlockExact(5);
        if (target == null || !(target.getState() instanceof Container container)) {
            locale.sendMessage(player, "command-create-invalid-block");
            return;
        }

        item.setAmount(1);
        Shop shop = new Shop(player.getUniqueId(), container.getLocation(), item, price);

        if (type != null)
            shop.setType(type);

        if (shop.create(player)) {
            locale.sendMessage(player, "command-create-success", shop.getPlaceholders());
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
