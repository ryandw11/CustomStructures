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

import com.ryandw11.structure.commands.SCommand;
import com.ryandw11.structure.listener.ChunkLoad;
import com.ryandw11.structure.utils.CheckSchematics;

/**
 * 
 * @author Ryandw11
 * @version 1.3
 *
 */

public class CustomStructures extends JavaPlugin{
	
	public static CustomStructures plugin;
	
	public File exfile = new File(getDataFolder() + "/schematics/Put_Schematics_In_Here.yml");
	public FileConfiguration ex = YamlConfiguration.loadConfiguration(exfile);
	
	@Override
	public void onEnable(){
		
		
		plugin = this;
		loadManager();
		registerConfig();
		
		@SuppressWarnings("unchecked")
		ArrayList<String> stuff = (ArrayList<String>) plugin.getConfig().get("Schematics.List");
		
		getLogger().info("The plugin has been enabled with " + stuff.size() + " schematics.");
		loadFile();
		CheckSchematics cs = new CheckSchematics(stuff);
		cs.runTaskTimer(plugin, 5L, 1L);
	}
	
	@Override
	public void onDisable(){
		saveFile();
	}
	
	public void loadManager(){
		Bukkit.getServer().getPluginManager().registerEvents(new ChunkLoad(), this);
		getCommand("customstructure").setExecutor(new SCommand());
	}
	
	public void registerConfig(){
		saveDefaultConfig();
	}
	
	
	public void saveFile(){
		try{
			ex.save(exfile);
		}catch(IOException e){
			e.printStackTrace();
			
		}	
	}
	
	public void loadFile(){
		if(exfile.exists()){
			try {
				ex.load(exfile);
				
			} catch (IOException | InvalidConfigurationException e) {

				e.printStackTrace();
			}
		}
		else{
			try {
				ex.save(exfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setupSchem(){
		File fil = new File(plugin.getDataFolder() + "/schematics/Demo.txt");
		if(!fil.exists()){
			try (BufferedWriter bw = new BufferedWriter(new FileWriter("Demo.txt"))) {

				String content = "In this folder is where you put schematics. For help go here: {Insert Github Link}";

				bw.write(content);

			} catch (IOException e1) {

				e1.printStackTrace();

			}
		}
	}
	

}
