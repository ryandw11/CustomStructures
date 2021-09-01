package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.CustomStructureAddon;
import com.ryandw11.structure.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The addons command for the plugin.
 *
 * <p>Permission: customstructures.addon</p>
 *
 * <code>
 * /cstruct addons
 * </code>
 */
public class AddonsCommand implements SubCommand {

    private final CustomStructures plugin;

    public AddonsCommand(CustomStructures plugin) {
        this.plugin = plugin;
    }

    private static final List<String> officialAddons = new ArrayList<>(Arrays.asList(
            "CSCustomBiomes"
    ));

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!sender.hasPermission("customstructures.addon")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            return true;
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3============[&2Enabled Addons&3]============"));
        for (CustomStructureAddon addon : plugin.getAddonHandler().getCustomStructureAddons()) {
            String official = "";
            // A simple hard-coded way to determine if an addon is official. This only serves as confirmation to the user that the
            // addon they are using is developed and supported officially.
            if (addon.getAuthors().size() == 1
                    && addon.getAuthors().get(0).equalsIgnoreCase("Ryandw11")
                    && officialAddons.contains(addon.getName()))
                official = "(Official)";
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    String.format("&a- &6%s &aby: &6%s   &9%s", addon.getName(), String.join("&a,&6 ", addon.getAuthors()), official)));
        }
        return false;
    }

}
