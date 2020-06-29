package com.ryandw11.structure.structure;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.loottables.LootTable;
import com.ryandw11.structure.structure.properties.StructureLimitations;
import com.ryandw11.structure.structure.properties.StructureLocation;
import com.ryandw11.structure.structure.properties.StructureProperties;
import com.ryandw11.structure.utils.RandomCollection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StructureBuilder {

    private FileConfiguration config;

    protected String name;
    protected String schematic;
    protected int chanceNumber;
    protected int chanceOutOf;
    protected StructureLocation structureLocation;
    protected StructureProperties structureProperties;
    protected StructureLimitations structureLimitations;
    protected RandomCollection<LootTable> lootTables;

    private boolean invalid;

    public StructureBuilder(String name, String schematic){
        this.name = name;
        this.schematic = schematic;
        lootTables = new RandomCollection<>();
    }


    public StructureBuilder(String name, File file){
        if(!file.exists())
            throw new RuntimeException("Cannot build structure: That file does not exist!");
        config = YamlConfiguration.loadConfiguration(file);

        invalid = false;

        this.name = name;

        if(!checkValidity())
            return;

        schematic = config.getString("schematic");
        chanceNumber = config.getInt("Chance.Number");
        chanceOutOf = config.getInt("Chance.OutOf");

        structureLocation = new StructureLocation(this, config);
        structureProperties = new StructureProperties(config);
        structureLimitations = new StructureLimitations(config);
        lootTables = new RandomCollection<>();
        if(config.contains("LootTables")){
            ConfigurationSection lootableConfig = config.getConfigurationSection("LootTables");
            assert lootableConfig != null;
            for (String lootTable : lootableConfig.getKeys(false)) {
                int weight = lootableConfig.getInt(lootTable);
                lootTables.add(weight, CustomStructures.getInstance().getLootTableHandler().getLootTableByName(lootTable));
            }
        }
    }

    private boolean checkValidity(){
        if(!config.contains("schematic")){
            CustomStructures.getInstance().getLogger().severe("Invalid Structure format for:" + config.getName());
            CustomStructures.getInstance().getLogger().severe("Schematic is mandatory, please add one in for this file to be valid.");
            setInvalid();
            return false;
        }

        if(!config.contains("Chance.Number")){
            CustomStructures.getInstance().getLogger().severe("Invalid Structure format for:" + config.getName());
            CustomStructures.getInstance().getLogger().severe("Chance.Number is mandatory, please add one in for this file to be valid.");
            setInvalid();
            return false;
        }
        if(!config.contains("Chance.OutOf")){
            CustomStructures.getInstance().getLogger().severe("Invalid Structure format for:" + config.getName());
            CustomStructures.getInstance().getLogger().severe("Chance.OutOf is mandatory, please add one in for this file to be valid.");
            setInvalid();
            return false;
        }
        return true;
    }

    public void setChance(int number, int outOf){
        this.chanceNumber = number;
        this.chanceOutOf = outOf;
    }

    public void setStructureLimitations(StructureLimitations limitations){
        this.structureLimitations = limitations;
    }

    public void setStructureProperties(StructureProperties properties){
        this.structureProperties = properties;
    }

    public void setStructureLocation(StructureLocation location){
        this.structureLocation = location;
    }

    public void setLootTables(ConfigurationSection lootableConfig){
        lootTables = new RandomCollection<>();
        assert lootableConfig != null;
        for (String lootTable : lootableConfig.getKeys(false)) {
            int weight = lootableConfig.getInt(lootTable);
            lootTables.add(weight, CustomStructures.getInstance().getLootTableHandler().getLootTableByName(lootTable));
        }
    }

    public void setLootTables(RandomCollection<LootTable> lootTables){
        this.lootTables = lootTables;
    }

    public void setInvalid(){
        invalid = true;
    }

    /**
     * Build the structure.
     * @return The structure. (Null if the file is invalid).
     */
    public Structure build(){
        if(invalid)
            return null;
        return new Structure(this);
    }

    public void save(File file) throws IOException {
        file.createNewFile();
        config = YamlConfiguration.loadConfiguration(file);
        config.set("schematic", schematic);
        config.set("Chance.Number", chanceNumber);
        config.set("Chance.OutOf", chanceOutOf);

        config.set("StructureLocation.Worlds", structureLocation.getWorlds());
        config.set("StructureLocation.SpawnY", structureLocation.getSpawnSettings().getValue());
        config.set("StructureLocation.Biome", structureLocation.getBiomes());

        config.set("StructureProperties.PlaceAir", structureProperties.canPlaceAir());
        config.set("StructureProperties.randomRotation", structureProperties.isRandomRotation());
        config.set("StructureProperties.ignorePlants", structureProperties.isIgnoringPlants());
        config.set("StructureProperties.spawnInWater", structureProperties.canSpawnInWater());
        config.set("StructureProperties.spawnInLavaLakes", structureProperties.canSpawnInLavaLakes());

        config.set("StructureLimitations.whitelistSpawnBlocks", structureLimitations.getWhitelistBlocks());

        for(Map.Entry<Double, LootTable> entry : lootTables.getMap().entrySet()){
            config.set("LootTables." + entry.getValue().getName(), entry.getKey());
        }
        config.save(file);
    }

}
