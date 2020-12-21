package com.ryandw11.structure;

import com.ryandw11.structure.commands.SCommand;
import com.ryandw11.structure.commands.SCommandTab;
import com.ryandw11.structure.listener.ChunkLoad;
import com.ryandw11.structure.listener.PlayerJoin;
import com.ryandw11.structure.loottables.LootTableType;
import com.ryandw11.structure.loottables.LootTablesHandler;
import com.ryandw11.structure.loottables.customitems.CustomItemManager;
import com.ryandw11.structure.mythicalmobs.MMDisabled;
import com.ryandw11.structure.mythicalmobs.MMEnabled;
import com.ryandw11.structure.mythicalmobs.MythicalMobHook;
import com.ryandw11.structure.structure.StructureHandler;
import com.ryandw11.structure.utils.Pair;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ryandw11
 * @version 1.5.4
 */

public class CustomStructures extends JavaPlugin {

    public static CustomStructures plugin;
    public File lootTablesfile = new File(getDataFolder() + "/lootTables/lootTable.yml");
    public FileConfiguration lootTablesFC = YamlConfiguration.loadConfiguration(lootTablesfile);

    public MythicalMobHook mmh;

    private StructureHandler structureHandler;
    private LootTablesHandler lootTablesHandler;
    private CustomItemManager customItemManager;
    private boolean debugMode;

    public static boolean enabled;

    public static final int COMPILED_STRUCT_VER = 1;

    @Override
    public void onEnable() {
        enabled = true;

        plugin = this;
        loadManager();
        registerConfig();

        if (getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            mmh = new MMEnabled();
            getLogger().info("MythicMobs detected! Activating plugin hook!");
        } else
            mmh = new MMDisabled();
        loadFile();
        debugMode = getConfig().getBoolean("debug");
        if (getConfig().getInt("configversion") < 6) {
            lootTablesHandler = new LootTablesHandler();
            updateConfig(getConfig().getInt("configversion"));
        }

        File f = new File(getDataFolder() + File.separator + "structures");
        if (!f.exists()) {
            saveResource("structures/demo.yml", false);
        }
        f = new File(getDataFolder() + File.separator + "schematics");
        if (!f.exists()) {
            saveResource("schematics/demo.schem", false);
            getLogger().info("Loading the plugin for the first time.");
            getLogger().info("A demo structure was added! Please make sure to test out this plugin in a test world!");
        }

        this.customItemManager = new CustomItemManager(this, new File(getDataFolder() + File.separator + "items" + File.separator + "customitems.yml"), new File(getDataFolder() + File.separator + "items"));

        lootTablesHandler = new LootTablesHandler();
        this.structureHandler = new StructureHandler(getConfig().getStringList("Structures"), this);

        getLogger().info("The plugin has been enabled with " + structureHandler.getStructures().size() + " structures.");

        if (getConfig().getBoolean("bstats")) {
            new Metrics(this, 7056);
            getLogger().info("Bstat metrics for this plugin is enabled. Disable it in the config if you do not want it on.");
        } else {
            getLogger().info("Bstat metrics is disabled for this plugin.");
        }
    }

    @Override
    public void onDisable() {

    }

    /**
     * Get the structure handler for the plugin.
     *
     * @return The structure handler.
     */
    public StructureHandler getStructureHandler() {
        return structureHandler;
    }

    /**
     * Get the loot table handler.
     *
     * @return The loot table handler.
     */
    public LootTablesHandler getLootTableHandler() {
        return lootTablesHandler;
    }

    /**
     * Reload the handlers.
     * <p>This is for internal use only.</p>
     */
    public void reloadHandlers() {
        this.structureHandler = new StructureHandler(getConfig().getStringList("Structures"), this);
        this.lootTablesHandler = new LootTablesHandler();
    }

    /**
     * Get the instance of the main class.
     *
     * @return The main class.
     */
    public static CustomStructures getInstance() {
        return plugin;
    }

