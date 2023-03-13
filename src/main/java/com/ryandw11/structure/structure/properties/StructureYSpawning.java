package com.ryandw11.structure.structure.properties;

import com.ryandw11.structure.exceptions.StructureConfigurationException;
import com.ryandw11.structure.utils.NumberStylizer;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This handles the SpawnY of a structure.
 */
public class StructureYSpawning {

    private boolean top = false;
    private boolean calculateSpawnYFirst = true;
    private final String value;
    private final HeightMap heightMap;

    /**
     * Get SpawnY from a configuration file.
     *
     * @param fc The file configuration.
     */
    public StructureYSpawning(FileConfiguration fc) {
        if (!fc.contains("StructureLocation.SpawnY") || !fc.contains("StructureLocation.SpawnYHeightMap"))
            throw new StructureConfigurationException("The structure must have a SpawnY value and SpawnY Height Map!");

        value = fc.getString("StructureLocation.SpawnY");

        try {
            heightMap = HeightMap.valueOf(Objects.requireNonNull(fc.getString("StructureLocation.SpawnYHeightMap")).toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new StructureConfigurationException("Invalid SpawnY HeightMap value! Please check your configuration!");
        }

        assert value != null;
        if (value.equalsIgnoreCase("top"))
            top = true;

        if (fc.contains("StructureLocation.CalculateSpawnFirst")) {
            calculateSpawnYFirst = fc.getBoolean("StructureLocation.CalculateSpawnFirst");
        }
    }

    /**
     * Set the StructureYSpawning with a value.
     *
     * @param value                The value of SpawnY.
     * @param heightMap            The height map for the Structure to use to spawn.
     * @param calculateSpawnYFirst If you want the SpawnY to be calculated before the other checks are completed (ex: block whitelist).
     */
    public StructureYSpawning(String value, HeightMap heightMap, boolean calculateSpawnYFirst) {
        this.value = value;
        this.heightMap = heightMap;
        if (value.equalsIgnoreCase("top"))
            top = true;
        this.calculateSpawnYFirst = calculateSpawnYFirst;
    }

    /**
     * Get the raw value of SpawnY.
     *
     * @return The raw value of SpawnY.
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the SpawnY Height Map.
     *
     * @return The SpawnY Height Map.
     */
    public HeightMap getHeightMap() {
        return heightMap;
    }

    /**
     * Get if the structure should spawn on the top.
     *
     * <p>As of 1.6.0, This will be true when spawning on the ocean floor too.</p>
     *
     * @return If the structure should spawn on the top.
     */
    public boolean isTop() {
        return top;
    }

    /**
     * Get if the HeightMap is set to the Ocean Floor.
     *
     * <p>{@link #isTop()} will also be true if set to spawn directly on the ocean floor.</p>
     *
     * @return If the structure picks a Y value from the Ocean Floor.
     */
    public boolean isOceanFloor() {
        return heightMap == HeightMap.OCEAN_FLOOR;
    }

    /**
     * Get if the structure should calculate SpawnY first.
     *
     * @return If the structure should calculate SpawnY first.
     */
    public boolean isCalculateSpawnYFirst() {
        return calculateSpawnYFirst;
    }

    /**
     * Get the highest block at a location according the structure rules.
     *
     * @param loc The initial location (Y does not matter).
     * @return The Highest block according to the structure rules for Height Maps.
     */
    public Block getHighestBlock(Location loc) {
        return Objects.requireNonNull(loc.getWorld()).getHighestBlockAt(loc, heightMap);
    }

    /**
     * Get the height from SpawnY value.
     *
     * @param location The location of the block for the height calculation (This is usually the location of the top block).
     *                 <p>AKA: What Y value top should return.</p>
     *                 <p>If Null is passed in that means it will spawn in the void.</p>
     * @return The height according to the rules of SpawnY.
     */
    public int getHeight(@Nullable Location location) {
        return NumberStylizer.getStylizedSpawnY(value, location);
    }
}
