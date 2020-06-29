package com.ryandw11.structure.structure.properties;

import com.ryandw11.structure.structure.StructureBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class StructureProperties {

    private boolean placeAir;
    private boolean randomRotation;
    private boolean ignorePlants;
    private boolean spawnInWater;
    private boolean spawnInLavaLakes;

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

    public StructureProperties(){
        this.placeAir = true;
        this.randomRotation = false;
        this.ignorePlants = true;
        this.spawnInWater = true;
        this.spawnInLavaLakes = true;
    }

    public boolean canPlaceAir(){
        return placeAir;
    }
    public void setPlaceAir(boolean placeAir){
        this.placeAir = placeAir;
    }

    public boolean isRandomRotation(){
        return randomRotation;
    }

    public void setRandomRotation(boolean randomRotation){
        this.randomRotation = randomRotation;
    }

    public boolean isIgnoringPlants(){
        return ignorePlants;
    }

    public void setIgnorePlants(boolean ignorePlants){
        this.ignorePlants = ignorePlants;
    }

    public boolean canSpawnInWater(){
        return spawnInWater;
    }

    public void setSpawnInWater(boolean spawnInWater){
        this.spawnInWater = spawnInWater;
    }

    public boolean canSpawnInLavaLakes(){
        return spawnInLavaLakes;
    }

    public void setSpawnInLavaLakes(boolean spawnInLavaLakes){
        this.spawnInLavaLakes = spawnInLavaLakes;
    }
}
