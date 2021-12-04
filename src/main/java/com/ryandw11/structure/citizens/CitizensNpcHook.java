package com.ryandw11.structure.citizens;

import com.ryandw11.structure.NpcHandler;
import org.bukkit.Location;

/**
 * Interface for "Citizen" integration.
 */
public interface CitizensNpcHook {

	/**
	 * Spawns a Citizen NPC.
	 *
	 * @param npcHandler The handler for the NPC config.
	 * @param alias The alias of the NPC in the "npc" configuration file.
	 * @param loc The location where to spawn the NPC.
	 */
	void spawnNpc(NpcHandler npcHandler, String alias, Location loc);
}
