package org.plugins.antidrop.objects;

import org.bukkit.inventory.ItemStack;
import roryslibrary.util.MessagingUtil;

public class AntiDropItem {

    private final String itemName;
    private final ItemStack item;
    private final String antiDropMessage, dropMessage;
    private final boolean drop;

    public AntiDropItem(String itemName, ItemStack item, String antiDropMessage, String dropMessage, boolean drop) {
        this.itemName = MessagingUtil.format(itemName);
        this.item = item;
        this.antiDropMessage = antiDropMessage;
        this.dropMessage = dropMessage;
        this.drop = drop;
    }

    public String getItemName() {
        return this.itemName;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public String getAntiDropMessage() {
        return this.antiDropMessage;
    }

    public String getDropMessage() {
        return this.dropMessage;
    }

    public boolean drop() {
        return this.drop;
    }

}
