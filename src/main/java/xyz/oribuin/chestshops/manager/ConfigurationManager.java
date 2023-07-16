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

        // Signs
        SIGN_SETTINGS("sign-settings", null, "Modify the text for the signs here."),
        SIGN_SETTINGS_MATERIAL("sign-settings.material", Material.OAK_WALL_SIGN.name(), "The material for the signs.", "This sign must be a wall sign."),
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
        ), "The text for the selling sign.");

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
