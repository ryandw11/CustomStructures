package com.ryandw11.structure.utils;

import org.bukkit.Location;

/**
 * General utilities for Custom Structures.
 */
public class CSUtils {
    /**
     * Replace the placeholders on commands in the command group.
     *
     * @param command      The command.
     * @param signLocation The location of the sign.
     * @param minLoc       The minimum location of the structure.
     * @param maxLoc       The maximum location of the structure.
     * @return The command with the placeholders replaced.
     */
    public static String replacePlaceHolders(String command, Location signLocation, Location minLoc, Location maxLoc) {
        return command
                .replace("<x>", "" + signLocation.getBlockX())
                .replace("<y>", "" + signLocation.getBlockY())
                .replace("<z>", "" + signLocation.getBlockZ())
                .replace("<structX1>", "" + minLoc.getBlockX())
                .replace("<structY1>", "" + minLoc.getBlockY())
                .replace("<structZ1>", "" + minLoc.getBlockZ())
                .replace("<structX2>", "" + maxLoc.getBlockX())
                .replace("<structY2>", "" + maxLoc.getBlockY())
                .replace("<structZ2>", "" + maxLoc.getBlockZ());
    }
}
