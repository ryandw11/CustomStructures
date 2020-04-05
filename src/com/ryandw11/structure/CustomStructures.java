package com.ryandw11.structure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.ryandw11.structure.api.CustomStructuresAPI;
import com.ryandw11.structure.commands.SCommand;
import com.ryandw11.structure.listener.ChunkLoad;
import com.ryandw11.structure.loottables.LootTablesHandler;
import com.ryandw11.structure.mythicalmobs.MMDisabled;
import com.ryandw11.structure.mythicalmobs.MMEnabled;
import com.ryandw11.structure.mythicalmobs.MythicalMobHook;
import com.ryandw11.structure.utils.CheckLootTables;
import com.ryandw11.structure.utils.CheckSchematics;

/**
 * 
 * @author Ryandw11
 * @version 1.4.2
 *
 */

public class CustomStructures extends JavaPlugin {

	public static CustomStructures plugin;

	public static LootTablesHandler lootTablesHandler;

	public File exfile = new File(getDataFolder() + "/schematics/Put_Schematics_In_Here.yml");
	public File lootTablesfile = new File(getDataFolder() + "/lootTables/lootTable.yml");
	public File demoSchematicFile = new File(getDataFolder() + "/schematics/demo.schem");
	public FileConfiguration ex = YamlConfiguration.loadConfiguration(exfile);
	public FileConfiguration lootTablesFC = YamlConfiguration.loadConfiguration(lootTablesfile);

	public ArrayList<String> structures;
	public MythicalMobHook mmh;

	@Override
	public void onEnable() {

		plugin = this;
		loadManager();
		registerConfig();
		
		if(getServer().getPluginManager().getPlugin("MythicalMobs") != null) {
			mmh = new MMEnabled();
			getLogger().info("MythicalMobs detected! Activating plugin hook!");
		}else {
			mmh = new MMDisabled();
		}

		CustomStructuresAPI capi = new CustomStructuresAPI();

		getLogger().info("The plugin has been enabled with " + capi.getNumberOfStructures() + " structures.");
		loadFile();
		CheckSchematics cs = new CheckSchematics(this.getConfig().getConfigurationSection("Schematics").getKeys(false));
		cs.runTaskTimer(plugin, 5L, 1L);
		setStructures();

		CheckLootTables cl = new CheckLootTables(this.getConfig().getConfigurationSection("Schematics").getKeys(false));
		cl.runTaskTimer(plugin, 5L, 1L);

		lootTablesHandler = new LootTablesHandler();
	}

	@Override
	public void onDisable() {
		saveFile();
	}

	public void loadManager() {
		Bukkit.getServer().getPluginManager().registerEvents(new ChunkLoad(), this);
		getCommand("customstructure").setExecutor(new SCommand());
	}

	public void setStructures() {
		structures = new ArrayList<String>();
		for (String s : this.getConfig().getConfigurationSection("Schematics").getKeys(false)) {
			structures.add(s);
		}
	}

	public void registerConfig() {
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

		if (demoSchematicFile.exists()) {
			try {
				lootTablesFC.load(demoSchematicFile);

			} catch (IOException | InvalidConfigurationException e) {

				e.printStackTrace();
			}
		} else {
			saveResource("schematics/demo.schem", false);
		}
	}

	public void setupSchem() {
		File fil = new File(plugin.getDataFolder() + "/schematics/Demo.txt");
		if (!fil.exists()) {
			try (BufferedWriter bw = new BufferedWriter(new FileWriter("Demo.txt"))) {

				String content = "In this folder is where you put schematics. For help go here: {Insert Github Link}";

				bw.write(content);

			} catch (IOException e1) {

				e1.printStackTrace();

			}
		}
	}

}
