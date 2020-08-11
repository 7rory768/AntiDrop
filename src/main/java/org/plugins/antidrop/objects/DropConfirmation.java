package org.plugins.antidrop.objects;

import org.bukkit.inventory.ItemStack;
import org.plugins.antidrop.managers.AntiDropManager;

public class DropConfirmation {

    private ItemStack item;
    private Long time;

    public DropConfirmation(ItemStack item) {
        this.item = item;
        this.time = System.currentTimeMillis();
    }

    public ItemStack getItem() {
        return this.item;
    }

    public boolean isWithinTimeLimit() {
        return System.currentTimeMillis() - this.time <= AntiDropManager.DROP_DELAY;
    }

    public void refreshTime() {
        this.time = System.currentTimeMillis();
    }

}