    private void loadManager() {
        Bukkit.getServer().getPluginManager().registerEvents(new ChunkLoad(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        getCommand("customstructure").setExecutor(new SCommand(this));
        getCommand("customstructure").setTabCompleter(new SCommandTab(this));
    }

    private void registerConfig() {
        saveDefaultConfig();
    }

    public void loadFile() {

        if (lootTablesfile.exists()) {
            try {
                lootTablesFC.load(lootTablesfile);

            } catch (IOException | InvalidConfigurationException e) {

                e.printStackTrace();
            }
        } else {
            saveResource("lootTables/lootTable.yml", false);
        }
    }

    /**
     * Updates the config to the latest version.
     * (Config version 4 to version 5)
     * or loot table version from 5 to 6.
     * This will be removed in future versions.
     */
    private void updateConfig(int ver) {
        getLogger().info("An older version of the plugin has been detected!");
        getLogger().info("Automatically converting old format into the new one.");
        // Update to new structure format.
        if (ver < 5) {
            getLogger().severe("Error: Your config is too old for the plugin to update.");
            getLogger().severe("Please use custom structures 1.5.4 or older before updating to the latest version.");
            getLogger().severe("The plugin will now disable itself.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Update to new loot table format.
        if (ver < 6) {
            getLogger().info("Updating loot tables.");
            File structDir = new File(getDataFolder(), "structures");
            if (!structDir.exists() && !structDir.isDirectory()) {
                getLogger().severe("An error occurred when trying to update the structure format: Unable to find structure directory! Does it exist?");
                return;
            }
            int structuresConverted = 0;
            Map<String, Pair<List<LootTableType>, Double>> alreadyConverted = new HashMap<>();
            for (File file : Objects.requireNonNull(structDir.listFiles())) {
                if (!file.getName().contains(".yml"))
                    continue;
                FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(file);
                if (!fileConfiguration.contains("LootTables"))
                    continue;
                Map<LootTableType, Map<String, Double>> lootTables = new HashMap<>();
                for (String lootName : fileConfiguration.getConfigurationSection("LootTables").getKeys(false)) {
                    if (LootTableType.exists(lootName)) break;
                    File lootFile = new File(getDataFolder(), "lootTables" + File.separator + lootName + ".yml");
                    if (!lootFile.exists()) {
                        getLogger().severe("An error has occurred when trying to update the structure format:" +
                                "\n Cannot find loot table: " + lootName + ".yml");
                        return;
                    }
                    FileConfiguration lootConfig = YamlConfiguration.loadConfiguration(lootFile);
                    if (!lootConfig.contains("Type")) {
                        if (alreadyConverted.containsKey(lootName)) {
                            for (LootTableType type : alreadyConverted.get(lootName).getLeft()) {
                                if (!lootTables.containsKey(type)) {
                                    Map<String, Double> lt = new HashMap<>();
                                    lt.put(lootName, alreadyConverted.get(lootName).getRight());
                                    lootTables.put(type, lt);
                                } else
                                    lootTables.get(type).put(lootName, alreadyConverted.get(lootName).getRight());
                            }
                        }
                        continue;
                    }
                    List<LootTableType> types = LootTableType.valueOfList(Objects.requireNonNull(lootConfig.getString("Type")));
                    for (LootTableType type : types) {
                        if (!lootTables.containsKey(type)) {
                            Map<String, Double> lt = new HashMap<>();
                            lt.put(lootName, fileConfiguration.getDouble("LootTables." + lootName));
                            lootTables.put(type, lt);
                        } else
                            lootTables.get(type).put(lootName, fileConfiguration.getDouble("LootTables." + lootName));
                    }
                    alreadyConverted.put(lootName, Pair.of(types, fileConfiguration.getDouble("LootTables." + lootName)));
                    lootConfig.set("Type", null);
                    try {
                        lootConfig.save(lootFile);
                    } catch (IOException ex) {
                        getLogger().severe("Error: Unable to save loot table file! Does the plugin have permission to edit that file?");
                    }
                }
                getLogger().info(file.getName() + ": " + lootTables.size() + "");
                fileConfiguration.set("LootTables", null);
                for (Map.Entry<LootTableType, Map<String, Double>> entry : lootTables.entrySet()) {
                    for (Map.Entry<String, Double> loot : entry.getValue().entrySet()) {
                        fileConfiguration.set(String.format("LootTables.%s.%s", entry.getKey().toString(), loot.getKey()), loot.getValue());
                    }
                }
                try {
                    fileConfiguration.save(file);
                } catch (IOException ex) {
                    getLogger().severe("Error: Unable to save changed structure file! Does the plugin have permission to edit that file?");
                }
                structuresConverted++;
            }
            getConfig().set("configversion", 6);
            saveConfig();
            getLogger().info("Successfully updated " + structuresConverted + " structures!");
        }
    }

    /**
     * If the plugin is in debug mode.
     *
     * @return If the plugin is in debug mode.
     */
    public boolean isDebug() {
        return debugMode;
    }

    /**
     * Get the custom item manager.
     *
     * @return The custom item manager.
     */
    public CustomItemManager getCustomItemManager() {
        return customItemManager;
    }
}
