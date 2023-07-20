package xyz.oribuin.chestshops.command.command;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.CommandContext;
import dev.rosewood.rosegarden.command.framework.RoseCommand;
import dev.rosewood.rosegarden.command.framework.RoseCommandWrapper;
import dev.rosewood.rosegarden.command.framework.annotation.Optional;
import dev.rosewood.rosegarden.command.framework.annotation.RoseExecutable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

        Block signBlock = target.getRelative(player.getFacing().getOppositeFace());
        if (signBlock.getType() != Material.AIR) {
            locale.sendMessage(player, "command-create-invalid-sign");
            return;
        }

        if (this.rosePlugin.getManager(ShopManager.class).isShop(target)) {
            locale.sendMessage(player, "command-create-already-shop");
            return;
        }

        item.setAmount(1);

        Shop shop = new Shop(player.getUniqueId(), container.getLocation(), item, price);
        shop.setSignDirection(player.getFacing().getOppositeFace());
        shop.setType(type == null ? ShopType.SELLING : type);
        shop.setOfflineOwner(player);

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
