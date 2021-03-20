package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * The CheckKey command for the plugin.
 *
 * <p>Permission: customstructures.checkkey</p>
 *
 * <code>
 * /cstruct checkkey
 * </code>
 */
public class CheckKeyCommand implements SubCommand {

    private final CustomStructures plugin;

    public CheckKeyCommand(CustomStructures plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!sender.hasPermission("customstructures.checkkey")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only!");
            return true;
        }
        Player p = (Player) sender;
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            p.sendMessage(ChatColor.RED + "You must be holding an item to use this command!");
            return true;
        }
        for (String items : Objects.requireNonNull(plugin.getCustomItemManager().getConfig().getConfigurationSection("")).getKeys(false)) {
            if (item.isSimilar(plugin.getCustomItemManager().getItem(items))) {
                p.sendMessage(ChatColor.GREEN + "The item you are holding has a key of: " + ChatColor.GOLD + items);
                return true;
            }
        }
        p.sendMessage(ChatColor.RED + "That item was not found in the item list.");
        return false;
    }

}
