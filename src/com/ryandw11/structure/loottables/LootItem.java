package com.ryandw11.structure.loottables;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Represents an Item within a loot table.
 * @author Chusca
 *
 */
public class LootItem {

	private int weight;
	private ItemStack item;

	public LootItem(String customName, String type, int amount, int weight, Map<String, Integer> enchants) {
		this.weight = weight;
		this.item = new ItemStack(Material.valueOf(type));
		this.item.setAmount(amount);
		
		if(customName != null) { //Catch for people who do not want different names
			ItemMeta meta = this.item.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
			this.item.setItemMeta(meta);
		}

		for (String enchantName : enchants.keySet()) {
			int level = enchants.get(enchantName);
			this.item.addUnsafeEnchantment(EnchantmentWrapper.getByName(enchantName), level);
		}
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public ItemStack getItemStack() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

}
