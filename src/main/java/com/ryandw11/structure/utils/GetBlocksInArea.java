package com.ryandw11.structure.utils;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

//Thanks Cycryl! https://www.spigotmc.org/threads/getting-all-blocks-between-to-locations.272852/

public class GetBlocksInArea {
	/**
	 * Get all of the locations between two points.
	 * @param loc1
	 * @param loc2
	 * @return The list of locations.
	 */
	// TODO, this process could (and should) be optimized in the future.
    public static List<Location> getLocationListBetween(Location loc1, Location loc2){
        int lowX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int lowY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int lowZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());

        List<Location> locs = new ArrayList<>();

        for(int x = 0; x<= Math.abs(loc1.getBlockX() - loc2.getBlockX()); x++){
            for(int y = 0; y<= Math.abs(loc1.getBlockY() - loc2.getBlockY()); y++){
                for(int z = 0; z<= Math.abs(loc1.getBlockZ() - loc2.getBlockZ()); z++){
                    locs.add(new Location(loc1.getWorld(), lowX + x, lowY + y, lowZ + z));
                }
            }
        }
        return locs;
    }
}
