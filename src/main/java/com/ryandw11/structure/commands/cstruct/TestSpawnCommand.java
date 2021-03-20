package com.ryandw11.structure.commands.cstruct;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.commands.SubCommand;
import com.ryandw11.structure.ignoreblocks.IgnoreBlocks;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.StructureHandler;
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

        bl = ch.getBlock(0, ch.getWorld().getHighestBlockYAt(bl.getX(), bl.getZ()), 0);

        // Calculate the chance.
        canSpawn(p, structure, bl, ch);

        // Allows the structure to spawn based on the ocean floor. (If the floor is not found than it just returns with the top of the water).
        if (structureSpawnSettings.isOceanFloor()) {
            if (bl.getType() == Material.WATER) {
                for (int i = bl.getY(); i >= 4; i--) {
                    if (ch.getBlock(0, i, 0).getType() != Material.WATER) {
                        bl = ch.getBlock(0, i, 0);
                        break;
                    }
                }
            }
        }

        // Allows the structures to no longer spawn on plant life.
        if (structure.getStructureProperties().isIgnoringPlants() && ignoreBlocks.getBlocks().contains(bl.getType())) {
            for (int i = bl.getY(); i >= 4; i--) {
                if (!ignoreBlocks.getBlocks().contains(ch.getBlock(0, i, 0).getType()) && ch.getBlock(0, i, 0).getType() != Material.AIR) {
                    bl = ch.getBlock(0, i, 0);
                    break;
                }
            }
        }

        // calculate SpawnY if first is true
        if (!structureSpawnSettings.isOceanFloor() && structureSpawnSettings.isCalculateSpawnYFirst()) {
            bl = ch.getBlock(0, structureSpawnSettings.getHeight(bl.getY()), 0);
            quickSendMessage(p, "&aSpawn Y Value: " + bl.getY());
        }

        if (!structure.getStructureLimitations().hasBlock(bl)) {
            quickSendMessage(p, "&cFailed Block Limitation! Cannot spawn on this block!");
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
        if (!structureSpawnSettings.isOceanFloor() && !structureSpawnSettings.isCalculateSpawnYFirst()) {
            bl = ch.getBlock(0, structureSpawnSettings.getHeight(bl.getY()), 0);
            quickSendMessage(p, "&aSpawn Y Value: " + bl.getY());
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
                        if (!(top.getType() == Material.AIR || ignoreBlocks.getBlocks().contains(top.getType()))){
                            quickSendMessage(p, "&cFailed Flat Block Level Limit test! The ground is not flat!");
                            return;
                        }
                        if (bottom.getType() == Material.AIR) {
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
                        if (!(top.getType() == Material.AIR || ignoreBlocks.getBlocks().contains(top.getType())))
                            error++;
                        if (bottom.getType() == Material.AIR)
                            error++;

                        total += 2;
                    }
                }

                if (((double) error / total) > limit.getError()){
                    quickSendMessage(p, "&cFailed Flat Error Block Level Limit test! The ground is not flat enough!");
                }
            }
        }
    }

    void canSpawn(Player p, Structure structure, Block block, Chunk chunk){
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

        // Check to see if the structure has the chance to spawn
        if (ThreadLocalRandom.current().nextInt(0, structure.getChanceOutOf() + 1) > structure.getChanceNumber())
            quickSendMessage(p, "&eDid not spawn by probability!");

        // Check to see if the structure can spawn in the current biome.
        if(!structure.getStructureLocation().hasBiome(block.getBiome()))
            quickSendMessage(p, "&cFailed Biome test! Cannot spawn in this biome!");
    }

    void quickSendMessage(Player p, String msg){
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

}
