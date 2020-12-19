package com.ryandw11.structure;

import com.ryandw11.structure.bstats.Metrics;
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
import com.ryandw11.structure.structure.StructureBuilder;
import com.ryandw11.structure.structure.StructureHandler;
import com.ryandw11.structure.structure.properties.*;
import com.ryandw11.structure.utils.Pair;
import com.ryandw11.structure.utils.RandomCollection;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
            List<String> structures = new ArrayList<>();
            File config = new File(getDataFolder(), "config.yml");
            File configBackup = new File(getDataFolder(), "config.yml.backup");
            File structuresDir = new File(getDataFolder(), "structures");
            try {
                configBackup.createNewFile();
                FileUtils.copyFile(config, configBackup);
                if (!structuresDir.exists())
                    structuresDir.mkdir();
            } catch (IOException ex) {
                getLogger().severe("The config converter failed to create a backup of the config!");
                getLogger().severe("Custom Structures will now disable itself.");
                getServer().getPluginManager().disablePlugin(this);
                if (debugMode)
                    ex.printStackTrace();
                return;
            }
            for (String key : Objects.requireNonNull(getConfig().getConfigurationSection("Schematics")).getKeys(false)) {
                ConfigurationSection section = getConfig().getConfigurationSection("Schematics." + key);
                File file = new File(getDataFolder() + File.separator + "structures" + File.separator + key + ".yml");
                assert section != null;
                StructureBuilder builder = new StructureBuilder(key, section.getString("Schematic"));

                builder.setChance(section.getInt("Chance.Number"), section.getInt("Chance.OutOf"));

                if (section.contains("whitelistSpawnBlocks"))
                    builder.setStructureLimitations(new StructureLimitations(section.getStringList("whitelistSpawnBlocks"), new BlockLevelLimit(), new HashMap<>()));
                else
                    builder.setStructureLimitations(new StructureLimitations(new ArrayList<>(), new BlockLevelLimit(), new HashMap<>()));

                StructureLocation structLocation = new StructureLocation();
                if (section.contains("AllowedWorlds") && section.contains("AllWorlds")) {
                    if (!section.getBoolean("AllWorlds")) {
                        structLocation.setWorlds(section.getStringList("AllowedWorlds"));
                    }
                }
                if (section.contains("SpawnY")) {
                    structLocation.setSpawnSettings(new StructureYSpawning(Objects.requireNonNull(section.getString("SpawnY"))));
                }
                if (section.contains("Biome")) {
                    String biomeValue = section.getString("Biome");
                    assert biomeValue != null;
                    if (!biomeValue.equalsIgnoreCase("all")) {
                        String[] biomes = biomeValue.split(",");
                        structLocation.setBiomes(new ArrayList<>(Arrays.asList(biomes)));
                    }
                }
                builder.setStructureLocation(structLocation);

                StructureProperties structProperties = new StructureProperties();
                structProperties.setIgnorePlants(section.getBoolean("ignorePlants"));
                structProperties.setPlaceAir(section.getBoolean("PlaceAir"));
                structProperties.setRandomRotation(section.getBoolean("randomRotation"));
                structProperties.setSpawnInWater(section.getBoolean("Ocean_Properties.spawnInLiquid"));
                builder.setStructureProperties(structProperties);

                if (section.contains("LootTables"))
                    builder.setLootTables(Objects.requireNonNull(section.getConfigurationSection("LootTables")));
                else
                    builder.setLootTables(new RandomCollection<>());

                try {
                    builder.save(file);
                } catch (IOException ex) {
                    getLogger().severe("The config converter failed to convert a structure to the new format!");
                    getLogger().severe("The structure that failed is " + key);
                    getLogger().severe("Custom Structures will now disable itself.");
                    getServer().getPluginManager().disablePlugin(this);
                    if (debugMode)
                        ex.printStackTrace();
                    return;
                }
                structures.add(key);
            }
            getConfig().set("Schematics", null);
            getConfig().set("Structures", structures);
            getConfig().set("configversion", 5);
            try {
                getConfig().save(plugin.getDataFolder() + File.separator + "config.yml");
            } catch (IOException ex) {
                getLogger().severe("An error has occurred when trying to save the new config.");
                getLogger().severe("The plugin will now disable itself.");
                getServer().getPluginManager().disablePlugin(this);
                if (debugMode)
                    ex.printStackTrace();
                return;
            }
            reloadConfig();
            getLogger().info("Successfully converted " + structures.size() + " structures to the new format!");
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
                    if(!lootConfig.contains("Type")){
                        if(alreadyConverted.containsKey(lootName)){
                            for(LootTableType type : alreadyConverted.get(lootName).getLeft()){
                                if(!lootTables.containsKey(type)){
                                    Map<String, Double> lt = new HashMap<>();
                                    lt.put(lootName, alreadyConverted.get(lootName).getRight());
                                    lootTables.put(type, lt);
                                }
                                else
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
                    try{
                        lootConfig.save(lootFile);
                    }catch (IOException ex){
                        getLogger().severe("Error: Unable to save loot table file! Does the plugin have permission to edit that file?");
                    }
                }
                getLogger().info(file.getName() + ": " + lootTables.size() + "");
                fileConfiguration.set("LootTables", null);
                for(Map.Entry<LootTableType, Map<String, Double>> entry : lootTables.entrySet()){
                	for(Map.Entry<String,Double> loot : entry.getValue().entrySet()){
                		fileConfiguration.set(String.format("LootTables.%s.%s", entry.getKey().toString(), loot.getKey()), loot.getValue());
					}
				}
                try {
                    fileConfiguration.save(file);
                }catch (IOException ex){
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
