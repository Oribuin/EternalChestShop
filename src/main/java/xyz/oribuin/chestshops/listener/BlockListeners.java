package xyz.oribuin.chestshops.listener;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.oribuin.chestshops.EternalChestShops;
import xyz.oribuin.chestshops.manager.LocaleManager;
import xyz.oribuin.chestshops.manager.ShopManager;
import xyz.oribuin.chestshops.model.Shop;
import xyz.oribuin.chestshops.model.ShopType;

@SuppressWarnings("deprecation")
public class BlockListeners implements Listener {

    private final EternalChestShops plugin;
    private final LocaleManager locale;
    private final ShopManager manager;

    public BlockListeners(EternalChestShops plugin) {
        this.plugin = plugin;
        this.locale = this.plugin.getManager(LocaleManager.class);
        this.manager = this.plugin.getManager(ShopManager.class);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onShopBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Container container))
            return;

        Shop shop = this.manager.getShop(container);
        if (shop == null) return;

        event.setCancelled(true);

        if (!event.getPlayer().getUniqueId().equals(shop.getOwner()) && !this.manager.isBypassing(event.getPlayer().getUniqueId())) {
            this.locale.sendMessage(event.getPlayer(), "command-remove-not-owner");
            return;
        }

        // Require sneaking to remove shop
        if (!event.getPlayer().isSneaking()) return;

        shop.remove();
        this.locale.sendMessage(event.getPlayer(), "command-remove-success");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onSignBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Sign sign))
            return;

        ShopManager manager = this.manager;
        Shop shop = manager.getShop(sign);
        if (shop == null) return;

        // no break if not owner
        if (!event.getPlayer().getUniqueId().equals(shop.getOwner()) || !event.getPlayer().isSneaking() && !manager.isBypassing(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // Cancel the event.
        event.setCancelled(true);

        // Remove the shop
        shop.remove();
        this.locale.sendMessage(event.getPlayer(), "command-remove-success");
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!(event.getClickedBlock().getState() instanceof Sign sign)) return;

        ShopManager manager = this.manager;
        Shop shop = manager.getShop(sign);
        if (shop == null) return;

        // Update the shop
        shop.update();
        event.setCancelled(true);
    }

    @EventHandler
    public void onBuy(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        ShopManager manager = this.manager;
        Shop shop = manager.getShop(block);
        if (shop == null) return;

        // Update the shop
        shop.update();

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        event.setCancelled(true);

        // Owner should be able to open the shop
        if (event.getPlayer().getUniqueId().equals(shop.getOwner()) && event.getPlayer().isSneaking() && !manager.isBypassing(event.getPlayer().getUniqueId())) {
            event.setCancelled(false);
            return;
        }

        if (shop.getType() == ShopType.BUYING && shop.getSpace() <= 0) {
            this.locale.sendMessage(event.getPlayer(), "command-sell-full", shop.getPlaceholders());
            return;
        }

        if (shop.getType() == ShopType.SELLING && shop.getStock() <= 0) {
            this.locale.sendMessage(event.getPlayer(), "command-buy-empty", shop.getPlaceholders());
            return;
        }

        Player player = event.getPlayer();

        manager.getAwaitingResponse().put(player.getUniqueId(), shop);

        switch (shop.getType()) {
            case SELLING -> this.locale.sendMessage(player, "command-buy-input", shop.getPlaceholders());
            case BUYING -> this.locale.sendMessage(player, "command-sell-input", shop.getPlaceholders());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        ShopManager manager = this.manager;
        Shop shop = manager.getAwaitingResponse().getIfPresent(event.getPlayer().getUniqueId());
        if (shop == null) return;

        event.setCancelled(true);

        int amount;
        try {
            amount = Integer.parseInt(event.getMessage());
        } catch (NumberFormatException ignored) {
            this.locale.sendMessage(event.getPlayer(), "command-buy-invalid-number");
            manager.getAwaitingResponse().invalidate(event.getPlayer().getUniqueId());
            return;
        }

        if (amount < 1) {
            this.locale.sendMessage(event.getPlayer(), "command-buy-invalid-number");
            manager.getAwaitingResponse().invalidate(event.getPlayer().getUniqueId());
            return;
        }

        // Has to be done synchronously :( Bukkit API moment
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (shop.getType() == ShopType.SELLING) {
                shop.buy(event.getPlayer(), amount);
            }

            if (shop.getType() == ShopType.BUYING) {
                shop.sell(event.getPlayer(), amount);
            }
        });
    }

}
