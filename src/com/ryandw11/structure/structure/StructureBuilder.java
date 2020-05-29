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

}
