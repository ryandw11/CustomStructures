package com.ryandw11.structure.listener;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.utils.StructurePicker;
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

    private final CustomStructures plugin;

    public ChunkLoad() {
        this.plugin = CustomStructures.getInstance();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!CustomStructures.enabled) return;

        // Allow new chunk to be disabled.
        boolean newChunk = plugin.getConfig().contains("new_chunks") && !plugin.getConfig().getBoolean("new_chunks");
        if (!newChunk && !e.isNewChunk()) return;


        Block b = e.getChunk().getBlock(8, 5, 8); //Grabs the block 8, 5, 8 in that chunk.

        /*
         * Schematic handler
         * This activity is done async to prevent the server from lagging.
         */
        StructurePicker s = new StructurePicker(b, e.getChunk(), CustomStructures.getInstance());
        s.runTaskTimer(CustomStructures.plugin, 1, 10);
    }
}
