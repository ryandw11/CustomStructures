package com.ryandw11.structure.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import com.ryandw11.structure.CustomStructures;

public class CheckSchematics extends BukkitRunnable{
	private int num;
	ArrayList<String> schematics;
	private CustomStructures plugin;
	String ex = "Schematics.";
	public CheckSchematics(Set<String> schematics){
		plugin = CustomStructures.plugin;
		num = 0;
		ArrayList<String> ls = new ArrayList<>();
		for(String s : schematics) {
			ls.add(s);
		}
		this.schematics = ls;
	}

	@Override
	public void run() {
		if(num >= schematics.size()){
			plugin.getLogger().info("The schematics have been checked! All seems to be in order!");
			this.cancel();
			return;
		}
		String s = schematics.get(num);
		
		File schematic = new File(plugin.getDataFolder() + "/schematics/" + plugin.getConfig().getString(ex + s + ".Schematic"));
		if(!schematic.exists()){
			plugin.getLogger().severe("Error: The schematic file for " + s + " does not exist!");
			plugin.getLogger().severe("For assistance please contact Ryandw11 on spigot.");
			plugin.getLogger().severe("The plugin will now disable it self.");
			this.cancel();
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
		num += 1;
	}

}
