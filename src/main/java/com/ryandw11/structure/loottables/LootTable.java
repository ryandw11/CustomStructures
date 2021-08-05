package com.ryandw11.structure.loottables;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.exceptions.LootTableException;
import com.ryandw11.structure.utils.RandomCollection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Represents a LootTable file.
 *
 * @author Chusca
 */
public class LootTable {

    private List<LootTableType> types;
    private int rolls;
    private RandomCollection<LootItem> randomCollection;
    private String name;

    public FileConfiguration lootTablesFC;

    public LootTable(String name) {
        this.LoadFile(name);
        this.name = name;

        if(!lootTablesFC.contains("Rolls")) throw new LootTableException("Invalid loot table format! Cannot find global 'Rolls' setting.");

        this.types = new ArrayList<>();
        this.rolls = this.lootTablesFC.getInt("Rolls");


        this.loadItems();
    }

    /**
     * Load the items of the Loot Table.
     */
    private void loadItems() {
        this.randomCollection = new RandomCollection<>();
        if (!lootTablesFC.contains("Items"))
            throw new LootTableException("Invalid LootTable format! The 'Items' section is required!");

        for (String itemID : this.lootTablesFC.getConfigurationSection("Items").getKeys(false)) {
            // This will throw an exception if the item is not valid.
            validateItem(itemID);

            int amount = this.lootTablesFC.getInt("Items." + itemID + ".Amount");
            int weight = this.lootTablesFC.getInt("Items." + itemID + ".Weight");

            if (lootTablesFC.getString("Items." + itemID + ".Type").equalsIgnoreCase("CUSTOM")) {
                ItemStack item = CustomStructures.getInstance().getCustomItemManager().getItem(this.lootTablesFC.getString("Items." + itemID + ".Key"));
                if (item == null) {
                    CustomStructures.getInstance().getLogger().warning("Cannot find a custom item with the id of " + itemID +
                            " in the " + name + " loot table!");
                    continue;
                }
                this.randomCollection.add(weight, new LootItem(item, amount, weight));
            } else if (lootTablesFC.getString("Items." + itemID + ".Type").equalsIgnoreCase("ITEMBRIDGE")) {
                // Code for ItemBridge implementation
                ItemStack item = CustomStructures.getInstance().getCustomItemManager().getItemBridgeItem(this.lootTablesFC.getString("Items." + itemID + ".Key"));
                if (item == null) {
                    CustomStructures.getInstance().getLogger().warning("Cannot find a custom ItemBridge item with the id of " + itemID +
                            " in the " + name + " loot table!");
                    continue;
                }
            } else if (lootTablesFC.getString("Items." + itemID + ".Type").equalsIgnoreCase("ITEMSADDER")) {
                // Code for ItemsAdder implementation
                ItemStack item = CustomStructures.getInstance().getCustomItemManager().getItemsAdderItem(this.lootTablesFC.getString("Items." + itemID + ".Key"));
                if (item == null) {
                    CustomStructures.getInstance().getLogger().warning("Cannot find a custom ItemsAdder item with the id of " + itemID +
                            " in the " + name + " loot table!");
                    continue;
                }
            } else {
                String customName = this.lootTablesFC.getString("Items." + itemID + ".Name");
                String type = this.lootTablesFC.getString("Items." + itemID + ".Type");
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

    }

    /**
     * Validate that a certain item contains all of the required information.
     *
     * @param itemID The item id.
     */
    private void validateItem(String itemID) {
        ConfigurationSection item = lootTablesFC.getConfigurationSection("Items." + itemID);
        if (item == null) throw new LootTableException("Invalid file format for loot table!");
        if (!item.contains("Amount")) throw new LootTableException("Invalid file format for loot table! Cannot find " +
                "'Amount' setting for item: " + itemID);
        if (!item.contains("Weight")) throw new LootTableException("Invalid file format for loot table! Cannot find " +
                "'Weight' setting for item: " + itemID);
        if (!item.contains("Type")) throw new LootTableException("Invalid file format for loot table! Cannot find " +
                "'Type' setting for item: " + itemID);
        if(!item.isInt("Amount")) throw new LootTableException("Invalid file format for loot table! 'Amount' is not an " +
                "integer for item: " + itemID);
        if(!item.isInt("Weight")) throw new LootTableException("Invalid file format for loot table! 'Weight' is not an " +
                "integer for item: " + itemID);
    }

    /**
     * Load the file.
     *
     * @param name The file name.
     */
    private void LoadFile(String name) {
        File lootTablesfile = new File(CustomStructures.plugin.getDataFolder() + "/lootTables/" + name + ".yml");
        if (!lootTablesfile.exists())
            throw new LootTableException("Cannot find the following loot table file: " + name);
        this.lootTablesFC = YamlConfiguration.loadConfiguration(lootTablesfile);

        try {
            lootTablesFC.load(lootTablesfile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new LootTableException("Invalid LootTable Configuration! Please view the guide on the wiki for more information.");
        }
    }

    /**
     * Get the types of the loot table.
     *
     * @return The types
     */
    public List<LootTableType> getTypes() {
        return types;
    }

    /**
     * Set the types of the loot table.
     *
     * @param types The types to set the loot table to.
     */
    public void setTypes(List<LootTableType> types) {
        this.types = types;
    }

    public void addType(LootTableType type){
        this.types.add(type);
    }

    /**
     * Get the name of the loot table.
     *
     * @return The name of the loot table.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the number of items chosen.
     *
     * @return The number of items chosen.
     */
    public int getRolls() {
        return rolls;
    }

    public void setRolls(int rolls) {
        this.rolls = rolls;
    }

    /**
     * Get a random item from the table.
     *
     * @return A random item.
     */
    public ItemStack getRandomWeightedItem() {
        return this.randomCollection.next().getItemStack();
    }

    /**
     * Get the items within the loot table.
     *
     * @return A list of items.
     */
    public List<LootItem> getItems() {
        List<LootItem> result = new ArrayList<>();
        for (String itemID : this.lootTablesFC.getConfigurationSection("Items").getKeys(false)) {
            this.validateItem(itemID);

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

            result.add(new LootItem(customName, type, amount, weight, enchants));
        }

        return result;
    }

}
