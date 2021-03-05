package org.plugins.antidrop.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.antidrop.AntiDropMain;
import org.plugins.antidrop.managers.AntiDropManager;
import roryslibrary.configs.CustomConfig;
import roryslibrary.util.ItemUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerDeathListener implements Listener {
	
	private final AntiDropMain plugin;
	private final CustomConfig playersConfig;
	private final AntiDropManager antiDropManager;
	
	public PlayerDeathListener(AntiDropMain plugin, CustomConfig playersConfig, AntiDropManager antiDropManager) {
		this.plugin = plugin;
		this.playersConfig = playersConfig;
		this.antiDropManager = antiDropManager;
	}
	
	@EventHandler
	public void onDeath(final PlayerDeathEvent e) {
		final Player p = e.getEntity();
		InventoryView view = p.getOpenInventory();
		
		List<String> appliedItems = this.plugin.getConfig().getStringList("applied-items");
		
		if (view.getCursor() != null && view.getCursor().getType() != Material.AIR) {
			ItemStack item = view.getCursor();
			
			boolean giveItemBack = false;
			
			if (p.hasPermission("antidrop.deathprotection") && (this.antiDropManager.isToggled(p) || !this.plugin.getConfig().getBoolean("death-drop-inherits-antidrop"))) {
				if (this.antiDropManager.isAnAntiDropItem(item)) {
					if (this.antiDropManager.getAntiDropItem(item).drop()) {
						giveItemBack = true;
					}
				} else if (!this.plugin.getConfig().getBoolean("applied-items-death-drop") && appliedItems.contains(item.getType().name())) {
					giveItemBack = true;
				}
			}
			
			if (giveItemBack) {
				new BukkitRunnable() {
					@Override
					public void run() {
						p.getInventory().addItem(item);
						p.updateInventory();
					}
				}.runTaskLater(plugin, 5L);
			} else {
				p.getWorld().dropItem(p.getLocation(), item);
				view.setCursor(null);
			}
		}
		
		if (!p.hasPermission("*") && p.hasPermission("-antidrop.deathprotection")) {
			return;
		}
		
		if (p.hasPermission("antidrop.deathprotection") && (this.antiDropManager.isToggled(p) || !this.plugin.getConfig().getBoolean("death-drop-inherits-antidrop"))) {
			HashMap<Integer, ItemStack> itemsToGive = new HashMap<Integer, ItemStack>();
			for (int slot = 0; slot < 36; slot++) {
				ItemStack item = p.getInventory().getItem(slot);
				if (item != null) {
					if (this.antiDropManager.isAnAntiDropItem(item)) {
						if (this.antiDropManager.getAntiDropItem(item).drop()) {
							itemsToGive.put(slot, item);
						}
					} else if (!this.plugin.getConfig().getBoolean("applied-items-death-drop") && appliedItems.contains(item.getType().name())) {
						itemsToGive.put(slot, item);
					}
				}
			}
			
			for (final Map.Entry<Integer, ItemStack> itemEntry : itemsToGive.entrySet()) {
				this.playersConfig.getConfig().set("items-saved-from-death", this.playersConfig.getConfig().getInt("items-saved-from-death") + 1);
				e.getDrops().remove(itemEntry.getValue());
			}
			this.playersConfig.saveConfig();
			this.playersConfig.reloadConfig();
			
			final HashMap<Integer, ItemStack> finalItemsToGive = itemsToGive;
			(new BukkitRunnable() {
				
				@Override
				public void run() {
					for (final Map.Entry<Integer, ItemStack> itemEntry : finalItemsToGive.entrySet()) {
						p.getInventory().setItem(itemEntry.getKey(), itemEntry.getValue());
					}
					p.updateInventory();
				}
				
			}).runTaskLater(this.plugin, 0L);
		}
	}
}
