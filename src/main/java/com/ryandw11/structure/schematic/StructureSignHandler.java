package com.ryandw11.structure.schematic;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.StructureSign;
import com.ryandw11.structure.schematic.structuresigns.CommandSign;
import com.ryandw11.structure.schematic.structuresigns.MobSign;
import com.ryandw11.structure.schematic.structuresigns.MythicMobSign;
import com.ryandw11.structure.schematic.structuresigns.NPCSign;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This handles the registration of structure signs.
 *
 * <p>Get the instance of this handler from {@link CustomStructures#getStructureSignHandler()}.</p>
 */
public class StructureSignHandler {
    private final Map<String, Class<? extends StructureSign>> structureSigns;

    /**
     * Construct the structure sign handler.
     *
     * <p>The plugin's default structure signs are added automatically.</p>
     *
     * <p>Internal Use Only.</p>
     */
    public StructureSignHandler() {
        this.structureSigns = new HashMap<>();

        // Register signs that come default with the plugin.
        registerStructureSign("mob", MobSign.class);
        registerStructureSign("npc", NPCSign.class);
        registerStructureSign("command", CommandSign.class);
        registerStructureSign("commands", CommandSign.class);
        registerStructureSign("mythicmob", MythicMobSign.class);
        registerStructureSign("mythicalmob", MythicMobSign.class);
    }

    /**
     * Register a structure sign with the plugin.
     *
     * @param name               The name of the sign (Not including the brackets []). This is what the user
     *                           will reference it as on the first line of the sign.
     * @param structureSignClass The class of the structure sign to register.
     * @return If the sign was successfully registered. If false is returned, then a sign with that name already exists.
     */
    public boolean registerStructureSign(@NotNull String name, @NotNull Class<? extends StructureSign> structureSignClass) {
        if (structureSigns.containsKey(name.toUpperCase())) return false;

        // Discourage a plugin from overriding the sub-schematic functionality.
        if (name.equalsIgnoreCase("schem") ||
                name.equalsIgnoreCase("schematic") ||
                name.equalsIgnoreCase("advschem")) {
            return false;
        }

        structureSigns.put(name.toUpperCase(), structureSignClass);
        return true;
    }

    /**
     * Get a map of the registered structure signs.
     *
     * @return A map of the registered structure signs.
     */
    public Map<String, Class<? extends StructureSign>> getRegisteredStructureSigns() {
        return this.structureSigns;
    }

    /**
     * Get a structure sign using its name.
     *
     * @param name The name of the structure sign.
     * @return The structure sign class. (Null if it does not exist).
     */
    public Class<? extends StructureSign> getStructureSign(@NotNull String name) {
        return this.structureSigns.get(name.toUpperCase());
    }

    /**
     * Check if a structure sign exists.
     *
     * @param name The name to check.
     * @return If it exists.
     */
    public boolean structureSignExists(@NotNull String name) {
        return this.structureSigns.containsKey(name.toUpperCase());
    }
}
