package com.ryandw11.structure.commands;;

import com.ryandw11.structure.structure.Structure;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ryandw11.structure.CustomStructures;

public class SCommand implements CommandExecutor {
	private CustomStructures plugin;

	public SCommand(CustomStructures plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if(!CustomStructures.enabled) {
			sender.sendMessage(ChatColor.RED + "One of your schematic or lootable files could not be found!");
			sender.sendMessage(ChatColor.RED + "Please check to see if all of your files are in the proper folders!");
			sender.sendMessage(ChatColor.RED + "To find out more, see the error in the console.");
			return true;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("customstructures.reload")) {
				plugin.reloadConfig();
				sender.sendMessage("The plugin has been reloaded!");
				plugin.getLogger().info("Plugin reloaded!");

				plugin.reloadHandlers();
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
			}
		} else if (args.length == 2 && args[0].equalsIgnoreCase("test")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command is for players only!");
				return true;
			}
			Player p = (Player) sender;
			if (!p.hasPermission("customstructures.test")) {
				p.sendMessage(ChatColor.RED + "You do not have permission for this command.");
				return true;
			}
			Structure structure = plugin.getStructureHandler().getStructure(args[1]);
			if(structure == null){
				p.sendMessage(ChatColor.RED + "That schematic does not exist!");
				return true;
			}

			structure.spawn(p.getLocation());

		} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			sender.sendMessage(ChatColor.GREEN + "Currently Active Schematics:");
			for (Structure st : plugin.getStructureHandler().getStructures()) {
				sender.sendMessage(ChatColor.GREEN + " - " + ChatColor.BLUE + st.getName());
			}
		} else {
			if (sender.hasPermission("customstructures.info")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3=============[&2CustomStructures&3]============="));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3Created by: &2Ryandw11"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3Version: &2" + plugin.getDescription().getVersion()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3Github wiki:&2 https://github.com/ryandw11/CustomStructures/wiki"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3Commands:"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3/cs reload - &2Reload the plugin."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure test (name) - &2Paste the defined structure."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure list - &2List the currently active structures."));
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
			}
		}

		return false;
	}

}
