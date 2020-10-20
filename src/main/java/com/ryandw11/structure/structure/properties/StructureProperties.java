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

    /**
     * Create StructureProperties from a config file.
     * @param configuration The configuration file.
     */
    public StructureProperties(FileConfiguration configuration){
        ConfigurationSection cs = configuration.getConfigurationSection("StructureProperties");
        if(cs == null){
            this.placeAir = true;
            this.randomRotation = false;
            this.ignorePlants = true;
            this.spawnInWater = true;
            this.spawnInLavaLakes = true;
            return;
        }
        this.placeAir = cs.contains("PlaceAir") && cs.getBoolean("PlaceAir");
        this.randomRotation = cs.contains("randomRotation") && cs.getBoolean("randomRotation");
        this.ignorePlants = cs.contains("ignorePlants") && cs.getBoolean("ignorePlants");
        this.spawnInWater = cs.contains("spawnInWater") && cs.getBoolean("spawnInWater");
        this.spawnInLavaLakes = cs.contains("spawnInLavaLakes") && cs.getBoolean("spawnInLavaLakes");
    }

    /**
     * Create StructureProperties using default values.
     */
    public StructureProperties(){
        this.placeAir = true;
        this.randomRotation = false;
        this.ignorePlants = true;
        this.spawnInWater = true;
        this.spawnInLavaLakes = true;
    }

    /**
     * If the structure will place air.
     * @return if the structure will place air.
     */
    public boolean canPlaceAir(){
        return placeAir;
    }

    /**
     * Set if the structure should place air.
     * @param placeAir If the structure should place air.
     */
    public void setPlaceAir(boolean placeAir){
        this.placeAir = placeAir;
    }

    /**
     * If the structure is randomly rotated.
     * @return If the structure is randomly rotated.
     */
    public boolean isRandomRotation(){
        return randomRotation;
    }

    /**
     * Set if the structure is randomly rotated.
     * @param randomRotation If the structure is randomly rotated.
     */
    public void setRandomRotation(boolean randomRotation){
        this.randomRotation = randomRotation;
    }

    /**
     * If the structure ignores plants.
     * @return If the structure ignores plants.
     */
    public boolean isIgnoringPlants(){
        return ignorePlants;
    }

    /**
     * Set if the structure ignores plants.
     * @param ignorePlants If the structure ignores plants.
     */
    public void setIgnorePlants(boolean ignorePlants){
        this.ignorePlants = ignorePlants;
    }

    /**
     * If the structure can spawn in water.
     * @return If the structure can spawn in water.
     */
    public boolean canSpawnInWater(){
        return spawnInWater;
    }

    /**
     * Set if the structure can spawn in water.
     * @param spawnInWater If the structure can spawn in water.
     */
    public void setSpawnInWater(boolean spawnInWater){
        this.spawnInWater = spawnInWater;
    }

    /**
     * If the structure can spawn in lava lakes.
     * @return If the structure can spawn in lava lakes.
     */
    public boolean canSpawnInLavaLakes(){
        return spawnInLavaLakes;
    }

    /**
     * Set if the structure can spawn in lava lakes.
     * @param spawnInLavaLakes If the structure can spawn in lava lakes.
     */
    public void setSpawnInLavaLakes(boolean spawnInLavaLakes){
        this.spawnInLavaLakes = spawnInLavaLakes;
    }
}
