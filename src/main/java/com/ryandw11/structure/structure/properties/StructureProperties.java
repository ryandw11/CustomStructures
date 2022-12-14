package com.ryandw11.structure.structure.properties;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Handles the StructureProperties config section of the structure.
 */
public class StructureProperties {

    private boolean placeAir;
    private boolean randomRotation;
    private boolean ignorePlants;
    private boolean spawnInWater;
    private boolean spawnInLavaLakes;
    private boolean spawnInVoid;
    private boolean ignoreWater;

    /**
     * Create StructureProperties from a config file.
     *
     * @param configuration The configuration file.
     */
    public StructureProperties(FileConfiguration configuration) {
        ConfigurationSection cs = configuration.getConfigurationSection("StructureProperties");
        if (cs == null) {
            this.placeAir = true;
            this.randomRotation = false;
            this.ignorePlants = true;
            this.spawnInWater = true;
            this.spawnInLavaLakes = true;
            this.spawnInVoid = false;
            return;
        }
        this.placeAir = cs.contains("PlaceAir") && cs.getBoolean("PlaceAir");
        this.randomRotation = cs.contains("RandomRotation") && cs.getBoolean("RandomRotation");
        this.ignorePlants = cs.contains("IgnorePlants") && cs.getBoolean("IgnorePlants");
        this.spawnInWater = cs.contains("SpawnInWater") && cs.getBoolean("SpawnInWater");
        this.spawnInLavaLakes = cs.contains("SpawnInLavaLakes") && cs.getBoolean("SpawnInLavaLakes");
        this.spawnInVoid = cs.contains("SpawnInVoid") && cs.getBoolean("SpawnInVoid");
        this.ignoreWater = cs.contains("IgnoreWater") && cs.getBoolean("IgnoreWater");
    }

    /**
     * Create StructureProperties using default values.
     */
    public StructureProperties() {
        this.placeAir = true;
        this.randomRotation = false;
        this.ignorePlants = true;
        this.spawnInWater = true;
        this.spawnInLavaLakes = true;
        this.spawnInVoid = false;
        this.ignoreWater = false;
    }

    /**
     * If the structure will place air.
     *
     * @return if the structure will place air.
     */
    public boolean canPlaceAir() {
        return placeAir;
    }

    /**
     * Set if the structure should place air.
     *
     * @param placeAir If the structure should place air.
     */
    public void setPlaceAir(boolean placeAir) {
        this.placeAir = placeAir;
    }

    /**
     * If the structure is randomly rotated.
     *
     * @return If the structure is randomly rotated.
     */
    public boolean isRandomRotation() {
        return randomRotation;
    }

    /**
     * Set if the structure is randomly rotated.
     *
     * @param randomRotation If the structure is randomly rotated.
     */
    public void setRandomRotation(boolean randomRotation) {
        this.randomRotation = randomRotation;
    }

    /**
     * If the structure ignores plants.
     *
     * @return If the structure ignores plants.
     */
    public boolean isIgnoringPlants() {
        return ignorePlants;
    }

    /**
     * Set if the structure ignores plants.
     *
     * @param ignorePlants If the structure ignores plants.
     */
    public void setIgnorePlants(boolean ignorePlants) {
        this.ignorePlants = ignorePlants;
    }

    /**
     * If the structure can spawn in water.
     *
     * @return If the structure can spawn in water.
     */
    public boolean canSpawnInWater() {
        return spawnInWater;
    }

    /**
     * Set if the structure can spawn in water.
     *
     * @param spawnInWater If the structure can spawn in water.
     */
    public void setSpawnInWater(boolean spawnInWater) {
        this.spawnInWater = spawnInWater;
    }

    /**
     * If the structure can spawn in lava lakes.
     *
     * @return If the structure can spawn in lava lakes.
     */
    public boolean canSpawnInLavaLakes() {
        return spawnInLavaLakes;
    }

    /**
     * Set if the structure can spawn in lava lakes.
     *
     * @param spawnInLavaLakes If the structure can spawn in lava lakes.
     */
    public void setSpawnInLavaLakes(boolean spawnInLavaLakes) {
        this.spawnInLavaLakes = spawnInLavaLakes;
    }

    /**
     * Get if the structure can spawn in the void.
     *
     * @return If the structure can spawn in the void.
     */
    public boolean canSpawnInVoid() {
        return spawnInVoid;
    }

    /**
     * Set if the structure should spawn in the void.
     *
     * <p>Note: This option does nothing if spawning in the void is not enabled in the config file by the user.</p>
     *
     * @param spawnInVoid If the structure should spawn in the void.
     */
    public void setSpawnInVoid(boolean spawnInVoid) {
        this.spawnInVoid = spawnInVoid;
    }

    /**
     * Get if the structure should ignore water.
     *
     * @return If the structure should ignore water.
     */
    public boolean shouldIgnoreWater() {
        return ignoreWater;
    }

    /**
     * Set if the structure should ignore water.
     *
     * @param ignoreWater If the structure should ignore water.
     */
    public void setIgnoreWater(boolean ignoreWater) {
        this.ignoreWater = ignoreWater;
    }
}
