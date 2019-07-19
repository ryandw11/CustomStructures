package com.ryandw11.structure.loottables;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.utils.RandomCollection;

public class LootTable {

	private LootTableType type;
	private int rolls;
	private RandomCollection<LootItem> randomCollection;

	public FileConfiguration lootTablesFC;

	public LootTable(String name) {
		this.LoadFile(name);

		this.type = LootTableType.valueOf(this.lootTablesFC.getString("Type"));
		this.rolls = this.lootTablesFC.getInt("Rolls");

		
		this.loadItems();
	}

	private void loadItems() {

		this.randomCollection = new RandomCollection<LootItem>();
		
		for (String itemID : this.lootTablesFC.getConfigurationSection("Items").getKeys(false)) {

			String customName = this.lootTablesFC.getString("Items." + itemID + ".Name");
			String type = this.lootTablesFC.getString("Items." + itemID + ".Type");
			int amount = this.lootTablesFC.getInt("Items." + itemID + ".Amount");
			int weight = this.lootTablesFC.getInt("Items." + itemID + ".Weight");
			Map<String, Integer> enchants = new HashMap<>();

			ConfigurationSection enchantMents = this.lootTablesFC
					.getConfigurationSection("Items." + itemID + ".Enchantments");

			if (enchantMents != null) {
				for (String enchantName : enchantMents.getKeys(false)) {
					int level = this.lootTablesFC.getInt("Items." + itemID + ".Enchantments." + enchantName);
					enchants.put(enchantName, level);
				}
			}

			this.randomCollection.add(weight, new LootItem(customName, type, amount, weight, enchants));
		}

	}

	private void LoadFile(String name) {
		File lootTablesfile = new File(CustomStructures.plugin.getDataFolder() + "/lootTables/" + name + ".yml");
		this.lootTablesFC = YamlConfiguration.loadConfiguration(lootTablesfile);

		try {
			lootTablesFC.load(lootTablesfile);

		} catch (IOException | InvalidConfigurationException e) {

			e.printStackTrace();
		}
	}

	public LootTableType getType() {
		return type;
	}

	public void setType(LootTableType type) {
		this.type = type;
	}

	public int getRolls() {
		return rolls;
	}

	public void setRolls(int rolls) {
		this.rolls = rolls;
	}

	public ItemStack getRandomWeightedItem() {
		return this.randomCollection.next().getItemStack();
	}

}
