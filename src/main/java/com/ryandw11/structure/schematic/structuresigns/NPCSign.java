package com.ryandw11.structure.schematic.structuresigns;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.StructureSign;
import com.ryandw11.structure.structure.Structure;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * The NPC Structure Sign.
 *
 * <p>This structure sign is registered in the {@link com.ryandw11.structure.schematic.StructureSignHandler} constructor.</p>
 */
public class NPCSign extends StructureSign {

    @Override
    public boolean onStructureSpawn(@NotNull Location location, @NotNull Structure structure) {
        CustomStructures plugin = CustomStructures.getInstance();
        if (!hasArgument(0)) {
            plugin.getLogger().warning(String.format("Invalid NPC on structure sign. (%s)", structure.getName()));
            return true;
        }

        plugin.getCitizensNpcHook().spawnNpc(plugin.getNpcHandler(), getStringArgument(0), location);
        return true;
    }

}
