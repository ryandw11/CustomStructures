package com.ryandw11.structure.loottables;

import com.ryandw11.structure.exceptions.LootTableException;
import com.ryandw11.structure.utils.NumberStylizer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a Standard Item within a {@link ConfigLootTable}.
 */
public class StandardLootItem implements LootItem {

    private int weight;
    private final String amount;
    private final Map<String, String> enchants;
    private ItemStack item;

    /**
     * This is for normal loot table items.
     *
     * @param customName The custom name.
     * @param material   The material of the item.
     * @param amount     The amount.
     * @param weight     The weight.
     * @param lore       The lore.
     * @param enchants   The enchants.
     */
    public StandardLootItem(String customName, String material, int amount, int weight, List<String> lore, Map<String, String> enchants) {
        this.weight = weight;
        try {
            this.item = new ItemStack(Material.valueOf(material.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new LootTableException("Unknown Material Type: " + material);
        }
        this.amount = amount + "";
        this.item.setAmount(amount);

        if (customName != null) { //Catch for people who do not want different names
            ItemMeta meta = Objects.requireNonNull(this.item.getItemMeta());
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
            this.item.setItemMeta(meta);
        }

        if (!lore.isEmpty()) {
            lore = lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
            Objects.requireNonNull(this.item.getItemMeta()).setLore(lore);
        }

        this.enchants = enchants;
    }

    /**
     * This is for normal loot table items.
     *
     * @param customName The custom name.
     * @param material   The material of the item.
     * @param amount     The amount (in stylized form).
     * @param weight     The weight.
     * @param lore       The lore.
     * @param enchants   The enchants.
     */
    public StandardLootItem(String customName, String material, String amount, int weight, List<String> lore, Map<String, String> enchants) {
        this.weight = weight;
        try {
            this.item = new ItemStack(Material.valueOf(material.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new LootTableException("Unknown Material Type: " + material);
        }
        this.amount = amount;
        this.item.setAmount(NumberStylizer.getStylizedInt(amount));
        ItemMeta meta = Objects.requireNonNull(this.item.getItemMeta());
        if (customName != null) { //Catch for people who do not want different names
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));

        }

        if (!lore.isEmpty()) {
            lore = lore.stream().map(s -> ChatColor.translateAlternateColorCodes('&', s)).collect(Collectors.toList());
            meta.setLore(lore);
        }

        this.item.setItemMeta(meta);
        this.enchants = enchants;
    }

    /**
     * Apply the stylized enchantments and amount to the itemstack.
     *
     * @param item The item stack to apply the stats to.
     */
    private void applyStats(ItemStack item) {
        item.setAmount(NumberStylizer.getStylizedInt(amount));
        for (String enchantName : enchants.keySet()) {
            int level = NumberStylizer.getStylizedInt(enchants.get(enchantName));
            Enchantment enchantment = EnchantmentWrapper.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
            if (enchantment == null)
                throw new LootTableException("Invalid Enchantment: " + enchantName);
            if (item.getItemMeta() instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
                enchantmentStorageMeta.addStoredEnchant(enchantment, level, true);
                item.setItemMeta(enchantmentStorageMeta);
            } else {
                item.addUnsafeEnchantment(enchantment, level);
            }
        }
    }

    /**
     * Get the weight of the loot item.
     *
     * @return The weight of the loot item.
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Set the weight of the loot item.
     *
     * @param weight The weight of the loot item to set.
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Get the item stack.
     *
     * @return The clone of the item stack with the applied stats.
     */
    @Override
    public ItemStack getItemStack() {
        ItemStack cloneStack = item.clone();
        applyStats(cloneStack);
        return cloneStack;
    }

    /**
     * Set the base item stack.
     *
     * @param item The item to set the loot item to.
     */
    public void setItem(ItemStack item) {
        this.item = item;
    }

}
