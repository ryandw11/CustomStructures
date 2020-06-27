package com.ryandw11.structure.commands;;

import com.ryandw11.structure.structure.Structure;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ryandw11.structure.CustomStructures;
import org.bukkit.inventory.ItemStack;

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
		} else if(args.length == 1 && args[0].equalsIgnoreCase("additem")){
			if(!sender.hasPermission("customstructures.additem")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			sender.sendMessage(ChatColor.RED + "You must specify a unique key to call the item by.");
		}else if(args.length == 2 && args[0].equalsIgnoreCase("additem")){
			if(!sender.hasPermission("customstructures.additem")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "This command is for players only!");
				return true;
			}
			Player p = (Player) sender;
			String key = args[1];
			ItemStack item = p.getInventory().getItemInMainHand();
			item.setAmount(1);
			if(item.getType() == Material.AIR){
				p.sendMessage(ChatColor.RED + "You must be holding an item to use this command!");
				return true;
			}
			if(!plugin.getCustomItemManager().addItem(key, item)){
				p.sendMessage(ChatColor.RED + "That key already exists!");
			}else{
				p.sendMessage(ChatColor.GREEN + "Successfully added the custom item to the list.");
			}
		}else if(args.length == 1 && args[0].equalsIgnoreCase("checkkey")){
			if(!sender.hasPermission("customstructures.checkkey")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "This command is for players only!");
				return true;
			}
			Player p = (Player) sender;
			ItemStack item = p.getInventory().getItemInMainHand();
			if(item.getType() == Material.AIR){
				p.sendMessage(ChatColor.RED + "You must be holding an item to use this command!");
				return true;
			}
			for(String items : plugin.getCustomItemManager().getConfig().getConfigurationSection("").getKeys(false)){
				if(item.isSimilar(plugin.getCustomItemManager().getItem(items))){
					p.sendMessage(ChatColor.GREEN + "The item you are holding has a key of: " + ChatColor.GOLD + items);
					return true;
				}
			}
			p.sendMessage(ChatColor.RED + "That item was not found in the item list.");
		}else if(args.length == 1 && args[0].equalsIgnoreCase("getitem")){
			if(!sender.hasPermission("customstructures.getitem")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			sender.sendMessage(ChatColor.RED + "You must specify the key in order to get an item from the list.");
		}else if(args.length == 2 && args[0].equalsIgnoreCase("getitem")){
			if(!sender.hasPermission("customstructures.getitem")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "This command is for players only!");
				return true;
			}
			Player p = (Player) sender;
			String key = args[1];
			ItemStack item = plugin.getCustomItemManager().getItem(key);
			if(item == null){
				p.sendMessage(ChatColor.RED + "An item with that key was not found!");
				return true;
			}
			p.getInventory().addItem(item);
			p.sendMessage(ChatColor.GREEN + "Successfully retrieved that item!");
		}
		else {
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
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure addItem {key} - &2Add an item to the custom items list."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure checkKey - &2Get the key of an item you are holding in your hand."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure getItem {key} - &2Get the item of the key specified."));
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
			}
		}

		return false;
	}

}
