package org.plugins.antidrop.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.plugins.antidrop.AntiDropMain;
import org.plugins.antidrop.managers.AntiDropManager;
import org.plugins.antidrop.objects.DropConfirmation;
import roryslibrary.configs.CustomConfig;
import roryslibrary.util.MessagingUtil;

public class PlayerDropListener implements Listener {

    private final AntiDropMain    plugin;
    private final MessagingUtil   messagingUtil;
    private final CustomConfig    playersConfig;
    private final AntiDropManager antiDropManager;

    public PlayerDropListener(AntiDropMain plugin, MessagingUtil messagingUtil, CustomConfig playersConfig, AntiDropManager antiDropManager) {
        this.plugin = plugin;
        this.messagingUtil = messagingUtil;
        this.playersConfig = playersConfig;
        this.antiDropManager = antiDropManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();
        if (e.getPlayer().hasPermission("antidrop.use") && this.playersConfig.getConfig().getBoolean("antidrop." + p.getUniqueId().toString(), this.plugin.getConfig().getBoolean("enabled-by-default")) && item.getType() != Material.AIR) {
            DropConfirmation dropConfirmation = this.antiDropManager.getDropConfirmation(p, item);
            if (this.plugin.getConfig().getStringList("applied-items").contains(item.getType().name())) {
                if (dropConfirmation == null) {
                    e.setCancelled(true);
                    if (this.plugin.getConfig().isSet("anti-drop-message")) {
                        e.getPlayer().sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("anti-drop-message")));
                    }
                    this.antiDropManager.addDropConfirmation(p, item);
                } else if (dropConfirmation.isWithinTimeLimit()) {
                    e.setCancelled(false);
                    this.antiDropManager.removeDropConfirmation(p, dropConfirmation);
                    if (this.plugin.getConfig().isSet("drop-message")) {
                        e.getPlayer().sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("drop-message")));
                    }
                    this.playersConfig.getConfig().set("anti-drop-items-dropped", this.playersConfig.getConfig().getInt("anti-drop-items-saved") + 1);
                    this.playersConfig.saveConfig();
                    this.playersConfig.reloadConfig();
                } else {
                    e.setCancelled(true);
                    if (this.plugin.getConfig().isSet("anti-drop-message")) {
                        e.getPlayer().sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("anti-drop-message")));
                    }
                    dropConfirmation.refreshTime();
                    this.playersConfig.getConfig().set("anti-drop-items-saved", this.playersConfig.getConfig().getInt("anti-drop-items-saved") + 1);
                    this.playersConfig.saveConfig();
                    this.playersConfig.reloadConfig();
                }
            } else if (this.antiDropManager.isAnAntiDropItem(e.getItemDrop().getItemStack())) {
                String itemName = this.antiDropManager.getAntiDropItem(e.getItemDrop().getItemStack()).getItemName();
                if (dropConfirmation == null) {
                    e.setCancelled(true);
                    if (this.plugin.getConfig().isSet("specific-items." + itemName + ".anti-drop-message")) {
                        e.getPlayer().sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("specific-items." + itemName + ".anti-drop-message")));
                    } else if (this.plugin.getConfig().isSet("anti-drop-message")) {
                        e.getPlayer().sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("anti-drop-message")));
                    }
                    this.antiDropManager.addDropConfirmation(p, item);
                } else if (dropConfirmation.isWithinTimeLimit()) {
                    e.setCancelled(false);
                    this.antiDropManager.removeDropConfirmation(p, dropConfirmation);
                    if (this.plugin.getConfig().isSet("specific-items." + itemName + ".drop-message")) {
                        e.getPlayer().sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("specific-items." + itemName + ".drop-message")));
                    } else if (this.plugin.getConfig().isSet("drop-message")) {
                        e.getPlayer().sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("drop-message")));
                    }
                    this.playersConfig.getConfig().set("anti-drop-items-dropped", this.playersConfig.getConfig().getInt("anti-drop-items-saved") + 1);
                    this.playersConfig.saveConfig();
                    this.playersConfig.reloadConfig();
                } else {
                    e.setCancelled(true);
                    if (this.plugin.getConfig().isSet("specific-items." + itemName + ".anti-drop-message")) {
                        e.getPlayer().sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("specific-items." + itemName + ".anti-drop-message")));
                    } else if (this.plugin.getConfig().isSet("anti-drop-message")) {
                        e.getPlayer().sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("anti-drop-message")));
                    }
                    dropConfirmation.refreshTime();
                    this.playersConfig.getConfig().set("anti-drop-items-saved", this.playersConfig.getConfig().getInt("anti-drop-items-saved") + 1);
                    this.playersConfig.saveConfig();
                    this.playersConfig.reloadConfig();
                }
            }
        }
    }
}
