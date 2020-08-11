package org.plugins.antidrop.managers;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.plugins.antidrop.AntiDropMain;
import org.plugins.antidrop.objects.AntiDropItem;
import org.plugins.antidrop.objects.DropConfirmation;
import roryslibrary.configs.CustomConfig;
import roryslibrary.util.ItemUtil;
import roryslibrary.util.MessagingUtil;

import java.util.*;

public class AntiDropManager {

    private final AntiDropMain                          plugin;
    private final ItemUtil                              itemUtil;
    private final CustomConfig                          playersConfig;
    private final HashSet<AntiDropItem>                 antiDropItems = new HashSet<>();
    private final HashMap<UUID, List<DropConfirmation>> dropConfirmations = new HashMap<>();

    public static Long DROP_DELAY;

    public AntiDropManager(AntiDropMain plugin, ItemUtil itemUtil, CustomConfig playersConfig) {
        this.plugin = plugin;
        this.itemUtil = itemUtil;
        this.playersConfig = playersConfig;
        this.refreshAntiDropItems();
        AntiDropManager.DROP_DELAY = 1000L * Long.valueOf(plugin.getConfig().getInt("drop-delay"));
        this.startDropConfirmationCleaner();
    }

    public void refreshAntiDropItems() {
        this.antiDropItems.clear();
        for (String itemName : this.plugin.getConfig().getConfigurationSection("specific-items").getKeys(false)) {
            AntiDropItem antiDropItem = new AntiDropItem(itemName, itemUtil.getItemStack("specific-items." + itemName), this.plugin.getConfig().getString("specific-items." + itemName + ".anti-drop-message"), this.plugin.getConfig().getString("specific-items." + itemName + ".drop-message"), this.plugin.getConfig().getBoolean("specific-items." + itemName + ".drop"));

            this.antiDropItems.add(antiDropItem);
        }
    }

    public void togglePlayer(Player p) {
        this.playersConfig.getConfig().set("antidrop." + p.getUniqueId().toString(),
                !this.playersConfig.getConfig().getBoolean("antidrop." + p.getUniqueId().toString(), this.plugin.getConfig().getBoolean("enabled-by-default")));
        this.playersConfig.saveConfig();
        this.playersConfig.reloadConfig();
    }

    public boolean isToggled(Player p) {
        return this.playersConfig.getConfig().getBoolean("antidrop." + p.getUniqueId().toString(), this.plugin.getConfig().getBoolean("enabled-by-default"));
    }

    public boolean isAnAntiDropItem(ItemStack item) {
        return this.getAntiDropItem(item) != null;
    }

    public AntiDropItem getAntiDropItem(ItemStack item) {
        for (AntiDropItem antiDropItem : this.antiDropItems) {
            if (this.isSimilar(item, antiDropItem.getItem())) {
                return antiDropItem;
            }
        }
        return null;
    }

    public boolean isSimilar(ItemStack item1, ItemStack item2) {
        ItemMeta itemMeta = item1.getItemMeta();
        ItemMeta item2Meta = item2.getItemMeta();
        if (item1.getType() == item2.getType() && item1.getDurability() == item2.getDurability() && itemMeta.hasDisplayName() == item2Meta.hasDisplayName() && itemMeta.hasLore() == item2Meta.hasLore() && itemMeta.hasEnchants() == item2Meta.hasEnchants()) {
            if (!itemMeta.hasDisplayName() && MessagingUtil.format(itemMeta.getDisplayName()).equals(MessagingUtil.format(item2Meta.getDisplayName()))) {
                return false;
            }

            if (itemMeta.hasLore()) {
                List<String> lore = itemMeta.getLore();
                if (lore.size() != item2Meta.getLore().size()) {
                    return false;
                }
                int count = 0;
                for (String loreLine : item2Meta.getLore()) {
                    if (!MessagingUtil.format(lore.get(count++)).equals(MessagingUtil.format(loreLine))) {
                        return false;
                    }
                }
            }

            if (itemMeta.hasEnchants()) {
                for (Enchantment enchant : itemMeta.getEnchants().keySet()) {
                    if (itemMeta.getEnchantLevel(enchant) != item2.getEnchantmentLevel(enchant)) {
                        return false;
                    }
                }
            }

            return true;
        }
        return false;
    }

    public DropConfirmation getDropConfirmation(Player p, ItemStack item) {
        if (this.dropConfirmations.containsKey(p.getUniqueId())) {
            for (DropConfirmation dropConfirmation : this.dropConfirmations.get(p.getUniqueId())) {
                if (dropConfirmation.getItem().isSimilar(item)) {
                    return dropConfirmation;
                }
            }
        }
        return null;
    }

    public void addDropConfirmation(Player p, ItemStack item) {
        if (!this.dropConfirmations.containsKey(p.getUniqueId())) {
            this.dropConfirmations.put(p.getUniqueId(), new ArrayList<DropConfirmation>());
        }
        this.dropConfirmations.get(p.getUniqueId()).add(new DropConfirmation(item));
    }

    public void removeDropConfirmation(Player p, DropConfirmation dropConfirmation) {
        if (dropConfirmation != null) {
            this.dropConfirmations.get(p.getUniqueId()).remove(dropConfirmation);
        }
    }

    public void startDropConfirmationCleaner() {
        (new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : dropConfirmations.keySet()) {
                    List<DropConfirmation> dropConfirmationList = dropConfirmations.get(uuid);
                    for (int i = dropConfirmationList.size() - 1; i >= 0; i--) {
                        if (!dropConfirmationList.get(i).isWithinTimeLimit()) {
                            dropConfirmationList.remove(i);
                        }
                    }
                }
            }
        }).runTaskTimer(this.plugin, 30 * 20L, 30 * 20L);
    }

}