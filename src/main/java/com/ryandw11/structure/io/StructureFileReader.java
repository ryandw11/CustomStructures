package com.ryandw11.structure.io;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.exceptions.RateLimitException;
import com.ryandw11.structure.exceptions.StructureNotFoundException;
import com.ryandw11.structure.exceptions.StructureReadWriteException;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.StructureHandler;
import com.ryandw11.structure.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This handles the Async IO System for saving and loading structures from the logs.
 *
 * <p>If you want to find pasted structures you can using the class. Get it from
 * {@link StructureHandler#getSpawnedStructures()}.</p>
 *
 * <p>Note: This feature needs to be enabled by the user in the config.</p>
 */
public class StructureFileReader extends BukkitRunnable {
    private final Map<Location, Structure> structuresToSave = new ConcurrentHashMap<>();
    private final List<Pair<Location, CompletableFuture<Structure>>> structuresToGet = new CopyOnWriteArrayList<>();
    private final List<Pair<Structure, CompletableFuture<List<Location>>>> locationsToGet = new CopyOnWriteArrayList<>();
    private final List<Pair<Location, CompletableFuture<Pair<Structure, Location>>>> findNearby = new CopyOnWriteArrayList<>();

    private final File structureFile;
    private final FileConfiguration fileConfiguration;

    private final CustomStructures plugin;

    /**
     * Construct the StructureFileReader.
     *
     * <p>Throws {@link StructureReadWriteException} if it cannot access the needed files.</p>
     *
     * @param plugin The plugin.
     */
    public StructureFileReader(CustomStructures plugin) {
        this.plugin = plugin;

        structureFile = new File(plugin.getDataFolder() + "/data/structures.yml");
        if (!structureFile.exists()) {
            try {
                File directory = new File(plugin.getDataFolder() + "/data");
                boolean result = true;
                if (!directory.exists())
                    result = directory.mkdir();
                result &= structureFile.createNewFile();
                if (!result) {
                    throw new StructureReadWriteException("Critical Error: Unable to create log files.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(structureFile);
    }

    /**
     * Add a structure to the storage file.
     *
     * @param loc       The location of the structure.
     * @param structure The structure.
     */
    public void addStructure(Location loc, Structure structure) {
        structuresToSave.put(loc, structure);
    }

    /**
     * Get a structure from the storage file.
     *
     * <p>This method is async and will complete at a later time.</p>
     *
     * <p>The completed future completes exceptionally with {@link StructureNotFoundException} if a structure
     * at the specified location cannot be found.</p>
     *
     * <h2>Usage</h2>
     * <code>
     * fileReader.getStructure(myLocation)<br>
     * .thenAccept(structure -&#62; {<br>
     * if(structure != null)<br>
     * System.out.println(structure.getName());<br>
     * else<br>
     * System.out.println("Cannot find a structure at that location!");<br>
     * });<br>
     * </code>
     *
     * @param location The location to get.
     * @return The completable future that this will return.
     */
    public CompletableFuture<Structure> getStructure(Location location) {
        CompletableFuture<Structure> completableFuture = new CompletableFuture<>();
        structuresToGet.add(Pair.of(location, completableFuture));

        return completableFuture;
    }

    /**
     * Find a structure nearby a location.
     *
     * <p>Warning: This operation is expensive on the server. Use lightly.</p>
     *
     * <p>The completed future could be null. Null means a structure was not found.</p>
     *
     * @param location The location.
     * @return A completable future containing a pair with the structure and location.
     */
    public CompletableFuture<Pair<Structure, Location>> findNearby(Location location) {
        CompletableFuture<Pair<Structure, Location>> completableFuture = new CompletableFuture<>();
        if (findNearby.size() <= 5)
            findNearby.add(Pair.of(location, completableFuture));
        else
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> completableFuture.completeExceptionally(new RateLimitException("The maximum amount of requests has been hit.")),
                    5);

        return completableFuture;
    }

    /**
     * Get the locations of a structure.
     *
     * @param structure The structure.
     * @return A completable future containing the list of locations.
     */
    public CompletableFuture<List<Location>> getStructureLocations(Structure structure) {
        CompletableFuture<List<Location>> completableFuture = new CompletableFuture<>();
        locationsToGet.add(Pair.of(structure, completableFuture));

        return completableFuture;
    }

    @Override
    public void run() {
        for (Map.Entry<Location, Structure> entry : structuresToSave.entrySet()) {
            String worldName = Objects.requireNonNull(entry.getKey().getWorld()).getName();
            if (!fileConfiguration.contains(entry.getValue().getName() + "." + worldName))
                // Split up the list of locations into Structures, then worlds.
                fileConfiguration.set(entry.getValue().getName() +
                        "." + worldName, new ArrayList<>());
            List<String> locs = fileConfiguration.getStringList(entry.getValue().getName() + "." + worldName);
            locs.add(serializeLocation(entry.getKey()));
            fileConfiguration.set(entry.getValue().getName() + "." + worldName, locs);
        }
        structuresToSave.clear();

        for (Pair<Location, CompletableFuture<Structure>> pair : structuresToGet) {
            boolean found = false;
            for (String key : fileConfiguration.getKeys(false)) {
                List<String> locs = fileConfiguration.getStringList(key + "." +
                        Objects.requireNonNull(pair.getLeft().getWorld()).getName());
                if (locs.contains(serializeLocation(pair.getLeft()))) {
                    pair.getRight().complete(plugin.getStructureHandler().getStructure(key));
                    found = true;
                    break;
                }
            }
            if (!found)
                pair.getRight().completeExceptionally(new StructureNotFoundException("Cannot find structure with the provided location."));

        }
        structuresToGet.clear();

        for (Pair<Structure, CompletableFuture<List<Location>>> pair : locationsToGet) {
            if (fileConfiguration.contains(pair.getLeft().getName())) {
                List<Location> result = new ArrayList<>();
                for (String world : Objects.requireNonNull(fileConfiguration.getConfigurationSection(pair.getLeft().getName())).getKeys(false)) {
                    List<String> locations = fileConfiguration.getStringList(pair.getLeft().getName() + "." + world);
                    for (String strLoc : locations) {
                        Location loc = deserializeLocation(strLoc);
                        if (loc == null)
                            continue;
                        result.add(loc);
                    }
                }
                pair.getRight().complete(result);
            } else {
                pair.getRight().completeExceptionally(new StructureNotFoundException("Cannot find desired structure.."));
            }
        }
        locationsToGet.clear();

        for (Pair<Location, CompletableFuture<Pair<Structure, Location>>> pair : findNearby) {
            // So this does not clog up the IO Operations.
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Pair<Structure, Location> closest = null;
                for (String key : fileConfiguration.getKeys(false)) {
                    List<String> locs = fileConfiguration.getStringList(key + "." +
                            Objects.requireNonNull(pair.getLeft().getWorld()).getName());
                    for (String s : locs) {
                        Location loc = deserializeLocation(s);
                        if (loc == null) continue;
                        if (closest == null) {
                            closest = Pair.of(plugin.getStructureHandler().getStructure(key), loc);
                            continue;
                        }
                        if (loc.distance(pair.getLeft()) < closest.getRight().distance(pair.getLeft()))
                            closest = Pair.of(plugin.getStructureHandler().getStructure(key), loc);
                    }
                }

                pair.getRight().complete(closest);
            });
        }
        findNearby.clear();

        try {
            fileConfiguration.save(structureFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Unable to save log file!");
            super.cancel();
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        run();
        super.cancel();
    }

    private String serializeLocation(Location location) {
        return String.format("%s;%s;%s;%s", Objects.requireNonNull(location.getWorld()).getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    private Location deserializeLocation(String location) {
        String[] data = location.split(";");
        if (data.length < 4)
            return null;
        World w = plugin.getServer().getWorld(data[0]);
        int x = Integer.parseInt(data[1]);
        int y = Integer.parseInt(data[2]);
        int z = Integer.parseInt(data[3]);

        return new Location(w, x, y, z);
    }
}
