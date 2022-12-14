package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.commands.SubCommand;
import com.ryandw11.structure.schematic.SchematicHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The Get Item command for the plugin.
 *
 * <p>Permission: customstructures.createschematic</p>
 * <p>Secondary Permission: customstructures.createschematic.options</p>
 *
 * <code>
 * /cstruct createschem {name} [-c]
 * </code>
 */
public class CreateSchematicCommand implements SubCommand {

    public CreateSchematicCommand() {
    }

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission("customstructures.createschematic")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
                return true;
            }
            sender.sendMessage(ChatColor.RED + "You must specify the name in order to create a schematic.");
        } else if (args.length == 1) {
            if (!sender.hasPermission("customstructures.createschematic")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command is for players only!");
                return true;
            }
            Player p = (Player) sender;
            String name = args[0].replace(".schem", "");
            if (SchematicHandler.createSchematic(name, p, p.getWorld(), false)) {
                p.sendMessage(ChatColor.GREEN + "Successfully created a schematic with the name of " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
                p.sendMessage(ChatColor.GREEN + "You can now use " + ChatColor.GOLD + name + ".schem" + ChatColor.GREEN + " in a structure.");
            } else {
                p.sendMessage(ChatColor.RED + "The world edit region seems to be incomplete! Try making a selection first!");
            }
        } else if (args.length == 2) {
            if (!sender.hasPermission("customstructures.createschematic.options")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
                return true;
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command is for players only!");
                return true;
            }

            if (args[1].equalsIgnoreCase("-c") || args[1].equalsIgnoreCase("-compile")) {
                Player p = (Player) sender;
                String name = args[0].replace(".schem", "");
                if (SchematicHandler.createSchematic(name, p, p.getWorld(), true)) {
                    p.sendMessage(ChatColor.GREEN + "Successfully created a schematic with the name of " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
                    p.sendMessage(ChatColor.GREEN + "Successfully compiled the schematic!");
                    p.sendMessage(ChatColor.GREEN + "You can now use " + ChatColor.GOLD + name + ".schem" +
                            ChatColor.GREEN + " and " + ChatColor.GOLD + name + ".cschem" + ChatColor.GREEN + " in a structure.");
                } else {
                    p.sendMessage(ChatColor.RED + "The world edit region seems to be incomplete! Try making a selection first!");
                }
            }
            if (args[1].equalsIgnoreCase("-cOnly") || args[1].equalsIgnoreCase("-compileOnly")) {
                Player p = (Player) sender;
                String name = args[0].replace(".schem", "").replace(".cschem", "");
                if (SchematicHandler.compileOnly(name, p, p.getWorld())) {
                    p.sendMessage(ChatColor.GREEN + "Successfully compiled the schematic with the name of " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
                    p.sendMessage(ChatColor.RED + "The option is for advanced users only. Please be sure the selection is valid.");
                } else {
                    p.sendMessage(ChatColor.RED + "The world edit region seems to be incomplete! Try making a selection first!");
                }
            }
        }
        return false;
    }

}
