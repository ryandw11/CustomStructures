package com.ryandw11.structure;

import com.ryandw11.structure.api.structaddon.CustomStructureAddon;
import com.ryandw11.structure.citizens.CitizensDisabled;
import com.ryandw11.structure.citizens.CitizensEnabled;
import com.ryandw11.structure.citizens.CitizensNpcHook;
import com.ryandw11.structure.commands.SCommand;
import com.ryandw11.structure.commands.SCommandTab;
import com.ryandw11.structure.ignoreblocks.*;
import com.ryandw11.structure.listener.ChunkLoad;
import com.ryandw11.structure.listener.PlayerJoin;
import com.ryandw11.structure.loottables.LootTableHandler;
import com.ryandw11.structure.loottables.customitems.CustomItemManager;
import com.ryandw11.structure.mythicalmobs.MMDisabled;
import com.ryandw11.structure.mythicalmobs.MMEnabled;
import com.ryandw11.structure.mythicalmobs.MythicalMobHook;
import com.ryandw11.structure.schematic.StructureSignHandler;
import com.ryandw11.structure.structure.StructureHandler;
import com.ryandw11.structure.utils.CSUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.io.FileUtils;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
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
 * @version 1.9.0
 */

public class CustomStructures extends JavaPlugin {

    public static CustomStructures plugin;
    public File lootTableFile = new File(getDataFolder() + "/lootTables/lootTable.yml");
    public FileConfiguration lootTablesFC = YamlConfiguration.loadConfiguration(lootTableFile);

    private MythicalMobHook mythicalMobHook;
    private CitizensNpcHook citizensNpcHook;

    private SignCommandsHandler signCommandsHandler;
    private NpcHandler npcHandler;
    private StructureHandler structureHandler;
    private LootTableHandler lootTableHandler;
    private CustomItemManager customItemManager;
    private IgnoreBlocks blockIgnoreManager;
    private AddonHandler addonHandler;
    private StructureSignHandler structureSignHandler;

    private Metrics metrics;

    private boolean debugMode;

    public static boolean enabled;
    private boolean initialized = false;

    /**
     * The current version of the compiled structure format.
     */
    public static final int COMPILED_STRUCT_VER = 1;

    /**
     * The current version of the structure configuration format.
     */
    public static final int CONFIG_VERSION = 9;

    private static boolean papiEnabled = false;

    @Override
    public void onEnable() {
        enabled = true;

        plugin = this;
        loadManager();
        registerConfig();
        setupBlockIgnore();

        // Small check to make sure that PlaceholderAPI is installed
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("Placeholder API found, placeholders supported.");
            CustomStructures.papiEnabled = true;
        } else {
            this.getLogger().info("PlaceholderAPI not found.");
        }


