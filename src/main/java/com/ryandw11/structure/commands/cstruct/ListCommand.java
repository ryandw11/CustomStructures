package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.commands.SubCommand;
import com.ryandw11.structure.structure.Structure;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * The list command for the plugin.
 *
 * <code>
 * /cstruct list
 * </code>
 */
public class ListCommand implements SubCommand {

    private final CustomStructures plugin;

    public ListCommand(CustomStructures plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        sender.sendMessage(ChatColor.GREEN + "Currently Active Structures:");

        for (Structure st : plugin.getStructureHandler().getStructures()) {
            sender.sendMessage(ChatColor.GREEN + " - " + ChatColor.BLUE + st.getName());
        }
        return false;
    }

}
