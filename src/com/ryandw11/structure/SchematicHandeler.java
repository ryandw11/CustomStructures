package com.ryandw11.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;


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
	 * @throws WorldEditException 
	 */
	public void schemHandle(Location loc, String filename, boolean useAir) throws IOException, WorldEditException{
        File schematicFile = new File(plugin.getDataFolder() + "/schematics/" + filename);
        // Check to see if the schematic is a thing.
        if(!schematicFile.exists()){
        	Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&3[&2CustomStructures&3] &cA fatal error has occured! Please check the console for errors."));
        	plugin.getLogger().warning("Error: The schematic " + filename + " does not exist!");
        	plugin.getLogger().warning("If this is your first time using this plugin you need to put a schematic in the schematic folder.");
        	plugin.getLogger().warning("Then add it into the config.");
        	plugin.getLogger().warning("If you need help look at the wiki: https://github.com/ryandw11/CustomStructures/wiki or contact Ryandw11 on spigot!");
        	plugin.getLogger().warning("The plugin will now disable to prevent damage to the server.");
        	Bukkit.getPluginManager().disablePlugin(plugin);
        	return;
        }
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        Clipboard clipboard;
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
        	   clipboard = reader.read();
        }
        //Paste the schematic
        try(EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(loc.getWorld()), -1)){
        	Operation operation = new ClipboardHolder(clipboard)
        			.createPaste(editSession)
        			.to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ()))
        			.ignoreAirBlocks(!useAir)
        			.build();
        	Operations.complete(operation);
        }
	}
	
}
