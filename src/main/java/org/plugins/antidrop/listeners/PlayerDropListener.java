package org.plugins.antidrop.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.antidrop.AntiDropMain;
import org.plugins.antidrop.managers.AntiDropManager;
import org.plugins.antidrop.objects.DropConfirmation;
import roryslibrary.configs.CustomConfig;
import roryslibrary.util.MessagingUtil;

public class PlayerDropListener implements Listener {
	
	private final AntiDropMain plugin;
	private final MessagingUtil messagingUtil;
	private final CustomConfig playersConfig;
	private final AntiDropManager antiDropManager;
	
	public PlayerDropListener(AntiDropMain plugin, MessagingUtil messagingUtil, CustomConfig playersConfig, AntiDropManager antiDropManager) {
		this.plugin = plugin;
		this.messagingUtil = messagingUtil;
		this.playersConfig = playersConfig;
		this.antiDropManager = antiDropManager;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getPlayer().isDead()) return;

		ItemStack item = event.getView().getCursor();

		if (event.getPlayer() instanceof Player) {
			Player player =  (Player) event.getPlayer();
			InventoryView view = event.getView();
			if (view.getTopInventory() != null && view.getTopInventory().getType() != InventoryType.CRAFTING) {
				int amountLeft = item.getAmount();
				for (int slot = 0; slot < 36; slot++) {
					if (view.getBottomInventory().getItem(slot) == null) {
						return;
					} else if (view.getBottomInventory().getItem(slot).isSimilar(item)) {
						amountLeft -= Math.max(0, view.getBottomInventory().getItem(slot).getMaxStackSize() - view.getBottomInventory().getItem(slot).getAmount());
					}
				}
				
				if (amountLeft > 0) {
					ItemStack itemLeft = item.clone();
					itemLeft.setAmount(amountLeft);
					
					for (ItemStack leftoverItem : event.getView().getBottomInventory().addItem(itemLeft).values()) {
						for (ItemStack leftoverItem1 : event.getView().getTopInventory().addItem(leftoverItem).values()) {
							player.getWorld().dropItem(player.getLocation(), leftoverItem1);
						}
					}
					
					view.setCursor(null);
					
					new BukkitRunnable() {
						@Override
						public void run() {
							player.updateInventory();
						}
					}.runTaskLater(plugin, 0L);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onDropItem(PlayerDropItemEvent e) {
		if (e.getPlayer().isDead()) return;

		Player p = e.getPlayer();
		ItemStack item = e.getItemDrop().getItemStack();
		
		if (antiDropCheck(p, item)) {
			InventoryView view = e.getPlayer().getOpenInventory();
			if (view.getTopInventory() != null && view.getTopInventory().getType() != InventoryType.CRAFTING) {
				for (int slot = 0; slot < 36; slot++) {
					if (view.getBottomInventory().getItem(slot) == null) {
						e.setCancelled(true);
						new BukkitRunnable() {
							@Override
							public void run() {
								p.updateInventory();
							}
						}.runTaskLater(plugin, 0L);
						return;
					}
				}
				
				e.getItemDrop().remove();
				
				for (ItemStack leftoverItem : view.getBottomInventory().addItem(item).values()) {
					for (ItemStack leftoverItem1 : view.getTopInventory().addItem(leftoverItem).values()) {
						view.setCursor(leftoverItem1);
					}
				}
				
				new BukkitRunnable() {
					@Override
					public void run() {
						p.updateInventory();
					}
				}.runTaskLater(plugin, 0L);
			} else {
				int amountLeft = item.getAmount();
				
				for (int slot = 0; slot < 36; slot++) {
					if (view.getBottomInventory().getItem(slot) == null) {
						e.setCancelled(true);
						new BukkitRunnable() {
							@Override
							public void run() {
								p.updateInventory();
							}
						}.runTaskLater(plugin, 0L);
						return;
					}
					else if (view.getBottomInventory().getItem(slot).isSimilar(item)) {
						amountLeft -= Math.max(0, item.getMaxStackSize() - view.getBottomInventory().getItem(slot).getAmount());
					}
				}
				
				e.setCancelled(true);
				new BukkitRunnable() {
					@Override
					public void run() {
						p.updateInventory();
					}
				}.runTaskLater(plugin, 0L);
				
				if (amountLeft > 0) {
					ItemStack leftover = item.clone();
					leftover.setAmount(amountLeft);
					e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), leftover);
				}
			}
		}
	}
	
	public boolean antiDropCheck(Player player, ItemStack item) {
		if (player.hasPermission("antidrop.use") && this.playersConfig.getConfig().getBoolean("antidrop." + player.getUniqueId().toString(), this.plugin.getConfig().getBoolean("enabled-by-default")) && item.getType() != Material.AIR) {
			DropConfirmation dropConfirmation = this.antiDropManager.getDropConfirmation(player, item);
			
			if (this.plugin.getConfig().getStringList("applied-items").contains(item.getType().name())) {
				if (dropConfirmation == null) {
					
					if (this.plugin.getConfig().isSet("anti-drop-message")) {
						player.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("anti-drop-message")));
					}
					
					this.antiDropManager.addDropConfirmation(player, item);
					return true;
				} else if (dropConfirmation.isWithinTimeLimit()) {
					this.antiDropManager.removeDropConfirmation(player, dropConfirmation);
					
					if (this.plugin.getConfig().isSet("drop-message")) {
						player.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("drop-message")));
					}
					
					this.playersConfig.getConfig().set("anti-drop-items-dropped", this.playersConfig.getConfig().getInt("anti-drop-items-dropped") + 1);
					this.playersConfig.saveConfig();
					this.playersConfig.reloadConfig();
					
					return false;
				} else {
					
					if (this.plugin.getConfig().isSet("anti-drop-message")) {
						player.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("anti-drop-message")));
					}
					
					dropConfirmation.refreshTime();
					this.playersConfig.getConfig().set("anti-drop-items-saved", this.playersConfig.getConfig().getInt("anti-drop-items-saved") + 1);
					this.playersConfig.saveConfig();
					this.playersConfig.reloadConfig();
					
					return true;
				}
			} else if (this.antiDropManager.isAnAntiDropItem(item)) {
				String itemName = this.antiDropManager.getAntiDropItem(item).getItemName();
				if (dropConfirmation == null) {
					
					if (this.plugin.getConfig().isSet("specific-items." + itemName + ".anti-drop-message")) {
						player.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("specific-items." + itemName + ".anti-drop-message")));
					} else if (this.plugin.getConfig().isSet("anti-drop-message")) {
						player.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("anti-drop-message")));
					}
					
					this.antiDropManager.addDropConfirmation(player, item);
					
					return true;
				} else if (dropConfirmation.isWithinTimeLimit()) {
					this.antiDropManager.removeDropConfirmation(player, dropConfirmation);
					
					if (this.plugin.getConfig().isSet("specific-items." + itemName + ".drop-message")) {
						player.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("specific-items." + itemName + ".drop-message")));
					} else if (this.plugin.getConfig().isSet("drop-message")) {
						player.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("drop-message")));
					}
					
					this.playersConfig.getConfig().set("anti-drop-items-dropped", this.playersConfig.getConfig().getInt("anti-drop-items-dropped") + 1);
					this.playersConfig.saveConfig();
					this.playersConfig.reloadConfig();
					
					return false;
				} else {
					
					if (this.plugin.getConfig().isSet("specific-items." + itemName + ".anti-drop-message")) {
						player.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("specific-items." + itemName + ".anti-drop-message")));
					} else if (this.plugin.getConfig().isSet("anti-drop-message")) {
						player.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("anti-drop-message")));
					}
					
					dropConfirmation.refreshTime();
					this.playersConfig.getConfig().set("anti-drop-items-saved", this.playersConfig.getConfig().getInt("anti-drop-items-saved") + 1);
					this.playersConfig.saveConfig();
					this.playersConfig.reloadConfig();
					
					return true;
				}
			}
		}
		
		return false;
	}
}
