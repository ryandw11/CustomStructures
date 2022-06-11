package com.ryandw11.structure.utils;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.SchematicHandler;
import com.ryandw11.structure.api.structaddon.StructureSection;
import com.ryandw11.structure.exceptions.StructureConfigurationException;
import com.ryandw11.structure.ignoreblocks.IgnoreBlocks;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.StructureHandler;
import com.ryandw11.structure.structure.properties.BlockLevelLimit;
import com.ryandw11.structure.structure.properties.StructureYSpawning;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * This class prevents the server from crashing when it attempts to pick a
 * structure.
 * <p>
 * The server will still lag a bit thanks to the nature of 1.14.
 * </p>
 *
 * @author Ryandw11
 */
public class StructurePicker extends BukkitRunnable {

    private final CustomStructures plugin;

    private int currentStructure;
    private final StructureHandler structureHandler;
    private final IgnoreBlocks ignoreBlocks;

    private final Block bl;
    private final Chunk ch;
    // Variable that contains the structureBlock of the current structure being processed.
    private Block structureBlock;

    public StructurePicker(@Nullable Block bl, Chunk ch, CustomStructures plugin) {
        this.plugin = plugin;
        currentStructure = -1;
        this.bl = bl;
        this.ch = ch;
        this.structureHandler = plugin.getStructureHandler();
        this.ignoreBlocks = plugin.getBlockIgnoreManager();

        if (this.structureHandler == null) {
            plugin.getLogger().warning("A structure is trying to spawn without the plugin initialization step being completed.");
            plugin.getLogger().warning("If you are using a fork of Spigot, this likely means that the fork does not adhere to the API standard properly.");
            throw new RuntimeException("Plugin Not Initialized.");
        }
    }

    @Override
    public void run() {
        try {
            currentStructure++;
            if (currentStructure >= structureHandler.getStructures().size()) {
                this.cancel();
                return;
            }

            Structure structure = structureHandler.getStructure(currentStructure);
            StructureYSpawning structureSpawnSettings = structure.getStructureLocation().getSpawnSettings();


            // Get the highest block according to the settings for the structure.
            structureBlock = structureSpawnSettings.getHighestBlock(bl.getLocation());

            // If the block is the void, then set it to null to maintain compatibility.
            if (structureBlock.getType() == Material.VOID_AIR) {
                structureBlock = null;
            }

            // Calculate the chance.
            if (!structure.canSpawn(structureBlock, ch))
                return;

            // If the block is null, Skip the other steps and spawn.
            if (structureBlock == null) {
                structureBlock = ch.getBlock(8, structureSpawnSettings.getHeight(null), 8);
                // Now to finally paste the schematic
                SchematicHandler sh = new SchematicHandler();
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    // It is assumed at this point that the structure has been spawned.
                    // Add it to the list of spawned structures.
                    plugin.getStructureHandler().putSpawnedStructure(structureBlock.getLocation(),
                            structure);
                    try {
                        sh.schemHandle(structureBlock.getLocation(),
                                structure.getSchematic(),
                                structure.getStructureProperties().canPlaceAir(),
                                structure);
                    } catch (IOException | WorldEditException e) {
                        e.printStackTrace();
                    }
                });

                // Cancel the process and return.
                this.cancel();
                return;
            }

            // Allows the structures to no longer spawn on plant life.
            if (structure.getStructureProperties().isIgnoringPlants() && ignoreBlocks.getBlocks().contains(structureBlock.getType())) {
                for (int i = structureBlock.getY(); i >= 4; i--) {
                    if (!ignoreBlocks.getBlocks().contains(ch.getBlock(8, i, 8).getType()) && !ch.getBlock(8, i, 8).getType().isAir()) {
                        structureBlock = ch.getBlock(8, i, 8);
                        break;
                    }
                }
            }

            // calculate SpawnY if first is true
            if (structureSpawnSettings.isCalculateSpawnYFirst()) {
                structureBlock = ch.getBlock(8, structureSpawnSettings.getHeight(structureBlock.getLocation()), 8);
            }

            if (!structure.getStructureLimitations().hasWhitelistBlock(structureBlock))
                return;

            if (structure.getStructureLimitations().hasBlacklistBlock(structureBlock))
                return;

            // If it can spawn in water
            if (!structure.getStructureProperties().canSpawnInWater()) {
                if (structureBlock.getType() == Material.WATER) return;
            }

