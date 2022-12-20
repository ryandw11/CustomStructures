package com.ryandw11.structure.loottables;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.exceptions.LootTableException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootContext;

import java.util.Random;

/**
 * Represents a loaded Minecraft loot table.
 *
 * <p>Example: <code>minecraft:chests/abandoned_mineshaft</code></p>
 * <p>This also works with loot tables provided by datapacks.</p>
 */
public class MinecraftLootTable extends LootTable {

    private final org.bukkit.loot.LootTable lootTable;

    /**
     * Load an existing minecraft loot table (includes loot table from data packs).
     *
     * @param nameSpaceString The loot table name space.
     */
    public MinecraftLootTable(String nameSpaceString) {
        super();

        if (nameSpaceString.startsWith("minecraft:"))
            this.lootTable = Bukkit.getLootTable(NamespacedKey.minecraft(nameSpaceString.replace("minecraft:", "")));
        else {
            String[] keys = nameSpaceString.split(":");
            if (keys.length != 2) {
                throw new LootTableException("Specified minecraft loot table not found! (" + nameSpaceString + ")");
            }
            // Using internal use only NamespacedKey constructor.
            this.lootTable = Bukkit.getLootTable(new NamespacedKey(keys[0], keys[1]));
        }

        if (this.lootTable == null) {
            throw new LootTableException("Specified minecraft loot table not found! (" + nameSpaceString + ")");
        }
    }

    /**
     * Get the name of the loot table.
     *
     * @return The name of the loot table.
     */
    @Override
    public String getName() {
        return lootTable.toString();
    }

    /**
     * Get the number of items chosen.
     *
     * @return The number of items chosen.
     */
    @Override
    public int getRolls() {
        return 0;
    }

    @Override
    public void setRolls(int rolls) {
        throw new UnsupportedOperationException("Rolls are not used in this LootTable.");
    }

    @Override
    public void fillContainerInventory(Inventory inventory, Random random, Location location) {
        LootContext lootContext = new LootContext.Builder(location).build();

        try {
            lootTable.fillInventory(inventory, random, lootContext);
        } catch (Exception ex) {
            CustomStructures.getInstance().getLogger().severe("Unable to fill loot table: " + getName() + "!");
            CustomStructures.getInstance().getLogger().severe("Does this loot table exist?");
        }
    }

    @Override
    public void fillFurnaceInventory(FurnaceInventory furnaceInventory, Random random, Location location) {
        LootContext lootContext = new LootContext.Builder(location).build();

        try {
            lootTable.fillInventory(furnaceInventory, random, lootContext);
        } catch (Exception ex) {
            CustomStructures.getInstance().getLogger().severe("Unable to fill loot table: " + getName() + "!");
            CustomStructures.getInstance().getLogger().severe("Does this loot table exist?");
        }
    }

    @Override
    public void fillBrewerInventory(BrewerInventory brewerInventory, Random random, Location location) {
        LootContext lootContext = new LootContext.Builder(location).build();

        try {
            lootTable.fillInventory(brewerInventory, random, lootContext);
        } catch (Exception ex) {
            CustomStructures.getInstance().getLogger().severe("Unable to fill loot table: " + getName() + "!");
            CustomStructures.getInstance().getLogger().severe("Does this loot table exist?");
        }
    }
}
