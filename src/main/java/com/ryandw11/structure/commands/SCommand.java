package com.ryandw11.structure.commands;;

import com.ryandw11.structure.SchematicHandler;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.StructureBuilder;
import com.ryandw11.structure.structure.properties.BlockLevelLimit;
import com.ryandw11.structure.structure.properties.StructureLimitations;
import com.ryandw11.structure.structure.properties.StructureLocation;
import com.ryandw11.structure.structure.properties.StructureProperties;
import com.ryandw11.structure.utils.RandomCollection;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.ryandw11.structure.CustomStructures;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SCommand implements CommandExecutor {
	private CustomStructures plugin;

	public SCommand(CustomStructures plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
		if(!CustomStructures.enabled) {
			sender.sendMessage(ChatColor.RED + "One of your schematic or lootable files could not be found!");
			sender.sendMessage(ChatColor.RED + "Please check to see if all of your files are in the proper folders!");
			sender.sendMessage(ChatColor.RED + "To find out more, see the error in the console.");
			return true;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("customstructures.reload")) {
				plugin.reloadConfig();
				sender.sendMessage("The plugin has been reloaded!");
				plugin.getLogger().info("Plugin reloaded!");

				plugin.reloadHandlers();
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
			}
		} else if (args.length == 2 && args[0].equalsIgnoreCase("test")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command is for players only!");
				return true;
			}
			Player p = (Player) sender;
			if (!p.hasPermission("customstructures.test")) {
				p.sendMessage(ChatColor.RED + "You do not have permission for this command.");
				return true;
			}
			Structure structure = plugin.getStructureHandler().getStructure(args[1]);
			if(structure == null){
				p.sendMessage(ChatColor.RED + "That schematic does not exist!");
				return true;
			}

			structure.spawn(p.getLocation());

		} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			sender.sendMessage(ChatColor.GREEN + "Currently Active Schematics:");
			for (Structure st : plugin.getStructureHandler().getStructures()) {
				sender.sendMessage(ChatColor.GREEN + " - " + ChatColor.BLUE + st.getName());
			}
		} else if(args.length == 1 && args[0].equalsIgnoreCase("additem")){
			if(!sender.hasPermission("customstructures.additem")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			sender.sendMessage(ChatColor.RED + "You must specify a unique key to call the item by.");
		}else if(args.length == 2 && args[0].equalsIgnoreCase("additem")){
			if(!sender.hasPermission("customstructures.additem")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "This command is for players only!");
				return true;
			}
			Player p = (Player) sender;
			String key = args[1];
			ItemStack item = p.getInventory().getItemInMainHand();
			item.setAmount(1);
			if(item.getType() == Material.AIR){
				p.sendMessage(ChatColor.RED + "You must be holding an item to use this command!");
				return true;
			}
			if(!plugin.getCustomItemManager().addItem(key, item)){
				p.sendMessage(ChatColor.RED + "That key already exists!");
			}else{
				p.sendMessage(ChatColor.GREEN + "Successfully added the custom item to the list.");
			}
		}else if(args.length == 1 && args[0].equalsIgnoreCase("checkkey")){
			if(!sender.hasPermission("customstructures.checkkey")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "This command is for players only!");
				return true;
			}
			Player p = (Player) sender;
			ItemStack item = p.getInventory().getItemInMainHand();
			if(item.getType() == Material.AIR){
				p.sendMessage(ChatColor.RED + "You must be holding an item to use this command!");
				return true;
			}
			for(String items : Objects.requireNonNull(plugin.getCustomItemManager().getConfig().getConfigurationSection("")).getKeys(false)){
				if(item.isSimilar(plugin.getCustomItemManager().getItem(items))){
					p.sendMessage(ChatColor.GREEN + "The item you are holding has a key of: " + ChatColor.GOLD + items);
					return true;
				}
			}
			p.sendMessage(ChatColor.RED + "That item was not found in the item list.");
		}else if(args.length == 1 && args[0].equalsIgnoreCase("getitem")){
			if(!sender.hasPermission("customstructures.getitem")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			sender.sendMessage(ChatColor.RED + "You must specify the key in order to get an item from the list.");
		}else if(args.length == 2 && args[0].equalsIgnoreCase("getitem")){
			if(!sender.hasPermission("customstructures.getitem")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "This command is for players only!");
				return true;
			}
			Player p = (Player) sender;
			String key = args[1];
			ItemStack item = plugin.getCustomItemManager().getItem(key);
			if(item == null){
				p.sendMessage(ChatColor.RED + "An item with that key was not found!");
				return true;
			}
			p.getInventory().addItem(item);
			p.sendMessage(ChatColor.GREEN + "Successfully retrieved that item!");
		}else if(args.length == 1 && args[0].equalsIgnoreCase("createschem")){
			if(!sender.hasPermission("customstructures.createschematic")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			sender.sendMessage(ChatColor.RED + "You must specify the name in order to create a schematic.");
		}
		else if(args.length == 2 && args[0].equalsIgnoreCase("createschem")){
			if(!sender.hasPermission("customstructures.createschematic")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "This command is for players only!");
				return true;
			}
			Player p = (Player) sender;
			String name = args[1].replace(".schem", "");
			SchematicHandler handeler = new SchematicHandler();
			if(handeler.createSchematic(name, p, p.getWorld(), false)){
				p.sendMessage(ChatColor.GREEN + "Successfully created a schematic with the name of " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
				p.sendMessage(ChatColor.GREEN + "You can now use " + ChatColor.GOLD + name + ".schem" + ChatColor.GREEN + " in a structure.");
			}else{
				p.sendMessage(ChatColor.RED + "The world edit region seems to be incomplete! Try making a selection first!");
			}
		}
		else if(args.length == 3 && args[0].equalsIgnoreCase("createschem")){
			if(!sender.hasPermission("customstructures.createschematic.options")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.RED + "This command is for players only!");
				return true;
			}

			if(args[2].equalsIgnoreCase("-c") || args[2].equalsIgnoreCase("-compile")){
				Player p = (Player) sender;
				String name = args[1].replace(".schem", "");
				SchematicHandler handeler = new SchematicHandler();
				if(handeler.createSchematic(name, p, p.getWorld(), true)){
					p.sendMessage(ChatColor.GREEN + "Successfully created a schematic with the name of " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
					p.sendMessage(ChatColor.GREEN + "Successfully compiled the schematic!");
					p.sendMessage(ChatColor.GREEN + "You can now use " + ChatColor.GOLD + name + ".schem" +
							ChatColor.GREEN + " and " + ChatColor.GOLD + name + ".cschem" + ChatColor.GREEN + " in a structure.");
				}else{
					p.sendMessage(ChatColor.RED + "The world edit region seems to be incomplete! Try making a selection first!");
				}
			}
			if(args[2].equalsIgnoreCase("-cOnly") || args[2].equalsIgnoreCase("-compileOnly")){
				Player p = (Player) sender;
				String name = args[1].replace(".schem", "").replace(".cschem", "");
				SchematicHandler handeler = new SchematicHandler();
				if(handeler.compileOnly(name, p, p.getWorld())){
					p.sendMessage(ChatColor.GREEN + "Successfully compiled the schematic with the name of " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
					p.sendMessage(ChatColor.RED + "The option is for advanced users only. Please be sure the selection is valid.");
				}else{
					p.sendMessage(ChatColor.RED + "The world edit region seems to be incomplete! Try making a selection first!");
				}
			}
		}
		else if( args.length != 0 && args.length < 3 && args[0].equalsIgnoreCase("create")){
			if(!sender.hasPermission("customstructures.create")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			sender.sendMessage(ChatColor.RED + "You must specify the name and schematic of a structure!");
		}
		else if(args.length == 3 && args[0].equalsIgnoreCase("create")){
			if(!sender.hasPermission("customstructures.create")){
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
				return true;
			}
			String name = args[1];
			String schematic = args[2].replace(".schem", "");
			if(schematic.equals("")){
				sender.sendMessage(ChatColor.RED + "Invalid schematic!");
				return true;
			}
			if(plugin.getStructureHandler().getStructure(name) != null){
				sender.sendMessage(ChatColor.RED + "A structure with that name already exists!");
				return true;
			}
			File f = new File(plugin.getDataFolder() + File.separator + "structures" + File.separator + name + ".yml");
			try{
				if(!f.exists())
					f.createNewFile();
			}catch(IOException ex){
				sender.sendMessage(ChatColor.RED + "An error occurred while creating a structure file!");
				plugin.getLogger().severe("Could not create structure file!");
				if(plugin.isDebug())
					ex.printStackTrace();
				return true;
			}
			StructureBuilder builder = new StructureBuilder(name, schematic + ".schem");
			builder.setChance(1, 1000);
			if(new File(plugin.getDataFolder() + "/schematics/" + schematic + ".cschem").exists()){
				builder.setCompiledSchematic(schematic + ".cschem");
			}
			builder.setStructureProperties(new StructureProperties());
			builder.setStructureLocation(new StructureLocation());
			builder.setStructureLimitations(new StructureLimitations(new ArrayList<>(), new BlockLevelLimit(), new HashMap<>()));
			try {
				builder.save(f);
			} catch (IOException e) {
				sender.sendMessage(ChatColor.RED + "An error occurred while saving the structure file!");
				plugin.getLogger().severe("Could not save structure file!");
				if(plugin.isDebug())
					e.printStackTrace();
				return true;
			}
			List<String> structs = plugin.getConfig().getStringList("Structures");
			structs.add(name);
			plugin.getConfig().set("Structures", structs);
			plugin.saveConfig();
			sender.sendMessage(ChatColor.GREEN + "Successfully created the structure " + ChatColor.GOLD + name + ChatColor.GREEN + "!");
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aRun the &6/cstructure reload &ato load in the new structure and changes."));
		}
		else {
			if (sender.hasPermission("customstructures.info")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3=============[&2CustomStructures&3]============="));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3Created by: &2Ryandw11"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3Version: &2" + plugin.getDescription().getVersion()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3Github wiki:&2 https://github.com/ryandw11/CustomStructures/wiki"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3Commands:"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3/cstructure reload - &2Reload the plugin."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure test (name) - &2Paste the defined structure."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure list - &2List the currently active structures."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure addItem {key} - &2Add an item to the custom items list."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure checkKey - &2Get the key of an item you are holding in your hand."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure getItem {key} - &2Get the item of the key specified."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure createschem {name} [-options] - &2Create a schematic from the current worldedit selection (This is automatically save to the CustomStructures schematic folder)."));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&3/cstructure create {name} {schematic} - &2Create a structure using the default settings."));
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
			}
		}

		return false;
	}

}
