package com.ryandw11.structure.loottables;

import com.ryandw11.structure.exceptions.LootTableException;
import com.ryandw11.structure.utils.NumberStylizer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an Item within a loot table.
 *
 * @author Chusca
 */
public class LootItem {

    private int weight;
    private final String amount;
    private final Map<String, String> enchants;
    private ItemStack item;

    /**
     * This is for normal loot table items.
     *
     * @param customName The custom name.
     * @param type       The type.
     * @param amount     The amount.
     * @param weight     The weight.
     * @param enchants   The enchants.
     */
    public LootItem(String customName, String type, int amount, int weight, Map<String, String> enchants) {
        this.weight = weight;
        try {
            this.item = new ItemStack(Material.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new LootTableException("Unknown Material Type: " + type);
        }
        this.amount = amount + "";
        this.item.setAmount(amount);

        if (customName != null) { //Catch for people who do not want different names
            ItemMeta meta = this.item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
            this.item.setItemMeta(meta);
        }

        this.enchants = enchants;
    }

    /**
     * This is for normal loot table items.
     *
     * @param customName The custom name.
     * @param type       The type.
     * @param amount     The amount (in stylized form).
     * @param weight     The weight.
     * @param enchants   The enchants.
     */
    public LootItem(String customName, String type, String amount, int weight, Map<String, String> enchants) {
        this.weight = weight;
        try {
            this.item = new ItemStack(Material.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new LootTableException("Unknown Material Type: " + type);
        }
        this.amount = amount;
        this.item.setAmount(NumberStylizer.getStylizedInt(amount));

        if (customName != null) { //Catch for people who do not want different names
            ItemMeta meta = this.item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', customName));
            this.item.setItemMeta(meta);
        }

        this.enchants = enchants;
    }

    /**
     * This is for use with custom items.
     *
     * @param itemStack The item stack to use.
     * @param amount    The amount to use.
     * @param weight    The weight to use.
     */
    public LootItem(ItemStack itemStack, int amount, int weight) {
        this.weight = weight;
        this.item = itemStack.clone();
        this.item.setAmount(amount);
        this.amount = amount + "";
        this.enchants = new HashMap<>();
    }

    /**
     * This is for use with custom items.
     *
     * @param itemStack The item stack to use.
     * @param amount    The amount to use.
     * @param weight    The weight to use.
     */
    public LootItem(ItemStack itemStack, String amount, int weight) {
        this.weight = weight;
        this.item = itemStack.clone();
        this.item.setAmount(1);
        this.amount = amount;
        this.enchants = new HashMap<>();
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
            item.addUnsafeEnchantment(enchantment, level);
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
