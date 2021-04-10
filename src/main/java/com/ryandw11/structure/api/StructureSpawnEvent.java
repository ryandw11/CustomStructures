package com.ryandw11.structure.api;

import com.ryandw11.structure.api.holder.StructureSpawnHolder;
import com.ryandw11.structure.structure.Structure;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This event is called every time a structure spawns. Use this event like any other Bukkit event.
 *
 * <p>This is called after the plugin does all of its primary operations, such as block replacement, loot tables, etc.</p>
 *
 * <p>It is not possible to cancel this event.</p>
 */
public class StructureSpawnEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Structure structure;
    private final Location location;
    private final double rotation;
    private final StructureSpawnHolder holder;

    /**
     * Construct a new spawn event.
     *
     * @param structure The structure.
     * @param location  The location of the structure.
     * @param rotation  The rotation (in degrees).
     * @param holder    The holder for structure data.
     */
    public StructureSpawnEvent(Structure structure, Location location, double rotation, StructureSpawnHolder holder) {
        this.structure = structure;
        this.location = location;
        this.rotation = rotation;
        this.holder = holder;
    }

    /**
     * Get the structure that was spawned.
     *
     * @return The structure that was spawned.
     */
    public Structure getStructure() {
        return structure;
    }

    /**
     * Get the location where the structure spawned.
     * <p>This is where the origin point of the schematic spawns.</p>
     *
     * @return The location where the structure spawned.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get the rotation of the structure. (In Degrees)
     *
     * @return The rotation of the structure (in degrees).
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * Get the minimum point of the structure schematic.
     * <p>The rotation transformation is already applied.</p>
     * <p>Note: This is not guaranteed to be the minimum point if the structure is rotated.
     * Use Math.min() in order to determine the true minimum x, y, and z values.</p>
     *
     * @return The minimum point of the structure schematic.
     */
    public Location getMinimumPoint() {
        return holder.getMinimumPoint();
    }

    /**
     * Get the maximum point of the structure schematic.
     * <p>The rotation transformation is already applied.</p>
     * <p>Note: This is not guaranteed to be the maximum point if the structure is rotated.
     * Use Math.min() in order to determine the true maximum x, y, and z values.</p>
     *
     * @return The maximum point of the structure schematic.
     */
    public Location getMaximumPoint() {
        return holder.getMaximumPoint();
    }

    /**
     * Get a list of locations for all signs and containers in the structure.
     * <p>Note: Containers may already have a loot table defined.</p>
     * <p>Note 2: Not all locations are guaranteed to be either a sign or a container since a sign may have been
     * removed by the plugin in order to spawn a mob or schematic.</p>
     * <p>Note 3: All locations already have the rotation transformation applied.</p>
     *
     * @return The list of locations for all signs and containers in the structure.
     */
    public List<Location> getContainersAndSignsLocations() {
        return holder.getContainersAndSignsLocations();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
