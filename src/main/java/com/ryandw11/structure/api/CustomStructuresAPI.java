package com.ryandw11.structure.api;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.CustomStructureAddon;
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

    /**
     * Construct the CustomStructuresAPI class.
     *
     * <p>This is how you obtain the CustomStructureAPI class. Nothing special needs to be done
     * other than use this constructor.</p>
     *
     * @throws IllegalStateException If CustomStructures has yet to be initialized.
     */
    public CustomStructuresAPI() {
        if (CustomStructures.plugin == null)
            throw new IllegalStateException("CustomStructures has yet to be initialized.");

        this.plugin = CustomStructures.plugin;
    }

    /**
     * Register an addon to the Custom Structure plugin.
     *
     * <p>This is used to add custom configuration sections and inform the plugin
     * that your plugin interfaces with it. This is not required to use
     * any of the events.</p>
     *
     * <p>This should be called in the plugin's onEnable method. Ensure that you add CustomStructures
     * as a dependency in your plugin.yml file so that way the plugin loads first and the addon
     * system can be initialized. An error may occur if your plugin loads first.</p>
     *
     * @param customStructureAddon The addon to register.
     */
    public void registerCustomAddon(CustomStructureAddon customStructureAddon) {
        if (plugin.getAddonHandler() == null)
            throw new IllegalStateException("The addon system has not been initialized yet. Please add CustomStructures " +
                    "as a dependency in your plugin.yml file.");
        plugin.getAddonHandler().registerAddon(customStructureAddon);
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
     * <p>This is not initialized until after all plugins are loaded.</p>
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
     * @deprecated Void spawning is now always enabled.
     *
     * @return Get if structures can spawn in the void.
     */
    @Deprecated
    public boolean isVoidSpawningEnabled() {
        return true;
    }
}
