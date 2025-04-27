package com.ryandw11.structure.structure.properties;

import com.ryandw11.structure.exceptions.StructureConfigurationException;
import com.ryandw11.structure.structure.StructureBuilder;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The configuration section for the Structure Location.
 */
public class StructureLocation implements StructureProperty {

    private List<String> worlds;
    private List<String> worldBlacklist;
    private StructureYSpawning spawnY;
    private List<String> biomes;
    private double distanceFromOthers;
    private double distanceFromSame;
    private boolean inner;
    private int xLimitation;
    private int zLimitation;
    private SpawnRegion spawnRegion;

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

        if (cs.contains("WorldBlacklist"))
            this.worldBlacklist = cs.getStringList("WorldBlacklist");
        else
            this.worldBlacklist = new ArrayList<>();

        this.spawnY = new StructureYSpawning(fileConfiguration);
        if (cs.contains("Biome"))
            this.biomes = cs.getStringList("Biome");
        else
            this.biomes = new ArrayList<>();

        if (cs.contains("DistanceFromOthers"))
            this.distanceFromOthers = Math.max(0, cs.getDouble("DistanceFromOthers"));
        else
            this.distanceFromOthers = 100;

        if (cs.contains("DistanceFromSame"))
            this.distanceFromSame = Math.max(0, cs.getDouble("DistanceFromSame"));
        else
            this.distanceFromSame = 100;

