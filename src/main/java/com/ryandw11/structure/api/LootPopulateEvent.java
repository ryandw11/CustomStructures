package com.ryandw11.structure.api;

import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.structure.Structure;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called just before a container is populated with a loot table.
 */
public class LootPopulateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Structure structure;
    private final Location location;
    private final LootTable lootTable;
    private boolean canceled;

    /**
     * Construct the loot populate event.
     *
     * @param structure The structure.
     * @param location  The location.
     * @param lootTable The loot table.
     */
    public LootPopulateEvent(Structure structure, Location location, LootTable lootTable) {
        this.structure = structure;
        this.lootTable = lootTable;
        this.location = location;
        this.canceled = false;
    }

    /**
     * Get the structure that spawned.
     *
     * @return The structure that spawned.
     */
    public Structure getStructure() {
        return structure;
    }

    /**
     * Get the selected loot table for the container.
     *
     * @return The selected loot table for the container.
     */
    public LootTable getLootTable() {
        return lootTable;
    }

    /**
     * Get the location of the container.
     *
     * @return The location of the container.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Set if the event is canceled.
     *
     * @param canceled If the event is canceled.
     */
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    /**
     * Get if the event is canceled.
     *
     * @return if the event is canceled.
     */
    public boolean isCanceled() {
        return canceled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
