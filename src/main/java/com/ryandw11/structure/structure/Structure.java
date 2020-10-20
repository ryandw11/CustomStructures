package com.ryandw11.structure.structure;

import com.ryandw11.structure.SchematicHandler;
import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.structure.properties.*;
import com.ryandw11.structure.utils.RandomCollection;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class stores everything about a structure.
 * <p>Nothing in this class can be edited.</p>
 * <p>The class is organized like the structure configuration files. Everything
 * is sorted into properties.</p>
 * <p>Use {@link StructureBuilder} to create a new Structure.</p>
 */
public class Structure {
    private final String name;
    private final String schematic;
    private final int chanceNumber;
    private final int chanceOutOf;
    private final boolean isCompiled;
    private final String compiledSchematic;
    private final StructureLocation structureLocation;
    private final StructureProperties structureProperties;
    private final StructureLimitations structureLimitations;
    private final MaskProperty maskProperty;
    private final SubSchematics subSchematics;
    private final RandomCollection<LootTable> lootTables;

    /**
     * Create a structure from the {@link StructureBuilder}.
     *
     * @param builder The structure builder to use.
     */
    protected Structure(StructureBuilder builder) {
        this.name = builder.name;
        this.schematic = builder.schematic;
        this.chanceNumber = builder.chanceNumber;
        this.chanceOutOf = builder.chanceOutOf;
        this.isCompiled = builder.isCompiled;
        this.compiledSchematic = builder.compiledSchematic;
        this.structureLocation = builder.structureLocation;
        this.structureProperties = builder.structureProperties;
        this.structureLimitations = builder.structureLimitations;
        this.maskProperty = builder.maskProperty;
        this.subSchematics = builder.subSchematics;
        this.lootTables = builder.lootTables;
    }

    /**
     * Get the name of the structure.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the name of the schematic.
     * <p>The .schem is included.</p>
     *
     * @return The name of the schematic.
     */
    public String getSchematic() {
        return schematic;
    }

    /**
     * Get the chance number.
     *
     * @return The chance number.
     */
    public int getChanceNumber() {
        return chanceNumber;
    }

    /**
     * Get the chance out of number.
     *
     * @return The chance out of number.
     */
    public int getChanceOutOf() {
        return chanceOutOf;
    }

    /**
     * If the schematic is compiled.
     *
     * @return If the schematic is compiled.
     */
    public boolean isCompiled() {
        return isCompiled;
    }

    /**
     * Get the name of the compiled schematic.
     * <p>Includes the .cschem</p>
     *
     * @return The name of the compiled schematic (null if it is not compiled).
     */
    @Nullable
    public String getCompiledSchematic() {
        return compiledSchematic;
    }

    /**
     * Get the structure location properties.
     *
     * @return The structure location properties.
     */
    public StructureLocation getStructureLocation() {
        return structureLocation;
    }

    /**
     * Get the structure properties.
     *
     * @return The structure properties.
     */
    public StructureProperties getStructureProperties() {
        return structureProperties;
    }

    /**
     * Get the structure limitations.
     *
     * @return The structure limitations.
     */
    public StructureLimitations getStructureLimitations() {
        return structureLimitations;
    }

    /**
     * Get the mask properties.
     *
     * @return The mask properties.
     */
    public MaskProperty getMaskProperties() {
        return maskProperty;
    }

    /**
     * Get the sub schematics.
     *
     * @return The sub schematics.
     */
    public SubSchematics getSubSchematics() {
        return subSchematics;
    }

    /**
     * Get the collection of loot tables.
     *
     * @return The collection of loot tables.
     */
    public RandomCollection<LootTable> getLootTables() {
        return this.lootTables;
    }

    /**
     * Checks to see if the structure can spawn.
     * <p>This also checks structure locations.</p>
     *
     * @return If the structure can spawn
     */
    public boolean canSpawn(Block block, Chunk chunk) {
        // Check to see if the structure can spawn in the current world.
        if (!getStructureLocation().getWorlds().isEmpty()) {
            if (!getStructureLocation().getWorlds().contains(chunk.getWorld().getName()))
                return false;
        }

        // Check to see if the structure is far enough away from spawn.
        if(Math.abs(block.getX()) < getStructureLocation().getXLimitation())
            return false;
        if(Math.abs(block.getZ()) < getStructureLocation().getZLimitation())
            return false;

        // Check to see if the structure has the chance to spawn
        if (ThreadLocalRandom.current().nextInt(0, getChanceOutOf() + 1) > getChanceNumber())
            return false;

        // Check to see if the structure can spawn in the current biome.
        return getStructureLocation().hasBiome(block.getBiome());
    }

    /**
     * Spawn the schematic at the given location.
     *
     * @param location The location to spawn it at.
     */
    public void spawn(Location location) {
        SchematicHandler handler = new SchematicHandler();
        try {
            handler.schemHandle(location, getSchematic(), getStructureProperties().canPlaceAir(), getLootTables(), this);
        } catch (IOException | WorldEditException ex) {
            ex.printStackTrace();
        }
    }
}
