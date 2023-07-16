package xyz.oribuin.chestshops.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.manager.AbstractConfigurationManager;
import org.bukkit.Material;
import xyz.oribuin.chestshops.EternalChestShops;

import java.util.List;

public class ConfigurationManager extends AbstractConfigurationManager {

    public ConfigurationManager(RosePlugin rosePlugin) {
        super(rosePlugin, Setting.class);
    }

    @Override
    protected String[] getHeader() {
        return new String[]{};
    }

    public enum Setting implements RoseSetting {

        SIGN_SETTINGS("sign-settings", null, "Modify the text for the signs here."),
        SIGN_SETTINGS_MATERIAL("sign-settings.material", Material.OAK_WALL_SIGN, "The material for the signs.", "This sign must be a wall sign."),
        SIGN_TEXT_SETTINGS_BUYING("sign-settings.buying", List.of(
                "&f%owner%",
                "&f%item%",
                "&#00B4DB&lBuying: &f%space%",
                "&f$#00B4DB&l%price_short% &fEach"
        ), "The text for the buying sign."),
        SIGN_TEXT_SETTINGS_SELLING("sign-settings.selling", List.of(
                "&f%owner%",
                "&f%item%",
                "&#00B4DB&lSelling: &f%stock%",
                "&f$#00B4DB&l%price_short% &fEach"
        ), "The text for the selling sign."),

        // Display Entities
        DISPLAY_ITEMS("display-items", null, "Display the shop item above the shop.", "Requires 1.20+, Uses display items."),
        DISPLAY_ITEMS_ENABLED("display-items.enabled", true, "Enable or disable display items."),
        DISPLAY_ITEMS_RANGE("display-items.range", 10, "The range to display the item in blocks."),
        DISPLAY_ITEM_Y_OFFSET("display-items.y-offset", 0.5, "The Y offset of the item."),
//        DISPLAY_ITEMS_SIZE("display-items.size", 0.3, "The width and height of the item."),
        ;

        private final String key;
        private final Object defaultValue;
        private final String[] comments;
        private Object value = null;

        Setting(String key, Object defaultValue, String... comments) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comments = comments != null ? comments : new String[0];
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public Object getDefaultValue() {
            return this.defaultValue;
        }

        @Override
        public String[] getComments() {
            return this.comments;
        }

        @Override
        public Object getCachedValue() {
            return this.value;
        }

        @Override
        public void setCachedValue(Object value) {
            this.value = value;
        }

        @Override
        public CommentedFileConfiguration getBaseConfig() {
            return EternalChestShops.getInstance().getManager(ConfigurationManager.class).getConfig();
        }
    }
}
