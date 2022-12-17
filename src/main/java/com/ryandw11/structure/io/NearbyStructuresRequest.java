package com.ryandw11.structure.io;

import com.ryandw11.structure.structure.Structure;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

/**
 * A request for retrieving structures near a location.
 *
 * <p>Submit this request to {@link StructureDatabaseHandler#findNearby(NearbyStructuresRequest)}.</p>
 */
public class NearbyStructuresRequest {

    private final Location location;
    private final String name;
    private final int limit;

    /**
     * Request a single structure near the specified location.
     *
     * @param location The location.
     */
    public NearbyStructuresRequest(Location location) {
        this(location, 1);
    }

    /**
     * Request a single structure of a specific type near the specified location.
     *
     * @param location      The location.
     * @param structureName The name of the desired structure.
     */
    public NearbyStructuresRequest(Location location, String structureName) {
        this(location, structureName, 1);
    }

    /**
     * Request a single structure of a specific type near the specified location.
     *
     * @param location  The location.
     * @param structure The desired structure.
     */
    public NearbyStructuresRequest(Location location, Structure structure) {
        this(location, structure.getName(), 1);
    }

    /**
     * Request structures near a specified location.
     *
     * @param location The location.
     * @param limit    The number of structures to retrieve. (Not validated.)
     */
    public NearbyStructuresRequest(Location location, int limit) {
        this(location, "", limit);
    }

    /**
     * Request structures of a certain type near a specified location.
     *
     * @param location  The location.
     * @param structure The desired structure type.
     * @param limit     The number of structures to retrieve. (Not validated.)
     */
    public NearbyStructuresRequest(Location location, Structure structure, int limit) {
        this(location, structure.getName(), limit);
    }

    /**
     * Request structures of a certain type near a specified location.
     *
     * @param location      The location.
     * @param structureName The desired structure type.
     * @param limit         The number of structures to retrieve. (Not validated.)
     */
    public NearbyStructuresRequest(Location location, String structureName, int limit) {
        this.location = location;
        this.name = structureName;
        this.limit = limit;
    }

    /**
     * Get the location of the request.
     *
     * @return The location of the request.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the name of the specified structure.
     *
     * @return The name of the specified structure. (Null if not specified).
     */
    @Nullable
    public String getName() {
        return name.isEmpty() ? null : name;
    }

    /**
     * Check if a structure name is specified.
     *
     * @return If a structure name is specified.
     */
    public boolean hasName() {
        return !name.isEmpty();
    }

    /**
     * Get the limit of the request.
     *
     * @return The limit of the request.
     */
    public int getLimit() {
        return limit;
    }
}
