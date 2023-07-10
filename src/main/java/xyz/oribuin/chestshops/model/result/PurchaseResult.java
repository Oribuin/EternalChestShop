package xyz.oribuin.chestshops.model.result;

public enum PurchaseResult {
    SUCCESS, // Purchase was successful
    NOT_ENOUGH_MONEY, // Player does not have enough money to purchase the item
    NOT_ENOUGH_SPACE, // Player does not have enough space to purchase the item
    NOT_ENOUGH_ITEMS, // Shop does not have enough items to sell#
    INVALID_SHOP, // Shop is invalid

}
