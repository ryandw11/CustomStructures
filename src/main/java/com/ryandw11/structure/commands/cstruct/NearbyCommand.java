package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.commands.SubCommand;
import com.ryandw11.structure.exceptions.RateLimitException;
import com.ryandw11.structure.structure.Structure;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * The nearby command for the plugin.
 *
 * <p>This command is expensive on the server and should be used sparingly.</p>
 *
 * <p>Permission: customstructures.findnearby</p>
 *
 * <code>
 * /cstruct nearby
 * </code>
 */
public class NearbyCommand implements SubCommand {

    private final CustomStructures plugin;

    public NearbyCommand(CustomStructures plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!sender.hasPermission("customstructures.findnearby")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "The command is for players only!");
            return true;
        }

        if (!plugin.getStructureHandler().getStructureFileReader().isPresent()) {
            sender.sendMessage(ChatColor.RED + "Structure logging is not enabled! If you are an admin check the" +
                    " console for more information!");
            plugin.getLogger().info("Structure logging is currently disabled! Enable it in the config.yml" +
                    " file in order ot enable features like /cstruct nearby.");
            return true;
        }

        Player p = (Player) sender;
        p.sendMessage(ChatColor.GREEN + "Fetching closest structure. This may take awhile...");
        try {
            plugin.getStructureHandler().getStructureFileReader().get().findNearby(p.getLocation())
                    .thenAccept(struct -> {
                        if (struct == null) {
                            p.sendMessage(ChatColor.RED + "Could not find a nearby structure!");
                            return;
                        }
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                String.format("&aFound structure &6%s &aat &6%s&a, &6%s&a, &6%s&a, in world:&6 %s&a!",
                                        struct.getLeft().getName(),
                                        struct.getRight().getBlockX(),
                                        struct.getRight().getBlockY(),
                                        struct.getRight().getBlockZ(),
                                        Objects.requireNonNull(struct.getRight().getWorld()).getName())
                        ));
                    });
        } catch (RateLimitException ex) {
            p.sendMessage(ChatColor.RED + "Too many requests have been sent at this time. Try again later.");
        }

        return false;
    }

}
