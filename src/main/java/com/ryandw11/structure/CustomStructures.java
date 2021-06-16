package com.ryandw11.structure;

import com.ryandw11.structure.commands.SCommand;
import com.ryandw11.structure.commands.SCommandTab;
import com.ryandw11.structure.ignoreblocks.*;
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
import com.ryandw11.structure.utils.SpawnYConversion;
import org.apache.commons.io.FileUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The main class for the Custom Structures plugin.
 *
 * @author Ryandw11
 * @version 1.6.0-SNAPSHOT
 */

public class CustomStructures extends JavaPlugin {

    public static CustomStructures plugin;
    public File lootTableFile = new File(getDataFolder() + "/lootTables/lootTable.yml");
    public FileConfiguration lootTablesFC = YamlConfiguration.loadConfiguration(lootTableFile);

    public MythicalMobHook mmh;

    private StructureHandler structureHandler;
    private LootTablesHandler lootTablesHandler;
    private CustomItemManager customItemManager;
    private IgnoreBlocks blockIgnoreManager;
    private AddonHandler addonHandler;

    private boolean debugMode;

    public static boolean enabled;

    public static final int COMPILED_STRUCT_VER = 1;
    public static final int CONFIG_VERSION = 7;

    @Override
    public void onEnable() {
        enabled = true;

        plugin = this;
        loadManager();
        registerConfig();
        setupBlockIgnore();

        if (getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            mmh = new MMEnabled();
            getLogger().info("MythicMobs detected! Activating plugin hook!");
        } else
            mmh = new MMDisabled();
        loadFile();
        debugMode = getConfig().getBoolean("debug");

        if (getConfig().getInt("configversion") < CONFIG_VERSION) {
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
        this.addonHandler = new AddonHandler();
        // Run this after the loading of all plugins.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.structureHandler = new StructureHandler(getConfig().getStringList("Structures"), this);
            getLogger().info("The plugin has been fully enabled with " + structureHandler.getStructures().size() + " structures.");
            getLogger().info(addonHandler.getCustomStructureAddons().size() + " addons were found.");
        }, 20);


