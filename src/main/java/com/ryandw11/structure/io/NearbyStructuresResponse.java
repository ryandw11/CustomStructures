package com.ryandw11.structure.io;

import com.ryandw11.structure.structure.Structure;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The response of the nearby request.
 *
 * <p>Returned in the CompletableFuture provided by {@link StructureDatabaseHandler#findNearby(NearbyStructuresRequest)}. </p>
 */
public class NearbyStructuresResponse {
    private final List<NearbyStructureContainer> response;

    /**
     * Construct a nearby structure response.
     * <p>For internal use only.</p>
     *
     * @param response The list of nearby structures.
     */
    public NearbyStructuresResponse(@NotNull List<NearbyStructureContainer> response) {
        this.response = response;
    }

    /**
     * Get the list of nearby structures.
     *
     * @return The list of nearby structures.
     */
    public List<NearbyStructureContainer> getResponse() {
        return response;
    }

    /**
     * Get the closest nearby structure.
     *
     * <p>Use {@link #hasEntries()} to check if there are any structures first.</p>
     *
     * @return The closest nearby structure. (Null if no structures are found).
     */
    @Nullable
    public NearbyStructureContainer getFirst() {
        if (response.isEmpty())
            return null;

        return response.get(0);
    }

    /**
     * Check if any nearby structures were found.
     *
     * @return If any nearby structures were found.
     */
    public boolean hasEntries() {
        return !response.isEmpty();
    }

    /**
     * The container to contain the nearby structure results.
     */
    public static class NearbyStructureContainer {
        private final Location location;
        private final Structure structure;
        private final double distance;

        /**
         * Construct a nearby structure.
         *
         * @param location  The location of the structure found.
         * @param structure The structure found.
         * @param distance  The distance from the requested location and the structure found.
         */
        public NearbyStructureContainer(@NotNull Location location, @NotNull Structure structure, double distance) {
            this.location = location;
            this.structure = structure;
            this.distance = distance;
        }

        /**
         * Get the location of the structure found.
         *
         * @return The location of the structure found.
         */
        public Location getLocation() {
            return location;
        }

        /**
         * Get the structure found.
         *
         * @return The structure found.
         */
        public Structure getStructure() {
            return structure;
        }

        /**
         * Get the distance from the requested location and the structure found.
         *
         * @return The distance from the requested location and the structure found.
         */
        public double getDistance() {
            return distance;
        }
    }
}
