package com.ryandw11.structure.schematic;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.LootPopulateEvent;
import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.loottables.LootTableType;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.utils.RandomCollection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

/**
 * Handle loot table replacement in schematics.
 */
public class LootTableReplacer {
    private LootTableReplacer() {
    }

    /**
     * Replace the contents of a container with the loot table from a structure.
     *
     * @param structure The structure that is being spawned.
     * @param location  The location of the container.
     */
    protected static void replaceContainerContent(Structure structure, Location location) {

        BlockState blockState = location.getBlock().getState();
        Container container = (Container) blockState;
        Inventory containerInventory = container.getInventory();
        Block block = location.getBlock();
        LootTableType blockType = LootTableType.valueOf(block.getType());

        // Number of times the run the loot table fill method. (Max of 20)
        int numberOfFills = 1;

        boolean explictLoottableDefined = false;
        LootTable lootTable = null;

        if (containerInventory.getItem(0) != null) {
            ItemStack paper = containerInventory.getItem(0);
            if (paper.getType() == Material.PAPER &&
                    paper.hasItemMeta() &&
                    paper.getItemMeta().hasDisplayName() &&
                    paper.getItemMeta().getDisplayName().contains("%${") &&
                    paper.getItemMeta().getDisplayName().contains("}$%")) {
                String name = paper.getItemMeta().getDisplayName()
                        .replace("%${", "")
                        .replace("}$%", "");
                lootTable = CustomStructures.getInstance().getLootTableHandler().getLootTableByName(name);
                numberOfFills = Math.min(20, paper.getAmount());
                containerInventory.clear();
                explictLoottableDefined = true;
            }
        }

        if (lootTable == null) {
            if (structure.getLootTables().isEmpty()) return;

            RandomCollection<LootTable> tables = structure.getLootTables(blockType);
            if (tables == null) return;

            lootTable = tables.next();
        }

        Random random = new Random();

        // Trigger the loot populate event.
        LootPopulateEvent event = new LootPopulateEvent(structure, location, lootTable);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCanceled()) return;

        // TODO: This is not a good method, should try to pick another loot table if failed.
        for(int i = 0; i < numberOfFills; i++) {
            if ((lootTable.getTypes().contains(blockType) || explictLoottableDefined) && containerInventory instanceof FurnaceInventory) {
                lootTable.fillFurnaceInventory((FurnaceInventory) containerInventory, random, container.getLocation());
            } else if ((lootTable.getTypes().contains(blockType) || explictLoottableDefined) && containerInventory instanceof BrewerInventory) {
                lootTable.fillBrewerInventory((BrewerInventory) containerInventory, random, container.getLocation());
            } else if (lootTable.getTypes().contains(blockType) || explictLoottableDefined) {
                lootTable.fillContainerInventory(containerInventory, random, container.getLocation());
            }
        }
    }

    /**
     * Replace the chest content.
     *
     * @param lootTable          The loot table.
     * @param random             The value of random.
     * @param containerInventory The container inventory
     */
    public static void replaceChestContent(LootTable lootTable, Random random, Inventory containerInventory) {
        for(int roll = 0; roll < lootTable.getRolls(); roll++){
            ItemStack[] containerContent = containerInventory.getContents();

            ItemStack randomItem = lootTable.getRandomWeightedItem();

            for (int j = 0; j < randomItem.getAmount(); j++) {
                boolean done = false;
                int attemps = 0;
                while (!done) {
                    int randomPos = random.nextInt(containerContent.length);
                    ItemStack randomPosItem = containerInventory.getItem(randomPos);
                    if (randomPosItem != null) {

                        if (isSameItem(randomPosItem, randomItem)) {
                            if (randomPosItem.getAmount() < randomItem.getMaxStackSize()) {
                                ItemStack randomItemCopy = randomItem.clone();
                                int newAmount = randomPosItem.getAmount() + 1;
                                randomItemCopy.setAmount(newAmount);
                                containerContent[randomPos] = randomItemCopy;
                                containerInventory.setContents(containerContent);
                                done = true;
                            }
                        }
                    } else {
                        ItemStack randomItemCopy = randomItem.clone();
                        randomItemCopy.setAmount(1);
                        containerContent[randomPos] = randomItemCopy;
                        containerInventory.setContents(containerContent);
                        done = true;

                    }
                    attemps++;
                    if (attemps >= containerContent.length) {
                        done = true;
                    }
                }
            }
        }
    }

    /**
     * Replace the contents of a brewer with the loot table.
     *
     * @param lootTable          The loot table to populate the brewer with.
     * @param containerInventory The inventory of the brewer.
     */
    public static void replaceBrewerContent(LootTable lootTable, BrewerInventory containerInventory) {
        for(int roll = 0; roll < lootTable.getRolls(); roll++) {
            ItemStack item = lootTable.getRandomWeightedItem();
            ItemStack ingredient = containerInventory.getIngredient();
            ItemStack fuel = containerInventory.getFuel();

            if ((ingredient == null) || ingredient.equals(item)) {
                containerInventory.setIngredient(item);
            } else if ((fuel == null) || fuel.equals(item)) {
                containerInventory.setFuel(item);
            }
        }
    }

    /**
     * Replace the content of the furnace with loot table items.
     *
     * @param lootTable          The loot table selected for the furnace.
     * @param containerInventory The inventory of the furnace.
     */
    public static void replaceFurnaceContent(LootTable lootTable, FurnaceInventory containerInventory) {
        for(int roll = 0; roll < lootTable.getRolls(); roll++) {
            ItemStack item = lootTable.getRandomWeightedItem();
            ItemStack result = containerInventory.getResult();
            ItemStack fuel = containerInventory.getFuel();
            ItemStack smelting = containerInventory.getSmelting();

            if ((result == null) || result.equals(item)) {
                containerInventory.setResult(item);
            } else if ((fuel == null) || fuel.equals(item)) {
                containerInventory.setFuel(item);
            } else if ((smelting == null) || smelting.equals(item)) {
                containerInventory.setSmelting(item);
            }
        }
    }

    /**
     * Check if two items are the same.
     *
     * @param randomPosItem The first item.
     * @param randomItem    The second item.
     * @return If the two items have the same metadata and type.
     */
    private static boolean isSameItem(ItemStack randomPosItem, ItemStack randomItem) {
        ItemMeta randomPosItemMeta = randomPosItem.getItemMeta();
        ItemMeta randomItemMeta = randomItem.getItemMeta();

        return randomPosItem.getType().equals(randomItem.getType()) && randomPosItemMeta.equals(randomItemMeta);
    }
}
