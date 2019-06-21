package com.ryandw11.structure.listener;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

//import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.utils.Structures;

/**
 * Class for when a chunk loads.
 * @author Ryandw11
 *
 */
public class ChunkLoad implements Listener{
	
	//private CustomStructures plugin;
	
	public ChunkLoad(){
		//this.plugin = CustomStructures.plugin;
	}
	
	@EventHandler
	public void loadevent(ChunkLoadEvent e){
		if(e.isNewChunk()){ //Checks to see if the chunk is new or an old one.
			World w = e.getChunk().getWorld(); //Grabs the world
			Block b = e.getChunk().getBlock(0, 5, 0); //Grabs the block 0, 5, 0 in that chunk.

			boolean foundLand = false; //True when the block selected is an ideal place for a structure.
			Block bb = e.getChunk().getBlock(0, w.getHighestBlockYAt(b.getX(), b.getZ()), 0); //grabs the highest block in that chunk at X = 0 and Z = 0 for that chunk.
			int trys = 0;
			while (!foundLand){//While land was not found it keeps checking.
				if(trys >= 20) return; //added anti crash.
				if(bb.getType() != Material.AIR){
					foundLand = true;
				}
				else{
					bb = bb.getLocation().subtract(0, 1, 0).getBlock();
				}
				trys++;
			}
			
			/*
			 * Schematic handeler
			 */
			Structures s = new Structures();
			s.chooseBestStructure(bb, e.getChunk());
		}
	}
}
