package com.ryandw11.structure.loottables;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.exceptions.LootTableException;
import com.ryandw11.structure.loottables.customitems.CustomLootItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a LootTable file.
 */
public class ConfigLootTable extends LootTable {

    private final String name;
    private int rolls;

    public FileConfiguration lootTablesFC;

    /**
     * Create a loot table with a certain name.
     *
     * <p>This will try to load the loot table file with the specified name.</p>
     *
     * @param name The name.
     */
    public ConfigLootTable(String name) {
        super();

        this.loadFile(name);
        this.name = name;

        if (!lootTablesFC.contains("Rolls"))
            throw new LootTableException("Invalid loot table format! Cannot find global 'Rolls' setting.");
        this.rolls = this.lootTablesFC.getInt("Rolls");


        this.loadItems();
    }

    /**
     * Get the name of the loot table.
     *
     * @return The name of the loot table.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Get the number of items chosen.
     *
     * @return The number of items chosen.
     */
    @Override
    public int getRolls() {
        return rolls;
    }

    @Override
    public void setRolls(int rolls) {
        this.rolls = rolls;
    }

    /**
     * Load the items of the Loot Table.
     */
    private void loadItems() {
        if (!lootTablesFC.contains("Items"))
            throw new LootTableException("Invalid LootTable format! The 'Items' section is required!");

        for (String itemID : this.lootTablesFC.getConfigurationSection("Items").getKeys(false)) {
            ConfigurationSection itemSection = lootTablesFC.getConfigurationSection("Items." + itemID);
            assert itemSection != null;

            // Grab the item type.
            String type = itemSection.getString("Type");
            if (type == null) {
                type = "Standard";
            }
            type = type.toUpperCase();

            int weight = itemSection.getInt("Weight", 1);
            String amount = itemSection.getString("Amount", "1");

            if (type.equalsIgnoreCase("STANDARD")) {
                handleStandardItem(itemID, itemSection);
            } else if (type.equalsIgnoreCase("CUSTOM")) {
                try {
                    CustomLootItem customLootItem = new CustomLootItem(this, itemID, weight, amount);
                    customLootItem.constructItem(itemSection);

                    this.randomCollection.add(weight, customLootItem);
                } catch (LootTableException ex) {
                    continue;
                }
            } else {
                Class<? extends ConfigLootItem> itemClass = CustomStructures.getInstance().getLootTableHandler().getLootItemClassByName(type);
                if (itemClass == null) {
                    throw new LootTableException(String.format("Unable to find custom loot item type %s!", type));
                }
                try {
                    Constructor<? extends ConfigLootItem> item = itemClass.getConstructor(LootTable.class, String.class, Integer.TYPE, String.class);
                    ConfigLootItem itemInst = item.newInstance(this, itemID, weight, amount);

                    itemInst.constructItem(itemSection);
                    this.randomCollection.add(weight, itemInst);
                } catch (NoSuchMethodException ex) {
                    throw new LootTableException(String.format("Unable to construct custom loot item type %s! Does the required constructor exist?", type));
                } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException | InstantiationException ex) {
                    throw new LootTableException(String.format("Unable to construct custom loot item type %s! Does the constructor have the correct access level?", type));
                }
            }
        }

    }

    private void handleStandardItem(String itemID, ConfigurationSection section) {
        // This will throw an exception if the item is not valid.
        validateItem(itemID, section);

        String customName = section.getString("Name");
        String material = Objects.requireNonNull(section.getString("Material"));
        String amount = Objects.requireNonNull(section.getString("Amount"));
        int weight = section.getInt("Weight");
        Map<String, String> enchants = new HashMap<>();

        ConfigurationSection enchantMents = section.getConfigurationSection("Enchantments");

        if (enchantMents != null) {
            for (String enchantName : enchantMents.getKeys(false)) {
                String level = Objects.requireNonNull(section.getString("Enchantments." + enchantName));
                enchants.put(enchantName, level);
            }
        }

        List<String> lore = section.getStringList("Lore");

        this.randomCollection.add(weight, new StandardLootItem(customName, material, amount, weight, lore, enchants));
    }

    /**
     * Validate that a certain item contains all of the required information.
     *
     * @param itemID The item id.
     */
    private void validateItem(String itemID, ConfigurationSection item) {
        if (item == null) throw new LootTableException("Invalid file format for loot table!");
        if (!item.contains("Amount")) throw new LootTableException("Invalid file format for loot table! Cannot find " +
                "'Amount' setting for item: " + itemID);
        if (!item.contains("Weight")) throw new LootTableException("Invalid file format for loot table! Cannot find " +
                "'Weight' setting for item: " + itemID);
        if (!item.contains("Type")) throw new LootTableException("Invalid file format for loot table! Cannot find " +
                "'Type' setting for item: " + itemID);
        if (!item.isInt("Weight"))
            throw new LootTableException("Invalid file format for loot table! 'Weight' is not an " +
                    "integer for item: " + itemID);
    }

    /**
     * Load the file.
     *
     * @param name The file name.
     */
    private void loadFile(String name) {
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

}
