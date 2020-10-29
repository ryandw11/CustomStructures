package com.ryandw11.structure.loottables;

import com.ryandw11.structure.CustomStructures;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Chusca
 */
public class LootTablesHandler {

    private Map<String, LootTable> lootTables;

    public LootTablesHandler() {
        this.lootTables = new HashMap<>();
    }

    public LootTable getLootTableByName(String lootTableName) {
        if (!this.lootTables.containsKey(lootTableName)) {
            try {
                this.lootTables.put(lootTableName, new LootTable(lootTableName));
            } catch (RuntimeException ex) {
                CustomStructures.getInstance().getLogger().severe("The following error has occurred when enabling the " +
                        lootTableName + " loot table:");
                CustomStructures.getInstance().getLogger().severe(ex.getMessage());

            }
        }
        return this.lootTables.get(lootTableName);
    }
}
