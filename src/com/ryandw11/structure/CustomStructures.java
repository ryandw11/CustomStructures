package com.ryandw11.structure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.ryandw11.structure.loottables.customitems.CustomItemManager;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.StructureBuilder;
import com.ryandw11.structure.structure.StructureHandler;
import com.ryandw11.structure.structure.properties.StructureLimitations;
import com.ryandw11.structure.structure.properties.StructureLocation;
import com.ryandw11.structure.structure.properties.StructureProperties;
import com.ryandw11.structure.structure.properties.StructureYSpawning;
import com.ryandw11.structure.utils.RandomCollection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;

import com.ryandw11.structure.bstats.Metrics;
import com.ryandw11.structure.commands.SCommand;
import com.ryandw11.structure.listener.ChunkLoad;
import com.ryandw11.structure.listener.PlayerJoin;
import com.ryandw11.structure.loottables.LootTablesHandler;
import com.ryandw11.structure.mythicalmobs.MMDisabled;
import com.ryandw11.structure.mythicalmobs.MMEnabled;
import com.ryandw11.structure.mythicalmobs.MythicalMobHook;

/**
 * 
 * @author Ryandw11
 * @version 1.5
 *
 */

public class CustomStructures extends JavaPlugin {

	public static CustomStructures plugin;

	public File exfile = new File(getDataFolder() + "/schematics/Put_Schematics_In_Here.yml");
	public File lootTablesfile = new File(getDataFolder() + "/lootTables/lootTable.yml");
	public FileConfiguration ex = YamlConfiguration.loadConfiguration(exfile);
	public FileConfiguration lootTablesFC = YamlConfiguration.loadConfiguration(lootTablesfile);

	public MythicalMobHook mmh;

	private StructureHandler structureHandler;
	private LootTablesHandler lootTablesHandler;
	private CustomItemManager customItemManager;
	private boolean debugMode;
	
	public static boolean enabled;

	@Override
	public void onEnable() {
		enabled = true;

		plugin = this;
		loadManager();
		registerConfig();
		
		if(getServer().getPluginManager().getPlugin("MythicMobs") != null) {
			mmh = new MMEnabled();
			getLogger().info("MythicMobs detected! Activating plugin hook!");
		}else
			mmh = new MMDisabled();
		loadFile();
		debugMode = getConfig().getBoolean("debug");
		if(getConfig().getInt("configversion") < 5){
			lootTablesHandler = new LootTablesHandler();
			updateConfig();
		}

		File f = new File(getDataFolder() + File.separator + "structures");
		if(!f.exists()){
			saveResource("structures/demo.yml", false);
		}
		f = new File(getDataFolder() + File.separator + "schematics");
		if(!f.exists()){
			saveResource("schematics/demo.schem", false);
			getLogger().info("Loading the plugin for the first time.");
			getLogger().info("A demo structure was added! Please make sure to test out this plugin in a test world!");
		}

		this.customItemManager = new CustomItemManager(this, new File(getDataFolder() + File.separator + "items" + File.separator + "customitems.yml"), new File(getDataFolder() + File.separator + "items"));

		lootTablesHandler = new LootTablesHandler();
		this.structureHandler = new StructureHandler(getConfig().getStringList("Structures"), this);

		getLogger().info("The plugin has been enabled with " + structureHandler.getStructures().size() + " structures.");
		
		if(getConfig().getBoolean("bstats")){
			new Metrics(this, 7056);
			getLogger().info("Bstat metrics for this plugin is enabled. Disable it in the config if you do not want it on.");
		}else{
			getLogger().info("Bstat metrics is disabled for this plugin.");
		}
	}

	@Override
	public void onDisable() {
		saveFile();
	}

	/**
	 * Get the structure handler for the plugin.
	 * @return The structure handler.
	 */
	public StructureHandler getStructureHandler(){
		return structureHandler;
	}

	/**
	 * Get the loot table handler.
	 * @return The loot table handler.
	 */
	public LootTablesHandler getLootTableHandler(){
		return lootTablesHandler;
	}

	/**
	 * Reload the handlers.
	 * <p>This is for internal use only.</p>
	 */
	public void reloadHandlers(){
		this.structureHandler = new StructureHandler(getConfig().getStringList("Structures"), this);
		this.lootTablesHandler = new LootTablesHandler();
	}

