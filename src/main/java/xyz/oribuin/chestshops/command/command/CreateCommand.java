package xyz.oribuin.chestshops.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.manager.ShopManager;
import xyz.oribuin.chestshops.model.Shop;
import xyz.oribuin.chestshops.model.ShopType;

public class CreateCommand extends RoseCommand {

    public CreateCommand(RosePlugin rosePlugin, RoseCommandWrapper parent) {
        super(rosePlugin, parent);
    }

    @RoseExecutable
    public void execute(CommandContext context, ShopType type, Double price) {
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

        // Modify the existing prices if the shop already exists
        Shop shop = this.rosePlugin.getManager(ShopManager.class).getShop(target);
        if (shop != null) {

            switch (type) {
                case BUYING -> shop.setBuyPrice(price);
                case SELLING -> shop.setSellPrice(price);
            }

            shop.update();
            locale.sendMessage(player, "command-create-modified", shop.getPlaceholders());
            return;
        }

        Block signBlock = target.getRelative(player.getFacing().getOppositeFace());
        if (signBlock.getType() != Material.AIR) {
            locale.sendMessage(player, "command-create-invalid-sign");
            return;
        }

        // Create a brand new shop here if the shop is null
        item.setAmount(1);
        shop = new Shop(player.getUniqueId(), container.getLocation(), item);
        shop.setSignDirection(player.getFacing().getOppositeFace());
        shop.setOfflineOwner(player);
        switch (type) {
            case BUYING -> shop.setBuyPrice(price);
            case SELLING -> shop.setSellPrice(price);
        }

        if (shop.getBuyPrice() == 0 && shop.getSellPrice() == 0) {
            locale.sendMessage(player, "command-create-invalid-price");
            return;
        }

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
