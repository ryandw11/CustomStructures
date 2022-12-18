package com.ryandw11.structure.schematic.structuresigns;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.StructureSign;
import com.ryandw11.structure.structure.Structure;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * The Mythic Mob Structure Sign.
 *
 * <p>This structure sign is registered in the {@link com.ryandw11.structure.schematic.StructureSignHandler} constructor.</p>
 */
public class MythicMobSign extends StructureSign {

    private final CustomStructures plugin;

    public MythicMobSign() {
        plugin = CustomStructures.getInstance();
    }

    @Override
    public boolean onStructureSpawn(@NotNull Location location, @NotNull Structure structure) {
        if (!hasArgument(0)) {
            plugin.getLogger().warning(String.format("Invalid mythic mob type on a structure sign! (%s)", structure.getName()));
            return true;
        }

        String mythicMob = getStringArgument(0);
        int count = 1;
        if (hasArgument(2)) {
            // Impose a maximum limit of 40 mobs.
            count = Math.min(getStylizedIntArgument(1), 40);
        }

        if (!hasArgument(1)) {
            plugin.getMythicalMobHook().spawnMob(mythicMob, location, count);
        } else {
            plugin.getMythicalMobHook().spawnMob(mythicMob, location, getStylizedIntArgument(1), count);
        }

        return true;
    }

}
