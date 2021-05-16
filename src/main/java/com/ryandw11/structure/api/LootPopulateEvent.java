package com.ryandw11.structure.api;

import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.structure.Structure;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class LootPopulateEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Structure structure;
    private final Location location;
    private final LootTable lootTable;

    public LootPopulateEvent(Structure structure, Location location, LootTable lootTable){
        this.structure = structure;
        this.lootTable = lootTable;
        this.location = location;
    }

    public Structure getStructure() {
        return structure;
    }

    public LootTable getLootTable() {
        return lootTable;
    }

    public Location getLocation() {
        return location;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return handlers;
    }
}
