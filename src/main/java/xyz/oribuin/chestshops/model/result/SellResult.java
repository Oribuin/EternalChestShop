package xyz.oribuin.chestshops.model.result;

public enum SellResult {
    SUCCESS, // Purchase was successful
    NOT_ENOUGH_MONEY, // Shop does not have enough money to purchase the item
    NOT_ENOUGH_SPACE, // Shop does not have enough space to purchase the item
    NOT_ENOUGH_ITEMS, // Player does not have enough items to sell
    INVALID_SHOP, // Shop is invalid
}
