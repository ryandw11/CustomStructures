package com.ryandw11.structure;

import com.ryandw11.structure.api.LootPopulateEvent;
import com.ryandw11.structure.api.StructureSpawnEvent;
import com.ryandw11.structure.api.holder.StructureSpawnHolder;
import com.ryandw11.structure.io.BlockTag;
import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.loottables.LootTableType;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.properties.MaskProperty;
import com.ryandw11.structure.structure.properties.SubSchematics;
import com.ryandw11.structure.structure.properties.schematics.SubSchematic;
import com.ryandw11.structure.utils.RandomCollection;
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
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class SchematicHandler {

    private final CustomStructures plugin;

    public SchematicHandler() {
        this.plugin = CustomStructures.plugin;
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
    public void schemHandle(Location loc, String filename, boolean useAir, Structure structure, int iteration)
            throws IOException, WorldEditException {

        if (iteration > 2) {
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
            Mask mi = null;
            if (structure.getMaskProperties().getUnionType() == MaskProperty.MaskUnion.AND) {
                mi = new MaskIntersection(structure.getMaskProperties().getMasks(clipboard));
            } else if (structure.getMaskProperties().getUnionType() == MaskProperty.MaskUnion.OR) {
                mi = new MaskUnion(structure.getMaskProperties().getMasks(clipboard));
            }

            Operation operation = ch.createPaste(editSession)
                    .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ())).maskSource(mi).ignoreAirBlocks(!useAir).build();

            Operations.complete(operation);

            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info(String.format("(%s) Created an instance of %s at %s, %s, %s with rotation %s", loc.getWorld().getName(), filename, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), rotY));
            }
        }

        //Schedule the signs & containers replacement task
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
                Location minimumPoint = getMinimumLocation(clipboard, loc, 0);
                Location maximumPoint = getMaximumLocation(clipboard, loc, 0);

                // Find the minimum of all three axises.
                int minX = Math.min(minimumPoint.getBlockX(), maximumPoint.getBlockX());
                int minY = Math.min(minimumPoint.getBlockY(), maximumPoint.getBlockY());
                int minZ = Math.min(minimumPoint.getBlockZ(), maximumPoint.getBlockZ());

                for (ObjectTag con : containers.getValue()) {
                    // Rotate con around the point and add the rotated min values.
                    containersAndSignsLocations.add(rotateAround(new BlockTag(con).getLocation(loc.getWorld()).add(minX, minY, minZ), loc, finalRotY));
                }
                for (ObjectTag sign : signs.getValue()) {
                    containersAndSignsLocations.add(rotateAround(new BlockTag(sign).getLocation(loc.getWorld()).add(minX, minY, minZ), loc, finalRotY));
                }
                // Replace the blocks of the structure (if enabled).
                replaceBlocks(clipboard, loc, finalRotY, structure);
            } else {
                // else find the data from the paste.
                containersAndSignsLocations = getContainersAndSignsLocations(ch.getClipboard(), loc, finalRotY, structure);
            }

            for (Location location : containersAndSignsLocations) {
                if (location.getBlock().getState() instanceof Container) {
                    replaceContainerContent(structure, location);
                }
                if (location.getBlock().getState() instanceof Sign) {
                    Location minLoc = getMinimumLocation(clipboard, loc, finalRotY);
                    Location maxLoc = getMaximumLocation(clipboard, loc, finalRotY);
                    processAndReplaceSign(location, minLoc, maxLoc);
                }
                // This is separate so that if the block doesn't exist anymore than it will not error out.
                if (location.getBlock().getState() instanceof Sign) {
                    replaceSignWithSchematic(location, structure.getSubSchematics(), structure, iteration);
                }
            }

            // Call the event for use by other plugins (only if it is the first iteration though.)
            if (iteration < 1) {
                StructureSpawnHolder structureSpawnHolder = new StructureSpawnHolder(getMinimumLocation(clipboard, loc, 0),
                        getMaximumLocation(clipboard, loc, 0), containersAndSignsLocations);
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
    public void schemHandle(Location loc, String filename, boolean useAir, Structure structure)
            throws IOException, WorldEditException {
        schemHandle(loc, filename, useAir, structure, 0);
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
    public boolean createSchematic(String name, Player player, World w, boolean compile) {
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
    public boolean compileOnly(String name, Player player, World w) {
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
    private void compileSchem(Location loc, Region reg, String name) {
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
                        if (blockState instanceof Chest) {
                            InventoryHolder holder = ((Chest) blockState).getInventory().getHolder();
                            if (holder instanceof DoubleChest) {
                                DoubleChest doubleChest = ((DoubleChest) holder);
                                Location leftSideLocation = ((Chest) doubleChest.getLeftSide()).getLocation();
                                Location rightSideLocation = ((Chest) doubleChest.getRightSide()).getLocation();

                                Location roundedLocation = new Location(location.getWorld(),
                                        Math.floor(location.getX()), Math.floor(location.getY()),
                                        Math.floor(location.getZ()));

                                // Check to see if this (or the other) side of the chest is already in the list
                                if (leftSideLocation.distance(roundedLocation) < 1) {
                                    if (this.isNotAlreadyIn(locations, rightSideLocation)) {
                                        locations.add(roundedLocation);
                                        containers.addTag(new BlockTag(Material.CHEST, location.subtract(minLoc)));
                                    }

                                } else if (rightSideLocation.distance(roundedLocation) < 1) {
                                    if (this.isNotAlreadyIn(locations, leftSideLocation)) {
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
     * Get the minimum location of a structure.
     *
     * @param clipboard     The clipboard of the paste.
     * @param pasteLocation The paste location.
     * @param rotation      The rotation of the structure.
     * @return The minimum location.
     */
    private Location getMinimumLocation(Clipboard clipboard, Location pasteLocation, double rotation) {
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
    private Location getMaximumLocation(Clipboard clipboard, Location pasteLocation, double rotation) {
        BlockVector3 originalOrigin = clipboard.getOrigin();
        BlockVector3 originalMaximumPoint = clipboard.getRegion().getMaximumPoint();

        BlockVector3 originalMaximumOffset = originalOrigin.subtract(originalMaximumPoint);

        BlockVector3 newOrigin = BukkitAdapter.asBlockVector(pasteLocation);
        BlockVector3 newMaximumPoint = newOrigin.subtract(originalMaximumOffset);

        BlockVector3 newRotatedMaximumPoint = rotateAround(newMaximumPoint, newOrigin, rotation);

        return new Location(pasteLocation.getWorld(), newRotatedMaximumPoint.getX(), newRotatedMaximumPoint.getY(), newRotatedMaximumPoint.getZ());
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
    private void replaceBlocks(Clipboard clipboard, Location pasteLocation, double rotation, Structure structure) {
        if (structure.getStructureLimitations().getBlockReplacement().isEmpty()) return;

        Location minLoc = getMinimumLocation(clipboard, pasteLocation, rotation);
        Location maxLoc = getMaximumLocation(clipboard, pasteLocation, rotation);

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
    private List<Location> getContainersAndSignsLocations(Clipboard clipboard, Location pasteLocation, double rotation, Structure structure) {
        Location minLoc = getMinimumLocation(clipboard, pasteLocation, rotation);
        Location maxLoc = getMaximumLocation(clipboard, pasteLocation, rotation);
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
                            if (holder instanceof DoubleChest) {
                                DoubleChest doubleChest = ((DoubleChest) holder);
                                Location leftSideLocation = ((Chest) doubleChest.getLeftSide()).getLocation();
                                Location rightSideLocation = ((Chest) doubleChest.getRightSide()).getLocation();

                                Location roundedLocation = new Location(location.getWorld(),
                                        Math.floor(location.getX()), Math.floor(location.getY()),
                                        Math.floor(location.getZ()));

                                // Check to see if this (or the other) side of the chest is already in the list
                                if (leftSideLocation.distance(roundedLocation) < 1) {
                                    if (this.isNotAlreadyIn(locations, rightSideLocation)) {
                                        locations.add(roundedLocation);
                                    }

                                } else if (rightSideLocation.distance(roundedLocation) < 1) {
                                    if (this.isNotAlreadyIn(locations, leftSideLocation)) {
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

    /**
     * Checks to see if a location is not already inside of a list of locations.
     *
     * @param locations The list of locations.
     * @param location  The location to check
     * @return If it is not already in.
     */
    private boolean isNotAlreadyIn(List<Location> locations, Location location) {
        for (Location auxLocation : locations) {
            if (location.distance(auxLocation) < 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Replace the contents of a container with the loot table from a structure.
     *
     * @param structure The structure that is being spawned.
     * @param location  The location of the container.
     */
    private void replaceContainerContent(Structure structure, Location location) {
        if (structure.getLootTables().isEmpty()) return;

        BlockState blockState = location.getBlock().getState();
        Container container = (Container) blockState;
        Inventory containerInventory = container.getInventory();
        Block block = location.getBlock();
        LootTableType blockType = LootTableType.valueOf(block.getType());

        RandomCollection<LootTable> tables = structure.getLootTables(blockType);
        if (tables == null) return;

        LootTable lootTable = tables.next();
        Random random = new Random();

        // Trigger the loot populate event.
        LootPopulateEvent event = new LootPopulateEvent(structure, location, lootTable);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCanceled()) return;

        for (int i = 0; i < lootTable.getRolls(); i++) {
            if (lootTable.getTypes().contains(blockType) && containerInventory instanceof FurnaceInventory) {
                this.replaceFurnaceContent(lootTable, random, (FurnaceInventory) containerInventory);
            } else if (lootTable.getTypes().contains(blockType) && containerInventory instanceof BrewerInventory) {
                this.replaceBrewerContent(lootTable, random, (BrewerInventory) containerInventory);
            } else if (lootTable.getTypes().contains(blockType)) {
                this.replaceChestContent(lootTable, random, containerInventory);
            }
        }

    }

    /**
     * Process a sign and spawn mobs, execute commands etc.
     *
     * @param location The location of the sign.
     */
    private void processAndReplaceSign(Location location, Location minLoc, Location maxLoc) {
        Sign sign = (Sign) location.getBlock().getState();
        String firstLine;
        String secondLine;
        String thirdLine;

        if (location.getBlock().getState() instanceof Sign) {
            firstLine = sign.getLine(0).trim();
            secondLine = sign.getLine(1).trim();
            thirdLine = sign.getLine(2).trim();
        } else if (location.getBlock().getState() instanceof WallSign) {
            firstLine = sign.getLine(0).trim();
            secondLine = sign.getLine(1).trim();
            thirdLine = sign.getLine(2).trim();
        } else return;

        if (firstLine.equalsIgnoreCase("[mob]")) {
            try {
                Entity ent = Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.valueOf(secondLine.toUpperCase()));
                if (ent instanceof LivingEntity) {
                    LivingEntity livingEntity = (LivingEntity) ent;
                    livingEntity.setRemoveWhenFarAway(false);
                }
                location.getBlock().setType(Material.AIR);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid mob type on structure sign.");
            }
        }
        if (firstLine.equalsIgnoreCase("[npc]")) {
            plugin.citizensNpcHook.spawnNpc(plugin.getNpcHandler(), secondLine, location);
            location.getBlock().setType(Material.AIR);
        }
        if (firstLine.equalsIgnoreCase("[commands]")) {
            String alias = secondLine;
            SignCommandsHandler.CommandGroupInfo info = plugin.getSignCommandsHandler().getCommandGroupInfoByAlias(alias);
            if(info != null) {
                List<String> lastName = new ArrayList<>();
                for(String command : info.commands) {
                    command = replacePlaceHolders(command, location, minLoc, maxLoc, lastName);
                    Bukkit.getLogger().info("Executing console command: '" + command + "'");
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            } else {
                Bukkit.getLogger().info("> Unable to execute commands with alias: '" + alias + "', no configuration found!");
            }
            location.getBlock().setType(Material.AIR);
        }
        if (firstLine.equalsIgnoreCase("[mythicmob]") || firstLine.equalsIgnoreCase("[mythicalmob]")) {
            // Allow for the third line to have the level of the mob.
            if (thirdLine.isEmpty())
                plugin.mythicalMobHook.spawnMob(secondLine, location);
            else {
                int level;
                try {
                    level = Integer.parseInt(thirdLine);
                } catch (NumberFormatException ex) {
                    level = 1;
                }
                plugin.mythicalMobHook.spawnMob(secondLine, location, level);
            }
            location.getBlock().setType(Material.AIR);
        }
    }

    private String replacePlaceHolders(String command, Location location, Location minLoc, Location maxLoc, List<String> lastName) {
        command = command.replace("<signX>", "" + location.getBlockX());
        command = command.replace("<signY>", "" + location.getBlockY());
        command = command.replace("<signZ>", "" + location.getBlockZ());

        command = command.replace("<structX1>", "" + minLoc.getBlockX());
        command = command.replace("<structY1>", "" + minLoc.getBlockY());
        command = command.replace("<structZ1>", "" + minLoc.getBlockZ());

        command = command.replace("<structX2>", "" + maxLoc.getBlockX());
        command = command.replace("<structY2>", "" + maxLoc.getBlockY());
        command = command.replace("<structZ2>", "" + maxLoc.getBlockZ());

        return command;
    }

    /**
     * Replace a sign with a schematic.
     *
     * @param location        The location of the sign.
     * @param subSchematics   The sub schematic handler for the structure.
     * @param parentStructure The parent structure.
     * @param iteration       The iteration of schematic pasting.
     */
    private void replaceSignWithSchematic(Location location, SubSchematics subSchematics, Structure parentStructure, int iteration) {
        Sign sign = (Sign) location.getBlock().getState();
        String firstLine = sign.getLine(0).trim();
        String secondLine = sign.getLine(1).trim();

        // Allow this to work with both wall signs and normal signs.
        if (location.getBlock().getBlockData() instanceof org.bukkit.block.data.type.Sign) {
            org.bukkit.block.data.type.Sign signData = (org.bukkit.block.data.type.Sign) location.getBlock().getBlockData();

            Vector direction = signData.getRotation().getDirection();
            double rotation = Math.atan2(direction.getZ(), direction.getX());
            if (direction.getX() != 0) {
                rotation -= (Math.PI / 2);
            } else {
                rotation += (Math.PI / 2);
            }
            parentStructure.setSubSchemRotation(rotation);
        } else if (location.getBlock().getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
            org.bukkit.block.data.type.WallSign signData = (org.bukkit.block.data.type.WallSign) location.getBlock().getBlockData();
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
                schemHandle(location, subSchem.getFile(), subSchem.isPlacingAir(), parentStructure, iteration + 1);
            } catch (Exception ex) {
                plugin.getLogger().warning("An error has occurred when attempting to paste a sub schematic.");
                if (plugin.isDebug()) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * Replace the chest content.
     *
     * @param lootTable          The loot table.
     * @param random             The value of random.
     * @param containerInventory The container inventory
     */
    private void replaceChestContent(LootTable lootTable, Random random, Inventory containerInventory) {
        ItemStack[] containerContent = containerInventory.getContents();

        ItemStack randomItem = lootTable.getRandomWeightedItem();

        for (int j = 0; j < randomItem.getAmount(); j++) {
            boolean done = false;
            int attemps = 0;
            while (!done) {
                int randomPos = random.nextInt(containerContent.length);
                ItemStack randomPosItem = containerInventory.getItem(randomPos);
                if (randomPosItem != null) {

                    if (this.isSameItem(randomPosItem, randomItem)) {
                        if (randomPosItem.getAmount() < randomItem.getMaxStackSize()) {
                            ItemStack randomItemCopy = randomItem.clone();
                            int newAmount = randomPosItem.getAmount() + 1;
                            randomItemCopy.setAmount(newAmount);
                            containerContent[randomPos] = randomItemCopy;
                            containerInventory.setContents(containerContent);
                            done = true;
                        }
                    }
                } else {
                    ItemStack randomItemCopy = randomItem.clone();
                    randomItemCopy.setAmount(1);
                    containerContent[randomPos] = randomItemCopy;
                    containerInventory.setContents(containerContent);
                    done = true;

                }
                attemps++;
                if (attemps >= containerContent.length) {
                    done = true;
                }
            }
        }
    }

    private boolean isSameItem(ItemStack randomPosItem, ItemStack randomItem) {
        ItemMeta randomPosItemMeta = randomPosItem.getItemMeta();
        ItemMeta randomItemMeta = randomItem.getItemMeta();

        return randomPosItem.getType().equals(randomItem.getType()) && randomPosItemMeta.equals(randomItemMeta);
    }

    private void replaceBrewerContent(LootTable lootTable, Random random, BrewerInventory containerInventory) {
        ItemStack item = lootTable.getRandomWeightedItem();
        ItemStack ingredient = containerInventory.getIngredient();
        ItemStack fuel = containerInventory.getFuel();

        if ((ingredient == null) || ingredient.equals(item)) {
            containerInventory.setIngredient(item);
        } else if ((fuel == null) || fuel.equals(item)) {
            containerInventory.setFuel(item);
        }

    }

    private void replaceFurnaceContent(LootTable lootTable, Random random, FurnaceInventory containerInventory) {
        ItemStack item = lootTable.getRandomWeightedItem();
        ItemStack result = containerInventory.getResult();
        ItemStack fuel = containerInventory.getFuel();
        ItemStack smelting = containerInventory.getSmelting();

        if ((result == null) || result.equals(item)) {
            containerInventory.setResult(item);
        } else if ((fuel == null) || fuel.equals(item)) {
            containerInventory.setFuel(item);
        } else if ((smelting == null) || smelting.equals(item)) {
            containerInventory.setSmelting(item);
        }
    }

    /**
     * Rotate the point around the center.
     *
     * @param point  The point
     * @param center The center
     * @param angle  The angle to rotate by.
     * @return The final position.
     */
    private BlockVector3 rotateAround(BlockVector3 point, BlockVector3 center, double angle) {
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
    private Location rotateAround(Location point, Location center, double angle) {
        angle = Math.toRadians(angle * -1);
        double rotatedX = Math.cos(angle) * (point.getBlockX() - center.getBlockX()) - Math.sin(angle) * (point.getBlockZ() - center.getBlockZ()) + center.getBlockX();
        double rotatedZ = Math.sin(angle) * (point.getBlockX() - center.getBlockX()) + Math.cos(angle) * (point.getBlockZ() - center.getBlockZ()) + center.getBlockZ();

        return new Location(point.getWorld(), Math.floor(rotatedX), point.getY(), Math.floor(rotatedZ));
    }
}