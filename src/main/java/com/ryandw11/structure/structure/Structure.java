package com.ryandw11.structure.structure;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.api.structaddon.StructureSection;
import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.loottables.LootTableType;
import com.ryandw11.structure.schematic.SchematicHandler;
import com.ryandw11.structure.structure.properties.*;
import com.ryandw11.structure.utils.RandomCollection;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a complete Structure for the plugin.
 * <p>This class is read-only and cannot be edited.</p>
 * <p>The class is organized like the structure configuration files. Everything
 * is sorted into properties.</p>
 * <p>Use {@link StructureBuilder} to create a new Structure.</p>
 * <p>Consider using the {@link StructureSection} API to add custom configuration sections to your structures.</p>
 * <p>You can detect when a structure spawns using {@link com.ryandw11.structure.api.StructureSpawnEvent}.</p>
 */
public class Structure {
    private final String name;
    private final String schematic;
    private final int probabilityNumerator;
    private final int probabilityDenominator;
    private final int priority;
    private final boolean isCompiled;
    private final String compiledSchematic;
    private final StructureLocation structureLocation;
    private final StructureProperties structureProperties;
    private final StructureLimitations structureLimitations;
    private final MaskProperty sourceMaskProperty;
    private final MaskProperty targetMaskProperty;
    private final SubSchematics subSchematics;
    private final AdvancedSubSchematics advancedSubSchematics;
    private final BottomSpaceFill bottomSpaceFill;
    private final Map<LootTableType, RandomCollection<LootTable>> lootTables;
    private final List<StructureSection> structureSections;
    private final double baseRotation;

    private double subSchemRotation = 0d;

