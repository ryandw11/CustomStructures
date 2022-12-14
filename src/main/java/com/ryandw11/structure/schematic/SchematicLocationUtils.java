package com.ryandw11.structure.schematic;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;

import java.util.List;

/**
 * Location utilities for schematic placement.
 */
public class SchematicLocationUtils {
    private SchematicLocationUtils() {}

    /**
     * Checks to see if a location is not already inside a list of locations.
     *
     * @param locations The list of locations.
     * @param location  The location to check
     * @return If it is not already in.
     */
    protected static boolean isNotAlreadyIn(List<Location> locations, Location location) {
        for (Location auxLocation : locations) {
            if (location.distance(auxLocation) < 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Rotate the point around the center.
     *
     * @param point  The point
     * @param center The center
     * @param angle  The angle to rotate by.
     * @return The final position.
     */
    public static BlockVector3 rotateAround(BlockVector3 point, BlockVector3 center, double angle) {
        angle = Math.toRadians(angle * -1);
        double rotatedX = Math.cos(angle) * (point.getX() - center.getX()) - Math.sin(angle) * (point.getZ() - center.getZ()) + center.getX();
        double rotatedZ = Math.sin(angle) * (point.getX() - center.getX()) + Math.cos(angle) * (point.getZ() - center.getZ()) + center.getZ();

        return BlockVector3.at(rotatedX, point.getY(), rotatedZ);
    }

    /**
     * Rotate the point around a center.
     *
     * @param point  The point
     * @param center The center
     * @param angle  The angle to rotate by.
     * @return The final position (in Location form).
     */
    public static Location rotateAround(Location point, Location center, double angle) {
        angle = Math.toRadians(angle * -1);
        double rotatedX = Math.cos(angle) * (point.getBlockX() - center.getBlockX()) - Math.sin(angle) * (point.getBlockZ() - center.getBlockZ()) + center.getBlockX();
        double rotatedZ = Math.sin(angle) * (point.getBlockX() - center.getBlockX()) + Math.cos(angle) * (point.getBlockZ() - center.getBlockZ()) + center.getBlockZ();

        return new Location(point.getWorld(), Math.floor(rotatedX), point.getY(), Math.floor(rotatedZ));
    }

    /**
     * Get the minimum location of a structure.
     *
     * @param clipboard     The clipboard of the paste.
     * @param pasteLocation The paste location.
     * @param rotation      The rotation of the structure.
     * @return The minimum location.
     */
    public static Location getMinimumLocation(Clipboard clipboard, Location pasteLocation, double rotation) {
        BlockVector3 originalOrigin = clipboard.getOrigin();
        BlockVector3 originalMinimumPoint = clipboard.getRegion().getMinimumPoint();

        BlockVector3 originalMinimumOffset = originalOrigin.subtract(originalMinimumPoint);

        BlockVector3 newOrigin = BukkitAdapter.asBlockVector(pasteLocation);
        BlockVector3 newMinimumPoint = newOrigin.subtract(originalMinimumOffset);

        BlockVector3 newRotatedMinimumPoint = rotateAround(newMinimumPoint, newOrigin, rotation);

        return new Location(pasteLocation.getWorld(), newRotatedMinimumPoint.getX(), newRotatedMinimumPoint.getY(), newRotatedMinimumPoint.getZ());
    }

    /**
     * Get the maximum location of a structure.
     *
     * @param clipboard     The clipboard of the paste.
     * @param pasteLocation The paste location.
     * @param rotation      The rotation of the structure.
     * @return The maximum location.
     */
    public static Location getMaximumLocation(Clipboard clipboard, Location pasteLocation, double rotation) {
        BlockVector3 originalOrigin = clipboard.getOrigin();
        BlockVector3 originalMaximumPoint = clipboard.getRegion().getMaximumPoint();

        BlockVector3 originalMaximumOffset = originalOrigin.subtract(originalMaximumPoint);

        BlockVector3 newOrigin = BukkitAdapter.asBlockVector(pasteLocation);
        BlockVector3 newMaximumPoint = newOrigin.subtract(originalMaximumOffset);

        BlockVector3 newRotatedMaximumPoint = rotateAround(newMaximumPoint, newOrigin, rotation);

        return new Location(pasteLocation.getWorld(), newRotatedMaximumPoint.getX(), newRotatedMaximumPoint.getY(), newRotatedMaximumPoint.getZ());
    }
}
