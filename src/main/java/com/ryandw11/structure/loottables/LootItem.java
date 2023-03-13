package com.ryandw11.structure.loottables;

import org.bukkit.inventory.ItemStack;

/**
 * A LootItem is an item which can be placed in a LootTable.
 *
 * <p>To define a custom LootItem to be used in a config file, see {@link ConfigLootItem}.</p>
 */
public interface LootItem {
    /**
     * The ItemStack representation of the LootItem.
     *
     * @return The ItemStack representation.
     */
    ItemStack getItemStack();
}
