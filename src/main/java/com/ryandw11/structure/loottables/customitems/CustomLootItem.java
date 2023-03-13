package com.ryandw11.structure.loottables.customitems;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.exceptions.LootTableException;
import com.ryandw11.structure.loottables.ConfigLootItem;
import com.ryandw11.structure.loottables.LootTable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * A custom loot item.
 */
public class CustomLootItem extends ConfigLootItem {
    private ItemStack itemStack;

    public CustomLootItem(LootTable lootTable, String itemID, int weight, String amount) {
        super(lootTable, itemID, weight, amount);
    }

    @Override
    public void constructItem(ConfigurationSection configurationSection) {
        ItemStack item = CustomStructures.getInstance().getCustomItemManager().getItem(configurationSection.getString("Key"));
        if (item == null) {
            CustomStructures.getInstance().getLogger().warning("Cannot find a custom item with the id of " + getItemID() +
                    " in the " + getLootTable().getName() + " loot table!");
            throw new LootTableException("Cannot find a custom item with the id of " + getItemID() + "!");
        }

        itemStack = item;
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack clone = itemStack.clone();
        clone.setAmount(getAmount());

        return clone;
    }
}
