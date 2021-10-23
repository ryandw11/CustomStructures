package com.ryandw11.structure.citizens;

import com.ryandw11.structure.NamesHandler;
import com.ryandw11.structure.NpcHandler;
import org.bukkit.Location;

/**
 * Interface for "Citizen" integration.
 *
 * @author Marcel Schoen
 */
public interface CitizensNpcHook {

	/**
	 * Spawns a Citizen NPC.
	 *
	 * @param npcHandler The handler for the NPC config.
	 * @param namesHandler The handler for generating names.
	 * @param alias The alias of the NPC in the "npc" configuration file.
	 * @param loc The location where to spawn the NPC.
	 */
	void spawnNpc(NpcHandler npcHandler, NamesHandler namesHandler, String alias, Location loc);
}
