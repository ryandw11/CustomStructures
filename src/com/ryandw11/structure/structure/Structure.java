package com.ryandw11.structure.structure;

import com.ryandw11.structure.SchematicHandeler;
import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.structure.properties.StructureLimitations;
import com.ryandw11.structure.structure.properties.StructureLocation;
import com.ryandw11.structure.structure.properties.StructureProperties;
import com.ryandw11.structure.utils.RandomCollection;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The main class when it comes to structures.
 */
public class Structure {
    private final String name;
    private final String schematic;
    private final int chanceNumber;
    private final int chanceOutOf;
    private final StructureLocation structureLocation;
    private final StructureProperties structureProperties;
    private final StructureLimitations structureLimitations;
    private final RandomCollection<LootTable> lootTables;

    protected Structure( StructureBuilder builder){
        this.name = builder.name;
        this.schematic = builder.schematic;
        this.chanceNumber = builder.chanceNumber;
        this.chanceOutOf = builder.chanceOutOf;
        this.structureLocation = builder.structureLocation;
        this.structureProperties = builder.structureProperties;
        this.structureLimitations = builder.structureLimitations;
        this.lootTables = builder.lootTables;
    }

    public String getName(){
        return name;
    }

    public String getSchematic(){
        return schematic;
    }

    public int getChanceNumber(){
        return chanceNumber;
    }

    public int getChanceOutOf(){
        return chanceOutOf;
    }

    public StructureLocation getStructureLocation(){
        return structureLocation;
    }

    public StructureProperties getStructureProperties(){
        return structureProperties;
    }

    public StructureLimitations getStructureLimitations() {
        return structureLimitations;
    }

    public RandomCollection<LootTable> getLootTables(){
        return this.lootTables;
    }

    /**
     * Checks to see if the structure can spawn.
     * <p>This also checks structure locations.</p>
     * @return If the structure can spawn
     */
    public boolean canSpawn(Block block, Chunk chunk){
        // Check to see if the structure can spawn in the current world.
        if(!getStructureLocation().getWorlds().isEmpty()){
            if(!getStructureLocation().getWorlds().contains(chunk.getWorld().getName()))
                return false;
        }

        // Check to see if the structure has the chance to spawn
        if( ThreadLocalRandom.current().nextInt(getChanceNumber(), getChanceOutOf()) != getChanceNumber())
            return false;

        // Check to see if the structure can spawn in the current biome.
        return getStructureLocation().hasBiome(block.getBiome());

        // TODO add in support for block conditions
    }

    /**
     * Spawn the schematic at the given location.
     * @param location The location to spawn it at.
     */
    public void spawn(Location location){
        SchematicHandeler handler = new SchematicHandeler();
        try {
            handler.schemHandle(location, getSchematic(), getStructureProperties().canPlaceAir(), getLootTables(), this);
        }catch(IOException | WorldEditException ex){
            ex.printStackTrace();
        }
    }
}
