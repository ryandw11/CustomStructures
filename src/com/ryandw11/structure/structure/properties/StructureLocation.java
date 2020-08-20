package com.ryandw11.structure.structure.properties;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.structure.StructureBuilder;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class StructureLocation {

    private List<String> worlds;
    private StructureYSpawning spawnY;
    private List<String> biomes;

    public StructureLocation(StructureBuilder sb, FileConfiguration configuration){
        ConfigurationSection cs = configuration.getConfigurationSection("StructureLocation");
        if(cs == null){
            CustomStructures.getInstance().getLogger().severe("Invalid Structure format for:" + configuration.getName());
            CustomStructures.getInstance().getLogger().severe("StructureLocation is mandatory, please add one in for this file to be valid.");
            sb.setInvalid();
            return;
        }
        if(cs.contains("Worlds"))
            this.worlds = cs.getStringList("Worlds");
        else
            this.worlds = new ArrayList<>();
        this.spawnY = new StructureYSpawning(configuration);
        if(cs.contains("Biome"))
            this.biomes = cs.getStringList("Biome");
        else
            this.biomes = new ArrayList<>();
    }

    public StructureLocation(List<String> worlds, StructureYSpawning spawnSettings, List<String> biomes){
        this.worlds = worlds;
        this.spawnY = spawnSettings;
        this.biomes = biomes;
    }

    public StructureLocation(){
        this.worlds = new ArrayList<>();
        this.spawnY = new StructureYSpawning("top");
        this.biomes = new ArrayList<>();
    }

    public List<String> getWorlds(){
        return worlds;
    }

    public StructureYSpawning getSpawnSettings(){
        return spawnY;
    }

    public List<String> getBiomes(){
        return biomes;
    }

    public void setWorlds(List<String> worlds){
        this.worlds = worlds;
    }

    public void setSpawnSettings(StructureYSpawning spawnY){
        this.spawnY = spawnY;
    }

    public void setBiomes(List<String> biomes){
        this.biomes = biomes;
    }

    public boolean hasBiome(Biome b){
        if(biomes.isEmpty())
            return true;
        for(String biome : biomes){
            if(b.toString().toLowerCase().equals(biome.toLowerCase()))
                return true;
        }
        return false;
    }
}
