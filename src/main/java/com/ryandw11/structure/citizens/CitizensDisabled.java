package com.ryandw11.structure.citizens;

import com.ryandw11.structure.NamesHandler;
import com.ryandw11.structure.NpcHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Dummy implementation for when the "Citizen" plugin is not available.
 *
 * @author Marcel Schoen
 */
public class CitizensDisabled implements CitizensNpcHook {

	@Override
	public void spawnNpc(NpcHandler npcHandler, NamesHandler namesHandler, String alias, Location loc) {
		Bukkit.getLogger().info("A schematic tried to spawn a Citizen NPC, but the server does not have that plugin installed!");
	}
}
