package com.ryandw11.structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ryandw11.structure.utils.GetBlocksInArea;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.loottables.LootTablesHandler;
import com.ryandw11.structure.utils.RandomCollection;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;

public class SchematicHandeler {

	private CustomStructures plugin;
	private LootTablesHandler lootTablesHandler;

	public SchematicHandeler() {
		this.plugin = CustomStructures.plugin;
		this.lootTablesHandler = CustomStructures.lootTablesHandler;
	}

	/**
	 * Handles the schematic.
	 * 
	 * @param loc        - The location
	 * @param filename   - The file name. Ex: demo.schematic
	 * @param useAir     - if air is to be used in the schematic
	 * @param lootTables - The Loot Tables specified for this structure, if any.
	 * @param cs         - The configurationsection for this structure.
	 * @throws WorldEditException
	 */
	public void schemHandle(Location loc, String filename, boolean useAir, RandomCollection<String> lootTables, ConfigurationSection cs)
			throws IOException, WorldEditException {
		File schematicFile = new File(plugin.getDataFolder() + "/schematics/" + filename);
		// Check to see if the schematic is a thing.
		if (!schematicFile.exists()) {
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
		ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
		Clipboard clipboard;

		try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
			clipboard = reader.read();
		}
		
		ClipboardHolder ch = new ClipboardHolder(clipboard);
		AffineTransform transform = new AffineTransform();
		int rotY = 0;

		// If random rotation is enabled, rotate the clipboard
		if(cs.contains("randomRotation") && cs.getBoolean("randomRotation")) {
			rotY = new Random().nextInt(4)*90;
			transform = transform.rotateY(rotY);
			ch.setTransform(ch.getTransform().combine(transform));
		}

		// Paste the schematic
		try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
				.getEditSession(BukkitAdapter.adapt(loc.getWorld()), -1)) {
			Operation operation = ch.createPaste(editSession)
					.to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ())).ignoreAirBlocks(!useAir).build();
			Operations.complete(operation);

			if(plugin.getConfig().getBoolean("debug")){
				plugin.getLogger().info(String.format("(%s) Created an instance of %s at %s, %s, %s with rotation %s", loc.getWorld().getName(), filename, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), rotY));
			}
		}

		//Schedule the signs & containers replacement task
		int finalRotY = rotY;
		this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
			List<Location> containersAndSignsLocations = getContainersAndSignsLocations(ch.getClipboard(), loc, finalRotY);
			for (Location location : containersAndSignsLocations) {
				if (location.getBlock().getState() instanceof Container) {
					replaceContainerContent(lootTables, location);
				} else if (location.getBlock().getState() instanceof Sign) {
					replaceSignWithMob(location);
				}
			}
		});

	}

	/**
	 * Get the location of containers and signs.
	 * @param clipboard The worldedit clipboard
	 * @param pasteLocation The location of the paste
	 * @param rotation The rotate value (in degrees).
	 * @return The list of locations
	 */
	private List<Location> getContainersAndSignsLocations(Clipboard clipboard, Location pasteLocation, int rotation) {

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
			}
		}//
		return locations;
	}

	/**
	 * Checks to see if a location is not already inside of a list of locations.
	 * @param locations
	 * @param location
	 * @return
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
	 * @param lootTables 
	 * @param location
	 */
	private void replaceContainerContent(RandomCollection<String> lootTables, Location location) {
		String lootTableName = lootTables.next();
		Random random = new Random();
		LootTable lootTable = this.lootTablesHandler.getLootTableByName(lootTableName);

		for (int i = 0; i < lootTable.getRolls(); i++) {
			BlockState blockState = location.getBlock().getState();
			Container container = (Container) blockState;
			Inventory containerInventory = container.getInventory();
			if (containerInventory instanceof FurnaceInventory) {
				this.replaceFurnaceContent(lootTable, random, (FurnaceInventory) containerInventory);
			} else if (containerInventory instanceof BrewerInventory) {
				this.replaceBrewerContent(lootTable, random, (BrewerInventory) containerInventory);
			} else {
				this.replaceChestContent(lootTable, random, containerInventory);
			}
		}

	}

	/**
	 * Spawn a mob with the signs.
	 * @param location
	 */
	private void replaceSignWithMob(Location location) {
		Sign sign = (Sign) location.getBlock().getState();
		String firstLine = sign.getLine(0).trim();
		String secondLine = sign.getLine(1).trim();

		if (firstLine.equalsIgnoreCase("[mob]")) {
			try {
				location.getWorld().spawnEntity(location, EntityType.valueOf(secondLine.toUpperCase()));
				location.getBlock().setType(Material.AIR);
			} catch (IllegalArgumentException e) {
			}
		}
		if (firstLine.equalsIgnoreCase("[mythicmob]") || firstLine.equalsIgnoreCase("[mythicalmob]")) {
			plugin.mmh.spawnMob(secondLine, location);
			location.getBlock().setType(Material.AIR);
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
	 * @param point The point
	 * @param center The center
	 * @param angle The angle to rotate by.
	 * @return The final position.
	 */
	private BlockVector3 rotateAround(BlockVector3 point, BlockVector3 center, double angle){
        angle = Math.toRadians(angle * -1);
        double rotatedX = Math.cos(angle) * (point.getX() - center.getX()) - Math.sin(angle) * (point.getZ() - center.getZ()) + center.getX();
        double rotatedZ = Math.sin(angle) * (point.getX() - center.getX()) + Math.cos(angle) * (point.getZ() - center.getZ()) + center.getZ();

        return BlockVector3.at(rotatedX, point.getY(), rotatedZ);
    }
}