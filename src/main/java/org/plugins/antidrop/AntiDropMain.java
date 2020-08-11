package org.plugins.antidrop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.plugins.antidrop.commands.AntiDropCommand;
import org.plugins.antidrop.listeners.PlayerDeathListener;
import org.plugins.antidrop.listeners.PlayerDropListener;
import org.plugins.antidrop.managers.AntiDropManager;
import org.plugins.antidrop.utils.Metrics;
import roryslibrary.configs.CustomConfig;
import roryslibrary.util.CustomConfigUtil;
import roryslibrary.util.ItemUtil;
import roryslibrary.util.MessagingUtil;
import roryslibrary.util.PluginMessagingUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class AntiDropMain extends JavaPlugin {

    private CustomConfig    playersConfig;
    private AntiDropManager antiDropManager;
    private MessagingUtil   messagingUtil;
    private ItemUtil        itemUtil;

    @Override
    public void onEnable() {
        this.loadConfigs();
        this.initializeVariables();
        this.setupMetrics();
        this.registerListeners();
        this.registerCommands();
    }

    public void initializeVariables() {
        this.messagingUtil = new PluginMessagingUtil(this);
        this.itemUtil = new ItemUtil(this);
        this.antiDropManager = new AntiDropManager(this, this.itemUtil, this.playersConfig);
    }

    public void loadConfigs() {
        this.playersConfig = new CustomConfig(this,"playerdata");
        CustomConfigUtil.loadDefaultConfig(this);
        CustomConfigUtil.loadConfig(this.playersConfig);
    }

    public void registerListeners() {
        PluginManager plManager = Bukkit.getPluginManager();
        plManager.registerEvents(new PlayerDeathListener(this, this.playersConfig, this.antiDropManager), this);
        plManager.registerEvents(new PlayerDropListener(this, this.messagingUtil, this.playersConfig, this.antiDropManager), this);
    }

    public void registerCommands() {
        this.getCommand("antidrop").setExecutor(new AntiDropCommand(this, this.messagingUtil, this.antiDropManager));
    }

    public void setupMetrics() {
        Metrics metrics = new Metrics(this, 2880);
        metrics.addCustomChart(new Metrics.AdvancedPie("item_stats", new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                Map<String, Integer> valueMap = new HashMap<>();
                valueMap.put("Items Dropped", playersConfig.getConfig().getInt("anti-drop-items-dropped", 0));
                valueMap.put("Items Saved", playersConfig.getConfig().getInt("anti-drop-items-saved", 0));
                valueMap.put("Items Saved (From Death)", playersConfig.getConfig().getInt("items-saved-from-death", 0));
                return valueMap;
            }
        }));
    }

}
