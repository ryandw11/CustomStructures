package com.ryandw11.structure.utils;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

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
                .replace("<world>", Objects.requireNonNull(signLocation.getWorld()).getName())
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

    public static void renameConfigString(ConfigurationSection configurationSection, String originalName, String newName) {
        if (configurationSection.contains(originalName)) {
            configurationSection.set(newName, configurationSection.getString(originalName));
            configurationSection.set(originalName, null);
        }
    }

    public static void renameConfigBoolean(ConfigurationSection configurationSection, String originalName, String newName) {
        if (configurationSection.contains(originalName)) {
            configurationSection.set(newName, configurationSection.getBoolean(originalName));
            configurationSection.set(originalName, null);
        }
    }

    public static void renameConfigInteger(ConfigurationSection configurationSection, String originalName, String newName) {
        if (configurationSection.contains(originalName)) {
            configurationSection.set(newName, configurationSection.getInt(originalName));
            configurationSection.set(originalName, null);
        }
    }

    public static void renameConfigStringList(ConfigurationSection configurationSection, String originalName, String newName) {
        if (configurationSection.contains(originalName)) {
            configurationSection.set(newName, configurationSection.getStringList(originalName));
            configurationSection.set(originalName, null);
        }
    }

    public static void renameStringConfigurationSection(ConfigurationSection configurationSection, String originalName, String newName) {
        if (!configurationSection.contains(originalName)) return;

        for (String key : configurationSection.getKeys(false)) {
           configurationSection.set(newName + "." + key, configurationSection.getString(originalName + "." + key));
        }
        configurationSection.set(originalName, null);
    }
}
