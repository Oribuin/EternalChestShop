package xyz.oribuin.chestshops.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.AbstractLocaleManager;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import org.bukkit.command.CommandSender;

import java.util.List;

public class LocaleManager extends AbstractLocaleManager {

    public LocaleManager(RosePlugin rosePlugin) {
        super(rosePlugin);
    }

    /**
     * Send a custom message to a CommandSender with placeholders
     *
     * @param sender       The CommandSender to send the message to
     * @param message      The message to send
     * @param placeholders The placeholders to apply to the message
     */
    public void sendCustomMessage(CommandSender sender, String message, StringPlaceholders placeholders) {
        if (message.isEmpty())
            return;

        if (placeholders == null)
            placeholders = StringPlaceholders.empty();

        this.handleMessage(sender, HexUtils.colorify(this.parsePlaceholders(sender, placeholders.apply(message))));
    }

    /**
     * Send a custom message to a CommandSender with placeholders
     *
     * @param sender       The CommandSender to send the message to
     * @param messages     The messages to send
     * @param placeholders The placeholders to apply to the messages
     */
    public void sendCustomMessage(CommandSender sender, List<String> messages, StringPlaceholders placeholders) {
        if (messages.isEmpty())
            return;

        if (placeholders == null)
            placeholders = StringPlaceholders.empty();

        for (String message : messages) {
            this.handleMessage(sender, HexUtils.colorify(this.parsePlaceholders(sender, placeholders.apply(message))));
        }
    }

}
