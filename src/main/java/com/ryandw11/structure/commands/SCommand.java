package com.ryandw11.structure.commands;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.SchematicHandler;
import com.ryandw11.structure.commands.cstruct.*;
import com.ryandw11.structure.exceptions.RateLimitException;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.StructureBuilder;
import com.ryandw11.structure.structure.properties.BlockLevelLimit;
import com.ryandw11.structure.structure.properties.StructureLimitations;
import com.ryandw11.structure.structure.properties.StructureLocation;
import com.ryandw11.structure.structure.properties.StructureProperties;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Handles the commands for the plugin.
 */
public class SCommand implements CommandExecutor {
    private final CustomStructures plugin;
    private final CommandHandler commandHandler;

    public SCommand(CustomStructures plugin) {
        this.plugin = plugin;
        this.commandHandler = new CommandHandler();
        this.commandHandler.registerCommand("test", new TestCommand(plugin));
        this.commandHandler.registerCommand("reload", new ReloadCommand(plugin));
        this.commandHandler.registerCommand("list", new ListCommand(plugin));
        this.commandHandler.registerCommand("nearby", new NearbyCommand(plugin));
        this.commandHandler.registerCommand("additem", new AddItemCommand(plugin));
        this.commandHandler.registerCommand("checkkey", new CheckKeyCommand(plugin));
        this.commandHandler.registerCommand("getitem", new GetItemCommand(plugin));
        this.commandHandler.registerCommand("createschem", new CreateSchematicCommand(plugin));
        this.commandHandler.registerCommand("create", new CreateCommand(plugin));
        this.commandHandler.registerCommand("testspawn", new TestSpawnCommand(plugin));
        this.commandHandler.registerCommand(new AddonsCommand(plugin), "addon", "addons");
        this.commandHandler.registerCommand(new SetLoottableCommand(plugin), "setloottable", "setloot", "setlt");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!CustomStructures.enabled) {
            sender.sendMessage(ChatColor.RED + "One of your schematic or loot table files could not be found!");
            sender.sendMessage(ChatColor.RED + "Please check to see if all of your files are in the proper folders!");
            sender.sendMessage(ChatColor.RED + "To find out more, see the error in the console.");
            return true;
        }

        try{
            if(args.length != 0 && !(args[0].equals("1") || args[0].equals("2")))
                return commandHandler.handleCommand(sender, cmd, s, args);
        }catch (IllegalArgumentException ex){
            // Do nothing as this exception was triggered purposefully.
        }

        String currentPage = args.length > 0 ? args[0] : "1";

        if (sender.hasPermission("customstructures.info")) {
            if(currentPage.equals("2")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&3=============[&2CustomStructures Page 2&3]============="));
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
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&3/cstructure addon - &2The list of addons."));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&3/cstructure setLootTable - &2Easily specify a loot table for a container."));
            } else {
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
                        "&3/cstructure testspawn (name) - &2Test the spawn conditions of a structure."));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&3/cstructure nearby - &2Find nearby structures."));
                sender.sendMessage("");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&2Use &3/cstructure 2 &2to view the second page of commands!"));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
        }

        return false;
    }

}
