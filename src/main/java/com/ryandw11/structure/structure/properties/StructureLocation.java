package com.ryandw11.structure.structure.properties;

import com.ryandw11.structure.exceptions.StructureConfigurationException;
import com.ryandw11.structure.structure.StructureBuilder;
import org.bukkit.HeightMap;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * The configuration section for the Structure Location.
 */
public class StructureLocation implements StructureProperty {

    private List<String> worlds;
    private StructureYSpawning spawnY;
    private List<String> biomes;
    private double distanceFromOthers;
    private int xLimitation;
    private int zLimitation;

    /**
     * Create the Structure Location configuration section.
     *
     * @param sb            The Structure Builder.
     * @param configuration The File Configuration.
     * @deprecated Use {@link StructureLocation#StructureLocation(FileConfiguration)} instead.
     */
    @Deprecated
    public StructureLocation(StructureBuilder sb, FileConfiguration configuration) {
        this(configuration);
    }

    /**
     * Create the Structure Location configuration section.
     *
     * @param fileConfiguration The file configuration to grab the section from.
     */
    public StructureLocation(FileConfiguration fileConfiguration) {
        ConfigurationSection cs = fileConfiguration.getConfigurationSection("StructureLocation");
        if (cs == null)
            throw new StructureConfigurationException("The `StructureLocation` property is mandatory, please add one to the file for the " +
                    "structure to be valid.");
        if (cs.contains("Worlds"))
            this.worlds = cs.getStringList("Worlds");
        else
            this.worlds = new ArrayList<>();
        this.spawnY = new StructureYSpawning(fileConfiguration);
        if (cs.contains("Biome"))
            this.biomes = cs.getStringList("Biome");
        else
            this.biomes = new ArrayList<>();

        if (cs.contains("DistanceFromOthers"))
            this.distanceFromOthers = Math.max(0, cs.getDouble("DistanceFromOthers"));
        else
            this.distanceFromOthers = 100;

        xLimitation = 0;
        zLimitation = 0;
        if (cs.contains("spawn_distance")) {
            if (cs.contains("spawn_distance.x")) {
                xLimitation = cs.getInt("spawn_distance.x");
            }
            if (cs.contains("spawn_distance.z")) {
                zLimitation = cs.getInt("spawn_distance.z");
            }
        }
    }

    /**
     * Construct the StructureLocation properties without a config.
     *
     * <p>Other values can be edited using setter methods.</p>
     *
     * @param worlds        The worlds.
     * @param spawnSettings The y setting.
     * @param biomes        The list of biomes.
     */
    public StructureLocation(List<String> worlds, StructureYSpawning spawnSettings, List<String> biomes) {
        this.worlds = worlds;
        this.spawnY = spawnSettings;
        this.biomes = biomes;
        this.distanceFromOthers = 100;
        this.xLimitation = 0;
        this.zLimitation = 0;
    }

    /**
     * Construct the Structure Location using default values.
     */
    public StructureLocation() {
        this(new ArrayList<>(), new StructureYSpawning("top", HeightMap.WORLD_SURFACE, true), new ArrayList<>());
    }

    /**
     * Get the list of worlds the structure can spawn in.
     *
     * @return The list of worlds.
     */
    public List<String> getWorlds() {
        return worlds;
    }

    /**
     * Get the Y Spawn settings.
     *
     * @return The Y Spawn settings.
     */
    public StructureYSpawning getSpawnSettings() {
        return spawnY;
    }

    /**
     * Get the list of biomes.
     *
     * @return The list of biomes.
     */
    public List<String> getBiomes() {
        return biomes;
    }

    /**
     * Set the list of worlds.
     *
     * @param worlds The list of worlds to set.
     */
    public void setWorlds(List<String> worlds) {
        this.worlds = worlds;
    }

    /**
     * Set the Spawn Y Settings.
     *
     * @param spawnY The spawn y settings.
     */
    public void setSpawnSettings(StructureYSpawning spawnY) {
        this.spawnY = spawnY;
    }

    /**
     * Set the list of biomes.
     * <p>An empty list is assumed to mean all biomes.</p>
     *
     * @param biomes The list of biomes to set.
     */
    public void setBiomes(List<String> biomes) {
        this.biomes = biomes;
    }

    /**
     * Check if the biomes list contains a biome.
     * <p>An empty list is assumed to mean all biomes.</p>
     *
     * @param b The biome to check for.
     * @return If the biome is in the list (or if the list is empty).
     */
    public boolean hasBiome(Biome b) {
        if (biomes.isEmpty())
            return true;
        for (String biome : biomes) {
            if (b.toString().equalsIgnoreCase(biome.replace("minecraft:", "")))
                return true;
        }
        return false;
    }

    /**
     * Set the X-axis limitation.
     *
     * @param x The X-Axis limitation.
     */
    public void setXLimitation(int x) {
        this.xLimitation = x;
    }

    /**
     * Get the X-Axis limitation.
     *
     * @return The X-Axis limitation.
     */
    public int getXLimitation() {
        return this.xLimitation;
    }

    /**
     * Set the Z-Axis limitation.
     *
     * @param z The Z-Axis limitation.
     */
    public void setZLimitation(int z) {
        this.zLimitation = z;
    }

    /**
     * Get the Z-Axis limitation.
     *
     * @return The Z-Axis limitation.
     */
    public int getZLimitation() {
        return this.zLimitation;
    }

    /**
     * Get the distance from others value.
     *
     * @return The distance from others value.
     */
    public double getDistanceFromOthers() {
        return distanceFromOthers;
    }

    /**
     * Set the distance from others value.
     *
     * @param distance The distance desired (Must be positive).
     */
    public void setDistanceFromOthers(double distance) {
        if (distance < 0)
            throw new IllegalArgumentException("Distance must be greater than 0!");
        this.distanceFromOthers = distance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveToFile(ConfigurationSection configurationSection) {
        configurationSection.set("Worlds", worlds);
        configurationSection.set("SpawnY", spawnY.getValue());
        configurationSection.set("SpawnYHeightMap", spawnY.getHeightMap().toString());
        configurationSection.set("Biome", biomes);
        configurationSection.set("DistanceFromOthers", distanceFromOthers);
        if (xLimitation > 0)
            configurationSection.set("spawn_distance.x", xLimitation);
        if (zLimitation > 0)
            configurationSection.set("spawn_distance.z", zLimitation);
    }
}
