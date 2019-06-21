package com.ryandw11.structure.api;

import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.SchematicHandeler;
import com.sk89q.worldedit.WorldEditException;

public class Structure {
	private String name;
	private ConfigurationSection cs;
	
	public Structure(String name) {
		this.name = name;
		cs = CustomStructures.plugin.getConfig().getConfigurationSection("Schematics." + name);
	}
	
	public String getName() {
		return name;
	}
	
	/**
	 * Get a Condition
	 * @param ct - The Condition wanted
	 * @return An object based upon the ConditionType entered and what it is set to in the config.
	 */
	public Object getCondition(ConditionType ct) {
		switch(ct) {
		case BIOME:
			return cs.getString("Biome");
		case WORLD:
			if(cs.getBoolean("AllWorlds"))
				return "all";
			else
				return cs.getStringList("AllowedWorlds");
		case INAIR:
			return cs.getBoolean("PlaceAir");
		case INLIQUID:
			return cs.getBoolean("spawnInLiquid");
		case SPAWNY:
			return cs.getInt("SpawnY");
		default:
			return null;
		}
	}
	
	/**
	 * Set a condition
	 * @param ct - The condition wanted
	 * @param set - The Object it is set to.
	 */
	public void setCondition(ConditionType ct, Object set) {
		cs.set(ct.getConfigSel(), set);
	}
	
	/**
	 * Spawn the structure in. (Ignores the Biome, Liquid, SpawnY, and World conditions.)
	 * @param loc The location where the structure should be spawned.
	 * @throws IOException
	 * @throws WorldEditException
	 */
	public void spawnStructure(Location loc) throws IOException, WorldEditException {
		SchematicHandeler sh = new SchematicHandeler();
		sh.schemHandle(loc, name, (boolean) this.getCondition(ConditionType.INAIR));
	}
}
