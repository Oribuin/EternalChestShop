package xyz.oribuin.chestshops.listener;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.oribuin.chestshops.EternalChestShops;
import xyz.oribuin.chestshops.manager.ShopManager;
import xyz.oribuin.chestshops.model.Shop;
import xyz.oribuin.chestshops.model.ShopType;
import xyz.oribuin.chestshops.model.result.PurchaseResult;

import java.util.UUID;

@SuppressWarnings("deprecation")
public class BlockListeners implements Listener {

    private final EternalChestShops plugin;

    public BlockListeners(EternalChestShops plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onShopBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Container container))
            return;

        Shop shop = this.plugin.getManager(ShopManager.class).getShop(container);
        if (shop == null) return;

        if (!event.getPlayer().getUniqueId().equals(shop.getOwner())) {
            event.setCancelled(true);
            return;
        }

        if (!event.getPlayer().isSneaking()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBuy(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        ShopManager manager = this.plugin.getManager(ShopManager.class);
        Shop shop = manager.getShop(block);
        if (shop == null) return;

        event.setCancelled(true);

        switch (event.getAction()) {
            case LEFT_CLICK_BLOCK -> {
                // TODO: Switch to locale message
                if (shop.getType() == ShopType.BUYING && shop.getSpace() <= 0) {
                    event.getPlayer().sendMessage(Component.text("The shop is full!"));
                    return;
                }

                if (shop.getType() == ShopType.SELLING && shop.getStock() <= 0) {
                    event.getPlayer().sendMessage(Component.text("The shop is out of stock!"));
                    return;
                }

                Player player = event.getPlayer();
                player.sendMessage(Component.text("Type the number of items you want to buy."));
                manager.getAwaitingResponse().put(player.getUniqueId(), shop);
            }


            case RIGHT_CLICK_BLOCK -> {
                // TODO: Show stats of the shop

                // Owner should be able to open the shop
                if (event.getPlayer().getUniqueId().equals(shop.getOwner()) && !event.getPlayer().isSneaking()) {
                    event.setCancelled(false);
                    return;
                }
                event.getPlayer().chat("/cshops stats");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void getStats(PlayerInteractEvent event) {

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
            if (shop.buy(event.getPlayer(), amount) == PurchaseResult.SUCCESS) {
                event.getPlayer().sendMessage(Component.text("You bought " + amount + " items from the shop!"));
            }
        });


    }

}
