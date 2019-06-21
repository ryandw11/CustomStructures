package com.ryandw11.structure.commands;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.SchematicHandeler;
import com.sk89q.worldedit.WorldEditException;

public class SCommand implements CommandExecutor {
	private CustomStructures plugin;
	public SCommand(){
		plugin = CustomStructures.plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
			if(sender.hasPermission("customstructures.reload")){
				plugin.reloadConfig();
				sender.sendMessage("The plugin has been reloaded!");
				plugin.getLogger().info("Plugin reloaded!");
			}else{
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
			}
		}else if(args.length == 2 && args[0].equalsIgnoreCase("test")){
			if(!(sender instanceof Player)){
				sender.sendMessage("This command is for players only!");
				return true;
			}
			Player p = (Player) sender;
			if(!p.hasPermission("customstructures.test")){
				p.sendMessage(ChatColor.RED + "You do not have permission for this command.");
				return true;
			}
			if(!plugin.getConfig().contains("Schematics." + args[1])){
				p.sendMessage(ChatColor.RED + "That schematic does not exist!");
			}
			SchematicHandeler sh = new SchematicHandeler();
			try {
				sh.schemHandle(p.getLocation(), plugin.getConfig().getString("Schematics." + args[1] + ".Schematic"), plugin.getConfig().getBoolean("Schematics." + s + ".PlaceAir"));
			} catch (IOException | WorldEditException e) {
				e.printStackTrace();
			}
		}else if(args.length == 1 && args[0].equalsIgnoreCase("list")){
			sender.sendMessage(ChatColor.GREEN + "Currently Active Schematics:");
			for(String st : plugin.getConfig().getConfigurationSection("Schematics").getKeys(false)){
				sender.sendMessage(ChatColor.GREEN + " - " + ChatColor.BLUE + st);
			}
		}
		else{
			if(sender.hasPermission("customstructures.info")){
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3=============[&2CustomStructures&3]============="));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3Created by: &2Ryandw11"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3Version: &2" + plugin.getDescription().getVersion()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3Github wiki:&2 https://github.com/ryandw11/CustomStructures/wiki"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3Commands:"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3/cs reload - &2Reload the plugin."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3/cs test (name) - &2Paste the defined structure."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3/cs list - &2List the currently active structures."));
			}else{
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
			}
		}
		
		return false;
	}

}
