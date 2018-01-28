package com.ryandw11.structure;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;

import com.sk89q.worldedit.data.DataException;

@SuppressWarnings("deprecation")
public class SchematicHandeler {
	
	private CustomStructures plugin;
	public SchematicHandeler(){
		this.plugin = CustomStructures.plugin;
	}
	/**
	 * Handels the schematic.
	 * @param loc - The location
	 * @param filename - The file name. Ex: demo.schematic
	 * @param useAir - if air is to be used in the schematic
	 */
	public void schemHandle(Location loc, String filename, boolean useAir) throws IOException, DataException{
		WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        File schematic = new File(plugin.getDataFolder() + "/schematics/" + filename);
        if(!schematic.exists()){
        	Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&3[&2CustomStructures&3] &cA fatal error has occured! Please check the console for errors."));
        	plugin.getLogger().warning("Error: The schematic " + filename + " does not exist!");
        	plugin.getLogger().warning("If this is your first time using this plugin you need to put a schematic in the schematic folder.");
        	plugin.getLogger().warning("Then add it into the config.");
        	plugin.getLogger().warning("If you need help look at the wiki: https://github.com/ryandw11/CustomStructures/wiki or contact Ryandw11 on spigot!");
        	plugin.getLogger().warning("The plugin will now disable to prevent damage to the server.");
        	Bukkit.getPluginManager().disablePlugin(plugin);
        	return;
        }
        EditSession session = we.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(loc.getWorld()), 1000000);
        try {
            MCEditSchematicFormat.getFormat(schematic).load(schematic).paste(session, new Vector(loc.getX(), loc.getY(), loc.getZ()), !useAir);
        } catch (MaxChangedBlocksException | DataException | IOException e2) {
            
        }
	}
	
	protected Vector getPointMin(CuboidClipboard schem, Location loc){
		Vector vec = new Vector((schem.getOrigin().subtract(schem.getOffset())).getBlockX(),(schem.getOrigin().subtract(schem.getOffset())).getBlockY(), (schem.getOrigin().subtract(schem.getOffset())).getBlockZ());
		Vector vce = vec.subtract(vec);
		return vce.add(new Vector(loc.getX(), loc.getY(), loc.getZ()));
	}
	protected Vector getPointMax(CuboidClipboard schem, Location loc){
		Vector vec = new Vector((schem.getOrigin().subtract(schem.getOffset())).getBlockX(),(schem.getOrigin().subtract(schem.getOffset())).getBlockY(), (schem.getOrigin().subtract(schem.getOffset())).getBlockZ());
		Vector vce = vec.add(vec);
		return vce.add(new Vector(loc.getX(), loc.getY(), loc.getZ()));
	}
	
}
