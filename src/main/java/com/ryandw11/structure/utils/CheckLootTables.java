package com.ryandw11.structure.utils;

import com.ryandw11.structure.CustomStructures;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Check to ensure the the loot tables are valid.
 */
public class CheckLootTables extends BukkitRunnable {
    private int num;
    private final List<String> schematics;
    private final CustomStructures plugin;

    public CheckLootTables(Set<String> schematics, CustomStructures plugin) {
        this.plugin = plugin;
        this.num = 0;
        this.schematics = new ArrayList<>(schematics);
    }

    @Override
    public void run() {
        if (num >= schematics.size()) {
            plugin.getLogger().info("The lootTables have been checked! All seems to be in order!");
            this.cancel();
            return;
        }
        String s = schematics.get(num);
        String ex = "Schematics.";
        ConfigurationSection lootTablesCS = plugin.getConfig().getConfigurationSection(ex + s + ".LootTables");
        if (lootTablesCS != null) {
            for (String name : lootTablesCS.getKeys(true)) {
                File schematic = new File(plugin.getDataFolder() + "/lootTables/" + name + ".yml");
                if (!schematic.exists()) {
                    plugin.getLogger().severe("Error: The Loot Table file for " + name + " does not exist!");
                    plugin.getLogger().severe("Please put that file in the Loottable Folder folder!");
                    plugin.getLogger().severe("For assistance please contact Ryandw11 on spigot.");
                    this.cancel();
                    CustomStructures.enabled = false;
                }
            }
        }
        num += 1;

    }

}
