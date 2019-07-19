package com.ryandw11.structure.loottables;

import java.util.HashMap;
import java.util.Map;

public class LootTablesHandler {

	private Map<String, LootTable> lootTables;

	public LootTablesHandler() {
		this.lootTables = new HashMap<>();
	}

	public LootTable getLootTableByName(String lootTableName) {
		if (!this.lootTables.containsKey(lootTableName)) {
			this.lootTables.put(lootTableName, new LootTable(lootTableName));
		}
		return this.lootTables.get(lootTableName);
	}
}
