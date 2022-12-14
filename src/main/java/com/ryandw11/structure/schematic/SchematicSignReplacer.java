package com.ryandw11.structure.schematic;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.properties.AdvancedSubSchematics;
import com.ryandw11.structure.structure.properties.SubSchematics;
import com.ryandw11.structure.structure.properties.schematics.SubSchematic;
import com.ryandw11.structure.utils.CSUtils;
import com.ryandw11.structure.utils.NumberStylizer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handle the replacement of signs in schematics.
 */
public class SchematicSignReplacer {
    private SchematicSignReplacer() {
    }

    /**
     * Process a sign and spawn mobs, execute commands etc.
     *
     * @param location The location of the sign.
     */
    protected static void processAndReplaceSign(Location location, Location minLoc, Location maxLoc) {
        CustomStructures plugin = CustomStructures.getInstance();

        Sign sign = (Sign) location.getBlock().getState();
        String firstLine;
        String secondLine;
        String thirdLine;
        String fourthLine;

        if (location.getBlock().getState() instanceof Sign || location.getBlock().getState() instanceof WallSign) {
            firstLine = sign.getLine(0).trim();
            secondLine = sign.getLine(1).trim();
            thirdLine = sign.getLine(2).trim();
            fourthLine = sign.getLine(3).trim();
        } else return;

        // Process the type of sign.
        // Normal Mob Sign
        if (firstLine.equalsIgnoreCase("[mob]")) {
            int count = 1;
            if (!thirdLine.isEmpty()) {
                try {
                    // Impose a maximum limit of 40 mobs.
                    count = Math.min(NumberStylizer.getStylizedInt(thirdLine), 40);
                } catch (NumberFormatException ex) {
                    // Ignore, keep count as 1.
                }
            }
            try {
                for (int i = 0; i < count; i++) {
                    Entity ent = Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.valueOf(secondLine.toUpperCase()));
                    if (ent instanceof LivingEntity livingEntity) {
                        livingEntity.setRemoveWhenFarAway(false);
                    }
                }

                location.getBlock().setType(Material.AIR);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid mob type on structure sign.");
            }
        }

        // NPC Sign
        if (firstLine.equalsIgnoreCase("[npc]")) {
            plugin.getCitizensNpcHook().spawnNpc(plugin.getNpcHandler(), secondLine, location);
            location.getBlock().setType(Material.AIR);
        }

        // Command Sign.
        if (firstLine.equalsIgnoreCase("[command]") || firstLine.equalsIgnoreCase("[commands]")) {
            List<String> commands = plugin.getSignCommandsHandler().getCommands(secondLine);
            if (commands != null) {
                for (String command : commands) {
                    command = CSUtils.replacePlaceHolders(command, location, minLoc, maxLoc);
                    command = CustomStructures.replacePAPIPlaceholders(command);
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                    if (plugin.isDebug()) {
                        plugin.getLogger().info("Executing console command: '" + command + "'");
                    }
                }
            } else {
                plugin.getLogger().warning("Unable to execute command group '" + secondLine + "', no configuration found!");
            }
            location.getBlock().setType(Material.AIR);
        }
        // Mythical Mob Sign
        if (firstLine.equalsIgnoreCase("[mythicmob]") || firstLine.equalsIgnoreCase("[mythicalmob]")) {
            int count = 1;
            if (!fourthLine.isEmpty()) {
                try {
                    // Impose a maximum limit of 40 mobs.
                    count = Math.min(NumberStylizer.getStylizedInt(fourthLine), 40);
                } catch (NumberFormatException ex) {
                    // Ignore, keep count as 1.
                }
            }
            // Allow for the third line to have the level of the mob.
            if (thirdLine.isEmpty())
                plugin.getMythicalMobHook().spawnMob(secondLine, location, count);
            else {
                double level;
                try {
                    level = Double.parseDouble(thirdLine);
                } catch (NumberFormatException ex) {
                    level = 1;
                }
                plugin.getMythicalMobHook().spawnMob(secondLine, location, level, count);
            }
            location.getBlock().setType(Material.AIR);
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

        if (firstLine.equalsIgnoreCase("[schematic]") || firstLine.equalsIgnoreCase("[schem]")) {
            int number = -1;
            if (secondLine.startsWith("[")) {
                String v = secondLine.replace("[", "").replace("]", "");
                String[] out = v.split("-");
                if (out.length != 2)
                    out = v.split(";");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);
                    number = ThreadLocalRandom.current().nextInt(num1, num2 + 1);

                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    plugin.getLogger().warning("Invalid schematic sign on structure. Cannot parse random numbers.");
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
                SchematicHandler.placeSchematic(location, subSchem.getFile(), subSchem.isPlacingAir(), parentStructure, iteration + 1);
            } catch (Exception ex) {
                plugin.getLogger().warning("An error has occurred when attempting to paste a sub schematic.");
                if (plugin.isDebug()) {
                    ex.printStackTrace();
                }
            }
        } else if (firstLine.equalsIgnoreCase("[advschem]")) {
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
