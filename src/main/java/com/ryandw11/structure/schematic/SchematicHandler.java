package com.ryandw11.structure.schematic;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.StructureSpawnEvent;
import com.ryandw11.structure.api.holder.StructureSpawnHolder;
import com.ryandw11.structure.bottomfill.BottomFillProvider;
import com.ryandw11.structure.io.BlockTag;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.properties.MaskProperty;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.MaskUnion;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.ryandw11.ods.ObjectDataStructure;
import me.ryandw11.ods.tags.IntTag;
import me.ryandw11.ods.tags.ListTag;
import me.ryandw11.ods.tags.ObjectTag;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Handles schematic operations.
 */
public class SchematicHandler {

    private SchematicHandler() {
    }

    /**
     * Handles the actual pasting of the structure.
     * <p>This method is to be called on the main Server thread.</p>
     *
     * @param loc       - The location
     * @param filename  - The file name. Ex: demo.schematic
     * @param useAir    - if air is to be used in the schematic
     * @param structure - The structure that is getting spawned.
     * @param iteration - The number of iterations in a structure.
     * @throws WorldEditException If world edit has a problem pasting the schematic.
     * @throws IOException        If an error occurs during file reading.
     */
    public static void placeSchematic(Location loc, String filename, boolean useAir, Structure structure, int iteration)
            throws IOException, WorldEditException {

        CustomStructures plugin = CustomStructures.getInstance();

        if (iteration > structure.getStructureLimitations().getIterationLimit()) {
            plugin.getLogger().severe("Critical Error: StackOverflow detected. Automatically terminating the spawning of the structure.");
            plugin.getLogger().severe("The structure '" + structure.getName() + "' has spawned too many sub structure via recursion.");
            return;
        }

        File schematicFile = new File(plugin.getDataFolder() + "/schematics/" + filename);
        // Check to see if the schematic is a thing.
        if (!schematicFile.exists() && iteration == 0) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    "&b[&aCustomStructures&b] &cA fatal error has occurred! Please check the console for errors."));
            plugin.getLogger().warning("Error: The schematic " + filename + " does not exist!");
            plugin.getLogger().warning(
                    "If this is your first time using this plugin you need to put a schematic in the schematic folder.");
            plugin.getLogger().warning("Then add it into the config.");
            plugin.getLogger().warning(
                    "If you need help look at the wiki: https://github.com/ryandw11/CustomStructures/wiki or contact Ryandw11 on spigot!");
            plugin.getLogger().warning("The plugin will now disable to prevent damage to the server.");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        } else if (!schematicFile.exists()) {
            plugin.getLogger().warning("Error: The schematic " + filename + " does not exist!");
            throw new RuntimeException("Cannot find schematic file!");
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        Clipboard clipboard;

        if (format == null) {
            plugin.getLogger().warning("Invalid schematic format for schematic " + filename + "!");
            plugin.getLogger().warning("Please create a valid schematic using the in-game commands!");
            return;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            clipboard = reader.read();
        }

        ClipboardHolder ch = new ClipboardHolder(clipboard);
        AffineTransform transform = new AffineTransform();

        // Define rotation y with the default base rotation.
        double rotY = Math.toDegrees(structure.getBaseRotation());

        // If random rotation is enabled, rotate the clipboard
        if (structure.getStructureProperties().isRandomRotation() && iteration == 0) {
            rotY = new Random().nextInt(4) * 90;
            transform = transform.rotateY(rotY);
            ch.setTransform(ch.getTransform().combine(transform));
        } else if (iteration != 0) {
            rotY = Math.toDegrees(structure.getSubSchemRotation());
            transform = transform.rotateY(rotY);
            ch.setTransform(ch.getTransform().combine(transform));
        }

        // Paste the schematic
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(BukkitAdapter.adapt(Objects.requireNonNull(loc.getWorld())), -1)) {
             /*
                Handle the masks of the structure.
             */
            Mask sourceMask = null;
            if (structure.getSourceMaskProperties().getUnionType() == MaskProperty.MaskUnion.AND) {
                sourceMask = new MaskIntersection(structure.getSourceMaskProperties().getMasks(clipboard));
            } else if (structure.getSourceMaskProperties().getUnionType() == MaskProperty.MaskUnion.OR) {
                sourceMask = new MaskUnion(structure.getSourceMaskProperties().getMasks(clipboard));
            }

            Mask targetMask = null;
            if (structure.getTargetMaskProperties().getUnionType() == MaskProperty.MaskUnion.AND) {
                targetMask = new MaskIntersection(structure.getTargetMaskProperties().getMasks(editSession));
            } else if (structure.getSourceMaskProperties().getUnionType() == MaskProperty.MaskUnion.OR) {
                targetMask = new MaskUnion(structure.getTargetMaskProperties().getMasks(editSession));
            }
            editSession.setMask(targetMask);

            Operation operation = ch.createPaste(editSession)
                    .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ())).maskSource(sourceMask).ignoreAirBlocks(!useAir).build();

            Operations.complete(operation);

            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info(String.format("(%s) Created an instance of %s at %s, %s, %s with rotation %s", loc.getWorld().getName(), filename, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), rotY));
            }
        }

        // If enabled, perform a bottom space fill.
        if (structure.getBottomSpaceFill().isEnabled()) {
            Location minLoc = SchematicLocationUtils.getMinimumLocation(clipboard, loc, rotY);
            Location maxLoc = SchematicLocationUtils.getMaximumLocation(clipboard, loc, rotY);
            int lowX = Math.min(minLoc.getBlockX(), maxLoc.getBlockX());
            int lowY = Math.min(minLoc.getBlockY(), maxLoc.getBlockY());
            int lowZ = Math.min(minLoc.getBlockZ(), maxLoc.getBlockZ());
            int highX = Math.max(minLoc.getBlockX(), maxLoc.getBlockX());
            int highY = Math.max(minLoc.getBlockY(), maxLoc.getBlockY());
            int highZ = Math.max(minLoc.getBlockZ(), maxLoc.getBlockZ());
            BottomFillProvider.provide().performFill(structure, loc, new Location(minLoc.getWorld(), lowX, lowY, lowZ), new Location(minLoc.getWorld(), highX, highY, highZ), transform);
        }

        // Schedule the signs & containers replacement task
        double finalRotY = rotY;
        // Run a task later. This is done so async plugins have time to paste as needed.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            List<Location> containersAndSignsLocations = new ArrayList<>();
            // If the structure is compiled, then grab the data from the cschem file.
            if (structure.isCompiled()) {
                ObjectDataStructure ods = new ObjectDataStructure(new File(plugin.getDataFolder() + "/schematics/" + structure.getCompiledSchematic()));
                ListTag<ObjectTag> containers = ods.get("containers");
                ListTag<ObjectTag> signs = ods.get("signs");
                // Get both the max and minimum points.
                Location minimumPoint = SchematicLocationUtils.getMinimumLocation(clipboard, loc, 0);
                Location maximumPoint = SchematicLocationUtils.getMaximumLocation(clipboard, loc, 0);

                // Find the minimum of all three axises.
                int minX = Math.min(minimumPoint.getBlockX(), maximumPoint.getBlockX());
                int minY = Math.min(minimumPoint.getBlockY(), maximumPoint.getBlockY());
                int minZ = Math.min(minimumPoint.getBlockZ(), maximumPoint.getBlockZ());

                for (ObjectTag con : containers.getValue()) {
                    // Rotate con around the point and add the rotated min values.
                    containersAndSignsLocations.add(SchematicLocationUtils.rotateAround(new BlockTag(con).getLocation(loc.getWorld()).add(minX, minY, minZ), loc, finalRotY));
                }
                for (ObjectTag sign : signs.getValue()) {
                    containersAndSignsLocations.add(SchematicLocationUtils.rotateAround(new BlockTag(sign).getLocation(loc.getWorld()).add(minX, minY, minZ), loc, finalRotY));
                }
            } else {
                // else find the data from the paste.
                containersAndSignsLocations = getContainersAndSignsLocations(ch.getClipboard(), loc, finalRotY, structure);
            }

            for (Location location : containersAndSignsLocations) {
                if (location.getBlock().getState() instanceof Container) {
                    LootTableReplacer.replaceContainerContent(structure, location);
                }
                if (location.getBlock().getState() instanceof Sign) {
                    Location minLoc = SchematicLocationUtils.getMinimumLocation(clipboard, loc, finalRotY);
                    Location maxLoc = SchematicLocationUtils.getMaximumLocation(clipboard, loc, finalRotY);
                    SchematicSignReplacer.processAndReplaceSign(location, minLoc, maxLoc);
                }
                // If the sign still exists, it could be a sub-schematic sign.
                if (location.getBlock().getState() instanceof Sign) {
                    SchematicSignReplacer.replaceSignWithSchematic(location, structure, iteration);
                }
            }

            // Replace the blocks of the structure (if enabled).
            replaceBlocks(clipboard, loc, finalRotY, structure);

            // Call the event for use by other plugins (only if it is the first iteration though.)
            if (iteration < 1) {
                StructureSpawnHolder structureSpawnHolder = new StructureSpawnHolder(SchematicLocationUtils.getMinimumLocation(clipboard, loc, 0),
                        SchematicLocationUtils.getMaximumLocation(clipboard, loc, 0), containersAndSignsLocations);
                StructureSpawnEvent structureSpawnEvent = new StructureSpawnEvent(structure, loc, finalRotY, structureSpawnHolder);
                Bukkit.getServer().getPluginManager().callEvent(structureSpawnEvent);
            }

        }, Math.round(structure.getStructureLimitations().getReplacementBlocksDelay() * 20));
    }

    /**
     * Handles the schematic.
     * <p>This method is to be called on the main Server thread.</p>
     *
     * @param loc       - The location
     * @param filename  - The file name. Ex: demo.schematic
     * @param useAir    - if air is to be used in the schematic
     * @param structure - The structure that is getting spawned.
     * @throws WorldEditException If world edit has a problem pasting the schematic.
     * @throws IOException        If an error occurs during file reading.
     */
    public static void placeSchematic(Location loc, String filename, boolean useAir, Structure structure)
            throws IOException, WorldEditException {
        placeSchematic(loc, filename, useAir, structure, 0);
    }

    /**
     * Create a schematic and save it to the schematics folder in the CustomStructures plugin.
     *
     * @param name    The name of the schematic.
     * @param player  The player.
     * @param w       The world
     * @param compile If the schematic should be compiled.
     * @return If the operation was successful.
     */
    public static boolean createSchematic(String name, Player player, World w, boolean compile) {
        CustomStructures plugin = CustomStructures.getInstance();

        try {
            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
            assert worldEditPlugin != null;
            Region selection = worldEditPlugin.getSession(player).getSelection(BukkitAdapter.adapt(w));
            CuboidRegion region = new CuboidRegion(selection.getWorld(), selection.getMinimumPoint(), selection.getMaximumPoint());
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

            // Set the origin point to where the player is standing.
            clipboard.setOrigin(BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(selection.getWorld(), -1)) {
                ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                        editSession, region, clipboard, region.getMinimumPoint()
                );
                // configure here
                Operations.complete(forwardExtentCopy);
            } catch (WorldEditException e) {
                e.printStackTrace();
            }

            File file = new File(plugin.getDataFolder() + File.separator + "schematics" + File.separator + name + ".schem");

            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                writer.write(clipboard);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (compile)
                compileSchem(player.getLocation(), selection, name);
            return true;
        } catch (IncompleteRegionException ex) {
            return false;
        }
    }

    /**
     * Only compile a selection into a compiled schematic.
     *
     * @param name   The name of the schematic.
     * @param player The player.
     * @param w      The world
     * @return If the operation was successful.
     */
    public static boolean compileOnly(String name, Player player, World w) {
        try {
            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
            assert worldEditPlugin != null;
            Region selection = worldEditPlugin.getSession(player).getSelection(BukkitAdapter.adapt(w));

            compileSchem(player.getLocation(), selection, name);
            return true;
        } catch (IncompleteRegionException ex) {
            return false;
        }
    }

    /**
     * Compiles a schematic
     *
     * @param loc  The location of the player.
     * @param reg  The region of the schematic.
     * @param name The name of the schematic.
     */
    private static void compileSchem(Location loc, Region reg, String name) {
        CustomStructures plugin = CustomStructures.getInstance();

        IntTag intTag = new IntTag("ver", CustomStructures.COMPILED_STRUCT_VER);
        ListTag<BlockTag> containers = new ListTag<>("containers", new ArrayList<>());
        ListTag<BlockTag> signs = new ListTag<>("signs", new ArrayList<>());

        List<Location> locations = new ArrayList<>();

        Location minLoc = new Location(loc.getWorld(), reg.getMinimumPoint().getX(), reg.getMinimumPoint().getY(), reg.getMinimumPoint().getZ());

        for (int x = reg.getMinimumPoint().getX(); x <= reg.getMaximumPoint().getX(); x++) {
            for (int y = reg.getMinimumPoint().getY(); y <= reg.getMaximumPoint().getY(); y++) {
                for (int z = reg.getMinimumPoint().getZ(); z <= reg.getMaximumPoint().getZ(); z++) {
                    Location location = new Location(loc.getWorld(), x, y, z);
                    Block block = location.getBlock();
                    BlockState blockState = location.getBlock().getState();

                    if (blockState instanceof Container) {
                        if (blockState instanceof Chest chestBlockState) {
                            InventoryHolder holder = chestBlockState.getInventory().getHolder();
                            if (holder instanceof DoubleChest doubleChest) {
                                Location leftSideLocation = ((Chest) doubleChest.getLeftSide()).getLocation();
                                Location rightSideLocation = ((Chest) doubleChest.getRightSide()).getLocation();

                                Location roundedLocation = new Location(location.getWorld(),
                                        Math.floor(location.getX()), Math.floor(location.getY()),
                                        Math.floor(location.getZ()));

                                // Check to see if this (or the other) side of the chest is already in the list
                                if (leftSideLocation.distance(roundedLocation) < 1) {
                                    if (SchematicLocationUtils.isNotAlreadyIn(locations, rightSideLocation)) {
                                        locations.add(roundedLocation);
                                        containers.addTag(new BlockTag(Material.CHEST, location.subtract(minLoc)));
                                    }

                                } else if (rightSideLocation.distance(roundedLocation) < 1) {
                                    if (SchematicLocationUtils.isNotAlreadyIn(locations, leftSideLocation)) {
                                        locations.add(roundedLocation);
                                        containers.addTag(new BlockTag(Material.CHEST, location.subtract(minLoc)));
                                    }
                                }

                            } else if (holder instanceof Chest) {
                                locations.add(location);
                                containers.addTag(new BlockTag(Material.CHEST, location.subtract(minLoc)));
                            }
                        } else {
                            locations.add(location);
                            containers.addTag(new BlockTag(block.getType(), location.subtract(minLoc)));
                        }
                    } else if (blockState instanceof Sign) {
                        locations.add(location);
                        signs.addTag(new BlockTag(block.getType(), location.subtract(minLoc)));
                    }
                }
            }
        }
        ObjectDataStructure ods = new ObjectDataStructure(new File(plugin.getDataFolder() + File.separator + "schematics" + File.separator + name + ".cschem"));
        ods.save(Arrays.asList(intTag, containers, signs));
        if (plugin.isDebug()) {
            plugin.getLogger().info("Successfully compiled the schematic: " + name);
        }
    }

    /**
     * Replace the blocks according to the 'replacement_blocks' section.
     * <p>Note: This is to be used by compiled schematics. Non compiled schematics are replaced in
     * the {@link #getContainersAndSignsLocations(Clipboard, Location, double, Structure)} method to save time.</p>
     *
     * @param clipboard     The clipboard of the paste.
     * @param pasteLocation The paste location.
     * @param rotation      The rotation of the structure.
     * @param structure     The structure itself.
     */
    private static void replaceBlocks(Clipboard clipboard, Location pasteLocation, double rotation, Structure structure) {
        if (structure.getStructureLimitations().getBlockReplacement().isEmpty()) return;

        Location minLoc = SchematicLocationUtils.getMinimumLocation(clipboard, pasteLocation, rotation);
        Location maxLoc = SchematicLocationUtils.getMaximumLocation(clipboard, pasteLocation, rotation);

        int lowX = Math.min(minLoc.getBlockX(), maxLoc.getBlockX());
        int lowY = Math.min(minLoc.getBlockY(), maxLoc.getBlockY());
        int lowZ = Math.min(minLoc.getBlockZ(), maxLoc.getBlockZ());

        for (int x = 0; x <= Math.abs(minLoc.getBlockX() - maxLoc.getBlockX()); x++) {
            for (int y = 0; y <= Math.abs(minLoc.getBlockY() - maxLoc.getBlockY()); y++) {
                for (int z = 0; z <= Math.abs(minLoc.getBlockZ() - maxLoc.getBlockZ()); z++) {
                    Block block = Objects.requireNonNull(pasteLocation.getWorld()).getBlockAt(lowX + x, lowY + y, lowZ + z);
                    if (structure.getStructureLimitations().getBlockReplacement().containsKey(block.getType())) {
                        block.setType(structure.getStructureLimitations().getBlockReplacement().get(block.getType()));
                        block.getState().update();
                    }
                }
            }
        }
    }

    /**
     * Get the location of containers and signs.
     * <p>This will also replace blocks from the replacement_blocks section.</p>
     *
     * @param clipboard     The worldedit clipboard
     * @param pasteLocation The location of the paste
     * @param rotation      The rotate value (in degrees).
     * @return The list of locations
     */
    private static List<Location> getContainersAndSignsLocations(Clipboard clipboard, Location pasteLocation, double rotation, Structure structure) {
        Location minLoc = SchematicLocationUtils.getMinimumLocation(clipboard, pasteLocation, rotation);
        Location maxLoc = SchematicLocationUtils.getMaximumLocation(clipboard, pasteLocation, rotation);
        List<Location> locations = new ArrayList<>();

        int lowX = Math.min(minLoc.getBlockX(), maxLoc.getBlockX());
        int lowY = Math.min(minLoc.getBlockY(), maxLoc.getBlockY());
        int lowZ = Math.min(minLoc.getBlockZ(), maxLoc.getBlockZ());

        for (int x = 0; x <= Math.abs(minLoc.getBlockX() - maxLoc.getBlockX()); x++) {
            for (int y = 0; y <= Math.abs(minLoc.getBlockY() - maxLoc.getBlockY()); y++) {
                for (int z = 0; z <= Math.abs(minLoc.getBlockZ() - maxLoc.getBlockZ()); z++) {
                    Location location = new Location(pasteLocation.getWorld(), lowX + x, lowY + y, lowZ + z);
                    Block block = location.getBlock();
                    BlockState blockState = location.getBlock().getState();

                    if (blockState instanceof Container) {
                        if (blockState instanceof Chest) {
                            InventoryHolder holder = ((Chest) blockState).getInventory().getHolder();
                            if (holder instanceof DoubleChest doubleChest) {
                                Location leftSideLocation = ((Chest) doubleChest.getLeftSide()).getLocation();
                                Location rightSideLocation = ((Chest) doubleChest.getRightSide()).getLocation();

                                Location roundedLocation = new Location(location.getWorld(),
                                        Math.floor(location.getX()), Math.floor(location.getY()),
                                        Math.floor(location.getZ()));

                                // Check to see if this (or the other) side of the chest is already in the list
                                if (leftSideLocation.distance(roundedLocation) < 1) {
                                    if (SchematicLocationUtils.isNotAlreadyIn(locations, rightSideLocation)) {
                                        locations.add(roundedLocation);
                                    }

                                } else if (rightSideLocation.distance(roundedLocation) < 1) {
                                    if (SchematicLocationUtils.isNotAlreadyIn(locations, leftSideLocation)) {
                                        locations.add(roundedLocation);
                                    }
                                }

                            } else if (holder instanceof Chest) {
                                locations.add(location);
                            }
                        } else {
                            locations.add(location);
                        }
                    } else if (blockState instanceof Sign) {
                        locations.add(location);
                    } else {
                        // For the block replacement system.
                        if (!structure.getStructureLimitations().getBlockReplacement().isEmpty()) {
                            if (structure.getStructureLimitations().getBlockReplacement().containsKey(block.getType())) {
                                block.setType(structure.getStructureLimitations().getBlockReplacement().get(block.getType()));
                                block.getState().update();
                            }
                        }
                    }
                }
            }
        }
        return locations;
    }
}