package com.ryandw11.structure.structure.properties.schematics;

import com.ryandw11.structure.utils.NumberStylizer;
import com.ryandw11.structure.utils.Pair;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

/**
 * The Vertical Repositioning settings of a Sub-Schematic.
 */
public class VerticalRepositioning {
    private final String range;
    private final String spawnY;
    private final HeightMap spawnYHeightMap;
    private final String noPointSolution;

    /**
     * Construct the settings from a configuration file.
     *
     * @param sectionName The name of the section.
     * @param section     The configuration section.
     */
    public VerticalRepositioning(String sectionName, ConfigurationSection section) {
        if (section.contains("Range"))
            range = section.getString("Range");
        else
            range = "";

        if (section.contains("SpawnY"))
            spawnY = section.getString("SpawnY");
        else
            throw new RuntimeException(String.format("Unable to find SpawnY section for sub-schematic %s!", sectionName));

        if (section.contains("SpawnYHeightMap"))
            spawnYHeightMap = HeightMap.valueOf(section.getString("SpawnYHeightMap").toUpperCase());
        else
            spawnYHeightMap = HeightMap.WORLD_SURFACE;

        if (section.contains("NoPointSolution"))
            noPointSolution = section.getString("NoPointSolution");
        else
            noPointSolution = "CURRENT";
    }

    /**
     * Create the vertical repositioning settings.
     *
     * @param range           The range that the schematic can spawn in. (ex: [-10; 20]).
     * @param spawnY          The SpawnY setting for the vertical repositioning.
     * @param heightMap       The height map to pick the top block from.
     * @param noPointSolution What to do when the SpawnY selected is out of the range. (CURRENT, PREVENT_SPAWN, Stylized SpawnY Int (without top)).
     */
    public VerticalRepositioning(String range, String spawnY, HeightMap heightMap, String noPointSolution) {
        this.range = range;
        this.spawnY = spawnY;
        this.spawnYHeightMap = heightMap;
        this.noPointSolution = noPointSolution;
    }

    /**
     * Get the range.
     *
     * @return The valid spawning range. (Null if none).
     */
    @Nullable
    public Pair<Integer, Integer> getRange() {
        if(range.isEmpty()) {
            return null;
        }
        return NumberStylizer.parseRangedInput(range);
    }

    /**
     * Get the raw SpawnY value.
     *
     * @return The raw SpawnY Value.
     */
    public String getRawSpawnY() {
        return spawnY;
    }

    /**
     * Get the processed SpawnY value.
     *
     * @param location The location of the top block (if there is one).
     * @return The location.
     */
    public int getSpawnY(@Nullable Location location) {
        return NumberStylizer.getStylizedSpawnY(spawnY, location);
    }

    /**
     * Get the Height Map to use.
     *
     * @return The height map to use.
     */
    public HeightMap getSpawnYHeightMap() {
        return spawnYHeightMap;
    }

    /**
     * Get the no-point solution.
     *
     * @return The no point solution.
     */
    public String getNoPointSolution() {
        return noPointSolution;
    }
}
