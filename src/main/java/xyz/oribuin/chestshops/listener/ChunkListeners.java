package xyz.oribuin.chestshops.listener;

import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import xyz.oribuin.chestshops.EternalChestShops;
import xyz.oribuin.chestshops.manager.ShopManager;
import xyz.oribuin.chestshops.model.Shop;

public class ChunkListeners implements Listener {

    private final EternalChestShops plugin;

    public ChunkListeners(EternalChestShops plugin) {
        this.plugin = plugin;
    }

    /**
     * Load the shop from the cache when the chunk is loaded
     *
     * @param event The ChunkLoadEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLoad(ChunkLoadEvent event) {
        for (BlockState state : event.getChunk().getTileEntities()) { // TODO: paper getTileEntities(true)
            if (!(state instanceof Container container)) continue;

            Shop shop = this.plugin.getManager(ShopManager.class).getShop(container);
            if (shop != null) {
                shop.updateSign();
            }
        }
    }

    /**
     * Unload the shop from the cache when the chunk is unloaded
     *
     * @param event The ChunkUnloadEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onUnload(ChunkUnloadEvent event) {
        ShopManager manager = this.plugin.getManager(ShopManager.class);
        manager.getCachedShop().values().removeIf(shop -> {
            Chunk chunk = shop.getLocation().getChunk();
            return chunk.getX() == event.getChunk().getX() && chunk.getZ() == event.getChunk().getZ();
        });
    }

}
