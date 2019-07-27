package com.ryandw11.structure.commands;

import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.SchematicHandeler;
import com.ryandw11.structure.loottables.LootTablesHandler;
import com.ryandw11.structure.utils.CheckLootTables;
import com.ryandw11.structure.utils.CheckSchematics;
import com.ryandw11.structure.utils.RandomCollection;
import com.sk89q.worldedit.WorldEditException;

public class SCommand implements CommandExecutor {
	private CustomStructures plugin;

	public SCommand() {
		plugin = CustomStructures.plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("customstructures.reload")) {
				plugin.reloadConfig();
				sender.sendMessage("The plugin has been reloaded!");
				plugin.getLogger().info("Plugin reloaded!");
				
				CheckSchematics cs = new CheckSchematics(plugin.getConfig().getConfigurationSection("Schematics").getKeys(false));
				cs.runTaskTimer(plugin, 5L, 1L);
				plugin.setStructures();

				CheckLootTables cl = new CheckLootTables(plugin.getConfig().getConfigurationSection("Schematics").getKeys(false));
				cl.runTaskTimer(plugin, 5L, 1L);

				CustomStructures.lootTablesHandler = new LootTablesHandler();
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
			if (!plugin.getConfig().contains("Schematics." + args[1])) {
				p.sendMessage(ChatColor.RED + "That schematic does not exist!");
			}
			SchematicHandeler sh = new SchematicHandeler();
			try {
				RandomCollection<String> lootTables = new RandomCollection<>();
				ConfigurationSection lootTablesCS = plugin.getConfig()
						.getConfigurationSection("Schematics." + args[1] + ".LootTables");
				if (lootTablesCS != null) {
					for (String name : lootTablesCS.getKeys(true)) {
						int weight = plugin.getConfig().getInt("Schematics." + args[1] + ".LootTables." + name);
						lootTables.add(weight, name);
					}
				}

				sh.schemHandle(p.getLocation(), plugin.getConfig().getString("Schematics." + args[1] + ".Schematic"),
						plugin.getConfig().getBoolean("Schematics." + s + ".PlaceAir"), lootTables, 
						plugin.getConfig().getConfigurationSection("Schematics." + args[1]));
				
			} catch (IOException | WorldEditException e) {
				e.printStackTrace();
			}
		} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			sender.sendMessage(ChatColor.GREEN + "Currently Active Schematics:");
			for (String st : plugin.getConfig().getConfigurationSection("Schematics").getKeys(false)) {
				sender.sendMessage(ChatColor.GREEN + " - " + ChatColor.BLUE + st);
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
