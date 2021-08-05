package com.ryandw11.structure.loottables.customitems;

import com.jojodmo.itembridge.ItemBridge;
import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.CustomStructuresAPI;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

/**
 * Manages the custom items defined for the loot tables.
 *
 * <p>Get this handler from {@link CustomStructuresAPI#getCustomItemManager()}</p>
 */
public class CustomItemManager {
    private FileConfiguration config;
    private File file;
    private CustomStructures structures;

    /**
     * This should only ever be constructed by the CustomStructures main class.
     * <p>Use {@link CustomStructuresAPI#getCustomItemManager()} to access this class for the plugin.</p>
     *
     * @param structures The main class.
     * @param file       The file.
     * @param dir        The directory to put the file.
     */
    public CustomItemManager(CustomStructures structures, File file, File dir) {
        if (!dir.exists())
            dir.mkdir();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                structures.getLogger().severe("Cannot create Custom Items file. Enable debug mode for more information.");
                if (structures.isDebug())
                    ex.printStackTrace();
                return;
            }
        }
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
        this.structures = structures;
    }

    /**
     * Add an item to the custom items file.
     *
     * @param key       The key to add.
     * @param itemStack The item stack to add.
     * @return If the item was successfully added.
     * <p>This returns false if the key already exists in the file.</p>
     */
    public boolean addItem(String key, ItemStack itemStack) {
        if (this.config.contains(key))
            return false;
        config.set(key, itemStack.clone());
        try {
            config.save(file);
            return true;
        } catch (IOException ex) {
            structures.getLogger().severe("Failed to save Custom Items file after adding an item.");
            if (structures.isDebug()) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Remove an item from the custom items file.
     *
     * @param key The key to remove.
     * @return If the key was successfully removed.
     * <p>If the key is not in the file than false is returned.</p>
     */
    public boolean removeItem(String key) {
        if (!this.config.contains(key))
            return false;
        config.set(key, null);
        try {
            config.save(file);
            return true;
        } catch (IOException ex) {
            structures.getLogger().severe("Failed to save Custom Items file after removing an item.");
            if (structures.isDebug()) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Get an item from the item file.
     *
     * @param key The key to check.
     * @return The item stack. (Will return null if the item does not exist).
     */
    public ItemStack getItem(String key) {
        if (!this.config.contains(key))
            return null;
        return config.getItemStack(key);
    }

    /**
     * Gets a custom ItemBridge item
     *
     * @author jazzyjake
     * @param key
     * @return
     */
    public ItemStack getItemBridgeItem(String key) {
        // Check if the server has ItemBridge
        if (Bukkit.getPluginManager().getPlugin("ItemBridge") == null) {
            CustomStructures.getInstance().getLogger().warning("ItemBridge is not installed!");
            return null;
        }

        return ItemBridge.getItemStack(key);
    }

    /**
     * Gets a custom ItemsAdder item
     *
     * @author jazzyjake
     * @param key
     * @return
     */
    public ItemStack getItemsAdderItem(String key) {
        // Check if the server has ItemsAdder
        if (Bukkit.getPluginManager().getPlugin("ItemsAdder") == null) {
            CustomStructures.getInstance().getLogger().warning("ItemsAdder is not installed!");
            return null;
        }

        CustomStack stack = CustomStack.getInstance(key);
        return stack.getItemStack();
    }

    /**
     * Get the File Configuration for the custom items file.
     *
     * <p>This is meant for internal use only.</p>
     *
     * @return The File Configuration.
     */
    public FileConfiguration getConfig() {
        return config;
    }
}
