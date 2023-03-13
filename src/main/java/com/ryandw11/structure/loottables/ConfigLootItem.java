package com.ryandw11.structure.loottables;

import com.ryandw11.structure.utils.NumberStylizer;
import org.bukkit.configuration.ConfigurationSection;

/**
 * This class allows you to create custom loot items to be used by users in the LootTable config files.
 *
 * <p>Create a new class that extends this class and use
 * {@link com.ryandw11.structure.api.structaddon.CustomStructureAddon#registerLootItem(String, Class)}
 * to register the item.</p>
 *
 * <p>Ensure any implementations include a construct that exactly matches the constructor defined by this class.</p>
 */
public abstract class ConfigLootItem implements LootItem {
    private final LootTable lootTable;
    private final String itemID;
    private final int weight;
    private final String amount;

    /**
     * The default constructor. There must exist a constructor in all child classes that
     * has the same parameters.
     *
     * @param lootTable The loot table.
     * @param itemID    The itemID.
     * @param weight    The weight.
     * @param amount    The raw amount.
     */
    public ConfigLootItem(LootTable lootTable, String itemID, int weight, String amount) {
        this.lootTable = lootTable;
        this.itemID = itemID;
        this.weight = weight;
        this.amount = amount;
    }

    /**
     * This is called when {@link ConfigLootTable} processes a custom loot item.
     *
     * @param configurationSection The configuration section of the loot item.
     */
    public abstract void constructItem(ConfigurationSection configurationSection);

    /**
     * The loot table that the item exists in.
     *
     * @return The loot table that the item exists in.
     */
    public final LootTable getLootTable() {
        return lootTable;
    }

    /**
     * The id of the item.
     *
     * @return The id of the item.
     */
    public final String getItemID() {
        return itemID;
    }

    /**
     * The weight of the item.
     *
     * @return The weight of the item.
     */
    public final int getWeight() {
        return weight;
    }

    /**
     * The amount of the item. The raw amount is processed as a stylized
     * integer. (See {@link NumberStylizer#getStylizedInt(String)}).
     *
     * @return The amount of the item.
     */
    public final int getAmount() {
        return NumberStylizer.getStylizedInt(amount);
    }

    /**
     * The raw amount of the item.
     *
     * @return The raw amount of the item.
     */
    public final String getRawAmount() {
        return amount;
    }
}
