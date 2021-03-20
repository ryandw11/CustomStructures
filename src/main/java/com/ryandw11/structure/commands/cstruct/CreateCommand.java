package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.commands.SubCommand;
import com.ryandw11.structure.structure.StructureBuilder;
import com.ryandw11.structure.structure.properties.BlockLevelLimit;
import com.ryandw11.structure.structure.properties.StructureLimitations;
import com.ryandw11.structure.structure.properties.StructureLocation;
import com.ryandw11.structure.structure.properties.StructureProperties;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Create Structure command for the plugin.
 *
 * <p>Permission: customstructures.create</p>
 *
 * <code>
 * /cstruct create {name} {schem}
 * </code>
 */
public class CreateCommand implements SubCommand {

    private final CustomStructures plugin;

    public CreateCommand(CustomStructures plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length < 2) {
            if (!sender.hasPermission("customstructures.create")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
                return true;
            }
            sender.sendMessage(ChatColor.RED + "You must specify the name and schematic of a structure!");
        } else if (args.length == 2) {
            if (!sender.hasPermission("customstructures.create")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
                return true;
            }
            String name = args[0];
            String schematic = args[1].replace(".schem", "");
            if (schematic.equals("")) {
                sender.sendMessage(ChatColor.RED + "Invalid schematic!");
                return true;
            }
            if (plugin.getStructureHandler().getStructure(name) != null) {
                sender.sendMessage(ChatColor.RED + "A structure with that name already exists!");
                return true;
            }
            File f = new File(plugin.getDataFolder() + File.separator + "structures" + File.separator + name + ".yml");
            try {
                if (!f.exists())
                    f.createNewFile();
            } catch (IOException ex) {
                sender.sendMessage(ChatColor.RED + "An error occurred while creating a structure file!");
                plugin.getLogger().severe("Could not create structure file!");
                if (plugin.isDebug())
                    ex.printStackTrace();
                return true;
            }
            StructureBuilder builder = new StructureBuilder(name, schematic + ".schem");
            builder.setChance(1, 1000);
            if (new File(plugin.getDataFolder() + "/schematics/" + schematic + ".cschem").exists()) {
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
                if (plugin.isDebug())
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
        return false;
    }

}