        if (getConfig().getBoolean("bstats")) {
            new Metrics(this, 7056);
            getLogger().info("Bstat metrics for this plugin is enabled. Disable it in the config if you do not want it on.");
        } else {
            getLogger().info("Bstat metrics is disabled for this plugin.");
        }
    }

    @Override
    public void onDisable() {
        structureHandler.cleanup();
    }

    /**
     * Set up block ignore depending on the Minecraft version.
     */
    private void setupBlockIgnore() {
        String version;
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException ex) {
            getLogger().severe("Unable to detect Minecraft version! The plugin will now be disabled.");
            getPluginLoader().disablePlugin(this);
            return;
        }

        // Initialize blockIgnoreManager with the proper class for the version.
        switch (version) {
            case "v1_16_R3":
            case "v1_16_R2":
            case "v1_16_R1":
                blockIgnoreManager = new IgnoreBlocks_1_16();
                break;
            case "v1_15_R1":
                blockIgnoreManager = new IgnoreBlocks_1_15();
                break;
            case "v1_14_R1":
                blockIgnoreManager = new IgnoreBlocks_1_14();
                break;
            case "v1_13_R1":
                blockIgnoreManager = new IgnoreBlocks_1_13();
                break;
            default:
                blockIgnoreManager = new IgnoreBlocks_1_17();
                break;
        }
    }

    /**
     * Get the blocks the plugin should ignore for the server's minecraft version.
     *
     * @return The proper block ignore list.
     */
    public IgnoreBlocks getBlockIgnoreManager() {
        return blockIgnoreManager;
    }

    /**
     * Get the structure handler for the plugin.
     *
     * <p>This will be null until after all plugins have been enabled.</p>
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
        this.structureHandler.cleanup();
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
        Objects.requireNonNull(getCommand("customstructure")).setExecutor(new SCommand(this));
        Objects.requireNonNull(getCommand("customstructure")).setTabCompleter(new SCommandTab(this));
    }

    private void registerConfig() {
        saveDefaultConfig();
    }

    public void loadFile() {

        if (lootTableFile.exists()) {
            try {
                lootTablesFC.load(lootTableFile);

            } catch (IOException | InvalidConfigurationException e) {

                e.printStackTrace();
            }
        } else {
            saveResource("lootTables/lootTable.yml", false);
        }
    }

    /**
     * Updates the config to the latest version.
     * (Also currently updates loot tables from 5 to 6)
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
        if (ver < 7) {
            getLogger().info("Updating all structure config files...");

            File structDir = new File(getDataFolder(), "structures");
            if (!structDir.exists() && !structDir.isDirectory()) {
                getLogger().severe("An error occurred when trying to update the structure format: Unable to find structure directory! Does it exist?");
                return;
            }

            List<String> updatedStructures = new ArrayList<>();

            File backupDirectory = new File(getDataFolder(), "backup");
            File backupdata = new File(backupDirectory, ".backups");
            if(!backupDirectory.exists()){
                if(!backupDirectory.mkdir()) {
                    getLogger().severe("Error: Unable to create backup directory!");
                    return;
                }
            }

            if(!backupdata.exists()) {
                try{
                    backupdata.createNewFile();
                }catch (IOException ex) {
                    getLogger().severe("Error: Unable to create backup file.");
                    return;
                }
            }

            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(backupdata);
            List<String> loadedStructures = new ArrayList<>(fileConfiguration.getStringList("UpdatedStructures"));

            // This detects if there are any loaded structures, and alerts the user if this is an error.
            if(!loadedStructures.isEmpty()) {
                getLogger().info("Previous update attempt detected.");
                getLogger().info(String.format("%s completed structure updates were found. If this is your first time updating" +
                        " to this version of CustomStructures, then please delete the backup directory and restart the server.",
                        loadedStructures.size()));
                getLogger().info("The server will now wait 5 seconds to give you a chance to stop the server before " +
                        "the update automatically continues. Press ctrl+c to cancel running the server.");
                try{
                    Thread.sleep(5000);
                }catch (InterruptedException ex) {
                    getLogger().info("Server shutdown detected. Stopping update.");
                    return;
                }
            }

            createBackupForFile("config.yml", "/backup/config.yml.backup");
            for(String s : getConfig().getStringList("Structures")) {
                if(loadedStructures.contains(s)) continue;

                try {
                    createBackupForFile("/structures/" + s + ".yml", "/backup/" + s + ".yml.backup");

                    FileConfiguration structConfig = YamlConfiguration.loadConfiguration(new File(structDir, s + ".yml"));

                    if(!structConfig.contains("StructureLocation.SpawnY")) {
                        getLogger().severe("Error: unable to find SpawnY value, please fix that issue and restart the server.");
                        return;
                    }
                    String spawnY = structConfig.getString("StructureLocation.SpawnY");
                    assert spawnY != null;
                    String newSpawnY = SpawnYConversion.convertSpawnYValue(spawnY);
                    structConfig.set("StructureLocation.SpawnY", newSpawnY);
                    try{
                        structConfig.save(new File(structDir, s + ".yml"));
                    }catch (IOException ex) {
                        getLogger().info(String.format("An error has occurred when updating %s!", s));
                        getLogger().severe("Error: unable to save updated structure file!");
                        return;
                    }
                    getLogger().info(String.format("Successfully updated the structure: %s!", s));
                    // Add the updated structure to the list.
                    updatedStructures.add(s);
                    fileConfiguration.set("UpdatedStructures", updatedStructures);
                    fileConfiguration.save(backupdata);
                }catch (Exception ex) {
                    getLogger().severe(String.format("An error has occurred when updating %s:", s));
                    ex.printStackTrace();
                    getLogger().severe("After fixing the error, restart the server for the plugin to continue updating" +
                            " from where it left off.");
                    return;
                }
            }

            getConfig().set("configversion", 7);
            saveConfig();

            getLogger().info("Successfully updated all structure files to the latest version.");
            getLogger().info("Please delete the backup folder that was created in the CustomStructures directory" +
                    " after you confirm everything was updated correctly.");



            // TODO implement a version updater
        }
    }

    private boolean createBackupForFile(String file, String backupFile) {
        File config = new File(getDataFolder(), file);
        File configBackup = new File(getDataFolder(), backupFile);
        try {
            configBackup.createNewFile();
            FileUtils.copyFile(config, configBackup);
        } catch (IOException ex) {
            getLogger().severe("A critical error was encountered when attempting to update plugin configuration" +
                    " files!");
            getLogger().severe("Unable to create a backup for " + file);

            return false;
        }

        return true;
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

    /**
     * Get the addon handler for the plugin.
     *
     * @return The addon handler.
     */
    public AddonHandler getAddonHandler() {
        return addonHandler;
    }
}
