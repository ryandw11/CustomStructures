package com.ryandw11.structure.api;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.loottables.LootTablesHandler;
import com.ryandw11.structure.loottables.customitems.CustomItemManager;
import com.ryandw11.structure.structure.StructureHandler;

/**
 * The class for the general API of CustomStructures.
 *
 * <p>This class is used to access the entire API of the plugin. From here you can access the various
 * handlers that the plugin uses.</p>
 */
public class CustomStructuresAPI {

    private final CustomStructures plugin;

    public CustomStructuresAPI() {
        this.plugin = CustomStructures.plugin;
    }

    /**
     * Get the number of structures.
     *
     * @return The number of structures.
     */
    public int getNumberOfStructures() {
        return getStructureHandler().getStructures().size();
    }

    /**
     * Get the structure handler.
     *
     * @return The structure handler.
     */
    public StructureHandler getStructureHandler() {
        return plugin.getStructureHandler();
    }

    /**
     * Get the loot table handler.
     *
     * @return The loot table handler.
     */
    public LootTablesHandler getLootTableHandler() {
        return plugin.getLootTableHandler();
    }

    /**
     * Get the custom item manager.
     *
     * @return The custom item manager.
     */
    public CustomItemManager getCustomItemManager() {
        return plugin.getCustomItemManager();
    }

    /**
     * Get the schematics folder.
     *
     * @return The schematics folder.
     */
    public String getSchematicsFolder() {
        return plugin.getDataFolder() + "/schematics/";
    }

    /**
     * Get if structures can spawn in the void.
     *
     * <p>This setting is set by the user in the config file.</p>
     *
     * @return Get if structures can spawn in the void.
     */
    public boolean isVoidSpawningEnabled() {
        return plugin.getConfig().contains("spawnInVoid") && plugin.getConfig().getBoolean("spawnInVoid");
    }
}
