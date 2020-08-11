package org.plugins.antidrop.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.plugins.antidrop.AntiDropMain;
import org.plugins.antidrop.managers.AntiDropManager;
import roryslibrary.util.MessagingUtil;

public class AntiDropCommand implements CommandExecutor {

    private final AntiDropMain    plugin;
    private final AntiDropManager antiDropManager;
    private final MessagingUtil   messagingUtil;

    public AntiDropCommand(AntiDropMain plugin, MessagingUtil messagingUtil, AntiDropManager antiDropManager) {
        this.plugin = plugin;
        this.messagingUtil = messagingUtil;
        this.antiDropManager = antiDropManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("antidrop") && args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("antidrop.reload")) {
                    sender.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("no-permission")));
                    return true;
                }
                this.plugin.reloadConfig();
                this.antiDropManager.refreshAntiDropItems();
                AntiDropManager.DROP_DELAY = 1000L * Long.valueOf(plugin.getConfig().getInt("drop-delay"));
                sender.sendMessage(this.messagingUtil.placeholders("{PREFIX}Configuration reloaded"));
                return true;
            }
        }

        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (cmd.getName().equalsIgnoreCase("antidrop")) {
                if (!sender.hasPermission("antidrop.toggle")) {
                    sender.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("no-permission")));
                    return true;
                } else {
                    this.antiDropManager.togglePlayer(p);
                    if (this.plugin.getConfig().isSet("anti-drop-toggled")) {
                        p.sendMessage(this.messagingUtil.placeholders(this.plugin.getConfig().getString("anti-drop-toggled").replace("{TOGGLED}", this.antiDropManager.isToggled(p) ? "enabled" : "disabled")));
                    }
                }
            }

        } else {
            return false;
        }

        return false;
    }
}
