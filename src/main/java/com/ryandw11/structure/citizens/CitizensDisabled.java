package com.ryandw11.structure.citizens;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.NpcHandler;
import org.bukkit.Location;

/**
 * Dummy implementation for when the "Citizen" plugin is not available.
 */
public class CitizensDisabled implements CitizensNpcHook {

    /**
     * {@inheritDoc}
     */
    @Override
    public void spawnNpc(NpcHandler npcHandler, String alias, Location loc) {
        CustomStructures.plugin.getLogger().info("A schematic tried to spawn a Citizen NPC, but the server does not have that plugin installed!");
    }
}
