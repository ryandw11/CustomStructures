package com.ryandw11.structure.api;

import java.util.Arrays;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import com.ryandw11.structure.CustomStructures;

public class CustomStructuresAPI {
	public CustomStructures getMainInstance() {
		return CustomStructures.plugin;
	}
	
	/**
	 * Get the number of structures.
	 * @return The number of structures.
	 */
	public int getNumberOfStructures() {
		Set<String> cs = this.getMainInstance().getConfig().getConfigurationSection("Schematics").getKeys(false);
		return cs.size();
	}
	
	/**
	 * Create a new structure.
	 * <p><b>Note: The Schematic file must already be inside the plugin schematic folder!</b></p>
	 * @param name The name of the strucuture
	 * @param schematic The name of the schematic file.
	 * @return The structure that was created.
	 */
	public Structure createStructure(String name, String schematic) {
		ConfigurationSection cs = this.getMainInstance().getConfig().getConfigurationSection("Schematics." + name);
		cs.set("Schematic", schematic);
		cs.set("Biome", "all");
		cs.set("Chance.Number", 1);
		cs.set("Chance.OutOf", 1000);
		cs.set("AllWorlds", true);
		cs.set("SpawnY", -1);
		cs.set("PlaceAir", true);
		cs.set("spawnInLiquid", false);
		cs.set("AllowedWorlds", Arrays.asList("world"));
		
		return new Structure(name);
	}
	
	/**
	 * Delete a Structure.
	 * @param name The name of the structure.
	 * @throws DeletionForbiddenException If the server has disabled plugins removing structures then this is thrown.
	 */
	public void deleteStructure(String name) throws DeletionForbiddenException {
		if(this.getMainInstance().getConfig().getBoolean("allowDeletion")) {
			this.getMainInstance().getConfig().set("Schematics." + name, null);
		}else {
			this.getMainInstance().getLogger().warning("A plugin has tried to delete a structure! To allow it to delete schematics, change the config option.");
			throw new DeletionForbiddenException("This plugin is not allowed to delete structures!");
		}
	}
}
