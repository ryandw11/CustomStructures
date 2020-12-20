package com.ryandw11.structure.structure.properties;

import com.ryandw11.structure.exceptions.StructureConfigurationException;
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
    private int xLimitation;
    private int zLimitation;

    public StructureLocation(StructureBuilder sb, FileConfiguration configuration) {
        ConfigurationSection cs = configuration.getConfigurationSection("StructureLocation");
        if (cs == null)
            throw new StructureConfigurationException("The `StructureLocation` property is mandatory, please add one to the file for the " +
                    "structure to be valid.");
        if (cs.contains("Worlds"))
            this.worlds = cs.getStringList("Worlds");
        else
            this.worlds = new ArrayList<>();
        this.spawnY = new StructureYSpawning(configuration);
        if (cs.contains("Biome"))
            this.biomes = cs.getStringList("Biome");
        else
            this.biomes = new ArrayList<>();

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

    public StructureLocation(List<String> worlds, StructureYSpawning spawnSettings, List<String> biomes) {
        this.worlds = worlds;
        this.spawnY = spawnSettings;
        this.biomes = biomes;
        this.xLimitation = 0;
        this.zLimitation = 0;
    }

    public StructureLocation() {
        this(new ArrayList<>(), new StructureYSpawning("top", true), new ArrayList<>());
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public StructureYSpawning getSpawnSettings() {
        return spawnY;
    }

    public List<String> getBiomes() {
        return biomes;
    }

    public void setWorlds(List<String> worlds) {
        this.worlds = worlds;
    }

    public void setSpawnSettings(StructureYSpawning spawnY) {
        this.spawnY = spawnY;
    }

    public void setBiomes(List<String> biomes) {
        this.biomes = biomes;
    }

    public boolean hasBiome(Biome b) {
        if (biomes.isEmpty())
            return true;
        for (String biome : biomes) {
            if (b.toString().toLowerCase().equals(biome.toLowerCase()))
                return true;
        }
        return false;
    }

    public void setXLimitation(int x) {
        this.xLimitation = x;
    }

    public int getXLimitation() {
        return this.xLimitation;
    }

    public void setZLimitation(int z) {
        this.zLimitation = z;
    }

    public int getZLimitation() {
        return this.zLimitation;
    }
}
