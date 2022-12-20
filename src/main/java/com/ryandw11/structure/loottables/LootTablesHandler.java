package com.ryandw11.structure.loottables;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.CustomStructuresAPI;
import com.ryandw11.structure.exceptions.LootTableException;

import java.util.*;

/**
 * This handles the loot tables.
 *
 * <p>Get this handler via {@link CustomStructuresAPI#getLootTableHandler()}.</p>
 */
public class LootTablesHandler {

    private final Map<String, LootTable> lootTables;

    public LootTablesHandler() {
        this.lootTables = new HashMap<>();
    }

    /**
     * Add a loot table to the LootTableHandler.
     *
     * <p>Addons should be using
     * {@link com.ryandw11.structure.api.structaddon.CustomStructureAddon#registerLootTable(LootTable)}
     * to add custom LootTables since the LootTableHandler is reset every time the plugin is reloaded.</p>
     *
     * @param lootTable The loot table to add.
     * @return If the loot table was added successfully.
     */
    public boolean addLootTable(LootTable lootTable) {
        if (lootTables.containsKey(lootTable.getName()))
            return false;

        this.lootTables.put(lootTable.getName(), lootTable);
        return true;
    }

    /**
     * Get the loot table by the name.
     * <p>This will automatically load a loot table</p>
     * <p>If the name starts with <code>minecraft:</code>, it will try to load a minecraft loot table.</p>
     *
     * @param lootTableName The name of the loot table.
     * @return The loot table. This will return null if the loot table does not exist or loads with an error.
     */
    public LootTable getLootTableByName(String lootTableName) {
        if (!this.lootTables.containsKey(lootTableName)) {
            try {
                // Support minecraft loot tables.
                if (lootTableName.contains(":")) {
                    this.lootTables.put(lootTableName, new MinecraftLootTable(lootTableName));
                } else {
                    this.lootTables.put(lootTableName, new ConfigLootTable(lootTableName));
                }
            } catch (LootTableException ex) {
                CustomStructures.getInstance().getLogger().severe("There seems to be a problem with the '" +
                        lootTableName + "' loot table:");
                CustomStructures.getInstance().getLogger().severe(ex.getMessage());
            }
        }
        return this.lootTables.get(lootTableName);
    }

    /**
     * Get the map of loot tables.
     *
     * <p>This returns an unmodifiable map.</p>
     *
     * @return The unmodifiable map of loot tables.
     */
    public Map<String, LootTable> getLootTables() {
        return Collections.unmodifiableMap(lootTables);
    }

    /**
     * Get a list with the names of all loot tables.
     *
     * @return The list with names of all loot tables.
     */
    public List<String> getLootTablesNames() {
        return new ArrayList<>(this.lootTables.keySet());
    }
}
