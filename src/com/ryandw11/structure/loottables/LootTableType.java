package com.ryandw11.structure.loottables;

import org.bukkit.Material;

/**
 * The type of a LootTable.
 */
public enum LootTableType {
	CHEST(Material.CHEST),
	FURNACE(Material.FURNACE),
	HOPPER(Material.HOPPER),
	BREWING_STAND(Material.BREWING_STAND),
	BARREL(Material.BARREL),
	TRAPPED_CHEST(Material.TRAPPED_CHEST);

	private Material material;
	LootTableType(Material material){
		this.material = material;
	}

	public Material getMaterial(){
		return material;
	}

}