	/**
	 * Get the instance of the main class.
	 * @return The main class.
	 */
	public static CustomStructures getInstance(){
		return plugin;
	}

	private void loadManager() {
		Bukkit.getServer().getPluginManager().registerEvents(new ChunkLoad(), this);
		Bukkit.getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
		getCommand("customstructure").setExecutor(new SCommand(this));
	}

	private void registerConfig() {
		saveDefaultConfig();
	}

	public void saveFile() {
		try {
			ex.save(exfile);
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	public void loadFile() {
		if (exfile.exists()) {
			try {
				ex.load(exfile);

			} catch (IOException | InvalidConfigurationException e) {

				e.printStackTrace();
			}
		} else {
			try {
				ex.save(exfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

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
	 * This will be removed in future versions.
	 */
	private void updateConfig(){
		getLogger().info("An older version of the plugin has been detected!");
		getLogger().info("Automatically converting old format into the new one.");

		List<String> structures = new ArrayList<>();
		File config = new File(getDataFolder(), "config.yml");
		File configBackup = new File(getDataFolder(), "config.yml.backup");
		File structuresDir = new File(getDataFolder(), "structures");
		try{
			configBackup.createNewFile();
			FileUtils.copyFile(config, configBackup);
			if(!structuresDir.exists())
				structuresDir.mkdir();
		}catch(IOException ex){
			getLogger().severe("The config converter failed to create a backup of the config!");
			getLogger().severe("Custom Structures will now disable itself.");
			getServer().getPluginManager().disablePlugin(this);
			if(debugMode)
				ex.printStackTrace();
			return;
		}
		for(String key : Objects.requireNonNull(getConfig().getConfigurationSection("Schematics")).getKeys(false)){
			ConfigurationSection section = getConfig().getConfigurationSection("Schematics." + key);
			File file = new File(getDataFolder() + File.separator + "structures" + File.separator + key + ".yml");
			assert section != null;
			StructureBuilder builder = new StructureBuilder(key, section.getString("Schematic"));

			builder.setChance(section.getInt("Chance.Number"), section.getInt("Chance.OutOf"));

			if(section.contains("whitelistSpawnBlocks"))
				builder.setStructureLimitations(new StructureLimitations(section.getStringList("whitelistSpawnBlocks")));
			else
				builder.setStructureLimitations(new StructureLimitations(new ArrayList<>()));

			StructureLocation structLocation = new StructureLocation();
			if(section.contains("AllowedWorlds") && section.contains("AllWorlds")){
				if(!section.getBoolean("AllWorlds")){
					structLocation.setWorlds(section.getStringList("AllowedWorlds"));
				}
			}
			if(section.contains("SpawnY")){
				structLocation.setSpawnSettings(new StructureYSpawning(Objects.requireNonNull(section.getString("SpawnY"))));
			}
			if(section.contains("Biome")){
				String biomeValue = section.getString("Biome");
				assert biomeValue != null;
				if(!biomeValue.equalsIgnoreCase("all")){
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

			if(section.contains("LootTables"))
				builder.setLootTables(Objects.requireNonNull(section.getConfigurationSection("LootTables")));
			else
				builder.setLootTables(new RandomCollection<>());

			try{
				builder.save(file);
			}catch(IOException ex){
				getLogger().severe("The config converter failed to convert a structure to the new format!");
				getLogger().severe("The structure that failed is " + key);
				getLogger().severe("Custom Structures will now disable itself.");
				getServer().getPluginManager().disablePlugin(this);
				if(debugMode)
					ex.printStackTrace();
				return;
			}
			structures.add(key);
		}
		getConfig().set("Schematics", null);
		getConfig().set("Structures", structures);
		getConfig().set("configversion", 5);
		try{
			getConfig().save(plugin.getDataFolder() + File.separator + "config.yml");
		}catch(IOException ex){
			getLogger().severe("An error has occurred when trying to save the new config.");
			getLogger().severe("The plugin will now disable itself.");
			getServer().getPluginManager().disablePlugin(this);
			if(debugMode)
				ex.printStackTrace();
			return;
		}
		reloadConfig();
		getLogger().info("Successfully converted " + structures.size() + " structures to the new format!");
	}

	public boolean isDebug(){
		return debugMode;
	}

	/**
	 * Get the custom item manager.
	 * @return The custom item manager.
	 */
	public CustomItemManager getCustomItemManager() {
		return customItemManager;
	}
}
