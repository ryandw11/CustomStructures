package com.ryandw11.structure.loottables;

import com.ryandw11.structure.schematic.LootTableReplacer;
import com.ryandw11.structure.utils.RandomCollection;
import org.bukkit.Location;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a LootTable.
 *
 * <p>You can extend this class to implement custom LootTables.</p>
 *
 * <p>This class comes with a {@link RandomCollection} of {@link LootItem} by default. You can opt to not use this
 * collection with your implementation by overriding {@link #getRandomWeightedItem()} and {@link #getItems()}.</p>
 */
public abstract class LootTable {
    /**
     * The default RandomCollection provided.
     */
    protected RandomCollection<LootItem> randomCollection;

    /**
     * The list of LootTable types.
     * <p>Avoid changing the default behavior of LootTableTypes.</p>
     */
    protected List<LootTableType> types;

    /**
     * The default constructor for the LootTable which initializes the RandomCollection and list of LootTableTypes.
     */
    public LootTable() {
        types = new ArrayList<>();
        randomCollection = new RandomCollection<>();
    }

    /**
     * Get the name of the LootTable.
     *
     * @return The name of the LootTable.
     */
    public abstract String getName();

    /**
     * Get the number of rolls for the loot table. (The number of items to be chosen).
     *
     * @return The number of rolls for the loot table.
     */
    public abstract int getRolls();

    /**
     * Set the number of rolls for the loot table.
     *
     * <p>An implementation of this method is not strictly necessary.</p>
     *
     * @param rolls The number of rolls for the loot tables.
     */
    public abstract void setRolls(int rolls);

    /**
     * Fills a container's inventory with items.
     *
     * <p>Override this method to replace the default functionality.</p>
     *
     * @param inventory The inventory to fill.
     * @param random    The randomizer.
     * @param location  The location of the container.
     */
    public void fillContainerInventory(Inventory inventory, Random random, Location location) {
        LootTableReplacer.replaceChestContent(this, random, inventory);
    }

    /**
     * Fills a furnace inventory with items.
     *
     * <p>Override this method to replace the default functionality.</p>
     *
     * @param furnaceInventory The furnace inventory to fill.
     * @param random           The randomizer.
     * @param location         The location of the furnace.
     */
    public void fillFurnaceInventory(FurnaceInventory furnaceInventory, Random random, Location location) {
        LootTableReplacer.replaceFurnaceContent(this, furnaceInventory);
    }

    /**
     * Fills a brewer with items.
     *
     * <p>Override this method to replace the default functionality.</p>
     *
     * @param brewerInventory The brewer invetory to fill.
     * @param random          The randomizer.
     * @param location        The location of the furnace.
     */
    public void fillBrewerInventory(BrewerInventory brewerInventory, Random random, Location location) {
        LootTableReplacer.replaceBrewerContent(this, brewerInventory);
    }

    /**
     * Get a random item from the loot table.
     *
     * <p>Override this method if you don't use the default RandomCollection.</p>
     *
     * @return A random item from the loot table.
     */
    public ItemStack getRandomWeightedItem() {
        return this.randomCollection.next().getItemStack();
    }

    /**
     * Get the list of items from the loot table.
     *
     * <p>Override this method if you don't use the default RandomCollection.</p>
     *
     * @return This list of possible items in the LootTable.
     */
    public List<LootItem> getItems() {
        return randomCollection.toList();
    }

    /**
     * Add an item to the LootTable's random collection.
     *
     * @param weight   The weight of the item to add.
     * @param lootItem The LootItem to add.
     */
    protected final void addLootItem(double weight, @NotNull LootItem lootItem) {
        this.randomCollection.add(weight, lootItem);
    }

    /**
     * Get the list of LootTable types.
     *
     * @return The list of LootTable types.
     */
    public final List<LootTableType> getTypes() {
        return types;
    }

    /**
     * Set the list of LootTable types.
     *
     * @param types The list of types to be set.
     */
    public final void setTypes(List<LootTableType> types) {
        this.types = types;
    }

    /**
     * Add a LootTable type.
     *
     * @param type The LootTable type to be added.
     */
    public final void addType(LootTableType type) {
        this.types.add(type);
    }
}
