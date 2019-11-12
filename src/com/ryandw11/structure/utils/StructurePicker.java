package com.ryandw11.structure.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.SchematicHandeler;
import com.ryandw11.structure.api.CustomStructuresAPI;
import com.sk89q.worldedit.WorldEditException;

/**
 * This class prevents the server from crashing when it attempts to pick a
 * structure.
 * <p>
 * The server will still lag a bit thanks to the nature of 1.14.
 * </p>
 * 
 * @author Ryandw11
 *
 */
public class StructurePicker extends BukkitRunnable {

	private CustomStructures plugin;
	private CustomStructuresAPI api;

	private Random r;
	private int count;
	private String currentSchem;
	private int numberOfSchem;

	private Block bl;
	private Chunk ch;

	public StructurePicker(Block bl, Chunk ch) {
		this.plugin = CustomStructures.plugin;
		r = new Random();
		count = 0;
		api = new CustomStructuresAPI();
		numberOfSchem = api.getNumberOfStructures();
		this.bl = bl;
		this.ch = ch;
	}

	@Override
	public void run() {
		if (count >= numberOfSchem) {
			this.cancel();
			return;
		}

		currentSchem = plugin.structures.get(count);

		// Calculate the chance.
		int num = r.nextInt(plugin.getConfig().getInt("Schematics." + currentSchem + ".Chance.OutOf") - 1) + 1;
		if (num <= plugin.getConfig().getInt("Schematics." + currentSchem + ".Chance.Number")) {
			if (!plugin.getConfig().getBoolean("Schematics." + currentSchem + ".AllWorlds")) { // Checking to see if the
																								// world is correct
				ArrayList<String> worlds = (ArrayList<String>) plugin.getConfig()
						.getStringList("Schematics." + currentSchem + ".AllowedWorlds");
				if (!worlds.contains(bl.getWorld().getName()))
					return;
			}
			
			

			ConfigurationSection cs = plugin.getConfig().getConfigurationSection("Schematics." + currentSchem);
			
			// Allows the structure to spawn based on the ocean floor. (If the floor is not found than it just returns with the top of the water).
			if(cs.getBoolean("Ocean_Properties.useOceanFloor")) {
				if(bl.getType() == Material.WATER) {
					for(int i = bl.getY(); i <= 4; i--) {
						if(ch.getBlock(0, i, 0).getType() != Material.WATER) {
							bl = ch.getBlock(0, i, 0);
							break;
						}
					}
				}
			}
			
			// Allows the structures to no longer spawn on plant life.
			if(cs.getBoolean("ignorePlants") && CSConstants.leafBlocks.contains(bl.getType())) {
				for(int i = bl.getY(); i <= 4; i--) {
					if(!CSConstants.leafBlocks.contains(ch.getBlock(0, i, 0).getType())) {
						bl = ch.getBlock(0, i, 0);
						break;
					}
				}
			}
			
			//If it can spawn in a boime.
			if (!plugin.getConfig().getString("Schematics." + currentSchem + ".Biome").equalsIgnoreCase("all")) {// Checking
																													// biome
				if (!getBiomes(plugin.getConfig().getString("Schematics." + currentSchem + ".Biome").toLowerCase())
						.contains(bl.getBiome().toString().toLowerCase()))
					return;
			}
			// calculate spawny
			if(cs.contains("SpawnY")) {
				bl = ch.getBlock(0, HandleY.calculateY(cs, bl.getY()), 0);
			}
			if(cs.contains("whitelistSpawnBlocks")) {
				List<Material> materials = new ArrayList<>();
				for(String s : cs.getStringList("whitelistSpawnBlocks")) {
					try {
						materials.add(Material.valueOf(s.toUpperCase()));
					}
					catch (Exception e) {
						continue;
					}
				}
				
				// If the material is not whitelisted.
				if(!materials.contains(bl.getType())) {
					return;
				}
			}
			// If it can spawn in a liquid
			if (!cs.getBoolean("Ocean_Properties.spawnInLiquid")) {
				if (bl.getType() == Material.WATER || bl.getType() == Material.LAVA)
					return;
			}
			// Now to finally paste the schematic
			SchematicHandeler sh = new SchematicHandeler();
			try {
				RandomCollection<String> lootTables = new RandomCollection<>();
				ConfigurationSection lootTablesCS = cs.getConfigurationSection("LootTables");
				if (lootTablesCS != null) {
					for (String name : lootTablesCS.getKeys(true)) {
						int weight = cs.getInt("LootTables." + name);
						lootTables.add(weight, name);
					}
				}
				// Line to actualy paste the schematic.
				sh.schemHandle(bl.getLocation(),
						plugin.getConfig().getString("Schematics." + currentSchem + ".Schematic"),
						plugin.getConfig().getBoolean("Schematics." + currentSchem + ".PlaceAir"), lootTables, cs);
			} catch (IOException | WorldEditException e) {
				e.printStackTrace();
			}
			this.cancel();// return after pasting
		}

		count++;
	}

	protected ArrayList<String> getBiomes(String s) {
		String[] biomes = s.split(",");
		ArrayList<String> output = new ArrayList<String>();
		for (String b : biomes)
			output.add(b);
		return output;
	}

}
