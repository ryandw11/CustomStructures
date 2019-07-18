package com.ryandw11.structure.api;

public enum ConditionType {
	BIOME("Biome"),
	WORLD("AllowedWorlds"),
	INAIR("PlaceAir"),
	INLIQUID("spawnInLiquid"),
	SPAWNY("SpawnY"),
	LOOTTABLES("LootTables");
	
	private String configSel;
	ConditionType(String configSel){
		this.configSel = configSel;
	}
	
	public String getConfigSel() {
		return configSel;
	}
}
