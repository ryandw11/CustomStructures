package com.ryandw11.structure.listener;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.utils.StructurePicker;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

/**
 * Class for when a chunk loads.
 *
 * @author Ryandw11
 */
public class ChunkLoad implements Listener {

    private CustomStructures plugin;

    public ChunkLoad() {
        this.plugin = CustomStructures.getInstance();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!CustomStructures.enabled) return;

        // Allow new chunk to be disabled.
        boolean newChunk = plugin.getConfig().contains("new_chunks") && !plugin.getConfig().getBoolean("new_chunks");
        if (!newChunk && !e.isNewChunk()) return;


        World w = e.getChunk().getWorld(); //Grabs the world
        Block b = e.getChunk().getBlock(0, 5, 0); //Grabs the block 0, 5, 0 in that chunk.

        boolean foundLand = false; //True when the block selected is an ideal place for a structure.
        Block bb;
        if (w.getHighestBlockYAt(b.getX(), b.getZ()) != -1)
            bb = e.getChunk().getBlock(0, w.getHighestBlockYAt(b.getX(), b.getZ()), 0); //grabs the highest block in that chunk at X = 0 and Z = 0 for that chunk.
        else
            bb = e.getChunk().getBlock(0, 20, 0);

//        int trys = 0;
//        while (!foundLand) {//While land was not found it keeps checking.
//            if (trys >= 20) return; //added anti crash.
//            if (bb.getType() != Material.AIR) {
//                foundLand = true;
//            } else {
//                bb = bb.getLocation().subtract(0, 1, 0).getBlock();
//            }
//            trys++;
//        }

        /*
         * Schematic handler
         * This activity is done async to prevent the server from lagging.
         */
        StructurePicker s = new StructurePicker(bb, e.getChunk(), CustomStructures.getInstance());
        s.runTaskTimer(CustomStructures.plugin, 1, 10);
    }
}
