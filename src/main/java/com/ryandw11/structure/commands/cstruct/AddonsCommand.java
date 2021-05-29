package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.CustomStructureAddon;
import com.ryandw11.structure.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * The addons command for the plugin.
 *
 * <p>Permission: customstructures.addon</p>
 *
 * <code>
 * /cstruct addons
 * </code>
 */
public class AddonsCommand implements SubCommand {

    private final CustomStructures plugin;

    public AddonsCommand(CustomStructures plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(!sender.hasPermission("customstructures.addon")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            return true;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3============[&2Enabled Addons&3]============"));
        for(CustomStructureAddon addon : plugin.getAddonHandler().getCustomStructureAddons()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    String.format("&a- &6%s &aby: &6%s", addon.getName(), String.join("&a,&6 ", addon.getAuthors()))));
        }
        return false;
    }

}
