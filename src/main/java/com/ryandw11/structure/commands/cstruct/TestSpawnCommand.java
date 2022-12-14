package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.commands.SubCommand;
import com.ryandw11.structure.ignoreblocks.IgnoreBlocks;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.properties.BlockLevelLimit;
import com.ryandw11.structure.structure.properties.StructureYSpawning;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The test spawn command for the plugin. The output of this command may
 * be large.
 *
 * <p>Permission: customstructures.test.spawn</p>
 *
 * <code>
 * /cstruct testspawn {struct_name}
 * </code>
 */
public class TestSpawnCommand implements SubCommand {

    private final CustomStructures plugin;

    public TestSpawnCommand(CustomStructures plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean subCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Invalid arguments. /cstruct testspawn {name}");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is for players only!");
            return true;
        }
        Player p = (Player) sender;
        if (!p.hasPermission("customstructures.test.spawn")) {
            p.sendMessage(ChatColor.RED + "You do not have permission for this command.");
            return true;
        }
        Structure structure = plugin.getStructureHandler().getStructure(args[0]);
        if (structure == null) {
            p.sendMessage(ChatColor.RED + "That structure does not exist!");
            return true;
        }
        p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&b=================[&6" + structure.getName() + "&b]================="));
        psuedoCalculate(p, structure, p.getLocation().getBlock(), p.getLocation().getChunk());
        p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&b=================[&6" + structure.getName() + "&b]================="));
        return true;
    }

    void psuedoCalculate(Player p, Structure structure, Block bl, Chunk ch) {

        IgnoreBlocks ignoreBlocks = plugin.getBlockIgnoreManager();

        StructureYSpawning structureSpawnSettings = structure.getStructureLocation().getSpawnSettings();

        bl = structureSpawnSettings.getHighestBlock(bl.getLocation());
        ;

        // Calculate the chance.
        canSpawn(p, structure, bl, ch);

        // Allows the structures to no longer spawn on plant life.
        if (structure.getStructureProperties().isIgnoringPlants() && ignoreBlocks.getBlocks().contains(bl.getType())) {
            for (int i = bl.getY(); i >= 4; i--) {
                if (!ignoreBlocks.getBlocks().contains(ch.getBlock(8, i, 8).getType()) && !ch.getBlock(8, i, 8).getType().isAir()) {
                    bl = ch.getBlock(8, i, 8);
                    break;
                }
            }
        }

        // calculate SpawnY if first is true
        if (structureSpawnSettings.isCalculateSpawnYFirst()) {
            bl = ch.getBlock(8, structureSpawnSettings.getHeight(bl.getLocation()), 8);
            quickSendMessage(p, "&aSpawn Y Value: " + bl.getY());
        }

        if (!structure.getStructureLimitations().hasWhitelistBlock(bl)) {
            quickSendMessage(p, String.format("&cFailed Block Limitation! Cannot spawn on %s! (Whitelist Defined)", bl.getType()));
            return;
        }

        if (structure.getStructureLimitations().hasBlacklistBlock(bl)) {
            quickSendMessage(p, String.format("&cFailed Block Limitation! Cannot spawn on %s! (Blacklist Defined)", bl.getType()));
            return;
        }

        // If it can spawn in water
        if (!structure.getStructureProperties().canSpawnInWater()) {
            if (bl.getType() == Material.WATER) {
                quickSendMessage(p, "&cFailed Water test! Cannot spawn in the water!");
                return;
            }
        }

        // If the structure can spawn in lava
        if (!structure.getStructureProperties().canSpawnInLavaLakes()) {
            if (bl.getType() == Material.LAVA) {
                quickSendMessage(p, "&cFailed Lava test! Cannot spawn in the lava!");
                return;
            }
        }

        // calculate SpawnY if first is false
        if (!structureSpawnSettings.isCalculateSpawnYFirst()) {
            bl = ch.getBlock(8, structureSpawnSettings.getHeight(bl.getLocation()), 8);
            quickSendMessage(p, "&aSpawn Y Value: " + bl.getY());
        }

        // If the structure is going to be cut off by the world height limit, pick a new structure.
        if (structure.getStructureLimitations().getWorldHeightRestriction() != -1 &&
                bl.getLocation().getY() > ch.getWorld().getMaxHeight() - structure.getStructureLimitations().getWorldHeightRestriction()) {
            quickSendMessage(p, "&cFailed World Height Restriction!");
            return;
        }

        // If the structure can follows block level limit.
        // This only triggers if it spawns on the top.
        if (structure.getStructureLimitations().getBlockLevelLimit().isEnabled()) {
            BlockLevelLimit limit = structure.getStructureLimitations().getBlockLevelLimit();
            if (limit.getMode().equalsIgnoreCase("flat")) {
                for (int x = limit.getX1() + bl.getX(); x <= limit.getX2() + bl.getX(); x++) {
                    for (int z = limit.getZ1() + bl.getZ(); z <= limit.getZ2() + bl.getZ(); z++) {
                        Block top = ch.getWorld().getBlockAt(x, bl.getY() + 1, z);
                        Block bottom = ch.getWorld().getBlockAt(x, bl.getY() - 1, z);
                        if (!(top.getType().isAir() || ignoreBlocks.getBlocks().contains(top.getType()))) {
                            // Output debug info if in debug mode.
                            if (plugin.isDebug()) {
                                p.sendMessage(top.getLocation() + " || TOP FAIL");
                                p.sendMessage(top.getType() + " || TOP FAIL");
                            }
                            quickSendMessage(p, "&cFailed Flat Block Level Limit test! The ground is not flat!");
                            return;
                        }
                        if (bottom.getType().isAir()) {
                            if (plugin.isDebug()) {
                                p.sendMessage(bottom.getLocation() + " || BOTTOM FAIL");
                            }
                            quickSendMessage(p, "&cFailed Flat Block Level Limit test! The ground is not flat!");
                            return;
                        }
                    }
                }
            } else if (limit.getMode().equalsIgnoreCase("flat_error")) {
                int total = 0;
                int error = 0;
                for (int x = limit.getX1() + bl.getX(); x <= limit.getX2() + bl.getX(); x++) {
                    for (int z = limit.getZ1() + bl.getZ(); z <= limit.getZ2() + bl.getZ(); z++) {
                        Block top = ch.getWorld().getBlockAt(x, bl.getY() + 1, z);
                        Block bottom = ch.getWorld().getBlockAt(x, bl.getY() - 1, z);
                        if (!(top.getType().isAir() || ignoreBlocks.getBlocks().contains(top.getType())))
                            error++;
                        if (bottom.getType().isAir())
                            error++;

                        total += 2;
                    }
                }
                // Debug the percent failure.
                if (plugin.isDebug()) {
                    p.sendMessage("Percent Failure: " + ((double) error / total) + " / " + limit.getError());
                }
                if (((double) error / total) > limit.getError()) {

                    quickSendMessage(p, "&cFailed Flat Error Block Level Limit test! The ground is not flat enough!");
                }
            }
        }
    }

    void canSpawn(Player p, Structure structure, Block block, Chunk chunk) {
        if (!structure.getStructureLocation().getWorlds().isEmpty()) {
            if (!structure.getStructureLocation().getWorlds().contains(chunk.getWorld().getName()))
                quickSendMessage(p, "&cFailed world test! Cannot spawn in current world!");
        }

        // Check to see if the structure is far enough away from spawn.
        if (Math.abs(block.getX()) < structure.getStructureLocation().getXLimitation())
            quickSendMessage(p, "&cFailed X Limitation test! Cannot spawn this close to (0, 0)!");
        if (Math.abs(block.getZ()) < structure.getStructureLocation().getZLimitation())
            quickSendMessage(p, "&cFailed Z Limitation test! Cannot spawn this close to (0, 0)!");

        if (!CustomStructures.getInstance().getStructureHandler().validDistance(structure, block.getLocation()))
            quickSendMessage(p, "&cFailed Distance Limitation test! Cannot spawn this close to another structure!");

        if (!CustomStructures.getInstance().getStructureHandler().validSameDistance(structure, block.getLocation()))
            quickSendMessage(p, "&cFailed Distance Limitation test! Cannot spawn this close to the same structure!");

        // Check to see if the structure has the chance to spawn
        if (ThreadLocalRandom.current().nextInt(0, structure.getProbabilityDenominator() + 1) > structure.getProbabilityNumerator())
            quickSendMessage(p, String.format("&eDid not spawn by probability! (%d/%d chance)", structure.getProbabilityNumerator(), structure.getProbabilityDenominator()));

        // Check to see if the structure can spawn in the current biome.
        if (!structure.getStructureLocation().hasBiome(block.getBiome()))
            quickSendMessage(p, "&cFailed Biome test! Cannot spawn in this biome!");
    }

    void quickSendMessage(Player p, String msg) {
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

}