        // Setup Citizens dependency.
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            citizensNpcHook = new CitizensEnabled(this);
            getLogger().info("Citizens detected! Activating plugin hook!");
        } else {
            citizensNpcHook = new CitizensDisabled();
        }
        // Setup Mythic Mobs dependency.
        if (getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            mythicalMobHook = new MMEnabled();
            getLogger().info("MythicMobs detected! Activating plugin hook!");
        } else {
            mythicalMobHook = new MMDisabled();
        }

        loadFiles();
        debugMode = getConfig().getBoolean("debug");

        if (getConfig().getInt("configversion") < CONFIG_VERSION) {
            this.lootTableHandler = new LootTableHandler();
            updateConfig(getConfig().getInt("configversion"));
        }

        File f = new File(getDataFolder() + File.separator + "schematics");
        if (!f.exists()) {
            getLogger().info("Loading the plugin for the first time.");
            getLogger().info("A demo structure will be added! Please make sure to test out this plugin in a test world!");
        }

        exportResource(new File(getDataFolder(), "schematics"), "demo.schem", "schematics/");
        exportResource(new File(getDataFolder(), "structures"), "demo.yml", "structures/");
        exportResource(getDataFolder(), "npcs.yml", "");
        exportResource(getDataFolder(), "signcommands.yml", "");

        // Configure the handlers and managers.
        this.customItemManager = new CustomItemManager(this, new File(getDataFolder() + File.separator + "items" + File.separator + "customitems.yml"), new File(getDataFolder() + File.separator + "items"));
        this.signCommandsHandler = new SignCommandsHandler(getDataFolder(), this);
        this.npcHandler = new NpcHandler(getDataFolder(), plugin);
        this.lootTableHandler = new LootTableHandler();
        this.addonHandler = new AddonHandler();
        this.structureSignHandler = new StructureSignHandler();

        // Run this after the loading of all plugins.
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, this::initialize, 30);


        if (getConfig().getBoolean("bstats")) {
            metrics = new Metrics(this, 7056);

            getLogger().info("Bstat metrics for this plugin is enabled. Disable it in the config if you do not want it on.");
        } else {
            getLogger().info("Bstat metrics is disabled for this plugin.");
        }
    }

    /**
     * Initializes the plugin by creating the {@link StructureHandler}.
     *
     * <p>This is called internally after all plugins are initialized.</p>
     */
    public void initialize() {
        if (initialized) return;

        if (getConfig().getInt("configversion") != CONFIG_VERSION) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[CustomStructures] Cannot enable plugin, your config version is outdated. " +
                    "Check the above for errors that may have occurred during the auto-update process." + ChatColor.RESET);
            return;
        }

        this.structureHandler = new StructureHandler(getConfig().getStringList("Structures"), this);
        getLogger().info("The plugin has been fully enabled with " + structureHandler.getStructures().size() + " structures.");
        getLogger().info(addonHandler.getCustomStructureAddons().size() + " addons were found.");

        if (metrics != null) {
            // Add a custom pie chart to track the addons used.
            metrics.addCustomChart(new AdvancedPie("used_addons", () -> {
                Map<String, Integer> valueMap = new HashMap<>();
                for (CustomStructureAddon addon : addonHandler.getCustomStructureAddons()) {
                    valueMap.put(addon.getName(), 1);
                }
                return valueMap;
            }));
        }
        initialized = true;
    }

    /**
     * Resolves PAPI placeholders.
     *
     * @param text The text which may contain placeholders.
     * @return The final text.
     */
    public static String replacePAPIPlaceholders(String text) {
        if (papiEnabled) {
            return PlaceholderAPI.setPlaceholders(null, text);
        }
        return text;
    }

    /**
     * Export resources.
     *
     * @param targetDirectory The target directory.
     * @param filename        The file name.
     * @param resourcePath    The path to find the resource.
     */
    private void exportResource(File targetDirectory, String filename, String resourcePath) {
        if (!targetDirectory.exists()) {
            targetDirectory.mkdirs();
        }
        File targetFile = new File(targetDirectory, filename);
        if (!targetFile.exists()) {
            saveResource(resourcePath + filename, false);
        }
    }

    @Override
    public void onDisable() {
        if (structureHandler == null) {
            getLogger().severe("ERROR: The Structure Handler was never initialized during setup.");
            return;
        }

        structureHandler.cleanup();
        npcHandler.cleanUp();
        signCommandsHandler.cleanUp();
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
            case "v1_18_R2", "v1_18_R1", "v1_17_R1" -> blockIgnoreManager = new IgnoreBlocks_1_17();
            case "v1_16_R3", "v1_16_R2", "v1_16_R1" -> blockIgnoreManager = new IgnoreBlocks_1_16();
            case "v1_15_R1" -> blockIgnoreManager = new IgnoreBlocks_1_15();
            case "v1_14_R1" -> blockIgnoreManager = new IgnoreBlocks_1_14();
            case "v1_13_R1" -> blockIgnoreManager = new IgnoreBlocks_1_13();
            default -> blockIgnoreManager = new IgnoreBlocks_1_19();
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
    public LootTableHandler getLootTableHandler() {
        return lootTableHandler;
    }

    /**
     * Reload the handlers.
     * <p>This is for internal use only.</p>
     */
    public void reloadHandlers() {
        this.signCommandsHandler.cleanUp();
        this.signCommandsHandler = new SignCommandsHandler(getDataFolder(), this);
        this.npcHandler.cleanUp();
        this.npcHandler = new NpcHandler(getDataFolder(), plugin);
        this.structureHandler.cleanup();
        this.structureHandler = new StructureHandler(getConfig().getStringList("Structures"), this);
        this.lootTableHandler = new LootTableHandler();

        // Update the addons.
        this.addonHandler.handlePluginReload();
    }

    /**
     * Get the instance of the main class.
     *
     * @return The main class.
     */
    public static CustomStructures getInstance() {
        return plugin;
    }

    /**
     * Load commands and listeners.
     */
    private void loadManager() {
        Bukkit.getServer().getPluginManager().registerEvents(new ChunkLoad(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        Objects.requireNonNull(getCommand("customstructure")).setExecutor(new SCommand(this));
        Objects.requireNonNull(getCommand("customstructure")).setTabCompleter(new SCommandTab(this));
    }

    /**
     * Register the default config.
     */
    private void registerConfig() {
        saveDefaultConfig();
    }

    /**
     * Loaded needed files.
     */
    public void loadFiles() {
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
     *
     * <p>Currently, only converts 6->7 and 7->8.</p>
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
        if (ver < 7) {
            getLogger().severe("Error: Your config is too old for the plugin to update.");
            getLogger().severe("Please consult the plugin wiki to see what version to use to update your configuration files.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Convert to config version 8. (Only change is upper-casing File for SubSchematics)
        if (ver < 8) {
            getLogger().info("Updating all structure config files...");

            File structDir = new File(getDataFolder(), "structures");
            if (!structDir.exists() && !structDir.isDirectory()) {
                getLogger().severe("An error occurred when trying to update the structure format: Unable to find structure directory! Does it exist?");
                return;
            }

            File backupDirectory = new File(getDataFolder(), "backup");
            File backupdata = new File(backupDirectory, ".backups");
            if (!backupDirectory.exists()) {
                if (!backupDirectory.mkdir()) {
                    getLogger().severe("Error: Unable to create backup directory!");
                    return;
                }
            }

            if (!backupdata.exists()) {
                try {
                    backupdata.createNewFile();
                } catch (IOException ex) {
                    getLogger().severe("Error: Unable to create backup file.");
                    return;
                }
            }

            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(backupdata);

            if (fileConfiguration.contains("backupVer")) {
                int backupVer = fileConfiguration.getInt("backupVer");
                if (backupVer != 8) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "===============[CUSTOM STRUCTURES UPDATE]===============" + ChatColor.RESET);
                    getLogger().severe("Unable to update plugin! Backup data is outdated!");
                    getLogger().severe("Please delete the backup folder in the CustomStructures directory before continuing!");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "===============[CUSTOM STRUCTURES UPDATE]===============" + ChatColor.RESET);
                    return;
                }
            } else {
                fileConfiguration.set("backupVer", 8);
                try {
                    fileConfiguration.save(backupdata);
                } catch (IOException ex) {
                    getLogger().severe("A critical error has occurred while backing up the plugin data.");
                    return;
                }
            }

            List<String> updatedStructures = new ArrayList<>(fileConfiguration.getStringList("UpdatedStructures"));

            // This detects if there are any loaded structures, and alerts the user if this is an error.
            if (!updatedStructures.isEmpty()) {
                getLogger().info("Previous update attempt detected.");
                getLogger().info(String.format("%s completed structure updates were found. If this is your first time updating" +
                                " to this version of CustomStructures, then please delete the backup directory and restart the server.",
                        updatedStructures.size()));
                getLogger().info("The server will now wait 5 seconds to give you a chance to stop the server before " +
                        "the update automatically continues. Press ctrl+c to cancel running the server.");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    getLogger().info("Server shutdown detected. Stopping update.");
                    return;
                }
            }

            createBackupForFile("config.yml", "/backup/config.yml.backup");
            for (String s : getConfig().getStringList("Structures")) {
                if (updatedStructures.contains(s)) continue;

                try {
                    createBackupForFile("/structures/" + s + ".yml", "/backup/" + s + ".yml.backup");

                    FileConfiguration structConfig = YamlConfiguration.loadConfiguration(new File(structDir, s + ".yml"));

                    if (!structConfig.contains("SubSchematics.Schematics")) {
                        continue;
                    }

                    for (String struct : structConfig.getConfigurationSection("SubSchematics.Schematics").getKeys(false)) {
                        ConfigurationSection section = structConfig.getConfigurationSection("SubSchematics.Schematics." + struct);
                        if (!section.contains("file")) {
                            getLogger().severe(String.format("An error has occurred when attempting to update structure %s!", s));
                            getLogger().severe(String.format("Cannot find 'file' option on %s when update the SubSchematics property!", struct));
                            getLogger().severe("After fixing the error, restart the server for the plugin to continue updating" +
                                    " from where it left off.");
                            return;
                        }
                        section.set("File", section.getString("file"));
                        section.set("file", null);
                    }

                    try {
                        structConfig.save(new File(structDir, s + ".yml"));
                    } catch (IOException ex) {
                        getLogger().info(String.format("An error has occurred when updating %s!", s));
                        getLogger().severe("Error: unable to save updated structure file!");
                        return;
                    }
                    getLogger().info(String.format("Successfully updated the structure: %s!", s));
                    // Add the updated structure to the list.
                    updatedStructures.add(s);
                    fileConfiguration.set("UpdatedStructures", updatedStructures);
                    fileConfiguration.save(backupdata);
                } catch (Exception ex) {
                    getLogger().severe(String.format("An error has occurred when updating %s:", s));
                    ex.printStackTrace();
                    getLogger().severe("After fixing the error, restart the server for the plugin to continue updating" +
                            " from where it left off.");
                    return;
                }
            }

            getConfig().set("configversion", 8);
            saveConfig();

            getLogger().info("Successfully updated all structure files to the latest version (8).");
            getLogger().info("Please delete the backup folder that was created in the CustomStructures directory" +
                    " after you confirm everything was updated correctly.");
        }
        // Convert to config version 9. (Masks to SourceMask, Standardize Config, Updated Loot Tables)
        if (ver < 9) {
            getLogger().info("Updating all structure config files...");

            File structDir = new File(getDataFolder(), "structures");
            if (!structDir.exists() && !structDir.isDirectory()) {
                getLogger().severe("An error occurred when trying to update the structure format: Unable to find structure directory! Does it exist?");
                return;
            }

            File backupDirectory = new File(getDataFolder(), "backup");
            File backupData = new File(backupDirectory, ".backups");
            if (!backupDirectory.exists()) {
                if (!backupDirectory.mkdir()) {
                    getLogger().severe("Error: Unable to create backup directory!");
                    return;
                }
            }

            if (!backupData.exists()) {
                try {
                    backupData.createNewFile();
                } catch (IOException ex) {
                    getLogger().severe("Error: Unable to create backup file.");
                    return;
                }
            }

            FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(backupData);

            if (fileConfiguration.contains("backupVer")) {
                int backupVer = fileConfiguration.getInt("backupVer");
                if (backupVer != 9) {
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "===============[CUSTOM STRUCTURES UPDATE]===============" + ChatColor.RESET);
                    getLogger().severe("Unable to update plugin! Backup data is outdated!");
                    getLogger().severe("Please delete the backup folder in the CustomStructures directory before continuing!");
                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "===============[CUSTOM STRUCTURES UPDATE]===============" + ChatColor.RESET);
                    return;
                }
            } else {
                fileConfiguration.set("backupVer", 9);
                try {
                    fileConfiguration.save(backupData);
                } catch (IOException ex) {
                    getLogger().severe("A critical error has occurred while backing up the plugin data.");
                    return;
                }
            }

            // Check if the updates were finished.
            if (!fileConfiguration.getBoolean("finishedStructureUpdates", false)) {
                List<String> updatedStructures = new ArrayList<>(fileConfiguration.getStringList("UpdatedStructures"));

                // This detects if there are any loaded structures, and alerts the user if this is an error.
                if (!updatedStructures.isEmpty()) {
                    getLogger().info("Previous update attempt detected.");
                    getLogger().info(String.format("%s completed structure updates were found. If this is your first time updating" +
                                    " to this version of CustomStructures, then please delete the backup directory and restart the server.",
                            updatedStructures.size()));
                    getLogger().info("The server will now wait 5 seconds to give you a chance to stop the server before " +
                            "the update automatically continues. Press ctrl+c to cancel running the server.");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        getLogger().info("Server shutdown detected. Stopping update.");
                        return;
                    }
                }

                createBackupForFile("config.yml", "/backup/config.yml.backup");
                for (String s : getConfig().getStringList("Structures")) {
                    if (updatedStructures.contains(s)) continue;

                    try {
                        createBackupForFile("/structures/" + s + ".yml", "/backup/" + s + ".yml.backup");

                        FileConfiguration structConfig = YamlConfiguration.loadConfiguration(new File(structDir, s + ".yml"));

                        CSUtils.renameConfigString(structConfig, "schematic", "Schematic");
                        CSUtils.renameConfigString(structConfig, "compiled_schematic", "CompiledSchematic");

                        CSUtils.renameConfigInteger(structConfig, "Chance.Number", "Probability.Numerator");
                        CSUtils.renameConfigInteger(structConfig, "Chance.OutOf", "Probability.Denominator");
                        structConfig.set("Chance", null);

                        CSUtils.renameConfigInteger(structConfig, "StructureLocation.spawn_distance.x", "StructureLocation.SpawnDistance.x");
                        CSUtils.renameConfigInteger(structConfig, "StructureLocation.spawn_distance.z", "StructureLocation.SpawnDistance.z");
                        structConfig.set("StructureLocation.spawn_distance", null);

                        // Structure Properties
                        CSUtils.renameConfigBoolean(structConfig, "StructureProperties.randomRotation", "StructureProperties.RandomRotation");
                        CSUtils.renameConfigBoolean(structConfig, "StructureProperties.ignorePlants", "StructureProperties.IgnorePlants");
                        CSUtils.renameConfigBoolean(structConfig, "StructureProperties.spawnInWater", "StructureProperties.SpawnInWater");
                        CSUtils.renameConfigBoolean(structConfig, "StructureProperties.spawnInLavaLakes", "StructureProperties.SpawnInLavaLakes");
                        CSUtils.renameConfigBoolean(structConfig, "StructureProperties.spawnInVoid", "StructureProperties.SpawnInVoid");
                        CSUtils.renameConfigBoolean(structConfig, "StructureProperties.ignoreWater", "StructureProperties.IgnoreWater");

                        // Structure Limitations
                        CSUtils.renameConfigStringList(structConfig, "StructureLimitations.whitelistSpawnBlocks", "StructureLimitations.WhitelistSpawnBlocks");
                        CSUtils.renameConfigStringList(structConfig, "StructureLimitations.blacklistSpawnBlocks", "StructureLimitations.BlacklistSpawnBlocks");
                        CSUtils.renameConfigString(structConfig, "StructureLimitations.BlockLevelLimit.mode", "StructureLimitations.BlockLevelLimit.Mode");
                        CSUtils.renameConfigInteger(structConfig, "StructureLimitations.BlockLevelLimit.cornerOne.x", "StructureLimitations.BlockLevelLimit.CornerOne.x");
                        CSUtils.renameConfigInteger(structConfig, "StructureLimitations.BlockLevelLimit.cornerOne.z", "StructureLimitations.BlockLevelLimit.CornerOne.z");
                        CSUtils.renameConfigInteger(structConfig, "StructureLimitations.BlockLevelLimit.cornerTwo.x", "StructureLimitations.BlockLevelLimit.CornerTwo.x");
                        CSUtils.renameConfigInteger(structConfig, "StructureLimitations.BlockLevelLimit.cornerTwo.z", "StructureLimitations.BlockLevelLimit.CornerTwo.z");
                        CSUtils.renameConfigInteger(structConfig, "StructureLimitations.BlockLevelLimit.error", "StructureLimitations.BlockLevelLimit.Error");
                        structConfig.set("StructureLimitations.BlockLevelLimit.cornerOne", null);
                        structConfig.set("StructureLimitations.BlockLevelLimit.cornerTwo", null);
                        CSUtils.renameStringConfigurationSection(structConfig, "StructureLimitations.replacement_blocks", "StructureLimitations.ReplaceBlocks");
                        CSUtils.renameConfigInteger(structConfig, "StructureLimitations.replacement_blocks_delay", "StructureLimitations.ReplaceBlockDelay");
                        CSUtils.renameConfigInteger(structConfig, "StructureLimitations.iterationLimit", "StructureLimitations.IterationLimit");

                        // Update SubSchematics
                        if(structConfig.contains("SubSchematics")) {
                            if(!structConfig.getBoolean("SubSchematics.Enabled", true) || structConfig.getConfigurationSection("SubSchematics.Schematics") == null) {
                                structConfig.set("SubSchematics", null);
                            }
                            else {
                                structConfig.set("SubSchematics.Enabled", null);
                                for(String key : structConfig.getConfigurationSection("SubSchematics.Schematics").getKeys(false)) {
                                    ConfigurationSection section = structConfig.getConfigurationSection("SubSchematics.Schematics."+ key);
                                    structConfig.set("SubSchematics." + key, section);
                                }
                                structConfig.set("SubSchematics.Schematics", null);
                            }
                        }

                    /*

                     Update Masks

                     */

                        if (structConfig.contains("Masks")) {
                            structConfig.set("SourceMask.Enabled", structConfig.getBoolean("Masks.enabled"));
                            structConfig.set("SourceMask.UnionType", structConfig.getString("Masks.union_type"));
                            structConfig.set("SourceMask.BlockTypeMask", structConfig.getStringList("Masks.BlockTypeMask"));
                            structConfig.set("SourceMask.NegatedBlockMask", structConfig.getStringList("Masks.NegatedBlockMask"));
                            structConfig.set("Masks", null);
                        }

                        try {
                            structConfig.save(new File(structDir, s + ".yml"));
                        } catch (IOException ex) {
                            getLogger().info(String.format("An error has occurred when updating %s!", s));
                            getLogger().severe("Error: unable to save updated structure file!");
                            return;
                        }
                        getLogger().info(String.format("Successfully updated the structure: %s!", s));
                        // Add the updated structure to the list.
                        updatedStructures.add(s);
                        fileConfiguration.set("UpdatedStructures", updatedStructures);
                        fileConfiguration.save(backupData);
                    } catch (Exception ex) {
                        getLogger().severe(String.format("An error has occurred when updating %s.", s));
                        ex.printStackTrace();
                        getLogger().severe("After fixing the error, restart the server for the plugin to continue updating" +
                                " from where it left off.");
                        return;
                    }
                }

                fileConfiguration.set("finishedStructureUpdates", true);
                try {
                    fileConfiguration.save(backupData);
                } catch (IOException ex) {
                    getLogger().severe("An error has occurred when trying to save the backup file!");
                    ex.printStackTrace();
                    getLogger().severe("After fixing the error, restart the server for the plugin to continue updating" +
                            " from where it left off.");
                }
            }

            // Update Loot Tables.

            File lootTableFolder = new File(getDataFolder(), "lootTables");
            File[] lootTableFiles = lootTableFolder.listFiles();

            if (lootTableFiles != null) {
                List<String> updatedLootTables = new ArrayList<>(fileConfiguration.getStringList("UpdatedLootTables"));
                for (File file : lootTableFiles) {
                    if (!file.getName().endsWith(".yml")) {
                        continue;
                    }

                    String lootTableName = file.getName().replace(".yml", "");

                    try {
                        if (updatedLootTables.contains(lootTableName))
                            continue;

                        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                        for (String key : configuration.getConfigurationSection("Items").getKeys(false)) {
                            ConfigurationSection section = configuration.getConfigurationSection("Items." + key);
                            String type = section.getString("Type", "AIR");
                            if (type.equalsIgnoreCase("CUSTOM")) {
                                section.set("Type", "CUSTOM");
                            } else {
                                section.set("Material", type);
                                section.set("Type", "STANDARD");
                            }
                        }

                        configuration.save(file);

                        getLogger().info(String.format("Successfully updated the loot table: %s!", lootTableName));

                        updatedLootTables.add(lootTableName);
                        fileConfiguration.set("UpdatedLootTables", updatedLootTables);
                        fileConfiguration.save(backupData);
                    } catch (Exception ex) {
                        getLogger().severe(String.format("An error has occurred when updating the loot table %s.", lootTableName));
                        ex.printStackTrace();
                        getLogger().severe("After fixing the error, restart the server for the plugin to continue updating" +
                                " from where it left off.");
                        return;
                    }
                }
            }

            getConfig().set("configversion", 9);
            saveConfig();

            getLogger().info("Successfully updated all structure files to the latest version (9).");
            getLogger().info("Please delete the backup folder that was created in the CustomStructures directory" +
                    " after you confirm everything was updated correctly.");
        }
    }

    /**
     * Create a backup of a certain file.
     *
     * @param file       The file to backup
     * @param backupFile The location for the backup file.
     * @return If the backup was successful.
     */
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
     * @return The commands config handler
     */
    public SignCommandsHandler getSignCommandsHandler() {
        return signCommandsHandler;
    }

    /**
     * @return The NPC config handler
     */
    public NpcHandler getNpcHandler() {
        return npcHandler;
    }

    /**
     * Get the addon handler for the plugin.
     *
     * @return The addon handler.
     */
    public AddonHandler getAddonHandler() {
        return addonHandler;
    }

    /**
     * Get the handler for registering custom structure signs.
     *
     * @return The handler for registering custom structure signs.
     */
    public StructureSignHandler getStructureSignHandler() {
        return structureSignHandler;
    }

    /**
     * Get the hook for mythical mobs.
     *
     * @return The hook for mythical mobs.
     */
    public MythicalMobHook getMythicalMobHook() {
        return mythicalMobHook;
    }

    /**
     * Get the hook for citizens.
     *
     * @return The hook for citizens.
     */
    public CitizensNpcHook getCitizensNpcHook() {
        return citizensNpcHook;
    }
}
