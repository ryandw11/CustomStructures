package com.ryandw11.structure.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
//import org.bukkit.block.Chest;
//import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.SchematicHandeler;
import com.sk89q.worldedit.WorldEditException;

/**
 * 
 * @author Ryandw11
 * @version 1.3
 * @deprecated Outdated. To be removed in future update.
 *
 */
public class Structures {
	private CustomStructures plugin;
	private ArrayList<String> st;
	private Random r;

	public Structures() {
		this.plugin = CustomStructures.plugin;
		// Get all of the strings for the structures.
		ArrayList<String> ls = new ArrayList<String>();
		for (String s : plugin.getConfig().getConfigurationSection("Schematics").getKeys(false))
			ls.add(s);
		st = ls;
		r = new Random();
	}

	/**
	 * Does all the schematic calculations for the ChunkLoad class.
	 * 
	 * @param bl Block
	 * @param ch Chunk
	 */
	public void chooseBestStructure(Block bl, Chunk ch) {
		int num;
		for (String s : st) {
			num = r.nextInt(plugin.getConfig().getInt("Schematics." + s + ".Chance.OutOf") - 1) + 1;
			if (num <= plugin.getConfig().getInt("Schematics." + s + ".Chance.Number")) {
				if (!plugin.getConfig().getBoolean("Schematics." + s + ".AllWorlds")) { // Checking to see if the world
																						// is correct
					@SuppressWarnings("unchecked")
					ArrayList<String> worlds = (ArrayList<String>) plugin.getConfig()
							.get("Schematics." + s + ".AllowedWorlds");
					if (!worlds.contains(bl.getWorld().getName()))
						return;
				}

				ConfigurationSection cs = plugin.getConfig().getConfigurationSection("Schematics." + s);

				if (!plugin.getConfig().getString("Schematics." + s + ".Biome").equalsIgnoreCase("all")) {// Checking
																											// biome
					if (!getBiomes(plugin.getConfig().getString("Schematics." + s + ".Biome").toLowerCase())
							.contains(bl.getBiome().toString().toLowerCase()))
						return;
				}
				if (cs.getInt("SpawnY") < -1) {
					bl = ch.getBlock(0, (bl.getY() + plugin.getConfig().getInt("Schematics." + s + ".SpawnY")), 0);
				} else if (cs.contains("SpawnY") && cs.getInt("SpawnY") != -1) {
					bl = ch.getBlock(0, cs.getInt("SpawnY"), 0);
				}
				if (!cs.getBoolean("spawnInLiquid")) {
					if (bl.getType() == Material.WATER || bl.getType() == Material.LAVA)
						return;
				}
				// Now to finally paste the schematic
				SchematicHandeler sh = new SchematicHandeler();
				try {
					RandomCollection<String> lootTables = new RandomCollection<>();

					for (String name : cs.getConfigurationSection("LootTables").getKeys(true)) {
						int weight = cs.getInt("LootTables." + name);
						lootTables.add(weight, name);
					}

					sh.schemHandle(bl.getLocation(), plugin.getConfig().getString("Schematics." + s + ".Schematic"),
							plugin.getConfig().getBoolean("Schematics." + s + ".PlaceAir"), lootTables);
				} catch (IOException | WorldEditException e) {
					e.printStackTrace();
				}
				return;// return after pasting
			}
		}
		return; // If no schematic is able to spawn.

	}

	protected ArrayList<String> getMyLore(java.util.List<String> list) {
		ArrayList<String> ls = new ArrayList<String>();
		for (String lore : list) {
			ls.add(ChatColor.translateAlternateColorCodes('&', lore));
		}
		return ls;
	}

	protected ArrayList<String> getBiomes(String s) {
		String[] biomes = s.split(",");
		ArrayList<String> output = new ArrayList<String>();
		for (String b : biomes)
			output.add(b);
		return output;

	}

}
