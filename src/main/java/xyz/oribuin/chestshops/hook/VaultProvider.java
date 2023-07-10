package xyz.oribuin.chestshops.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class VaultProvider {

    private static VaultProvider instance;
    private static boolean enabled = false;
    private @Nullable Economy economy;

    public VaultProvider() {
        instance = this;

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            enabled = true;
            this.economy = Bukkit.getServicesManager().getRegistration(Economy.class).getProvider();
        }

    }

    public static VaultProvider getInstance() {
        return instance;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Take money from a player
     *
     * @param player The player
     * @param amount The amount
     * @return If the transaction was successful
     */
    public boolean take(OfflinePlayer player, double amount) {
        if (this.economy == null)
            return false;

        return this.economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    /**
     * Give a player money
     *
     * @param player The player
     * @param amount The amount
     * @return If the transaction was successful
     */
    public boolean give(OfflinePlayer player, double amount) {
        if (this.economy == null)
            return false;

        return this.economy.depositPlayer(player, amount).transactionSuccess();
    }



    /**
     * Get the balance of a player
     *
     * @param player The player
     * @return The balance
     */
    public double balance(Player player) {
        if (this.economy == null)
            return 0;

        return this.economy.getBalance(player);
    }

    /**
     * Check if a player has a certain amount of money
     *
     * @param player The player
     * @param amount The amount
     * @return If the player has the amount
     */
    public boolean has(OfflinePlayer player, double amount) {
        if (this.economy == null)
            return false;

        return this.economy.has(player, amount);
    }

}
