package com.ryandw11.structure.io;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.exceptions.RateLimitException;
import com.ryandw11.structure.exceptions.StructureDatabaseException;
import com.ryandw11.structure.exceptions.StructureNotFoundException;
import com.ryandw11.structure.io.sql.DistanceFunction;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.StructureHandler;
import com.ryandw11.structure.utils.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.sqlite.Function;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles the saving and reading of structures from the spawned structure database.
 *
 * <p>This class handles all operations asynchronously. It is also thread safe.</p>
 *
 * <p>All CompletableFutures returned are <b>not</b> guaranteed to be on the main thread.</p>
 *
 * <p>Access this class from {@link StructureHandler#getStructureDatabaseHandler()}</p>
 *
 * <p>Note: This feature needs to be enabled by the user in the config.</p>
 */
public class StructureDatabaseHandler extends BukkitRunnable {
    private final Map<Location, Structure> structuresToSave = new ConcurrentHashMap<>();
    private final List<Pair<Location, CompletableFuture<Structure>>> structuresToGet = new CopyOnWriteArrayList<>();
    private final List<Pair<Structure, CompletableFuture<List<Location>>>> locationsToGet = new CopyOnWriteArrayList<>();
    private final List<Pair<NearbyStructuresRequest, CompletableFuture<NearbyStructuresResponse>>> findNearby = new CopyOnWriteArrayList<>();

    private final Connection connection;

    private final CustomStructures plugin;

    /**
     * Construct the StructureDatabaseHandler.
     *
     * <p>For internal use only. Access through {@link StructureHandler#getStructureDatabaseHandler()}.</p>
     *
     * <p>Throws {@link StructureDatabaseException} if it cannot connect to the SQLite database successfully.</p>
     *
     * @param plugin The instance of the plugin.
     */
    public StructureDatabaseHandler(CustomStructures plugin) {
        this.plugin = plugin;

        File dataDirectory = new File(plugin.getDataFolder() + "/data/");
        if (!dataDirectory.exists())
            if (!dataDirectory.mkdir())
                throw new StructureDatabaseException("Unable to create 'data' folder. Does the plugin have the correct permissions?");

        try {
            connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", plugin.getDataFolder() + "/data/structures.db"));

            // Create custom SQLite functions.
            Function.create(connection, "DIST", new DistanceFunction());

            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS Structures (
                        id INTEGER PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        x DOUBLE NOT NULL,
                        y DOUBLE NOT NULL,
                        z DOUBLE NOT NULL,
                        world VARCHAR(300) NOT NULL
                    )
                    """);
            statement.close();


        } catch (SQLException exception) {
            if (plugin.isDebug()) {
                exception.printStackTrace();
            }
            throw new StructureDatabaseException("Unable to connect to SQLite database.");
        }
    }

    /**
     * Add a structure to the database.
     *
     * <p>Internal Use Only. To manually add a structure after spawning it through {@link Structure#spawn(Location)},
     * use {@link StructureHandler#putSpawnedStructure(Location, Structure)} instead!</p>
     *
     * @param loc       The location of the structure.
     * @param structure The structure.
     */
    public void addStructure(Location loc, Structure structure) {
        structuresToSave.put(loc, structure);
    }

    /**
     * Get a structure from the structure database using its location.
     *
     * <p>The completed future completes exceptionally with {@link StructureNotFoundException} if a structure
     * at the specified location cannot be found.</p>
     * <p>It also completes exceptionally with {@link StructureDatabaseException} if a SQL error occurs
     * when attempting to retrieve the structure.</p>
     * <p></p>
     * Usage
     * <code>
     * databaseHandler.getStructure(myLocation)<br>
     * .thenAccept(structure -&#62; {<br>
     * System.out.println(structure.getName());<br>
     * })<br>
     * .exceptionally(exception -&#62; {<br>
     * System.out.println("Structure not found or an error occurred");<br>
     * return null;<br>
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
     * Find structures closest to a certain location. Customize the request using {@link NearbyStructuresRequest}.
     *
     * <p>Warning: This operation is (somewhat) expensive on the server. Use sparingly.</p>
     *
     * <p>The completed future could be completed exceptionally. ({@link RateLimitException} or {@link StructureDatabaseException}).</p>
     *
     * @param request The nearby structures request to be made.
     * @return A completable future containing the NearbyStructuresResponse.
     */
    public CompletableFuture<NearbyStructuresResponse> findNearby(NearbyStructuresRequest request) {
        CompletableFuture<NearbyStructuresResponse> completableFuture = new CompletableFuture<>();
        if (findNearby.size() <= 5)
            findNearby.add(Pair.of(request, completableFuture));
        else
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> completableFuture.completeExceptionally(new RateLimitException("The maximum amount of requests has been hit.")),
                    5);

        return completableFuture;
    }

    /**
     * Get all the locations of a structure.
     *
     * @param structure The structure to find the locations for.
     * @return A completable future containing the list of locations.
     */
    public CompletableFuture<List<Location>> getStructureLocations(Structure structure) {
        CompletableFuture<List<Location>> completableFuture = new CompletableFuture<>();
        locationsToGet.add(Pair.of(structure, completableFuture));

        return completableFuture;
    }

    @Override
    public void run() {
        // Handle save requests.
        for (Map.Entry<Location, Structure> entry : structuresToSave.entrySet()) {
            String worldName = Objects.requireNonNull(entry.getKey().getWorld()).getName();
            try {
                PreparedStatement statement = connection.prepareStatement("INSERT INTO Structures (name, x, y, z, world) VALUES (?, ?, ?, ?, ?)");
                statement.setString(1, entry.getValue().getName());
                statement.setDouble(2, entry.getKey().getBlockX());
                statement.setDouble(3, entry.getKey().getBlockY());
                statement.setDouble(4, entry.getKey().getBlockZ());
                statement.setString(5, worldName);

                statement.executeUpdate();
                statement.close();
            } catch (SQLException exception) {
                if (plugin.isDebug()) {
                    plugin.getLogger().warning("An error was encountered when attempting to save a structure to the structure database!");
                    exception.printStackTrace();
                }
            }
        }
        structuresToSave.clear();

        // Handle structures at a specific location requests.
        for (Pair<Location, CompletableFuture<Structure>> pair : structuresToGet) {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT name FROM Structures WHERE x = ? AND y = ? AND z = ? AND world = ?");
                statement.setDouble(1, pair.getLeft().getBlockX());
                statement.setDouble(2, pair.getLeft().getBlockY());
                statement.setDouble(3, pair.getLeft().getBlockZ());
                statement.setString(4, Objects.requireNonNull(pair.getLeft().getWorld()).getName());

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    Structure structure = plugin.getStructureHandler().getStructure(resultSet.getString("name"));
                    if (structure != null) {
                        pair.getRight().complete(structure);
                    } else {
                        pair.getRight().completeExceptionally(new StructureNotFoundException("Retrieved structure is not loaded!"));
                    }
                } else {
                    pair.getRight().completeExceptionally(new StructureNotFoundException("Cannot find structure with the provided location."));
                }
            } catch (SQLException exception) {
                pair.getRight().completeExceptionally(new StructureDatabaseException("An error was encountered when attempting to retrieve a structure from the structure database!"));
                if (plugin.isDebug()) {
                    plugin.getLogger().warning("An error was encountered when attempting to retrieve a structure from the structure database!");
                    exception.printStackTrace();
                }
            }
        }
        structuresToGet.clear();

        // Handle locations of a specific structure requests.
        for (Pair<Structure, CompletableFuture<List<Location>>> pair : locationsToGet) {
            List<Location> result = new ArrayList<>();
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Structures WHERE name = ?");
                statement.setString(1, pair.getLeft().getName());

                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    result.add(new Location(
                            Bukkit.getWorld(resultSet.getString("world")),
                            resultSet.getDouble("x"),
                            resultSet.getDouble("y"),
                            resultSet.getDouble("z")
                    ));
                }

                pair.getRight().complete(result);
            } catch (SQLException exception) {
                pair.getRight().completeExceptionally(new StructureDatabaseException("An error was encountered when attempting to retrieve structures from the structure database!"));
                if (plugin.isDebug()) {
                    plugin.getLogger().warning("An error was encountered when attempting to retrieve structures from the structure database!");
                    exception.printStackTrace();
                }
            }
        }
        locationsToGet.clear();

        // Handle nearby requests.
        for (Pair<NearbyStructuresRequest, CompletableFuture<NearbyStructuresResponse>> pair : findNearby) {
            try {
                List<NearbyStructuresResponse.NearbyStructureContainer> result = new ArrayList<>();
                // So this does not clog up the IO Operations.
                NearbyStructuresRequest nearbyStructuresRequest = pair.getLeft();
                PreparedStatement statement = null;

                if (nearbyStructuresRequest.hasName()) {
                    statement = connection.prepareStatement("SELECT *, DIST(?, ?, ?, x, y, z) AS dist FROM Structures WHERE name = ? AND world = ? ORDER BY dist ASC LIMIT ?");
                    statement.setInt(1, nearbyStructuresRequest.getLocation().getBlockX());
                    statement.setInt(2, nearbyStructuresRequest.getLocation().getBlockY());
                    statement.setInt(3, nearbyStructuresRequest.getLocation().getBlockZ());
                    statement.setString(4, nearbyStructuresRequest.getName());
                    statement.setString(5, Objects.requireNonNull(nearbyStructuresRequest.getLocation().getWorld()).getName());
                    statement.setInt(6, nearbyStructuresRequest.getLimit());
                } else {
                    statement = connection.prepareStatement("SELECT *, DIST(?, ?, ?, x, y, z) AS dist FROM Structures WHERE world = ? ORDER BY dist ASC LIMIT ?");
                    statement.setInt(1, nearbyStructuresRequest.getLocation().getBlockX());
                    statement.setInt(2, nearbyStructuresRequest.getLocation().getBlockY());
                    statement.setInt(3, nearbyStructuresRequest.getLocation().getBlockZ());
                    statement.setString(4, Objects.requireNonNull(nearbyStructuresRequest.getLocation().getWorld()).getName());
                    statement.setInt(5, nearbyStructuresRequest.getLimit());
                }
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    result.add(new NearbyStructuresResponse.NearbyStructureContainer(
                            new Location(
                                    Bukkit.getWorld(resultSet.getString("world")),
                                    resultSet.getDouble("x"),
                                    resultSet.getDouble("y"),
                                    resultSet.getDouble("z")
                            ),
                            plugin.getStructureHandler().getStructure(resultSet.getString("name")),
                            resultSet.getDouble("dist")
                    ));
                }

                pair.getRight().complete(new NearbyStructuresResponse(result));
            } catch (SQLException ex) {
                pair.getRight().completeExceptionally(new StructureDatabaseException("An error was encountered when attempting to retrieve structures from the structure database!"));
                if (plugin.isDebug()) {
                    plugin.getLogger().warning("An error was encountered when attempting to retrieve structures from the structure database! (Nearby)");
                    ex.printStackTrace();
                }
            }
        }
        findNearby.clear();
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        run();
        super.cancel();
        try {
            connection.close();
        } catch (SQLException ex) {
            if (plugin.isDebug()) {
                plugin.getLogger().warning("An error was encountered when attempting to close the database connection!");
                ex.printStackTrace();
            }
        }
    }
}
