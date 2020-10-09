package com.ryandw11.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.properties.MaskProperty;
import com.ryandw11.structure.structure.properties.SubSchematics;
import com.ryandw11.structure.structure.properties.schematics.SubSchematic;
import com.ryandw11.structure.utils.GetBlocksInArea;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.mask.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.utils.RandomCollection;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;

public class SchematicHandeler {

    private CustomStructures plugin;

    public SchematicHandeler() {
        this.plugin = CustomStructures.plugin;
    }

    /**
     * Handles the schematic.
     * <p>This method is to be called on the main Server thread.</p>
     * @param loc        - The location
     * @param filename   - The file name. Ex: demo.schematic
     * @param useAir     - if air is to be used in the schematic
     * @param lootTables - The Loot Tables specified for this structure, if any.
     * @param structure  - The structure that is getting spawned.
     * @param iteration  - The number of iterations in a structure.
     * @throws WorldEditException If world edit has a problem pasting the schematic.
     */
    public void schemHandle(Location loc, String filename, boolean useAir, RandomCollection<LootTable> lootTables, Structure structure, int iteration)
            throws IOException, WorldEditException {

        if(iteration > 2){
            plugin.getLogger().severe("Critical Error: StackOverflow detected. Automatically terminating the spawning of the structure.");
            plugin.getLogger().severe("The structure '" + structure.getName() + "' has spawned too many sub structure via recursion.");
            return;
        }

        File schematicFile = new File(plugin.getDataFolder() + "/schematics/" + filename);
        // Check to see if the schematic is a thing.
        if (!schematicFile.exists() && iteration == 0) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
                    "&3[&2CustomStructures&3] &cA fatal error has occurred! Please check the console for errors."));
            plugin.getLogger().warning("Error: The schematic " + filename + " does not exist!");
            plugin.getLogger().warning(
                    "If this is your first time using this plugin you need to put a schematic in the schematic folder.");
            plugin.getLogger().warning("Then add it into the config.");
            plugin.getLogger().warning(
                    "If you need help look at the wiki: https://github.com/ryandw11/CustomStructures/wiki or contact Ryandw11 on spigot!");
            plugin.getLogger().warning("The plugin will now disable to prevent damage to the server.");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
        else if(!schematicFile.exists()){
            plugin.getLogger().warning("Error: The schematic " + filename + " does not exist!");
            throw new RuntimeException("Cannot find schematic file!");
        }

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        Clipboard clipboard;

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            clipboard = reader.read();
        }

        ClipboardHolder ch = new ClipboardHolder(clipboard);
        AffineTransform transform = new AffineTransform();
        int rotY = 0;

        // If random rotation is enabled, rotate the clipboard
        if (structure.getStructureProperties().isRandomRotation()) {
            rotY = new Random().nextInt(4) * 90;
            transform = transform.rotateY(rotY);
            ch.setTransform(ch.getTransform().combine(transform));
        }

        // Paste the schematic
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(BukkitAdapter.adapt(loc.getWorld()), -1)) {
             /*
                Handle the masks of the structure.
             */
             Mask mi = null;
            if(structure.getMaskProperties().getUnionType() == MaskProperty.MaskUnion.AND){
                mi = new MaskIntersection(structure.getMaskProperties().getMasks(editSession));
            }
            else if(structure.getMaskProperties().getUnionType() == MaskProperty.MaskUnion.OR){
                mi = new MaskUnion(structure.getMaskProperties().getMasks(editSession));
            }
            Operation operation = ch.createPaste(editSession)
                    .to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ())).maskSource(mi).ignoreAirBlocks(!useAir).build();

            Operations.complete(operation);

            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info(String.format("(%s) Created an instance of %s at %s, %s, %s with rotation %s", loc.getWorld().getName(), filename, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), rotY));
            }
        }

        //Schedule the signs & containers replacement task
        int finalRotY = rotY;
        // Run a task later. This is done so async plugins have time to paste as needed.
        Bukkit.getScheduler().runTaskLater(plugin, ()->{
            List<Location> containersAndSignsLocations = getContainersAndSignsLocations(ch.getClipboard(), loc, finalRotY, structure);
            for (Location location : containersAndSignsLocations) {
                if (location.getBlock().getState() instanceof Container) {
                    replaceContainerContent(lootTables, location);
                }
                if (location.getBlock().getState() instanceof Sign) {
                    replaceSignWithMob(location);
                }
                // This is separate so that if the block doesn't exist anymore than it will not error out.
                if(location.getBlock().getState() instanceof Sign){
                    replaceSignWithSchematic(location, structure.getSubSchematics(), structure, iteration);
                }
            }

        }, Math.round(structure.getStructureLimitations().getReplacementBlocksDelay() * 20));
    }

    /**
     * Handles the schematic.
     * <p>This method is to be called on the main Server thread.</p>
     * @param loc        - The location
     * @param filename   - The file name. Ex: demo.schematic
     * @param useAir     - if air is to be used in the schematic
     * @param lootTables - The Loot Tables specified for this structure, if any.
     * @param structure  - The structure that is getting spawned.
     * @throws WorldEditException If world edit has a problem pasting the schematic.
     */
    public void schemHandle(Location loc, String filename, boolean useAir, RandomCollection<LootTable> lootTables, Structure structure)
            throws IOException, WorldEditException {
        schemHandle(loc, filename, useAir, lootTables, structure, 0);
    }

    /**
     * Create a schematic and save it to the schematics folder in the CustomStructures plugin.
     * @param name The name of the schematic.
     * @param player The player.
     * @param w The world
     * @return If the operation was successful.
     */
    public boolean createSchematic(String name, Player player, World w)  {
        try{
            WorldEditPlugin worldEditPlugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
            assert worldEditPlugin != null;
            Region selection = worldEditPlugin.getSession(player).getSelection(BukkitAdapter.adapt(w));
            CuboidRegion region = new CuboidRegion(selection.getWorld(), selection.getMinimumPoint(), selection.getMaximumPoint());
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

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
            return true;
        } catch (IncompleteRegionException ex){
            return false;
        }
    }

    /**
     * Get the location of containers and signs.
     *
     * @param clipboard     The worldedit clipboard
     * @param pasteLocation The location of the paste
     * @param rotation      The rotate value (in degrees).
     * @return The list of locations
     */
    private List<Location> getContainersAndSignsLocations(Clipboard clipboard, Location pasteLocation, int rotation, Structure structure) {

        BlockVector3 originalOrigin = clipboard.getOrigin();
        BlockVector3 originalMinimumPoint = clipboard.getRegion().getMinimumPoint();
        BlockVector3 originalMaximumPoint = clipboard.getRegion().getMaximumPoint();

        BlockVector3 originalMinimumOffset = originalOrigin.subtract(originalMinimumPoint);
        BlockVector3 originalMaximumOffset = originalOrigin.subtract(originalMaximumPoint);

        BlockVector3 newOrigin = BukkitAdapter.asBlockVector(pasteLocation);
        BlockVector3 newMinimumPoint = newOrigin.subtract(originalMinimumOffset);
        BlockVector3 newMaximumPoint = newOrigin.subtract(originalMaximumOffset);

        BlockVector3 newRotatedMinimumPoint = rotateAround(newMinimumPoint, newOrigin, rotation);
        BlockVector3 newRotatedMaximumPoint = rotateAround(newMaximumPoint, newOrigin, rotation);

        Location minLoc = new Location(pasteLocation.getWorld(), newRotatedMinimumPoint.getX(), newRotatedMinimumPoint.getY(), newRotatedMinimumPoint.getZ());
        Location maxLoc = new Location(pasteLocation.getWorld(), newRotatedMaximumPoint.getX(), newRotatedMaximumPoint.getY(), newRotatedMaximumPoint.getZ());

        List<Location> locations = new ArrayList<>();
        for (Location location : GetBlocksInArea.getLocationListBetween(minLoc, maxLoc)) {

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
            }else{
                // For the block replacement system.
                if(!structure.getStructureLimitations().getBlockReplacement().isEmpty()){
                    if(structure.getStructureLimitations().getBlockReplacement().containsKey(block.getType())){
                        block.setType(structure.getStructureLimitations().getBlockReplacement().get(block.getType()));
                        block.getState().update();
                    }
                }
            }
        }//
        return locations;
    }

    /**
     * Checks to see if a location is not already inside of a list of locations.
     *
     * @param locations The list of locations.
     * @param location The location to check
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
     * Replace the contents of a container with the loottable.
     *
     * @param lootTables The loot table
     * @param location The location of the container.
     */
    private void replaceContainerContent(RandomCollection<LootTable> lootTables, Location location) {
        if(lootTables.isEmpty()) return;
        LootTable lootTable = lootTables.next();
        Random random = new Random();

        for (int i = 0; i < lootTable.getRolls(); i++) {
            BlockState blockState = location.getBlock().getState();
            Container container = (Container) blockState;
            Inventory containerInventory = container.getInventory();
            Block block = location.getBlock();
            if (block.getType() == lootTable.getType().getMaterial() && containerInventory instanceof FurnaceInventory) {
                this.replaceFurnaceContent(lootTable, random, (FurnaceInventory) containerInventory);
            } else if (block.getType() == lootTable.getType().getMaterial() && containerInventory instanceof BrewerInventory) {
                this.replaceBrewerContent(lootTable, random, (BrewerInventory) containerInventory);
            } else if(block.getType() == lootTable.getType().getMaterial()) {
                this.replaceChestContent(lootTable, random, containerInventory);
            }
        }

    }

    /**
     * Spawn a mob with the signs.
     *
     * @param location The location of the sign.
     */
    private void replaceSignWithMob(Location location) {
        Sign sign = (Sign) location.getBlock().getState();
        String firstLine = sign.getLine(0).trim();
        String secondLine = sign.getLine(1).trim();

        if (firstLine.equalsIgnoreCase("[mob]")) {
            try {
                Entity ent = Objects.requireNonNull(location.getWorld()).spawnEntity(location, EntityType.valueOf(secondLine.toUpperCase()));
                if(ent instanceof LivingEntity){
                    LivingEntity livingEntity = (LivingEntity) ent;
                    livingEntity.setRemoveWhenFarAway(false);
                }
                location.getBlock().setType(Material.AIR);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid mob type on structure sign.");
            }
        }
        if (firstLine.equalsIgnoreCase("[mythicmob]") || firstLine.equalsIgnoreCase("[mythicalmob]")) {
            plugin.mmh.spawnMob(secondLine, location);
            location.getBlock().setType(Material.AIR);
        }

    }

    private void replaceSignWithSchematic(Location location, SubSchematics subSchematics, Structure parentStructure, int iteration) {
        Sign sign = (Sign) location.getBlock().getState();
        String firstLine = sign.getLine(0).trim();
        String secondLine = sign.getLine(1).trim();

        if (firstLine.equalsIgnoreCase("[schematic]") || firstLine.equalsIgnoreCase("[schem]")) {
            int number = -1;
            if(secondLine.startsWith("[")){
                String v = secondLine.replace("[", "").replace("]", "");
                String[] out = v.split("-");
                try {
                    int num1 = Integer.parseInt(out[0]);
                    int num2 = Integer.parseInt(out[1]);
                    number = ThreadLocalRandom.current().nextInt(num1, num2 + 1);

                }catch(NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    plugin.getLogger().warning("Invalid schematic sign on structure. Cannot parse random numbers.");
                    return;
                }
            }else{
                try{
                    number = Integer.parseInt(secondLine);
                }catch(NumberFormatException ex){
                    plugin.getLogger().warning("Invalid schematic sign on structure. Cannot parse number.");
                    return;
                }
            }
            if(number < -1 || number >= subSchematics.getSchematics().size()){
                plugin.getLogger().warning("Invalid schematic sign on structure. Schematic number is not within the valid bounds.");
                return;
            }

            SubSchematic subSchem = subSchematics.getSchematics().get(number);
            try{
                schemHandle(location, subSchem.getFile(), subSchem.isPlacingAir(), parentStructure.getLootTables(), parentStructure, iteration + 1);
            }catch (Exception ex){
                plugin.getLogger().warning("An error has occurred when attempting to paste a sub schematic.");
                if(plugin.isDebug()){
                    ex.printStackTrace();
                }
            }
        }
    }

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
}