package com.ryandw11.structure.structure.properties;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class StructureLimitations {

    private List<String> whitelistSpawnBlocks;
    private BlockLevelLimit blockLevelLimit;
    private Map<Material, Material> blockReplacement;
    private double replacementBlocksDelay;

    public StructureLimitations(FileConfiguration configuration){
        if(!configuration.contains("StructureLimitations.whitelistSpawnBlocks"))
            whitelistSpawnBlocks = new ArrayList<>();
        else
            whitelistSpawnBlocks = configuration.getStringList("StructureLimitations.whitelistSpawnBlocks");

        this.blockLevelLimit = new BlockLevelLimit(configuration);

        replacementBlocksDelay = !configuration.contains("StructureLimitations.replacement_blocks_delay") ? 0
                : configuration.getDouble("StructureLimitations.replacement_blocks_delay");

        blockReplacement = new HashMap<>();
        if(configuration.contains("StructureLimitations.replacement_blocks")){
            for(String s : Objects.requireNonNull(configuration.getConfigurationSection("StructureLimitations.replacement_blocks")).getKeys(false)){
                Material firstMaterial = Material.getMaterial(s);
                Material secondMaterial = Material.getMaterial(Objects.requireNonNull(configuration.getString("StructureLimitations.replacement_blocks." + s)));
                blockReplacement.put(firstMaterial, secondMaterial);
            }
        }
    }

    public StructureLimitations(List<String> whitelistSpawnBlocks, BlockLevelLimit blockLevelLimit, Map<Material, Material> blockReplacement){
        this.whitelistSpawnBlocks = whitelistSpawnBlocks;
        this.blockLevelLimit = blockLevelLimit;
        this.blockReplacement = blockReplacement;
    }

    public List<String> getWhitelistBlocks(){
        return whitelistSpawnBlocks;
    }

    public boolean hasBlock(Block b){
        if(whitelistSpawnBlocks.isEmpty()) return true;
        for(String block : whitelistSpawnBlocks){
            if(block.equalsIgnoreCase(b.getType().toString()))
                return true;
        }
        return false;
    }

    public BlockLevelLimit getBlockLevelLimit(){
        return blockLevelLimit;
    }

    public Map<Material, Material> getBlockReplacement(){
        return blockReplacement;
    }

    public double getReplacementBlocksDelay(){
        return replacementBlocksDelay;
    }

    public void setReplacementBlocksDelay(double value){
        replacementBlocksDelay = value;
    }
}
