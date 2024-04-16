package xyz.oribuin.chestshops.listener;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
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
import xyz.oribuin.chestshops.model.ShopResponse;
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShopBreak(BlockBreakEvent event) {
        BlockState state = event.getBlock().getState();
        Shop shop = null;

        // Check if the block is a container
        if (state instanceof Container container)
            shop = this.manager.getShop(container);

        // Check if the block is a sign
        if (state instanceof Sign sign)
            shop = this.manager.getShop(sign);

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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBuy(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getState() instanceof Sign sign)) return;

        Shop shop = this.manager.getShop(sign);
        if (shop == null) return;

        // Update the shop
        shop.update();
        event.setCancelled(true);

        if (event.getPlayer().isSneaking()) {
            locale.sendCustomMessages(event.getPlayer(), "command-stats-list", shop.getPlaceholders());
            return;
        }

        // buying = left click, selling = right click
        boolean isBuying = event.getAction() == Action.LEFT_CLICK_BLOCK;
        if (shop.getSellPrice() > 0 && shop.getSpace() <= 0 && !isBuying) {
            this.locale.sendMessage(event.getPlayer(), "command-sell-full", shop.getPlaceholders());
            return;
        }

        if (shop.getBuyPrice() > 0 && shop.getStock() <= 0 && isBuying) {
            this.locale.sendMessage(event.getPlayer(), "command-buy-empty", shop.getPlaceholders());
            return;
        }

        Player player = event.getPlayer();

        this.manager.getAwaitingResponse().put(player.getUniqueId(), new ShopResponse(shop, isBuying ? ShopType.BUYING : ShopType.SELLING));
        this.locale.sendMessage(player, isBuying ? "command-buy-input" : "command-sell-input", shop.getPlaceholders());
    }

    @EventHandler
    public void onBreak(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Container)) return; // don't care if not container, this event is specifically when interacting with shop itself

        Shop shop = this.manager.getShop(block);
        if (shop == null) return;

        // Update the shop
        shop.update();

        if (shop.getOwner().equals(event.getPlayer().getUniqueId())) return;

        // Bypassing the protection on the shop
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (this.manager.isBypassing(event.getPlayer().getUniqueId()) && !shop.getOwner().equals(event.getPlayer().getUniqueId())) {
                locale.sendMessage(event.getPlayer(), "command-bypass-used");
            }

            return;
        }

        event.setCancelled(true);
        locale.sendCustomMessages(event.getPlayer(), "command-stats-list", shop.getPlaceholders());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        ShopResponse response = this.manager.getAwaitingResponse().getIfPresent(event.getPlayer().getUniqueId());
        if (response == null) return;

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
            switch (response.type()) {
                case BUYING -> response.shop().sellToShop(event.getPlayer(), amount); //
                case SELLING -> response.shop().buyFromShop(event.getPlayer(), amount);
            }
        });
    }

}
