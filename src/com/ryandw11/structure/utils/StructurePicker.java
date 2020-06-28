package com.ryandw11.structure.utils;

import java.io.IOException;
import java.util.ArrayList;

import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.StructureHandler;
import com.ryandw11.structure.structure.properties.BlockLevelLimit;
import com.ryandw11.structure.structure.properties.StructureYSpawning;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.SchematicHandeler;
import com.sk89q.worldedit.WorldEditException;

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

    private CustomStructures plugin;

    private int currentStructure;
    private StructureHandler structureHandler;

    private Block bl;
    private Chunk ch;

    public StructurePicker(Block bl, Chunk ch, CustomStructures plugin) {
        this.plugin = plugin;
        currentStructure = -1;
        this.bl = bl;
        this.ch = ch;
        this.structureHandler = plugin.getStructureHandler();
    }

    @Override
    public void run() {
        try{
            currentStructure++;
            if (currentStructure >= structureHandler.getStructures().size()) {
                this.cancel();
                return;
            }

            Structure structure = structureHandler.getStructure(currentStructure);
            StructureYSpawning structureSpawnSettings = structure.getStructureLocation().getSpawnSettings();

            // Calculate the chance.
            if (!structure.canSpawn(bl, ch))
                return;

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
            if (structure.getStructureProperties().isIgnoringPlants() && CSConstants.leafBlocks.contains(bl.getType())) {
                for (int i = bl.getY(); i <= 4; i--) {
                    if (!CSConstants.leafBlocks.contains(ch.getBlock(0, i, 0).getType())) {
                        bl = ch.getBlock(0, i, 0);
                        break;
                    }
                }
            }

            // calculate spawny
            if (!structureSpawnSettings.isOceanFloor()) {
                bl = ch.getBlock(0, structureSpawnSettings.getHeight(bl.getY()), 0);
            }

            if (!structure.getStructureLimitations().hasBlock(bl))
                return;

            // If it can spawn in water
            if (!structure.getStructureProperties().canSpawnInWater()) {
                if (bl.getType() == Material.WATER) return;
            }

            // If the structure can spawn in lava
            if (!structure.getStructureProperties().canSpawnInLavaLakes()) {
                if (bl.getType() == Material.LAVA) return;
            }

            // If the structure can follows block level limit.
            if(structure.getStructureLimitations().getBlockLevelLimit().isEnabled()){
                BlockLevelLimit limit = structure.getStructureLimitations().getBlockLevelLimit();
                for(int x = limit.getX1(); x <= limit.getX2(); x++){
                    for(int z = limit.getZ1(); z <= limit.getZ2(); z++){
                        Block b = ch.getWorld().getBlockAt(x, bl.getY(), z);
                        if(b.getType() == Material.AIR) return;
                    }
                }
            }

            // Now to finally paste the schematic
            SchematicHandeler sh = new SchematicHandeler();
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                try {
                    sh.schemHandle(bl.getLocation(),
                            structure.getSchematic(),
                            structure.getStructureProperties().canPlaceAir(),
                            structure.getLootTables(),
                            structure);
                } catch (IOException | WorldEditException e) {
                    e.printStackTrace();
                }
            });

            this.cancel();// return after pasting
        }catch(Exception ex){
            this.cancel();
            plugin.getLogger().severe("An error was encountered during the schematic pasting section.");
            plugin.getLogger().severe("The task was stopped for the safety of your server!");
            plugin.getLogger().severe("For more information enable debug mode.");
            if(plugin.isDebug())
                ex.printStackTrace();
        }
    }

    protected ArrayList<String> getBiomes(String s) {
        String[] biomes = s.split(",");
        ArrayList<String> output = new ArrayList<String>();
        for (String b : biomes)
            output.add(b);
        return output;
    }

}
