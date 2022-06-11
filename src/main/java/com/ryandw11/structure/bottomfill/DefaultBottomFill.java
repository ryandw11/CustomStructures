package com.ryandw11.structure.bottomfill;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.structure.Structure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * The default implementation for the bottom fill feature.
 */
public class DefaultBottomFill extends BukkitRunnable implements BottomFillImpl {

    private Location minLoc;
    private Location maxLoc;
    private Location location;
    private Structure structure;
    private int currentX;
    private int currentY;
    private int currentZ;
    private BukkitTask bukkitTask;

    @Override
    public void performFill(Structure structure, Location spawnLocation, Location minLoc, Location maxLoc) {
        if (structure.getBottomSpaceFill().getFillMaterial(spawnLocation.getBlock().getBiome()).isEmpty())
            return;

        this.structure = structure;
        this.location = spawnLocation;
        this.minLoc = new Location(minLoc.getWorld(), minLoc.getBlockX(), minLoc.getBlockY() - 1, minLoc.getBlockZ());
        this.maxLoc = new Location(minLoc.getWorld(), maxLoc.getBlockX(), minLoc.getBlockY() - 1, maxLoc.getBlockZ());
        currentX = minLoc.getBlockX();
        currentY = minLoc.getBlockY() - 1;
        currentZ = minLoc.getBlockZ();
        bukkitTask = this.runTaskTimer(CustomStructures.getInstance(), 0, 1);
    }

    @Override
    public void run() {
        // TODO:: Make it so that if there is not block at the bottom of the structure, then there will not be ground placed.
        for (int i = 0; i < 40; i++) {

            if (
                // If the block is not empty
                    !location.getWorld().getBlockAt(currentX, currentY, currentZ).isEmpty()
                            // and the block is not in the list of ignore blocks.
                            && !CustomStructures.getInstance().getBlockIgnoreManager().getBlocks()
                            .contains(location.getWorld().getBlockAt(currentX, currentY, currentZ).getType())
                            // And not water (if it is set to be ignored)
                            && !(structure.getStructureProperties().shouldIgnoreWater()
                            && location.getWorld().getBlockAt(currentX, currentY, currentZ).getType() == Material.WATER)
            ) {
                // Then move on.
                currentY = minLoc.getBlockY();
                currentX++;
            }

            if (currentX > maxLoc.getBlockX()) {
                currentX = minLoc.getBlockX();
                currentZ++;
            }

            if (currentZ > maxLoc.getBlockZ()) {
                this.cancel();
                bukkitTask = null;
                return;
            }

            if (structure.getBottomSpaceFill().getFillMaterial(location.getBlock().getBiome()).isPresent())
                location.getWorld().getBlockAt(currentX, currentY, currentZ)
                        .setType(structure.getBottomSpaceFill().getFillMaterial(location.getBlock().getBiome()).get());

            currentY--;
        }
    }
}