    /**
     * Create a structure from the {@link StructureBuilder}.
     *
     * @param builder The structure builder to use.
     */
    protected Structure(StructureBuilder builder) {
        this.name = builder.name;
        this.schematic = builder.schematic;
        this.probabilityNumerator = builder.probabilityNumerator;
        this.probabilityDenominator = builder.probabilityDenominator;
        this.priority = builder.priority;
        this.isCompiled = builder.isCompiled;
        this.compiledSchematic = builder.compiledSchematic;
        this.structureLocation = builder.structureLocation;
        this.structureProperties = builder.structureProperties;
        this.structureLimitations = builder.structureLimitations;
        this.sourceMaskProperty = builder.sourceMaskProperty;
        this.targetMaskProperty = builder.targetMaskProperty;
        this.subSchematics = builder.subSchematics;
        this.advancedSubSchematics = builder.advancedSubSchematics;
        this.bottomSpaceFill = builder.bottomSpaceFill;
        this.lootTables = builder.lootTables;
        this.structureSections = builder.structureSections;
        this.baseRotation = builder.baseRotation;
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
    public int getProbabilityNumerator() {
        return probabilityNumerator;
    }

    /**
     * Get the chance out of number.
     *
     * @return The chance out of number.
     */
    public int getProbabilityDenominator() {
        return probabilityDenominator;
    }

    /**
     * Get the priority of the structure.
     * <p>The lower the number, the greater the priority for the structure to spawn when compared to others.</p>
     *
     * @return The priority.
     */
    public int getPriority() {
        return priority;
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
     * Get the source mask properties.
     *
     * @return The source mask properties.
     */
    public MaskProperty getSourceMaskProperties() {
        return sourceMaskProperty;
    }

    /**
     * Get the target mask properties.
     *
     * @return The target mask properties.
     */
    public MaskProperty getTargetMaskProperties() {
        return targetMaskProperty;
    }

    /**
     * Get the (simple) sub schematics.
     *
     * @return The sub schematics.
     */
    public SubSchematics getSubSchematics() {
        return subSchematics;
    }

    /**
     * Get the advanced schematics.
     *
     * @return The advanced schematics.
     */
    public AdvancedSubSchematics getAdvancedSubSchematics() {
        return advancedSubSchematics;
    }

    /**
     * Get the bottom space fill configuration section.
     *
     * @return The bottom space fill section.
     */
    public BottomSpaceFill getBottomSpaceFill() {
        return bottomSpaceFill;
    }

    /**
     * Get the map of loot tables containing all types.
     *
     * @return The map of loot tables containing all types.
     */
    public Map<LootTableType, RandomCollection<LootTable>> getLootTables() {
        return this.lootTables;
    }

    /**
     * Get the collection of loot tables for a certain loot table type.
     *
     * @param type The type of loot table to get.
     * @return The loot table type.
     */
    public RandomCollection<LootTable> getLootTables(LootTableType type) {
        return this.lootTables.get(type);
    }

    /**
     * Get the unmodifiable list of structure sections.
     *
     * @return The unmodifiable list of structure sections.
     */
    public List<StructureSection> getStructureSections() {
        return Collections.unmodifiableList(structureSections);
    }

    /**
     * Get the base rotation of a structure.
     *
     * @return The base rotation. (In Radians).
     */
    public double getBaseRotation() {
        return baseRotation;
    }

    /**
     * Checks to see if the structure can spawn.
     * <p>This also checks structure locations.</p>
     *
     * @param block The block (Null means it is spawning in the void.)
     * @param chunk The chunk
     * @return If the structure can spawn
     */
    public boolean canSpawn(@Nullable Block block, @NotNull Chunk chunk) {
        // Check to see if the structure can spawn in the current world.
        if (!getStructureLocation().getWorlds().isEmpty()) {
            if (!getStructureLocation().getWorlds().contains(chunk.getWorld().getName()))
                return false;
        }
        // If the block is null, that means it is in the void, check if it can spawn in the void.
        if (block == null && !getStructureProperties().canSpawnInVoid())
            return false;
        else if (block == null) {
            if (ThreadLocalRandom.current().nextInt(0, getProbabilityDenominator() + 1) > getProbabilityNumerator())
                return false;

            return getStructureLocation().hasBiome(chunk.getBlock(0, 20, 0).getBiome());
        }

        // Check to see if the structure is far enough away from spawn.
        if (Math.abs(block.getX()) < getStructureLocation().getXLimitation())
            return false;
        if (Math.abs(block.getZ()) < getStructureLocation().getZLimitation())
            return false;

        if (!CustomStructures.getInstance().getStructureHandler().validDistance(this, block.getLocation()))
            return false;

        if (!CustomStructures.getInstance().getStructureHandler().validSameDistance(this, block.getLocation()))
            return false;

        // Check to see if the structure has the chance to spawn
        if (ThreadLocalRandom.current().nextInt(0, getProbabilityDenominator() + 1) > getProbabilityNumerator())
            return false;

        // Check to see if the structure can spawn in the current biome.
        return getStructureLocation().hasBiome(block.getBiome());
    }

    /**
     * The rotation of the current sub schematic.
     * <p>This is for internal use only.</p>
     *
     * @param rot The rotation of the sub schematic in radians.
     */
    public void setSubSchemRotation(double rot) {
        this.subSchemRotation = rot;
    }

    /**
     * Get the rotation of the current sub schematic.
     * <p>For internal use only.</p>
     *
     * @return The rotation of the current sub schematic in radians.
     */
    public double getSubSchemRotation() {
        return this.subSchemRotation;
    }

    /**
     * Spawn the schematic at the given location.
     *
     * <p>This will not add the structure to the structure database.
     * Call {@link StructureHandler#putSpawnedStructure(Location, Structure)} to add it after spawning.</p>
     *
     * @param location The location to spawn it at.
     * @return If the structure was spawned successfully.
     */
    public boolean spawn(Location location) {
        try {
            SchematicHandler.placeSchematic(location, getSchematic(), getStructureProperties().canPlaceAir(), this);
            return true;
        } catch (IOException | WorldEditException ex) {
            if (CustomStructures.getInstance().isDebug()) {
                ex.printStackTrace();
            }
            return false;
        }
    }
}