        xLimitation = 0;
        zLimitation = 0;
        inner = false;
        if (cs.contains("SpawnDistance.inner")) {
            inner = cs.getBoolean("SpawnDistance.inner");
        }
        if (cs.contains("SpawnDistance.x")) {
            xLimitation = cs.getInt("SpawnDistance.x");
        }
        if (cs.contains("SpawnDistance.z")) {
            zLimitation = cs.getInt("SpawnDistance.z");
        }
        if (cs.contains("SpawnRegion")) {
            Location cornerOne = new Location(null,
                    cs.getDouble("SpawnRegion.CornerOne.x", 0),
                    cs.getDouble("SpawnRegion.CornerOne.y", 0),
                    cs.getDouble("SpawnRegion.CornerOne.z", 0));
            Location cornerTwo = new Location(null,
                    cs.getDouble("SpawnRegion.CornerTwo.x", 0),
                    cs.getDouble("SpawnRegion.CornerTwo.y", 0),
                    cs.getDouble("SpawnRegion.CornerTwo.z", 0));
            SpawnRegion spRegion = new SpawnRegion(cornerOne, cornerTwo);
            spawnRegion = spRegion.standardize();
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
        this.worldBlacklist = new ArrayList<>();
        this.spawnY = spawnSettings;
        this.biomes = biomes;
        this.distanceFromOthers = 100;
        this.distanceFromSame = 100;
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
     * Get the list of blacklisted worlds.
     *
     * @return The list of blacklisted worlds.
     */
    public List<String> getWorldBlacklist() {
        return worldBlacklist;
    }

    /**
     * Check if a structure can spawn in the provided world.
     *
     * @param world The world to check.
     * @return If the structure can spawn in that world.
     */
    public boolean canSpawnInWorld(@NotNull World world) {
        if (!worlds.isEmpty()) {
            if (!worlds.contains(world.getName()))
                return false;
        }

        if (worldBlacklist.contains(world.getName())) {
            return false;
        }

        return true;
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
     * If the spawn distance limitation is "inner".
     *
     * @return If the spawn distance limitation is in "inner" mode.
     * @apiNote "Inner" means that the structure will only spawn inside the box defined by the spawn distance.
     */
    public boolean isInner() {
        return inner;
    }

    /**
     * Set the spawn distance limitation in "inner" mode.
     *
     * @param inner If the spawn distance limitation should be in "inner" mode.
     * @apiNote "Inner" means that the structure will only spawn inside the box defined by the spawn distance.
     */
    public void setInner(boolean inner) {
        this.inner = inner;
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
     * Get the minimum distance from other structures.
     *
     * @return The minimum distance from other structures.
     */
    public double getDistanceFromOthers() {
        return distanceFromOthers;
    }

    /**
     * Set the minimum distance from other structures.
     *
     * @param distance The distance desired (Must be positive).
     */
    public void setDistanceFromOthers(double distance) {
        if (distance < 0)
            throw new IllegalArgumentException("Distance must be greater than 0!");
        this.distanceFromOthers = distance;
    }

    /**
     * Get the minimum distance from other structures of the same time.
     *
     * @return Minimum distance from structures.
     */
    public double getDistanceFromSame() {
        return distanceFromSame;
    }

    /**
     * Set the distance requirement for the same structures.
     *
     * @param distance Minimum distance from other structures of the same type.
     */
    public void setDistanceFromSame(double distance) {
        if (distance < 0)
            throw new IllegalArgumentException("Distance must be greater than 0!");
        this.distanceFromSame = distance;
    }

    /**
     * Get the spawn region.
     *
     * @return The spawn region.
     */
    @Nullable
    public SpawnRegion getSpawnRegion() {
        return spawnRegion;
    }

    /**
     * Set the spawn region limitation.
     * <p>The spawn region determine the region in which a structure can spawn.</p>
     *
     * @param spawnRegion The spawn region limitation.
     */
    public void setSpawnRegion(@Nullable SpawnRegion spawnRegion) {
        this.spawnRegion = spawnRegion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveToFile(ConfigurationSection configurationSection) {
        configurationSection.set("Worlds", worlds);
        configurationSection.set("WorldBlacklist", worldBlacklist);
        configurationSection.set("SpawnY", spawnY.getValue());
        configurationSection.set("SpawnYHeightMap", spawnY.getHeightMap().toString());
        configurationSection.set("Biome", biomes);
        configurationSection.set("DistanceFromOthers", distanceFromOthers);
        if (xLimitation > 0)
            configurationSection.set("SpawnDistance.x", xLimitation);
        if (zLimitation > 0)
            configurationSection.set("SpawnDistance.z", zLimitation);
        if (spawnRegion != null) {
            configurationSection.set("SpawnRegion.CornerOne.x", spawnRegion.minCorner.getX());
            configurationSection.set("SpawnRegion.CornerOne.y", spawnRegion.minCorner.getY());
            configurationSection.set("SpawnRegion.CornerOne.z", spawnRegion.minCorner.getZ());
            configurationSection.set("SpawnRegion.CornerTwo.x", spawnRegion.maxCorner.getX());
            configurationSection.set("SpawnRegion.CornerTwo.y", spawnRegion.maxCorner.getY());
            configurationSection.set("SpawnRegion.CornerTwo.z", spawnRegion.maxCorner.getZ());
        }
    }

    /**
     * A record that represents a SpawnRegion.
     *
     * @param minCorner The minimum corner.
     * @param maxCorner The maximum corner.
     */
    public record SpawnRegion(Location minCorner, Location maxCorner) {
        /**
         * Creates a new spawn region that ensure minCorner and maxCorner are truly
         * the min and max corners.
         *
         * @return A new standardized SpawnRegion record.
         */
        public SpawnRegion standardize() {
            Location newMinCorner = new Location(null,
                    Math.min(minCorner.getX(), maxCorner.getX()),
                    Math.min(minCorner.getY(), maxCorner.getY()),
                    Math.min(minCorner.getZ(), maxCorner.getZ()));
            Location newMaxCorner = new Location(null,
                    Math.max(minCorner.getX(), maxCorner.getX()),
                    Math.max(minCorner.getY(), maxCorner.getY()),
                    Math.max(minCorner.getZ(), maxCorner.getZ()));

            return new SpawnRegion(newMinCorner, newMaxCorner);
        }

        /**
         * Check if location loc is in the region.
         * <p>Note: loc should be standardized for this check to work.</p>
         *
         * @param loc The location to check.
         * @return If loc is in the region.
         */
        public boolean isInRegion(Location loc) {
            return loc.getX() > minCorner.getX() &&
                    loc.getY() > minCorner.getY() &&
                    loc.getZ() > minCorner.getZ() &&
                    loc.getX() < maxCorner.getX() &&
                    loc.getY() < maxCorner.getY() &&
                    loc.getZ() < maxCorner.getZ();
        }
    }
}
