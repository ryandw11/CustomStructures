package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * The reload command for the plugin.
 *
 * <code>
 * /cstruct reload
 * </code>
 */
public class ReloadCommand implements SubCommand {

    private final CustomStructures plugin;

    public ReloadCommand(CustomStructures plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (sender.hasPermission("customstructures.reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "The plugin has been reloaded!");
            plugin.getLogger().info("Plugin reloaded!");
            plugin.reloadHandlers();
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
        }

        return false;
    }

}
