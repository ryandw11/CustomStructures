package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.commands.SubCommand;
import com.ryandw11.structure.io.NearbyStructuresRequest;
import com.ryandw11.structure.io.NearbyStructuresResponse;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * The nearby command for the plugin.
 *
 * <p>This command is expensive on the server and should be used sparingly.</p>
 *
 * <p>Permission: customstructures.findnearby</p>
 *
 * <code>
 * /cstruct nearby [structName] [limit]
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

        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "The command is for players only!");
            return true;
        }

        if (plugin.getStructureHandler().getStructureDatabaseHandler().isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Structure logging is not enabled! If you are an admin check the" +
                    " console for more information!");
            plugin.getLogger().info("Structure logging is currently disabled! Enable it in the config.yml" +
                    " file in order ot enable features like /cstruct nearby.");
            return true;
        }

        p.sendMessage(ChatColor.GREEN + "Fetching closest structures. This may take awhile...");

        NearbyStructuresRequest nearbyStructuresRequest;

        if (args.length == 0) {
            nearbyStructuresRequest = new NearbyStructuresRequest(p.getLocation());
        } else if (args.length == 1) {
            try {
                int limit = Integer.parseInt(args[0]);
                // Impose a minimum value of 1, and a maximum value of 20 for the limit.
                limit = Math.max(1, Math.min(limit, 20));
                nearbyStructuresRequest = new NearbyStructuresRequest(p.getLocation(), limit);
            } catch (NumberFormatException exception) {
                nearbyStructuresRequest = new NearbyStructuresRequest(p.getLocation(), args[0]);
            }
        } else if (args.length == 2) {
            try {
                int limit = Integer.parseInt(args[1]);
                // Impose a minimum value of 1, and a maximum value of 20 for the limit.
                limit = Math.max(1, Math.min(limit, 20));
                nearbyStructuresRequest = new NearbyStructuresRequest(p.getLocation(), args[0], limit);
            } catch (NumberFormatException exception) {
                p.sendMessage(ChatColor.RED + "Error: Invalid limit specified.");
                return true;
            }
        } else {
            p.sendMessage(ChatColor.RED + "Error: Invalid number of arguments. (/cstruct nearby [structName] [limit])");
            return true;
        }

        NearbyStructuresRequest finalNearbyStructuresRequest = nearbyStructuresRequest;
        plugin.getStructureHandler().getStructureDatabaseHandler().get().findNearby(nearbyStructuresRequest)
                .thenAccept(response -> {
                    if (!response.hasEntries()) {
                        p.sendMessage(ChatColor.RED + "Could not find any nearby structures!");
                        return;
                    }
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                    String.format("&aNearby Structures (&c%s&a, Limit &c%d&a):",
                                            finalNearbyStructuresRequest.hasName() ?
                                                    finalNearbyStructuresRequest.getName() : "All Structures",
                                            finalNearbyStructuresRequest.getLimit())
                            )
                    );
                    for (NearbyStructuresResponse.NearbyStructureContainer nearbyStructure : response.getResponse()) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                String.format("&aFound structure &6%s &aat &6%s&a, &6%s&a, &6%s&a (&6%.2f &ablocks away)!",
                                        nearbyStructure.getStructure().getName(),
                                        nearbyStructure.getLocation().getBlockX(),
                                        nearbyStructure.getLocation().getBlockY(),
                                        nearbyStructure.getLocation().getBlockZ(),
                                        nearbyStructure.getDistance()
                                )
                        ));
                    }
                }).exceptionally((ex) -> {
                    p.sendMessage(ChatColor.RED + "Too many requests have been sent at this time. Try again later.");
                    return null;
                });

        return false;
    }

}
