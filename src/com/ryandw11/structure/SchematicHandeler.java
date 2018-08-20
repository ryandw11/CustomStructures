package com.ryandw11.structure;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import com.google.common.io.Closer;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

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
	public void schemHandle(Location loc, String filename, boolean useAir) throws Exception{
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
        EditSession session=we.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(loc.getWorld()),1000000);
        ClipboardFormat format=ClipboardFormats.findByFile(schematic);
        try(Closer closer=Closer.create()){
            FileInputStream fis=closer.register(new FileInputStream(schematic));
            BufferedInputStream bis=closer.register(new BufferedInputStream(fis));
            ClipboardReader reader=closer.register(format.getReader(bis));
            Clipboard clipboard=reader.read();
            ClipboardHolder holder=new ClipboardHolder(clipboard);
            Region region=clipboard.getRegion();
            Vector to=new Vector(loc.getX(),loc.getY(),loc.getZ());
            Operation operation=holder.createPaste(session).to(to).ignoreAirBlocks(!useAir).build();
            Operations.completeLegacy(operation);
        }catch(Exception Ex){
            Ex.printStackTrace();
        }
	}
	
}
