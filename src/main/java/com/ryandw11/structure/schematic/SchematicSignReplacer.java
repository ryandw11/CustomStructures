package com.ryandw11.structure.schematic;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.StructureSign;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.properties.AdvancedSubSchematics;
import com.ryandw11.structure.structure.properties.SubSchematics;
import com.ryandw11.structure.structure.properties.schematics.SubSchematic;
import com.ryandw11.structure.structure.properties.schematics.VerticalRepositioning;
import com.ryandw11.structure.utils.CSUtils;
import com.ryandw11.structure.utils.NumberStylizer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;

/**
 * Handle the replacement of signs in schematics.
 */
public class SchematicSignReplacer {
    private SchematicSignReplacer() {
    }

    /**
     * Process a structure sign.
     *
     * @param location          The location of the sign.
     * @param minLoc            The minimum location of the structure schematic.
     * @param maxLoc            The maximum location of the structure schematic.
     * @param structure         The structure that was spawned.
     * @param structureRotation The rotation of the structure.
     */
    protected static void processAndReplaceSign(Location location, Location minLoc, Location maxLoc, Structure structure, double structureRotation) {
        CustomStructures plugin = CustomStructures.getInstance();

        if (!(location.getBlock().getState() instanceof Sign || location.getBlock().getState() instanceof WallSign)) {
            return;
        }

        Sign sign = (Sign) location.getBlock().getState();
        String firstLine = sign.getLine(0).trim();
        String secondLine = sign.getLine(1).trim();
        String thirdLine = sign.getLine(2).trim();
        String fourthLine = sign.getLine(3).trim();

        if (!firstLine.startsWith("["))
            return;

        String signName = firstLine.replaceAll("\\[", "").replaceAll("]", "");

        if (!plugin.getStructureSignHandler().structureSignExists(signName))
            return;

        double signRotation;
        // Allow this to work with both wall signs and normal signs.
        if (location.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Sign signData) {

            Vector direction = signData.getRotation().getDirection();
            signRotation = Math.atan2(direction.getZ(), direction.getX());
            if (direction.getX() != 0) {
                signRotation -= (Math.PI / 2);
            } else {
                signRotation += (Math.PI / 2);
            }
        } else if (location.getBlock().getBlockData() instanceof WallSign signData) {
            Vector direction = signData.getFacing().getDirection();
            signRotation = Math.atan2(direction.getZ(), direction.getX());
            if (direction.getX() != 0) {
                signRotation -= (Math.PI / 2);
            } else {
                signRotation += (Math.PI / 2);
            }
        } else {
            signRotation = 0;
        }

        Class<? extends StructureSign> structureSignClass = plugin.getStructureSignHandler().getStructureSign(signName);
        try {
            StructureSign structureSign = structureSignClass.getConstructor().newInstance();

            String[] args = {secondLine, thirdLine, fourthLine};
            structureSign.initialize(args, signRotation, structureRotation, minLoc, maxLoc);

            // Replace the sign with air if desired.
            if (structureSign.onStructureSpawn(location, structure)) {
                location.getBlock().setType(Material.AIR);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            plugin.getLogger().severe(String.format("Unable to process structure sign %s in structure %s!", signName, structure.getName()));
            plugin.getLogger().severe("Does that structure sign class have a default constructor?");
        }
    }

    /**
     * Replace a sign with a schematic.
     *
     * @param location        The location of the sign.
     * @param parentStructure The parent structure.
     * @param iteration       The iteration of schematic pasting.
     */
    protected static void replaceSignWithSchematic(Location location, Structure parentStructure, int iteration) {
        CustomStructures plugin = CustomStructures.getInstance();

        SubSchematics subSchematics = parentStructure.getSubSchematics();
        AdvancedSubSchematics advancedSubSchematics = parentStructure.getAdvancedSubSchematics();

        Sign sign = (Sign) location.getBlock().getState();
        String firstLine = sign.getLine(0).trim();
        String secondLine = sign.getLine(1).trim();

        // Allow this to work with both wall signs and normal signs.
        if (location.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Sign signData) {

            Vector direction = signData.getRotation().getDirection();
            double rotation = Math.atan2(direction.getZ(), direction.getX());
            if (direction.getX() != 0) {
                rotation -= (Math.PI / 2);
            } else {
                rotation += (Math.PI / 2);
            }
            parentStructure.setSubSchemRotation(rotation);
        } else if (location.getBlock().getBlockData() instanceof WallSign signData) {
            Vector direction = signData.getFacing().getDirection();
            double rotation = Math.atan2(direction.getZ(), direction.getX());
            if (direction.getX() != 0) {
                rotation -= (Math.PI / 2);
            } else {
                rotation += (Math.PI / 2);
            }
            parentStructure.setSubSchemRotation(rotation);
        }

        // Normal Sub-Schematic
        if (firstLine.equalsIgnoreCase("[schematic]") || firstLine.equalsIgnoreCase("[schem]")) {
            int number;
            if (secondLine.startsWith("[")) {
                try {
                    number = NumberStylizer.retrieveRangedInput(secondLine);
                } catch (NumberFormatException ex) {
                    plugin.getLogger().warning("Invalid schematic sign on structure. Cannot parse ranged number.");
                    return;
                }
            } else {
                try {
                    number = Integer.parseInt(secondLine);
                } catch (NumberFormatException ex) {
                    plugin.getLogger().warning("Invalid schematic sign on structure. Cannot parse number.");
                    return;
                }
            }

            if (number < -1 || number >= subSchematics.getSchematics().size()) {
                plugin.getLogger().warning("Invalid schematic sign on structure. Schematic number is not within the valid bounds.");
                return;
            }

            // Remove the sign after placing the schematic.
            location.getBlock().setType(Material.AIR);

            SubSchematic subSchem = subSchematics.getSchematics().get(number);

            // Disable rotation if the structure is not using it.
            if (!subSchem.isUsingRotation())
                parentStructure.setSubSchemRotation(0);
            try {
                if (subSchem.getVerticalRepositioning() != null) {
                    VerticalRepositioning vertRep = subSchem.getVerticalRepositioning();
                    Location heightBlock = location.getWorld().getHighestBlockAt(location, vertRep.getSpawnYHeightMap()).getLocation();

                    int newSpawnY = vertRep.getSpawnY(heightBlock);
                    if (CSUtils.isPairInLocalRange(vertRep.getRange(), location.getBlockY(), newSpawnY)) {
                        location = new Location(location.getWorld(), location.getBlockX(), newSpawnY, location.getBlockZ());
                    } else {
                        if (vertRep.getNoPointSolution().equalsIgnoreCase("CURRENT")) {
                            // Do Nothing, keep the current location.
                        } else if (vertRep.getNoPointSolution().equalsIgnoreCase("PREVENT_SPAWN")) {
                            return;
                        } else {
                            newSpawnY = NumberStylizer.getStylizedSpawnY(vertRep.getNoPointSolution(), location);
                            location = new Location(location.getWorld(), location.getBlockX(), newSpawnY, location.getBlockZ());
                        }
                    }
                }
                SchematicHandler.placeSchematic(location, subSchem.getFile(), subSchem.isPlacingAir(), parentStructure, iteration + 1);
            } catch (Exception ex) {
                plugin.getLogger().warning("An error has occurred when attempting to paste a sub schematic.");
                if (plugin.isDebug()) {
                    ex.printStackTrace();
                }
            }
        }
        // Advanced sub-schematic.
        else if (firstLine.equalsIgnoreCase("[advschem]")) {
            if (!advancedSubSchematics.containsCategory(secondLine)) {
                plugin.getLogger().warning("Cannot replace Advanced Sub-Schematic sign.");
                plugin.getLogger().warning(String.format("The category \"%s\" does not exist!", secondLine));
                return;
            }

            // Remove the sign after placing the schematic.
            location.getBlock().setType(Material.AIR);

            SubSchematic subSchem = advancedSubSchematics.getCategory(secondLine).next();

            // Disable rotation if the structure is not using it.
            if (!subSchem.isUsingRotation())
                parentStructure.setSubSchemRotation(0);
            try {
                if (subSchem.getVerticalRepositioning() != null) {
                    VerticalRepositioning vertRep = subSchem.getVerticalRepositioning();

                    Location heightBlock = location.getWorld().getHighestBlockAt(location, vertRep.getSpawnYHeightMap()).getLocation();
                    int newSpawnY = vertRep.getSpawnY(heightBlock);

                    if (CSUtils.isPairInLocalRange(vertRep.getRange(), location.getBlockY(), newSpawnY)) {
                        location = new Location(location.getWorld(), location.getBlockX(), newSpawnY, location.getBlockZ());
                    } else {
                        if (vertRep.getNoPointSolution().equalsIgnoreCase("CURRENT")) {
                            // Do Nothing, keep the current location.
                        } else if (vertRep.getNoPointSolution().equalsIgnoreCase("PREVENT_SPAWN")) {
                            return;
                        } else {
                            newSpawnY = NumberStylizer.getStylizedSpawnY(vertRep.getNoPointSolution(), location);
                            location = new Location(location.getWorld(), location.getBlockX(), newSpawnY, location.getBlockZ());
                        }
                    }
                }

                SchematicHandler.placeSchematic(location, subSchem.getFile(), subSchem.isPlacingAir(), parentStructure, iteration + 1);
            } catch (Exception ex) {
                plugin.getLogger().warning("An error has occurred when attempting to paste a sub schematic.");
                if (plugin.isDebug()) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
