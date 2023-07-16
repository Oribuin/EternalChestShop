package xyz.oribuin.chestshops.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.oribuin.chestshops.EternalChestShops;
import xyz.oribuin.chestshops.manager.ShopManager;
import xyz.oribuin.chestshops.model.Shop;
import xyz.oribuin.chestshops.model.ShopType;
import xyz.oribuin.chestshops.model.result.PurchaseResult;
import xyz.oribuin.chestshops.model.result.SellResult;

@SuppressWarnings("deprecation")
public class BlockListeners implements Listener {

    private final EternalChestShops plugin;

    public BlockListeners(EternalChestShops plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent event) {
        if (!(event.getBlock().getState() instanceof Chest container))
            return;

        ShopManager manager = this.plugin.getManager(ShopManager.class);
        Shop shop = manager.getShop((Container) container);
        if (shop == null) return;


        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onShopBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Container container))
            return;

        Shop shop = this.plugin.getManager(ShopManager.class).getShop(container);
        if (shop == null) return;

        event.setCancelled(true);

        if (!event.getPlayer().getUniqueId().equals(shop.getOwner()) || !event.getPlayer().isSneaking()) {
            return;
        }

        shop.remove();

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Sign sign))
            return;

        ShopManager manager = this.plugin.getManager(ShopManager.class);
        Shop shop = manager.getShop(sign);
        if (shop == null) return;

        // no break if not owner
        if (!event.getPlayer().getUniqueId().equals(shop.getOwner()) || !event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            return;
        }

        // Cancel the event.
        event.setCancelled(true);

        // Remove the shop
        shop.remove();

        event.getPlayer().sendMessage(Component.text("You have removed the shop."));
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getClickedBlock().getState() instanceof Sign sign)) return;

        ShopManager manager = this.plugin.getManager(ShopManager.class);
        Shop shop = manager.getShop(sign);
        if (shop == null) return;

        // Update the shop
        shop.update();
        event.setCancelled(true);

        Bukkit.getServer().dispatchCommand(event.getPlayer(), "/cshops stats");

        // TODO: Display stats about the shop
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBuy(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof Container container)) return;
        if (block == null) return;

        ShopManager manager = this.plugin.getManager(ShopManager.class);
        Shop shop = manager.getShop(container);
        if (shop == null) return;

        // Update the shop
        shop.update();

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        event.setCancelled(true);

        // TODO: Switch to locale

        // Owner should be able to open the shop
        if (event.getPlayer().getUniqueId().equals(shop.getOwner()) && event.getPlayer().isSneaking()) {
            event.setCancelled(false);
            return;
        }

        if (shop.getType() == ShopType.BUYING && shop.getSpace() <= 0) {
            event.getPlayer().sendMessage(Component.text("The shop is full!"));
            return;
        }

        if (shop.getType() == ShopType.SELLING && shop.getStock() <= 0) {
            event.getPlayer().sendMessage(Component.text("The shop is out of stock!"));
            return;
        }

        // TODO: Display stats for the shop

        Player player = event.getPlayer();
        player.sendMessage(Component.text("Type the number of items you want to buy."));
        manager.getAwaitingResponse().put(player.getUniqueId(), shop);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        ShopManager manager = this.plugin.getManager(ShopManager.class);

        Shop shop = manager.getAwaitingResponse().getIfPresent(event.getPlayer().getUniqueId());
        if (shop == null) return;

        event.setCancelled(true);

        int amount;
        try {
            amount = Integer.parseInt(event.getMessage());
        } catch (NumberFormatException ignored) {

            // TODO: Invalid amount
            manager.getAwaitingResponse().invalidate(event.getPlayer().getUniqueId());
            return;
        }

        if (amount < 1) {
            // TODO: Invalid amount
            return;
        }

        // Has to be done synchronously :( Bukkit API moment
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (shop.getType() == ShopType.SELLING && shop.buy(event.getPlayer(), amount) == PurchaseResult.SUCCESS) {
                event.getPlayer().sendMessage(Component.text("You bought " + amount + " items to the shop!"));
            }

            if (shop.getType() == ShopType.BUYING && shop.sell(event.getPlayer(), amount) == SellResult.SUCCESS) {
                event.getPlayer().sendMessage(Component.text("You selling " + amount + " items from the shop!"));
            }
        });


    }

}
