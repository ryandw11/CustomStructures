package com.ryandw11.structure.api.holder;

import com.ryandw11.structure.api.StructureSpawnEvent;
import org.bukkit.Location;

import java.util.List;

/**
 * Holds specific information about the spawning of a structure for the event.
 * <p>
 * This is meant to be used internally and should not be accessed by other plugins.
 * <p>
 * Get the same information from the {@link com.ryandw11.structure.api.StructureSpawnEvent}.
 */
public class StructureSpawnHolder {
    private final Location minimumPoint;
    private final Location maximumPoint;
    private final List<Location> containersAndSignsLocations;

    public StructureSpawnHolder(Location minimumPoint, Location maximumPoint, List<Location> containersAndSignsLocations) {
        this.maximumPoint = maximumPoint;
        this.minimumPoint = minimumPoint;
        this.containersAndSignsLocations = containersAndSignsLocations;
    }

    /**
     * The minimum point for a structure.
     * <p>See {@link StructureSpawnEvent#getMinimumPoint()} for additional details and usage.</p>
     *
     * @return The minimum point.
     */
    public Location getMinimumPoint() {
        return minimumPoint;
    }

    /**
     * The maximum point for a structure.
     * <p>See {@link StructureSpawnEvent#getMaximumPoint()} for additional details and usage.</p>
     *
     * @return The maximum point.
     */
    public Location getMaximumPoint() {
        return maximumPoint;
    }

    /**
     * Get the locations for containers and signs.
     * <p>See {@link StructureSpawnEvent#getContainersAndSignsLocations()}.</p>
     *
     * @return The locations for containers and signs.
     */
    public List<Location> getContainersAndSignsLocations() {
        return containersAndSignsLocations;
    }

}
