package com.ryandw11.structure.schematic.structuresigns;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.StructureSign;
import com.ryandw11.structure.structure.Structure;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The Mob Structure Sign.
 *
 * <p>This structure sign is registered in the {@link com.ryandw11.structure.schematic.StructureSignHandler} constructor.</p>
 */
public class MobSign extends StructureSign {

    private final CustomStructures plugin;

    public MobSign() {
        plugin = CustomStructures.getInstance();
    }

    @Override
    public boolean onStructureSpawn(@NotNull Location location, @NotNull Structure structure) {
        if (!hasArgument(0)) {
            plugin.getLogger().warning(String.format("Invalid mob type on a structure sign! (%s)", structure.getName()));
            return true;
        }

        String mobName = getStringArgument(0).toUpperCase();
        int count = 1;
        if (hasArgument(1)) {
            // Impose a maximum limit of 40 mobs.
            count = Math.min(getStylizedIntArgument(1), 40);
        }

        try {
            for (int i = 0; i < count; i++) {
                Entity ent = Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.valueOf(mobName));
                if (ent instanceof LivingEntity livingEntity) {
                    livingEntity.setRemoveWhenFarAway(false);
                }
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning(String.format("Invalid mob type on a structure sign! (%s)", structure.getName()));
        }

        return true;
    }

}
