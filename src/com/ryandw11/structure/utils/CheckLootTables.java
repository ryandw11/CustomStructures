package com.ryandw11.structure.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import com.ryandw11.structure.CustomStructures;

public class CheckLootTables extends BukkitRunnable {
	private int num;
	private ArrayList<String> schematics;
	private CustomStructures plugin;
	private String ex = "Schematics.";

	public CheckLootTables(Set<String> schematics) {
		plugin = CustomStructures.plugin;
		num = 0;
		ArrayList<String> ls = new ArrayList<>();
		for (String s : schematics) {
			ls.add(s);
		}
		this.schematics = ls;
	}

	@Override
	public void run() {
		if (num >= schematics.size()) {
			plugin.getLogger().info("The lootTables have been checked! All seems to be in order!");
			this.cancel();
			return;
		}
		String s = schematics.get(num);
		ConfigurationSection lootTablesCS = plugin.getConfig().getConfigurationSection(ex + s + ".LootTables");
		if (lootTablesCS != null) {
			for (String name : lootTablesCS.getKeys(true)) {
				File schematic = new File(plugin.getDataFolder() + "/lootTables/" + name + ".yml");
				if (!schematic.exists()) {
					plugin.getLogger().severe("Error: The Loot Table file for " + name + " does not exist!");
					plugin.getLogger().severe("For assistance please contact Ryandw11 on spigot.");
					plugin.getLogger().severe("The plugin will now disable it self.");
					this.cancel();
					Bukkit.getPluginManager().disablePlugin(plugin);
				}
			}
		}
		num += 1;

	}

}
