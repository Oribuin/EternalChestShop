package xyz.oribuin.chestshops.command.argument;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.command.framework.ArgumentParser;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentHandler;
import dev.rosewood.rosegarden.command.framework.RoseCommandArgumentInfo;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import xyz.oribuin.chestshops.model.ShopType;
import xyz.oribuin.chestshops.util.ShopUtils;

import java.util.Arrays;
import java.util.List;

public class ShopTypeArgumentHandler extends RoseCommandArgumentHandler<ShopType> {

    public ShopTypeArgumentHandler(RosePlugin rosePlugin) {
        super(rosePlugin, ShopType.class);
    }

    @Override
    protected ShopType handleInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) throws HandledArgumentException {
        String input = argumentParser.next();

        ShopType type = ShopUtils.getEnum(ShopType.class, input);
        if (type != null)
            return type;

        throw new HandledArgumentException("argument-handler-shop-type", StringPlaceholders.of("input", input));
    }

    @Override
    protected List<String> suggestInternal(RoseCommandArgumentInfo argumentInfo, ArgumentParser argumentParser) {
        argumentParser.next();

        return Arrays.stream(ShopType.values()).map(shopType -> shopType.name().toLowerCase()).toList();
    }

}
