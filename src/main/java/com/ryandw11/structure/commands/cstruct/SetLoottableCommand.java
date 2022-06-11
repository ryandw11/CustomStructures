package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * The Add Item command for the plugin.
 *
 * <p>Permission: customstructures.setloottable</p>
 *
 * <code>
 * /cstruct setloottable {lootTableName}
 * </code>
 */
public class SetLoottableCommand implements SubCommand {

    private final CustomStructures plugin;

    public SetLoottableCommand(CustomStructures plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length == 0) {
            if (!sender.hasPermission("customstructures.setloottable")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
                return true;
            }
            sender.sendMessage(ChatColor.RED + "You must specify the loot table for the chest to have.");
        } else if (args.length == 1) {
            if (!sender.hasPermission("customstructures.setloottable")) {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
                return true;
            }
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatColor.RED + "This command is for players only!");
                return true;
            }

            if(plugin.getLootTableHandler().getLootTableByName(args[0]) == null) {
                sender.sendMessage(ChatColor.RED + "Cannot find specified loot table. Check to make sure that it exists.");
                return true;
            }

            Block block = p.getTargetBlock(null, 20);
            if(!(block.getState() instanceof Container container)) {
                sender.sendMessage(ChatColor.RED + "You must be looking at a container to set its loot table.");
                return true;
            }
            container.getInventory().clear();
            ItemStack paper = new ItemStack(Material.PAPER, 1);
            ItemMeta itemMeta = paper.getItemMeta();
            itemMeta.setDisplayName("%${" + args[0] + "}$%");
            itemMeta.setLore(List.of("Defines a specific loot table for this container to use.", "This must be in the first item slot to work."));
            paper.setItemMeta(itemMeta);
            container.getInventory().setItem(0, paper);
            sender.sendMessage(ChatColor.GREEN + "The loot table item has been added to the container!");
        }
        return false;
    }

}