            // If the structure can spawn in lava
            if (!structure.getStructureProperties().canSpawnInLavaLakes()) {
                if (structureBlock.getType() == Material.LAVA) return;
            }

            // calculate SpawnY if first is false
            if (!structureSpawnSettings.isCalculateSpawnYFirst()) {
                structureBlock = ch.getBlock(8, structureSpawnSettings.getHeight(structureBlock.getLocation()), 8);
            }

            // If the structure can follows block level limit.
            // This only triggers if it spawns on the top.
            if (structure.getStructureLimitations().getBlockLevelLimit().isEnabled()) {
                BlockLevelLimit limit = structure.getStructureLimitations().getBlockLevelLimit();
                if (limit.getMode().equalsIgnoreCase("flat")) {
                    for (int x = limit.getX1() + structureBlock.getX(); x <= limit.getX2() + structureBlock.getX(); x++) {
                        for (int z = limit.getZ1() + structureBlock.getZ(); z <= limit.getZ2() + structureBlock.getZ(); z++) {
                            Block top = ch.getWorld().getBlockAt(x, structureBlock.getY() + 1, z);
                            Block bottom = ch.getWorld().getBlockAt(x, structureBlock.getY() - 1, z);
                            if (!(top.getType().isAir() || ignoreBlocks.getBlocks().contains(top.getType())))
                                return;
                            if (bottom.getType().isAir())
                                return;
                        }
                    }
                } else if (limit.getMode().equalsIgnoreCase("flat_error")) {
                    int total = 0;
                    int error = 0;
                    for (int x = limit.getX1() + structureBlock.getX(); x <= limit.getX2() + structureBlock.getX(); x++) {
                        for (int z = limit.getZ1() + structureBlock.getZ(); z <= limit.getZ2() + structureBlock.getZ(); z++) {
                            Block top = ch.getWorld().getBlockAt(x, structureBlock.getY() + 1, z);
                            Block bottom = ch.getWorld().getBlockAt(x, structureBlock.getY() - 1, z);
                            if (!(top.getType().isAir() || ignoreBlocks.getBlocks().contains(top.getType())))
                                error++;
                            if (bottom.getType().isAir())
                                error++;

                            total += 2;
                        }
                    }

                    if (((double) error / total) > limit.getError())
                        return;
                }
            }

            for (StructureSection section : structure.getStructureSections()) {
                // Check if the structure can spawn according to the section.
                // If an error occurs, report it to the user.
                try {
                    if (!section.checkStructureConditions(structure, structureBlock, ch)) return;
                } catch (Exception ex) {
                    plugin.getLogger().severe(String.format("[CS Addon] An error has occurred when attempting to spawn" +
                            "the structure %s with the custom property %s!", structure.getName(), section.getName()));
                    plugin.getLogger().severe("This is not a CustomStructures error! Please report" +
                            "this to the developer of the addon.");
                    if (plugin.isDebug()) {
                        ex.printStackTrace();
                    } else {
                        plugin.getLogger().severe("Enable debug mode to see the stack trace.");
                    }
                    return;
                }
            }

            // Now to finally paste the schematic
            SchematicHandler sh = new SchematicHandler();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                // It is assumed at this point that the structure has been spawned.
                // Add it to the list of spawned structures.
                plugin.getStructureHandler().putSpawnedStructure(structureBlock.getLocation(),
                        structure);
                try {
                    sh.schemHandle(structureBlock.getLocation(),
                            structure.getSchematic(),
                            structure.getStructureProperties().canPlaceAir(),
                            structure);
                } catch (IOException | WorldEditException e) {
                    e.printStackTrace();
                }
            });

            this.cancel();// return after pasting
        } catch (StructureConfigurationException ex) {
            this.cancel();
            plugin.getLogger().severe("A configuration error was encountered when attempting to spawn the structure: "
                    + structureHandler.getStructure(currentStructure).getName());
            plugin.getLogger().severe(ex.getMessage());
        } catch (Exception ex) {
            this.cancel();
            plugin.getLogger().severe("An error was encountered during the schematic pasting section.");
            plugin.getLogger().severe("The task was stopped for the safety of your server!");
            plugin.getLogger().severe("For more information enable debug mode.");
            if (plugin.isDebug())
                ex.printStackTrace();
        }
    }

}
