package com.ryandw11.structure.bottomfill;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.structure.Structure;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

/**
 * The default implementation for the bottom fill feature.
 */
public class DefaultBottomFill extends BukkitRunnable implements BottomFillImpl {

    private Structure structure;
    private Location spawnLocation;
    private Material fillMaterial;
    private int minY;
    private Queue<BlockVector2> groundPlane; // The 2D plane which the blocks will be ground placed on

    @Override
    public void performFill(Structure structure, Location spawnLocation, Location minLoc, Location maxLoc) {
        // Deprecated
    }

    @Override
    public void performFill(Structure structure, Location spawnLocation, Location minLoc, Location maxLoc, AffineTransform transform) {

        Optional<Material> fillMaterial = structure.getBottomSpaceFill().getFillMaterial(spawnLocation.getBlock().getBiome());
        if (fillMaterial.isPresent()) {
            this.fillMaterial = fillMaterial.get();
        } else return;

        this.structure = structure;
        this.spawnLocation = spawnLocation;
        this.groundPlane = new LinkedList<>();
        this.minY = minLoc.getBlockY();

        Bukkit.getScheduler().runTaskAsynchronously(CustomStructures.getInstance(), () -> {

            // ---- This part of code should be safe to run async ----

            // To get the ground plane, we need to read the schematic
            File file = new File(CustomStructures.getInstance().getDataFolder() + "/schematics/" + structure.getSchematic());
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
                CustomStructures.getInstance().getLogger().warning("Invalid schematic format for schematic " + structure.getSchematic());
                CustomStructures.getInstance().getLogger().warning("Please create a valid schematic using the in-game commands");
                return;
            }
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                Clipboard clipboard = reader.read();

                // The new origin point which the structure is pasted onto
                int oX = spawnLocation.getBlockX();
                int oY = spawnLocation.getBlockY();
                int oZ = spawnLocation.getBlockZ();

                int clipboardMinY = clipboard.getMinimumPoint().getBlockY();

                for (int x = clipboard.getMinimumPoint().getBlockX(); x <= clipboard.getMaximumPoint().getBlockX(); x++) {
                    for (int z = clipboard.getMinimumPoint().getBlockZ(); z <= clipboard.getMaximumPoint().getBlockZ(); z++) {
                        // Loop through bottom plane of the region

                        if (clipboard.getBlock(BlockVector3.at(x, clipboardMinY, z)).getBlockType().getMaterial().isMovementBlocker()) {
                            // Find the certain point of the bottom plane which bottom fill should start at

                            BlockVector3 groundPoint = BlockVector3.at(x, clipboardMinY, z);

                            groundPoint = groundPoint.subtract(clipboard.getOrigin()); // Translate point back to origin (0,0)
                            Vector3 transformed = transform.apply(groundPoint.toVector3()); // Apply transformation (rotation, etc.)
                            groundPoint = transformed.add(oX, oY, oZ).toBlockPoint(); // Translate point back (to new origin)

                            groundPlane.add(groundPoint.toBlockVector2());
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                CustomStructures.getInstance().getLogger().warning("Cannot find schematic file " + file.getPath());
                CustomStructures.getInstance().getLogger().warning("Bottom fill will not be applied to structure " + structure.getName());
                return;
            } catch (IOException e) {
                CustomStructures.getInstance().getLogger().warning("Some unknown error occurs while reading " + file.getPath());
                CustomStructures.getInstance().getLogger().warning("Bottom fill will not be applied to structure " + structure.getName());
                return;
            }

            // ---- Then do the block placement on the main thread ----

            Bukkit.getScheduler().runTask(CustomStructures.getInstance(), () -> {
                runTaskTimer(CustomStructures.getInstance(), 0, 1);
            });
        });
    }

    @Override
    public void run() {
        World world = spawnLocation.getWorld();
        if (world == null) {
            CustomStructures.getInstance().getLogger().warning("The world in which the structure " + structure.getName() + " spawns is not loaded");
            CustomStructures.getInstance().getLogger().warning("Bottom fill will not be applied to structure " + structure.getName());
            cancel();
            return;
        }

        for (int i = 0; i < 8; i++) { // Select 8 ground points in a single tick
            BlockVector2 groundPoint = groundPlane.poll();
            if (groundPoint == null) {
                cancel();
                return;
            }

            int y = minY - 1;
            int x = groundPoint.getBlockX();
            int z = groundPoint.getBlockZ();
            for (int j = 0; j < 64; j++) { // Fill the bottom space of the selected ground points down to 64 blocks
                boolean shouldFill =
                        // If the block is empty
                        world.getBlockAt(x, y, z).isEmpty() ||
                        // Or if the block is in the list of ignore blocks.
                        CustomStructures.getInstance().getBlockIgnoreManager().getBlocks().contains(world.getBlockAt(x, y, z).getType()) ||
                        // Or if it is water (if it is set to be ignored)
                        (structure.getStructureProperties().shouldIgnoreWater() && world.getBlockAt(x, y, z).getType() == Material.WATER);
                if (shouldFill) {
                    world.getBlockAt(x, y--, z).setType(fillMaterial);
                } else break;
            }
        }
    }

}
