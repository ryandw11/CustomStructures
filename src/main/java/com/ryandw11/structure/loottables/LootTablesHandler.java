package com.ryandw11.structure.loottables;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.CustomStructuresAPI;
import com.ryandw11.structure.exceptions.LootTableException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
     * Get the loot table by the name.
     * <p>This will automatically load a loot table</p>
     *
     * @param lootTableName The name of the loot table.
     * @return The loot table. This will return null if the loot table does not exist or loads with an error.
     */
    public LootTable getLootTableByName(String lootTableName) {
        if (!this.lootTables.containsKey(lootTableName)) {
            try {
                this.lootTables.put(lootTableName, new LootTable(lootTableName));
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
}
