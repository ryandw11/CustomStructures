package com.ryandw11.structure;

import java.io.File;
import java.io.IOException;

import com.ryandw11.structure.structure.StructureHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

}
